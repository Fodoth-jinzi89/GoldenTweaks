package net.fodoth.skina.goldentweaks.compat.mioicif;

import com.miophas.singularity_iteration.common.blockentity.generator.SteamTunnelStructure;
import com.miophas.singularity_iteration.common.fusion.FusionStructure;
import com.miophas.singularity_iteration.core.api.multiblock.StructureBlueprint;
import com.miophas.singularity_iteration.core.api.multiblock.StructureBlueprint.BlockRef;
import com.miophas.singularity_iteration.core.api.multiblock.StructureBlueprint.Key;
import com.miophas.singularity_iteration.core.api.multiblock.StructureBlueprint.Placement;
import com.miophas.singularity_iteration.core.api.multiblock.StructureBlueprintRegistry;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.mioicif.GTMioIcifStructures.Cell;
import net.fodoth.skina.goldentweaks.compat.mioicif.GTMioIcifStructures.Layout;
import net.fodoth.skina.goldentweaks.compat.mioicif.GTMioIcifStructures.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads Singularity Iteration's own structure definitions and turns them into projections.
 *
 * <p>This class names SI's types, so it must only be loaded when SI is installed; everything
 * reaches it through {@link GTMioIcifStructures}, which checks first.</p>
 *
 * <p>{@link StructureBlueprint#placements()} holds absolute grid coordinates with the controller at
 * a known cell, so subtracting the controller's cell gives exactly the offset SI's validator will
 * look at — the projection lands where the machine actually forms.</p>
 *
 * <p><b>Rotation.</b> Most SI validators ignore the controller's facing and check fixed offsets.
 * Two do not: the fusion tokamak turns every offset by {@link FusionStructure#turns(Direction)},
 * and the steam tunnel <em>generates</em> its 5-deep tunnel from the facing through
 * {@link SteamTunnelStructure#layout(BlockPos, Direction)} — which is why the tunnel is expanded
 * per facing instead of being rotated after the fact.</p>
 */
final class GTMioIcifBlueprints {

    /** Namespace SI registers its blueprints under. */
    private static final String NAMESPACE = "mio_icif";

    private static final ResourceLocation FUSION =
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, "fusion_reactor");

    private static final ResourceLocation STEAM_TUNNEL =
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, "steam_tunnel");

    /** Resolved per controller block; blueprints never change once installed. */
    private static final Map<Block, Layout> CACHE = new HashMap<>();

    private GTMioIcifBlueprints() {
    }

    /**
     * The layout a controller block projects.
     *
     * <p>A miss is only cached when the answer is trustworthy: during startup
     * {@code BuiltInRegistries.BLOCK.getKey} can still answer {@code minecraft:air} for a block
     * that is not bound yet, and the blueprint registry can still be empty. Caching either as
     * "not a controller" would break the projection for that block forever.</p>
     */
    @Nullable
    static Layout layoutFor(BlockState state) {
        Block block = state.getBlock();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        if (blockId == null || !NAMESPACE.equals(blockId.getNamespace())) {
            return null;
        }

        if (CACHE.containsKey(block)) {
            return CACHE.get(block);
        }

        Layout resolved = resolve(blockId);
        if (resolved != null) {
            CACHE.put(block, resolved);
        }
        return resolved;
    }

    @Nullable
    private static Layout resolve(ResourceLocation blockId) {
        List<StructureBlueprint> all = List.copyOf(StructureBlueprintRegistry.all());
        if (all.isEmpty()) {
            // Blueprints are installed during common setup; too early means "ask again later".
            return null;
        }

        List<StructureBlueprint> candidates = new ArrayList<>();
        for (StructureBlueprint blueprint : all) {
            if (ownsController(blueprint, blockId)) {
                candidates.add(blueprint);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }

        // One controller can own several shapes — the nuclear reactor builds either a 3x3x3 core or
        // the full 5x5x5 vessel. Project the largest, which is the complete machine.
        StructureBlueprint chosen = candidates.getFirst();
        for (StructureBlueprint candidate : candidates) {
            if (volume(candidate) > volume(chosen)) {
                chosen = candidate;
            }
        }
        return convert(chosen);
    }

    private static boolean ownsController(StructureBlueprint blueprint, ResourceLocation blockId) {
        Key controller = blueprint.controller();
        if (controller == null) {
            return false;
        }
        for (BlockRef ref : controller.blocks()) {
            if (blockId.equals(ref.block())) {
                return true;
            }
        }
        return false;
    }

    private static long volume(StructureBlueprint blueprint) {
        return (long) blueprint.sizeX() * blueprint.sizeY() * blueprint.sizeZ();
    }

    @Nullable
    private static Layout convert(StructureBlueprint blueprint) {
        BlockPos master = blueprint.controllerPos();
        if (master == null) {
            return null;
        }

        if (STEAM_TUNNEL.equals(blueprint.id())) {
            Layout tunnel = convertSteamTunnel(blueprint);
            if (tunnel != null) {
                return tunnel;
            }
        }

        List<Cell> cells = new ArrayList<>();
        for (Placement placement : blueprint.placements()) {
            BlockPos pos = placement.pos();
            if (pos.equals(master)) {
                continue;
            }
            Block block = preferredBlock(placement.key().blocks());
            if (block == null) {
                continue;
            }
            cells.add(new Cell(
                    pos.getX() - master.getX(),
                    pos.getY() - master.getY(),
                    pos.getZ() - master.getZ(),
                    block));
        }

        Rotation rotation = FUSION.equals(blueprint.id()) ? Rotation.FUSION : Rotation.NONE;
        GoldenTweaks.LOGGER.info("[GT] SI projection {}: {} cells ({})",
                blueprint.id(), cells.size(), rotation);
        return new Layout(rotation, cells, null);
    }

    /**
     * Expands the steam tunnel for each of the four horizontal facings.
     *
     * <p>Its cells come from {@link SteamTunnelStructure#layout}, which already applies the facing;
     * rotating the blueprint's own grid afterwards would point the tunnel the wrong way.</p>
     */
    @Nullable
    private static Layout convertSteamTunnel(StructureBlueprint blueprint) {
        Block shell = shellBlock(blueprint);
        if (shell == null) {
            return null;
        }
        BlockPos origin = BlockPos.ZERO;

        Map<Direction, List<Cell>> byFacing = new EnumMap<>(Direction.class);
        for (Direction facing : List.of(Direction.NORTH, Direction.EAST,
                Direction.SOUTH, Direction.WEST)) {
            List<Cell> cells = new ArrayList<>();
            for (Map.Entry<BlockPos, SteamTunnelStructure.Role> entry
                    : SteamTunnelStructure.layout(origin, facing).entrySet()) {
                SteamTunnelStructure.Role role = entry.getValue();
                if (role == SteamTunnelStructure.Role.CONTROLLER
                        || role == SteamTunnelStructure.Role.INTERIOR) {
                    // The interior is the open bore the rotor sits in: SI's own validator requires
                    // air there (or a rotor), so previewing or building a casing would both draw the
                    // wrong shape and, worse, wall up the tunnel so it can never form.
                    continue;
                }
                // Every remaining cell previews as the shell, including FACE cells that could hold a
                // port: the shell is what the player fills in, a port is a deliberate choice.
                BlockPos pos = entry.getKey();
                cells.add(new Cell(pos.getX(), pos.getY(), pos.getZ(), shell));
            }
            byFacing.put(facing, List.copyOf(cells));
        }

        List<Cell> east = byFacing.getOrDefault(Direction.EAST, List.of());
        GoldenTweaks.LOGGER.info("[GT] SI projection {}: {} cells per facing (HORIZONTAL)",
                blueprint.id(), east.size());
        return new Layout(Rotation.HORIZONTAL, east, byFacing);
    }

    /** The shell block a shape uses, taken from a key that reads as casing. */
    @Nullable
    private static Block shellBlock(StructureBlueprint blueprint) {
        for (Key key : blueprint.keys().values()) {
            for (BlockRef ref : key.blocks()) {
                ResourceLocation id = ref.block();
                if (id == null) {
                    continue;
                }
                if (isCasingId(id.getPath())) {
                    Block block = BuiltInRegistries.BLOCK.get(id);
                    if (block != null) {
                        return block;
                    }
                }
            }
        }
        return null;
    }

    /**
     * The block a cell should preview and build as.
     *
     * <p>SI lists a cell's acceptable blocks in author order; for port-capable cells the casing is
     * usually first but not always. A casing is preferred over a port regardless of order: the
     * shell is what the player fills in en masse, and a port is a deliberate choice made afterwards.</p>
     */
    @Nullable
    private static Block preferredBlock(List<BlockRef> refs) {
        Block first = null;
        for (BlockRef ref : refs) {
            ResourceLocation id = ref.block();
            if (id == null) {
                continue;
            }
            Block block = BuiltInRegistries.BLOCK.get(id);
            if (block == null) {
                continue;
            }
            if (first == null) {
                first = block;
            }
            if (isCasingId(id.getPath())) {
                return block;
            }
        }
        return first;
    }

    /** Whether an id reads as shell material rather than a functional port. */
    private static boolean isCasingId(String path) {
        String name = path.substring(path.lastIndexOf('/') + 1);
        return name.contains("casing")
                || name.contains("vessel")
                || name.contains("shell")
                || name.contains("scaffold")
                || name.endsWith("_base");
    }

    /** Rotates an offset by the same rule SI uses for the fusion tokamak. */
    static BlockPos rotateFusion(Direction facing, int x, int y, int z) {
        return FusionStructure.turn(x, y, z, FusionStructure.turns(facing));
    }

    static List<ResourceLocation> controllerIds() {
        List<ResourceLocation> ids = new ArrayList<>();
        for (StructureBlueprint blueprint : StructureBlueprintRegistry.all()) {
            Key controller = blueprint.controller();
            if (controller == null) {
                continue;
            }
            for (BlockRef ref : controller.blocks()) {
                ResourceLocation id = ref.block();
                if (id != null && !ids.contains(id)) {
                    ids.add(id);
                }
            }
        }
        return List.copyOf(ids);
    }
}
