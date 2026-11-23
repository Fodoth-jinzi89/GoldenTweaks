"""Give the essentia port the suction a real reservoir has, but only while it is active.

Thaumcraft's own reservoir reports a minimum suction of 24, and 24 from a face while it still
wants more (it drops to 0 once full). A passive port advertises neither — it is a buffer the
machine feeds, not something competing with the tube network — so suction is zero until the
player switches the port to active.

An output port inverts the sign. Suction is how strongly a block *pulls*; an output wants the
opposite, so a negative amount tells the network this is a source rather than a sink.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTPortEssentiaStorage.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    private final Aspect[] aspects;
    private final int[] amounts;
    private final int tankCapacity;
    private final BooleanSupplier singleCell;
    private Runnable changedHook = () -> {
    };

    public GTPortEssentiaStorage(int tankCount, int tankCapacity, BooleanSupplier singleCell) {
        int count = Math.max(1, tankCount);
        this.aspects = new Aspect[count];
        this.amounts = new int[count];
        this.tankCapacity = tankCapacity;
        this.singleCell = singleCell;
    }''',
'''    /**
     * The suction a Thaumcraft reservoir advertises.
     *
     * <p>Read from {@code EssentiaReservoirBlockEntity}: it reports 24 as its minimum suction,
     * and 24 from a face while it still has room. Matching that number is what makes our port
     * compete with a tube on equal terms instead of starving or hoarding.</p>
     */
    private static final int RESERVOIR_SUCTION = 24;

    private final Aspect[] aspects;
    private final int[] amounts;
    private final int tankCapacity;
    private final BooleanSupplier singleCell;
    /** Whether the port is in active mode; only then does it advertise suction. */
    private final BooleanSupplier active;
    /** Whether this port outputs; an output advertises the negative of the same figure. */
    private final BooleanSupplier output;
    private Runnable changedHook = () -> {
    };

    public GTPortEssentiaStorage(int tankCount, int tankCapacity, BooleanSupplier singleCell,
                                 BooleanSupplier active, BooleanSupplier output) {
        int count = Math.max(1, tankCount);
        this.aspects = new Aspect[count];
        this.amounts = new int[count];
        this.tankCapacity = tankCapacity;
        this.singleCell = singleCell;
        this.active = active;
        this.output = output;
    }''', 1)

s = s.replace('''    /**
     * Suction is not modelled.
     *
     * <p>A port is a buffer inside a machine, not a tube: it neither pulls from nor competes with
     * the network, so it advertises the minimum and lets the machine decide when to ask.</p>
     */
    @Override
    public void setSuction(Aspect aspect, int amount) {
    }

    @Override
    public Aspect getSuctionType(Direction side) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction side) {
        return 0;
    }''',
'''    /** Suction is computed from the mode, so an external write has nothing to set. */
    @Override
    public void setSuction(Aspect aspect, int amount) {
    }

    /** The aspect this port asks for: the one already held, or whichever it has room for. */
    @Override
    public Aspect getSuctionType(Direction side) {
        return suctionAmount() == 0 ? null : getEssentiaType(side);
    }

    /**
     * How hard this port pulls, in the units a tube compares.
     *
     * <p>Zero while passive: the port is a buffer the machine feeds, and advertising suction would
     * make it fight the pipes for essentia it does not want. Once active it matches a reservoir —
     * except in output mode, where the sign flips because the port is a source, not a sink.</p>
     */
    @Override
    public int getSuctionAmount(Direction side) {
        return suctionAmount();
    }

    private int suctionAmount() {
        if (!this.active.getAsBoolean()) {
            return 0;
        }
        // A full input has no room, so it stops asking — the same rule the reservoir uses.
        if (!this.output.getAsBoolean() && totalAmount() >= totalCapacity()) {
            return 0;
        }
        return this.output.getAsBoolean() ? -RESERVOIR_SUCTION : RESERVOIR_SUCTION;
    }

    /** Room across every tank. */
    private int totalCapacity() {
        return this.aspects.length * this.tankCapacity;
    }''', 1)

s = s.replace('''    @Override
    public int getMinimumSuction() {
        return 0;
    }''',
'''    /**
     * The floor a tube must beat to connect.
     *
     * <p>Reported only while active, for the same reason as the face figure: a passive port is not
     * part of the suction competition at all.</p>
     */
    @Override
    public int getMinimumSuction() {
        if (!this.active.getAsBoolean()) {
            return 0;
        }
        return this.output.getAsBoolean() ? -RESERVOIR_SUCTION : RESERVOIR_SUCTION;
    }''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("essentia suction follows the mode")
