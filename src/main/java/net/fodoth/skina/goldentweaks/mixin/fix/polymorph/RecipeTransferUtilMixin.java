package net.fodoth.skina.goldentweaks.mixin.fix.polymorph;

import com.illusivesoulworks.polymorph.common.integration.util.RecipeTransfer;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.transfer.RecipeTransferUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 复刻原 {@code PolymorphJeiPlugin.PolymorphRecipeTransferListener.afterRecipeTransfer}：
 * JEI 成功把配方转移到容器后，让 Polymorph 选中该配方（输出槽选择器随之更新）。
 * <p>
 * 打包内 JEI 19.x 没有 {@code IRecipeTransferListener} API，故改为在 JEI 的转移入口
 * {@link RecipeTransferUtil#transferRecipe} 返回成功时补上同样的动作。
 */
@Mixin(value = RecipeTransferUtil.class, remap = false)
public class RecipeTransferUtilMixin {

    @Inject(
            method = "transferRecipe(Lmezz/jei/api/recipe/transfer/IRecipeTransferManager;Lnet/minecraft/world/inventory/AbstractContainerMenu;Lmezz/jei/api/gui/IRecipeLayoutDrawable;Lnet/minecraft/world/entity/player/Player;Z)Z",
            at = @At("RETURN"))
    private static void gt$selectTransferredRecipe(IRecipeTransferManager manager,
                                                   AbstractContainerMenu container,
                                                   IRecipeLayoutDrawable<?> layout,
                                                   Player player, boolean maxTransfer,
                                                   CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && layout.getRecipe() instanceof RecipeHolder<?> recipeHolder) {
            RecipeTransfer.selectRecipe(recipeHolder.id());
        }
    }
}
