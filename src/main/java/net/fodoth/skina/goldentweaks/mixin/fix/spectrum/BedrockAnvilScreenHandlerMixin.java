package net.fodoth.skina.goldentweaks.mixin.fix.spectrum;

import de.dafuqs.spectrum.registries.SpectrumItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "de.dafuqs.spectrum.inventories.BedrockAnvilScreenHandler")
public abstract class BedrockAnvilScreenHandlerMixin extends ItemCombinerMenu {

    @Shadow private int repairItemCount;
    @Shadow @Final private DataSlot levelCost;

    protected BedrockAnvilScreenHandlerMixin() {
        super(null, 0, null, null);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$toggleEnchantmentTooltip(CallbackInfo ci) {
        ItemStack item = inputSlots.getItem(0);
        ItemStack petals = inputSlots.getItem(1);
        if (item.isEmpty() || !petals.is(SpectrumItems.JADEITE_PETALS.get())) {
            return;
        }

        ItemEnchantments enchantments = item.get(DataComponents.ENCHANTMENTS);
        ItemEnchantments stored = item.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchantments == null && stored == null) {
            return;
        }

        ItemStack result = item.copy();
        if (enchantments != null) {
            result.set(DataComponents.ENCHANTMENTS, enchantments.withTooltip(!goldentweaks$showsTooltip(enchantments)));
        }
        if (stored != null) {
            result.set(DataComponents.STORED_ENCHANTMENTS, stored.withTooltip(!goldentweaks$showsTooltip(stored)));
        }
        resultSlots.setItem(0, result);
        repairItemCount = 1;
        levelCost.set(0);
        broadcastChanges();
        ci.cancel();
    }

    private static boolean goldentweaks$showsTooltip(ItemEnchantments enchantments) {
        return enchantments.equals(enchantments.withTooltip(true));
    }
}
