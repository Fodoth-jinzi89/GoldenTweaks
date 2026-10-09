package net.fodoth.skina.goldentweaks.mixin.fix.thaumicbases;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tb.common.block.AshroomCropBlock;

/**
 * Ashroom spawns its aspect orbs at the block's minimum corner (pos.getX()/getY()/getZ()), so they pop out of the
 * ground corner. Shift every orb this method adds to the centre of the block's bottom face instead.
 * <p>
 * {@code spawnAfterBreak} is the only orb source, so this covers normal breaking as well as the right-click harvest.
 */
@Mixin(value = AshroomCropBlock.class, remap = false)
public abstract class AshroomCropOrbMixin {

    @Redirect(
            method = "spawnAfterBreak",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
            )
    )
    private static boolean goldentweaks$centerOrb(ServerLevel level, Entity entity) {
        entity.setPos(entity.getX() + 0.5, entity.getY(), entity.getZ() + 0.5);
        return level.addFreshEntity(entity);
    }
}
