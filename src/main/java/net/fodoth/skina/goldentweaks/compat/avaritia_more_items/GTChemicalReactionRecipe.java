package net.fodoth.skina.goldentweaks.compat.avaritia_more_items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.recipe.IMultiblockRecipe;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.recipe.MultiblockRecipeInput;

import java.util.List;

/**
 * One reaction performed by the chemical kettle.
 *
 * <p>AMI ships recipe types for its own stations (compressor, roller press, …) but none for a
 * generic reaction, so the kettle declares its own. Everything that makes a recipe usable by an AMI
 * multiblock — {@code matches}, {@code assemble}, {@code getResultItem} — already has a working
 * default on {@link IMultiblockRecipe}; only the data and the timing are supplied here.</p>
 *
 * <p>Both sides are plain lists of {@link GTReactionStack}, so any number of items, fluids,
 * Mekanism chemicals and other AE keys can appear on either side. AMI's own interface only speaks
 * items and fluids — {@link #getItemInputs()} and friends therefore expose the matching subset,
 * and the machine's own tick path reads the full lists. See {@link GTReactionStacks} for the
 * conversions.</p>
 *
 * <p>Energy is charged per tick, not up front: {@code processingTime} is the number of ticks the
 * craft runs, and {@code energyPerTick} is taken from the input ports on each of them. The total
 * cost is therefore {@code processingTime * energyPerTick}.</p>
 */
public class GTChemicalReactionRecipe implements IMultiblockRecipe {

    public static final String ID = "chemical_reaction";

    private final List<GTReactionStack> inputs;
    private final List<GTReactionStack> outputs;
    private final int processingTime;
    private final int energyPerTick;

    /** The mode this reaction belongs to; {@link #ANY_MODE} matches every port mode. */
    private final int mode;

    /** The wildcard mode: a recipe carrying it runs under any port mode. */
    public static final int ANY_MODE = 0;

    public GTChemicalReactionRecipe(List<GTReactionStack> inputs,
                                    List<GTReactionStack> outputs,
                                    int processingTime,
                                    int energyPerTick) {
        this(inputs, outputs, processingTime, energyPerTick, ANY_MODE);
    }

    public GTChemicalReactionRecipe(List<GTReactionStack> inputs,
                                    List<GTReactionStack> outputs,
                                    int processingTime,
                                    int energyPerTick,
                                    int mode) {
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.processingTime = processingTime;
        this.energyPerTick = energyPerTick;
        this.mode = mode;
    }

    /**
     * The mode this reaction belongs to.
     *
     * <p>Ports declare which modes they serve, and the machine works through those modes in the
     * order its ports are ranked. A recipe marked {@link #ANY_MODE} skips that ranking and is
     * eligible under every mode.</p>
     */
    public int mode() {
        return this.mode;
    }

    /** Every input, across all kinds. */
    public List<GTReactionStack> inputs() {
        return this.inputs;
    }

    /** Every output, across all kinds. */
    public List<GTReactionStack> outputs() {
        return this.outputs;
    }

    /** Total energy the reaction draws over its whole run. */
    public long totalEnergy() {
        return (long) this.processingTime * (long) this.energyPerTick;
    }

    // --- AMI's item/fluid-only view -------------------------------------------------------
    //
    // The base interface predates the extra kinds and is what AMI's own matcher and port code
    // reads. Only the matching subset is reported so nothing downstream sees a stack it cannot
    // handle; the machine's tick path uses inputs()/outputs() directly.

    @Override
    public List<ItemStack> getItemInputs() {
        return this.inputs.stream()
                .filter(stack -> stack.kind() == GTReactionStack.Kind.ITEM)
                .map(GTReactionStack::toItemStack)
                .toList();
    }

    @Override
    public List<FluidStack> getFluidInputs() {
        return this.inputs.stream()
                .filter(stack -> stack.kind() == GTReactionStack.Kind.FLUID)
                .map(GTReactionStack::toFluidStack)
                .toList();
    }

    @Override
    public List<ItemStack> getItemOutputs() {
        return this.outputs.stream()
                .filter(stack -> stack.kind() == GTReactionStack.Kind.ITEM)
                .map(GTReactionStack::toItemStack)
                .toList();
    }

