package net.fodoth.skina.goldentweaks.event;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.debug.GuiInspector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public class MouseDebugHandler {

    @SubscribeEvent
    public static void onMouse(InputEvent.MouseButton.Pre event) {

        if (!GoldenTweaksClientConfig.doDebugGuiInspector()) {
            GoldenTweaks.LOGGER.debug("[GT-INPUT] GUI inspector disabled in config");
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (mc.screen == null || player == null) {
            GoldenTweaks.LOGGER.debug("[GT-INPUT] No active screen or player");
            return;
        }

        int button = event.getButton();
        int action = event.getAction();

        // 只处理 PRESS
        if (action != 0) {
            return;
        }

        boolean shift = Screen.hasShiftDown();
        boolean alt = Screen.hasAltDown();


        // ======================================================
        // ② SHIFT + 中键 => GUI Inspector
        // ======================================================
        if (!shift || button != 2) {
            GoldenTweaks.LOGGER.debug(
                    "[GT-INPUT] Input mismatch: shift={}, alt={}, button={}",
                    shift,
                    alt,
                    button
            );
            return;
        }

        double mouseX = mc.mouseHandler.xpos()
                * mc.getWindow().getGuiScaledWidth()
                / mc.getWindow().getScreenWidth();

        double mouseY = mc.mouseHandler.ypos()
                * mc.getWindow().getGuiScaledHeight()
                / mc.getWindow().getScreenHeight();

        GoldenTweaks.LOGGER.warn(
                "[GT-INPUT] GUI inspect at ({}, {}) screen={}",
                mouseX,
                mouseY,
                mc.screen.getClass().getSimpleName()
        );

        GuiInspector.dumpUI(mouseX, mouseY);
    }
}