package net.fodoth.skina.goldentweaks.mixin.fix.mekanicalcreate;

import io.github.langqi99.mekanicalcreate.client.jei.MekanicalCreateJeiPlugin;
import io.github.langqi99.mekanicalcreate.content.SimulationRecipeResolver;
import net.fodoth.skina.goldentweaks.compat.mekanicalcreate.GTMekanicalDeferredRecipes;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * 那两趟配方目录预计算（实测 11097ms）改由 {@link GTMekanicalDeferredRecipes} 提前预热。
 *
 * <p>这里只负责"取"：命中/等到预热结果就用它，否则交回原实现自己算 ——
 * <b>首次进世界也一定有配方</b>，最坏情况下仍要付那 11 秒。</p>
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

        List<SimulationRecipeResolver.DisplayRecipe> ready =
                GTMekanicalDeferredRecipes.await(level, allowFluidProcessing, 30_000L);

        return ready != null ? ready : SimulationRecipeResolver.getDisplayRecipes(level, allowFluidProcessing);
    }
}
