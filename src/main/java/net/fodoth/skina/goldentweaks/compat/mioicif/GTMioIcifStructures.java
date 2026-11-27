package net.fodoth.skina.goldentweaks.compat.mioicif;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Facade for projecting and building Singularity Iteration's multiblocks.
 *
 * <p>Every SI type lives in {@link GTMioIcifBlueprints}, which is only touched once SI is known to
 * be installed. This class is what the rest of the mod talks to, so a pack without SI never loads
 * a class that names SI's packages — such a reference fails as a {@code NoClassDefFoundError}
 * while the class is still being verified, before any guard inside it could run.</p>
 */
public final class GTMioIcifStructures {

    /** Id of the mod whose structures are projected. */
    public static final String MOD_ID = "mio_icif";

    /** Main inventory plus hotbar; armour and offhand are excluded. */
    private static final int MAIN_INVENTORY_SIZE = 36;

    private GTMioIcifStructures() {
    }

    /**
     * One block of a projected shape, as an offset from the controller.
     *
     * @param x     offset east of the controller
     * @param y     offset above the controller
     * @param z     offset south of the controller
     * @param block the block that goes there
     */
    public record Cell(int x, int y, int z, Block block) {
    }

    /** How a shape follows its controller's facing. */
    public enum Rotation {
        /** Fixed offsets, whatever way the controller points. */
        NONE,
        /** The fusion tokamak: every offset turns around Y by SI's own rule. */
        FUSION,
        /** The steam tunnel: a 5-deep tunnel generated per horizontal facing. */
        HORIZONTAL
    }

    /**
     * A projected shape.
     *
     * <p>The steam tunnel is pre-expanded into one cell list per facing, because its layout is
     * <em>generated</em> from the facing rather than being a fixed grid that can be rotated. Every
     * other shape keeps one list and is turned on demand.</p>
     */
    public static final class Layout {

        private final Rotation rotation;
        private final List<Cell> base;
        @Nullable
        private final Map<Direction, List<Cell>> horizontal;

        Layout(Rotation rotation, List<Cell> base, @Nullable Map<Direction, List<Cell>> horizontal) {
            this.rotation = rotation;
            this.base = List.copyOf(base);
            this.horizontal = horizontal;
        }

        public Rotation rotation() {
            return this.rotation;
        }

        /** The cells to draw or build for a controller pointing the given way. */
        public List<Cell> cells(Direction facing) {
            if (this.rotation == Rotation.HORIZONTAL && this.horizontal != null) {
                List<Cell> aimed = this.horizontal.get(facing);
                if (aimed != null) {
                    return aimed;
                }
                return this.horizontal.getOrDefault(Direction.EAST, this.base);
            }
            if (this.rotation == Rotation.FUSION) {
                List<Cell> turned = turnFusion(facing);
                if (turned != null) {
                    return turned;
                }
            }
            return this.base;
        }

        /** The shell block this shape is built from, or {@code null} if it has none. */
        @Nullable
        public Block casing() {
            return this.base.isEmpty() ? null : this.base.getFirst().block();
        }

        @Nullable
        private List<Cell> turnFusion(Direction facing) {
            if (!available()) {
                return null;
            }
            try {
                List<Cell> out = new java.util.ArrayList<>(this.base.size());
                for (Cell cell : this.base) {
                    BlockPos turned = GTMioIcifBlueprints.rotateFusion(
                            facing, cell.x(), cell.y(), cell.z());
                    out.add(new Cell(turned.getX(), turned.getY(), turned.getZ(), cell.block()));
                }
                return out;
            } catch (LinkageError error) {
                return null;
            }
        }
    }

    /** Whether SI is installed. */
    public static boolean available() {
        return ModList.get().isLoaded(MOD_ID);
    }

    /**
     * The shape an SI controller block projects.
     *
     * @return the layout, or {@code null} when this block is not an SI multiblock controller
     */
    @Nullable
    public static Layout layoutFor(BlockState state) {
        if (!available()) {
            return null;
        }
        try {
            return GTMioIcifBlueprints.layoutFor(state);
        } catch (LinkageError error) {
            // SI is present but lacks an API we compiled against: degrade to no projection.
            return null;
        }
    }

    /** The controller's facing, or {@link Direction#EAST} when it has no such property. */
    public static Direction facing(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof DirectionProperty && property.getName().equals("facing")) {
                return state.getValue((DirectionProperty) property);
            }
        }
        return Direction.EAST;
    }

    /** Whether the controller reports a formed structure, so the projection can hide itself. */
    public static boolean formed(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property.getName().equals("formed") && property.getValueClass() == Boolean.class) {
                return Boolean.TRUE.equals(state.getValue((Property<?>) property));
            }
        }
        return false;
    }

    /** Block ids SI registers as multiblock controllers; empty when SI is absent. */
    public static List<ResourceLocation> controllerIds() {
        if (!available()) {
            return List.of();
        }
        try {
            return GTMioIcifBlueprints.controllerIds();
        } catch (LinkageError error) {
            return List.of();
        }
    }

    /**
     * Outcome of a build attempt.
     *
     * @param placed  how many blocks were placed
     * @param missing how many cells are still empty afterwards
     */
    public record BuildResult(int placed, int missing) {

        /** True when the build could not finish, so the player still needs to see the gaps. */
        public boolean incomplete() {
            return this.missing > 0;
        }
    }

    /**
     * Fills every empty cell of the controller's shape with its own block.
     *
     * <p>Only cells that are free are touched: anything already standing there — a port the player
     * placed, or an unrelated block — is left alone, so running this again never overwrites work.
     * Creative placement does not consume the inventory.</p>
     */
    public static BuildResult build(Level level, BlockPos controller, BlockState hostState, Player player) {
        Layout layout = layoutFor(hostState);
        if (layout == null || level.isClientSide) {
            return new BuildResult(0, 0);
        }

        Direction facing = facing(hostState);
        boolean creative = player.getAbilities().instabuild;

        int placed = 0;
        int missing = 0;
        for (Cell cell : layout.cells(facing)) {
            BlockPos pos = controller.offset(cell.x(), cell.y(), cell.z());
            if (!level.isLoaded(pos)) {
                continue;
            }
            if (!level.getBlockState(pos).canBeReplaced()) {
                // Already filled, by us or by the player; not a gap.
                continue;
            }
            if (!creative && !consume(player, cell.block())) {
                missing++;
                continue;
            }
            level.setBlock(pos, cell.block().defaultBlockState(), Block.UPDATE_ALL);
            placed++;
        }

        if (!creative && placed > 0) {
            player.getInventory().setChanged();
        }
        return new BuildResult(placed, missing);
    }

    /** Takes one matching block from the player's main inventory. */
    private static boolean consume(Player player, Block type) {
        var inventory = player.getInventory();
        int slots = Math.min(inventory.getContainerSize(), MAIN_INVENTORY_SIZE);
        for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
                continue;
            }
            if (!blockItem.getBlock().equals(type)) {
                continue;
            }
            stack.shrink(1);
            return true;
        }
        return false;
    }
}
