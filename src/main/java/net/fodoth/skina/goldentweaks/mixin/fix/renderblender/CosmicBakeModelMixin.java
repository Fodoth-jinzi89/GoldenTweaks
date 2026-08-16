package net.fodoth.skina.goldentweaks.mixin.fix.renderblender;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.weibai.renderblender.api.client.model.bakedmodels.WrappedItemModel;
import net.weibai.renderblender.client.model.loader.base.CosmicSetting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * 修复 renderblender 从 Re-Avaritia 迁移时 cosmic 层 mask quad 烘焙方式的退化。
 *
 * <p>原版 Re-Avaritia 的 {@code CosmicBakeModel.renderCosmicLayer} 通过
 * {@code WrappedItemModel.bakeItem(List<TextureAtlasSprite>)} 生成 cosmic 层 quad：
 * 该方法调用 {@code ItemModelGenerator.processFrames}，按 mask 纹理的 <b>alpha</b>
 * 通道裁剪出 silhouette 形状的 quad（CPU 端抠形）。
 *
 * <p>renderblender 迁移后改用了 {@code bakeCosmicItem(CosmicSetting, BlockModel)}，
 * 直接烘焙模型 JSON 里显式声明的 16x16 满框 {@code elements}，得到满框 quad，
 * 完全依赖 cosmic shader 里 {@code mask.r < 0.01} 的 discard 来抠形（GPU 端抠形）。
 *
 * <p>在 vanilla 下 shader 的 Sampler0 正确绑定 atlas，mask 采样正常，两者视觉一致；
 * 但在 iris+sodium 下 Sampler0（atlas）绑定被干扰，mask 采样退化为全白，
 * 满框 quad 就会铺满整个物品格子（要素图标/安瓿/天域之华覆盖整个物品方框）。
 * 原版按 alpha 裁剪的 quad 不依赖 shader 的 mask 采样，形状依然正确。
 *
 * <p>这里把 {@code bakeCosmicItem} 调用重定向回原版的 {@code bakeItem}，
 * 恢复按 mask 纹理 alpha 裁剪的 quad。
 */
@Mixin(value = WrappedItemModel.class, remap = false)
public class CosmicBakeModelMixin {

    @Inject(
            method = "bakeCosmicItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gt$trimmedCosmicQuads(
            CosmicSetting setting,
            BlockModel baseModel,
            CallbackInfoReturnable<List<BakedQuad>> cir
    ) {
        Minecraft mc = Minecraft.getInstance();
        List<TextureAtlasSprite> sprites = new ArrayList<>();
        for (ResourceLocation mask : setting.masks().values()) {
            TextureAtlasSprite sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(mask);
            if (sprite != null) {
                sprites.add(sprite);
            }
        }
        cir.setReturnValue(WrappedItemModel.bakeItem(sprites));
    }
}
