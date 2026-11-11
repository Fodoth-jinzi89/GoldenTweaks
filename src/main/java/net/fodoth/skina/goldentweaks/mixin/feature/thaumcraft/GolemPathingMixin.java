package net.fodoth.skina.goldentweaks.mixin.feature.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTGolemSimplify;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.common.entities.golem.ThaumcraftGolemEntity;

/**
 * 神秘傀儡简化模式（见 {@link GTGolemSimplify}）：傀儡不再寻路，直接在原点完成工作。
 *
 * <p>移植版把"先走过去、到了再交互"收敛到 {@code GolemPathing.moveToward}：返回
 * {@code false} 表示"已经就位"，调用方随即原地交互。把它取消成 {@code false} 就等于让
 * 傀儡原地完成工作，同时绕开整套寻路、卡死重试与目标封锁逻辑——这正是傀儡卡住不动的来源。</p>
 */
@Mixin(targets = "thaumcraft.common.entities.golem.GolemPathing", remap = false)
public abstract class GolemPathingMixin {

    @Shadow
    @Final
    private ThaumcraftGolemEntity golem;

    @Inject(method = "moveToward", at = @At("HEAD"), cancellable = true)
    private void gt$workWithoutMoving(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (GTGolemSimplify.appliesTo(golem)) {
            cir.setReturnValue(false);
        }
    }
}
