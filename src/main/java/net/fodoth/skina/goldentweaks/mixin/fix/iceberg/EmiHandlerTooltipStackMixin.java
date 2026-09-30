package net.fodoth.skina.goldentweaks.mixin.fix.iceberg;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.render.EmiTooltipMetadata;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "com.anthonyhilyard.iceberg.compat.EMIHandler", remap = false)
public class EmiHandlerTooltipStackMixin {

    @Inject(method = "getTooltipStack", at = @At("HEAD"), cancellable = true)
    private static void goldentweaks$useOwnTooltipStack(List<ClientTooltipComponent> components, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = ItemStack.EMPTY;

        EmiIngredient metadataStack = EmiTooltipMetadata.of(components).getStack();
        if (metadataStack instanceof EmiStack emiStack) {
            stack = emiStack.getItemStack();
        }

        Screen screen = Minecraft.getInstance().screen;
        if (stack.isEmpty() && screen != null && screen.getClass().getName().startsWith("dev.emi.emi")) {
            EmiIngredient hoveredStack = EmiApi.getHoveredStack(true).getStack();
            if (hoveredStack instanceof EmiStack emiStack) {
                stack = emiStack.getItemStack();
            }
        }

        cir.setReturnValue(stack);
    }
}
