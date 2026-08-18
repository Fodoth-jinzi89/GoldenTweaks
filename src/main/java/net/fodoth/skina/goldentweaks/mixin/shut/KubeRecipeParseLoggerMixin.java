package net.fodoth.skina.goldentweaks.mixin.shut;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.script.ConsoleLine;
import dev.latvian.mods.kubejs.script.SourceLine;
import java.util.regex.Pattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = KubeRecipe.class, remap = false)
public class KubeRecipeParseLoggerMixin {

    @Redirect(
            method = "deserialize",
            at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/script/ConsoleJS;warn(Ljava/lang/String;Ldev/latvian/mods/kubejs/script/SourceLine;Ljava/lang/Throwable;Ljava/util/regex/Pattern;)Ldev/latvian/mods/kubejs/script/ConsoleLine;"))
    private ConsoleLine suppressFallbackWarning(ConsoleJS console, String message,
                                                SourceLine source, Throwable error,
                                                Pattern skipPattern) {
        return null;
    }
}
