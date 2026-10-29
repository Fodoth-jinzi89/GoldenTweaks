package net.fodoth.skina.goldentweaks.mixin.fix.academy;

import com.mohistmc.academy.client.KeyInputHandler;
import com.mohistmc.academy.world.item.TerminalInstaller;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 数据终端（{@code academy:terminal_installer}）的物品提示把打开终端的热键写死成了 {@code Alt}：
 *
 * <pre>components.add(Component.literal("§7安装后可按 Alt 打开终端界面"));</pre>
 *
 * <p>实际热键是 {@link KeyInputHandler#OPEN_TERMINAL}（{@code key.academy.open_terminal}），玩家改键后提示
 * 不会跟着变。这里在 {@code appendHoverText} 末尾把含 {@code Alt} 的那一行替换为按当前键位绑定渲染的文本，
 * 使其随键位设置变化。</p>
 */
@Mixin(TerminalInstaller.class)
public class TerminalInstallerTooltipMixin {

    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void gt$dynamicOpenKeyHint(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag,
                                       CallbackInfo ci) {
        String key = KeyInputHandler.OPEN_TERMINAL.getTranslatedKeyMessage().getString();
        for (int i = 0; i < components.size(); i++) {
            if (components.get(i).getString().contains("Alt")) {
                components.set(i, Component.literal("§7安装后可按 " + key + " 打开终端界面"));
            }
        }
    }
}
