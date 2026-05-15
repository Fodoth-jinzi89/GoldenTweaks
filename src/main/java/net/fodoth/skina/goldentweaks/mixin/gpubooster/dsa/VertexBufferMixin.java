package net.fodoth.skina.goldentweaks.mixin.gpubooster.dsa;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.gpubooster.api.GTGL;
import net.fodoth.skina.goldentweaks.gpubooster.api.controller.dsa.DSAController;
import net.fodoth.skina.goldentweaks.gpubooster.client.renderer.gl.VertexBufferCache;
import net.fodoth.skina.goldentweaks.util.DSAMode;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL46C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Mixin(VertexBuffer.class)
public abstract class VertexBufferMixin {

    @Shadow
    private int vertexBufferId;

    @Shadow
    private int indexBufferId;

    @Shadow
    private int arrayObjectId;

    @Shadow
    @Final
    private VertexBuffer.Usage usage;

    @Shadow
    private RenderSystem.AutoStorageIndexBuffer sequentialIndices;

    @Shadow
    private @Nullable VertexFormat format;

    @Shadow
    protected abstract RenderSystem.AutoStorageIndexBuffer uploadIndexBuffer(
            MeshData.DrawState drawState,
            @Nullable ByteBuffer indexBuffer
    );

    @Shadow
    protected abstract VertexFormat uploadVertexBuffer(
            MeshData.DrawState drawState,
            @Nullable ByteBuffer vertexBuffer
    );

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void goldentweaks$reinitNamed(VertexBuffer.Usage usage, CallbackInfo ci) {
        if (GoldenTweaksClientConfig.hasDSA(DSAMode.VBO)) {
            RenderSystem.assertOnRenderThread();

            this.vertexBufferId = VertexBufferCache.getVBO();
            this.indexBufferId = VertexBufferCache.getEBO();
            this.arrayObjectId = GTGL.makeVAO();
        }
    }

    @Redirect(
            method = "upload(Lcom/mojang/blaze3d/vertex/MeshData;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;uploadVertexBuffer(Lcom/mojang/blaze3d/vertex/MeshData$DrawState;Ljava/nio/ByteBuffer;)Lcom/mojang/blaze3d/vertex/VertexFormat;"
            )
    )
    private VertexFormat goldentweaks$uploadVBO(
            VertexBuffer instance,
            MeshData.DrawState drawState,
            ByteBuffer vertexBuffer
    ) {
        return GoldenTweaksClientConfig.hasDSA(DSAMode.VBO)
                ? this.goldentweaks$uploadNamedVBO(drawState, vertexBuffer)
                : this.uploadVertexBuffer(drawState, vertexBuffer);
    }

    @Redirect(
            method = "upload(Lcom/mojang/blaze3d/vertex/MeshData;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;uploadIndexBuffer(Lcom/mojang/blaze3d/vertex/MeshData$DrawState;Ljava/nio/ByteBuffer;)Lcom/mojang/blaze3d/systems/RenderSystem$AutoStorageIndexBuffer;"
            )
    )
    private RenderSystem.AutoStorageIndexBuffer goldentweaks$uploadEBO(
            VertexBuffer instance,
            MeshData.DrawState drawState,
            ByteBuffer indexBuffer
    ) {
        return GoldenTweaksClientConfig.hasDSA(DSAMode.VBO)
                ? this.goldentweaks$uploadNamedEBO(drawState, indexBuffer)
                : this.uploadIndexBuffer(drawState, indexBuffer);
    }

    @Inject(
            method = "close",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldentweaks$closeNamed(CallbackInfo ci) {
        if (GoldenTweaksClientConfig.hasDSA(DSAMode.VBO)) {
            VertexBufferCache.clean(
                    this.format,
                    this.arrayObjectId,
                    this.vertexBufferId,
                    this.indexBufferId
            );

            this.arrayObjectId = -1;
            this.vertexBufferId = -1;
            this.indexBufferId = -1;

            ci.cancel();
        }
    }

    @Unique
    private VertexFormat goldentweaks$uploadNamedVBO(
            MeshData.DrawState drawState,
            @Nullable ByteBuffer vertexBuffer
    ) {
        RenderSystem.assertOnRenderThreadOrInit();

        if (vertexBuffer != null) {
            DSAController.get().namedBufferData(
                    this.vertexBufferId,
                    vertexBuffer,
                    this.usage == VertexBuffer.Usage.DYNAMIC
                            ? GL46C.GL_DYNAMIC_DRAW
                            : GL46C.GL_STATIC_DRAW
            );
        }

        DSAController.get().addVBO2VAO(
                this.arrayObjectId,
                0,
                this.vertexBufferId,
                0L,
                drawState.format().getVertexSize()
        );

        return drawState.format();
    }

    @Unique
    private RenderSystem.AutoStorageIndexBuffer goldentweaks$uploadNamedEBO(
            MeshData.DrawState drawState,
            @Nullable ByteBuffer indexBuffer
    ) {
        RenderSystem.assertOnRenderThreadOrInit();

        if (indexBuffer != null) {
            DSAController.get().namedBufferData(
                    this.indexBufferId,
                    indexBuffer,
                    this.usage == VertexBuffer.Usage.DYNAMIC
                            ? GL46C.GL_DYNAMIC_DRAW
                            : GL46C.GL_STATIC_DRAW
            );

            DSAController.get().addEBO2VAO(this.arrayObjectId, this.indexBufferId);
            return null;
        }

        RenderSystem.AutoStorageIndexBuffer autoIndexBuffer =
                RenderSystem.getSequentialBuffer(drawState.mode());

        if (autoIndexBuffer != this.sequentialIndices
                || !autoIndexBuffer.hasStorage(drawState.indexCount())) {
            autoIndexBuffer.bind(drawState.indexCount());
        }

        return autoIndexBuffer;
    }
}