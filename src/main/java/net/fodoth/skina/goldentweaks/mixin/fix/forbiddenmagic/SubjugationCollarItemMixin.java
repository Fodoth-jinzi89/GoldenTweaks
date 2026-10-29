package net.fodoth.skina.goldentweaks.mixin.fix.forbiddenmagic;

import com.spitefulfox.forbiddenmagic.common.item.SubjugationCollarItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import thaumcraft.common.items.TCFunctionalItems;
import thaumcraft.common.vis.TCVisHelper;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * 苦痛项圈被注册成了神秘时代的 vis 护身符（在 {@code thaumcraft:vis_amulets} 标签里），
 * 但和原版魔力存储护身符（{@code TCFunctionalItems.RunicBaubleItem}）不同，它只是一个普通
 * {@code Item}，没有实现 {@link ICurioItem}。于是 Curios 从不会 tick 它，既不会把储存的 vis
 * 注入手持法杖，也不会从附近的 vis 中继器回充。
 *
 * <p>这里补上 curio 能力，并复用原版护身符 {@code curioTick} 一模一样的两步逻辑。</p>
 */
@Mixin(value = SubjugationCollarItem.class, remap = false)
public abstract class SubjugationCollarItemMixin implements ICurioItem {

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity().level().isClientSide || slotContext.entity().tickCount % 5 != 0) {
            return;
        }

        ItemStack held = slotContext.entity().getMainHandItem();
        if (held.getItem() instanceof TCFunctionalItems.WandCastingItem) {
            TCVisHelper.transferAmuletToHeldWand(stack, held);
        }

        TCVisHelper.rechargeAmuletFromNearestRelay(slotContext.entity(), stack);
    }
}
