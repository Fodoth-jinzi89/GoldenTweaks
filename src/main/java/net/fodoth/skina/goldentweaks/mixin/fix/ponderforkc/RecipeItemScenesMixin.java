package net.fodoth.skina.goldentweaks.mixin.fix.ponderforkc;

import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Kaleidoscope Cookery 1.5.1 给 {@link RecipeItem.RecipeRecord} 追加了 {@code flexRecipe} 字段，
 * 原先的 3 参构造器 {@code RecipeRecord(List, ItemStack, ResourceLocation)} 已不存在。
 *
 * <p>而 PonderForKaleidoscopeCookery 1.0.0 的「菜谱」思索场景仍在调用旧签名，
 * 按 W 思索会经由 {@code PonderSceneRegistry.compileScene} 抛出
 * {@link NoSuchMethodError} 使客户端崩溃。</p>
 *
 * <p>这里把该场景里的对象创建重定向到当前的 4 参构造器，补上 {@code flexRecipe = false}
 * （与 Kaleidoscope Cookery 自带的 {@code pot}/{@code stockpot} 工厂一致）。</p>
 */
@Pseudo
@Mixin(targets = "com.mastermarisa.ponderforkc.ponder.scene.RecipeItemScenes", remap = false)
public class RecipeItemScenesMixin {

    @Redirect(
            method = "introduction",
            at = @At(value = "NEW", target = "com/github/ysbbbbbb/kaleidoscopecookery/item/RecipeItem$RecipeRecord")
    )
    private static RecipeItem.RecipeRecord goldentweaks$appendFlexRecipe(
            List<ItemStack> input, ItemStack output, ResourceLocation type) {
        return new RecipeItem.RecipeRecord(input, output, type, false);
    }
}
