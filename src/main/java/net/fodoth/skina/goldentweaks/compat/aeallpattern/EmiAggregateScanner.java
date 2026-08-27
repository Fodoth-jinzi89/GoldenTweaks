package net.fodoth.skina.goldentweaks.compat.aeallpattern;

import appeng.api.stacks.GenericStack;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeManager;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.nolij.toomanyrecipeviewers.impl.ingredient.TMRVStack;
import io.github.langqi99.aeallpattern.aggregate.AggregateInputSlot;
import io.github.langqi99.aeallpattern.aggregate.AggregatePatternKind;
import io.github.langqi99.aeallpattern.aggregate.AggregateRecipe;
import io.github.langqi99.aeallpattern.network.GenerateAggregatePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public final class EmiAggregateScanner {
    private static final int MAX_RECIPES = 16384;
    private static final int PAGE_SIZE = 128;
    private static final AtomicBoolean SCAN_RUNNING = new AtomicBoolean();

    private EmiAggregateScanner() {
    }

    public static boolean scan(BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return true;
        }

        Block block = minecraft.level.getBlockState(pos).getBlock();
        ItemStack machine = block.asItem().getDefaultInstance();
        if (machine.isEmpty()) {
            return false;
        }

        EmiRecipeManager manager = EmiApi.getRecipeManager();
        EmiStack machineStack = EmiStack.of(machine);
        List<EmiRecipeCategory> categories = manager.getCategories().stream()
                .filter(category -> isCraftingMachine(machine, category)
                        || manager.getWorkstations(category).stream().anyMatch(workstation -> workstation.getEmiStacks().stream()
                        .anyMatch(stack -> stack.isEqual(machineStack))))
                .toList();
        if (categories.isEmpty()) {
            return false;
        }

        List<EmiRecipe> candidates = categories.stream()
                .flatMap(category -> manager.getRecipes(category).stream())
                .limit(MAX_RECIPES * 2L)
                .toList();
        if (candidates.isEmpty() || !SCAN_RUNNING.compareAndSet(false, true)) {
            return false;
        }

        ResourceLocation catalystId = BuiltInRegistries.BLOCK.getKey(block);
        String machineName = block.getDescriptionId();
        CompletableFuture.runAsync(() -> buildAndSend(pos, catalystId, machineName, candidates))
                .whenComplete((ignored, error) -> SCAN_RUNNING.set(false));
        return true;
    }

    private static void buildAndSend(BlockPos pos, ResourceLocation catalystId, String machineName,
                                     List<EmiRecipe> candidates) {
        List<AggregateRecipe> recipes = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (EmiRecipe recipe : candidates) {
            toAggregate(recipe, ids).ifPresent(recipes::add);
            if (recipes.size() >= MAX_RECIPES) {
                break;
            }
        }
        if (recipes.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            UUID uploadId = UUID.randomUUID();
            int pageCount = (recipes.size() + PAGE_SIZE - 1) / PAGE_SIZE;
            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                int from = pageIndex * PAGE_SIZE;
                int to = Math.min(recipes.size(), from + PAGE_SIZE);
                PacketDistributor.sendToServer(new GenerateAggregatePayload(uploadId, pos, catalystId,
                        machineName, pageIndex, pageCount, recipes.size(), recipes.subList(from, to)));
            }
        });
    }

    private static boolean isCraftingMachine(ItemStack machine, EmiRecipeCategory category) {
        return category == VanillaEmiRecipeCategories.CRAFTING
                && (machine.is(Blocks.CRAFTING_TABLE.asItem())
                || BuiltInRegistries.ITEM.getKey(machine.getItem()).toString().equals("ae2:molecular_assembler"));
    }

    private static Optional<AggregateRecipe> toAggregate(EmiRecipe recipe, Set<String> ids) {
        if (recipe.getInputs().isEmpty() || recipe.getInputs().size() > 9 || recipe.getOutputs().isEmpty()) {
            return Optional.empty();
        }

        int alternativesPerSlot = Math.max(1, 512 / recipe.getInputs().size());
        List<AggregateInputSlot> inputSlots = recipe.getInputs().stream()
                .map(ingredient -> toInputSlot(ingredient, alternativesPerSlot))
                .flatMap(Optional::stream)
                .toList();
        List<GenericStack> outputs = recipe.getOutputs().stream()
                .map(EmiAggregateScanner::toGenericStack)
                .flatMap(Optional::stream)
                .limit(3)
                .toList();
        if (inputSlots.size() != recipe.getInputs().size() || outputs.isEmpty()) {
            return Optional.empty();
        }

        RecipeHolder<?> backingRecipe = recipe.getBackingRecipe();
        ResourceLocation recipeId = backingRecipe == null ? recipe.getId() : backingRecipe.id();
        if (recipeId == null) {
            return Optional.empty();
        }
        String patternId = recipe.getCategory().getId() + "/" + recipeId;
        if (!ids.add(patternId)) {
            return Optional.empty();
        }
        return Optional.of(new AggregateRecipe(patternId, recipeId, patternKind(backingRecipe),
                inputSlots.stream().map(AggregateInputSlot::primary).toList(), inputSlots, outputs, 1));
    }

    private static Optional<AggregateInputSlot> toInputSlot(EmiIngredient ingredient, int limit) {
        List<GenericStack> alternatives = ingredient.getEmiStacks().stream()
                .map(stack -> toGenericStack(stack.copy().setAmount(ingredient.getAmount())))
                .flatMap(Optional::stream)
                .limit(limit)
                .toList();
        return alternatives.isEmpty()
                ? Optional.empty()
                : Optional.of(new AggregateInputSlot(alternatives, Optional.empty()));
    }

    private static Optional<GenericStack> toGenericStack(EmiStack stack) {
        if (stack.isEmpty() || stack.getAmount() <= 0) {
            return Optional.empty();
        }
        ItemStack itemStack = stack.getItemStack();
        if (!itemStack.isEmpty()) {
            itemStack.setCount((int) Math.min(Integer.MAX_VALUE, stack.getAmount()));
            return Optional.ofNullable(GenericStack.fromItemStack(itemStack));
        }
        if (stack.getKey() instanceof Fluid fluid) {
            return Optional.ofNullable(GenericStack.fromFluidStack(new FluidStack(fluid,
                    (int) Math.min(Integer.MAX_VALUE, stack.getAmount()))));
        }
        if (stack instanceof TMRVStack<?> tmrv) {
            return convertRegistered(tmrv);
        }
        return Optional.empty();
    }

    private static Optional<GenericStack> convertRegistered(TMRVStack<?> stack) {
        try {
            Class<?> converters = Class.forName("tamaized.ae2jeiintegration.api.integrations.jei.IngredientConverters");
            Method getConverter = converters.getMethod("getConverter", mezz.jei.api.ingredients.IIngredientType.class);
            Object converter = getConverter.invoke(null, stack.type);
            if (converter == null) {
                return Optional.empty();
            }
            Class<?> converterType = Class.forName("tamaized.ae2jeiintegration.api.integrations.jei.IngredientConverter");
            Method convert = converterType.getMethod("getStackFromIngredient", Object.class);
            GenericStack result = (GenericStack) convert.invoke(converter, stack.ingredient);
            return result == null ? Optional.empty() : Optional.of(new GenericStack(result.what(), stack.getAmount()));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static AggregatePatternKind patternKind(RecipeHolder<?> backingRecipe) {
        if (backingRecipe != null && backingRecipe.value() instanceof CraftingRecipe) {
            return AggregatePatternKind.CRAFTING;
        }
        if (backingRecipe != null && backingRecipe.value() instanceof StonecutterRecipe) {
            return AggregatePatternKind.STONECUTTING;
        }
        if (backingRecipe != null && backingRecipe.value() instanceof SmithingRecipe) {
            return AggregatePatternKind.SMITHING;
        }
        return AggregatePatternKind.PROCESSING;
    }
}
