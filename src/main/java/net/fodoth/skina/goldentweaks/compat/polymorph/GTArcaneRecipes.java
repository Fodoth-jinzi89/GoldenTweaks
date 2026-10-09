package net.fodoth.skina.goldentweaks.compat.polymorph;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.common.research.TCResearchManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 奥术工作台的多态候选项：合成 {@link RecipeType}、稳定的配方 id 以及候选收集。
 *
 * <p>奥术配方本身没有 id，这里按它们在 {@link ThaumcraftApi#getCraftingRecipes()} 中的
 * 出现次序（仅统计 {@link IArcaneRecipe}）生成 {@code goldentweaks:arcane/<index>}。
 * 该列表在一次服务器运行内保持稳定，服务端用它把玩家选择的 id 还原回配方。</p>
 */
public final class GTArcaneRecipes {

    private static final String PATH_PREFIX = "arcane/";

    /** 奥术配方专用的合成类型，仅用于让 Polymorph 有一个身份键。 */
    public static final RecipeType<GTArcaneRecipe> TYPE =
            RecipeType.simple(ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "arcane_workbench"));

    private GTArcaneRecipes() {
    }

    public static boolean isSynthetic(ResourceLocation id) {
        return GoldenTweaks.MODID.equals(id.getNamespace()) && id.getPath().startsWith(PATH_PREFIX);
    }

    /** 按扫描顺序列出所有奥术配方（与 {@code findMatchingArcaneRecipeEntry} 的判定顺序一致）。 */
    public static List<IArcaneRecipe> all() {
        List<IArcaneRecipe> list = new ArrayList<>();
        for (Object entry : ThaumcraftApi.getCraftingRecipes()) {
            if (entry instanceof IArcaneRecipe arcane) {
                list.add(arcane);
            }
        }
        return list;
    }

    private static ResourceLocation idOf(int index) {
        return ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, PATH_PREFIX + index);
    }

    private static int indexOf(ResourceLocation id) {
        if (!isSynthetic(id)) {
            return -1;
        }
        try {
            return Integer.parseInt(id.getPath().substring(PATH_PREFIX.length()));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 复刻 {@code findMatchingArcaneRecipeEntry} 的筛选条件（研究已解锁 + 配方匹配），
     * 返回当前网格下所有可选配方及其稳定 id。
     */
    public static List<RecipeHolder<GTArcaneRecipe>> matching(Container container, Player player) {
        List<IArcaneRecipe> all = all();
        List<RecipeHolder<GTArcaneRecipe>> result = new ArrayList<>();
        Level level = player.level();
        for (int i = 0; i < all.size(); i++) {
            IArcaneRecipe recipe = all.get(i);
            if (!TCResearchManager.has(player, recipe.getResearch())) {
                continue;
            }
            if (!recipe.matches(container, level, player)) {
                continue;
            }
            result.add(new RecipeHolder<>(idOf(i), wrap(recipe, container, player)));
        }
        return result;
    }

    /** 把客户端回传的 id 还原成背包里的配方外壳（用于写入玩家选择）。 */
    public static @Nullable GTArcaneRecipe byId(ResourceLocation id, Player player) {
        int index = indexOf(id);
        List<IArcaneRecipe> all = all();
        if (index < 0 || index >= all.size()) {
            return null;
        }
        return wrap(all.get(index), null, player);
    }

    private static GTArcaneRecipe wrap(IArcaneRecipe recipe, @Nullable Container container, Player player) {
        return new GTArcaneRecipe(recipe, container, player, outputOf(recipe, container));
    }

    private static net.minecraft.world.item.ItemStack outputOf(IArcaneRecipe recipe, @Nullable Container container) {
        if (container != null) {
            net.minecraft.world.item.ItemStack result = recipe.getCraftingResult(container);
            if (!result.isEmpty()) {
                return result;
            }
        }
        return recipe.getRecipeOutput();
    }
}
