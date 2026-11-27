package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.common.blockentity.reactor.mio_icif_reactor_nuke;
import net.fodoth.skina.goldentweaks.compat.mioicif.GTNukeFuel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 核弹中心槽改用铀标签判断，不再只认奇点迭代自家的物品类。
 *
 * <p>原版有两处 {@code instanceof mio_icif_nuclear_material}，两处都得改，否则只会修好一半：</p>
 *
 * <ul>
 *   <li>{@code canPlaceItem} — 决定物品能不能放进中心槽。不改这里，外来铀块根本放不进去。</li>
 *   <li>{@code getTotalRadioactivity} — 决定爆炸威力里的辐射部分。不改这里，即便塞进去也按
 *       0 辐射计算，核弹只剩 IC_TNT 那点威力，等于白放。</li>
 * </ul>
 *
 * <p>两个方法都很短，整段接管比逐条重定向更清楚，也不会漏掉方法里其他分支。原逻辑保留在
 * {@link GTNukeFuel} 里：自家物品仍取自身辐射值，标签里的外来物品取配置的默认值。</p>
 */
@Mixin(value = mio_icif_reactor_nuke.class, remap = false)
public abstract class MioIcifNukeFuelTagMixin {

    /** 中心槽的槽号（原版 {@code NUCLEAR_SLOT}）。 */
    @Unique
    private static final int gt$nuclearSlot = 8;

    /** 装 IC_TNT 的槽位区间的右开边界（原版 {@code ICTNT_SLOT_COUNT}）。 */
    @Unique
    private static final int gt$icTntSlots = 8;

    @Shadow
    protected com.miophas.singularity_iteration.core.prefab.inventory.MachineItemHandler itemHandler;

    /**
     * 入槽判断：IC_TNT 槽原样走原逻辑，中心槽改看铀标签。
     *
     * <p>只接管中心槽这一种情况。0–7 与其它槽号直接返回不处理，让原方法照常执行，
     * 这样 IC_TNT 的栏位行为一点都不会被碰。</p>
     */
    @Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
    private void gt$canPlaceNuclearFuel(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot == gt$nuclearSlot) {
            cir.setReturnValue(GTNukeFuel.isNuclear(stack));
        }
    }

    /**
     * 辐射量：中心槽里每件物品贡献的数值。
     *
     * <p>外来物品没有 radioactivity 字段，取配置的默认值——否则它们辐射为 0，
     * {@code calculateBaseExplosionPower} 算出的威力会少掉整个辐射项。</p>
     */
    @Inject(method = "getTotalRadioactivity", at = @At("HEAD"), cancellable = true)
    private void gt$totalRadioactivity(CallbackInfoReturnable<Float> cir) {
        ItemStack fuel = this.itemHandler.getStackInSlot(gt$nuclearSlot);
        cir.setReturnValue(fuel.isEmpty() ? 0.0F : GTNukeFuel.radioactivity(fuel) * fuel.getCount());
    }
}
