package net.fodoth.skina.goldentweaks.mixin.fix.minecraft;

import net.fodoth.skina.goldentweaks.util.GTTranslationStrings;
import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;

@Mixin(ClientLanguage.class)
public abstract class ClientLanguageMixin {

    @ModifyArg(method = "loadFrom", at = @At(value = "INVOKE",
            target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;",
            ordinal = 0, remap = false), index = 0)
    private static Map<String, String> gt$compactTranslations(Map<String, String> translations) {
        return GTTranslationStrings.compact(translations);
    }
}
