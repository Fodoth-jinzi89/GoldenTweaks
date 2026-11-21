package net.fodoth.skina.goldentweaks.mixin.fix.apothic_enchanting;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.apothic_enchanting.EnchantmentInfo;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ApothicEnchanting.class, remap = false)
public class ApothicEnchantingMixin {

    @Inject(method = "getEnchInfo", at = @At("HEAD"), cancellable = true)
    private static void gt$fallbackBeforeLoaded(@NotNull Holder<Enchantment> enchantment,
                                              @NotNull CallbackInfoReturnable<EnchantmentInfo> cir) {
        // CC:Tweaked can build creative tabs before enchantment information is available.
        // Do not cache the fallback: subsequent lookups must use the loaded configuration.
        if (ApothicEnchanting.ENCHANTMENT_INFO.isEmpty()) {
            cir.setReturnValue(EnchantmentInfo.fallback(enchantment));
        }
    }
}
