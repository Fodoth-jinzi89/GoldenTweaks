package net.fodoth.skina.goldentweaks.compat.polymorph;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.crafting.IArcaneRecipe;

import java.util.Optional;

/**
 * 奥术工作台与 Polymorph 的接线入口。
 *
 * <p>本类刻意不出现任何 Polymorph 类型：真正的调用都放在 {@link PolymorphArcaneImpl}，
 * 只有 {@link #isActive()} 通过后才类加载它，这样未安装 Polymorph 时不会因为缺少
 * 类而抛 {@link NoClassDefFoundError}。</p>
 */
public final class PolymorphArcaneCompat {

    private PolymorphArcaneCompat() {
    }

    public static boolean isActive() {
        return ModList.get().isLoaded("polymorph");
    }

    /** 在奥术分支刷新前重建候选项并交给 Polymorph 选择/同步。 */
    public static void refresh(Player player, Container workbench) {
        if (isActive()) {
            PolymorphArcaneImpl.refresh(player, workbench);
        }
    }

    /** Polymorph 当前选中的奥术配方；若没有或已不再匹配当前网格则返回 {@code null}。 */
    public static @Nullable IArcaneRecipe selected(Player player, Container workbench) {
        return isActive() ? PolymorphArcaneImpl.selected(player, workbench) : null;
    }

    /** 原版合成路径的多态选择；未安装 Polymorph 时回退为原版首个匹配。 */
    public static Optional<RecipeHolder<CraftingRecipe>> selectVanilla(Player player, RecipeManager recipeManager,
                                                                       RecipeType<CraftingRecipe> type,
                                                                       CraftingInput input, Level level) {
        return isActive()
                ? PolymorphArcaneImpl.selectVanilla(player, recipeManager, type, input, level)
                : recipeManager.getRecipeFor(type, input, level);
    }

    /** 处理客户端回传的配方选择。 */
    public static void applySelection(Player player, ResourceLocation id) {
        if (isActive()) {
            PolymorphArcaneImpl.applySelection(player, id);
        }
    }
}
