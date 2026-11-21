package net.fodoth.skina.goldentweaks.mixin.fix.createaerofreight;

import cn.blockforge.generated.createareofreight.api.modifier.ModifierDefinition;
import cn.blockforge.generated.createareofreight.modifier.ModifierCatalog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = ModifierCatalog.class, remap = false)
public abstract class ModifierCatalogMixin {
    @Inject(method = "currentDefinitions", at = @At("RETURN"))
    private static void gt$professionDescriptions(CallbackInfoReturnable<Map<String, ModifierDefinition>> cir) {
        cir.getReturnValue().replaceAll((id, definition) -> {
            String prefix = "goldentweaks_bountiful_";
            if (!definition.value().startsWith(prefix) || !definition.value().endsWith("_sell")) {
                return definition;
            }
            String profession = definition.value().substring(prefix.length());
            return new ModifierDefinition(definition.id(), definition.category(), definition.nameKey(),
                    "tooltip.goldentweaks.aerofreight." + profession, definition.score(), definition.value());
        });
    }
}
