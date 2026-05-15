package net.fodoth.skina.goldentweaks.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.gpubooster.api.controller.dsa.DSAController;
import net.fodoth.skina.goldentweaks.gpubooster.client.renderer.gl.VAOInstance;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL46;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SimpleBatchVertexConsumer {

    private static final long MAX_BYTES = 6_291_456L;

    private final VertexFormat format;
    private final int stride;

    private int vao;
    private int vbo;

    private ByteBuffer buffer;
    private int vertexCount;

    public SimpleBatchVertexConsumer(VertexFormat format) {
        this.format = format;
        this.stride = format.getVertexSize();
        init();
    }

    /* ------------------------------------------------------------ */

    private void init() {
        RenderSystem.recordRenderCall(() -> {

            this.vao = VAOInstance.makeVAO(format).getID();
            this.vbo = DSAController.get().createBO();

            if (GoldenTweaksClientConfig.hasDSA(DSAMode.VBO)) {

                DSAController.get().addVBO2VAO(
                        vao,
                        0,
                        vbo,
                        0L,
                        stride
                );

                DSAController.get().namedBufferData(
                        vbo,
                        MAX_BYTES,
                        GL46.GL_DYNAMIC_DRAW
                );

            } else {

                GL46.glBindVertexArray(vao);
                GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo);
                GL46.glBufferData(GL46.GL_ARRAY_BUFFER, MAX_BYTES, GL46.GL_DYNAMIC_DRAW);
            }

            // ✔ 安全 buffer（统一内存语义）
            this.buffer = ByteBuffer
                    .allocateDirect((int) MAX_BYTES)
                    .order(ByteOrder.nativeOrder());
        });
    }

    /* ------------------------------------------------------------ */

    public void prepare() {
        buffer.clear();
        vertexCount = 0;
    }

    public void ensureVertexSpace() {
        if (buffer.remaining() < stride) {
            flushAndDraw();
        }
    }

    /* ------------------------------------------------------------ */

    public void flushAndDraw() {
        RenderSystem.assertOnRenderThread();

        if (vertexCount == 0) return;

        buffer.flip();

        if (GoldenTweaksClientConfig.hasDSA(DSAMode.VBO)) {

            DSAController.get().namedBufferSubData(
                    vbo,
                    0L,
                    buffer
            );

        } else {

            GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo);
            GL46.glBufferSubData(GL46.GL_ARRAY_BUFFER, 0L, buffer);
        }

        GL46.glBindVertexArray(vao);
        GL46.glDrawArrays(GL46.GL_TRIANGLES, 0, vertexCount);

        prepare();
    }

    /* ------------------------------------------------------------ */

    public void delete() {

        RenderSystem.assertOnRenderThreadOrInit();

        GL46.glDeleteVertexArrays(vao);
        GL46.glDeleteBuffers(vbo);

        buffer = null;
        vao = 0;
        vbo = 0;
    }

    /* ------------------------------------------------------------ */
    /* vertex API
    /* ------------------------------------------------------------ */

    public SimpleBatchVertexConsumer vertex(float x, float y, float z) {
        ensureVertexSpace();
        buffer.putFloat(x).putFloat(y).putFloat(z);
        vertexCount++;
        return this;
    }

    public SimpleBatchVertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
        ensureVertexSpace();

        Vector3f v = matrix.transformPosition(x, y, z, new Vector3f());

        buffer.putFloat(v.x())
                .putFloat(v.y())
                .putFloat(v.z());

        vertexCount++;
        return this;
    }

    public SimpleBatchVertexConsumer color(int r, int g, int b, int a) {
        buffer.put((byte) r)
                .put((byte) g)
                .put((byte) b)
                .put((byte) a);
        return this;
    }

    public SimpleBatchVertexConsumer color(float r, float g, float b, float a) {
        return color(
                (int)(r * 255f),
                (int)(g * 255f),
                (int)(b * 255f),
                (int)(a * 255f)
        );
    }

    public SimpleBatchVertexConsumer texture(float u, float v) {
        buffer.putFloat(u).putFloat(v);
        return this;
    }

    public SimpleBatchVertexConsumer overlay(int u, int v) {
        buffer.putInt(u).putInt(v);
        return this;
    }

    public SimpleBatchVertexConsumer light(int u, int v) {
        buffer.putInt(u).putInt(v);
        return this;
    }

    public SimpleBatchVertexConsumer light(int uv) {
        buffer.putInt(uv);
        return this;
    }

    public SimpleBatchVertexConsumer normal(float x, float y, float z) {
        buffer.putFloat(x).putFloat(y).putFloat(z);
        return this;
    }

    /* ------------------------------------------------------------ */

    public ByteBuffer getBuffer() {
        return buffer;
    }
}