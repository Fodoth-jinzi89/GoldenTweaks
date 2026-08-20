package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "thaumcraft.common.worldgen.ThaumcraftOverworldBiomes", remap = false)
public abstract class ThaumcraftOverworldBiomesMixin {
    @ModifyConstant(method = "partitionOverworldParameters", constant = @Constant(intValue = 3))
    private static int preventTaintedBiomeParameterExplosion(int subdivisions) {
        return 1;
    }
}
