package net.fodoth.skina.goldentweaks.mixin.fix;


import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import plus.dragons.createenchantmentindustry.client.ponder.scene.MiscScene;


@Mixin(MiscScene.class)
public class MiscSceneMixin {

    @Redirect(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/fluids/tank/FluidTankBlockEntity;getControllerBE()Lcom/simibubi/create/content/fluids/tank/FluidTankBlockEntity;"
            )
    )
    private static FluidTankBlockEntity goldentweaks$fixNullController(FluidTankBlockEntity be) {

        FluidTankBlockEntity controller = be.getControllerBE();

        return controller != null ? controller : be;
    }
}
