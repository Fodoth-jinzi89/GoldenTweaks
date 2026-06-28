package net.fodoth.skina.goldentweaks.mixin.feature.kaleidoscope;

import com.github.ysbbbbbb.kaleidoscopecookery.block.misc.TrashCanBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.misc.TrashCanBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrashCanBlock.class)
public class TrashCanBlockMixin {

    @Inject(
            method = "useItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;isSecondaryUseActive()Z"
            ),
            cancellable = true
    )
    private void onUseItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                             Player player, InteractionHand hand, BlockHitResult hit,
                             CallbackInfoReturnable<ItemInteractionResult> cir) {
        // 如果触发了 isSecondaryUseActive() 检查，直接修改逻辑
        if (hand == InteractionHand.MAIN_HAND) {
            ItemStack itemInHand = player.getItemInHand(hand);
            if (level.getBlockEntity(pos) instanceof TrashCanBlockEntity trashCan) {
                if (itemInHand.isEmpty()) {
                    // 直接执行取回操作，不检查是否潜行
                    trashCan.withdrawItem(player);
                    cir.setReturnValue(ItemInteractionResult.SUCCESS);
                } else {
                    trashCan.putItem(itemInHand);
                    cir.setReturnValue(ItemInteractionResult.SUCCESS);
                }
            }
        }
    }
}
