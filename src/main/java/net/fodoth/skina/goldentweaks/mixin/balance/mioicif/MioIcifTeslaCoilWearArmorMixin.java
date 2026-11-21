package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.common.blockentity.producer.mio_icif_tesla;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 特斯拉线圈磨损护甲：原版只磨损玩家的护甲，这里对任意被电击的活体统一磨损其四个护甲槽。
 * 以上行为受配置项控制，默认开启；关闭时保持原版仅玩家生效。
 */
@Mixin(value = mio_icif_tesla.class, remap = false)
public abstract class MioIcifTeslaCoilWearArmorMixin {

    @Inject(method = "damageArmor", at = @At("HEAD"), cancellable = true)
    private void gt$wearArmorOnAnyTarget(LivingEntity entity, Level level, CallbackInfo ci) {

        if (!GoldenTweaksCommonConfig.isMioIcifTeslaCoilWearArmor()) {
            return;
        }

        int damage = 4 + level.random.nextInt(3);

        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        }) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem) {
                stack.hurtAndBreak(damage, entity, slot);
            }
        }

        ci.cancel();
    }
}
