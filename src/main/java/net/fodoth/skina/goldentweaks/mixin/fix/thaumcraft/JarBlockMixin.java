package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.blocks.JarBlock;

/**
 * Lets {@code goldentweaks:phial_of_essentia_<tag>} variants participate in the
 * vanilla warded-jar phial interactions (pour in / draw out essentia). The
 * vanilla implementation hardcodes the {@code thaumcraft} namespace, so custom
 * aspect phials are otherwise ignored.
 */
@Mixin(value = JarBlock.class, remap = false)
public abstract class JarBlockMixin {

    @Unique
    private static final String PHIAL_PREFIX = "phial_of_essentia_";

    @Inject(method = "filledPhialAspect", at = @At("HEAD"), cancellable = true)
    private static void goldenTweaks$resolveCustomPhial(ItemStack stack, CallbackInfoReturnable<Aspect> cir) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = id.getPath();
        if (path.startsWith(PHIAL_PREFIX)) {
            Aspect aspect = Aspect.get(path.substring(PHIAL_PREFIX.length()));
            if (aspect != null) {
                cir.setReturnValue(aspect);
            }
        }
    }

    @Inject(method = "filledPhialStack", at = @At("HEAD"), cancellable = true)
    private static void goldenTweaks$createCustomPhial(Aspect aspect, CallbackInfoReturnable<ItemStack> cir) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("goldentweaks", PHIAL_PREFIX + aspect.tag());
        BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> cir.setReturnValue(new ItemStack(item)));
    }
}
