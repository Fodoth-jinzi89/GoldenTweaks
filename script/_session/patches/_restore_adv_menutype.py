"""Restore the advanced machine's menu-type override.

The override was lost in a later cleanup, so `createMenu` fell back to the basic kettle's type.
The menu type is what the client maps to a screen class: the server named the basic type, the
client opened `GTChemicalReactorScreen` against a menu it had registered as the advanced one,
and the mismatch is what closes the window immediately.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTAdvancedChemicalReactorBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

# duplicate import: RecipeType appears twice
s = s.replace("""import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.crafting.RecipeType;
""", """import net.minecraft.world.level.block.Block;
""", 1)

s = s.replace('''    @Override
    protected RecipeType<GTChemicalReactionRecipe> recipeType() {
        return GTAdvancedChemicalReactionRecipes.TYPE.get();
    }''',
'''    @Override
    protected RecipeType<GTChemicalReactionRecipe> recipeType() {
        return GTAdvancedChemicalReactionRecipes.TYPE.get();
    }

    /**
     * The advanced window, not the basic one.
     *
     * <p>The menu type is the client's cue for which screen to build. Without this override the
     * server opened the basic type, so the client had no screen for the menu it was handed and the
     * window closed on the tick it opened.</p>
     */
    @Override
    protected MenuType<?> menuType() {
        return GTAdvancedChemicalReactorMultiblock.MENU.get();
    }''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("menuType override restored; duplicate import removed")
print("RecipeType imports:", s.count("import net.minecraft.world.item.crafting.RecipeType;"))
