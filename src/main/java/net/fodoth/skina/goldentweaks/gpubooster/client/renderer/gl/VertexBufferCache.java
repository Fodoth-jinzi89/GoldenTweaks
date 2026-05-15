package net.fodoth.skina.goldentweaks.gpubooster.client.renderer.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.gpubooster.api.controller.dsa.DSAController;
import net.fodoth.skina.goldentweaks.gpubooster.api.VertexRenderCacheAPI;
import net.fodoth.skina.goldentweaks.util.IntArrayDeque;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.GL11.glGetError;

@OnlyIn(Dist.CLIENT)
@ApiStatus.Internal
public class VertexBufferCache {

    private static final Map<VertexFormat, VAOInstance> VAOS = new ConcurrentHashMap<>();

    private static IntArrayDeque VBO_POOL;
    private static IntArrayDeque EBO_POOL;

    public static void init() {
        int size = GoldenTweaksClientConfig.RENDER_CYCLE_POOL_SIZE.get();

        VBO_POOL = new IntArrayDeque(size);
        EBO_POOL = new IntArrayDeque(size);

        for (int i = 0; i < size; i++) {
            VBO_POOL.addFirst(DSAController.get().createBO());
            EBO_POOL.addFirst(DSAController.get().createBO());
        }
    }

    public static int getVBO() {
        RenderSystem.assertOnRenderThread();
        return VBO_POOL.pool().orElse(0);
    }

    public static int getEBO() {
        RenderSystem.assertOnRenderThread();
        return EBO_POOL.pool().orElse(0);
    }

    public static int getVAO(VertexFormat format) {
        RenderSystem.assertOnRenderThread();

        if (format == null) {
            return 0;
        }

        if (VertexRenderCacheAPI.canBeCached(format)) {
            return VAOS.computeIfAbsent(format, VAOInstance::makeVAO)
                    .getID();
        }

        return VAOInstance.makeVAO(format).getID();
    }

    public static void clean(VertexFormat format, int vao, int vbo, int ebo) {

        RenderSystem.assertOnRenderThread();

        if (vao == 0) {
            return;
        }

        // 防 double-free / 已释放 VAO
        if (format != null && !VAOS.containsKey(format)) {
            DSAController.get().deleteVAO(vao);
        }

        // 安全解绑（避免 null / stale state）
        DSAController dsa = DSAController.get();

        try {
            dsa.addEBO2VAO(vao, 0);
            dsa.addVBO2VAO(vao, 0, 0, 0L, 0);
        } catch (Throwable t) {
            GoldenTweaks.LOGGER.warn("[GoldenTweaker] VAO unbind failed: {}", vao, t);
        }

        // pool 回收保护（避免 0 / 重复回收）
        if (vbo != 0) {
            VBO_POOL.addLast(vbo);
        }

        if (ebo != 0) {
            EBO_POOL.addLast(ebo);
        }
    }

    public static void printInfo() {
        RenderSystem.recordRenderCall(() -> {
            int error = glGetError();

            GoldenTweaks.LOGGER.info(
                    "Alive VBOs:{}, Alive EBOs:{}, Cached VAOs:{}, GLErrors:{}",
                    VBO_POOL.size(),
                    EBO_POOL.size(),
                    VAOS.size(),
                    error == 0 ? "none" : error
            );
        });
    }
}