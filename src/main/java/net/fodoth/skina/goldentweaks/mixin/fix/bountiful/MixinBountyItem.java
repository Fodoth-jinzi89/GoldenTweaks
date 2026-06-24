package net.fodoth.skina.goldentweaks.mixin.fix.bountiful;

import io.ejekta.bountiful.bounty.BountyRarity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import io.ejekta.bountiful.content.item.BountyItem;
import io.ejekta.bountiful.components.BountyStack;

@Mixin(BountyItem.class)
public abstract class MixinBountyItem {

    /**
     * @author Fodoth_jinzi89
     * @reason i18n
     */
    @Overwrite
    public @NotNull Component getName(ItemStack stack) {



        if (Minecraft.getInstance().level == null) {

            return Component.translatable("bountiful.bounty");
        }

        BountyStack info = new BountyStack(stack);
        BountyRarity rarity = info.getInfo().rarity();


        String key = "item.bountiful.bounty.rarity." + rarity.name().toLowerCase();


        return Component.translatable(key)
                .withStyle(rarity.getColor());
    }
}