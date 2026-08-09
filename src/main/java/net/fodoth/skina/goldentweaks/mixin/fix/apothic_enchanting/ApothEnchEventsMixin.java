package net.fodoth.skina.goldentweaks.mixin.fix.apothic_enchanting;

import dev.shadowsoffire.apothic_enchanting.ApothEnchEvents;
import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ApothEnchEvents.class)
public class ApothEnchEventsMixin {

    @Inject(
            method = "clamp",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldentweaks$skipBeforeLoaded(
            GetEnchantmentLevelEvent e,
            CallbackInfo ci
    ) {

        if (ApothicEnchanting.ENCHANTMENT_INFO.isEmpty()) {
            ci.cancel();
        }

    }
}
