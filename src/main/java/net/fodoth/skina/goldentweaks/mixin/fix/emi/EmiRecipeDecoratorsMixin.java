package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.screen.RecipeDisplay;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 恢复配方页上由其它模组通过 {@code EmiRegistry#addRecipeDecorator} 注入的按钮。
 * <p>
 * wcwt（ME 综合工作终端）的「填充物品」锤子按钮就是作为 recipe decorator 加进配方页的，
 * 但 EMI 在 {@link RecipeDisplay#getWidgets} 里把 decorator 的调用整个关在
 * {@link EmiConfig#showRecipeDecorators} 之后，而该开关默认取
 * {@code EmiAgnos.isDevelopmentEnvironment()}，正式客户端恒为 {@code false}，
 * 于是 wcwt 的按钮在 EMI 下从不出现。同一个按钮走 JEI 的
 * {@code IAdvancedRegistration#addRecipeButtonFactory}，所以 JEI 侧正常、EMI 侧缺失。
 * <p>
 * 这里只改写该处读到的开关值，不修改配置本身；该字段全代码库仅此一处读取，
 * 因此不会波及其它行为。按钮自身仍会按 {@code WcwtPullItemsSupport} 判断
 * 当前是否处于 WCWT 终端，非 WCWT 场景下不会绘制。
 */
@Mixin(value = RecipeDisplay.class, remap = false)
public abstract class EmiRecipeDecoratorsMixin {

    @ModifyExpressionValue(
            method = "getWidgets",
            at = @At(
                    value = "FIELD",
                    target = "Ldev/emi/emi/config/EmiConfig;showRecipeDecorators:Z",
                    opcode = Opcodes.GETSTATIC))
    private boolean goldentweaks$restoreRecipeDecorators(boolean original) {
        return true;
    }
}
