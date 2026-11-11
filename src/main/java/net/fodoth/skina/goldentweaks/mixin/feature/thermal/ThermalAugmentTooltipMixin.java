package net.fodoth.skina.goldentweaks.mixin.feature.thermal;

import cofh.thermal.core.client.event.TCoreClientEvents;
import net.fodoth.skina.goldentweaks.compat.thermal.GTThermalFantasyItems;
import net.fodoth.skina.goldentweaks.compat.thermal.ThermalCrystallarieumAugment;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 专业化升级的提示改由 {@code GTSpecializationAugmentItem} 自绘，这里把现代 Thermal 那套「类型 + 属性加成」的
 * 提示块跳过。
 *
 * <p>注入点选在 {@code AugmentDataHelper.hasAugmentData} 这次调用上（而不是方法头），这样 CoFH 前面的
 * 「关键词 / 说明」处理照常执行，只有后面追加机制式行的代码块被取消。</p>
 */
@Mixin(value = TCoreClientEvents.class, remap = false)
public abstract class ThermalAugmentTooltipMixin {

    @Inject(
            method = "handleItemTooltipEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lcofh/core/util/helpers/AugmentDataHelper;hasAugmentData(Lnet/minecraft/world/item/ItemStack;)Z"
            ),
            cancellable = true
    )
    private static void gt$skipSpecializationTooltip(ItemTooltipEvent event, CallbackInfo ci) {
        ItemStack stack = event.getItemStack();
        if (stack.is(ThermalCrystallarieumAugment.CRYSTALLARIEUM_GROWING_AUGMENT.get())
                || stack.is(GTThermalFantasyItems.TRAY_DISTILLATION_EXTRACTION_AUGMENT.get())
                || stack.is(GTThermalFantasyItems.OIL_DISTILLATION_AUGMENT.get())
                || stack.is(GTThermalFantasyItems.AIR_DISTILLATION_AUGMENT.get())
                || stack.is(GTThermalFantasyItems.RARE_METAL_CRYSTALLIZATION_AUGMENT.get())
                || stack.is(GTThermalFantasyItems.LAVA_PRODUCTION_AUGMENT.get())) {
            ci.cancel();
        }
    }
}
