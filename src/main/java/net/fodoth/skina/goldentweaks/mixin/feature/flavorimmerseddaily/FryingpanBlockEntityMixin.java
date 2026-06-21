package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import net.mcreator.flavorimmerseddaily.block.entity.FryingpanBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FryingpanBlockEntity.class)
public class FryingpanBlockEntityMixin {

    // =========================
    // 输入规则
    // =========================
    @Inject(method = "canPlaceItemThroughFace", at = @At("HEAD"), cancellable = true)
    private void gt$canInsert(int index, ItemStack stack, Direction direction,
                              CallbackInfoReturnable<Boolean> cir) {

        // slot 0：输出槽 → 禁止输入
        if (index == 0) {
            cir.setReturnValue(false);
            return;
        }

        // slot 1：只能顶部输入
        if (index == 1) {
            cir.setReturnValue(direction == Direction.UP);
            return;
        }

        // slot 2：顶部 + 侧面输入
        if (index == 2) {
            cir.setReturnValue(
                    direction == Direction.UP ||
                            direction.getAxis().isHorizontal()
            );
            return;
        }

        cir.setReturnValue(false);
    }

    // =========================
    // 输出规则
    // =========================
    @Inject(method = "canTakeItemThroughFace", at = @At("HEAD"), cancellable = true)
    private void gt$canExtract(int index, ItemStack stack, Direction direction,
                               CallbackInfoReturnable<Boolean> cir) {

        // slot 0：只能侧面输出（核心要求）
        if (index == 0) {
            cir.setReturnValue(direction.getAxis().isHorizontal());
            return;
        }

        // slot 1/2：禁止自动输出（内部槽）
        cir.setReturnValue(false);
    }

    // =========================
    // 可访问槽位（关键修正）
    // =========================
    @Inject(method = "getSlotsForFace", at = @At("HEAD"), cancellable = true)
    private void gt$slots(Direction side, CallbackInfoReturnable<int[]> cir) {

        switch (side) {

            case DOWN -> cir.setReturnValue(new int[]{}); // only output slot

            case UP -> cir.setReturnValue(new int[]{1, 2}); // top input + mixed input

            default -> cir.setReturnValue(new int[]{0, 2}); // side only
        }
    }
}