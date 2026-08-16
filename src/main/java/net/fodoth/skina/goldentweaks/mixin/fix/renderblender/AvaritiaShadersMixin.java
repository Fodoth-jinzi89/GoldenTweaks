package net.fodoth.skina.goldentweaks.mixin.fix.renderblender;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.weibai.renderblender.api.APILang;
import net.weibai.renderblender.client.shader.AvaritiaShaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

@Mixin(value = AvaritiaShaders.class, remap = false)
public class AvaritiaShadersMixin {

    @Redirect(
            method = "onRegisterShaders",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/client/renderer/ShaderInstance"
            ),
            remap = false
    )
    private static ShaderInstance gt$useNewEntityVertexFormat(
            ResourceProvider resourceProvider,
            ResourceLocation shaderLocation,
            VertexFormat vertexFormat
    ) throws IOException {

        if (shaderLocation.equals(APILang.rl("cosmic"))
                && vertexFormat == DefaultVertexFormat.BLOCK) {

            return new ShaderInstance(
                    resourceProvider,
                    shaderLocation,
                    DefaultVertexFormat.NEW_ENTITY
            );
        }

        return new ShaderInstance(
                resourceProvider,
                shaderLocation,
                vertexFormat
        );
    }
}