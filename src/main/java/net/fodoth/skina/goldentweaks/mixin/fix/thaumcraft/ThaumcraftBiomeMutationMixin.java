package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTTaintConversionGuard;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.common.worldgen.TCBiomes;
import thaumcraft.common.worldgen.ThaumcraftBiomeMutation;

/**
 * "Disable taint spread" config option: every runtime conversion of a column into the tainted land
 * biome goes through {@code ThaumcraftBiomeMutation.setColumn(ServerLevel, ...)}, so cancelling this
 * single overload stops taint fibres, flux goo, taint creatures and thrown taint from expanding the
 * biome. World generation uses the separate {@code WorldGenLevel} overload and is left untouched,
 * and the aura nodes' own conversion is governed by their independent options (it is allowed through
 * here via {@link GTTaintConversionGuard}).
 */
@Mixin(value = ThaumcraftBiomeMutation.class, remap = false)
public class ThaumcraftBiomeMutationMixin {

    @Inject(
            method = "setColumn(Lnet/minecraft/server/level/ServerLevel;IILnet/minecraft/resources/ResourceKey;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void gt$disableTaintSpread(
            ServerLevel level,
            int x,
            int z,
            ResourceKey<Biome> biome,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!GTTaintConversionGuard.isNodeConversion()
                && GoldenTweaksCommonConfig.isTaintSpreadDisabled()
                && TCBiomes.TAINTED_LAND.equals(biome)) {
            cir.setReturnValue(false);
        }
    }
}
