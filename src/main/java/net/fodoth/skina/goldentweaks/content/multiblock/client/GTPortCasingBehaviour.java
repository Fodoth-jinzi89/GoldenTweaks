package net.fodoth.skina.goldentweaks.content.multiblock.client;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import net.createmod.catnip.render.StitchedSprite;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.GTCasingProvider;
import net.fodoth.skina.goldentweaks.content.multiblock.GTMultiblockBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * Connects every casing plate to the casing wall it sits in.
 *
 * <p>One rule serves all three kinds of surface: a wall of casing blocks, a port's plate, and a
 * machine's own base plate. Each block states which casing it draws through
 * {@link GTCasingProvider}; this class only has to point the quad at that casing's connected sheet and
 * decide when two neighbouring cells agree.</p>
 *
 * <p>Nothing here knows about a particular machine or casing. A new casing costs one {@code _ctm} sheet
 * and one entry in {@link GTMultiblockBlocks#CASINGS}; a new machine costs nothing at all, because its
 * base plate is a casing like any other and the block it is registered with says which.</p>
 *
 * <p>Why Create's system rather than Continuity's: this hook is given the state and runs through
 * NeoForge's own {@code getQuads}, so a plate can be re-skinned per face without a model file per
 * casing. Continuity resolves sprites from {@code SpriteCalculator} with no block state, which would
 * have needed a model per combination.</p>
 */
public class GTPortCasingBehaviour extends ConnectedTextureBehaviour.Base {

    /**
     * The CT layout a casing sheet uses.
     *
     * <p>A four-direction sheet: a plate connects along the four edges of each face, which is what
     * {@code _ctm} art provides. The corner-aware layouts would expect a larger sheet.</p>
     */
    private static final CTType TYPE = AllCTTypes.RECTANGLE;

    /** A shift entry for one (drawn sheet, connected sheet) pair. */
    private record Shift(ResourceLocation drawn, ResourceLocation connected) {
    }

    /**
     * Every shift, keyed by the pair of sheets.
     *
     * <p>Filled when the atlas is stitched, then only read. Sheets cannot be resolved before that, and
     * must not be resolved during chunk meshing either: building an entry registers new
     * {@link StitchedSprite}s, and registering one on a mesh worker while another thread walks the same
     * registry throws {@link java.util.ConcurrentModificationException}.</p>
     */
    private static volatile Map<Shift, CTSpriteShiftEntry> SHIFTS = Map.of();

    public GTPortCasingBehaviour() {
        super();
    }

    /**
     * The connected entry for a quad, or {@code null} to leave it as authored.
     *
     * <p>`CTModel` passes each quad's own sprite, which is how a single model can keep its icon and
     * ring while its plate is re-skinned: the icon and ring are not a casing sheet, so they never
     * match a shift.</p>
     *
     * <p>The entry starts from the sprite actually on the quad rather than from the casing's sheet.
     * A port model is authored once and draws the default casing whatever casing it is set into, so
     * starting from the casing would build an entry whose original never equals the quad's sprite —
     * and `CTModel` only applies a shift whose original matches.</p>
     */
    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction,
                                       TextureAtlasSprite original) {
        if (original == null) {
            return null;
        }
        // Only a plate is re-skinned. A machine front is a casing plate with the icon, name and arrow
        // drawn over it; shifting those too would remap them into the casing sheet and paint over the
        // artwork. They are drawn from their own files, so they are not casing sheets and stop here.
        ResourceLocation drawn = original.contents().name();
        if (!isCasingSheet(drawn)) {
            return null;
        }
        ResourceLocation connected = connectedSheetOf(casingOf(state));
        if (connected == null) {
            return null;
        }
        return SHIFTS.get(new Shift(drawn, connected));
    }

    /**
     * Whether a neighbouring cell continues this plate.
     *
     * <p>Both sides are asked which casing they draw, so a wall continues into a port set into it and
     * into a machine's base plate, without either side knowing what the other is. Anything that draws
     * no casing — air, a machine part — ends the run, which is what the sheet's edge tiles are for.</p>
     */
    @Override
    public boolean connectsTo(BlockState state, BlockState otherState, BlockAndTintGetter level,
                              BlockPos pos, BlockPos otherPos, Direction direction) {
        Block mine = casingOf(state);
        return mine != null && mine == casingOf(otherState);
    }

    @Override
    public CTType getDataType(BlockAndTintGetter level, BlockPos pos, BlockState state,
                              Direction direction) {
        return TYPE;
    }

    /** The casing a state draws, or {@code null} when its block draws none. */
    private static Block casingOf(BlockState state) {
        return state.getBlock() instanceof GTCasingProvider provider
                ? provider.gtCasing(state)
                : null;
    }

    /** Whether a sprite is a casing's own sheet, and so a plate that may be re-skinned. */
    private static boolean isCasingSheet(ResourceLocation sprite) {
        for (var casing : GTMultiblockBlocks.CASINGS) {
            if (sprite.equals(sheetOf(casing.get()))) {
                return true;
            }
        }
        return false;
    }

    /** The connected sheet for a casing, or {@code null} when that casing has no {@code _ctm} art. */
    private static ResourceLocation connectedSheetOf(Block casing) {
        if (casing == null) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(casing);
        if (id == null) {
            return null;
        }
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath() + "_ctm");
    }

    /**
     * Resolves every shift, now that the block atlas is complete.
     *
     * <p>Run from the stitch event: the atlas is the only place a texture id can be turned into a
     * sprite, and this is the one moment it is both complete and read on a single thread.</p>
     *
     * <p>Every pairing of drawn sheet to connected sheet is resolved, because a port model draws the
     * default casing whatever casing it is set into — the plate painted over can be any casing whose
     * {@code _ctm} art exists.</p>
     */
    public static void rebuildShifts() {
        Map<Shift, CTSpriteShiftEntry> built = new HashMap<>();
        for (var drawn : GTMultiblockBlocks.CASINGS) {
            ResourceLocation drawnSheet = sheetOf(drawn.get());
            if (drawnSheet == null) {
                continue;
            }
            for (var connected : GTMultiblockBlocks.CASINGS) {
                ResourceLocation connectedSheet = connectedSheetOf(connected.get());
                if (connectedSheet == null) {
                    continue;
                }
                Shift key = new Shift(drawnSheet, connectedSheet);
                CTSpriteShiftEntry entry = buildShift(key);
                if (entry != null) {
                    built.put(key, entry);
                }
            }
        }
        SHIFTS = Map.copyOf(built);
        GoldenTweaks.LOGGER.info("[GT] casing shifts resolved: {}", built.size());
    }

    /**
     * Whether a shift entry actually resolved both of its sheets.
     *
     * <p>An entry whose texture never reached the atlas maps every quad onto the missing sprite, which
     * draws as the black-and-magenta checker. Reporting that as unusable leaves the authored plate in
     * place instead.</p>
     */
    private static boolean isUsable(CTSpriteShiftEntry entry) {
        var original = entry.getOriginal();
        var target = entry.getTarget();
        if (original == null || target == null) {
            return false;
        }
        ResourceLocation missing = MissingTextureAtlasSprite.getLocation();
        return !original.contents().name().equals(missing)
                && !target.contents().name().equals(missing);
    }

    /**
     * Builds one shift entry, or {@code null} when either sheet is not in the atlas.
     *
     * <p>Called from {@link #rebuildShifts} and nowhere else: it registers two {@link StitchedSprite}s,
     * which is not safe on a chunk-mesh worker.</p>
     */
    private static CTSpriteShiftEntry buildShift(Shift key) {
        // Constructed here rather than through `CTSpriteShifter.getCT`: that helper caches its entry on
        // the first call and never re-runs the texture lookup, so an entry built before the atlas was
        // readable keeps two null sprites for the rest of the session.
        CTSpriteShiftEntry entry = new CTSpriteShiftEntry(TYPE);
        entry.set(key.drawn(), key.connected());

        // `set` only registers proxies; they are filled by this callback, which otherwise ran before
        // this entry existed. Re-running it now fills this one from the current atlas.
        TextureAtlas atlas = Minecraft.getInstance().getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS);
        if (atlas != null) {
            StitchedSprite.onTextureStitchPost(atlas);
        }

        return isUsable(entry) ? entry : null;
    }

    /** The block texture a casing draws, from its registered id. */
    private static ResourceLocation sheetOf(Block casing) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(casing);
        return id == null ? null
                : ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath());
    }
}
