package net.fodoth.skina.goldentweaks.mixin.fix.mekanicalcreate;

import io.github.langqi99.mekanicalcreate.client.jei.MekanicalCreateJeiPlugin;
import io.github.langqi99.mekanicalcreate.content.SimulationRecipeResolver;
import net.fodoth.skina.goldentweaks.compat.mekanicalcreate.GTMekanicalDeferredRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

/**
 * 配方目录预计算改由 {@link GTMekanicalDeferredRecipes} 提前预热，这里只负责"取"。
 *
 * <p>只完整枚举流体目录：物品目录是它的子集 —— 物品趟只会少掉
 * {@code compacting/mixing/filling/emptying} 与「带流体的 dynamic/sequenced」条目，
 * 按 {@code id} 里的 {@code mekanicalcreate_<process>} 过滤即可精确还原，省掉一整趟全量枚举。</p>
 */
@Mixin(value = MekanicalCreateJeiPlugin.class, remap = false)
public abstract class MekanicalCreateDeferredRecipesMixin {

    @Redirect(method = "registerRecipes",
            at = @At(value = "INVOKE",
                    target = "Lio/github/langqi99/mekanicalcreate/content/SimulationRecipeResolver;getDisplayRecipes(Lnet/minecraft/world/level/Level;Z)Ljava/util/List;"))
    private static List<SimulationRecipeResolver.DisplayRecipe> gt$usePrewarmed(Level level,
                                                                                boolean allowFluidProcessing) {

        if (!GTMekanicalDeferredRecipes.enabled()) {
            return SimulationRecipeResolver.getDisplayRecipes(level, allowFluidProcessing);
        }

        List<SimulationRecipeResolver.DisplayRecipe> cached =
                GTMekanicalDeferredRecipes.cached(level, allowFluidProcessing);
        if (cached != null) {
            return cached;
        }

        // 预热实测可达 2 分钟以上；超时回退只是把同一趟计算再做一遍，反而与预热互相抢 CPU
        List<SimulationRecipeResolver.DisplayRecipe> fluid =
                GTMekanicalDeferredRecipes.await(level, true, 300_000L);
        if (fluid == null) {
            return SimulationRecipeResolver.getDisplayRecipes(level, allowFluidProcessing);
        }
        if (allowFluidProcessing) {
            return fluid;
        }

        List<SimulationRecipeResolver.DisplayRecipe> items = gt$deriveItemRecipes(fluid);
        GTMekanicalDeferredRecipes.storeItemResults(level, items);
        return items;
    }

    private static List<SimulationRecipeResolver.DisplayRecipe> gt$deriveItemRecipes(
            List<SimulationRecipeResolver.DisplayRecipe> fluidRecipes) {

        List<SimulationRecipeResolver.DisplayRecipe> items = new ArrayList<>(fluidRecipes.size());

        for (SimulationRecipeResolver.DisplayRecipe recipe : fluidRecipes) {
            String process = gt$process(recipe.id());

            if ("compacting".equals(process) || "mixing".equals(process)
                    || "filling".equals(process) || "emptying".equals(process)) {
                continue;
            }
            if (process != null && process.startsWith("dynamic_")
                    && (!recipe.fluidInputs().isEmpty() || !recipe.fluidOutputs().isEmpty())) {
                continue;
            }
            if ("sequenced_assembly".equals(process) && !recipe.fluidInputs().isEmpty()) {
                continue;
            }

            items.add(recipe);
        }

        return List.copyOf(items);
    }

    /** 从 {@code <source>/mekanicalcreate_<process>/<variant>} 里取出 process。 */
    private static String gt$process(ResourceLocation id) {

        String path = id.getPath();
        int marker = path.indexOf("/mekanicalcreate_");
        if (marker < 0) {
            return null;
        }

        int start = marker + "/mekanicalcreate_".length();
        int end = path.indexOf('/', start);
        return end < 0 ? path.substring(start) : path.substring(start, end);
    }
}
