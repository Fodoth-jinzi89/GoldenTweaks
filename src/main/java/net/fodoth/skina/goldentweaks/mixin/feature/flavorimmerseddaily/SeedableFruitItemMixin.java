package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import com.flavor_immersed_daily.item.SeedableFruitItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(SeedableFruitItem.class)
public class SeedableFruitItemMixin {

    /**
     * 重定向 isShiftKeyDown() 调用，永远返回 false
     * 这样 !player.isShiftKeyDown() 永远为 true
     */
    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;isShiftKeyDown()Z"
            )
    )
    private boolean alwaysFalse(Player player) {
        // 永远返回 false
        return false;
    }

    @Redirect(
            method = "appendHoverText",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    remap = false
            )
    )
    private boolean filterTooltip(List<Component> tooltip, Object component) {
        if (component instanceof Component comp) {
            String content = comp.getString();
            // 如果包含自定义tooltip的翻译key或特定文本，则不添加
            if (content.contains("seedable_fruit") || content.contains("潜行右键")) {
                return false; // 不添加
            }
        }
        if (component instanceof Component) {
            return tooltip.add((Component) component);
        } else {
            return false;
        }
    }
}
