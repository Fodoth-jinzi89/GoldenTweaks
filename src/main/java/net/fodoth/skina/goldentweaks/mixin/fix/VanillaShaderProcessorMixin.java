package net.fodoth.skina.goldentweaks.mixin.fix;

import io.github.ocelot.glslprocessor.api.GlslParser;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(targets = "foundry.veil.impl.client.render.shader.processor.VanillaShaderProcessor")
public class VanillaShaderProcessorMixin {

    @Redirect(
            method = "modify",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/github/ocelot/glslprocessor/api/GlslParser;preprocessParse(Ljava/lang/String;Ljava/util/Map;)Lio/github/ocelot/glslprocessor/api/node/GlslTree;"
            )
    )
    private static GlslTree goldenTweaks$safeParse(String input, Map<String, String> macros) {

        try {
            return GlslParser.preprocessParse(input, macros);
        } catch (Throwable t) {
            return new GlslTree();
        }
    }

    @Redirect(
            method = "modify",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/github/ocelot/glslprocessor/api/node/GlslTree;stripGLMacros(Ljava/util/Map;)V"
            )
    )
    private static void goldenTweaks$safeStrip(Map<String, String> macros) {
        try {
            GlslTree.stripGLMacros(macros);
        } catch (Throwable ignored) {
        }
    }
}