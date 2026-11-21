package net.fodoth.skina.goldentweaks.compat.ftblibrary.client;

import dev.ftb.mods.ftblibrary.api.client.FTBLibraryClientApi;

/**
 * FTB Library 的侧边栏按钮组会绘制在所有 {@code AbstractContainerScreen} 上。
 * 期货机与 AeroFreight 订单配置界面已有自己的控件，这些 FTB 按钮会叠在上面，因此把这些界面登记进
 * FTB Library 的侧边栏屏幕黑名单，使其不再渲染。
 */
public final class GTFtbSidebarBlacklist {

    private static final String FUTURES_MACHINE_SCREEN =
            "com.miophas.singularity_iteration.common.client.screen.mio_icif_gui_future_elc";
    private static final String AEROFREIGHT_ADMIN_ORDER_EDITOR_SCREEN =
            "cn.blockforge.generated.createareofreight.client.AdminOrderEditorScreen";

    private GTFtbSidebarBlacklist() {
    }

    public static void install() {
        FTBLibraryClientApi.get().addSidebarScreenBlacklist(FUTURES_MACHINE_SCREEN);
        FTBLibraryClientApi.get().addSidebarScreenBlacklist(AEROFREIGHT_ADMIN_ORDER_EDITOR_SCREEN);
    }
}
