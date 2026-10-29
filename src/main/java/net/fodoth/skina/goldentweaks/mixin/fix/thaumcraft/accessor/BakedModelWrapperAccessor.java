package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft.accessor;

import net.minecraft.client.resources.model.BakedModel;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 取被包裹的原模型。{@code WardedGlassBakedModelMixin} 需要它把无朝向那趟调用交还原模型，
 * 但字段 {@code originalModel} 声明在 NeoForge 的父类 {@link BakedModelWrapper} 里，
 * 直接 {@code @Shadow} 会报 “field was not located in the target class”。
 */
@Mixin(value = BakedModelWrapper.class, remap = false)
public interface BakedModelWrapperAccessor {

    @Accessor("originalModel")
    BakedModel gt$getOriginalModel();
}
