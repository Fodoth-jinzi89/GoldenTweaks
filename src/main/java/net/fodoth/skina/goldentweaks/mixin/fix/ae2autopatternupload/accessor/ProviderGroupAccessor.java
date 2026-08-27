package net.fodoth.skina.goldentweaks.mixin.fix.ae2autopatternupload.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "com.gali.ae2_auto_pattern_upload.client.ProviderSelectScreen$Group", remap = false)
public interface ProviderGroupAccessor {
    @Accessor("name")
    String gt$getName();

    @Accessor("representativeId")
    long gt$getRepresentativeId();

    @Invoker("label")
    String gt$label();
}
