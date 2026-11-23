package net.fodoth.skina.goldentweaks.compat.thaumicenergistics;

import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.helpers.InterfaceLogicHost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.integration.appeng.IEssentiaInterfaceLogic;
import thaumicenergistics.common.integration.tc.AspectIds;

/**
 * Talks to a ThaumicEnergistics ME interface that carries the essentia card.
 *
 * <p>The interface block does not expose Thaumcraft's {@code EssentiaCapabilities.TRANSPORT}: that
 * capability is registered only for the mod's export-bus part, so a port reading its neighbour
 * through the capability sees nothing at all. Nor is the interface logic the block entity itself —
 * the block entity implements AE2's {@link InterfaceLogicHost} and the essentia behaviour is mixed
 * into the {@code InterfaceLogic} it hands out. So the lookup goes host → logic → interface.</p>
 *
 * <p>Kept in its own class so a pack without ThaumicEnergistics never links these types.</p>
 */
public final class GTInterfaceEssentia {

    private GTInterfaceEssentia() {
    }

    /**
     * The essentia logic facing {@code side} at {@code pos}, or {@code null}.
     *
     * <p>An interface exists in two shapes. As a block it is the block entity itself; as a panel it
     * is a part mounted on a cable bus, and only the part carries {@link InterfaceLogicHost}. The
     * essenta behaviour is mixed into the {@code InterfaceLogic} either way, so the lookup is
     * host → logic → interface.</p>
     */
    @Nullable
    public static IEssentiaInterfaceLogic logicAt(Level level, BlockPos pos, Direction side) {
        if (!level.isLoaded(pos)) {
            return null;
        }
        BlockEntity entity = level.getBlockEntity(pos);
        Object host = entity;
        if (entity instanceof CableBusBlockEntity cableBus) {
            // The panel sits on the face the port is touching, so it is addressed by that side.
            host = cableBus.getPart(side);
        }
        if (!(host instanceof InterfaceLogicHost logicHost)) {
            return null;
        }
        Object logic = logicHost.getInterfaceLogic();
        return logic instanceof IEssentiaInterfaceLogic essentia ? essentia : null;
    }

    /** Whether an interface with its essentia card installed stands at {@code pos}. */
    public static boolean isActive(Level level, BlockPos pos, Direction side) {
        IEssentiaInterfaceLogic logic = logicAt(level, pos, side);
        return logic != null && logic.thE$essentiaActive();
    }

    /** The aspect the interface holds, or {@code null} when it holds nothing. */
    @Nullable
    public static Aspect stockAspect(Level level, BlockPos pos, Direction side) {
        IEssentiaInterfaceLogic logic = logicAt(level, pos, side);
        return logic == null ? null : logic.thE$stockAspect();
    }

    /** How much of an aspect the interface holds. */
    public static int stockAmount(Level level, BlockPos pos, Direction side, Aspect aspect) {
        IEssentiaInterfaceLogic logic = logicAt(level, pos, side);
        return logic == null || aspect == null ? 0 : logic.thE$stockAmount(aspect);
    }

    /** Takes up to {@code amount} of an aspect out of an interface. */
    public static int take(Level level, BlockPos pos, Direction side, Aspect aspect, int amount) {
        IEssentiaInterfaceLogic logic = logicAt(level, pos, side);
        if (logic == null || aspect == null || amount <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(amount, logic.thE$takeFromStock(aspect, amount)));
    }

    /** Offers up to {@code amount} of an aspect; returns how much the interface took. */
    public static int accept(Level level, BlockPos pos, Direction side, Aspect aspect, int amount) {
        IEssentiaInterfaceLogic logic = logicAt(level, pos, side);
        if (logic == null || aspect == null || amount <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(amount, logic.thE$acceptEssentia(aspect, amount)));
    }

    /** The game id of an aspect, as the interface would report it. */
    @Nullable
    public static ResourceLocation idOf(Aspect aspect) {
        return aspect == null ? null : AspectIds.getAspectId(aspect);
    }
}
