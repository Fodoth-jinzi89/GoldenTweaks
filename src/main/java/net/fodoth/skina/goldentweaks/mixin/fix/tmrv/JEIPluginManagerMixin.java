package net.fodoth.skina.goldentweaks.mixin.fix.tmrv;

import dev.nolij.toomanyrecipeviewers.plugin.DispatchStrategy;
import dev.nolij.toomanyrecipeviewers.plugin.JEIPluginManager;
import dev.nolij.toomanyrecipeviewers.plugin.Plugin;
import dev.nolij.toomanyrecipeviewers.plugin.PluginType;
import mezz.jei.api.IModPlugin;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
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
     * 让神秘时代与我们自己的插件完整加载。
     *
     * <p>TMRV 在初始化时按命名空间判定重复插件：只要已注册的某个 EMI 配方分类 id 的命名空间与插件 uid 的
     * 命名空间相同，就把整个插件标记为 {@code DUPLICATE_NAMESPACE}（partialLoad），随后 {@code registerRecipes}
     * 被直接跳过。</p>
     *
     * <ul>
     *   <li>神秘时代：{@code thaumcraft:outer_lands} 这个 EMI 分类是「世界生成信息」类模组为所有维度注册的，
     *       与神秘时代真正的配方毫无关系，却把整个命名空间拖下水；神秘时代本身并没有 EMI 插件，跳过它的 JEI
     *       配方就等于什么都没有（奥术工作台 / 坩埚 / 注魔 / 要素来源 / 炼狱熔炉分类全消失）。</li>
     *   <li>我们自己的 {@code goldentweaks:crystallarieum_crystallizer}：goldentweaks 命名空间下已经有
     *       异界漩涡的 EMI 分类（见 {@code GTRiftEmiPlugin}），同一个坑。</li>
     * </ul>
     */
    @Redirect(
            method = "<init>",
            at = @At(value = "NEW", target = "dev/nolij/toomanyrecipeviewers/plugin/Plugin"))
    private Plugin goldentweaks$forcePluginFullLoad(ResourceLocation id, PluginType type,
                                                    boolean hasOverrides, IModPlugin plugin) {
        if ("thaumcraft".equals(id.getNamespace()) || isCrystallarieumPlugin(id)) {
            return new Plugin(id, PluginType.NORMAL, hasOverrides, plugin);
        }
        return new Plugin(id, type, hasOverrides, plugin);
    }

    /** 见 {@link JEIPluginManagerMixin} 类注释：{@code GTCrystallarieumJeiPlugin} 的 uid。 */
    private static boolean isCrystallarieumPlugin(ResourceLocation id) {
        return GoldenTweaks.MODID.equals(id.getNamespace())
                && "crystallarieum_crystallizer".equals(id.getPath());
    }
}
