package net.fodoth.skina.goldentweaks.mixin.fix.academy;

import org.spongepowered.asm.mixin.Mixin;

/**
 * AcademyCraft 的「跟随玩家」视觉实体（风之翼 {@code storm_wing_visual}、光盾 {@code shield_effect}、
 * 跟随玩家的熔解射线 {@code meltdown_beam}）由两部分同时驱动位置：
 *
 * <ul>
 *     <li><b>客户端</b>：实体的 {@code tick()} 里每刻都会 {@code setPos(...)} 到玩家身上
 *         （用的是客户端玩家本身、已带局部插值的位置）；</li>
 *     <li><b>服务端</b>：实体在服务端也同样每刻 {@code setPos(...)}，以 {@code updateInterval(1)}
 *         每刻下发位置包，客户端在 {@code handleMoveEntity}/{@code handleTeleportEntity} 里通过
 *         {@code entity.lerpTo(...)} 应用。</li>
 * </ul>
 *
 * <p>在 1.21.1 里普通 {@code Entity} 的 {@code lerpTo} 就是直接 {@code setPos} 吸附（没有独立的插值目标），
 * 于是客户端一个 tick 内位置会被写两次：先是服务端位置（有网络延迟），再是客户端 follow 位置。渲染在
 * {@code xo}→{@code pos} 之间插值时，锚点与终值一个来自服务端、一个来自客户端，二者随延迟抖动，表现为
 * 实体随玩家移动时「一格一格地传送」。</p>
 *
 * <p>这些实体是纯视觉效果、位置本就完全可由客户端按玩家位置推算，因此这里给它们加一个空的
 * {@code lerpTo} 覆写，让客户端位置只由 {@code follow()}/{@code updateFromOwner()} 驱动；服务端位置仍照常
 * 同步（用于区块追踪），只是不再参与渲染。</p>
 */
@Mixin(targets = {
        "com.mohistmc.academy.entity.StormWingVisualEntity",
        "com.mohistmc.academy.entity.ShieldEffectEntity",
        "com.mohistmc.academy.entity.MeltdownBeamEntity"
})
public class PlayerFollowVisualSmoothMixin {

    public void lerpTo(double x, double y, double z, float yRot, float xRot, int lerpSteps) {
        // 位置完全由客户端 tick 里的 follow()/updateFromOwner() 从玩家（含局部插值）推算，
        // 忽略服务端每刻的位置同步，避免两者叠加导致抖动。
    }
}
