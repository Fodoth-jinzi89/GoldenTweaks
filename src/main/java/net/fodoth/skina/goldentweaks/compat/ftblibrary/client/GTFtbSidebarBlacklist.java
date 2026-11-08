package net.fodoth.skina.goldentweaks.compat.ftblibrary.client;

import dev.ftb.mods.ftblibrary.api.client.FTBLibraryClientApi;

/**
 * FTB Library 的侧边栏按钮组会绘制在所有 {@code AbstractContainerScreen} 上。
 * 期货机界面左上角已有自己的页签，这些 FTB 按钮会叠在上面，因此把该界面登记进
 * FTB Library 的侧边栏屏幕黑名单，使其不再渲染。
 */
public final class GTFtbSidebarBlacklist {

    private static final String FUTURES_MACHINE_SCREEN =
            "com.miophas.singularity_iteration.common.client.screen.mio_icif_gui_future_elc";

    private GTFtbSidebarBlacklist() {
    }

    public static void install() {
        FTBLibraryClientApi.get().addSidebarScreenBlacklist(FUTURES_MACHINE_SCREEN);
    }
}
