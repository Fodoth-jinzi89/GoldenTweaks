package net.fodoth.skina.goldentweaks.mixin.feature.academy;

import com.mohistmc.academy.config.DynamicSkillRules;
import com.mohistmc.academy.entity.RailgunBeamEntity;
import com.mohistmc.academy.skill.AcademyDamageHelper;
import com.mohistmc.academy.skill.PlayerAbilityData;
import com.mohistmc.academy.skill.SkillDamageSource;
import com.mohistmc.academy.skill.ability.electromaster.RailgunEffect;
import com.mohistmc.academy.skill.passive.PassiveSkillEventHandler;
import com.mohistmc.academy.utils.MathUtils;
import com.mohistmc.academy.world.AcademyEntities;
import com.mohistmc.academy.world.AcademySounds;
import com.mohistmc.academy.world.effect.EffectHelper;
import com.mohistmc.academy.world.entity.CoinEntity;
import net.fodoth.skina.goldentweaks.compat.academy.GTBeamScale;
import net.fodoth.skina.goldentweaks.compat.academy.GTRailgunDestroyer;
import net.fodoth.skina.goldentweaks.compat.academy.GTRailgunDestruction;
import net.fodoth.skina.goldentweaks.compat.academy.GTThermalRailgunCoins;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 超电磁炮（{@code railgun}）按所消耗硬币的倍率放大「伤害 / 破坏 / 光线直径 / 光线长度」。
 *
 * <p>倍率由被消耗的硬币决定（见 {@link GTThermalRailgunCoins}）：在 {@code tryRelease} 里于
 * {@code coin.consumeForRailgun} 处把倍率截获下来，再在 {@code performRailgun} 里统一应用。
 * 学院币（空变体）倍率为 1.0，行为与原版完全一致。</p>
 *
 * <p>破坏区的取样间距固定约 1 格，因此放大半径后仍是一整片实心圆柱；破坏本身按
 * {@link GTRailgunDestruction} 分摊到多 tick 执行。</p>
 */
@Mixin(RailgunEffect.class)
public abstract class RailgunEffectAmmoMixin implements GTRailgunDestroyer {

    @Unique
    private static double gt$ammoMultiplier = 1.0D;

    /** 圆盘取样间距：固定约为 1 格，保证相邻射线覆盖相邻方块列，破坏区是实心圆柱而非稀疏线束。 */
    @Unique
    private static final double gt$SAMPLE_STEP = 0.9D;

    /** 单条射线的能量上限：给足后射线只受射程 / 屏障 / 区域保护限制，从而把路径范围内整段扫穿。 */
    @Unique
    private static final double gt$UNLIMITED_ENERGY = Double.MAX_VALUE / 4.0D;

    @Shadow
    private static double perpendicularDistance(Vec3 start, Vec3 direction, Vec3 point) {
        throw new AssertionError();
    }

    @Shadow
    static Optional<Vec3> firstIntersection(Vec3 start, Vec3 end, AABB box) {
        throw new AssertionError();
    }

    @Shadow
    private static void destroyLine(ServerLevel level, ServerPlayer player, Vec3 origin, Vec3 direction,
                                    double maxDistance, double energy) {
        throw new AssertionError();
    }

    @Shadow
    private void reflectDamage(ServerPlayer reflector) {
        throw new AssertionError();
    }

    /** 把学院私有的单条射线破坏逻辑暴露给 {@link GTRailgunDestruction} 分帧调用。 */
    @Override
    public void gt$destroyRay(ServerLevel level, ServerPlayer player, Vec3 origin, Vec3 direction,
                              double maxDistance, double energy) {
        destroyLine(level, player, origin, direction, maxDistance, energy);
    }

