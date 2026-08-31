package net.fodoth.skina.goldentweaks.mixin.feature.compactmachines;

import dev.compactmods.machines.api.room.template.RoomTemplate;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.compactmods.machines.room.RoomRegistrarData")
public class RoomRegistrarDataMixin {

    @Inject(method = "getNextBoundaries", at = @At("RETURN"), cancellable = true)
    private void gt$alignFourChunkRooms(RoomTemplate template, CallbackInfoReturnable<AABB> cir) {
        if (template.internalDimensions().width() != 64 || template.internalDimensions().depth() != 64) {
            return;
        }

        AABB boundaries = cir.getReturnValue();
        double innerMinX = boundaries.minX + 1.0D;
        double innerMinZ = boundaries.minZ + 1.0D;
        double offsetX = Math.floor(innerMinX / 16.0D) * 16.0D - innerMinX;
        double offsetZ = Math.floor(innerMinZ / 16.0D) * 16.0D - innerMinZ;
        cir.setReturnValue(boundaries.move(offsetX, 0.0D, offsetZ));
    }
}
