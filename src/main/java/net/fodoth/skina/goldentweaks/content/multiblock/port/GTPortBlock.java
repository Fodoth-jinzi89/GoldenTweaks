package net.fodoth.skina.goldentweaks.content.multiblock.port;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.MultiblockPortTier;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.PortMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTCasingProvider;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMultiblockBlocks;

/**
 * A port of this mod's own multiblock, replacing AMI's port blocks.
 *
 * <p>AMI's port is three exactly coincident cubes whose shell texture provides the window, so it
 * cannot be re-skinned: a solid casing at the same depth hides the icon, and every attempt to work
 * around that had to patch two render paths at once. This block is modelled the way the rest of the
 * mod's own components are — a solid shell cube with the icon and tier ring as flat overlays a
 * fraction of a pixel proud of the front face. Nothing of AMI's renderer is involved.</p>
 *
 * <p>Only the front face is usable. The port is embedded in a wall, so its other five faces are
 * buried; the capability is therefore exposed on the facing side alone.</p>
 */
public class GTPortBlock extends BaseEntityBlock implements GTCasingProvider {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    /**
     * Which casing this port's plate is drawn with, as an index into
     * {@link GTMultiblockBlocks#CASINGS}.
     *
     * <p>In the state because the connected-texture hook that picks a face's sheet is given the state
     * and nothing else. The bound is the list's size, which is why adding a casing is an edit to that
     * list rather than to anything here.</p>
     */
    public static final IntegerProperty CASING = IntegerProperty.create("casing",
            0, GTMultiblockBlocks.CASINGS.size() - 1);

    private final GTPortKind kind;
    private final PortMode mode;
    /**
     * The tier this block was registered at.
     *
     * <p>A field rather than a block state value: each tier is its own block, so the tier is fixed
     * per instance and never changes. Keeping it out of the state also means the item and the block
     * cannot disagree about which tier they are.</p>
     */
    private final GTPortTier tier;
    private final MapCodec<GTPortBlock> codec;

    public GTPortBlock(Properties properties, GTPortKind kind, PortMode mode, GTPortTier tier) {
        super(properties);
        this.kind = kind;
        this.mode = mode;
        this.tier = tier;
        // The codec has to close over this instance's kind, mode and tier: they are fixed per
        // registered block, not part of the serialized state, so a reflective codec cannot rebuild
        // them.
        this.codec = simpleCodec(propertiesIn -> new GTPortBlock(propertiesIn, kind, mode, tier));
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(CASING, 0));
    }

    public GTPortKind kind() {
        return this.kind;
    }

    public PortMode mode() {
        return this.mode;
    }

    /** The voltage tier this block belongs to. */
    public GTPortTier portTier() {
        return this.tier;
    }

    /** The casing block a state's plate is drawn with. */
    public static Block casingOf(BlockState state) {
        return state.hasProperty(CASING)
                ? GTMultiblockBlocks.casingAt(state.getValue(CASING))
                : GTMultiblockBlocks.casingAt(0);
    }

    @Override
    public Block gtCasing(BlockState state) {
        return casingOf(state);
    }


    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return this.codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CASING);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        // The model draws the shell, icon and ring; no block entity renderer is involved.
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(net.minecraft.core.@NotNull BlockPos pos, @NotNull BlockState state) {
        return new GTPortBlockEntity(pos, state);
    }

    /**
     * The port ticks so an active one can reach through its face.
     *
     * <p>Only the server side matters: the reach-through moves real contents, and a client copy
     * doing it as well would double every transfer.</p>
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide || type != Objects.requireNonNull(GTMultiblockBlocks.PORT_ENTITY).get()) {
            return null;
        }
        return (tickingLevel, pos, tickingState, blockEntity) -> {
            if (blockEntity instanceof GTPortBlockEntity port) {
                port.serverTick();
            }
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    /**
     * Opens the port's own window.
     *
     * <p>Only the server opens it: the menu is built there and the client receives the id, so
     * opening on both sides would produce two containers. The interaction is reported as success
     * on both sides so the arm swing looks the same.</p>
     */
    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!level.isClientSide
                && level.getBlockEntity(pos) instanceof GTPortBlockEntity port
                && player instanceof ServerPlayer serverPlayer) {
            // The kind and slot count are written here, from the server's own entity. The client
            // cannot work them out reliably: it would have to read its copy of the entity, and
            // that copy is only ever told about item stacks, never about the layout.
            serverPlayer.openMenu(port, buffer -> {
                buffer.writeBlockPos(pos);
                buffer.writeEnum(port.kind());
                buffer.writeVarInt(port.itemHandler() == null
                        ? 0 : Objects.requireNonNull(port.itemHandler()).getSlots());
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Drops the port's contents, the way the machine drops its controller slot.
     *
     * <p>Same helper and same guard as the controller: {@code Containers.dropContentsOnDestroy}
     * only acts when the block actually changes, so a state update drops nothing. Contents are taken
     * out through the container view here rather than at the moment of the drop, because they have
     * to be packed into matter clusters first — dropping them one slot at a time would scatter a
     * hatch's whole inventory across the ground.</p>
     *
     * <p>Fluids are packed the same way, from the tanks. Energy is deliberately not carried: a
     * broken hatch comes back as the hatch alone, since charge has no item to travel in.</p>
     */
    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState,
                         boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof GTPortBlockEntity port) {
            port.dropContentsPacked();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * Drops the port as an item, carrying the name the player gave it.
     *
     * <p>The loot table cannot do this: it produces a plain item with no components, so a renamed
     * port would come back under its default name. Replacing the drop here is the only place that
     * still has the block entity to read the name from.</p>
     *
     * <p>The name is the only thing carried over. A port's contents, tier and channel are its
     * working state, and the item is meant to be a blank part — the fields are reset by the entity's
     * own defaults when it is placed again.</p>
     */
    @Override
    public void playerDestroy(Level level, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state,
                              @Nullable BlockEntity blockEntity, @NotNull ItemStack tool) {
        if (!level.isClientSide
                && blockEntity instanceof GTPortBlockEntity port
                && port.getCustomName() != null) {
            ItemStack drop = new ItemStack(this);
            drop.set(DataComponents.CUSTOM_NAME, port.getCustomName());
            popResource(level, pos, drop);
            return;
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                            @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        // Carry the name over from the item, so a port picked up under a custom name comes back
        // with it. The block's own default is used when the item has none.
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name != null && level.getBlockEntity(pos) instanceof GTPortBlockEntity port) {
            port.setCustomName(name);
        }
    }

    /** The tier of a state's block, as AMI's enum. Used by the structure scan. */
    public static MultiblockPortTier tierOf(BlockState state) {
        return state.getBlock() instanceof GTPortBlock port
                ? port.portTier().ami() : MultiblockPortTier.values()[0];
    }
}
