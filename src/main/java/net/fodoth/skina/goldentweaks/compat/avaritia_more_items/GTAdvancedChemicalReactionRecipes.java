package net.fodoth.skina.goldentweaks.compat.avaritia_more_items;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.resources.ResourceLocation;

/**
 * Recipe type for the advanced kettle.
 *
 * <p>A separate type, not a filter over the basic one: the two machines hold different recipe
 * sets, and giving each its own registry entry is what lets a data pack target one without the
 * other. The serializer is shared — the JSON shape is identical, only the type differs.</p>
 */
public final class GTAdvancedChemicalReactionRecipes {

    /** Directory under {@code data/<namespace>/recipe/} that these files live in. */
    public static final String DIRECTORY = "advanced_chemical_reaction";

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, GoldenTweaks.MODID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, GoldenTweaks.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<GTChemicalReactionRecipe>> TYPE =
            TYPES.register(DIRECTORY,
                    () -> RecipeType.simple(
                            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, DIRECTORY)));

    // Declared after TYPE: the serializer names it, and a field initialiser may not forward-reference.
    @SuppressWarnings("unused")
    public static final DeferredHolder<RecipeSerializer<?>,
            GTChemicalReactionRecipe.Serializer> SERIALIZER =
            SERIALIZERS.register(DIRECTORY,
                    () -> new GTChemicalReactionRecipe.Serializer(TYPE.get()));

    private GTAdvancedChemicalReactionRecipes() {
    }

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
