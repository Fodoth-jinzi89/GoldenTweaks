package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraftcelestial;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.celestial.common.research.CelestialRecipes;

@Mixin(value = CelestialRecipes.class, remap = false)
public class CelestialRecipesMixin {

    @ModifyArg(
            method = "bootstrap",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/celestial/common/research/CelestialRecipes;aspects([Ljava/lang/Object;)Lthaumcraft/api/aspects/AspectList;",
                    ordinal = 4
            ),
            index = 0
    )
    private static Object[] gt$changeArcaneContemplatorAspects(Object[] aspects) {
        return new Object[]{Aspect.WATER, 16, Aspect.ORDER, 16, Aspect.EARTH, 16};
    }
}
