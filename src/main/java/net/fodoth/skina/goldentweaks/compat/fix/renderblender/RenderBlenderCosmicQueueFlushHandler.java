package net.fodoth.skina.goldentweaks.compat.fix.renderblender;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.renderblender.GTCosmicJarRenderQueue;
import net.minecraft.client.Minecraft;
import net.irisshaders.iris.vertices.ImmediateState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.lang.reflect.Method;

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

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            boolean bypass = ImmediateState.bypass;
            ImmediateState.bypass = true;
            try {
                GTCosmicJarRenderQueue.renderAll();
            } finally {
                ImmediateState.bypass = bypass;
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
}
