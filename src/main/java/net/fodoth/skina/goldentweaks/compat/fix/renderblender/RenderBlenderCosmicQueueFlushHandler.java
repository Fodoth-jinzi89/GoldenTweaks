package net.fodoth.skina.goldentweaks.compat.fix.renderblender;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.renderblender.GTCosmicJarRenderQueue;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
 * Flushes GoldenTweaks' custom cosmic jar queue after world rendering.
 * RenderBlender owns and flushes its item queue at the same stage.
 */
@OnlyIn(Dist.CLIENT)
public class RenderBlenderCosmicQueueFlushHandler {

    private static final String COSMIC_RENDER_QUEUE =
            "net.weibai.renderblender.api.client.render.CosmicRenderQueue";

    private static boolean queueLookupComplete;
    private static Method cosmicRenderAll;
    private static boolean bypassLookupComplete;
    private static Field irisBypass;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            Boolean bypass = getIrisBypass();
            setIrisBypass(true);
            try {
                GTCosmicJarRenderQueue.renderAll();
            } finally {
                if (bypass != null) {
                    setIrisBypass(bypass);
                }
            }
        }
    }

    public static void flushRenderBlenderQueue() {
        if (queueLookupComplete && cosmicRenderAll == null) {
            return;
        }
        try {
            if (!queueLookupComplete) {
                cosmicRenderAll = Class.forName(COSMIC_RENDER_QUEUE).getMethod("renderAll");
                queueLookupComplete = true;
            }
            cosmicRenderAll.invoke(null);
        } catch (Throwable t) {
            queueLookupComplete = true;
            cosmicRenderAll = null;
            GoldenTweaks.LOGGER.debug("[GT] renderblender cosmic queue flush skipped: {}", t.toString());
        }
    }

    private static Boolean getIrisBypass() {
        Field field = findIrisBypass();
        if (field == null) return null;
        try {
            return field.getBoolean(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void setIrisBypass(boolean value) {
        Field field = findIrisBypass();
        if (field == null) return;
        try {
            field.setBoolean(null, value);
        } catch (Throwable ignored) {
        }
    }

    private static Field findIrisBypass() {
        if (bypassLookupComplete) return irisBypass;
        bypassLookupComplete = true;
        try {
            Class<?> state = Class.forName("net.irisshaders.iris.vertices.ImmediateState");
            irisBypass = state.getField("bypass");
        } catch (Throwable ignored) {
            irisBypass = null;
        }
        return irisBypass;
    }
}
