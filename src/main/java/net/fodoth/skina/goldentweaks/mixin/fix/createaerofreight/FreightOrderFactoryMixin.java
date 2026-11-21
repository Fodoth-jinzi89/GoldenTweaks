package net.fodoth.skina.goldentweaks.mixin.fix.createaerofreight;

import cn.blockforge.generated.createareofreight.FreightOrderFactory;
import cn.blockforge.generated.createareofreight.OrderIntroduction;
import cn.blockforge.generated.createareofreight.infrastructure.config.OrderIntroductionConfig;
import cn.blockforge.generated.createareofreight.modifier.ModifierOrderSnapshot;
import net.fodoth.skina.goldentweaks.compat.createaerofreight.GTProfessionIntroductions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = FreightOrderFactory.class, remap = false)
public abstract class FreightOrderFactoryMixin {
    @Redirect(method = "createModifier", at = @At(value = "INVOKE", target = "Lcn/blockforge/generated/createareofreight/infrastructure/config/OrderIntroductionConfig;resolve(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIIILjava/lang/String;J)Lcn/blockforge/generated/createareofreight/OrderIntroduction;"))
    private static OrderIntroduction gt$professionIntroduction(String id, String type, String cargo, String template,
            int width, int height, int length, int distance, String destination, long seed,
            ServerLevel level, BlockPos source, BlockPos target, ModifierOrderSnapshot snapshot) {
        String pool = snapshot.attributes().rewardPool();
        String prefix = "goldentweaks_bountiful_";
        if (pool.startsWith(prefix) && pool.endsWith("_sell")) {
            OrderIntroduction introduction = OrderIntroductionConfig.resolve(id, type, cargo, template,
                    width, height, length, distance, destination, seed, GTProfessionIntroductions.TEMPLATE, pool.substring(prefix.length()));
            if (!introduction.templateId().isBlank()) return introduction;
        }
        return OrderIntroductionConfig.resolve(id, type, cargo, template, width, height, length, distance, destination, seed);
    }
}
