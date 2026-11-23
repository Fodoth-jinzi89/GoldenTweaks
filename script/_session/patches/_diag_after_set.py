"""Print what the entry holds immediately after `set`, and again on a later render.

`atlas lookup` reports both sheets present, so the textures are there. `set` was still leaving the entry
unusable, which points at when it fills in rather than whether it can: a `SpriteShiftEntry` is probably
populated from an atlas event, so reading it in the same call that created it sees the empty state.

Printing the entry's fields right after `set`, and once more on a later render, distinguishes "never
fills" from "fills later".
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingBehaviour.java"
s = io.open(p, encoding="utf-8").read()

old = '''        CTSpriteShiftEntry entry = new CTSpriteShiftEntry(TYPE);
        entry.set(sheet, connected);

        if (isUsable(entry)) {'''
new = '''        CTSpriteShiftEntry entry = new CTSpriteShiftEntry(TYPE);
        entry.set(sheet, connected);

        if (PROBES.getAndIncrement() < 6) {
            GoldenTweaks.LOGGER.info("[GT/diag] after set: original={} target={} usable={}",
                    entry.getOriginal() == null ? "NULL" : entry.getOriginal().contents().name(),
                    entry.getTarget() == null ? "NULL" : entry.getTarget().contents().name(),
                    isUsable(entry));
        }

        if (isUsable(entry)) {'''
assert old in s
s = s.replace(old, new, 1)

s = s.replace('''    private static final Map<ResourceLocation, CTSpriteShiftEntry> SHIFTS = new ConcurrentHashMap<>();''',
'''    private static final Map<ResourceLocation, CTSpriteShiftEntry> SHIFTS = new ConcurrentHashMap<>();

    /** Throttles the probe that reports the entry's state right after `set`. */
    private static final java.util.concurrent.atomic.AtomicInteger PROBES =
            new java.util.concurrent.atomic.AtomicInteger();''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("post-set probe added")
