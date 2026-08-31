package net.fodoth.skina.goldentweaks.mixin.feature.compactmachines;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;

@Mixin(targets = "dev.compactmods.machines.client.creative.CreativeTabs")
public interface CreativeTabsMixin {

    @Redirect(
            method = "fillItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/HolderLookup$RegistryLookup;listElements()Ljava/util/stream/Stream;"
            )
    )
    private static Stream<? extends Holder.Reference<?>> gt$hideVariantMachines(HolderLookup.RegistryLookup<?> lookup) {
        return lookup.listElements().filter(holder -> holder.key().location().getNamespace().equals("compactmachines"));
    }
}
