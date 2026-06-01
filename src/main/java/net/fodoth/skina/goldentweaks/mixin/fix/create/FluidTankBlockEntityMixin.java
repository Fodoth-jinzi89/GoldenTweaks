package net.fodoth.skina.goldentweaks.mixin.fix.create;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidTankBlockEntity.class, remap = false)
public class FluidTankBlockEntityMixin {

    @Inject(
            method = "getControllerBE*",
            at = @At("RETURN"),
            cancellable = true
    )
    private void goldentweaks$neverReturnNull(
            CallbackInfoReturnable<FluidTankBlockEntity> cir
    ) {
        if (cir.getReturnValue() == null) {
            cir.setReturnValue((FluidTankBlockEntity) (Object) this);
        }
    }
}