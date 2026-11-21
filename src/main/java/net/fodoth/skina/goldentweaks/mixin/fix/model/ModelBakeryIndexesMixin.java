package net.fodoth.skina.goldentweaks.mixin.fix.model;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryIndexesMixin {
    @Shadow @Final @Mutable
    private Map<ResourceLocation, UnbakedModel> unbakedCache;
    @Shadow @Final @Mutable
    private Map<Object, BakedModel> bakedCache;
    @Shadow @Final @Mutable
    private Map<ModelResourceLocation, UnbakedModel> topLevelModels;
    @Shadow @Final @Mutable
    private Map<ModelResourceLocation, BakedModel> bakedTopLevelModels;

    @Inject(method = "<init>", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/resources/model/ModelBakery;bakedTopLevelModels:Ljava/util/Map;",
            opcode = 181, shift = At.Shift.AFTER))
    private void gt$compactIndexes(CallbackInfo ci) {
        // Run before model loading and before consumers can retain a reference to these maps.
        // Copying also preserves entries if another constructor injection populated a map earlier.
        unbakedCache = new Object2ObjectOpenHashMap<>(unbakedCache);
        bakedCache = new Object2ObjectOpenHashMap<>(bakedCache);
        topLevelModels = new Object2ObjectOpenHashMap<>(topLevelModels);
        bakedTopLevelModels = new Object2ObjectOpenHashMap<>(bakedTopLevelModels);
    }
}
