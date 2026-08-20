package net.fodoth.skina.goldentweaks.mixin.feature.thaumcraft.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import thaumcraft.common.blockentities.BrainJarBlockEntity;

@Mixin(BrainJarBlockEntity.class)
public interface BrainJarBlockEntityAccessor {
    @Accessor("xp")
    int gt$getXp();

    @Accessor("xp")
    void gt$setXp(int value);

    @Invoker("sync")
    void gt$sync();
}
