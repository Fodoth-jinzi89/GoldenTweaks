package net.fodoth.skina.goldentweaks.mixin.fix.fancymenu;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Set;

@Pseudo
@Mixin(targets = "de.keksuccino.fancymenu.util.resource.ClientResourceIndexBuilder", remap = false)
public abstract class ClientResourceIndexBuilderMixin {

    @ModifyArg(method = "build", at = @At(value = "INVOKE",
            target = "Ljava/util/Collections;unmodifiableSet(Ljava/util/Set;)Ljava/util/Set;"), index = 0)
    private static Set<?> gt$compactResourceIndex(Set<?> locations) {
        return ImmutableSet.copyOf(locations);
    }
}
