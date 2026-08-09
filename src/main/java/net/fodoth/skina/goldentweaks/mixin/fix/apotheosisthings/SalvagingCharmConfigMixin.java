package net.fodoth.skina.goldentweaks.mixin.fix.apotheosisthings;

import com.chen1335.apotheosisThings.component.SalvagingCharmConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.injection.At;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(SalvagingCharmConfig.class)
public class SalvagingCharmConfigMixin {


    @ModifyReturnValue(
            method = "rarity",
            at = @At("RETURN")
    )
    private LootRarity fixNull(LootRarity rarity) {

        if (rarity != null)
            return rarity;

        return RarityRegistry.INSTANCE
                .getValue(
                        ResourceLocation
                                .parse("apotheosis:common")
                );
    }
}