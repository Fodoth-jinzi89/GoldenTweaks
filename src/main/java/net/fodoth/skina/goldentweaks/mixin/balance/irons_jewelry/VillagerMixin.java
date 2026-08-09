package net.fodoth.skina.goldentweaks.mixin.balance.irons_jewelry;

import io.redspace.ironsjewelry.core.bonuses.TradeDiscountBonusType;
import io.redspace.ironsjewelry.registry.BonusTypeRegistry;
import io.redspace.ironsjewelry.utils.Utils;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public class VillagerMixin {

    @Inject(
            method = "updateSpecialPrices",
            at = @At("RETURN")
    )
    private void gt$handleHagglerBonus(Player player, CallbackInfo ci) {

        int totalDiscount = Utils.getEquippedBonuses(player)
                .stream()
                .filter(bonus -> bonus.bonusType().equals(BonusTypeRegistry.TRADE_DISCOUNT_BONUS.get()))
                .mapToInt(bonus ->
                        ((TradeDiscountBonusType) bonus.bonusType())
                                .getItemDiscount(bonus.quality())
                )
                .sum();


        if (totalDiscount <= 0) {
            return;
        }


        for (MerchantOffer offer : ((Villager) (Object) this).getOffers()) {

            int originalPrice = offer.getBaseCostA().getCount();

            int maxDiscountByPercent = (int)(originalPrice * GoldenTweaksCommonConfig.getIJHagglerMaxDiscountPercentage());

            int finalDiscount = Math.min(
                    totalDiscount,
                    Math.min(GoldenTweaksCommonConfig.getIJHagglerMaxDiscount(), maxDiscountByPercent)
            );

            offer.addToSpecialPriceDiff(-finalDiscount);
        }
    }
}