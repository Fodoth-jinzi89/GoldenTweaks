package net.fodoth.skina.goldentweaks.mixin.feature.compactmachines;

import dev.compactmods.machines.api.room.RoomDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(RoomDimensions.class)
public class RoomDimensionsMixin {

    @ModifyConstant(method = "lambda$static$0", constant = @Constant(intValue = 45), require = 3)
    private static int gt$removeCodecRoomSizeLimit(int original) {
        return Integer.MAX_VALUE;
    }

    @ModifyConstant(method = "<init>(I)V", constant = @Constant(intValue = 45), require = 3)
    private static int gt$removeCubicRoomSizeLimit(int original) {
        return Integer.MAX_VALUE;
    }
}
