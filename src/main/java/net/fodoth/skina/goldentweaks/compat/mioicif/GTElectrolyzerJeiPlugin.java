package net.fodoth.skina.goldentweaks.compat.mioicif;

import com.miophas.singularity_iteration.common.recipe.generic.ElectrolyzerRecipe;
import com.miophas.singularity_iteration.common.recipe.generic.ElectrolyzerRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 电解机 JEI 页面：奇点迭代没有为 {@code mio_icif:electrolyzer} 注册 JEI 分类，这里补一个，
 * 沿用 SI 的 {@code SiItemMachineCategory} 布局（输入 → 箭头 → 输出 + 耗能/耗时）。
 *
 * <p>插件 UID 用 {@code goldentweaks_jei} 命名空间：SI 自身有原生 EMI 分类，TMRV 会把同命名空间的
 * JEI 插件判为 DUPLICATE_NAMESPACE 而跳过配方注册。</p>
 */
@JeiPlugin
public class GTElectrolyzerJeiPlugin implements IModPlugin {

    private static final String SI_MODID = "mio_icif";

    private static final ResourceLocation MACHINE_ID =
            ResourceLocation.parse("mio_icif:producer/block_electrolyzer_elc");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("goldentweaks_jei", "electrolyzer");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        if (present() && FMLEnvironment.dist.isClient()) {
            GTElectrolyzerJeiCategory.register(registration);
        }
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (present()) {
            registration.addRecipes(GTElectrolyzerJeiCategory.TYPE, displayRecipes());
        }
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (!present()) {
            return;
        }
        BuiltInRegistries.ITEM.getOptional(MACHINE_ID)
                .ifPresent(item -> registration.addRecipeCatalyst(new ItemStack(item), GTElectrolyzerJeiCategory.TYPE));
    }

    private static List<RecipeHolder<ElectrolyzerRecipe>> displayRecipes() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        return new ArrayList<>(
                level.getRecipeManager().getAllRecipesFor(ElectrolyzerRecipes.ELECTROLYZER_TYPE.get()));
    }

    /** Guarded before touching any Singularity Iteration class. */
    private static boolean present() {
        return ModList.get().isLoaded(SI_MODID);
    }
}
