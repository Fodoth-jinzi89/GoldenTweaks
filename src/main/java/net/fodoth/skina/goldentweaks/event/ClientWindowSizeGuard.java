package net.fodoth.skina.goldentweaks.event;

import com.mojang.blaze3d.platform.Window;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * 让窗口尺寸跟随启动器传入的 {@code --width/--height}。
 * <p>
 * NeoForge 的早期窗口（自定义 {@code earlyWindowProvider}，例如 Drippy）会先按
 * {@code config/fml.toml} 的 {@code earlyWindowWidth/earlyWindowHeight} 建窗，再把同一个 GLFW
 * 窗口移交给游戏本体，启动器设置的尺寸因此被忽略。这里在客户端启动后读取进程命令行里的
 * {@code --width/--height} 并重新套用一次。
 */
public final class ClientWindowSizeGuard {

    private static boolean applied;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (applied) {
            return;
        }
        applied = true;

        if (!GoldenTweaksClientConfig.doFollowLauncherWindowSize()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        Window window = minecraft.getWindow();
        if (window == null || window.isFullscreen()) {
            return;
        }

        int[] requested = launcherWindowSize();
        if (requested == null) {
            return;
        }

        int width = requested[0];
        int height = requested[1];
        if (window.getWidth() == width && window.getHeight() == height) {
            return;
        }

        GoldenTweaks.LOGGER.info(
                "[GoldenTweaks] Window is {}x{} but the launcher requested {}x{}, resizing to the launcher size",
                window.getWidth(), window.getHeight(), width, height);
        window.setWindowed(width, height);
    }

    private static int[] launcherWindowSize() {
        String[] arguments;
        try {
            arguments = ProcessHandle.current().info().arguments().orElse(null);
        } catch (Throwable ignored) {
            return null;
        }
        if (arguments == null) {
            return null;
        }

        int width = -1;
        int height = -1;
        for (int i = 0; i < arguments.length - 1; i++) {
            if ("--width".equals(arguments[i])) {
                width = parseInt(arguments[i + 1]);
            } else if ("--height".equals(arguments[i])) {
                height = parseInt(arguments[i + 1]);
            }
        }
        return width > 0 && height > 0 ? new int[] { width, height } : null;
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private ClientWindowSizeGuard() {
    }
}
