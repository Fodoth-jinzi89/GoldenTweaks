package net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alshanex.familiarslib.util.consumables.FamiliarConsumableComponent;
import net.alshanex.familiarslib.util.consumables.FamiliarConsumableSystem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FamiliarConsumableComponent.class)
public class FamiliarConsumableComponentMixin {

    @Shadow
    @Final
    @Mutable
    public static Codec<FamiliarConsumableComponent> CODEC;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void gt$replaceCodec(CallbackInfo ci) {

        CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        FamiliarConsumableSystem.ConsumableType.CODEC
                                .fieldOf("type")
                                .forGetter(FamiliarConsumableComponent::type),

                        Codec.intRange(1, 10)
                                .fieldOf("tier")
                                .forGetter(FamiliarConsumableComponent::tier)

                ).apply(instance, FamiliarConsumableComponent::new)
        );
    }
}
