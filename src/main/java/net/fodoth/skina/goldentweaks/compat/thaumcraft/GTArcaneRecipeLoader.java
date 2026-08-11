package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

/**
 * 神秘时代奥术合成配方加载器
 * 在服务器启动时自动加载 data/modid/recipe/thaumcraft/arcane_crafting 目录下的所有配方
 * 委托给 GTArcaneRecipe.load() 执行实际加载逻辑
 */
public final class GTArcaneRecipeLoader {

    private GTArcaneRecipeLoader() {
        // 工具类，禁止实例化
    }

    // ==================== 事件订阅 ====================
    /**
     * 服务器启动事件处理器
     * 在服务器即将启动时加载所有奥术合成配方
     */
    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        GTArcaneRecipe.load(event.getServer().getResourceManager());
    }
}