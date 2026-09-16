package net.fodoth.skina.goldentweaks.mixin.fix.hazennstuff;

import net.hazen.hazennstuff.Item.Curios.Misc.PendantOfHarmonyCurio;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 同 {@link SpectrumItemMixin}：和谐吊坠的彩虹物品名在专用服务器上会加载客户端 Minecraft 而崩服。
 */
@Mixin(PendantOfHarmonyCurio.class)
public abstract class PendantOfHarmonyCurioMixin {

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
