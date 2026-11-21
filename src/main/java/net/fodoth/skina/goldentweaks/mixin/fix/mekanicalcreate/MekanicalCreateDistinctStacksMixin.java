package net.fodoth.skina.goldentweaks.mixin.fix.mekanicalcreate;

import net.fodoth.skina.goldentweaks.compat.mekanicalcreate.ItemStackKey;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@code SimulationRecipeResolver#distinctStacks} 用「对已收列表逐个 noneMatch」实现去重，
 * 而 {@code addNativeItemChargingCandidates} 会拿全部注册物品（3 万级）喂给它，
 * 单趟就是 4.5 亿次 {@code ItemStack.isSameItemSameComponents}。改成哈希集合，O(n)。
 *
 * <p>语义不变：按首次出现顺序保留、按物品+组件比较、计数归一为 1。</p>
 */
@Mixin(targets = "io.github.langqi99.mekanicalcreate.content.SimulationRecipeResolver", remap = false)
public class MekanicalCreateDistinctStacksMixin {

    @Inject(method = "distinctStacks", at = @At("HEAD"), cancellable = true, require = 0)
    private static void gt$linearDedupe(List<ItemStack> stacks, CallbackInfoReturnable<List<ItemStack>> cir) {

        Set<ItemStackKey> seen = new HashSet<>(stacks.size() * 2);
        List<ItemStack> distinct = new ArrayList<>();

        for (ItemStack stack : stacks) {
            if (seen.add(ItemStackKey.of(stack))) {
                distinct.add(stack.copyWithCount(1));
            }
        }

        cir.setReturnValue(distinct);
    }
}
