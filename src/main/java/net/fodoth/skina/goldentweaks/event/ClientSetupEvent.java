package net.fodoth.skina.goldentweaks.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksConfigScreen;
import net.fodoth.skina.goldentweaks.gpubooster.api.VertexRenderCacheAPI;
import net.fodoth.skina.goldentweaks.gpubooster.client.renderer.gl.VertexBufferCache;
import net.fodoth.skina.goldentweaks.util.DSAMode;
import net.fodoth.skina.goldentweaks.util.GTState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

@EventBusSubscriber(modid = GoldenTweaks.MODID)
public class ClientSetupEvent {

    public static boolean GL46;
    public static boolean DSA;
    public static boolean ARB_DSA;
    public static boolean NV_TEX_BARRIER;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

        GTState.setReady();

        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (mc, parent) -> GoldenTweaksConfigScreen.create(parent)
        );

        RenderSystem.recordRenderCall(() -> {
            GLCapabilities caps = GL.getCapabilities();

            GL46 = caps.OpenGL46;
            ARB_DSA = caps.GL_ARB_direct_state_access;
            DSA = ARB_DSA || GL46;
            NV_TEX_BARRIER = caps.GL_NV_texture_barrier;

            if (GoldenTweaksClientConfig.hasDSA(DSAMode.VBO)) {
                VertexBufferCache.init();
            }

            GoldenTweaks.LOGGER.info("GPUBooster caps initialized.");
        });

        if (GoldenTweaksClientConfig.VERTEX_FORMAT_CACHE.get()) {

            VertexRenderCacheAPI.addCacheableFormats(
                    DefaultVertexFormat.BLOCK,
                    DefaultVertexFormat.NEW_ENTITY
            );

            GoldenTweaks.LOGGER.debug("Cacheable vertex formats added.");
        }
    }
}