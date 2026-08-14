package net.fodoth.skina.goldentweaks.mixin.feature.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.client.GTResearchTableScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.client.TCClientSetup;
import thaumcraft.common.registry.TCMenuTypes;

@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(value = TCClientSetup.class, remap = false)
public class TCClientSetupMixin {

    @Redirect(
            method = "registerMenuScreens",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/client/event/RegisterMenuScreensEvent;register(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/client/gui/screens/MenuScreens$ScreenConstructor;)V"
            )
    )
    private static void gt$replaceResearchTableScreen(
            RegisterMenuScreensEvent event,
            MenuType menuType,
            MenuScreens.ScreenConstructor constructor
    ) {
        if (menuType == TCMenuTypes.RESEARCH_TABLE.get()) {
            event.register(
                    TCMenuTypes.RESEARCH_TABLE.get(),
                    GTResearchTableScreen::new
            );
        } else {
            event.register(menuType, constructor);
        }
    }
}