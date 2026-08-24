package net.fodoth.skina.goldentweaks.mixin.shut;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.IrisLogging;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Iris.class, remap = false)
public abstract class IrisPipelineErrorMixin {

    @Shadow
    private static void handleException(Exception exception) {
        throw new AssertionError();
    }

    @Redirect(
            method = "createPipeline",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/Iris;handleException(Ljava/lang/Exception;)V"
            )
    )
    private static void gt$suppressZeroWidthChat(Exception exception) {
        if (!gt$isZeroWidthError(exception)) {
            handleException(exception);
        }
    }

    @Redirect(
            method = "createPipeline",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/IrisLogging;error(Ljava/lang/String;Ljava/lang/Throwable;)V"
            )
    )
    private static void gt$suppressZeroWidthLog(IrisLogging logger, String message, Throwable throwable) {
        if (!gt$isZeroWidthError(throwable)) {
            logger.error(message, throwable);
        }
    }

    private static boolean gt$isZeroWidthError(Throwable throwable) {
        return throwable instanceof IllegalArgumentException
                && "Width must be greater than zero".equals(throwable.getMessage());
    }
}
