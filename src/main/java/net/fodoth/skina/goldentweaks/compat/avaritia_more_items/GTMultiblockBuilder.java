package net.fodoth.skina.goldentweaks.compat.avaritia_more_items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Builds a multiblock shape out of the blocks a player is carrying.
 *
 * <p>The structure file names each cell by symbol, and the projector fills in only the cells that
 * stand for casing. Ports are left alone: a port has a function and a facing, so the player has to
 * place those themselves. That distinction comes straight from the structure file — the port
 * symbol maps to the {@code multiblock/ports} tag — which is why the layout is read from
 * {@link GTMultiblockStructures} rather than from AMI's opaque pattern object.</p>
 */
public final class GTMultiblockBuilder {

    /** Tag whose members are ports; every other named cell is casing. */
    private static final TagKey<Block> PORTS =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("goldentweaks", "multiblock/ports"));

    /** Main inventory plus hotbar; armour and offhand are excluded. */
    private static final int MAIN_INVENTORY_SIZE = 36;

    private GTMultiblockBuilder() {
    }

    /**
     * Outcome of a build attempt.
     *
     * @param placed  how many casing blocks were placed
     * @param missing how many casing cells are still empty afterwards
     */
    public record Result(int placed, int missing) {
        /** True when the build could not finish, so the player still needs to see the gaps. */
        public boolean incomplete() {
            return this.missing > 0;
        }
    }

    /**
     * Fills every casing cell of the given structure that is currently empty.
     *
     * <p>Runs to completion even when the player runs out of blocks: whatever fits gets placed and
     * the rest is simply skipped, so a partial build still saves effort.</p>
     */
    public static Result build(Level level, BlockPos masterPos, GTMultiblockPattern layout,
                               Direction forwards, Direction up, boolean mirrored, Player player) {
        if (level.isClientSide) {
            return new Result(0, 0);
        }

        boolean creative = player.getAbilities().instabuild;
        Block type = casingBlock(layout);
        if (type == null) {
            return new Result(0, 0);
        }

        GTMultiblockPattern.Master master = layout.master();
        // The same anchor the matcher uses, so the shape lands exactly where it will be validated.
        // frontTopLeft wants AMI indices, which are flipped relative to ours.
        BlockPos anchor = GTChemicalReactorOrientation.frontTopLeft(
                masterPos, forwards, up, mirrored,
                master.column(),
                layout.height() - 1 - master.layer(),
                layout.depth() - 1 - master.row());

        List<BlockPos> targets = new ArrayList<>();
        for (int layer = 0; layer < layout.height(); layer++) {
            for (int row = 0; row < layout.depth(); row++) {
                for (int column = 0; column < layout.width(); column++) {
                    if (!isCasing(layout, column, layer, row)) {
                        continue;
                    }
                    BlockPos pos = GTChemicalReactorOrientation.cell(
                            anchor, forwards, up, mirrored,
                            layout.height(), layout.depth(), column, layer, row);
                    if (pos.equals(masterPos)) {
                        continue;
                    }
                    // Only fill cells that are free. Anything already standing there — a port the
                    // player placed, or some unrelated block — is left untouched, so running the
                    // projector again never overwrites existing work.
                    if (!level.getBlockState(pos).canBeReplaced()) {
                        continue;
                    }
                    targets.add(pos);
                }
            }
        }

        int placed = 0;
        for (BlockPos pos : targets) {
            if (!creative && !consume(player, type)) {
                break;
            }
            level.setBlock(pos, type.defaultBlockState(), Block.UPDATE_ALL);
            placed++;
        }
        if (!creative && placed > 0) {
            // One inventory refresh for the whole batch rather than one per block.
            player.getInventory().setChanged();
        }
        return new Result(placed, targets.size() - placed);
    }

    /** Whether the cell is casing: a named cell that is not a port and not the master. */
    private static boolean isCasing(GTMultiblockPattern layout, int column, int layer, int row) {
        char symbol = layout.symbolAt(column, layer, row);
        if (layout.isWildcard(symbol)) {
            return false;
        }
        String key = layout.keyAt(symbol);
        if (key == null) {
            return false;
        }
        if (!key.startsWith("#")) {
            // A plain block id: that is either the host or some fixed decoration, never filler.
            return false;
        }
        TagKey<Block> tag = TagKey.create(Registries.BLOCK,
                ResourceLocation.parse(key.substring(1)));
        return !tag.equals(PORTS);
    }

    /**
     * The block a single structure cell stands for.
     *
     * <p>Resolved per symbol rather than once for the whole structure: a shape may name several
     * casing symbols, and each cell should preview with what its own letter means.</p>
     *
     * <p>A tag resolves to its first member that is not a port. The casing tag deliberately also
     * contains the ports tag, so a cell accepting either is legal; drawing a port as shell filler
     * would mislead, and a port is placed by the player with a function in mind.</p>
     *
     * @return the block, or {@code null} when the symbol has no usable member
     */
    @Nullable
    public static Block casingFor(GTMultiblockPattern layout, char symbol) {
        if (layout.isWildcard(symbol)) {
            return null;
        }
        String key = layout.keyAt(symbol);
        if (key == null) {
            return null;
        }
        if (!key.startsWith("#")) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            return id == null ? null : BuiltInRegistries.BLOCK.get(id);
        }

        TagKey<Block> tag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(key.substring(1)));
        if (tag.equals(PORTS)) {
            return null;
        }
        var holders = BuiltInRegistries.BLOCK.getTag(tag);
        if (holders.isEmpty()) {
            return null;
        }
        for (var holder : holders.get()) {
            Block candidate = holder.value();
            if (!candidate.defaultBlockState().is(PORTS)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * The block used as casing, resolved from whatever the structure calls its casing.
     *
     * <p>The casing tag includes the ports tag, so taking "the first block in the tag" could pick a
     * port block and build the whole shell out of it. Members that belong to the ports tag are
     * therefore skipped, and the first remaining block wins.</p>
     *
     * <p>Shared with the projection, which draws its ghost blocks from the same answer: a preview
     * that disagreed with what the builder would place would be worse than no preview.</p>
     */
    public static Block casingBlock(GTMultiblockPattern layout) {
        for (char symbol : layout.symbols()) {
            String key = layout.keyAt(symbol);
            if (key == null || !key.startsWith("#")) {
                continue;
            }
            TagKey<Block> tag = TagKey.create(Registries.BLOCK,
                    ResourceLocation.parse(key.substring(1)));
            if (tag.equals(PORTS)) {
                continue;
            }
            var holders = BuiltInRegistries.BLOCK.getTag(tag);
            if (holders.isEmpty()) {
                continue;
            }
            for (var holder : holders.get()) {
                Block candidate = holder.value();
                if (!candidate.defaultBlockState().is(PORTS)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Takes one matching block from the player's main inventory.
     *
     * <p>Only the thirty-six main and hotbar slots are searched:
     * {@code getContainerSize()} also counts armour and offhand, and pulling casing out of an
     * armour slot would be surprising.</p>
     */
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
