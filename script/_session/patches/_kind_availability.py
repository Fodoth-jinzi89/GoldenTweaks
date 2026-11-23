"""Register the two new port families only when their mod is present.

The loop already walks `GTPortKind.values()`, so the new kinds would be registered
unconditionally — and a chemical hopper on a world without Mekanism would be a block whose
storage is `null`, which is worse than not shipping the block at all.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTMultiblockBlocks.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    private static void declarePorts() {
        for (GTPortKind kind : GTPortKind.values()) {''',
'''    private static void declarePorts() {
        for (GTPortKind kind : GTPortKind.values()) {
            if (!kindAvailable(kind)) {
                continue;
            }''', 1)

s = s.replace('''    /**
     * Projects a multiblock layout in the world so the player can see where the parts go.''',
'''    /**
     * Whether a port kind's backing mod is installed.
     *
     * <p>The two extended kinds hold Mekanism chemicals and Thaumcraft essentia, so registering
     * them without their mod would put a block in the game whose storage is always empty — and
     * whose compat class cannot even be loaded. Skipped rather than shipped broken.</p>
     */
    private static boolean kindAvailable(GTPortKind kind) {
        return switch (kind) {
            case CHEMICAL -> ModList.get().isLoaded("mekanism");
            case ESSENTIA -> ModList.get().isLoaded("thaumcraft");
            default -> true;
        };
    }

    /**
     * Projects a multiblock layout in the world so the player can see where the parts go.''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("port registration respects the mod checks")
