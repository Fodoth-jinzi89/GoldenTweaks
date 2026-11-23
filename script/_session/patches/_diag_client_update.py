"""Trace where the casing hand-off stops, on the client.

Three things have to happen for the plate to change without a reload:

  1. the server writes the casing and ships it;
  2. the client's entity receives it;
  3. the section is rebuilt, and the model is asked for its quads again.

Previous diagnostics covered 1. These cover 2 and 3, and print the world position so a stall can be
matched to the block being looked at.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

# where the field is assigned: both the setter and the load path
s = s.replace('''        this.casing = casing;
        setChanged();''',
'''        this.casing = casing;
        if (level != null && level.isClientSide && DIAG.getAndIncrement() < 10) {
            net.fodoth.skina.goldentweaks.GoldenTweaks.LOGGER.info(
                    "[GT/diag] client setCasing at {} -> {}", this.worldPosition, casing);
        }
        setChanged();''', 1)

s = s.replace('''        this.casing = tag.contains(TAG_CASING)
                ? ResourceLocation.tryParse(tag.getString(TAG_CASING))
                : null;''',
'''        this.casing = tag.contains(TAG_CASING)
                ? ResourceLocation.tryParse(tag.getString(TAG_CASING))
                : null;
        if (this.level != null && this.level.isClientSide && DIAG.getAndIncrement() < 10) {
            net.fodoth.skina.goldentweaks.GoldenTweaks.LOGGER.info(
                    "[GT/diag] client load at {} -> {}", this.worldPosition, this.casing);
        }''', 1)

s = s.replace('''    private static final String TAG_CASING = "Casing";''',
'''    private static final String TAG_CASING = "Casing";

    /** Throttles the diagnostics while the update path is being traced. */
    private static final java.util.concurrent.atomic.AtomicInteger DIAG =
            new java.util.concurrent.atomic.AtomicInteger();''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("client-side diagnostics added")
