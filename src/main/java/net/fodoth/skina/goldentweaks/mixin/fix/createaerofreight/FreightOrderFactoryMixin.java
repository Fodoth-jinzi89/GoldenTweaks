package net.fodoth.skina.goldentweaks.mixin.fix.createaerofreight;

import cn.blockforge.generated.createareofreight.FreightOrderFactory;
import cn.blockforge.generated.createareofreight.OrderIntroduction;
import cn.blockforge.generated.createareofreight.api.order.OrderIntroductionVariableContext;
import cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig;
import net.fodoth.skina.goldentweaks.compat.createaerofreight.GTProfessionIntroductions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 让职业订单挑到自己的介绍文案。
 *
 * 1.0.x 里 createModifier 直接调 OrderIntroductionConfig.resolve 的十参重载，我们 redirect 那一次调用即可。
 * 1.1.2 把它拆成了「组装 OrderIntroductionVariableContext → resolveIntroduction」，十参重载不再被走到，
 * 于是老的 @Redirect 扫不到目标、启动即崩。这里改拦 resolveIntroduction 内部的五参重载，
 * 顺带用 Context 自带的 rewardPool()——比原来绕 ModifierOrderSnapshot.attributes() 更直接。
 */
@Mixin(value = FreightOrderFactory.class, remap = false)
public abstract class FreightOrderFactoryMixin {
    @Redirect(method = "resolveIntroduction",
            at = @At(value = "INVOKE", target = "Lcn/blockforge/generated/createareofreight/infrastructure/config/OrderIntroductionConfig;resolve(Lcn/blockforge/generated/createareofreight/api/order/OrderIntroductionVariableContext;JLjava/lang/String;Ljava/lang/String;)Lcn/blockforge/generated/createareofreight/OrderIntroduction;"))
    private static OrderIntroduction gt$professionIntroduction(OrderIntroductionVariableContext context, long seed,
            String template, String content) {
        String pool = context.rewardPool();
        String prefix = "goldentweaks_bountiful_";
        if (pool != null && pool.startsWith(prefix) && pool.endsWith("_sell")) {
            OrderIntroduction introduction = OrderIntroductionConfig.resolve(context, seed,
                    GTProfessionIntroductions.TEMPLATE, pool.substring(prefix.length()));
            if (!introduction.templateId().isBlank()) return introduction;
        }
        return OrderIntroductionConfig.resolve(context, seed, template, content);
    }
}
