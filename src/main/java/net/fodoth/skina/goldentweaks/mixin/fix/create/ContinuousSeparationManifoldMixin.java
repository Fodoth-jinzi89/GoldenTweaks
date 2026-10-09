package net.fodoth.skina.goldentweaks.mixin.fix.create;

import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Create 6.0.10 服务端崩溃修复：ContinuousOBBCollider.collideMany 中读取 "mf.axis" 时 NPE。
 *
 * reset() 把 axis / normalAxis 置为 null，separate() 只有在某条分离轴上得到非零距离时
 * 才会重新赋值。当实体包围盒中心与装置碰撞箱中心在所有 SAT 轴上完全重合（零分离退化情形，
 * 例如恰好站在装置方块中心）时，两者不会被重新赋值，collideMany 随后读取
 * mf.axis.x / mf.normalAxis.x 就抛出 NullPointerException，使服务端 tick 崩溃。
 *
 * 修复：reset() 结束后把两个字段兜底为非空的 Vec3.ZERO。零分离下 axis 为零向量，其碰撞
 * 贡献恒为 0（不产生推挤），与 Create 上游 #10301 的“null 轴贡献零分离”语义一致；
 * 正常碰撞路径仍会被 separate() 覆盖为真实轴，行为不变。
 */
@Mixin(targets = "com.simibubi.create.foundation.collision.ContinuousOBBCollider$ContinuousSeparationManifold", remap = false)
public class ContinuousSeparationManifoldMixin {

    @Shadow(remap = false)
    private Vec3 axis;

    @Shadow(remap = false)
    private Vec3 normalAxis;

    @Inject(
            method = "reset",
            at = @At("TAIL"),
            remap = false
    )
    private void gt$replaceNullAxes(CallbackInfo ci) {
        if (this.axis == null) {
            this.axis = Vec3.ZERO;
        }
        if (this.normalAxis == null) {
            this.normalAxis = Vec3.ZERO;
        }
    }
}