    /** 被超电磁炮打掉的方块不掉落物品。 */
    @Redirect(method = "destroyLine",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"))
    private static boolean gt$noBlockDrops(ServerLevel level, BlockPos pos, boolean drop, Entity entity) {
        return level.destroyBlock(pos, false, entity);
    }

    @Inject(method = "tryRelease", at = @At("HEAD"))
    private static void gt$resetAmmo(ServerPlayer player, PlayerAbilityData data, int ticks, CallbackInfoReturnable<Boolean> cir) {
        // 默认（铁锭等 ITEM 来源）不使用任何硬币倍率
        gt$ammoMultiplier = 1.0D;
    }

    @Redirect(method = "tryRelease",
            at = @At(value = "INVOKE",
                    target = "Lcom/mohistmc/academy/world/entity/CoinEntity;consumeForRailgun(Lnet/minecraft/world/entity/player/Player;)Z"))
    private static boolean gt$captureAmmo(CoinEntity coin, Player player) {
        gt$ammoMultiplier = GTThermalRailgunCoins.multiplierOf(coin);
        return coin.consumeForRailgun(player);
    }

    /**
     * @author GoldenTweaks
     * @reason 让硬币倍率同时放大伤害、破坏能量、破坏半径与射程，并把倍率同步给光束实体用于缩放可见光束的粗细与长度。
     */
    @Overwrite
    private void performRailgun(ServerPlayer player, PlayerAbilityData data, float exp) {
        double m = gt$ammoMultiplier;
        ServerLevel level = player.serverLevel();
        float damage = (float) (MathUtils.lerpf(60.0F, 110.0F, exp) * m);
        double range = 50.0D * m;
        double radius = 2.0D * m;
        double detect = 2.4D * m;
        Vec3 lookVec = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition(0.0F);
        Vec3 rightVec = lookVec.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
        Vec3 visualStart = eyePos.add(rightVec.scale(0.3D)).add(0.0D, -0.2D, 0.0D).add(lookVec.scale(0.3D));
        Vec3 rayStart = eyePos.add(lookVec.scale(0.1D));
        Vec3 end = rayStart.add(lookVec.scale(range));
        List<LivingEntity> targets = new ArrayList<>(
                level.getEntitiesOfClass(LivingEntity.class, new AABB(rayStart, end).inflate(detect),
                        e -> e != player && e.isAlive())
        );
        targets.removeIf(
                e -> perpendicularDistance(rayStart, lookVec, e.position()) >= detect
                        || firstIntersection(rayStart, end, e.getBoundingBox().inflate(radius)).isEmpty()
        );
        targets.sort(
                Comparator.comparingDouble(
                        e -> firstIntersection(rayStart, end, e.getBoundingBox().inflate(radius))
                                .map(p -> p.distanceTo(rayStart)).orElse(Double.MAX_VALUE)
                )
        );
        boolean hitEntity = false;
        boolean mayDestroyBlocks = DynamicSkillRules.destroysBlocks(level, "railgun");
        double stopDistance = mayDestroyBlocks ? RailgunEffect.traceBarrier(level, player, rayStart, lookVec, range, gt$UNLIMITED_ENERGY) : range;

        for (LivingEntity target : targets) {
            Optional<Vec3> intersection = firstIntersection(rayStart, rayStart.add(lookVec.scale(stopDistance)),
                    target.getBoundingBox().inflate(radius));
            if (!intersection.isEmpty()) {
                double distance = intersection.get().distanceTo(rayStart);
                float attenuated = damage * MathUtils.lerpf(1.0F, 0.2F,
                        (float) Math.min(1.0D, perpendicularDistance(rayStart, lookVec, target.position()) / 50.0D));
                hitEntity = true;
                if (target instanceof ServerPlayer reflector && PassiveSkillEventHandler.reflectSpecialRay(reflector, player, attenuated)) {
                    stopDistance = distance;
                    this.reflectDamage(reflector);
                    break;
                }

                AcademyDamageHelper.hurt(player, target, SkillDamageSource.attack(player), DynamicSkillRules.damage("railgun", attenuated));
                EffectHelper.glowBurst(level, target.getX(), target.getEyeY(), target.getZ(), 3, 0.3F, -1996501948, 10, 0.2F);
            }
        }

        RailgunBeamEntity beam = new RailgunBeamEntity(AcademyEntities.RAILGUN_BEAM.get(), level);
        beam.setPos(visualStart.x, visualStart.y, visualStart.z);
        beam.setBeam(visualStart, lookVec, Math.min(45.0D * m, stopDistance));
        ((GTBeamScale) beam).gt$setBeamScale((float) m);
        level.addFreshEntity(beam);
        Vec3 up = Math.abs(lookVec.y) > 0.95D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = lookVec.cross(up).normalize();
        Vec3 vertical = right.cross(lookVec).normalize();
        List<Vec3> origins = new ArrayList<>();

        for (double x = -radius; x <= radius; x += gt$SAMPLE_STEP) {
            for (double y = -radius; y <= radius; y += gt$SAMPLE_STEP) {
                if (x * x + y * y <= radius * radius) {
                    origins.add(rayStart.add(right.scale(x)).add(vertical.scale(y)));
                }
            }
        }

        if (mayDestroyBlocks) {
            // 由中心向两边：按到中轴的距离排序，队列按此顺序分帧执行，破坏区从中心一圈圈向外扩展。
            origins.sort(Comparator.comparingDouble(origin -> origin.distanceToSqr(rayStart)));
            // 破坏区很大，交给 GTRailgunDestruction 在多 tick 里按预算分帧扫完，避免单 tick 卡死服务端。
            GTRailgunDestruction.enqueue(this, level, player, origins, lookVec, stopDistance, gt$UNLIMITED_ENERGY);
        }

        AcademySounds.playSound(level, player.getX(), player.getY(), player.getZ(), AcademySounds.EM_RAILGUN, SoundSource.PLAYERS, 0.5F, 1.0F);
        if (hitEntity) {
            DynamicSkillRules.addExp(player, data, "railgun", 0.01F);
        } else {
            DynamicSkillRules.addExp(player, data, "railgun", 0.005F);
        }
    }
}
