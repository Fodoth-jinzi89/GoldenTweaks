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
 * 恢复 Re-Avaritia 迁移时丢失的 cosmic 渲染队列刷新。
 * <p>
 * 光影（Iris shaderpack）启用时，renderblender 会把 cosmic 物品的
 * cosmic 层延迟入队（{@code IrisCompat.shouldDefer} 对第一/第三人称
 * 上下文返回 true）。世界物品在 {@code RenderLevelStageEvent#AFTER_LEVEL}
 * 阶段调用 {@code CosmicRenderQueue.renderAll()} 统一渲染，以避开
 * Iris 光影管线对自定义 core shader 的接管。原版 Re-Avaritia 在
 * {@code AvaritiaModClient.onRenderLevel} 中完成此调用，renderblender
 * 1.0.0 迁移时丢失，导致光影下 cosmic 效果（物品展示框安瓿/天域之华、
 * 罐内源质块等）不渲染。此处通过事件订阅补回该调用。
 * <p>
 * 队列为空时 {@code renderAll()} 直接返回，无渲染开销；renderblender
 * 未安装时反射调用失败，仅记录 debug 日志，不影响其它功能。
 * <p>
 * 由 {@code GoldenTweaks} 在游戏事件总线上显式注册
 * （{@code NeoForge.EVENT_BUS.register}），遵循本模组对兼容事件处理器的注册约定。
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
            flushRenderBlenderQueue();
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
