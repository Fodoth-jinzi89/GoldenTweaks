package net.fodoth.skina.goldentweaks.content.multiblock;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.math.BigInteger;

/**
 * Energy store behind an energy port.
 *
 * <p>The two limits are deliberately not symmetric. {@link #receiveEnergy} caps at the port's
 * transfer rate, so an external cable feeds the port at the same rate a native AMI port would.
 * {@link #extractEnergy} has no rate cap at all, and that asymmetry is the whole point: the
 * multiblock draws a whole tick's energy cost in one call and treats anything short of the full
 * amount as "no power", switching itself off. A symmetric cap of {@code transfer} (which is 100 FE
 * for a tier I port) made any recipe costing more than that look unpowered even with a full port.</p>
 *
 * <p>The rate cap on insertion is kept because it is what the port's capacity table is calibrated
 * against — a port is meant to throttle how fast energy arrives, not how fast its owner consumes
 * what has already arrived.</p>
 */
public class GTPortEnergyStorage implements IEnergyStorage {

    private static final String TAG_ENERGY = "Energy";

    /**
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
    }

    public void setChangedHook(Runnable hook) {
        this.changedHook = hook;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        if (toReceive <= 0) {
            return 0;
        }
        BigInteger room = this.capacity.subtract(this.energy);
        BigInteger accepted = BigInteger.valueOf(toReceive).min(this.transfer).min(room);
        if (accepted.signum() <= 0) {
            return 0;
        }
        if (!simulate) {
            this.energy = this.energy.add(accepted);
            this.changedHook.run();
        }
        return accepted.intValueExact();
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (toExtract <= 0) {
            return 0;
        }
        BigInteger taken = BigInteger.valueOf(toExtract).min(this.energy);
        if (taken.signum() <= 0) {
            return 0;
        }
        if (!simulate) {
            this.energy = this.energy.subtract(taken);
            this.changedHook.run();
        }
        return taken.intValue();
    }

    @Override
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

    private static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    public void save(CompoundTag tag) {
        // Written as a string: NBT's largest integer is a long, and the whole point of this store
        // is that its contents can exceed one.
        tag.putString(TAG_ENERGY, this.energy.toString());
    }

    public void load(CompoundTag tag) {
        // Clamped rather than trusted: the tier can shrink between sessions, and a stale value
        // larger than the new capacity would let the gauge show more than it can hold.
        // Three shapes in the wild: the original int, the short-lived long, and the string this
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
        this.energy = stored.max(BigInteger.ZERO).min(this.capacity);
    }
}
