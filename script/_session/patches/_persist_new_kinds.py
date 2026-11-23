"""Save and load the two new storages.

Both classes expose save/load with a registries provider, so the bridge forwards to them the
same way it does for contents.
"""
import io

# ---------------- bridge ----------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortStorages.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    /** The contents of a chemical hopper as window rows. */''',
'''    /** Writes a chemical hopper's tanks, or an empty tag when there is nothing to write. */
    public static net.minecraft.nbt.CompoundTag saveChemical(
            @Nullable Object storage, net.minecraft.core.HolderLookup.Provider registries) {
        if (storage instanceof net.fodoth.skina.goldentweaks.compat.mekanism.GTPortChemicalStorage chemical) {
            return chemical.save(registries);
        }
        return new net.minecraft.nbt.CompoundTag();
    }

    public static void loadChemical(@Nullable Object storage, net.minecraft.nbt.CompoundTag tag,
                                    net.minecraft.core.HolderLookup.Provider registries) {
        if (storage instanceof net.fodoth.skina.goldentweaks.compat.mekanism.GTPortChemicalStorage chemical) {
            chemical.load(tag, registries);
        }
    }

    /** Writes an essentia tank's contents. */
    public static net.minecraft.nbt.CompoundTag saveEssentia(
            @Nullable Object storage, net.minecraft.core.HolderLookup.Provider registries) {
        if (storage instanceof net.fodoth.skina.goldentweaks.compat.thaumcraft.GTPortEssentiaStorage essentia) {
            return essentia.save(registries);
        }
        return new net.minecraft.nbt.CompoundTag();
    }

    public static void loadEssentia(@Nullable Object storage, net.minecraft.nbt.CompoundTag tag,
                                    net.minecraft.core.HolderLookup.Provider registries) {
        if (storage instanceof net.fodoth.skina.goldentweaks.compat.thaumcraft.GTPortEssentiaStorage essentia) {
            essentia.load(tag, registries);
        }
    }

    /** The contents of a chemical hopper as window rows. */''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("bridge saves and loads")

# ---------------- entity ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()

s2 = s2.replace('''        } else if (this.energy instanceof GTPortEnergyStorage storage) {
            CompoundTag content = new CompoundTag();
            storage.save(content);
            tag.put(TAG_CONTENT, content);
        }''',
'''        } else if (this.energy instanceof GTPortEnergyStorage storage) {
            CompoundTag content = new CompoundTag();
            storage.save(content);
            tag.put(TAG_CONTENT, content);
        } else if (this.chemicals != null) {
            tag.put(TAG_CONTENT, GTPortStorages.saveChemical(this.chemicals, registries));
        } else if (this.essentia != null) {
            tag.put(TAG_CONTENT, GTPortStorages.saveEssentia(this.essentia, registries));
        }''', 1)

s2 = s2.replace('''        } else if (this.energy instanceof GTPortEnergyStorage storage) {
            storage.load(content);
        }''',
'''        } else if (this.energy instanceof GTPortEnergyStorage storage) {
            storage.load(content);
        } else if (this.chemicals != null) {
            GTPortStorages.loadChemical(this.chemicals, content, registries);
        } else if (this.essentia != null) {
            GTPortStorages.loadEssentia(this.essentia, content, registries);
        }''', 1)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("entity persists the new storages")
