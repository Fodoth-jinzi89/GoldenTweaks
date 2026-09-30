package net.fodoth.skina.goldentweaks.compat.mcwifipnp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.Locale;

/**
 * 调整 zstdnet 追加到 mcwifipnp「对局域网开放」界面上的 zstd 端口输入框及其标签。
 * <p>
 * zstdnet 会把端口框贴到任意类名含 {@code sharetolan} 的界面（包括 mcwifipnp 的
 * {@code ShareToLanScreenNew}），并且贴在 mcwifipnp「监听端口号」输入框左侧的同一行，
 * 因此与 mcwifipnp 的控件重叠；该框由 zstdnet 一次性加入屏幕，也不随标签页切换。
 * <p>
 * 本处理器把该框移到第一个标签页所有设置项的下方，并且仅在第一个标签页显示；
 * 同时把 zstdnet 画在输入框上方的标签重定位到与 mcwifipnp「监听端口号」标签一致的位置
 * （左侧与其左对齐、与输入框垂直居中，见 {@link #drawZstdPortLabel}）。
 * <p>
 * mcwifipnp 使用原版 {@code TabManager}，第一个标签页的控件（含「监听端口号」输入框与标签）
 * 只在第一个标签页存在，因此以「当前是否找得到该输入框」判断是否处于第一个标签页。
 * <p>
 * 仅在同时加载 mcwifipnp 与 zstdnet 时由 {@code GoldenTweaks} 注册。
 */
public final class McWifiPnPZstdPortLayout {

    /** mcwifipnp「监听端口号」输入框及其标签的本地化标签（原版 key），只在第一个标签页出现。 */
    private static final String BACKEND_PORT_LABEL_KEY = "lanServer.port";

    /** zstdnet 端口输入框的本地化标签。 */
    private static final String ZSTD_PORT_LABEL_KEY = "zstdnet.share_to_lan.zstd_port";

    /** zstd 端口框与上方设置区之间的距离。 */
    private static final int GAP = 6;

    /** 隐藏时把输入框移到屏幕外，zstdnet 画在输入框上方的标签也会一起移出可视范围。 */
    private static final int OFFSCREEN_Y = -10_000;

    /** 内容区下沿的兜底值：原版页脚高度。 */
    private static final int DEFAULT_FOOTER_HEIGHT = 33;

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        Screen screen = event.getScreen();
        if (!isMcWifiPnPShareToLan(screen)) {
            return;
        }

        EditBox zstdPort = findEditBox(screen, ZSTD_PORT_LABEL_KEY);
        if (zstdPort == null) {
            return;
        }

        EditBox backendPort = findEditBox(screen, BACKEND_PORT_LABEL_KEY);
        if (backendPort == null) {
            // 不在第一个标签页：隐藏（移出屏幕，连同标签）
            zstdPort.visible = false;
            zstdPort.setY(OFFSCREEN_Y);
            return;
        }

        // 第一个标签页：对齐到 mcwifipnp 端口输入框，放到所有设置项下方
        zstdPort.visible = true;
        zstdPort.setX(backendPort.getX());
        zstdPort.setY(bottomOfSettings(screen, zstdPort));
    }

    /**
     * 绘制 zstd 端口标签（由 zstdnet 的 {@code GuiGraphics.drawString} 调用重定向而来）。
     * <p>
     * 在 mcwifipnp「对局域网开放」界面上，把标签从「输入框正上方、与输入框左对齐」改为
     * 「与 mcwifipnp「监听端口号」标签左对齐、与 zstd 输入框垂直居中」，与 mcwifipnp 的标签保持一致；
     * 其它界面（含原版局域网界面、隐藏状态）按 zstdnet 原位置绘制。
     */
    public static int drawZstdPortLabel(GuiGraphics graphics, Font font, Component text,
                                        int fallbackX, int fallbackY, int color) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen == null || !isMcWifiPnPShareToLan(screen)) {
            return graphics.drawString(font, text, fallbackX, fallbackY, color);
        }
        EditBox zstdPort = findEditBox(screen, ZSTD_PORT_LABEL_KEY);
        AbstractStringWidget backendPortLabel = findStringWidget(screen, BACKEND_PORT_LABEL_KEY);
        if (zstdPort == null || backendPortLabel == null) {
            return graphics.drawString(font, text, fallbackX, fallbackY, color);
        }
        int x = backendPortLabel.getX();
        int y = zstdPort.getY() + (zstdPort.getHeight() - font.lineHeight) / 2;
        return graphics.drawString(font, text, x, y, color);
    }

    /** 目标界面：mcwifipnp 的「对局域网开放」界面。 */
    public static boolean isMcWifiPnPShareToLan(Screen screen) {
        String name = screen.getClass().getName().toLowerCase(Locale.ROOT);
        return name.contains("mcwifipnp") && name.contains("sharetolan");
    }

    /** 在屏幕子控件中按本地化标签查找输入框；标签比较用 I18n，跨语言均成立。 */
    private static EditBox findEditBox(Screen screen, String labelKey) {
        String label = I18n.get(labelKey);
        for (GuiEventListener child : screen.children()) {
            if (child instanceof EditBox editBox && editBox.getMessage().getString().equals(label)) {
                return editBox;
            }
        }
        return null;
    }

    /** 在屏幕子控件中按本地化标签查找文本标签。 */
    private static AbstractStringWidget findStringWidget(Screen screen, String labelKey) {
        String label = I18n.get(labelKey);
        for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractStringWidget widget && widget.getMessage().getString().equals(label)) {
                return widget;
            }
        }
        return null;
    }

    /** 所有设置项（不含页脚、不含 zstd 端口框自身）最下方的 y。 */
    private static int bottomOfSettings(Screen screen, AbstractWidget zstdPort) {
        int footerTop = footerTop(screen);
        int lowest = 0;
        for (GuiEventListener child : screen.children()) {
            if (child instanceof AbstractWidget widget && widget != zstdPort
                    && widget.getY() + widget.getHeight() <= footerTop) {
                lowest = Math.max(lowest, widget.getY() + widget.getHeight());
            }
        }
        return Math.min(lowest + GAP, footerTop - zstdPort.getHeight() - 2);
    }

    /** 页脚顶边：取屏幕下半部按钮（确认/取消/返回原版等）中最靠上的 y，兜底用原版页脚高度。 */
    private static int footerTop(Screen screen) {
        int footerTop = screen.height - DEFAULT_FOOTER_HEIGHT;
        for (GuiEventListener child : screen.children()) {
            if (child instanceof Button button && button.getY() > screen.height / 2) {
                footerTop = Math.min(footerTop, button.getY());
            }
        }
        return footerTop;
    }

    private McWifiPnPZstdPortLayout() {
    }
}
