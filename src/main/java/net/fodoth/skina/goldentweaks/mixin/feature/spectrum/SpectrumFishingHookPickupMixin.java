package net.fodoth.skina.goldentweaks.mixin.feature.spectrum;

import de.dafuqs.spectrum.entity.entity.SpectrumFishingHook;
import net.fodoth.skina.goldentweaks.util.ItemPickupUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 光谱「自动收集」在钓鱼钩上的入包路径：{@code SpectrumFishingHook#catchLoot} 命中该附魔时
 * 直接把战利品 {@code Inventory.placeItemBackInInventory}，绕过了拾取事件与满包处理。
 * <p>
 * 这里把这次入包改走 GoldenTweaks 的统一拾取（{@link ItemPickupUtil#collect}），与挖掘 / 生物
 * 掉落保持同一口径；经验结算与拾取音效仍由光谱自己的代码负责。
 */
@Mixin(SpectrumFishingHook.class)
public class SpectrumFishingHookPickupMixin {

    @Redirect(
            method = "catchLoot(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;)V"
            )
    )
    private void gt$collectLoot(Inventory inventory, ItemStack stack) {
        Player player = inventory.player;
        ItemPickupUtil.collect(player, stack, player.level(), player.position());
    }
}
