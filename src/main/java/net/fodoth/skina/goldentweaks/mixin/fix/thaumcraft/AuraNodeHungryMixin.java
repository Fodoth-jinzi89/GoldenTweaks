package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.common.blockentities.AuraNodeBlockEntity;

/**
 * "Hungry nodes break blocks" config option: the port's hungry aura nodes destroy the blocks they
 * consume. Skipping this single call keeps the node's feeding behaviour, sounds and particles but
 * leaves the terrain intact.
 */
@Mixin(value = AuraNodeBlockEntity.class, remap = false)
public class AuraNodeHungryMixin {

    @Redirect(
            method = "handleHungryNodeSecond",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"
            )
    )
    private boolean gt$keepTerrain(ServerLevel level, BlockPos pos, boolean dropItems) {
        if (!GoldenTweaksCommonConfig.doesHungryNodeBreakBlocks()) {
            return false;
        }

        return level.destroyBlock(pos, dropItems);
    }
}
