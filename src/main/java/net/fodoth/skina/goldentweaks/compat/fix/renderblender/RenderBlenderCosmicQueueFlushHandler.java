package net.fodoth.skina.goldentweaks.compat.fix.renderblender;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.renderblender.GTCosmicJarRenderQueue;
import net.minecraft.client.Minecraft;
import net.irisshaders.iris.vertices.ImmediateState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Flushes GoldenTweaks' custom cosmic jar queue after world rendering.
 * RenderBlender owns and flushes its item queue at the same stage.
 */
@OnlyIn(Dist.CLIENT)
public class RenderBlenderCosmicQueueFlushHandler {

    private static final String COSMIC_RENDER_QUEUE =
            "net.weibai.renderblender.api.client.render.CosmicRenderQueue";

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
        try {
            Class.forName(COSMIC_RENDER_QUEUE).getMethod("renderAll").invoke(null);
        } catch (Throwable t) {
            GoldenTweaks.LOGGER.debug("[GT] renderblender cosmic queue flush skipped: {}", t.toString());
        }
    }
}
