package net.fodoth.skina.goldentweaks.mixin.fix.ali;

import com.yanny.ali.compatibility.common.GenericUtils;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.network.chat.Component;

@Mixin(GenericUtils.class)
public class GenericUtilsMixin {


    @Inject(
            method = "ellipsis(Ljava/lang/String;Ljava/lang/String;I)Lnet/minecraft/network/chat/Component;",
            at = @At("HEAD")
    )
    private static void goldenTweaks$logText(
            String text,
            String fallback,
            int maxWidth,
            CallbackInfoReturnable<Component> cir
    ) {
        //GoldenTweaks.LOGGER.info("[ALI] ellipsis text={}", text);
    }
}