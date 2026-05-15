package net.fodoth.skina.goldentweaks.mixin.fix;

import org.spongepowered.asm.mixin.Mixin;

import com.yanny.ali.plugin.mods.PluginUtils;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PluginUtils.class)
public class AliOptionalItemPoolMixin {

    @ModifyArg(
            method = "registerEntry",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Class;forName(Ljava/lang/String;)Ljava/lang/Class;"
            ),
            index = 0
    )
    private static String goldentweaks$redirectMoonlightClass(String name) {

        if (name.equals("net.mehvahdjukaar.moonlight.core.loot.OptionalItemPool")) {
            return "net.mehvahdjukaar.moonlight.core.loot.OptionalItemPoolEntry";
        }

        return name;
    }
}
