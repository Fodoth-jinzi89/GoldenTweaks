package net.fodoth.skina.goldentweaks.mixin.feature.thermal.accessor;

import cofh.thermal.lib.common.block.entity.Reconfigurable4WayBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * {@code transferOutput()} / {@code transferInput()} 声明在 Thermal 机器的父类
 * {@link Reconfigurable4WayBlockEntity} 上，而 {@code @Shadow} 只能引用目标类自身的成员
 * （引用父类方法会在应用阶段抛 InvalidMixinException），所以这里用 {@code @Invoker} 把它们暴露出来，
 * 供 {@code MachineParallelOverclockMixin} 在并行循环里照原版语义搬运产出与输入。
 */
@Mixin(value = Reconfigurable4WayBlockEntity.class, remap = false)
public interface Reconfigurable4WayBlockEntityInvoker {

    @Invoker("transferOutput")
    void gt$transferOutput();

    @Invoker("transferInput")
    void gt$transferInput();
}
