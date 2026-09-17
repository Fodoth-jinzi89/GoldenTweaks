package net.fodoth.skina.goldentweaks.network.handler;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * {@code S2COpenMaterialBookPacket} 的客户端侧动作：打开 Silent Gear 的材料书界面。
 * <p>
 * 单独放在这个类里，是为了让 common 的 packet 类**只提到这个类名**、不再直接出现
 * {@code net.minecraft.client.*}：S2C 处理器只在客户端执行，所以本类在专用服务器上永远不会被加载；
 * 反之，客户端逻辑若留在 packet 里，服务端一旦加载到那段代码就会崩
 * （同类事故：{@code NoClassDefFoundError: net/minecraft/client/player/LocalPlayer}）。
 */
public final class MaterialBookClientHandler {

    /** Silent Gear 材料书界面类名。第三方客户端类，用反射访问以避免编译期硬依赖。 */
    private static final String MATERIAL_BOOK_SCREEN =
            "net.silentchaos512.gear.client.gui.book.MaterialBookScreen";

    /** 打开材料书界面；任何失败只记日志，不影响客户端继续运行。 */
    public static void openMaterialBook() {

        try {
            Class<?> clazz = Class.forName(
                    MATERIAL_BOOK_SCREEN
            );

            Object instance = clazz.getDeclaredConstructor().newInstance();

            if (instance instanceof Screen screen) {
                Minecraft.getInstance().setScreen(screen);
            } else {
                GoldenTweaks.LOGGER.warn(
                        "MaterialBookScreen is not a Screen instance"
                );
            }
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn(
                    "Failed to open MaterialBookScreen",
                    e
            );
        }
    }

    private MaterialBookClientHandler() {
    }
}
