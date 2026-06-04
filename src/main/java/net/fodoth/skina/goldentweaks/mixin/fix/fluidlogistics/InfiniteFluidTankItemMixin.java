package net.fodoth.skina.goldentweaks.mixin.fix.fluidlogistics;

import com.simibubi.create.infrastructure.config.AllConfigs;
import com.yision.fluidlogistics.item.InfiniteFluidTankItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(InfiniteFluidTankItem.class)
public class InfiniteFluidTankItemMixin {

    @ModifyConstant(
            method = "isInfiniteSupply",
            constant = @Constant(intValue = 10000000)
    )
    private static int goldentweaks$replaceInfiniteSupplyThreshold(int constant) {
        return AllConfigs.server().fluids.hosePulleyBlockThreshold.get() * 1000;
    }

    @ModifyConstant(
            method = "appendHoverText",
            constant = @Constant(intValue = 10000000)
    )
    private int goldentweaks$replaceTooltipThreshold(int constant) {
        return AllConfigs.server().fluids.hosePulleyBlockThreshold.get() * 1000;
    }
}
