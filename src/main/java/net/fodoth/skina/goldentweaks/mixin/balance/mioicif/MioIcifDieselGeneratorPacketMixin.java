package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.core.api.energy.EnergyNodeRegistry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EnergyNodeRegistry.class, remap = false)
public abstract class MioIcifDieselGeneratorPacketMixin {

    private static final ResourceLocation DIESEL_GENERATOR =
            ResourceLocation.fromNamespaceAndPath("mio_icif", "generator/block_diesel_generator");

    @Inject(method = "generatorPacket", at = @At("HEAD"), cancellable = true)
    private static void gt$dieselGeneratorPacket(ResourceLocation blockId, long fallback,
                                                  CallbackInfoReturnable<Long> cir) {
        if (DIESEL_GENERATOR.equals(blockId)) {
            cir.setReturnValue(2048L);
        }
    }
}
