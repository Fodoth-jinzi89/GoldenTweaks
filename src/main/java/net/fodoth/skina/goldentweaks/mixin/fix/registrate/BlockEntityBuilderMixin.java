package net.fodoth.skina.goldentweaks.mixin.fix.registrate;

import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(BlockEntityBuilder.class)
public abstract class BlockEntityBuilderMixin {

    @Shadow(remap = false)
    private NonNullSupplier<?> renderer;


    @Inject(
            method = "lambda$registerRenderer$1",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void fixRenderer(FMLClientSetupEvent $, CallbackInfo ci) {

        if (renderer != null) {

            Object factory = renderer.get();

            if (factory instanceof Function function) {

                BlockEntityType type =
                        (BlockEntityType) ((Builder<?, ?, ?, ?>) this).getEntry();


                BlockEntityRenderers.register(
                        type,
                        context ->
                                (BlockEntityRenderer) function.apply(context)
                );
            }
        }

        ci.cancel();
    }
}