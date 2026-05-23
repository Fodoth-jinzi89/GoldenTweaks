package net.fodoth.skina.goldentweaks.compat.alshanex_familiars;

import net.alshanex.familiarslib.util.consumables.FamiliarConsumableSystem;
import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;
import java.util.Map;

public class GoldenTweaksConsumableData {

    private final EnumMap<
            FamiliarConsumableSystem.ConsumableType,
            Double
            > values =
            new EnumMap<>(
                    FamiliarConsumableSystem.ConsumableType.class
            );

    public GoldenTweaksConsumableData() {

        this.clear();
    }

    public GoldenTweaksConsumableData(
            CompoundTag tag
    ) {

        this();

        this.loadFromNBT(tag);
    }

    public double getValue(
            FamiliarConsumableSystem.ConsumableType type
    ) {

        return this.values.getOrDefault(
                type,
                0D
        );
    }

    public void setValue(
            FamiliarConsumableSystem.ConsumableType type,
            double value
    ) {

        if (Double.isNaN(value)
                || Double.isInfinite(value)) {

            value = 0D;
        }

        this.values.put(
                type,
                value
        );
    }

    public void addValue(
            FamiliarConsumableSystem.ConsumableType type,
            double value
    ) {

        this.setValue(
                type,
                this.getValue(type) + value
        );
    }

    public boolean hasAnyData() {

        for (double value : this.values.values()) {

            if (value != 0D) {
                return true;
            }
        }

        return false;
    }

    public void clear() {

        for (FamiliarConsumableSystem.ConsumableType type :
                FamiliarConsumableSystem.ConsumableType.values()) {

            this.values.put(type, 0D);
        }
    }

    public CompoundTag saveToNBT(
            CompoundTag tag
    ) {

        for (Map.Entry<
                FamiliarConsumableSystem.ConsumableType,
                Double
                > entry : this.values.entrySet()) {

            tag.putDouble(
                    entry.getKey().getSerializedName(),
                    entry.getValue()
            );
        }

        return tag;
    }

    public void loadFromNBT(
            CompoundTag tag
    ) {

        this.clear();

        for (FamiliarConsumableSystem.ConsumableType type :
                FamiliarConsumableSystem.ConsumableType.values()) {

            String key =
                    type.getSerializedName();

            if (!tag.contains(key)) {
                continue;
            }

            double value =
                    tag.getDouble(key);

            if (Double.isNaN(value)
                    || Double.isInfinite(value)) {

                value = 0D;
            }

            this.values.put(
                    type,
                    value
            );
        }
    }

    public CompoundTag serializeNBT() {

        return this.saveToNBT(
                new CompoundTag()
        );
    }

    public static GoldenTweaksConsumableData fromNBT(
            CompoundTag tag
    ) {

        return new GoldenTweaksConsumableData(tag);
    }

    @Override
    public String toString() {

        StringBuilder builder =
                new StringBuilder(
                        "GoldenTweaksConsumableData{"
                );

        boolean first = true;

        for (Map.Entry<
                FamiliarConsumableSystem.ConsumableType,
                Double
                > entry : this.values.entrySet()) {

            if (!first) {
                builder.append(", ");
            }

            builder.append(entry.getKey().name())
                    .append("=")
                    .append(entry.getValue());

            first = false;
        }

        builder.append("}");

        return builder.toString();
    }

    public double getArmor() {

        return this.getValue(
                FamiliarConsumableSystem.ConsumableType.ARMOR
        );
    }

    public void setArmor(
            double value
    ) {

        this.setValue(
                FamiliarConsumableSystem.ConsumableType.ARMOR,
                value
        );
    }

    public double getHealth() {

        return this.getValue(
                FamiliarConsumableSystem.ConsumableType.HEALTH
        );
    }

    public void setHealth(
            double value
    ) {

        this.setValue(
                FamiliarConsumableSystem.ConsumableType.HEALTH,
                value
        );
    }

    public double getSpellPower() {

        return this.getValue(
                FamiliarConsumableSystem.ConsumableType.SPELL_POWER
        );
    }

    public void setSpellPower(
            double value
    ) {

        this.setValue(
                FamiliarConsumableSystem.ConsumableType.SPELL_POWER,
                value
        );
    }

    public double getSpellResist() {

        return this.getValue(
                FamiliarConsumableSystem.ConsumableType.SPELL_RESIST
        );
    }

    public void setSpellResist(
            double value
    ) {

        this.setValue(
                FamiliarConsumableSystem.ConsumableType.SPELL_RESIST,
                value
        );
    }

    public double getEnraged() {

        return this.getValue(
                FamiliarConsumableSystem.ConsumableType.ENRAGED
        );
    }

    public void setEnraged(
            double value
    ) {

        this.setValue(
                FamiliarConsumableSystem.ConsumableType.ENRAGED,
                value
        );
    }

    public double getBlocking() {

        return this.getValue(
                FamiliarConsumableSystem.ConsumableType.BLOCKING
        );
    }

    public void setBlocking(
            double value
    ) {

        this.setValue(
                FamiliarConsumableSystem.ConsumableType.BLOCKING,
                value
        );
    }
}