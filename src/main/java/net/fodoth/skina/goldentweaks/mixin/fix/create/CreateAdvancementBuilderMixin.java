package net.fodoth.skina.goldentweaks.mixin.fix.create;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.simibubi.create.foundation.advancement.CreateAdvancement$Builder")
public class CreateAdvancementBuilderMixin {

    @Redirect(
            method = "icon(Lcom/tterrag/registrate/util/entry/ItemProviderEntry;)Lcom/simibubi/create/foundation/advancement/CreateAdvancement$Builder;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tterrag/registrate/util/entry/ItemProviderEntry;asStack()Lnet/minecraft/world/item/ItemStack;"
            ),
            remap = false
    )
    private ItemStack goldentweaks$fallbackIcon(ItemProviderEntry<?, ?> entry) {
        try {
            return entry.asStack();
        } catch (Throwable t) {
            return new ItemStack(Items.COPPER_INGOT);
        }
    }

    @Redirect(
            method = "whenItemCollected(Lcom/tterrag/registrate/util/entry/ItemProviderEntry;)Lcom/simibubi/create/foundation/advancement/CreateAdvancement$Builder;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tterrag/registrate/util/entry/ItemProviderEntry;asStack()Lnet/minecraft/world/item/ItemStack;"
            ),
            remap = false
    )
    private ItemStack goldentweaks$fallbackCollected(ItemProviderEntry<?, ?> entry) {
        try {
            return entry.asStack();
        } catch (Throwable t) {
            return new ItemStack(Items.COPPER_INGOT);
        }
    }
}