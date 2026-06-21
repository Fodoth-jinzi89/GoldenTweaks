package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.mcreator.flavorimmerseddaily.block.entity.PressurecookerBlockEntity;
import net.mcreator.flavorimmerseddaily.network.PressureguiButtonMessage;
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

@Mixin(PressurecookerBlockEntity.class)
public abstract class PressurecookerBlockEntityMixin {

    @Unique
    private boolean gt$lastPowered = false;

    @Unique
    private int gt$cooldown = 0;

    @Unique
    private int gt$continuousTimer = 0;

    @Inject(method = "canPlaceItem", at = @At("HEAD"))
    private void gt$onCanPlaceItem(int index, ItemStack stack,
                                   CallbackInfoReturnable<Boolean> cir) {

        PressurecookerBlockEntity self = (PressurecookerBlockEntity)(Object)this;
        Level level = self.getLevel();
        BlockPos pos = self.getBlockPos();

        if (level == null || level.isClientSide) return;

        boolean powered = level.hasNeighborSignal(pos);

        // ===== 冷却 =====
        if (gt$cooldown > 0) {
            gt$cooldown--;
        }

        // =========================
        // ✔ 脉冲模式（配置控制）
        // =========================
        if (GoldenTweaksCommonConfig.FLAVOR_AUTO_PULSE.get()) {

            if (powered && !gt$lastPowered) {
                triggerCraft(self, level, pos);
            }
        }

        // =========================
        // ✔ 持续模式（配置控制）
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

    @Unique
    private void triggerCraft(PressurecookerBlockEntity self,
                              Level level,
                              BlockPos pos) {

        if (gt$cooldown > 0) return;

        gt$cooldown = GoldenTweaksCommonConfig.FLAVOR_AUTO_PULSE_TICK.get();

        ServerLevel serverLevel = (ServerLevel) level;

        // 延迟1刻再执行检查配方
        serverLevel.scheduleTick(pos, self.getBlockState().getBlock(), 1);
        serverLevel.getServer().execute(() -> {
            ServerPlayer fake = serverLevel.getRandomPlayer();
            if (fake != null) {
                PressureguiButtonMessage.handleButtonAction(
                        fake,
                        0,
                        pos.getX(),
                        pos.getY(),
                        pos.getZ()
                );
            }
        });
    }

    @Inject(method = "canPlaceItemThroughFace", at = @At("HEAD"), cancellable = true)
    private void gt$canInsert(int index, ItemStack stack, Direction direction,
                              CallbackInfoReturnable<Boolean> cir) {

        if (stack.is(Items.REDSTONE_TORCH)) {
            cir.setReturnValue(false);
            return;
        }

        // slot 1：顶部输入
        if (index == 1) {
            cir.setReturnValue(direction == Direction.UP);
            return;
        }

        // slot 2：顶部 + 南北
        if (index == 2) {
            cir.setReturnValue(
                    direction == Direction.UP ||
                            direction == Direction.NORTH ||
                            direction == Direction.SOUTH
            );
            return;
        }

        // slot 3：顶部 + 东西
        if (index == 3) {
            cir.setReturnValue(
                    direction == Direction.UP ||
                            direction == Direction.EAST ||
                            direction == Direction.WEST
            );
            return;
        }

        cir.setReturnValue(false);
    }

    @Inject(method = "canTakeItemThroughFace", at = @At("HEAD"), cancellable = true)
    private void gt$canExtract(int index, ItemStack stack, Direction direction,
                               CallbackInfoReturnable<Boolean> cir) {

        // slot 0：只能底部输出
        if (index == 0) {
            cir.setReturnValue(direction == Direction.DOWN);
            return;
        }

        // slot 1/2/3：禁止自动输出（机器内部槽）
        cir.setReturnValue(false);
    }

    @Inject(method = "getSlotsForFace", at = @At("HEAD"), cancellable = true)
    private void gt$slots(Direction side, CallbackInfoReturnable<int[]> cir) {

        switch (side) {

            case UP -> cir.setReturnValue(new int[]{1, 2, 3});
            case DOWN -> cir.setReturnValue(new int[]{0});
            case EAST, WEST -> cir.setReturnValue(new int[]{3});
            default -> cir.setReturnValue(new int[]{2});
        }
    }
}