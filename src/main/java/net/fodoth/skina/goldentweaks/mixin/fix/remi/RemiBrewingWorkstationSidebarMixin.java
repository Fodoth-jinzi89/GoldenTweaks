package net.fodoth.skina.goldentweaks.mixin.fix.remi;

import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * REMI（Reliable EMI）往 EMI 的 {@link SidebarType} 里追加了一个原版没有的 {@code WORKSTATION}
 * 侧栏（remi 的 {@code SidebarTypeMixin} / {@code WorkstationSidebarManager}）。打开酿造台等工作站
 * 界面时，它会在界面左侧额外画出该工作站的配方列表（酿造台就是酿造配方），挤占并盖住原版 GUI，
 * 而 EMI 的侧栏又只提供「切换页」按钮、没有单独关掉这一项的开关。
 *
 * <p>这里只处理酿造台：当前界面是 {@link BrewingStandScreen} 且侧栏类型是 REMI 的
 * {@code WORKSTATION} 时，让该面板不可见。EMI 的排版（{@code getVerticalConstraint}）和渲染都以
 * {@code SidebarPanel#isVisible()} 为准，返回 false 后面板既不绘制也不再占用空间；
 * 其它工作站（熔炉 / 切石机 / 铁砧 …）行为不变。</p>
 */
@Mixin(value = EmiScreenManager.SidebarPanel.class, remap = false)
public class RemiBrewingWorkstationSidebarMixin {

    @Inject(method = "isVisible", at = @At("HEAD"), cancellable = true)
    private void gt$hideWorkstationSidebarOnBrewingStand(CallbackInfoReturnable<Boolean> cir) {

        if (!(Minecraft.getInstance().screen instanceof BrewingStandScreen)) {
            return;
        }

        SidebarType type = ((EmiScreenManager.SidebarPanel) (Object) this).getType();
        if (type != null && ("WORKSTATION".equals(type.name()) || "workstation".equals(type.getName()))) {
            cir.setReturnValue(false);
        }
    }
}
