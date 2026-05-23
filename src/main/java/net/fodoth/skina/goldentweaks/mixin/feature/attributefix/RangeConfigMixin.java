package net.fodoth.skina.goldentweaks.mixin.feature.attributefix;

import net.darkhax.attributefix.common.impl.config.RangeConfig;
import net.minecraft.resources.ResourceLocation;
import java.lang.reflect.Field;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RangeConfig.class)
public class RangeConfigMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void modifyDefaults(CallbackInfo ci) {
        try {
            Field f = RangeConfig.class.getDeclaredField("NEW_DEFAULT_VALUES");
            f.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<ResourceLocation, Double> map =
                    (Map<ResourceLocation, Double>) f.get(null);

            map.replaceAll((k, v) -> Double.MAX_VALUE);

        } catch (Throwable ignored) {}
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void fixMin(
            ResourceLocation id, RangedAttribute attribute, CallbackInfo ci
    ) {
        var self = (RangeConfig)(Object)this;

        self.min = -Double.MAX_VALUE;
        self.max = Double.MAX_VALUE;
        self.modify_range = true;
    }
}