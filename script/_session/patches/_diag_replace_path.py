"""Temporary diagnostics across the three steps of the replace path.

Each step can silently do nothing:
  1. the baking hook may not match the models;
  2. getModelData may not be called, or may find no casing;
  3. the plate may be identified by a sprite name that never matches.

Printing one line per step, throttled, tells which of the three it is without filling the log.
"""
import io

# ---- 1. the baking hook -----------------------------------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortModelWrapping.java"
s = io.open(p, encoding="utf-8").read()

old = '''        models.putAll(wrapped);
    }'''
new = '''        models.putAll(wrapped);

        net.fodoth.skina.goldentweaks.GoldenTweaks.LOGGER.info(
                "[GT/diag] baking hook: {} models total, {} wrapped as ports",
                models.size(), wrapped.size());
    }'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("hook diagnostic added")

# ---- 2/3. the model -----------------------------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingModel.java"
s2 = io.open(p2, encoding="utf-8").read()

s2 = s2.replace('''    public GTPortCasingModel(BakedModel original) {
        super(original);
    }''',
'''    /** Throttles the diagnostics: one report per step is enough to tell what is happening. */
    private static final java.util.concurrent.atomic.AtomicInteger DIAG = new java.util.concurrent.atomic.AtomicInteger();

    public GTPortCasingModel(BakedModel original) {
        super(original);
    }''', 1)

s2 = s2.replace('''        List<BakedQuad> quads = this.originalModel.getQuads(state, side, random, data, type);

        BlockState casing = data.get(CASING);''',
'''        List<BakedQuad> quads = this.originalModel.getQuads(state, side, random, data, type);

        BlockState casing = data.get(CASING);
        if (DIAG.getAndIncrement() < 6) {
            String firstSprite = quads.isEmpty() || quads.getFirst().getSprite() == null
                    ? "none"
                    : quads.getFirst().getSprite().contents().name().toString();
            net.fodoth.skina.goldentweaks.GoldenTweaks.LOGGER.info(
                    "[GT/diag] getQuads side={} state={} casing={} quads={} firstSprite={}",
                    side, state == null ? "null" : state.getBlock(), casing, quads.size(), firstSprite);
        }''', 1)

s2 = s2.replace('''        ModelData fromWrapped = this.originalModel.getModelData(level, pos, state, data);''',
'''        ModelData fromWrapped = this.originalModel.getModelData(level, pos, state, data);
        if (DIAG.getAndIncrement() < 6) {
            Object entity = level.getBlockEntity(pos);
            net.fodoth.skina.goldentweaks.GoldenTweaks.LOGGER.info(
                    "[GT/diag] getModelData state={} entity={} casing={}",
                    state.getBlock(), entity == null ? "null" : entity.getClass().getSimpleName(),
                    entity instanceof GTPortBlockEntity port ? port.casing() : "n/a");
        }''', 1)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("model diagnostics added")
