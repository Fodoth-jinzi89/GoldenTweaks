package net.fodoth.skina.goldentweaks.mixin.fix.ae2peat;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

import javax.annotation.Nullable;

@Mixin(targets = "appeng.integration.modules.emi.EmiItemStackConverter")
@Pseudo
public class EmiItemStackConverterMixin {

    /**
     * @author Fodoth_jinzi89
     * @reason Prevent AEItemKey null crash in EMI integration
     */
    @Overwrite
    public @Nullable @org.jetbrains.annotations.Nullable GenericStack toGenericStack(EmiStack stack) {
        Item item = stack.getKeyOfType(Item.class);

        if (item == null || item == Items.AIR) {
            return null;
        }

        AEItemKey itemKey = AEItemKey.of(stack.getItemStack());

        if (itemKey == null) {
            return null;
        }

        long amount = stack.getAmount();

        if (amount <= 0) {
            return null;
        }

        return new GenericStack(itemKey, amount);
    }
}
