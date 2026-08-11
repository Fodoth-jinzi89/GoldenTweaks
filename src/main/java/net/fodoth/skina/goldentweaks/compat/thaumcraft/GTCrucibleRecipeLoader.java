package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

/**
 * 神秘时代坩埚配方加载器
 * 在服务器启动时自动加载 data/modid/recipe/thaumcraft/crucible 目录下的所有配方
 * 委托给 GTCrucibleRecipe.load() 执行实际加载逻辑
 */
public final class GTCrucibleRecipeLoader {

    private GTCrucibleRecipeLoader() {
        // 工具类，禁止实例化
    }

    // ==================== 事件订阅 ====================
    /**
     * 服务器启动事件处理器
     * 在服务器即将启动时加载所有坩埚配方
     */
    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        GTCrucibleRecipe.load(event.getServer().getResourceManager());
    }
}