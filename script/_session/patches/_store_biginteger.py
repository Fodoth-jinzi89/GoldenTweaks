"""Give the energy store BigInteger capacity.

The API speaks int, but the store is where a MAX hatch's 344 G FE has to actually live. The
int methods saturate at the boundary; the store keeps the exact figure and saves it as a
string, since NBT has no bigger number type.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortEnergyStorage.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.energy.IEnergyStorage;''',
'''import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.math.BigInteger;''', 1)

s = s.replace('''    /**
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
    }''',
'''    /**
     * Held as {@code BigInteger} even though the API speaks {@code int}.
     *
     * <p>The upper tiers hold far more than an {@code int}, and then more than a {@code long}, so
     * neither width is a safe container: clamping on the way in would cap a MAX hatch at two
     * billion FE, and a long would only postpone the same problem by thirty tiers. The API
     * conversions saturate at the boundary; the store itself has no ceiling.</p>
     */
    private final BigInteger capacity;
    private final BigInteger transfer;
    private Runnable changedHook = () -> {
    };
    private BigInteger energy = BigInteger.ZERO;

    public GTPortEnergyStorage(BigInteger capacity, BigInteger transfer) {
        this.capacity = capacity;
        this.transfer = transfer;
    }''', 1)

s = s.replace('''        long accepted = Math.min(Math.min(toReceive, this.transfer), this.capacity - this.energy);
        if (accepted <= 0) {
            return 0;
        }
        if (!simulate) {
            this.energy += accepted;
            this.changedHook.run();
        }
        return (int) accepted;''',
'''        BigInteger room = this.capacity.subtract(this.energy);
        BigInteger accepted = BigInteger.valueOf(toReceive).min(this.transfer).min(room);
        if (accepted.signum() <= 0) {
            return 0;
        }
        if (!simulate) {
            this.energy = this.energy.add(accepted);
            this.changedHook.run();
        }
        return accepted.intValueExact();''', 1)

s = s.replace('''        long taken = Math.min(toExtract, this.energy);
        if (taken <= 0) {
            return 0;
        }
        if (!simulate) {
            this.energy -= taken;
            this.changedHook.run();
        }
        return (int) taken;''',
'''        BigInteger taken = BigInteger.valueOf(toExtract).min(this.energy);
        if (taken.signum() <= 0) {
            return 0;
        }
        if (!simulate) {
            this.energy = this.energy.subtract(taken);
            this.changedHook.run();
        }
        return taken.intValue();''', 1)

s = s.replace('''    @Override
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
    }''',
'''    @Override
    public int getEnergyStored() {
        return saturated(this.energy);
    }

    @Override
    public int getMaxEnergyStored() {
        return saturated(this.capacity);
    }

    /** The exact stored amount, for the port's own display. */
    public BigInteger storedExact() {
        return this.energy;
    }

    /** The exact capacity, for the port's own display. */
    public BigInteger capacityExact() {
        return this.capacity;
    }

    private static int saturated(BigInteger value) {
        return value.min(INT_MAX).max(BigInteger.ZERO).intValue();
    }

    private static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);''', 1)

s = s.replace('''    public void save(CompoundTag tag) {
        // Stored as a long: an int would cap a top-tier hatch's charge at two billion FE and
        // silently discard the rest every time the world was saved.
        tag.putLong(TAG_ENERGY, this.energy);
    }''',
'''    public void save(CompoundTag tag) {
        // Written as a string: NBT's largest integer is a long, and the whole point of this store
        // is that its contents can exceed one.
        tag.putString(TAG_ENERGY, this.energy.toString());
    }''', 1)

s = s.replace('''        // Read as a long, falling back to the int field older saves wrote.
        long stored = tag.contains(TAG_ENERGY) && tag.getTagType(TAG_ENERGY) == 4
                ? tag.getInt(TAG_ENERGY)
                : tag.getLong(TAG_ENERGY);
        this.energy = Math.max(0L, Math.min(this.capacity, stored));''',
'''        // Three shapes in the wild: the original int, the short-lived long, and the string this
        // writes now. Accepted in that order rather than assuming a fresh save.
        BigInteger stored;
        int type = tag.contains(TAG_ENERGY) ? tag.getTagType(TAG_ENERGY) : 0;
        if (type == 8) {
            try {
                stored = new BigInteger(tag.getString(TAG_ENERGY));
            } catch (NumberFormatException malformed) {
                stored = BigInteger.ZERO;
            }
        } else if (type == 3 || type == 4) {
            stored = BigInteger.valueOf(tag.getLong(TAG_ENERGY));
        } else {
            stored = BigInteger.ZERO;
        }
        this.energy = stored.max(BigInteger.ZERO).min(this.capacity);''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("energy store is BigInteger")
