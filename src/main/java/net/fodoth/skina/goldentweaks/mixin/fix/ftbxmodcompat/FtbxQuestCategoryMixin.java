package net.fodoth.skina.goldentweaks.mixin.fix.ftbxmodcompat;

import dev.ftb.mods.ftbxmodcompat.ftbquests.jei.QuestCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;

/**
 * FTB XMod Compat 把 FTB 任务作为 JEI/EMI 配方展示时，{@code QuestCategory.setRecipe} 用
 * {@code taskIterator.next()} 逐个取任务，却只在 {@code i >= taskOffset} 时才消费迭代器，
 * 且从不做 {@code hasNext()} 判断：只要任务的输入格数超过任务数量（例如单任务、多物品的任务），
 * 就会抛 {@link java.util.NoSuchElementException}。
 *
 * <p>该异常在 TMRV（JEI Stub）遍历万余 CLI/任务配方时每个任务抛一次，进而被记成
 * {@code Found a broken recipe, failed to addRecipe/setRecipe} 的 ERROR——日志里 3k+ 条异常与
 * 上万行刷屏都来自这里。这里把 {@code next()} 换成「取不到就返回 null」：
 * 后面的 {@code instanceof FluidTask} 判定为 false 时会退化成正常的分支
 * （{@code slot.addItemStacks(recipe.input.get(i))}），配方照常显示且不再抛异常。</p>
 */
@Mixin(value = QuestCategory.class, remap = false)
public abstract class FtbxQuestCategoryMixin {

    @Redirect(
            method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Ldev/ftb/mods/ftbxmodcompat/ftbquests/recipemod_common/WrappedQuest;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;"))
    private Object gt$safeTaskNext(Iterator<?> tasks) {
        return tasks.hasNext() ? tasks.next() : null;
    }
}
