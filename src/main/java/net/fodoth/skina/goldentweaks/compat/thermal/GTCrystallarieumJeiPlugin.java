package net.fodoth.skina.goldentweaks.compat.thermal;

import de.dafuqs.spectrum.recipe.crystallarieum.CrystallarieumRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 「结晶器：光谱结晶」JEI 分类：把每条光谱 {@code spectrum:crystallarieum_growing} 配方按转换后的样子列出来
 * （晶种 + 培养液 + 可选添加剂 → 结晶簇；不消耗墨水）。
 *
 * <p>EMI / TMRV 会通过它们的 JEI 兼容层渲染同一个分类。物品只在 thermal + spectrum 都在时才注册，所以这里先判模组
 * 再碰它们的类；{@link GTCrystallarieumJeiCategory} 带客户端类型，只从 {@code registerCategories} 走。</p>
 */
@JeiPlugin
public class GTCrystallarieumJeiPlugin implements IModPlugin {

    static final RecipeType<GTCrystallarieumDisplayRecipe> TYPE =
            RecipeType.create(GoldenTweaks.MODID, "crystallarieum_crystallizer", GTCrystallarieumDisplayRecipe.class);

    private static final String CRYSTALLIZER_ID = "thermal:machine_crystallizer";

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "crystallarieum_crystallizer");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        if (present() && FMLEnvironment.dist.isClient()) {
            GTCrystallarieumJeiCategory.register(registration);
        }
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (present()) {
            registration.addRecipes(TYPE, displayRecipes());
        }
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (present()) {
            registration.addRecipeCatalyst(
                    new ItemStack(ThermalCrystallarieumAugment.CRYSTALLARIEUM_GROWING_AUGMENT.get()), TYPE);
            BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(CRYSTALLIZER_ID))
                    .ifPresent(item -> registration.addRecipeCatalyst(new ItemStack(item), TYPE));
        }
    }

    /**
     * 展示用的转换配方。服务端（或集成服）在时直接现取一份，避免和 Thermal 配方管理器刷新的先后顺序打架；
     * 否则退回我们在配方重载时缓存的那份。
     */
    private static List<GTCrystallarieumDisplayRecipe> displayRecipes() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<CrystallarieumRecipe> source = server == null
                ? SpectrumCrystallarieumRecipes.all()
                : SpectrumCrystallarieumRecipes.snapshot(server.getRecipeManager());

        List<GTCrystallarieumDisplayRecipe> recipes = new ArrayList<>(source.size());
        for (CrystallarieumRecipe recipe : source) {
            recipes.add(GTCrystallarieumDisplayRecipe.of(recipe));
        }
        return recipes;
    }

    /** Guarded before touching any Thermal / Spectrum class. */
    private static boolean present() {
        return ModList.get().isLoaded("thermal")
                && ModList.get().isLoaded("thermal_expansion")
                && ModList.get().isLoaded("spectrum");
    }
}
