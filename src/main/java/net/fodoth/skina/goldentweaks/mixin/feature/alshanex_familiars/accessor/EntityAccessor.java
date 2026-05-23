package net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars.accessor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("level")
    Level goldentweaks$getLevel();
}
