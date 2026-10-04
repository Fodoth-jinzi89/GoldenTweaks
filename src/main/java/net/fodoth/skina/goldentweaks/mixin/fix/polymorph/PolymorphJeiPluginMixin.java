package net.fodoth.skina.goldentweaks.mixin.fix.polymorph;

import com.illusivesoulworks.polymorph.common.integration.PolymorphJeiPlugin;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Polymorph 的 JEI 插件把「配方转移后选中该配方」注册成 JEI 的
 * {@code IRecipeTransferListener}，但打包内的 JEI（19.x，由 TMRV 提供的 stub）没有
 * {@code IRecipeTransferListener} / {@code addRecipeTransferListener}，
 * 该插件类一旦被 TMRV 反射加载就会抛 NoClassDefFoundError（日志里的
 * "Failed to initialize JEI plugin ... PolymorphJeiPlugin"）。
 * <p>
 * 这里不摘除插件，而是重写它的注册方法为安全的空实现，使插件能够正常加载；
 * 「转移后选中配方」的实际行为改由 {@link RecipeTransferUtilMixin} 在 JEI 19.x
 * 的配方转移入口上补回（与 Polymorph 自身 {@code MixinEmiRecipeFiller} 对 EMI 的做法一致）。
 */
@Mixin(value = PolymorphJeiPlugin.class, remap = false)
public abstract class PolymorphJeiPluginMixin {

    /**
     * @author Fodoth_jinzi89
     * @reason 原实现引用打包内 JEI 不存在的 IRecipeTransferListener API，会让整个插件类加载失败；
     *         行为改由 RecipeTransferUtilMixin 在 JEI 19.x 的配方转移路径上补回。
     */
    @Overwrite(remap = false)
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // JEI 19.x 没有 addRecipeTransferListener，行为见 RecipeTransferUtilMixin
    }
}
