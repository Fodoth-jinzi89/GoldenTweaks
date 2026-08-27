package net.fodoth.skina.goldentweaks.mixin.fix.create;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.simibubi.create.foundation.sound.SoundScapes;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;
import java.util.Set;

@Mixin(value = SoundScapes.class, remap = false)
public class SoundScapesMixin {

    @ModifyReturnValue(method = "getAllLocations", at = @At("RETURN"))
    private static Set<BlockPos> goldentweaks$snapshotSoundLocations(Set<BlockPos> original) {
        return Set.copyOf(Arrays.asList(original.toArray(BlockPos[]::new)));
    }
}
