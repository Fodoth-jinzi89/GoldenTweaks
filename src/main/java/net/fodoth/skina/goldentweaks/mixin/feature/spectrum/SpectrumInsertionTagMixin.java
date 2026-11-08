package net.fodoth.skina.goldentweaks.mixin.feature.spectrum;

import de.dafuqs.spectrum.registries.SpectrumEnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 关掉光谱「自动收集」挖掘掉落的直接入包：光谱的 {@code BlockMixin#spectrum$getDroppedStacks}
 * 用 {@code EnchantmentHelper.hasTag(tool, INVENTORY_INSERTION_EFFECT)} 判定，命中后直接
 * {@code Inventory.add}，绕过了拾取事件与满包处理。
 * <p>
 * 该效果标签只被这一处使用（钓鱼钩与生物掉落路径用的是 {@code SpectrumEnchantmentKeys} 里的
 * 附魔 key），因此这里让这个标签的判定恒为 {@code false}，光谱自己的挖掘处理就只剩重击/烧炼等
 * 变换；入包改由 {@link SpectrumInventoryInsertionBlockMixin} 在光谱跑完之后统一走 GoldenTweaks。
 * 这样共振、自动烧炼等与「自动收集」同持时的前置变换仍然照常生效。
 */
@Mixin(EnchantmentHelper.class)
public class SpectrumInsertionTagMixin {

    @Inject(
            method = "hasTag(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/tags/TagKey;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void gt$disableBlockInsertion(
            ItemStack stack,
            TagKey<Enchantment> tag,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (SpectrumEnchantmentTags.INVENTORY_INSERTION_EFFECT.equals(tag)) {
            cir.setReturnValue(false);
        }
    }
}
