package net.fodoth.skina.goldentweaks.mixin.fix.fluidlogistics;

import com.simibubi.create.infrastructure.config.AllConfigs;
import com.yision.fluidlogistics.block.InfiniteFluidTank.InfiniteFluidTankBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(InfiniteFluidTankBlockEntity.class)
public abstract class InfiniteFluidTankBlockEntityMixin {

    @ModifyConstant(
            method = "<init>",
            constant = @Constant(intValue = 10000000)
    )
    private int goldentweaks$replaceCtorCapacity(int original) {
        return AllConfigs.server().fluids.hosePulleyBlockThreshold.get() * 1000;
    }

    @ModifyConstant(
            method = "read",
            constant = @Constant(intValue = 10000000)
    )
    private int goldentweaks$replaceReadCapacity(int original) {
        return AllConfigs.server().fluids.hosePulleyBlockThreshold.get() * 1000;
    }
}
