"""Pass the port's active and direction switches into the essentia storage."""
import io

# ---------------- bridge ----------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortStorages.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    /** The essentia container for a tank, or {@code null} without Thaumcraft. */
    @Nullable
    public static Object createEssentia(int tanks, int tankCapacity, BooleanSupplier singleCell,
                                        Runnable changedHook) {
        if (!ModList.get().isLoaded(THAUMCRAFT)) {
            return null;
        }
        return ThaumcraftBridge.create(tanks, tankCapacity, singleCell, changedHook);
    }''',
'''    /**
     * The essentia container for a tank, or {@code null} without Thaumcraft.
     *
     * <p>The two extra suppliers are the port's reach-through switch and its direction: suction is
     * only advertised while active, and inverted for an output. Passed as suppliers rather than
     * values so a toggle applies on the very next tick.</p>
     */
    @Nullable
    public static Object createEssentia(int tanks, int tankCapacity, BooleanSupplier singleCell,
                                        BooleanSupplier active, BooleanSupplier output,
                                        Runnable changedHook) {
        if (!ModList.get().isLoaded(THAUMCRAFT)) {
            return null;
        }
        return ThaumcraftBridge.create(tanks, tankCapacity, singleCell, active, output, changedHook);
    }''', 1)

s = s.replace('''    private static final class ThaumcraftBridge {
        private static Object create(int tanks, int tankCapacity, BooleanSupplier singleCell,
                                     Runnable changedHook) {
            var storage = new net.fodoth.skina.goldentweaks.compat.thaumcraft.GTPortEssentiaStorage(
                    tanks, tankCapacity, singleCell);
            storage.setChangedHook(changedHook);
            return storage;
        }
    }''',
'''    private static final class ThaumcraftBridge {
        private static Object create(int tanks, int tankCapacity, BooleanSupplier singleCell,
                                     BooleanSupplier active, BooleanSupplier output,
                                     Runnable changedHook) {
            var storage = new net.fodoth.skina.goldentweaks.compat.thaumcraft.GTPortEssentiaStorage(
                    tanks, tankCapacity, singleCell, active, output);
            storage.setChangedHook(changedHook);
            return storage;
        }
    }''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("bridge forwards the switches")

# ---------------- the entity's call ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()
s2 = s2.replace('''                this.essentia = GTPortStorages.createEssentia(
                        portTier().essentiaTanks(), portTier().essentiaTankCapacity(),
                        this::isSingleCellMode, this::onContentChanged);''',
'''                this.essentia = GTPortStorages.createEssentia(
                        portTier().essentiaTanks(), portTier().essentiaTankCapacity(),
                        this::isSingleCellMode, this::isActive,
                        () -> mode() == PortMode.OUTPUT, this::onContentChanged);''', 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("entity passes active and output")
