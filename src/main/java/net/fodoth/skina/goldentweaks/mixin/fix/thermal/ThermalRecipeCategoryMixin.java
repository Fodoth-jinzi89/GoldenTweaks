package net.fodoth.skina.goldentweaks.mixin.fix.thermal;

import cofh.thermal.lib.compat.jei.ThermalRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Thermal 的机器/动力/装置 JEI 分类用「机器容器贴图」的一块区域当配方背景，而那块贴图里
 * 既有淡灰色面板，也硬画了物品格子。这里把 {@code paddedBackground} 收到的贴图换成
 * {@code goldentweaks:textures/gui/thermal_jei/...} 下的副本：副本只清掉了 {@code #C6C6C6}
 * 面板底色，格子边框保留。于是 JEI/EMI 里不再有淡灰面板、格子依旧存在，而游戏内机器 GUI
 * 用的是原贴图，不受影响。
 */
@Mixin(value = ThermalRecipeCategory.class, remap = false)
public abstract class ThermalRecipeCategoryMixin {

    private static final String THERMAL_CONTAINER = "textures/gui/container/";

    @ModifyArg(
            method = "paddedBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/helpers/IGuiHelper;drawableBuilder(Lnet/minecraft/resources/ResourceLocation;IIII)Lmezz/jei/api/gui/drawable/IDrawableBuilder;"
            ),
            index = 0
    )
    private static ResourceLocation gt$slotsOnlyBackground(ResourceLocation texture) {
        if (texture != null && "thermal".equals(texture.getNamespace())
                && texture.getPath().startsWith(THERMAL_CONTAINER)) {
            return ResourceLocation.fromNamespaceAndPath("goldentweaks",
                    "textures/gui/thermal_jei/" + texture.getPath().substring("textures/gui/".length()));
        }
        return texture;
    }
}
