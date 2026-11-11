package net.fodoth.skina.goldentweaks.mixin.fix.mioicif.accessor;

import com.miophas.singularity_iteration.core.prefab.blockentity.AbstractProcessingMachineBlockEntity;
import com.miophas.singularity_iteration.core.prefab.inventory.MachineItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * {@code itemHandler} 声明在电解机（以及其它奇点迭代机器）的父类
 * {@link AbstractProcessingMachineBlockEntity} 上，而 {@code @Shadow} 只能引用目标类自身的成员
 * （引用父类成员会在应用阶段抛 InvalidMixinException），所以这里用 {@code @Accessor} 把它暴露出来，
 * 供电解机 mixin 读取输入槽、判断配方是否标记了 {@code no_energy}。
 */
@Mixin(value = AbstractProcessingMachineBlockEntity.class, remap = false)
public interface MachineItemHandlerAccessor {

    @Accessor("itemHandler")
    MachineItemHandler gt$getItemHandler();
}
