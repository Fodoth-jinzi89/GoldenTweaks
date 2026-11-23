"""Wire the two new kinds into the port entity.

Both storages live in compat packages that name optional mod types, so the entity must not
reference their classes directly: doing so would throw NoClassDefFoundError on a world without
Mekanism or Thaumcraft. They are held as `Object` and reached through a small bridge that is
only loaded when the mod is present.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

# ---------------- fields ----------------
s = s.replace('''    /** The tool and filter slots in the lower right, one item each. */''',
'''    /**
     * The chemical storage for a hopper, or {@code null} for any other kind.
     *
     * <p>Typed as {@code Object} on purpose: the concrete class names {@code ChemicalStack}, so a
     * field of that type would have to be resolved whenever this entity loads — and it loads on
     * every world, including ones without Mekanism. {@link GTPortStorages} narrows it back when the
     * mod is known to be there.</p>
     */
    @Nullable
    private Object chemicals;

    /** The essentia storage for a tank, or {@code null} for any other kind. Same reasoning. */
    @Nullable
    private Object essentia;

    /** The tool and filter slots in the lower right, one item each. */''', 1)

# ---------------- kind dispatch ----------------
s = s.replace('''            case ENERGY -> {
                // A dynamo hatch supplies and stores half what an energy hatch of the same tier
                // does; the block's own mode is what decides which of the two this port is.''',
'''            case CHEMICAL -> {
                this.items = null;
                this.fluids = null;
                this.energy = null;
                this.chemicals = GTPortStorages.createChemical(
                        portTier().chemicalTanks(), portTier().chemicalTankCapacity(),
                        this::isSingleCellMode, this::onContentChanged);
                this.essentia = null;
            }
            case ESSENTIA -> {
                this.items = null;
                this.fluids = null;
                this.energy = null;
                this.chemicals = null;
                this.essentia = GTPortStorages.createEssentia(
                        portTier().essentiaTanks(), portTier().essentiaTankCapacity(),
                        this::isSingleCellMode, this::onContentChanged);
            }
            case ENERGY -> {
                // A dynamo hatch supplies and stores half what an energy hatch of the same tier
                // does; the block's own mode is what decides which of the two this port is.''', 1)

# The other branches must clear the new fields too, or a rebuilt port keeps a stale one.
s = s.replace('''                this.items = replacement;
                this.fluids = null;
                this.energy = null;
            }''',
'''                this.items = replacement;
                this.fluids = null;
                this.energy = null;
                this.chemicals = null;
                this.essentia = null;
            }''', 1)

s = s.replace('''                this.fluids = replacement;
                this.items = null;
                this.energy = null;
            }''',
'''                this.fluids = replacement;
                this.items = null;
                this.energy = null;
                this.chemicals = null;
                this.essentia = null;
            }''', 1)

s = s.replace('''                this.energy = replacement;
                this.items = null;
                this.fluids = null;
            }''',
'''                this.energy = replacement;
                this.items = null;
                this.fluids = null;
                this.chemicals = null;
                this.essentia = null;
            }''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("entity fields and dispatch wired")
