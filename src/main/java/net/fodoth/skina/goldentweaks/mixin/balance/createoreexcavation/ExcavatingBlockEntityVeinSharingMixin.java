package net.fodoth.skina.goldentweaks.mixin.balance.createoreexcavation;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tom.createores.block.entity.ExcavatingBlockEntity;
import net.fodoth.skina.goldentweaks.compat.createoreexcavation.GTVeinSharing;
import net.fodoth.skina.goldentweaks.compat.createoreexcavation.IGTOreVeinOwner;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * 矿脉分享：给每台挖掘机（钻机 / 提取器）补记「所有者玩家」，并按同矿脉上的不同玩家数给出加速与产量加成。
 *
 * <ul>
 *   <li>所有者记录进 NBT，并随 {@code write(..., clientPacket = true)} 一起同步到客户端（读取时补回来）。</li>
 *   <li>旧的存档里没有所有者，所以首次被玩家交互时补记一次。</li>
 *   <li>运行时间：把 {@code tick()} 里用来判定完成的 {@code getTicks()} 按倍率缩小，
 *       护目镜里进度条用的是同一个值，避免进度条只走一半就重置。</li>
 *   <li>双倍产出：在 {@code tick()} 调用 {@code onFinished()} 的位置再调用一次（掉落/流体都会被产出两次）。</li>
 * </ul>
 */
@Mixin(value = ExcavatingBlockEntity.class, remap = false)
public abstract class ExcavatingBlockEntityVeinSharingMixin implements IGTOreVeinOwner {

    @Unique
    private UUID gt$veinOwner;

    @Unique
    private int gt$sharingSteps;

    @Unique
    private long gt$sharingStepsTick = -100L;

    @Override
    public @Nullable UUID gt$getVeinOwner() {
        return gt$veinOwner;
    }

    @Override
    public void gt$setVeinOwner(@Nullable UUID owner) {
        this.gt$veinOwner = owner;
        ((ExcavatingBlockEntity<?>) (Object) this).notifyUpdate();
    }

    @Inject(
            method = "write(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Z)V",
            at = @At("TAIL")
    )
    private void gt$writeVeinOwner(CompoundTag tag, HolderLookup.Provider provider, boolean clientPacket,
                                   CallbackInfo ci) {
        if (gt$veinOwner != null) {
            tag.putUUID("GTShareOwner", gt$veinOwner);
        }
    }

    @Inject(
            method = "read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Z)V",
            at = @At("TAIL")
    )
    private void gt$readVeinOwner(CompoundTag tag, HolderLookup.Provider provider, boolean clientPacket,
                                  CallbackInfo ci) {
        if (tag.hasUUID("GTShareOwner")) {
            gt$veinOwner = tag.getUUID("GTShareOwner");
        }
    }

    @Inject(
            method = "onClick(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/ItemInteractionResult;",
            at = @At("HEAD")
    )
    private void gt$backfillVeinOwner(Player player, InteractionHand hand,
                                      CallbackInfoReturnable<ItemInteractionResult> cir) {
        Level level = ((ExcavatingBlockEntity<?>) (Object) this).getLevel();
        if (level == null || level.isClientSide || gt$veinOwner != null) {
            return;
        }
        gt$setVeinOwner(player.getUUID());
    }

    @ModifyExpressionValue(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tom/createores/recipe/ExcavatingRecipe;getTicks()I"
            ),
            remap = false
    )
    private int gt$applyVeinSpeedBonus(int original) {
        return gt$scaleTicks(original);
    }

    @ModifyExpressionValue(
            method = "addToGoggleTooltip(Ljava/util/List;Z)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tom/createores/recipe/ExcavatingRecipe;getTicks()I"
            ),
            remap = false
    )
    private int gt$applyVeinSpeedBonusToTooltip(int original) {
        return gt$scaleTicks(original);
    }

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tom/createores/block/entity/ExcavatingBlockEntity;onFinished()V"
            ),
            remap = false
    )
    private void gt$applyVeinYieldBonus(ExcavatingBlockEntity<?> instance, Operation<Void> original) {
        original.call(instance);

        Level level = instance.getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        if (GTVeinSharing.rollDoubleOutput(level, gt$getSharingSteps())) {
            original.call(instance);
        }
    }

    @Unique
    private int gt$scaleTicks(int original) {
        double multiplier = GTVeinSharing.getTimeMultiplier(gt$getSharingSteps());
        if (multiplier >= 1.0D) {
            return original;
        }
        return Math.max(1, (int) Math.round(original * multiplier));
    }

    /**
     * 统计结果每 10 tick 才重算一次：这个值在 {@code tick()} 里每 tick 都要用，但邻机的增删不需要即时反映。
     */
    @Unique
    private int gt$getSharingSteps() {
        ExcavatingBlockEntity<?> self = (ExcavatingBlockEntity<?>) (Object) this;
        Level level = self.getLevel();
        if (level == null) {
            return 0;
        }
        long now = level.getGameTime();
        if (now - gt$sharingStepsTick < 10L) {
            return gt$sharingSteps;
        }
        gt$sharingSteps = GTVeinSharing.getSharingSteps(level, self.getBlockPos());
        gt$sharingStepsTick = now;
        return gt$sharingSteps;
    }
}
