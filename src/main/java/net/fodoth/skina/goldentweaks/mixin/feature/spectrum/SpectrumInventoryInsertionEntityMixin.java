package net.fodoth.skina.goldentweaks.mixin.feature.spectrum;

import de.dafuqs.spectrum.helpers.SpectrumEnchantmentHelper;
import de.dafuqs.spectrum.registries.SpectrumEnchantmentKeys;
import net.fodoth.skina.goldentweaks.util.ItemPickupUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 光谱「自动收集」的生物掉落路径：光谱在 {@code Entity#spawnAtLocation(ItemStack)} 上直接
 * {@code Player.getInventory().add} 并取消掉落物生成，绕过了拾取事件与满包处理。
 * <p>
 * 这里在同一入口先接管：条件与光谱一致（死亡中的生物、击杀者为持有「自动收集」的玩家），
 * 改为生成掉落物后走 GoldenTweaks 的统一拾取（{@link ItemPickupUtil#collect}）并取消原逻辑。
 * <p>
 * {@code priority = 500} 低于光谱默认的 1000：同点注入按优先级升序应用，先应用者先执行并
 * 取消后，光谱的注入会被取消检查跳过；顺序相反时最多是光谱先处理（不会重复入包）。
 */
@Mixin(value = Entity.class, priority = 500)
public class SpectrumInventoryInsertionEntityMixin {

    @Inject(
            method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gt$collectIntoInventory(ItemStack stack, CallbackInfoReturnable<ItemEntity> cir) {

        Entity self = (Entity) (Object) this;

        if (!(self instanceof LivingEntity living) || !living.isDeadOrDying()) {
            return;
        }

        if (living.level().isClientSide()) {
            return;
        }

        if (!(living.getLastHurtByMob() instanceof Player player)) {
            return;
        }

        int level = SpectrumEnchantmentHelper.getEquipmentLevel(
                player.level().registryAccess(),
                SpectrumEnchantmentKeys.INVENTORY_INSERTION,
                player
        );

        if (level <= 0) {
            return;
        }

        ItemPickupUtil.collect(player, stack, living.level(), living.position());

        cir.setReturnValue(null);
        cir.cancel();
    }
}
