"""Register a re-textured copy of every port model, one per casing.

The blockstates name models that do not exist as files — `.../gt_port_item_input_lv#casing=shell_pp`.
This is where they come to exist: at load time each authored port model is cloned, its plate sprite
replaced, and the clone registered under that id.

Cloning at load time rather than shipping a file per casing is what keeps the cost flat. A hundred
casings would otherwise mean a hundred copies of every port model on disk, all regenerated whenever a
port's own artwork changes; here they are one baked model and N small transforms.

The plate is identified by the sprite the authored model uses, and its UVs are carried into the new
sprite's rectangle — swapping the sprite alone leaves the vertices sampling the old pixels. Both steps
come from GregTech's texture-override model, which exists for this same problem.
"""
import io

path = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortModelWrapping.java"

content = '''package net.fodoth.skina.goldentweaks.content.multiblock.client;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.GTMultiblockBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Gives every port a model per casing, by cloning the authored one at load time.
 *
 * <p>The blockstates name ids like {@code .../gt_port_item_input_lv#casing=shell_pp}. Those are not
 * files; this class creates them by wrapping the authored model so it draws the casing's plate.</p>
 *
 * <p>Why the casing has to reach the model at all: CTM decides whether two cells connect by
 * comparing their sprites, and it asks for those with {@code SpriteCalculator.getSprite(state,
 * direction)} — no model data. So the plate has to differ per block state, and a state can only name
 * a bounded set. {@code GTMultiblockBlocks.CASINGS} is that set.</p>
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public final class GTPortModelWrapping {

    /** Marks a model id whose plate has been replaced with a named casing. */
    private static final String CASING_MARKER = "#casing=";

    private GTPortModelWrapping() {
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();

        // The authored models, with their plate sprite resolved: the casing variants are cut from
        // these, and cutting has to happen before the map gains entries or the clones would be
        // re-cloned.
        Map<ModelResourceLocation, BakedModel> authored = new java.util.HashMap<>();
        for (Map.Entry<ModelResourceLocation, BakedModel> entry : models.entrySet()) {
            if (isPortModel(entry.getKey())) {
                authored.put(entry.getKey(), entry.getValue());
            }
        }

        int added = 0;
        for (int index = 1; index < GTMultiblockBlocks.CASINGS.size(); index++) {
            String casing = GTMultiblockBlocks.CASINGS.get(index).getId().getPath();
            TextureAtlasSprite plate = plateSprite(GTMultiblockBlocks.casingAt(index));
            if (plate == null) {
                GoldenTweaks.LOGGER.warn("[GT] casing {} has no sprite; its ports keep the default plate",
                        casing);
                continue;
            }

            for (Map.Entry<ModelResourceLocation, BakedModel> entry : authored.entrySet()) {
                ModelResourceLocation variant = casingVariant(entry.getKey(), casing);
                models.put(variant, new PlateModel(entry.getValue(), plate));
                added++;
            }
        }

        GoldenTweaks.LOGGER.info("[GT] port models: {} authored, {} casing variants added",
                authored.size(), added);
    }

    /**
     * The id a casing's copy of an authored model is registered under.
     *
     * <p>The variant part is kept and the casing is appended to it, so {@code facing=up} and
     * {@code #inventory} stay their own models — the blockstate chooses between them by facing.</p>
     */
    private static ModelResourceLocation casingVariant(ModelResourceLocation authored, String casing) {
        String path = authored.id().getPath() + CASING_MARKER + casing;
        String variant = authored.getVariant() == null ? "" : authored.getVariant();
        return new ModelResourceLocation(
                ResourceLocation.fromNamespaceAndPath(authored.id().getNamespace(), path), variant);
    }

    /**
     * Whether a model id names a port block's model.
     *
     * <p>The file name alone is compared: a block model's id is {@code <namespace>:<folder>/<name>},
     * and a casing variant carries a marker that must not be mistaken for a second port.</p>
     */
    private static boolean isPortModel(ModelResourceLocation id) {
        String path = id.id().getPath();
        if (path.contains(CASING_MARKER)) {
            return false;
        }
        int slash = path.lastIndexOf('/');
        return (slash < 0 ? path : path.substring(slash + 1)).startsWith("gt_port_");
    }

    /** The casing block's main sprite, or {@code null} when its model offers none. */
    @Nullable
    private static TextureAtlasSprite plateSprite(net.minecraft.world.level.block.Block casing) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer()
                .getBlockModel(casing.defaultBlockState());
        return model.getParticleIcon(ModelData.EMPTY);
    }

    /**
     * A port model that draws its plate with another casing's sprite.
     *
     * <p>Only the plate quad is replaced. The icon and the tier ring keep their own sprites and their
     * tint indices, so the voltage colouring still comes from the block colour handler.</p>
     */
    private static final class PlateModel extends net.neoforged.neoforge.client.model.BakedModelWrapper<BakedModel> {

        /** The sprite the authored model uses for its plate. */
        private final ResourceLocation authoredPlate;

        private final TextureAtlasSprite plate;

        private PlateModel(BakedModel authored, TextureAtlasSprite plate) {
            super(authored);
            this.plate = plate;
            this.authoredPlate = authored.getParticleIcon(ModelData.EMPTY).contents().name();
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable net.minecraft.world.level.block.state.BlockState state,
                                        @Nullable Direction side, RandomSource random,
                                        ModelData data, @Nullable net.minecraft.client.renderer.RenderType type) {
            List<BakedQuad> quads = this.originalModel.getQuads(state, side, random, data, type);

            List<BakedQuad> result = new ArrayList<>(quads.size());
            boolean replaced = false;
            for (BakedQuad quad : quads) {
                TextureAtlasSprite sprite = quad.getSprite();
                if (sprite != null && sprite.contents().name().equals(this.authoredPlate)) {
                    result.add(retexture(quad, sprite, this.plate));
                    replaced = true;
                } else {
                    result.add(quad);
                }
            }
            // Nothing matched: the artwork changed under us. The authored plate is a better failure
            // than drawing none.
            return replaced ? result : quads;
        }
    }

    /** Vertices in a block quad. */
    private static final int VERTICES_PER_QUAD = 4;

    /**
     * The same quad, drawn with another sprite and its UVs carried across.
     *
     * <p>The UVs are in atlas space, computed from the old sprite's rectangle when the model was
     * baked. Relabelling the sprite alone leaves them sampling the old pixels, which is why the plate
     * has to be remapped rather than renamed.</p>
     */
    private static BakedQuad retexture(BakedQuad quad, TextureAtlasSprite from, TextureAtlasSprite to) {
        int[] vertices = quad.getVertices().clone();
        for (int vertex = 0; vertex < VERTICES_PER_QUAD; vertex++) {
            int base = vertex * IQuadTransformer.STRIDE + IQuadTransformer.UV0;

            float u = Float.intBitsToFloat(vertices[base]);
            float v = Float.intBitsToFloat(vertices[base + 1]);

            vertices[base] = Float.floatToRawIntBits(
                    Mth.map(u, from.getU0(), from.getU1(), to.getU0(), to.getU1()));
            vertices[base + 1] = Float.floatToRawIntBits(
                    Mth.map(v, from.getV0(), from.getV1(), to.getV0(), to.getV1()));
        }
        return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(),
                to, quad.isShade(), quad.hasAmbientOcclusion());
    }
}
'''

io.open(path, "w", encoding="utf-8", newline="\n").write(content)
print("casing variants are registered at load time")
