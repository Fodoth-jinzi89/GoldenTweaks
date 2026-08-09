package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import com.flavor_immersed_daily.Config;
import net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily.accessor.ConfigAccessor;
import net.neoforged.fml.event.config.ModConfigEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Config.class)
public class ConfigMixin {

    @Unique
    private static final String OLD_RED_BEAN = "redbeanshrub_0";
    @Unique
    private static final String NEW_RED_BEAN = "red_bean_block";

    @Unique
    private static final String OLD_RED_PEPPER = "redpepper";
    @Unique
    private static final String NEW_RED_PEPPER = "redrepper";

    @Inject(
            method = "onLoad",
            at = @At("TAIL"),
            remap = false
    )
    private static void onLoadTail(ModConfigEvent event, CallbackInfo ci) {
        // 修改 WILDGRAINPLANT_DROPS: redbeanshrub_0 -> red_bean_block
        modifyWildDrops("flavor_immersed_daily:wildgrainplant", OLD_RED_BEAN, NEW_RED_BEAN);

        // 修改 WILDSEEDPLANT_DROPS: redpepper -> redrepper
        modifyWildDrops("flavor_immersed_daily:wildseedplant", OLD_RED_PEPPER, NEW_RED_PEPPER);
    }

    @Unique
    private static void modifyWildDrops(String key, String oldValue, String newValue) {
        List<String> drops = Config.wildDrops.get(key);
        if (drops != null) {
            // 检查是否包含需要替换的项
            boolean containsOld = drops.stream().anyMatch(item -> item.equals(oldValue));

            if (containsOld) {
                // 获取原始配置字符串
                String raw = getRawConfigValue(key);
                if (raw != null && raw.contains(oldValue)) {
                    String modifiedRaw = raw.replace(oldValue, newValue);

                    // 使用 Accessor 重新解析
                    List<String> modifiedDrops = ConfigAccessor.invokeParseDrops(modifiedRaw);

                    // 更新到 map
                    Config.wildDrops.put(key, modifiedDrops);
                }
            }
        }
    }

    @Unique
    private static String getRawConfigValue(String key) {
        // 根据 key 返回对应的配置值
        if ("flavor_immersed_daily:wildgrainplant".equals(key)) {
            return (String) Config.WILDGRAINPLANT_DROPS.get();
        } else if ("flavor_immersed_daily:wildseedplant".equals(key)) {
            return (String) Config.WILDSEEDPLANT_DROPS.get();
        }
        return null;
    }
}
