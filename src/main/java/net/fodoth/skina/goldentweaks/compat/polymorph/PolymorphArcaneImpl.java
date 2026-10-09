package net.fodoth.skina.goldentweaks.compat.polymorph;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IPlayerRecipeData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.crafting.IArcaneRecipe;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * 真正接触 Polymorph 类型的实现，只会在 {@link PolymorphArcaneCompat#isActive()} 成立后被加载。
 */
final class PolymorphArcaneImpl {

    /** 记录每个玩家上一次是否显示过奥术选择控件，用于把控件及时收起（见 {@link #refresh}）。 */
    private static final Map<Player, Boolean> SELECTOR_SHOWN =
            Collections.synchronizedMap(new WeakHashMap<>());

    /** 同理，记录原版合成路径是否显示过选择控件。 */
    private static final Map<Player, Boolean> VANILLA_SELECTOR_SHOWN =
            Collections.synchronizedMap(new WeakHashMap<>());

    private PolymorphArcaneImpl() {
    }

    static void refresh(Player player, Container workbench) {
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return;
        }
        List<RecipeHolder<GTArcaneRecipe>> matches =
                workbench.isEmpty() ? List.of() : GTArcaneRecipes.matching(workbench, player);
        boolean show = matches.size() > 1;

        // 没有可切换的候选、且上次也没显示控件时不必打扰 Polymorph（最常见的情况，省开销）；
        // 但若上次显示过，即使这次没有候选也要同步一次，才能让控件正确消失。
        if (!show && !Boolean.TRUE.equals(SELECTOR_SHOWN.get(player))) {
            return;
        }
        PolymorphApi.getInstance().getRecipeManager().getPlayerRecipe(
                menu, GTArcaneRecipes.TYPE, CraftingInput.EMPTY, player.level(), player, matches);
        if (show) {
            SELECTOR_SHOWN.put(player, Boolean.TRUE);
        } else {
            SELECTOR_SHOWN.remove(player);
        }
    }

    static @Nullable IArcaneRecipe selected(Player player, Container workbench) {
        IPlayerRecipeData data = PolymorphApi.getInstance().getPlayerRecipeData(player);
        if (data == null) {
            return null;
        }
        RecipeHolder<?> selected = data.getSelectedRecipe();
        if (selected == null || !(selected.value() instanceof GTArcaneRecipe arcane)) {
            return null;
        }
        IArcaneRecipe recipe = arcane.arcane();
        // 网格可能已经变了，选中配方必须先重新校验。
        return recipe.matches(workbench, player.level(), player) ? recipe : null;
    }

    /**
     * 原版合成路径的多态选择。
     *
     * <p>只用一次「全量过滤」拿到当前网格的所有匹配配方（不走会额外排序的 {@code getRecipesFor}），
     * 并据此得到原版首个匹配；只有在候选多于一个、或者上一次显示过控件时才通知 Polymorph ——
     * 常见情况既不建列表也不发包，避免每次放置物品都触发一次同步。</p>
     */
    static Optional<RecipeHolder<CraftingRecipe>> selectVanilla(Player player, RecipeManager recipeManager,
                                                                RecipeType<CraftingRecipe> type, CraftingInput input,
                                                                Level level) {
        if (!(player instanceof ServerPlayer)) {
            return recipeManager.getRecipeFor(type, input, level);
        }
        List<RecipeHolder<CraftingRecipe>> matches = input.isEmpty()
                ? List.of()
                : recipeManager.getAllRecipesFor(type).stream()
                        .filter(holder -> holder.value().matches(input, level))
                        .toList();
        Optional<RecipeHolder<CraftingRecipe>> first =
                matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));

        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return first;
        }
        boolean shown = Boolean.TRUE.equals(VANILLA_SELECTOR_SHOWN.get(player));
        if (matches.size() <= 1 && !shown) {
            return first;
        }
        Optional<RecipeHolder<CraftingRecipe>> selected = PolymorphApi.getInstance().getRecipeManager()
                .getPlayerRecipe(menu, type, input, level, player, matches);
        if (matches.size() > 1) {
            VANILLA_SELECTOR_SHOWN.put(player, Boolean.TRUE);
        } else {
            VANILLA_SELECTOR_SHOWN.remove(player);
        }
        return selected.isPresent() ? selected : first;
    }

    static void applySelection(Player player, ResourceLocation id) {
        GTArcaneRecipe recipe = GTArcaneRecipes.byId(id, player);
        if (recipe == null) {
            return;
        }
        IPlayerRecipeData data = PolymorphApi.getInstance().getPlayerRecipeData(player);
        if (data == null) {
            return;
        }
        data.selectRecipe(new RecipeHolder<>(id, recipe));
        if (player.containerMenu != null) {
            player.containerMenu.slotsChanged(player.getInventory());
        }
    }
}
