package net.fodoth.skina.goldentweaks.mixin.fix.createenchantmentindustry;

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
    private void goldentweaks$fixCEIPonder(
            CallbackInfoReturnable<FluidTankBlockEntity> cir
    ) {
        if (cir.getReturnValue() != null) {
            return;
        }

        boolean ceiPonderCall = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(stream -> stream.anyMatch(frame ->
                        frame.getClassName().equals(
                                "plus.dragons.createenchantmentindustry.client.ponder.scene.MiscScene"
                        )
                ));

        if (ceiPonderCall) {
            cir.setReturnValue((FluidTankBlockEntity) (Object) this);
        }
    }
}