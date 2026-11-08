package net.fodoth.skina.goldentweaks.mixin.fix.create;

import com.simibubi.create.compat.jei.category.ItemDrainCategory;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Create 的「排液」（Item Drain）JEI 页会把所有带流体能力的物品都排空一次来生成展示配方，
 * 排完后用 {@link ItemHelper#sameItem} 判断容器是否「还是原来那个物品」；是的话直接把**输入物品**
 * 当成产物塞进结果槽。
 * <p>
 * 但 {@code sameItem} 只比较物品类型、忽略数据组件。mio_icif 的单元正是「同一个物品
 * {@code cell/item_cell_empty} + 流体组件」，排空前后物品相同、组件不同，于是排液页把注满冷却液的
 * 单元当成了产物（游戏内 id 形如 {@code create:empty_mio_icif_cell/item_cell_empty_of_mio_icif_coolant}），
 * 看起来像没排空。
 * <p>
 * 这里把判定收紧为「同物品且同组件」：排空后组件确实变了时改走正常分支，用真正的容器（空单元）当产物。
 */
@Mixin(ItemDrainCategory.class)
public class ItemDrainCategorySameItemMixin {

    @Redirect(
            method = "consumeRecipes",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/item/ItemHelper;sameItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private static boolean goldentweaks$sameItemAndComponents(ItemStack input, ItemStack drained) {
        return ItemHelper.sameItem(input, drained) && ItemStack.isSameItemSameComponents(input, drained);
    }
}
