package net.fodoth.skina.goldentweaks.mixin.fix.packagedauto;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thelm.packagedauto.integration.jei.PackageItemSubtypeInterpreter;

import java.util.Arrays;
import java.util.List;

/**
 * PackagedAuto 的 JEI 子类型解释器用
 * {@code List.of(stack.get(RECIPE), stack.get(PACKAGE_INDEX))} 区分不同包裹。
 * 裸的 {@code packagedauto:package} 并不携带这两个数据组件，取值均为 null，而
 * {@link List#of} 不允许 null，于是 JEI 生成物品 UID（例如神秘要素 JEI 扫描）时抛 NPE。
 * 这里换成允许 null 的列表，保持原本「恒为两个元素」的语义。
 */
@Mixin(PackageItemSubtypeInterpreter.class)
public class PackageItemSubtypeInterpreterMixin {

    @Redirect(
            method = "getSubtypeData(Lnet/minecraft/world/item/ItemStack;Lmezz/jei/api/ingredients/subtypes/UidContext;)Ljava/lang/Object;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;of(Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;"
            ),
            remap = false
    )
    private static List<Object> goldenTweaks$allowNullComponents(Object recipe, Object index) {
        return recipe == null || index == null ? Arrays.asList(recipe, index) : List.of(recipe, index);
    }
}
