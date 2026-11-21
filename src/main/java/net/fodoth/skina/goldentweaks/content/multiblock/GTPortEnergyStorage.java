package net.fodoth.skina.goldentweaks.content.multiblock;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.energy.IEnergyStorage;

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

    private final int capacity;
    private final int transfer;
    private Runnable changedHook = () -> {
    };
    private int energy;

    public GTPortEnergyStorage(int capacity, int transfer) {
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
        int accepted = Math.min(Math.min(toReceive, this.transfer), this.capacity - this.energy);
        if (accepted <= 0) {
            return 0;
        }
        if (!simulate) {
            this.energy += accepted;
            this.changedHook.run();
        }
        return accepted;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (toExtract <= 0) {
            return 0;
        }
        int taken = Math.min(toExtract, this.energy);
        if (taken <= 0) {
            return 0;
        }
        if (!simulate) {
            this.energy -= taken;
            this.changedHook.run();
        }
        return taken;
    }

    @Override
    public int getEnergyStored() {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return this.capacity;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    public void save(CompoundTag tag) {
        tag.putInt(TAG_ENERGY, this.energy);
    }

    public void load(CompoundTag tag) {
        // Clamped rather than trusted: the tier can shrink between sessions, and a stale value
        // larger than the new capacity would let the gauge show more than it can hold.
        this.energy = Math.max(0, Math.min(this.capacity, tag.getInt(TAG_ENERGY)));
    }
}
