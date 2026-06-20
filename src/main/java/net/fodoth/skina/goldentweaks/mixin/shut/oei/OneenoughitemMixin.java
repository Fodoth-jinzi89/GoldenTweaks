package net.fodoth.skina.goldentweaks.mixin.shut.oei;

import net.fodoth.skina.goldentweaks.util.NoOpLogger;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.mafuyu404.oneenoughitem.Oneenoughitem")
public class OneenoughitemMixin {

    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/LogManager;getLogger()Lorg/apache/logging/log4j/Logger;"
            ),
            remap = false
    )
    private static Logger replaceLogger() {
        return new NoOpLogger();
    }
}
