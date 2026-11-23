"""Let a subclass choose its own menu type.

`createMenu` built a menu with the basic kettle's type regardless of which machine was opening
it. The menu type is what the client uses to pick a screen class, so the advanced machine opened
a window the client had registered under a different type — which is why its GUI failed to show.

Two hooks: the menu type itself, and how the menu is constructed with it.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''        return new GTChemicalReactorMenu(containerId, inventory, this);
    }''',
'''        return GTChemicalReactorMenu.create(menuType(), containerId, inventory, this);
    }

    /**
     * The window type this machine opens.
     *
     * <p>A hook because the menu type decides which screen the client builds: opening the advanced
     * kettle with the basic type would ask for a window registered under a different id, and the
     * GUI would not appear.</p>
     */
    protected MenuType<?> menuType() {
        return GTChemicalReactorMultiblock.MENU.get();
    }''', 1)

if "import net.minecraft.world.inventory.MenuType;" not in s:
    lines = s.split("\n")
    last = max(i for i, l in enumerate(lines) if l.startswith("import "))
    lines.insert(last + 1, "import net.minecraft.world.inventory.MenuType;")
    s = "\n".join(lines)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("menu type is a hook")

# ---------------- the advanced entity answers with its own ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTAdvancedChemicalReactorBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()
s2 = s2.replace('''    @Override
    protected net.minecraft.world.item.crafting.RecipeType<GTChemicalReactionRecipe> recipeType() {
        return GTAdvancedChemicalReactionRecipes.TYPE.get();
    }''',
'''    @Override
    protected RecipeType<GTChemicalReactionRecipe> recipeType() {
        return GTAdvancedChemicalReactionRecipes.TYPE.get();
    }

    @Override
    protected MenuType<?> menuType() {
        return GTAdvancedChemicalReactorMultiblock.MENU.get();
    }''', 1)
s2 = s2.replace('''import net.minecraft.world.level.block.entity.BlockEntityType;''',
'''import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;''', 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("advanced entity names its menu type")
