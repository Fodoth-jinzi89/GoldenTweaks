"""Keep the energy store's arithmetic in long.

Its fields stay `int` because the API demands it, but `capacity - energy` overflows once a
top-tier port holds more than Integer.MAX_VALUE, and the subtraction then wraps negative —
which reads as "no room at all". Widening the fields costs nothing and makes the clamp
meaningless instead of harmful.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortEnergyStorage.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    private final int capacity;
    private final int transfer;
    private Runnable changedHook = () -> {
    };
    private int energy;

    public GTPortEnergyStorage(int capacity, int transfer) {
        this.capacity = capacity;
        this.transfer = transfer;
    }''',
'''    /**
     * Held as {@code long} even though the API speaks {@code int}.
     *
     * <p>The top tiers hold more than an {@code int} can express, and the storage has to keep the
     * real figure: clamping it on the way in would cap a MAX hatch at two billion FE. The API
     * conversions saturate at the boundary; the store itself does not.</p>
     */
    private final long capacity;
    private final long transfer;
    private Runnable changedHook = () -> {
    };
    private long energy;

    public GTPortEnergyStorage(long capacity, long transfer) {
        this.capacity = capacity;
        this.transfer = transfer;
    }''', 1)

s = s.replace('''        int accepted = Math.min(Math.min(toReceive, this.transfer), this.capacity - this.energy);
        if (accepted <= 0) {
            return 0;
        }
        if (!simulate) {
            this.energy += accepted;
            this.changedHook.run();
        }
        return accepted;''',
'''        long accepted = Math.min(Math.min(toReceive, this.transfer), this.capacity - this.energy);
        if (accepted <= 0) {
            return 0;
        }
        if (!simulate) {
            this.energy += accepted;
            this.changedHook.run();
        }
        return (int) accepted;''', 1)

s = s.replace('''        int taken = Math.min(toExtract, this.energy);
        if (taken <= 0) {
            return 0;
        }
        if (!simulate) {
            this.energy -= taken;
            this.changedHook.run();
        }
        return taken;''',
'''        long taken = Math.min(toExtract, this.energy);
        if (taken <= 0) {
            return 0;
        }
        if (!simulate) {
            this.energy -= taken;
            this.changedHook.run();
        }
        return (int) taken;''', 1)

s = s.replace('''    @Override
    public int getEnergyStored() {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return this.capacity;
    }''',
'''    @Override
    public int getEnergyStored() {
        return saturated(this.energy);
    }

    @Override
    public int getMaxEnergyStored() {
        return saturated(this.capacity);
    }

    /** The true stored amount, for the port's own display. */
    public long storedLong() {
        return this.energy;
    }

    /** The true capacity, for the port's own display. */
    public long capacityLong() {
        return this.capacity;
    }

    private static int saturated(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.max(0L, value);
    }''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("energy store widened")
