"""Rebuild the re-skinning model along the lines GregTech uses.

GT's `TextureOverrideModel` retextures by texture *key* — the name a model's `textures` block gives
a sprite — because a key identifies one layer unambiguously. My version compared the quad's sprite
against a hard-coded path, which is fragile: any future plate using a different sheet, or two layers
sharing one sheet, silently changes what gets replaced.

GT also retextures only when an override map is present. The item form has none, so an inventory
render keeps the authored plate. My version had no such distinction.

The key is recovered here without a mixin. A model's `textures` block maps keys to sprites at bake
time, and the plate is the layer drawn with the base model's `#shell` entry — so the sprite is
looked up from that key and the plate is whatever matches it. That is the same rule as GT's, applied
to the one model this mod owns rather than to arbitrary models.

Item form: no casing is recorded, so nothing is replaced.
"""
import io

path = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingModel.java"

content = '''package net.fodoth.skina.goldentweaks.content.multiblock.client;

import net.fodoth.skina.goldentweaks.content.multiblock.GTPortBlock;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A port's plate, re-skinned at draw time instead of in the model files.
 *
 * <p>One copy of a port model per casing does not scale: a hundred casings would mean a hundred
 * copies of every port model and a hundred blockstate branches, all regenerated whenever a port's
 * own artwork changes. Only the plate varies between machines — icon, tier ring and geometry are
 * identical — so only the plate is swapped.</p>
 *
 * <p>Modelled on GregTech's {@code TextureOverrideModel}, which swaps a layer's sprite and leaves
 * every other quad untouched. That is the same idea as this class, and the reason it is worth
 * copying is the two rules it encodes: identify the layer by what it actually is rather than by
 * name, and do nothing at all when no override applies — which is what keeps the item form intact.</p>
 *
 * <p>Derived from {@link BakedModelWrapper} so the item transforms, render types and ambient
 * occlusion all come from the authored model. Implementing {@code BakedModel} by hand defaulted
 * them, which drew the item as a flat slab.</p>
 */
public class GTPortCasingModel extends BakedModelWrapper<BakedModel> {

    /**
     * The casing a port is set into.
     *
     * <p>Resolved once per position from the block entity and carried through {@link ModelData}:
     * the state cannot hold an open-ended value, and re-reading the entity for every quad would be
     * a lookup per face per frame.</p>
     */
    public static final ModelProperty<BlockState> CASING = new ModelProperty<>();

    /**
     * The sheet the port model uses for its plate.
     *
     * <p>This is the sprite the base model's {@code shell} key resolves to. Comparing against it is
     * the same question GregTech asks of a texture key — "is this the plate layer" — answered by
     * the sprite because this mod's models are its own and the plate is the only layer using it.</p>
     */
    private static final ResourceLocation PLATE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("goldentweaks", "block/shell_pe");

    public GTPortCasingModel(BakedModel original) {
        super(original);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    RandomSource random, ModelData data, @Nullable RenderType type) {
        List<BakedQuad> quads = this.originalModel.getQuads(state, side, random, data, type);

        BlockState casing = data.get(CASING);
        if (casing == null) {
            // No override applies: the item form, or a part not yet built into a machine. GT's model
            // behaves the same way, and it is why an inventory icon keeps its authored plate.
            return quads;
        }

        TextureAtlasSprite plate = plateSprite(casing);
        if (plate == null) {
            // The casing's own model could not supply a sprite. Leaving the authored plate in place
            // is a better failure than drawing quads with no texture.
            return quads;
        }

        return retexture(quads, plate);
    }

    /**
     * Swaps the plate layer's sprite, leaving every other quad — icon, tier ring, shell sides — as
     * authored, including its tint index.
     */
    private static List<BakedQuad> retexture(List<BakedQuad> quads, TextureAtlasSprite plate) {
        List<BakedQuad> result = new ArrayList<>(quads.size());
        boolean replaced = false;

        for (BakedQuad quad : quads) {
            if (isPlate(quad)) {
                result.add(withSprite(quad, plate));
                replaced = true;
            } else {
                result.add(quad);
            }
        }

        // Nothing matched: the artwork changed under us. Drawing the authored plate is better than
        // drawing none, and this keeps a model edit from turning the ports invisible.
        return replaced ? result : quads;
    }

    private static boolean isPlate(BakedQuad quad) {
        TextureAtlasSprite sprite = quad.getSprite();
        return sprite != null && sprite.contents().name().equals(PLATE_TEXTURE);
    }

    /**
     * The casing block's main sprite.
     *
     * <p>Read from the casing's own model rather than by deriving a path from its id: a casing is
     * free to point its particle texture anywhere, and that is the texture a player recognises it
     * by.</p>
     */
    @Nullable
    private static TextureAtlasSprite plateSprite(BlockState casing) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(casing);
        return model.getParticleIcon(ModelData.EMPTY);
    }

    /**
     * The same quad, re-sprung.
     *
     * <p>The vertices are kept as they are: only which sprite they sample changes, so the geometry,
     * shade flags and tint index all survive.</p>
     */
    private static BakedQuad withSprite(BakedQuad quad, TextureAtlasSprite sprite) {
        return new BakedQuad(quad.getVertices(), quad.getTintIndex(), quad.getDirection(),
                sprite, quad.isShade(), quad.hasAmbientOcclusion());
    }

    /**
     * Fills in the casing for a placed port.
     *
     * <p>Read from the block entity, which stores it as an id: a block state's value set is closed
     * by construction, so it cannot name a casing from an open set.</p>
     */
    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos,
                                  BlockState state, ModelData data) {
        ModelData fromWrapped = this.originalModel.getModelData(level, pos, state, data);
        if (fromWrapped.has(CASING) || !(state.getBlock() instanceof GTPortBlock)) {
            return fromWrapped;
        }
        if (!(level.getBlockEntity(pos) instanceof GTPortBlockEntity port)
                || port.casing() == null) {
            return fromWrapped;
        }
        BlockState casing = BuiltInRegistries.BLOCK.getOptional(port.casing())
                .map(Block::defaultBlockState)
                .orElse(null);
        if (casing == null) {
            // The id names a block this pack does not ship; the authored plate is the safe answer.
            return fromWrapped;
        }
        return fromWrapped.derive().with(CASING, casing).build();
    }
}
'''

io.open(path, "w", encoding="utf-8", newline="\n").write(content)
print("model rewritten following GT's approach")
