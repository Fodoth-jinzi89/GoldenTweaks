package net.fodoth.skina.goldentweaks.content.multiblock.vacuum_cooler;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTChemicalReactionRecipe;

/**
 * Recipe type for the vacuum cooler.
 *
 * <p>Its own type rather than a filter over the basic kettle's: the cooler runs the cooling
 * reactions, and a data pack should be able to target one machine without the other. The serializer
 * is shared — the JSON shape is identical, only the type differs, exactly as the advanced kettle
 * does it.</p>
 */
public final class GTVacuumCoolingRecipes {

    /** Directory under {@code data/<namespace>/recipe/} that these files live in. */
    public static final String DIRECTORY = "vacuum_cooling";

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

    private GTVacuumCoolingRecipes() {
    }

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
