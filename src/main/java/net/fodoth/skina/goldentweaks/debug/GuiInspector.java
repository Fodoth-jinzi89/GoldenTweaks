package net.fodoth.skina.goldentweaks.debug;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Field;
import java.util.List;

public class GuiInspector {

    public static void dumpUI(double mouseX, double mouseY) {

        Minecraft mc = Minecraft.getInstance();
        StringBuilder sb = new StringBuilder();

        sb.append("========== GOLDEN UI INSPECTOR ==========\n");

        Screen screen = mc.screen;

        // ================= SCREEN =================
        if (screen != null) {

            sb.append("\n[SCREEN]\n")
                    .append(screen.getClass().getName())
                    .append("\n");

            // ===== 1. children =====
            List<? extends GuiEventListener> children = screen.children();

            sb.append("\n[CHILDREN]\ncount=")
                    .append(children.size())
                    .append("\n");

            for (GuiEventListener child : children) {
                dumpWidget(child, sb, mouseX, mouseY);
            }

            // ===== 2. renderables =====
            try {
                Field f = Screen.class.getDeclaredField("renderables");
                f.setAccessible(true);

                List<?> renderables = (List<?>) f.get(screen);

                sb.append("\n[RENDERABLES]\ncount=")
                        .append(renderables.size())
                        .append("\n");

                for (Object r : renderables) {
                    dumpRenderable(r, sb);
                }

            } catch (Exception e) {
                sb.append("\n[RENDERABLES]\nerror=").append(e.getMessage()).append("\n");
            }

            // ===== 3. narratables =====
            try {
                Field f = Screen.class.getDeclaredField("narratables");
                f.setAccessible(true);

                List<?> narratables = (List<?>) f.get(screen);

                sb.append("\n[NARRATABLES]\ncount=")
                        .append(narratables.size())
                        .append("\n");

            } catch (Exception ignored) {
                sb.append("\n[NARRATABLES]\nunavailable\n");
            }

        } else {
            sb.append("\n[SCREEN]\nNULL (in world)\n");
        }

        // ================= HUD =================
        dumpHud(mc, sb);

        sb.append("\n[Mouse]\n")
                .append(mouseX).append(", ").append(mouseY)
                .append("\n");

        String result = sb.toString();

        // ================= LOG =================
        GoldenTweaks.LOGGER.warn(result);

        // ================= CLIPBOARD =================
        if (GoldenTweaksClientConfig.doDebugGuiCopyToClipboard()) {
            try {
                mc.keyboardHandler.setClipboard(result);
            } catch (Exception e) {
                GoldenTweaks.LOGGER.warn("[GT-UI] clipboard failed: {}", e.getMessage());
            }
        }
    }

    // ================= Widget =================
    private static void dumpWidget(GuiEventListener node, StringBuilder sb, double mouseX, double mouseY) {

        sb.append("\n- ").append(node.getClass().getSimpleName()).append("\n");

        if (node instanceof AbstractWidget w) {

            boolean hover = w.isMouseOver(mouseX, mouseY);

            sb.append("  pos=")
                    .append(w.getX()).append(",").append(w.getY())
                    .append(" size=")
                    .append(w.getWidth()).append("x").append(w.getHeight())
                    .append(" hover=").append(hover)
                    .append("\n");

            String msg = w.getMessage().getString();
            if (!msg.isEmpty()) {
                sb.append("  text=").append(msg).append("\n");
            }
        }
    }

    // ================= Renderable =================
    private static void dumpRenderable(Object r, StringBuilder sb) {

        sb.append("\n- ").append(r.getClass().getSimpleName());

        if (r instanceof Renderable) {
            sb.append(" (Renderable)");
        }

        if (r instanceof AbstractWidget w) {
            sb.append(" pos=").append(w.getX()).append(",").append(w.getY());
        }

        sb.append("\n");
    }

    // ================= HUD =================
    private static void dumpHud(Minecraft mc, StringBuilder sb) {

        sb.append("\n[HUD SNAPSHOT]\n");

        try {
            sb.append("screenOpen=").append(mc.screen != null).append("\n");
            sb.append("paused=").append(mc.isPaused()).append("\n");
            sb.append("debugOverlay=active\n");
        } catch (Exception e) {
            sb.append("hudError=").append(e.getMessage()).append("\n");
        }
    }
}