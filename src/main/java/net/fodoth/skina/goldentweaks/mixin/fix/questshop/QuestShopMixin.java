package net.fodoth.skina.goldentweaks.mixin.fix.questshop;

import com.holysweet.questshop.QuestShop;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(QuestShop.class)
public abstract class QuestShopMixin {

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/holysweet/questshop/integrations/IntegrationBootstrap;bootstrap()V"
            )
    )
    private void goldentweaks$delayBootstrap(
            Operation<Void> original
    ) {
        GoldenTweaks.LOGGER.info(
                "Prevented QuestShop early integration bootstrap"
        );
    }
}
