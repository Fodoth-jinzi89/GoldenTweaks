package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import net.mcreator.flavorimmerseddaily.block.entity.SteamboxBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SteamboxBlockEntity.class)
public class SteamboxBlockEntityMixin {


    /**
     * SLOT规则：
     * 0：输出槽 → 只能底部输出
     * 1：输入槽 → 只能顶部输入
     * 2：输入槽 → 顶部 + 侧面输入
     */

    // =========================
    // 输入规则（自动化 + GUI）
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
                            direction == Direction.NORTH ||
                            direction == Direction.SOUTH ||
                            direction == Direction.EAST ||
                            direction == Direction.WEST
            );
            return;
        }

        cir.setReturnValue(false);
    }

    // =========================
    // 输出规则（漏斗/管道）
    // =========================
    @Inject(method = "canTakeItemThroughFace", at = @At("HEAD"), cancellable = true)
    private void gt$canExtract(int index, ItemStack stack, Direction direction,
                               CallbackInfoReturnable<Boolean> cir) {

        // slot 0：只能底部输出
        if (index == 0) {
            cir.setReturnValue(direction == Direction.DOWN);
            return;
        }

        // slot 1/2：禁止自动输出（机器内部槽）
        cir.setReturnValue(false);
    }

    @Inject(method = "getSlotsForFace", at = @At("HEAD"), cancellable = true)
    private void gt$slots(Direction side, CallbackInfoReturnable<int[]> cir) {

        switch (side) {

            case UP -> cir.setReturnValue(new int[]{1, 2});
            case DOWN -> cir.setReturnValue(new int[]{0});
            default -> cir.setReturnValue(new int[]{2});
        }
    }
}