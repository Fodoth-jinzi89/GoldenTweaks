package net.fodoth.skina.goldentweaks.mixin.compat.sophisticatedstorage;

import net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.client.SsStorageSettingsScreen;
import net.minecraft.client.Minecraft;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.BackToStorageTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * SS 的「返回存储」页签默认把 {@code OpenStorageInventoryPayload} 发给服务端，服务端按**世界方块 pos**
 * 打开 SS 的原生 {@code StorageContainerMenu}/{@code LimitedBarrelContainerMenu}。装置上的容器没有
 * 对应的世界方块，会打开错误/不存在的菜单，甚至因 {@code BlockPos 0,0,0} 构造失败抛出异常导致客户端断连。
 * 这里在装置设置界面（{@link SsStorageSettingsScreen}）上拦截该页签，改为重开装置上的挂载存储菜单。
 */
@Mixin(value = BackToStorageTab.class, remap = false)
public abstract class BackToStorageTabMixin {

    @Inject(method = "onTabIconClicked", at = @At("HEAD"), cancellable = true)
    private void gt$reopenContraptionStorage(int button, CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof SsStorageSettingsScreen settingsScreen) {
            settingsScreen.reopenContraptionStorage();
            ci.cancel();
        }
    }
}
