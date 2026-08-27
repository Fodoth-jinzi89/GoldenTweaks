package net.fodoth.skina.goldentweaks.mixin.fix.aeallpattern;

import io.github.langqi99.aeallpattern.aggregate.AggregatePatternExpander;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AggregatePatternExpander.class)
public abstract class AggregatePatternExpanderMixin {
    @Redirect(
            method = "expand",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"
            )
    )
    private static void gt$suppressRejectedChild(Logger logger, String message, Object recipeId) {
    }

    @Redirect(
            method = "expand",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;[Ljava/lang/Object;)V"
            )
    )
    private static void gt$debugExpansionFailure(Logger logger, String message, Object[] arguments) {
        logger.debug(message, arguments);
    }
}
