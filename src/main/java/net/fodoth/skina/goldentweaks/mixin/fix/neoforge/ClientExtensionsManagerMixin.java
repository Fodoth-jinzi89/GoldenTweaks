package net.fodoth.skina.goldentweaks.mixin.fix.neoforge;

import net.neoforged.neoforge.client.extensions.common.ClientExtensionsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(ClientExtensionsManager.class)
public class ClientExtensionsManagerMixin {

    @Redirect(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private static Object goldenTweaks$ignoreDuplicateClientExtension(
            Map<Object, Object> map,
            Object key,
            Object value
    ) {

        Object old = map.get(key);

        if (old != null) {

            // 删除旧注册
            return null;
        }

        return map.put(key, value);
    }
}
