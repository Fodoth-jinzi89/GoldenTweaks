package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.search.EmiSearch;
import net.fodoth.skina.goldentweaks.compat.emi.GTEmiSearchEngine;
import net.minecraft.client.searchtree.SuffixArray;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * EMI 搜索内核的接入点，见 {@link GTEmiSearchEngine}。
 *
 * <p><b>为什么要拦 {@code SuffixArray#add}：</b>EMI 在 {@code bake()} 里为名称 / tooltip / mod / 别名
 * 各建一张 vanilla 后缀数组，而 tooltip 文本是 <b>每次调用现算</b>的
 * （{@code ItemEmiStack#getTooltipText} 每次都重新拼 tooltip，没有缓存）。
 * 因此绝不能为了建自己的索引再遍历一遍物品——那会把上万次 tooltip 生成做两遍。
 * 这里直接在 EMI 写后缀数组的<b>同一个调用点</b>把 (条目, 字符串) 截走，
 * 既拿到与 EMI 完全一致的原始串，又能顺手跳过原版的存储与排序。</p>
 *
 * <p><b>为什么关闭本内核时仍然正确：</b>{@code capture} 之后会按开关把原调用补回去，
 * 于是「关掉极速搜索」= 后缀数组的 {@code add}/{@code generate} 全都照常执行
 * = 完全回到 EMI 原版行为。</p>
 */
@Mixin(value = EmiSearch.class, remap = false)
public abstract class EmiSearchMixin {

    @Inject(method = "bake", at = @At("HEAD"))
    private static void gt$beginBake(CallbackInfo ci) {
        GTEmiSearchEngine.beginBake(GTEmiSearchEngine.fastSearch());
    }

    @Redirect(method = "bake",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/searchtree/SuffixArray;add(Ljava/lang/Object;Ljava/lang/String;)V"))
    private static void gt$captureSuffixEntry(SuffixArray<?> array, Object value, String contents) {

        GTEmiSearchEngine.capture(array, value, contents);

        if (!GTEmiSearchEngine.skipVanillaIndex()) {
            addToSuffixArray(array, value, contents);
        }
    }

    @Redirect(method = "bake",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/searchtree/SuffixArray;generate()V"))
    private static void gt$skipSuffixGenerate(SuffixArray<?> array) {

        // add 已被跳过时后缀数组是空的，generate() 本来就退化成空操作；
        // 真正省掉的是「没被跳过」那种情况下的倍增排序（秒级），所以这里只需保持一致
        if (!GTEmiSearchEngine.skipVanillaIndex()) {
            array.generate();
        }
    }

    /**
     * 接管 tooltip 的<b>生成与投喂</b>：EMI 原本在这一步给每个 stack 生成完整 tooltip，再逐行写进
     * 后缀数组（装了 JECharacters 时是写进它的 PinIn trie）。两者合计是 `Baking search` 那 19.5 s 的主体。
     *
     * <p>改成由本内核接管：tooltip 只进我们自己的 gram 索引，且可以推迟到重载之后的后台线程，
     * 见 {@link GTEmiSearchEngine#captureTooltip}。</p>
     */
    @Redirect(method = "bake",
            at = @At(value = "INVOKE",
                    target = "Ldev/emi/emi/api/stack/EmiStack;getTooltipText()Ljava/util/List;"))
    private static List<Component> gt$captureTooltip(EmiStack stack) {
        return GTEmiSearchEngine.captureTooltip(stack);
    }

    /** 那次 {@code getItemStack()} 只为判断附魔书，却给每个 stack 造一个 ItemStack。 */
    @Redirect(method = "bake",
            at = @At(value = "INVOKE",
                    target = "Ldev/emi/emi/api/stack/EmiStack;getItemStack()Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack gt$itemStackForBookCheck(EmiStack stack) {
        return GTEmiSearchEngine.itemStackForBookCheck(stack);
    }

    @Inject(method = "bake", at = @At("TAIL"))
    private static void gt$finishBake(CallbackInfo ci) {
        GTEmiSearchEngine.finishBake(GTEmiSearchEngine.fastSearch());
    }

    @Inject(method = "search", at = @At("HEAD"), cancellable = true)
    private static void gt$fastSearch(String query, CallbackInfo ci) {

        if (GTEmiSearchEngine.submit(query)) {
            ci.cancel();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addToSuffixArray(SuffixArray array, Object value, String contents) {
        array.add(value, contents);
    }
}
