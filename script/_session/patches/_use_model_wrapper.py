"""Wrap through NeoForge's BakedModelWrapper instead of implementing BakedModel by hand.

Implementing the interface directly looked fine and compiled, but `BakedModel` has surface beyond
the abstract methods — item transforms, render types, ambient occlusion defaults — and every one
left unimplemented silently took a default. The visible result was a flat, untextured slab in the
inventory: with no transforms the item was drawn at block scale with no camera, so the whole cube
collapsed into one visible face.

`BakedModelWrapper` forwards everything by default, so only the two methods that matter here are
overridden and the rest behaves exactly as the authored model does.
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
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
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
 * <p>The casing arrives through {@link ModelData}, filled in per position, so a single registered
 * model answers every casing: adding one costs a texture and nothing else.</p>
 *
 * <p>Derived from {@link BakedModelWrapper} rather than implementing the interface directly. The
 * interface has more to it than its abstract methods — item transforms above all — and a hand-
 * written implementation silently defaulted them, which is what turned the item into a flat slab.</p>
 */
public class GTPortCasingModel extends BakedModelWrapper<BakedModel> {

    /**
     * The casing a port is set into.
     *
     * <p>Resolved once per position from the block entity and carried through {@link ModelData}:
     * the state itself cannot hold an open-ended value, and re-reading the entity for every quad
     * would be a lookup per face per frame.</p>
     */
    public static final ModelProperty<BlockState> CASING = new ModelProperty<>();

    /**
     * The sheet the port model uses for its plate.
     *
     * <p>Identified by texture rather than by element index: an index breaks the moment an element
     * is added, and this has to survive the artwork changing.</p>
     */
    private static final ResourceLocation PLATE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("goldentweaks", "block/shell_pe");

    public GTPortCasingModel(BakedModel original) {
        super(original);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    RandomSource random, ModelData data, @Nullable RenderType type) {
        List<BakedQuad> quads = this.original.getQuads(state, side, random, data, type);

        BlockState casing = data.get(CASING);
        if (casing == null) {
            // Nothing recorded: the item form, or a part not yet built into a machine. The model's
            // own plate is the right answer for both.
            return quads;
        }

        return reskin(quads, casing);
    }

    private List<BakedQuad> reskin(List<BakedQuad> quads, BlockState casing) {
        TextureAtlasSprite plate = plateSprite(casing);
        if (plate == null) {
            // The casing's model has no sprite to offer — a broken or missing model. Leaving the
            // authored plate alone is better than drawing quads with no texture at all.
            return quads;
        }

        List<BakedQuad> result = new ArrayList<>(quads.size());
        boolean replaced = false;
        for (BakedQuad quad : quads) {
            if (isPlate(quad)) {
                result.add(copyWithSprite(quad, plate));
                replaced = true;
            } else {
                result.add(quad);
            }
        }
        // Nothing matched means the model was rebuilt with different artwork; drawing its own plate
        // is a better failure than drawing none.
        return replaced ? result : quads;
    }

    private static boolean isPlate(BakedQuad quad) {
        TextureAtlasSprite sprite = quad.getSprite();
        return sprite != null && sprite.contents().name().equals(PLATE_TEXTURE);
    }

    /**
     * The casing block's own particle sprite, which is its main texture.
     *
     * <p>Read from the casing's model rather than by deriving a texture path, so a casing whose
     * artwork is laid out differently still works.</p>
     */
    private static TextureAtlasSprite plateSprite(BlockState casing) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(casing);
        return model.getParticleIcon(ModelData.EMPTY);
    }

    private static BakedQuad copyWithSprite(BakedQuad quad, TextureAtlasSprite sprite) {
        return new BakedQuad(quad.getVertices(), quad.getTintIndex(), quad.getDirection(),
                sprite, quad.isShade(), quad.hasAmbientOcclusion());
    }

    /**
     * Fills in the casing for a placed port.
     *
     * <p>Read from the block entity, which carries it as an id: a block state's value set is closed
     * by construction, so it cannot name a casing from an open set.</p>
     */
    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                  ModelData data) {
        ModelData fromWrapped = this.original.getModelData(level, pos, state, data);
        if (fromWrapped.has(CASING)) {
            return fromWrapped;
        }
        if (!(state.getBlock() instanceof GTPortBlock)) {
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
            // The id names a block this pack does not have; the authored plate is the safe answer.
            return fromWrapped;
        }
        return fromWrapped.derive().with(CASING, casing).build();
    }
}
'''

io.open(path, "w", encoding="utf-8", newline="\n").write(content)
print("model derives from BakedModelWrapper")
