package net.fodoth.skina.goldentweaks.mixin.feature.compactmachines;

import dev.compactmods.machines.api.component.CMDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapelessRecipe.class)
public class ShapelessRecipeMixin {

    @Inject(method = "matches", at = @At("RETURN"), cancellable = true)
    private void gt$requireSoarynMachine(CraftingInput input, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }

        ShapelessRecipe recipe = (ShapelessRecipe) (Object) this;
        ResourceLocation template = recipe.getResultItem(level.registryAccess()).get(CMDataComponents.ROOM_TEMPLATE_ID.get());
        if (template == null || !template.getNamespace().equals("goldentweaks")
                || !(template.getPath().startsWith("false_sky_floor_")
                || template.getPath().startsWith("starry_sky_night_floor_"))) {
            return;
        }

        boolean hasSoaryn = input.items().stream().anyMatch(stack ->
                ResourceLocation.parse("compactmachines:soaryn").equals(
                        stack.get(CMDataComponents.ROOM_TEMPLATE_ID.get())
                ));
        cir.setReturnValue(hasSoaryn);
    }
}
