package net.fodoth.skina.goldentweaks.mixin.feature.thaumichorizons;

import com.kentington.thaumichorizons.common.planar.PlanarHazards;
import net.fodoth.skina.goldentweaks.compat.thaumichorizons.GTVortexOutputs;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Applies two GoldenTweaks rules to the rift's planar hazards:
 * <ul>
 *     <li>the "rifts break blocks" config option gates every terrain removal the rift performs
 *     ({@code remove} is the only place the rift destroys blocks);</li>
 *     <li>freshly crafted outputs are filtered out of the hungry field so the rift never swallows
 *     what it just produced.</li>
 * </ul>
 */
@Mixin(value = PlanarHazards.class, remap = false)
public class PlanarHazardsMixin {

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private static void gt$keepTerrain(
            ServerLevel level,
            BlockEntity blockEntity,
            UUID owner,
            BlockPos pos,
            boolean first,
            boolean second,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!GoldenTweaksCommonConfig.doesHorizonsVortexBreakBlocks()) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(
            method = "hungry",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;I)V"
            )
    )
    private static void gt$skipFreshOutputs(
            ServerLevel level,
            EntityTypeTest<Entity, ?> typeTest,
            AABB area,
            Predicate<? super Entity> predicate,
            List<Entity> output,
            int maxResults
    ) {
        level.getEntities(typeTest, area, predicate, output, maxResults);
        output.removeIf(entity -> GTVortexOutputs.isFresh(entity.getUUID()));
    }
}
