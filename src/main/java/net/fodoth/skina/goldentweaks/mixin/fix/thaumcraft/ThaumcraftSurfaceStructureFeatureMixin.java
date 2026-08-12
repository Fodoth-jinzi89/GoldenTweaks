package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import thaumcraft.common.worldgen.ThaumcraftSurfaceStructureFeature;

@Mixin(value = ThaumcraftSurfaceStructureFeature.class, remap = false)
public class ThaumcraftSurfaceStructureFeatureMixin {

    @ModifyConstant(
            method = "selectStructure",
            constant = @Constant(intValue = 150)
    )
    private static int reduceMoundRarity(int original) {
        return 1500; 
    }

    @ModifyConstant(
            method = "selectStructure",
            constant = @Constant(intValue = 40)
    )
    private static int reduceHilltopShrineRarity(int original) {
        return 400; 
    }

    @ModifyConstant(
            method = "selectStructure",
            constant = @Constant(intValue = 66)
    )
    private static int reduceEldritchRingRarity(int original) {
        return 660;
    }
}
