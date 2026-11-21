package net.fodoth.skina.goldentweaks.mixin.fix.acceleratedrendering;

import net.minecraft.client.resources.model.SimpleBakedModel;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = SimpleBakedModel.class, priority = 100)
public abstract class SimpleBakedModelMixin {
}
