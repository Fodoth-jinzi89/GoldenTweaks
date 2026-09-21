package net.fodoth.skina.goldentweaks.mixin.shut.oei;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.util.ThrottledForwardingLogger;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * OEI 自己的日志很吵（每条替换规则一条 INFO、每个物品栈一条 DEBUG），但直接静音会丢掉有价值的
 * 警告。这里转发到 GoldenTweaks 的 logger，并去掉 DEBUG/TRACE、对 INFO/WARN 做去重与配额限制。
 */
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
        return new ThrottledForwardingLogger(GoldenTweaks.LOGGER, "[OEI] ");
    }
}
