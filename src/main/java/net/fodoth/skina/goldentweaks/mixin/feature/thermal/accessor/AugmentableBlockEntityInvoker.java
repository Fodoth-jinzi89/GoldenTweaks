package net.fodoth.skina.goldentweaks.mixin.feature.thermal.accessor;

import cofh.thermal.lib.common.block.entity.AugmentableBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

/**
 * {@code getAugmentsAsList()} 声明在 Thermal 机器的父类 {@link AugmentableBlockEntity} 上，而 {@code @Shadow}
 * 只能引用目标类自身的成员，所以这里用 {@code @Invoker} 把它暴露出来，供结晶器的光谱专业化升级判断是否装了本组件。
 */
@Mixin(value = AugmentableBlockEntity.class, remap = false)
public interface AugmentableBlockEntityInvoker {

    @Invoker("getAugmentsAsList")
    List<ItemStack> gt$getAugmentsAsList();
}
