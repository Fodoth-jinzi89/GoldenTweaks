package net.fodoth.skina.goldentweaks.mixin.fix.compactmachines;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.data.manager.CMKeyedDataFileManager;
import dev.compactmods.machines.mixin.CodecNbtFunctions;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CMKeyedDataFileManager.class)
public class CMKeyedDataFileManagerMixin {

    @Redirect(
            method = "lambda$save$1",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;store(Ljava/lang/String;Lcom/mojang/serialization/Codec;Ljava/lang/Object;)V"
            )
    )
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void gt$skipInvalidData(CompoundTag tag, String key, Codec codec, Object value) {
        try {
            ((CodecNbtFunctions) (Object) tag).store(key, codec, value);
        } catch (RuntimeException ignored) {
            // Compact Machines can retain incomplete spawn data after room removal.
        }
    }
}
