package net.fodoth.skina.goldentweaks.mixin.fix.scexaspect;

import dev.scex.aspectjei.AspectCatalog;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.fodoth.skina.goldentweaks.compat.jei.GTJeiAspectScan;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * scex_aspect_jei 的 {@code AspectCatalog#add} 也遍历全部物品，对每个物品调两次
 * {@code getUidForStack} 且<b>都发生在去重之前</b> —— 实测 {@code elapsedMs=22490}，
 * 是 TMRV 那 24.7s 墙钟的另一半。
 *
 * <p>去重键改成 {@code Item}（无自定义 components 时两者等价，见 {@link GTJeiAspectScan}），
 * 那个返回值被丢弃的探错调用在快路径下跳过。</p>
 */
@Mixin(value = AspectCatalog.class, remap = false)
public abstract class AspectCatalogMixin {

    @Redirect(method = "add",
            at = @At(value = "INVOKE",
                    target = "Lmezz/jei/api/helpers/IStackHelper;getUidForStack(Lnet/minecraft/world/item/ItemStack;Lmezz/jei/api/ingredients/subtypes/UidContext;)Ljava/lang/Object;",
                    ordinal = 0))
    private static Object gt$dedupKey(IStackHelper helper, ItemStack stack, UidContext context) {
        return GTJeiAspectScan.dedupKey(helper, stack, context);
    }

    @Redirect(method = "add",
            at = @At(value = "INVOKE",
                    target = "Lmezz/jei/api/helpers/IStackHelper;getUidForStack(Lnet/minecraft/world/item/ItemStack;Lmezz/jei/api/ingredients/subtypes/UidContext;)Ljava/lang/Object;",
                    ordinal = 1))
    private static Object gt$probeOrNull(IStackHelper helper, ItemStack stack, UidContext context) {
        return GTJeiAspectScan.probeOrNull(helper, stack, context);
    }
}
