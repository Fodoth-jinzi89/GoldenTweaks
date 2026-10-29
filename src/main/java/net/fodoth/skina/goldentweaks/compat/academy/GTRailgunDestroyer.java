package net.fodoth.skina.goldentweaks.compat.academy;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 由 {@code RailgunEffectAmmoMixin} 实现：把学院私有的「沿一条射线扫穿方块」逻辑暴露出来，
 * 供 {@link GTRailgunDestruction} 在多 tick 里分帧执行。
 */
public interface GTRailgunDestroyer {

    void gt$destroyRay(ServerLevel level, ServerPlayer player, Vec3 origin, Vec3 direction, double maxDistance, double energy);
}
