package net.fodoth.skina.goldentweaks.mixin.fix.tmrv;

import dev.nolij.toomanyrecipeviewers.plugin.DispatchStrategy;
import dev.nolij.toomanyrecipeviewers.plugin.JEIPluginManager;
import dev.nolij.toomanyrecipeviewers.plugin.Plugin;
import dev.nolij.toomanyrecipeviewers.plugin.PluginType;
import mezz.jei.api.IModPlugin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = JEIPluginManager.class, remap = false)
public class JEIPluginManagerMixin {

    @ModifyArg(
            method = "registerIngredients",
            at = @At(value = "INVOKE", target = "Ldev/nolij/toomanyrecipeviewers/plugin/JEIPluginManager;dispatch(Ljava/util/function/Consumer;Ldev/nolij/toomanyrecipeviewers/plugin/DispatchStrategy;ZZJ)V"),
            index = 1)
    private DispatchStrategy goldentweaks$registerIngredientsSynchronously(DispatchStrategy original) {
        return DispatchStrategy.SYNC_EMI;
    }

    /**
     * 让神秘时代的 JEI 插件完整加载。
     *
     * <p>TMRV 在初始化时按命名空间判定重复插件：只要已经存在某个 id 前缀为
     * {@code thaumcraft} 的 EMI 配方分类，就把整个 {@code thaumcraft:jei_plugin}
     * 标记为 {@code DUPLICATE_NAMESPACE}（partialLoad），随后 {@code registerRecipes}
     * 被直接跳过——奥术工作台/坩埚/注魔/要素来源/炼狱熔炉全部分类随之消失。</p>
     *
     * <p>而 {@code thaumcraft:outer_lands} 这个 EMI 分类是「世界生成信息」类模组为所有维度
     * 注册的，与神秘时代真正的配方毫无关系，却因此把整个命名空间拖下水。神秘时代本身
     * 并没有 EMI 插件，跳过它的 JEI 配方就等于什么都没有，所以这里把它的插件类型强制为
     * {@link PluginType#NORMAL}，让 {@code registerRecipes} 正常执行。</p>
     */
    @Redirect(
            method = "<init>",
            at = @At(value = "NEW", target = "dev/nolij/toomanyrecipeviewers/plugin/Plugin"))
    private Plugin goldentweaks$forceThaumcraftPluginFullLoad(ResourceLocation id, PluginType type,
                                                              boolean hasOverrides, IModPlugin plugin) {
        if ("thaumcraft".equals(id.getNamespace())) {
            return new Plugin(id, PluginType.NORMAL, hasOverrides, plugin);
        }
        return new Plugin(id, type, hasOverrides, plugin);
    }
}
