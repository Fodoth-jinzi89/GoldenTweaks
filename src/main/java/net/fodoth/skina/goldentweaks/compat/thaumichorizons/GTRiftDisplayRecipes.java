package net.fodoth.skina.goldentweaks.compat.thaumichorizons;

import com.kentington.thaumichorizons.common.animation.AnimationRegistry;
import com.kentington.thaumichorizons.common.planar.PlanarRegistry;
import com.kentington.thaumichorizons.common.putty.VoidPuttyRegistry;
import com.kentington.thaumichorizons.common.wand.WandRegistry;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import thaumcraft.common.registry.TCItems;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything the recipe viewers show for the Thaumic Horizons rift: the built-in recipes that
 * Thaumic Horizons resolves inside {@code VortexBlockEntity#craft} plus every
 * {@code goldentweaks:rift_crafting} recipe loaded from JSON.
 */
public final class GTRiftDisplayRecipes {

    /* Page layout, shared by the JEI and EMI pages. */
    public static final int WIDTH = 104;
    public static final int HEIGHT = 44;
    public static final int INPUT_X = 2;
    public static final int OUTPUT_X = 84;
    public static final int ROW_Y = 9;
    /** Single conversion arrow, centred between both slots. */
    public static final int ARROW_X = 40;
    public static final int ARROW_Y = 10;
    public static final int ARROW_WIDTH = 24;
    public static final int ARROW_HEIGHT = 17;
    /** EMI animates widgets in milliseconds (see AnimatedTextureWidget). */
    public static final int ARROW_DURATION = 1000;
    public static final int NOTE_X = 2;
    public static final int NOTE_Y = 32;

    private static final String BUILT_IN = "builtin/";

    private GTRiftDisplayRecipes() {
    }

    public static ItemStack riftIcon() {
        return new ItemStack(PlanarRegistry.VORTEX_ITEM.get());
    }

    public static List<GTRiftDisplayRecipe> all() {
        List<GTRiftDisplayRecipe> recipes = new ArrayList<>();

        // Thaumic Horizons' own hard-coded rift recipes.
        add(recipes, "primordial_pearl", new ItemStack(TCItems.PRIMORDIAL_PEARL.get()),
                ItemStack.EMPTY, "note.pocket_plane");
        add(recipes, "golem_powder", new ItemStack(AnimationRegistry.POWDER.get()),
                ItemStack.EMPTY, "note.voidling_golem");
        add(recipes, "zombie_brain", new ItemStack(TCItems.ZOMBIE_BRAIN.get()),
                ItemStack.EMPTY, "note.wisp");
        add(recipes, "void_seed", new ItemStack(TCItems.VOID_SEED.get()),
                new ItemStack(VoidPuttyRegistry.PUTTY.get()), null);
        add(recipes, "inert_wand", new ItemStack(WandRegistry.INERT.get()),
                WandRegistry.DISPOSABLE.get().creativeTabStack(), null);

        // Recipes added through data/goldentweaks/recipe/thaumichorizons/rift_crafting/.
        for (GTRiftRecipe recipe : GTRiftRecipe.entries()) {
            recipes.add(new GTRiftDisplayRecipe(recipe.id(), recipe.input(), recipe.result(), null));
        }

        return recipes;
    }

    private static void add(
            List<GTRiftDisplayRecipe> recipes,
            String path,
            ItemStack input,
            ItemStack output,
            @Nullable String noteKey
    ) {
        recipes.add(new GTRiftDisplayRecipe(
                ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "rift_crafting/" + BUILT_IN + path),
                input,
                output,
                noteKey == null ? null : Component.translatable("goldentweaks.rift_crafting." + noteKey)
        ));
    }
}
