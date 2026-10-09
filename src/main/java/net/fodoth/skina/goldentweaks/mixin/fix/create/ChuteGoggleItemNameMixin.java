package net.fodoth.skina.goldentweaks.mixin.fix.create;

import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Create 的溜槽护目镜信息在显示「包含的物品」时，用的是
 * {@code Component.translatable(item.getDescriptionId())}——直接翻译物品的描述 id 且不传任何参数。
 * 对精妙存储这类「显示名把木材塞进 %s 占位」的物品（如 {@code 限类%s木桶}），不带参数翻译就会原样
 * 残留字面 {@code %s}，于是护目镜里显示成「限类%s木桶」。这里改成取物品自身已解析好的
 * {@link ItemStack#getHoverName()}，即与物品栏里显示的名字一致。
 */
@Mixin(value = ChuteBlockEntity.class, remap = false)
public abstract class ChuteGoggleItemNameMixin {

    @Shadow
    private ItemStack item;

    @Redirect(
            method = "addToGoggleTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent gt$useItemHoverName(String descriptionId) {
        return Component.literal(item.getHoverName().getString());
    }
}
