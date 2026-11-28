package net.fodoth.skina.goldentweaks.content.multiblock.common;

import net.neoforged.neoforge.energy.EnergyStorage;

/**
 * Works around {@code avaritia_more_items}' energy buffer losing charge across a reload.
 *
 * <p>AMI restores a port's energy with {@code receiveEnergy(stored, false)}. That call honours
 * {@code maxReceive}, which its own constructor sets to the same value as the capacity — so for any
 * buffer whose stored energy exceeds the per-tick intake, the excess is silently dropped and the
 * port appears to have reset. The block NBT still holds the right number; only the restore is
 * lossy.</p>
 *
 * <p>The value is therefore written straight into the buffer's charge field. It is read-only from
 * the outside, so this goes through a cached reflective handle, with a fallback that keeps the old
 * behaviour if the field ever disappears rather than failing the load.</p>
 */
public final class GTEnergyBufferFix {

    /** Not our field, so it is looked up lazily and once. Null means reflection is unavailable. */
    private static volatile java.lang.reflect.Field energyField;
    private static volatile boolean lookedUp;

    private GTEnergyBufferFix() {
    }

    /**
     * Sets the buffer's charge directly, clamped to its capacity.
     *
     * @return true when the value was written; false when the caller should fall back
     */
    public static boolean restore(EnergyStorage storage, int amount) {
        int target = Math.max(0, Math.min(amount, storage.getMaxEnergyStored()));
        java.lang.reflect.Field field = energyField();
        if (field == null) {
            return false;
        }
        try {
            // Start from whatever is there and move it, so the buffer's own bookkeeping stays
            // consistent even if it is asked to restore over an already half filled buffer.
            field.setInt(storage, target);
            return true;
        } catch (ReflectiveOperationException | RuntimeException error) {
            return false;
        }
    }

    private static java.lang.reflect.Field energyField() {
        if (!lookedUp) {
            synchronized (GTEnergyBufferFix.class) {
                if (!lookedUp) {
                    try {
                        java.lang.reflect.Field field = EnergyStorage.class.getDeclaredField("energy");
                        field.setAccessible(true);
                        energyField = field;
                    } catch (ReflectiveOperationException | RuntimeException error) {
                        energyField = null;
                    }
                    lookedUp = true;
                }
            }
        }
        return energyField;
    }
}
