package net.fodoth.skina.goldentweaks.mixin.fix.flavorimmerseddailyargo;

import com.flavor_immersed_daily_argo.economy.product.AutomaticProductDiscovery;
import com.google.gson.JsonElement;
import net.fodoth.skina.goldentweaks.compat.flavorimmerseddailyargo.GTLootDiscoveryData;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

@Mixin(value = AutomaticProductDiscovery.class, remap = false)
public abstract class AutomaticProductDiscoveryMixin {

    @ModifyVariable(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Map<ResourceLocation, JsonElement> gt$retainDiscoveryFields(Map<ResourceLocation, JsonElement> tables) {
        return GTLootDiscoveryData.compact(tables);
    }
}
