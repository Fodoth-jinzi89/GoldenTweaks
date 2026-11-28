package net.fodoth.skina.goldentweaks.compat.jade;

import java.math.BigInteger;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMachineBlockEntity;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTReactionStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * Ships the kettle's live state to the client for the tooltip panel.
 *
 * <p>Jade reads tooltips on the client, which has no access to the running recipe — that lives only
 * on the server. This provider is what puts the status, channel, progress, energy and pending
 * products into the packet, so the client side can draw them without needing the machine.</p>
 *
 * <p>Only what the panel shows is sent, and only while it means something: a machine with nothing
 * running sends neither a channel nor products, so an idle machine's tooltip stays short.</p>
 */
public final class GTReactorJadeProvider implements IServerDataProvider<BlockAccessor> {

    /** Key holding everything this provider writes. */
    static final String ROOT = "GoldenTweaksReactor";

    static final String STATUS = "Status";
    static final String CHANNEL = "Channel";
    static final String HAS_CHANNEL = "HasChannel";
    static final String PROGRESS = "Progress";
    static final String LENGTH = "Length";
    static final String ENERGY = "Energy";
    static final String CAPACITY = "Capacity";
    static final String RATE = "Rate";
    /** Highest energy hatch tier in use, as its one-based level; zero when there is none. */
    static final String ENERGY_TIER = "EnergyTier";
    /** The window's state bit set: lock, void, batch, isolation. */
    static final String FLAGS = "Flags";
    /** What the running craft draws each tick after its overclock, in FE per tick. */
    static final String POWER = "Power";
    /** Cost of the cheapest recipe the last search turned down, in FE per tick. */
    static final String REFUSED_COST = "RefusedCost";
    static final String PRODUCTS = "Products";
    /** Kind tag, so the client knows which icon to build for an entry. */
    static final String PRODUCT_KIND = "Kind";
    static final String KIND_ITEM = "item";
    static final String KIND_FLUID = "fluid";
    static final String KIND_NAMED = "named";
    /**
     * The real kind of a {@link #KIND_NAMED} entry.
     *
     * <p>Sent because the client has to rebuild the stack to draw it through EMI, and a chemical and
     * an aspect are not interchangeable there: one resolves through Mekanism's registry, the other
     * through scex. {@code KIND_NAMED} only says "Jade has no element for this".</p>
     */
    static final String PRODUCT_REAL_KIND = "RealKind";
    /** Registry id of the item, fluid, chemical or aspect. */
    static final String PRODUCT_ID = "Id";
    /** Count for an item, millibuckets for a fluid. */
    static final String PRODUCT_COUNT = "Count";

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "chemical_reactor");
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof GTMachineBlockEntity machine)) {
            return;
        }

        CompoundTag root = new CompoundTag();
        root.putString(STATUS, machine.getStatus().name());
        root.putInt(PROGRESS, machine.getRecipeProgress());
        root.putInt(LENGTH, machine.getRecipeLength());
        root.putInt(ENERGY, machine.storedEnergy());
        root.putInt(CAPACITY, machine.energyCapacity());
        root.putInt(RATE, machine.getInputEnergyRate());
        // Next to the hatch rate on purpose: this is what the craft actually costs, and the two are
        // what a player compares when deciding whether the hatches can carry it. Stored as a long
        // because a parallel craft's bill can exceed the int range; saturated, since NBT has no
        // unbounded integer and the panel only ever prints a figure.
        root.putLong(POWER, clampPower(machine.activePowerDraw()));
        // The effective tier, not the raw one: a dual-hatch pair supplies a tier's worth more, and
        // the line sits directly above the power figure it has to agree with.
        root.putInt(ENERGY_TIER, machine.effectiveEnergyTier());
        // Sent as the same bit set the window reads, so the two panels can never disagree about
        // which switches are on.
        root.putInt(FLAGS, machine.getStateFlags());
        // Carried alongside the status: the "not enough power" line names this figure, and the
        // client has no other way to learn it.
        root.putInt(REFUSED_COST, machine.refusedCost());

        Integer channel = machine.activeChannel();
        if (channel != null) {
            root.putBoolean(HAS_CHANNEL, true);
            root.putInt(CHANNEL, channel);
        }

        ListTag products = new ListTag();
        for (GTReactionStack stack : machine.activeProducts()) {
            CompoundTag entry = new CompoundTag();
            boolean listed = true;
            switch (stack.kind()) {
                case ITEM -> {
                    ItemStack item = stack.toItemStack();
                    if (item.isEmpty()) {
                        listed = false;
                        break;
                    }
                    entry.putString(PRODUCT_KIND, KIND_ITEM);
                    entry.putString(PRODUCT_ID,
                            BuiltInRegistries.ITEM.getKey(item.getItem()).toString());
                    // Long throughout: a parallel craft multiplies the per-craft amount, and the
                    // panel would otherwise print a saturated figure as if it were the real one.
                    entry.putLong(PRODUCT_COUNT, item.getCount());
                }
                case FLUID -> {
                    entry.putString(PRODUCT_KIND, KIND_FLUID);
                    entry.putString(PRODUCT_ID, stack.id().toString());
                    entry.putLong(PRODUCT_COUNT, stack.amount());
                }
                default -> {
                    // Chemicals and aspects have no icon Jade can build, but both have one in EMI, so
                    // the id and its real kind are sent and the client draws them through it.
                    entry.putString(PRODUCT_KIND, KIND_NAMED);
                    entry.putString(PRODUCT_REAL_KIND, stack.kind().name());
                    entry.putString(PRODUCT_ID, stack.id().toString());
                    entry.putLong(PRODUCT_COUNT, stack.amount());
                }
            }
            if (listed) {
                products.add(entry);
            }
        }
        root.put(PRODUCTS, products);

        tag.put(ROOT, root);
    }

    /** An exact power figure as the {@code long} NBT speaks, saturated rather than wrapped. */
    private static long clampPower(BigInteger value) {
        if (value.compareTo(LONG_MAX) >= 0) {
            return Long.MAX_VALUE;
        }
        return Math.max(0L, value.longValue());
    }

    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
}
