package net.fodoth.skina.goldentweaks.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class GetEntityHitResult {

    @Nullable
    public static EntityHitResult get(Entity shooter, Vec3 startVec, Vec3 endVec, AABB boundingBox, Predicate<Entity> filter, double distance, double bias) {
        Level level = shooter.level();
        double d0 = distance;
        Entity entity = null;
        Vec3 vec3 = null;

        for(Entity entity1 : level.getEntities(shooter, boundingBox, filter)) {
            AABB aabb = entity1.getBoundingBox().inflate(bias);
            Optional<Vec3> optional = aabb.clip(startVec, endVec);
            if (aabb.contains(startVec)) {
                if (d0 >= (double)0.0F) {
                    entity = entity1;
                    vec3 = optional.orElse(startVec);
                    d0 = 0.0D;
                }
            } else if (optional.isPresent()) {
                Vec3 vec31 = optional.get();
                double d1 = startVec.distanceToSqr(vec31);
                if (d1 < d0 || d0 == 0.0D) {
                    if (entity1.getRootVehicle() == shooter.getRootVehicle() && !entity1.canRiderInteract()) {
                        if (d0 == 0.0D) {
                            entity = entity1;
                            vec3 = vec31;
                        }
                    } else {
                        entity = entity1;
                        vec3 = vec31;
                        d0 = d1;
                    }
                }
            }
        }

        return entity == null ? null : new EntityHitResult(entity, vec3);
    }

    public record TraceHit(
            Entity entity,
            Vec3 hitPos,
            double distanceSqr
    ) {}

    public static List<TraceHit> traceEntities(
            Entity shooter,
            Vec3 startVec,
            Vec3 endVec,
            AABB searchBox,
            Predicate<Entity> filter,

            // 基础碰撞扩张
            double baseBias,

            // 是否允许穿墙
            boolean throughBlocks,

            // 最大命中数量
            int maxHits
    ) {

        Level level = shooter.level();

        List<TraceHit> hits = new ArrayList<>();

        // =========================
        // Block Raycast
        // =========================

        double blockDistanceSqr = Double.MAX_VALUE;

        if (!throughBlocks) {

            BlockHitResult blockHit = level.clip(
                    new ClipContext(
                            startVec,
                            endVec,
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            shooter
                    )
            );

            if (blockHit.getType() != HitResult.Type.MISS) {
                blockDistanceSqr =
                        startVec.distanceToSqr(blockHit.getLocation());
            }
        }

        // =========================
        // Entity Scan
        // =========================

        for (Entity entity : level.getEntities(shooter, searchBox, filter)) {

            // 排除同载具误命中
            if (entity.getRootVehicle() == shooter.getRootVehicle()
                    && !entity.canRiderInteract()) {
                continue;
            }

            // 动态 Bias
            double bias = baseBias + entity.getPickRadius();

            // 特殊实体补偿
            if (entity.isPickable()) {

                // 小实体更容易选中
                if (entity.getBbWidth() < 0.5F) {
                    bias += 0.15D;
                }

                // 高速实体补偿
                if (entity.getDeltaMovement().lengthSqr() > 1.0D) {
                    bias += 0.1D;
                }
            }

            AABB aabb = entity.getBoundingBox().inflate(bias);

            Optional<Vec3> optional = aabb.clip(startVec, endVec);

            Vec3 hitPos = null;

            // 起点就在实体内
            if (aabb.contains(startVec)) {
                hitPos = optional.orElse(startVec);
            }

            // 正常相交
            else if (optional.isPresent()) {
                hitPos = optional.get();
            }

            if (hitPos == null) {
                continue;
            }

            double distanceSqr =
                    startVec.distanceToSqr(hitPos);

            // 被方块遮挡
            if (!throughBlocks && distanceSqr > blockDistanceSqr) {
                continue;
            }

            hits.add(new TraceHit(
                    entity,
                    hitPos,
                    distanceSqr
            ));
        }

        // =========================
        // Sort
        // =========================

        hits.sort(Comparator.comparingDouble(
                TraceHit::distanceSqr
        ));

        // =========================
        // Max Hits Limit
        // =========================

        if (maxHits > 0 && hits.size() > maxHits) {
            return new ArrayList<>(
                    hits.subList(0, maxHits)
            );
        }

        return hits;
    }
}
