package net.fodoth.skina.goldentweaks.mixin.fix.thaumicenergistics;

import net.fodoth.skina.goldentweaks.compat.polymorph.PolymorphArcaneCompat;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumicenergistics.common.container.ContainerArcaneCraftingTerminal;

import java.util.Optional;

/**
 * 给神秘能源（Thaumic Energistics）的「奥术合成终端」接上 Polymorph。
 *
 * <p>终端的 {@code updateCurrentRecipeAndOutput} 和工作台一样：先查奥术配方
 * （{@code findMatchingArcaneRecipe}/{@code ...Aspects}，最终汇聚到
 * {@code findMatchingArcaneRecipeEntry}），没有再退回原版 {@code RecipeType.CRAFTING}。
 * 这里在奥术查询前重建候选项、在选择列表就绪后交给 Polymorph，并在原版分支上做同样的
 * 多态选择。</p>
 *
 * <p>仅在同时装了 thaumicenergistics 与 Polymorph 时启用（见 {@code GoldenTweaksMixinPlugin}）。</p>
 */
@Mixin(value = ContainerArcaneCraftingTerminal.class, remap = false)
public abstract class ArcaneCraftingTerminalMixin {

    @Redirect(
            method = "updateCurrentRecipeAndOutput",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/common/lib/crafting/ThaumcraftCraftingManager;findMatchingArcaneRecipe(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack gt$polymorphArcaneRefresh(Container workbench, Player player) {
        PolymorphArcaneCompat.refresh(player, workbench);
        return ThaumcraftCraftingManager.findMatchingArcaneRecipe(workbench, player);
    }

    @SuppressWarnings("unchecked")
    @Redirect(
            method = "updateCurrentRecipeAndOutput",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"
            )
    )
    private Optional<RecipeHolder<CraftingRecipe>> gt$polymorphVanillaRecipe(
            RecipeManager recipeManager, RecipeType<?> type, RecipeInput input, Level level) {
        Player player = ((ContainerArcaneCraftingTerminal) (Object) this).getPlayerInventory().player;
        return PolymorphArcaneCompat.selectVanilla(player, recipeManager,
                (RecipeType<CraftingRecipe>) type, (CraftingInput) input, level);
    }
}
