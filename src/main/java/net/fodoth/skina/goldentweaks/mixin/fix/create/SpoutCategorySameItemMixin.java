package net.fodoth.skina.goldentweaks.mixin.fix.create;

import com.simibubi.create.compat.jei.category.SpoutCategory;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Create 的「注液」（Spout）JEI 页同样会遍历所有带流体能力的物品逐一注入来生成展示配方，并用
 * {@link ItemHelper#sameItem}（只比较物品类型、忽略数据组件）判断注液后容器是否「没变化」；
 * 是的话整条配方直接跳过。
 * <p>
 * mio_icif 的单元是「同一物品 + 流体组件」，注满前后物品类型相同，于是注液页从来不会为它们生成配方。
 * 判定收紧为「同物品且同组件」后，注满确实改变了组件时就会正常出页，产物是注满的单元。
 */
@Mixin(SpoutCategory.class)
public class SpoutCategorySameItemMixin {

    @Redirect(
            method = "consumeRecipes",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/item/ItemHelper;sameItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private static boolean goldentweaks$sameItemAndComponents(ItemStack filled, ItemStack input) {
        return ItemHelper.sameItem(filled, input) && ItemStack.isSameItemSameComponents(filled, input);
    }
}
