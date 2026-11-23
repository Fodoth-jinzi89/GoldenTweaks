package net.fodoth.skina.goldentweaks.compat.avaritia_more_items;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Recipe type and serializer for the chemical kettle's reactions.
 *
 * <p>Registered from the compat package, so nothing here is touched unless
 * {@code avaritia_more_items} is installed — the recipe implements AMI's interface, which would not
 * resolve otherwise.</p>
 */
public final class GTChemicalReactionRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, GoldenTweaks.MODID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, GoldenTweaks.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<GTChemicalReactionRecipe>> TYPE =
            TYPES.register(GTChemicalReactionRecipe.ID,
                    () -> RecipeType.simple(
                            ResourceLocation.fromNamespaceAndPath(
                                    GoldenTweaks.MODID, GTChemicalReactionRecipe.ID)));

    // Declared after TYPE: the serializer names it, and a field initialiser may not forward-reference.
    public static final DeferredHolder<RecipeSerializer<?>,
            GTChemicalReactionRecipe.Serializer> SERIALIZER =
            SERIALIZERS.register(GTChemicalReactionRecipe.ID,
                    () -> new GTChemicalReactionRecipe.Serializer(TYPE.get()));

    private GTChemicalReactionRecipes() {
    }

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
