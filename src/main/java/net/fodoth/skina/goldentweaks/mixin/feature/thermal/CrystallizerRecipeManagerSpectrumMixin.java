package net.fodoth.skina.goldentweaks.mixin.feature.thermal;

import cofh.thermal.core.util.managers.machine.CrystallizerRecipeManager;
import net.fodoth.skina.goldentweaks.compat.thermal.SpectrumCrystallarieumRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让热力结晶器接纳光谱结晶配方所需的输入。
 *
 * <p>结晶器的输入槽 / 储罐只接受 Thermal 配方管理器里出现过的物品与流体，光谱的种子与液态水晶本来都会被挡下。
 * 这里在 Thermal 配方管理器 {@code refresh}（每次配方重载）时把光谱结晶配方缓存下来，并让
 * {@code validItem}/{@code validFluid} 对缓存里出现过的种子物品 / 流体放行。是否真的能运行仍由升级组件决定
 * （见 {@code MachineCrystallizerSpectrumMixin}）。</p>
 */
@Mixin(value = CrystallizerRecipeManager.class, remap = false)
public abstract class CrystallizerRecipeManagerSpectrumMixin {

    @Inject(method = "refresh", at = @At("HEAD"))
    private void gt$indexSpectrumRecipes(RecipeManager recipeManager, HolderLookup.Provider registries,
                                         PotionBrewing potionBrewing, CallbackInfo ci) {
        SpectrumCrystallarieumRecipes.refresh(recipeManager);
    }

    @Inject(method = "validItem", at = @At("HEAD"), cancellable = true)
    private void gt$allowSpectrumSeed(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (SpectrumCrystallarieumRecipes.isSeed(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "validFluid", at = @At("HEAD"), cancellable = true)
    private void gt$allowSpectrumFluid(FluidStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (SpectrumCrystallarieumRecipes.isFluid(stack)) {
            cir.setReturnValue(true);
        }
    }
}
