package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily.accessor;

import com.flavor_immersed_daily.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(Config.class)
public interface ConfigAccessor {

    @Invoker("parseDrops")
    static List<String> invokeParseDrops(String raw) {
        throw new AssertionError("This should be replaced by Mixin");
    }
}
