package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.mcreator.flavorimmerseddaily.block.entity.DryerBlockEntity;
import net.mcreator.flavorimmerseddaily.network.DryerguiButtonMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DryerBlockEntity.class)
public class DryerBlockEntityMixin {

    @Unique
    private boolean gt$lastPowered = false;

    @Unique
    private int gt$cooldown = 0;

    @Unique
    private int gt$continuousTimer = 0;

    // =========================
    // 自动化入口（借 canPlaceItem 驱动）
    // =========================
    @Inject(method = "canPlaceItem", at = @At("HEAD"))
    private void gt$autoTick(int index, ItemStack stack,
                             CallbackInfoReturnable<Boolean> cir) {

        DryerBlockEntity self = (DryerBlockEntity)(Object)this;
        Level level = self.getLevel();
        BlockPos pos = self.getBlockPos();

        if (level == null || level.isClientSide) return;

        boolean powered = level.hasNeighborSignal(pos);

        // ===== 冷却 =====
        if (gt$cooldown > 0) {
            gt$cooldown--;
        }

        // =========================
        // 脉冲模式
        // =========================
        if (GoldenTweaksCommonConfig.FLAVOR_AUTO_PULSE.get()) {

            if (powered && !gt$lastPowered) {
                triggerCraft(self, level, pos);
            }
        }

        // =========================
        // 持续模式
        // =========================
        if (GoldenTweaksCommonConfig.FLAVOR_AUTO_CONTINUOUS.get()) {

            if (powered) {
                gt$continuousTimer++;

                int interval = GoldenTweaksCommonConfig.FLAVOR_AUTO_CONTINUOUS_TICK.get();

                if (gt$continuousTimer >= interval) {
                    gt$continuousTimer = 0;
                    triggerCraft(self, level, pos);
                }
            } else {
                gt$continuousTimer = 0;
            }
        }

        gt$lastPowered = powered;
    }

    // =========================
    // 触发合成
    // =========================
    @Unique
    private void triggerCraft(DryerBlockEntity self,
                              Level level,
                              BlockPos pos) {

        if (gt$cooldown > 0) return;

        gt$cooldown = GoldenTweaksCommonConfig.FLAVOR_AUTO_PULSE_TICK.get();

        ServerLevel serverLevel = (ServerLevel) level;

        ServerPlayer fake = serverLevel.getRandomPlayer();
        serverLevel.scheduleTick(pos, self.getBlockState().getBlock(), 1);
        if (fake != null) {
            DryerguiButtonMessage.handleButtonAction(
                    fake,
                    0,
                    pos.getX(),
                    pos.getY(),
                    pos.getZ()
            );
        }
    }

    // =========================
    // 输入规则
    // =========================
    @Inject(method = "canPlaceItemThroughFace", at = @At("HEAD"), cancellable = true)
    private void gt$canInsert(int index, ItemStack stack, Direction direction,
                              CallbackInfoReturnable<Boolean> cir) {

        if (stack.is(Items.REDSTONE_TORCH)) {
            cir.setReturnValue(false);
            return;
        }

        // slot 0：输出槽 → 禁止输入
        if (index == 0) {
            cir.setReturnValue(false);
            return;
        }

        // slot 1：侧面输入槽（核心要求）
        if (index == 1) {
            cir.setReturnValue(direction.getAxis().isHorizontal());
            return;
        }

        // 其他槽：禁止外部输入
        cir.setReturnValue(false);
    }

    // =========================
    // 输出规则
    // =========================
    @Inject(method = "canTakeItemThroughFace", at = @At("HEAD"), cancellable = true)
    private void gt$canExtract(int index, ItemStack stack, Direction direction,
                               CallbackInfoReturnable<Boolean> cir) {

        // slot 0：底部输出
        if (index == 0) {
            cir.setReturnValue(direction == Direction.DOWN);
            return;
        }

        // 其他槽：禁止自动输出
        cir.setReturnValue(false);
    }

    // =========================
    // 可访问槽位
    // =========================
    @Inject(method = "getSlotsForFace", at = @At("HEAD"), cancellable = true)
    private void gt$slots(Direction side, CallbackInfoReturnable<int[]> cir) {

        if (side == Direction.DOWN) {
            cir.setReturnValue(new int[]{0}); // 输出槽
            return;
        }

        if (side.getAxis().isHorizontal()) {
            cir.setReturnValue(new int[]{1}); // 侧面输入槽
            return;
        }

        // UP 不允许访问任何槽
        cir.setReturnValue(new int[]{});
    }
}