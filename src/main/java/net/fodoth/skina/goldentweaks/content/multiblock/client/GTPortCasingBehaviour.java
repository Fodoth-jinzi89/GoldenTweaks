package net.fodoth.skina.goldentweaks.content.multiblock.client;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import net.createmod.catnip.render.StitchedSprite;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTCasingProvider;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMultiblockBlocks;
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
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTShellBlock;

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
     * The CT layout a casing sheet uses, which depends on how that casing's art was drawn.
     *
     * <p>{@code OMNIDIRECTIONAL} reads the four diagonals as well as the edges, so a plate meeting
     * another diagonally gets a concave inner corner rather than a straight seam. That needs an 8x8
     * sheet of 47 usable cells, which in turn needs four corner marks the 4x4 art has no pixel for.</p>
     *
     * <p>A casing whose sheet has not been converted has to stay on {@code RECTANGLE}: its {@code _ctm}
     * is 4x4, and reading it with the eight-direction layout would slice each cell into quarters. Which
     * one applies is declared by the block, not read off the sheet — see {@link #resolveType}.</p>
     *
     * <p>Only the converted sheet ships. The 4x4 sheet and the corner tile it is built from are art
     * sources; they live under {@code arts/} and stay out of the jar.</p>
     */
    private static final CTType TYPE_OMNI = AllCTTypes.OMNIDIRECTIONAL;
    private static final CTType TYPE_EDGES = AllCTTypes.RECTANGLE;

    /**
     * Every layout a casing may name, keyed by the name used in the block registration.
     *
     * <p>Naming is needed because four of the eight layouts are 4x4 sheets — CROSS, ROOF, ROOF_STAIR
     * and RECTANGLE — so the sheet's size cannot tell them apart.</p>
     */
    private static final Map<String, CTType> NAMED_LAYOUTS = Map.of(
            "HORIZONTAL", AllCTTypes.HORIZONTAL,
            "HORIZONTAL_KRYPPERS", AllCTTypes.HORIZONTAL_KRYPPERS,
            "VERTICAL", AllCTTypes.VERTICAL,
            "OMNIDIRECTIONAL", AllCTTypes.OMNIDIRECTIONAL,
            "ROOF", AllCTTypes.ROOF,
            "ROOF_STAIR", AllCTTypes.ROOF_STAIR,
            "CROSS", AllCTTypes.CROSS,
            "RECTANGLE", AllCTTypes.RECTANGLE);

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

    /**
     * Which layout each casing's art uses, resolved alongside {@link #SHIFTS}.
     *
     * <p>Kept separate from the shifts because {@code getDataType} is asked during chunk meshing and
     * must not touch the atlas there. Emptied until the first stitch, during which every casing falls
     * back to the edges-only layout — which is what its art is until proven otherwise.</p>
     */
    private static volatile Map<Block, CTType> TYPES = Map.of();

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

    /**
     * The layout to decode this cell's neighbours with.
     *
     * <p>`CTModel` calls this to pick the index formula, so it has to describe the sheet actually on
     * the plate — which is why it is answered per casing rather than from a shared constant. Answering
     * {@code OMNIDIRECTIONAL} for a casing whose art is still 4x4 would sample a quarter-cell and shift
     * every quad into the wrong place.</p>
     */
    @Override
    public CTType getDataType(BlockAndTintGetter level, BlockPos pos, BlockState state,
                              Direction direction) {
        return typeOf(casingOf(state));
    }

    /**
     * The layout a casing's art uses: eight-direction when it has corner marks, else edges only.
     *
     * <p>Read from {@link #TYPES}, which is resolved once per stitch rather than per quad: this runs for
     * every face of every plate while chunks are meshed, and asking the atlas there would put a map
     * lookup on the meshing path for a value that cannot change between stitches.</p>
     */
    private static CTType typeOf(Block casing) {
        return casing == null ? TYPE_EDGES : TYPES.getOrDefault(casing, TYPE_EDGES);
    }

    /** The casing a state draws, or {@code null} when its block draws none. */
    private static Block casingOf(BlockState state) {
        return state.getBlock() instanceof GTCasingProvider provider
                ? provider.gtCasing(state)
                : null;
    }

    /** Whether a sprite is a casing's own sheet, and so a plate that may be re-skinned. */
    private static boolean isCasingSheet(ResourceLocation sprite) {
        for (Block casing : allCasings()) {
            if (sprite.equals(sheetOf(casing))) {
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
        TextureAtlas atlas = Minecraft.getInstance().getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS);

        java.util.List<Block> casings = allCasings();
        Map<Block, CTType> types = new HashMap<>();
        Map<Shift, CTSpriteShiftEntry> built = new HashMap<>();
        for (Block drawn : casings) {
            ResourceLocation drawnSheet = sheetOf(drawn);
            for (Block target : casings) {
                ResourceLocation connectedSheet = connectedSheetOf(target);
                if (connectedSheet == null) {
                    continue;
                }
                // The type belongs to the sheet being sampled, so it is read from that sheet's own
                // size rather than from anything declared beside it.
                CTType type = types.computeIfAbsent(target, block -> resolveType(block, atlas));
                Shift key = new Shift(drawnSheet, connectedSheet);
                CTSpriteShiftEntry entry = buildShift(key, type, atlas);
                if (entry != null) {
                    built.put(key, entry);
                }
            }
        }
        TYPES = Map.copyOf(types);
        SHIFTS = Map.copyOf(built);

        // The sheet's pixel width decides the layout and the layout decides how indices address the
        // sheet, so both are reported: a wrong width here shows up in game as cells reading from the
        // wrong place, which is otherwise very hard to attribute.
        for (Block block : casings) {
            ResourceLocation sheet = connectedSheetOf(block);
            int width = -1;
            if (sheet != null) {
                width = atlas.getSprite(sheet).contents().width();
            }
            GoldenTweaks.LOGGER.info("[GT] casing {}: sheet={} width={} layout={}",
                    BuiltInRegistries.BLOCK.getKey(block), sheet, width,
                    typeOf(block).getId().getPath().toUpperCase(java.util.Locale.ROOT));
        }
        GoldenTweaks.LOGGER.info("[GT] casing shifts resolved: {} ({} with corner art)",
                built.size(), types.values().stream().filter(t -> t == TYPE_OMNI).count());
    }

    /**
     * The layout a casing's connected sheet is drawn in, read from what the casing declares.
     *
     * <p>Nothing is inferred from the sheet's dimensions. Four of the eight layouts are 4x4 sheets, so a
     * size cannot tell CROSS from ROOF from ROOF_STAIR from RECTANGLE — and a wrong guess draws the
     * wrong cells with no visible error, only art that looks subtly off. Every casing therefore names
     * its layout; one that names nothing gets the edges-only default, which at least draws correctly
     * for that layout.</p>
     */
    private static CTType resolveType(Block casing, TextureAtlas atlas) {
        if (casing instanceof GTShellBlock shell) {
            String named = shell.ctLayout();
            if (named != null) {
                CTType type = NAMED_LAYOUTS.get(named);
                if (type != null) {
                    return type;
                }
            }
        }
        return TYPE_EDGES;
    }

    /**
     * Every casing whose sheet should be resolved.
     */
    private static java.util.List<Block> allCasings() {
        java.util.List<Block> all = new java.util.ArrayList<>();
        for (var casing : GTMultiblockBlocks.CASINGS) {
            all.add(casing.get());
        }
        return all;
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
    private static CTSpriteShiftEntry buildShift(Shift key, CTType type, TextureAtlas atlas) {
        // Constructed here rather than through `CTSpriteShifter.getCT`: that helper caches its entry on
        // the first call and never re-runs the texture lookup, so an entry built before the atlas was
        // readable keeps two null sprites for the rest of the session.
        CTSpriteShiftEntry entry = new CTSpriteShiftEntry(type);
        entry.set(key.drawn(), key.connected());

        // `set` only registers proxies; they are filled by this callback, which otherwise ran before
        // this entry existed. Re-running it now fills this one from the current atlas.
        if (atlas != null) {
            StitchedSprite.onTextureStitchPost(atlas);
        }

        return isUsable(entry) ? entry : null;
    }

    /** The block texture a casing draws, from its registered id. */
    private static ResourceLocation sheetOf(Block casing) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(casing);
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath());
    }
}
