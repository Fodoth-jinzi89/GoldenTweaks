package net.fodoth.skina.goldentweaks.mixin.fix.zstdnet;

import net.fodoth.skina.goldentweaks.compat.mcwifipnp.McWifiPnPZstdPortLayout;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * zstdnet 把 zstd 端口标签画在输入框正上方（{@code onScreenRender} 里的
 * {@code GuiGraphics.drawString}）。这里把这次绘制重定位到与 mcwifipnp「监听端口号」标签一致的位置：
 * 左侧与其左对齐、与 zstd 输入框垂直居中（见 {@link McWifiPnPZstdPortLayout#drawZstdPortLabel}）。
 * <p>
 * {@code targets} 用类名寻址，避免编译期依赖 zstdnet；{@code require = 0} 让方法改名后只失效不崩溃。
 */
@Mixin(targets = "cn.tohsaka.factory.zstdnet.client.ClientProxyPublisher", remap = false)
public class ZstdShareToLanPortLabelMixin {

    @Redirect(method = "onScreenRender",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"),
            require = 0)
    private int gt$relocateShareToLanPortLabel(GuiGraphics graphics, Font font, Component text,
                                               int x, int y, int color) {
        return McWifiPnPZstdPortLayout.drawZstdPortLabel(graphics, font, text, x, y, color);
    }
}