    @Override
    public List<FluidStack> getFluidOutputs() {
        return this.outputs.stream()
                .filter(stack -> stack.kind() == GTReactionStack.Kind.FLUID)
                .map(GTReactionStack::toFluidStack)
                .toList();
    }

    @Override
    public int getProcessingTime() {
        return this.processingTime;
    }

    @Override
    public int getEnergyPerTick() {
        return this.energyPerTick;
    }

    /** Flattened ingredients, so JEI/EMI and AMI's own bookkeeping can enumerate the item inputs. */
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (GTReactionStack stack : this.inputs) {
            if (stack.kind() != GTReactionStack.Kind.ITEM) {
                continue;
            }
            ItemStack item = stack.toItemStack();
            if (!item.isEmpty()) {
                // A stack stands for itself; repeating it would inflate the ingredient count
                // without changing what the recipe accepts.
                ingredients.add(Ingredient.of(item.getItem()));
            }
        }
        return ingredients;
    }

    @Override
    public boolean matches(MultiblockRecipeInput input, Level level) {
        // Delegate to AMI's matcher rather than comparing hand-rolled: it already accounts for
        // stack counts, fluid amounts and partially filled ports.
        return IMultiblockRecipe.super.matches(input, level);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack assemble(MultiblockRecipeInput input, HolderLookup.Provider registries) {
        return this.outputs.stream()
                .filter(stack -> stack.kind() == GTReactionStack.Kind.ITEM)
                .map(GTReactionStack::toItemStack)
                .findFirst()
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return assemble(MultiblockRecipeInput.EMPTY, registries);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return GTChemicalReactionRecipes.SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return GTChemicalReactionRecipes.TYPE.get();
    }

    /** Datapack shape: {@code inputs}, {@code outputs}, {@code processing_time}, {@code energy_per_tick}, {@code mode}. */
    public static final class Serializer implements RecipeSerializer<GTChemicalReactionRecipe> {

        private static final MapCodec<GTChemicalReactionRecipe> CODEC =
                RecordCodecBuilder.mapCodec(instance -> instance.group(
                        GTReactionStack.CODEC.listOf()
                                .fieldOf("inputs").forGetter(GTChemicalReactionRecipe::inputs),
                        GTReactionStack.CODEC.listOf()
                                .optionalFieldOf("outputs", List.of())
                                .forGetter(GTChemicalReactionRecipe::outputs),
                        Codec.INT.optionalFieldOf("processing_time", 40)
                                .forGetter(GTChemicalReactionRecipe::getProcessingTime),
                        Codec.INT.optionalFieldOf("energy_per_tick", 0)
                                .forGetter(GTChemicalReactionRecipe::getEnergyPerTick),
                        Codec.INT.optionalFieldOf("mode", GTChemicalReactionRecipe.ANY_MODE)
                                .forGetter(GTChemicalReactionRecipe::mode)
                ).apply(instance, GTChemicalReactionRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, GTChemicalReactionRecipe> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public GTChemicalReactionRecipe decode(RegistryFriendlyByteBuf buffer) {
                        List<GTReactionStack> inputs = buffer.readList(GTReactionStack.STREAM_CODEC);
                        List<GTReactionStack> outputs = buffer.readList(GTReactionStack.STREAM_CODEC);
                        return new GTChemicalReactionRecipe(inputs, outputs,
                                buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
                    }

                    @Override
                    public void encode(RegistryFriendlyByteBuf buffer, GTChemicalReactionRecipe recipe) {
                        buffer.writeCollection(recipe.inputs(), GTReactionStack.STREAM_CODEC);
                        buffer.writeCollection(recipe.outputs(), GTReactionStack.STREAM_CODEC);
                        buffer.writeVarInt(recipe.getProcessingTime());
                        buffer.writeVarInt(recipe.getEnergyPerTick());
                        buffer.writeVarInt(recipe.mode());
                    }
                };

        @Override
        public MapCodec<GTChemicalReactionRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GTChemicalReactionRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
