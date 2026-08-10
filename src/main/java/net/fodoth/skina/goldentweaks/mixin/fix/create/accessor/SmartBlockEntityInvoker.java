package net.fodoth.skina.goldentweaks.mixin.fix.create.accessor;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Consumer;

@Mixin(SmartBlockEntity.class)
public interface SmartBlockEntityInvoker {

    @Invoker("forEachBehaviour")
    void invokeForEachBehaviour(Consumer<BlockEntityBehaviour> action);
}
