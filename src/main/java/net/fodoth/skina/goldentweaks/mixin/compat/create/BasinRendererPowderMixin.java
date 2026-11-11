package net.fodoth.skina.goldentweaks.mixin.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fodoth.skina.goldentweaks.content.reactors.GTReactorItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Create 工作盆里的物品默认走 {@code ItemRenderer.renderStatic(..., GROUND, ...)} 渲染完整物品模型。
 * 对 GoldenTweaks 的固体粉末改画单层 powder 遮罩，物品会按粉末颜色染色；其他地方仍使用原三层模型。
 */
@Mixin(targets = "com.simibubi.create.content.processing.basin.BasinRenderer")
public class BasinRendererPowderMixin {

    private static final ResourceLocation POWDER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("goldentweaks", "item/powder/powder_mask");

    @Inject(
            method = "renderItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gt$renderPowderInBasin(PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay,
                                        ItemStack stack, CallbackInfo ci) {
        if (GTReactorItems.solidFor(stack.getItem()) == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        TextureAtlasSprite sprite = minecraft.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(POWDER_TEXTURE);
        BakedModel original = itemRenderer.getModel(stack, minecraft.level, null, 0);
        List<BakedQuad> templateQuads = original.getQuads(null, null, RandomSource.create());
        BakedQuad quad = powderQuad(templateQuads, sprite);
        if (quad == null) {
            return;
        }

        itemRenderer.render(stack, ItemDisplayContext.GROUND, false, poseStack, bufferSource, light, overlay,
                new PowderModel(original, quad));
        ci.cancel();
    }

    private static BakedQuad powderQuad(List<BakedQuad> quads, TextureAtlasSprite sprite) {
        if (quads.isEmpty()) {
            return null;
        }
        BakedQuad template = quads.get(0);
        int[] src = template.getVertices();
        int[] dst = java.util.Arrays.copyOf(src, src.length);
        TextureAtlasSprite from = template.getSprite();
        float fromU = from.getU1() - from.getU0();
        float fromV = from.getV1() - from.getV0();
        float toU = sprite.getU1() - sprite.getU0();
        float toV = sprite.getV1() - sprite.getV0();
        int stride = src.length / 4;
        for (int i = 0; i < 4; i++) {
            int base = i * stride;
            float u = Float.intBitsToFloat(src[base + 4]);
            float v = Float.intBitsToFloat(src[base + 5]);
            float nu = sprite.getU0() + (u - from.getU0()) / fromU * toU;
            float nv = sprite.getV0() + (v - from.getV0()) / fromV * toV;
            dst[base + 4] = Float.floatToRawIntBits(nu);
            dst[base + 5] = Float.floatToRawIntBits(nv);
        }
        return new BakedQuad(dst, 1, template.getDirection(), sprite,
                template.isShade(), template.hasAmbientOcclusion());
    }

    private static final class PowderModel extends BakedModelWrapper<BakedModel> {

        private final List<BakedQuad> quads;

        private PowderModel(BakedModel original, BakedQuad quad) {
            super(original);
            this.quads = List.of(quad);
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
            return side == null ? quads : List.of();
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand,
                                        ModelData data, RenderType renderType) {
            return side == null ? quads : List.of();
        }

        @Override
        public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
            return List.of(this);
        }

        @Override
        public List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
            return List.of(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        }

        @Override
        public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean leftHand) {
            originalModel.applyTransform(transformType, poseStack, leftHand);
            return this;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return quads.get(0).getSprite();
        }
    }
}
