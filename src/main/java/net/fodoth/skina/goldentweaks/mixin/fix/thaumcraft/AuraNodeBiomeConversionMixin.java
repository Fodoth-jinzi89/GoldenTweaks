package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTTaintConversionGuard;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.common.blockentities.AuraNodeBlockEntity;
import thaumcraft.common.worldgen.ThaumcraftBiomeMutation;

/**
 * Per-node biome conversion options. Each aura node type converts a column of the nearby biome in
 * its own tick, all through the same {@code setColumn} call:
 * <ul>
 *     <li>pure nodes cleanse tainted land into the magical forest;</li>
 *     <li>sinister (dark) nodes convert the surroundings into the eerie biome;</li>
 *     <li>tainted nodes convert the surroundings into the tainted land biome.</li>
 * </ul>
 * Skipping the call keeps the node's particles, discharge and (for sinister nodes) its summoning
 * behaviour intact while leaving the biome unchanged. The tainted node's conversion also bypasses
 * the global "disable taint spread" gate, so the two options stay independent.
 */
@Mixin(value = AuraNodeBlockEntity.class, remap = false)
public class AuraNodeBiomeConversionMixin {

    @Redirect(
            method = "handlePureNode",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/common/worldgen/ThaumcraftBiomeMutation;setColumn(Lnet/minecraft/server/level/ServerLevel;IILnet/minecraft/resources/ResourceKey;)Z"
            )
    )
    private boolean gt$pureNodeBiomeConversion(ServerLevel level, int x, int z, ResourceKey<Biome> biome) {
        if (GoldenTweaksCommonConfig.isPureNodeBiomeConversionDisabled()) {
            return false;
        }

        return ThaumcraftBiomeMutation.setColumn(level, x, z, biome);
    }

    @Redirect(
            method = "handleDarkNode",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/common/worldgen/ThaumcraftBiomeMutation;setColumn(Lnet/minecraft/server/level/ServerLevel;IILnet/minecraft/resources/ResourceKey;)Z"
            )
    )
    private boolean gt$sinisterNodeBiomeConversion(ServerLevel level, int x, int z, ResourceKey<Biome> biome) {
        if (GoldenTweaksCommonConfig.isSinisterNodeBiomeConversionDisabled()) {
            return false;
        }

        return ThaumcraftBiomeMutation.setColumn(level, x, z, biome);
    }

    @Redirect(
            method = "handleTaintNode",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/common/worldgen/ThaumcraftBiomeMutation;setColumn(Lnet/minecraft/server/level/ServerLevel;IILnet/minecraft/resources/ResourceKey;)Z"
            )
    )
    private boolean gt$taintedNodeBiomeConversion(ServerLevel level, int x, int z, ResourceKey<Biome> biome) {
        if (GoldenTweaksCommonConfig.isTaintedNodeBiomeConversionDisabled()) {
            return false;
        }

        GTTaintConversionGuard.beginNodeConversion();
        try {
            return ThaumcraftBiomeMutation.setColumn(level, x, z, biome);
        } finally {
            GTTaintConversionGuard.endNodeConversion();
        }
    }
}
