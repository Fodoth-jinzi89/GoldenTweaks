"""Finish the wiring: imports, persistence signatures, and the model's remaining overrides."""
import io, re

# ---- 1. port entity: import + persistence ------------------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

if "import net.minecraft.resources.ResourceLocation;" not in s:
    s = s.replace("import net.minecraft.nbt.CompoundTag;",
                  "import net.minecraft.nbt.CompoundTag;\nimport net.minecraft.resources.ResourceLocation;", 1)

s = s.replace('''        super.saveAdditional(tag, registries);''',
'''        super.saveAdditional(tag, registries);
        if (this.casing != null) {
            tag.putString(TAG_CASING, this.casing.toString());
        }''', 1)

s = s.replace('''        super.loadAdditional(tag, registries);''',
'''        super.loadAdditional(tag, registries);
        this.casing = tag.contains(TAG_CASING)
                ? ResourceLocation.tryParse(tag.getString(TAG_CASING))
                : null;''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("port entity persistence wired")

# ---- 2. advanced entity: the hook is gone ------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTAdvancedChemicalReactorBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()

old = '''    /**
     * The wall this machine is built from is the advanced one, so its ports show that plate.
     *
     * <p>Derived from {@link #casingBlock()} rather than declared again: two statements of the same
     * fact are two chances to disagree.</p>
     */
    @Override
    protected boolean usesAdvancedCasing() {
        return casingBlock() == GTMultiblockBlocks.SHELL_PP.get();
    }

'''
s2 = s2.replace(old, "", 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("stale override removed")

# ---- 3. the model: the three-argument getQuads ------------------------------------------------
p3 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingModel.java"
s3 = io.open(p3, encoding="utf-8").read()

old3 = '''    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    RandomSource random, ModelData data, @Nullable RenderType type) {'''
new3 = '''    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    RandomSource random) {
        // The plain signature has no model data, so there is no casing to apply: an inventory or
        // in-hand render lands here and keeps the authored plate.
        return this.original.getQuads(state, side, random);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    RandomSource random, ModelData data, @Nullable RenderType type) {'''
assert old3 in s3
s3 = s3.replace(old3, new3, 1)
io.open(p3, "w", encoding="utf-8", newline="\n").write(s3)
print("plain getQuads forwarded")
