package net.fodoth.skina.goldentweaks.compat.polymorph;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.crafting.IArcaneRecipe;

/**
 * 奥术配方在 Polymorph 眼中的「原版配方」外壳。
 *
 * <p>Polymorph 的多态合成 API 完全建立在原版 {@link Recipe}/{@link RecipeType} 之上，
 * 而神秘时代的奥术配方是 {@link IArcaneRecipe}、并不在 {@code RecipeManager} 里。
 * 这个外壳把一个 {@link IArcaneRecipe} 包装成原版配方，使 Polymorph 能把它当作候选项
 * 展示、选择。</p>
 *
 * <p>Polymorph 只会读取 {@link #getResultItem(HolderLookup.Provider)}（读取失败才会退回
 * {@link #assemble}）来构建选择列表，以及在选中后比对配方 id，因此
 * {@link #getSerializer()} 永不会被调用，这里直接返回 {@code null}；本外壳也不会进入
 * {@code RecipeManager}、不会参与网络同步。</p>
 */
public final class GTArcaneRecipe implements Recipe<CraftingInput> {

    private final IArcaneRecipe recipe;
    private final @Nullable Container container;
    private final Player player;
    private final ItemStack output;

    public GTArcaneRecipe(IArcaneRecipe recipe, @Nullable Container container, Player player, ItemStack output) {
        this.recipe = recipe;
        this.container = container;
        this.player = player;
        this.output = output;
    }

    public IArcaneRecipe arcane() {
        return recipe;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return container != null && recipe.matches(container, level, player);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return null;
    }

    @Override
    public RecipeType<?> getType() {
        return GTArcaneRecipes.TYPE;
    }
}
