package net.fodoth.skina.goldentweaks.mixin.fix.maid;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 心契誓约 / 女仆核心 的按键在客户端 tick 里通过 {@link KeyMapping#consumeClick()} 轮询。
 * 原版 {@code KeyMapping.click} 无条件累加点击次数（不看冲突上下文），
 * 所以这些快捷键在打开任何界面时也会触发。这里在「非本模组自己的界面」中吞掉它们，
 * 让快捷键只在游戏内生效（界面内仍保留模组自己界面的按键操作）。
 */
@Mixin(KeyMapping.class)
public abstract class KeyMappingMaidHotkeyScreenGuardMixin {

    private static final String MAIDMARRIAGE_CATEGORY = "key.categories.maidmarriage";
    private static final String MAIDSOULCORE_CATEGORY = "key.categories.maidsoulcore";
    private static final String MAIDMARRIAGE_PACKAGE = "com.example.maidmarriage";

    @Shadow
    private int clickCount;

    @Shadow
    public abstract String getCategory();

    @Inject(method = "consumeClick", at = @At("HEAD"), cancellable = true)
    private void gt$blockMaidHotkeysInForeignScreens(CallbackInfoReturnable<Boolean> cir) {
        if (!GoldenTweaksClientConfig.doBlockMaidHotkeysInGui()) {
            return;
        }

        String category = this.getCategory();
        if ((MAIDMARRIAGE_CATEGORY.equals(category) || MAIDSOULCORE_CATEGORY.equals(category))
                && gt$isForeignScreen()) {
            this.clickCount = 0;
            cir.setReturnValue(false);
        }
    }

    private static boolean gt$isForeignScreen() {
        Screen screen = Minecraft.getInstance().screen;
        return screen != null && !screen.getClass().getName().startsWith(MAIDMARRIAGE_PACKAGE);
    }
}
