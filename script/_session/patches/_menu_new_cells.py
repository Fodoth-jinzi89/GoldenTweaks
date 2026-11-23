"""Teach the window how many cells a hopper or an essentia tank reports."""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortMenu.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''            case ENERGY -> 0;
        };
    }

    @Override
    public boolean stillValid(Player player) {''',
'''            case ENERGY -> 0;
            case CHEMICAL -> this.port == null ? 0 : this.port.chemicalTankCount();
            case ESSENTIA -> this.port == null ? 0 : this.port.essentiaTankCount();
        };
    }

    @Override
    public boolean stillValid(Player player) {''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("menu knows the new cell counts")

# ---------------- the entity exposes those counts ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()
s2 = s2.replace('''    /** How much one slot of this port holds, which is what an entry's cell span is measured in. */''',
'''    /** Tanks a chemical hopper reports, or zero for the other kinds. */
    public int chemicalTankCount() {
        return kind() == GTPortKind.CHEMICAL ? portTier().chemicalTanks() : 0;
    }

    /** Tanks an essentia port reports, or zero for the other kinds. */
    public int essentiaTankCount() {
        return kind() == GTPortKind.ESSENTIA ? portTier().essentiaTanks() : 0;
    }

    /** The chemical handler, or {@code null}. Narrowed by the caller through GTPortStorages. */
    @Nullable
    public Object chemicalStorage() {
        ensureHandler();
        return this.chemicals;
    }

    /** The essentia container, or {@code null}. Narrowed by the caller through GTPortStorages. */
    @Nullable
    public Object essentiaStorage() {
        ensureHandler();
        return this.essentia;
    }

    /** How much one slot of this port holds, which is what an entry's cell span is measured in. */''', 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("entity exposes the storage accessors")
