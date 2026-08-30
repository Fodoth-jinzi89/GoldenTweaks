package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.aspects.ItemAspectRegistry;
import thaumcraft.integration.jei.ThaumcraftJeiPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

@Mixin(value = ThaumcraftJeiPlugin.class, remap = false)
public abstract class ThaumcraftJeiPluginMixin {

    @Unique
    private static Constructor<?> recipeConstructor;

    @Unique
    private static Method createAspectPhialMethod;

    @Unique
    private static boolean hasSafeJeiUid(IStackHelper stackHelper, ItemStack stack) {
        try {
            stackHelper.getUidForStack(stack, UidContext.Recipe);
            stackHelper.getUidForStack(stack, UidContext.Ingredient);
            return true;
        } catch (LinkageError | RuntimeException error) {
            return false;
        }
    }

    /**
     * @author GoldenTweaks
     * @reason Optimize aspect source collection
     */
    @Overwrite
    private static List<?> collectAspectSources(IStackHelper stackHelper) {
        Map<Aspect, List<ItemStack>> aspectSources = new HashMap<>();

        for (var item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;

            ItemStack stack = item.getDefaultInstance();
            if (!hasSafeJeiUid(stackHelper, stack)) continue;

            var aspects = ItemAspectRegistry.getObjectTags(stack);

            for (Aspect aspect : Aspect.ordered()) {
                if (aspects.amount(aspect) > 0) {
                    aspectSources.computeIfAbsent(aspect, a -> new ArrayList<>()).add(stack.copy());
                }
            }
        }

        ArrayList<Object> recipes = new ArrayList<>();

        for (Aspect aspect : Aspect.ordered()) {
            List<ItemStack> sources = aspectSources.get(aspect);
            if (sources == null || sources.isEmpty()) continue;

            sources.sort(Comparator.comparing(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()));

            ItemStack aspectStack = invokeCreateAspectPhial(aspect);
            int pageCount = (sources.size() + 27) / 28;
            int maxPage = GoldenTweaksCommonConfig.getTCJEIAspectMaxPage();

            for (int page = 0; page < pageCount && page < maxPage; page++) {
                int start = page * 28;
                int end = Math.min(start + 28, sources.size());

                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                        "thaumcraft",
                        "aspect_sources/" + aspect.tag() + "/" + (page + 1)
                );

                recipes.add(createRecipe(
                        id,
                        aspect,
                        aspectStack.copy(),
                        List.copyOf(sources.subList(start, end)),
                        sources,
                        page + 1,
                        Math.min(pageCount, maxPage)
                ));
            }
        }

        return List.copyOf(recipes);
    }

    @Unique
    private static Object createRecipe(ResourceLocation id, Aspect aspect, ItemStack aspectStack,
                                       List<ItemStack> sources, List<ItemStack> allSources,
                                       int page, int pageCount) {
        try {
            if (recipeConstructor == null) {
                Class<?> clazz = Class.forName("thaumcraft.integration.jei.JeiAspectSourceRecipe");
                recipeConstructor = clazz.getDeclaredConstructor(
                        ResourceLocation.class, Aspect.class, ItemStack.class,
                        List.class, int.class
                );
                recipeConstructor.setAccessible(true);
            }
            return recipeConstructor.newInstance(id, aspect, aspectStack,
                    List.of(List.copyOf(sources)), allSources.size());
        } catch (Exception e) {
            throw new RuntimeException("Failed creating JeiAspectSourceRecipe", e);
        }
    }

    @Unique
    private static ItemStack invokeCreateAspectPhial(Aspect aspect) {
        try {
            if (createAspectPhialMethod == null) {
                createAspectPhialMethod = ThaumcraftJeiPlugin.class.getDeclaredMethod("createAspectPhial", Aspect.class);
                createAspectPhialMethod.setAccessible(true);
            }
            return (ItemStack) createAspectPhialMethod.invoke(null, aspect);
        } catch (Exception e) {
            throw new RuntimeException("Failed invoking createAspectPhial", e);
        }
    }
}
