package net.fodoth.skina.goldentweaks.mixin.feature.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTGolemSimplify;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.common.entities.golem.ThaumcraftGolemEntity;

/**
 * 神秘傀儡简化模式（见 {@link GTGolemSimplify}）：傀儡不再寻路，直接在原点完成工作。
 *
 * <p>移植版所有核心工作的"先走过去、到了再交互"都汇聚到 {@code moveToward}：返回
 * {@code false} 表示"已经就位"，调用方随即原地交互。把它取消成 {@code false} 就等于让
 * 傀儡原地完成工作，同时绕开整套寻路、卡死重试与目标封锁逻辑——这正是傀儡卡住不动的来源。</p>
 *
 * <p>因为 {@code moveToward} 不再把 {@code coreWorkBusy} 置位，工作目标会在一轮工作后自行
 * 结束（{@code GolemWorkGoal.canContinueToUse} 返回假），于是"每秒（20 tick）工作一次"
 * 由检索间隔统一决定，不需要额外的节流逻辑。</p>
 */
@Mixin(value = ThaumcraftGolemEntity.class, remap = false)
public abstract class ThaumcraftGolemEntityMixin {

    @Inject(method = "moveToward", at = @At("HEAD"), cancellable = true)
    private void gt$workWithoutMoving(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (GTGolemSimplify.appliesTo((ThaumcraftGolemEntity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    /**
     * 简化模式下傀儡应当站在原点：被活塞/爆炸等推离原点时直接拉回。
     * 原点就是放下傀儡的那一格（必定是可替换方块），因此直接传送是安全的。
     * 正在移动（例如躲避苦力怕）时不打断，等它停下来再拉回。
     */
    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    private void gt$snapBackHome(CallbackInfo ci) {
        ThaumcraftGolemEntity golem = (ThaumcraftGolemEntity) (Object) this;
        if (!golem.getNavigation().isDone()) {
            return;
        }

        BlockPos home = GTGolemSimplify.homeSnapTarget(golem);
        if (home == null) {
            return;
        }

        golem.teleportTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D);
        golem.getNavigation().stop();
    }
}
