package net.fodoth.skina.goldentweaks.mixin.fix.hazennstuff;

import net.hazen.hazennstuff.Item.Weapons.Generic.Spectrum.SpectrumItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * hazennstuff 的彩虹物品名读 {@code Minecraft}/{@code ClientLevel}，专用服务器上任何取名字的
 * 代码（如 touhou_lost_maid 的 RandomEquipment 在 ServerAboutToStartEvent 里遍历全部物品）
 * 都会撞上 RuntimeDistCleaner 的 “invalid dist DEDICATED_SERVER” 而崩服。
 * 服务端退回上游实现（等价于 super.getName，即 Component.translatable(descriptionId)），
 * 彩虹名只在客户端生效。
 */
@Mixin(SpectrumItem.class)
public abstract class SpectrumItemMixin {

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void gt$serverSafeName(ItemStack stack, CallbackInfoReturnable<Component> cir) {

        if (FMLEnvironment.dist.isClient()) {
            return;
        }

        cir.setReturnValue(
                Component.translatable(((Item) (Object) this).getDescriptionId(stack))
        );
    }
}
