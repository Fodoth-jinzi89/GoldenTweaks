package net.fodoth.skina.goldentweaks.mixin.fix.createaerofreight;

import cn.blockforge.generated.createareofreight.OrderIntroduction;
import cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig;
import net.fodoth.skina.goldentweaks.compat.createaerofreight.GTProfessionIntroductions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = OrderIntroductionConfig.class, remap = false)
public abstract class OrderIntroductionConfigMixin {
    @Inject(method = "resolve(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIIILjava/lang/String;JLjava/lang/String;Ljava/lang/String;)Lcn/blockforge/generated/createareofreight/OrderIntroduction;",
            at = @At("RETURN"), cancellable = true)
    private static void gt$personalText(String id, String type, String cargo, String cargoTemplate,
            int width, int height, int length, int distance, String destination, long seed,
            String template, String content, CallbackInfoReturnable<OrderIntroduction> cir) {
        OrderIntroduction base = cir.getReturnValue();
        // A letter writer may be a guest or a ghostwriter from another shop.
        // Personal content IDs must not enter this branch again during re-resolution.
        if (!GTProfessionIntroductions.TEMPLATE.equals(template) || content == null || !content.endsWith("_sell")
                || base.commissionerId().isBlank()) return;
        OrderIntroduction personal = OrderIntroductionConfig.resolve(id, type, cargo, cargoTemplate,
                width, height, length, distance, destination, seed, template, base.commissionerId());
        if (!personal.templateId().isBlank()) cir.setReturnValue(personal);
    }
}
