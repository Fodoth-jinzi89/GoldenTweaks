"""The casing reaches the model through the block entity, not the block.

`Block` has no `getModelData` in 1.21.1 — the hook is `BlockEntity#getModelData()`, which NeoForge
calls while assembling a block's render data. My version put it on the block, where it overrode
nothing and never ran: the override compiled and the casing never reached the renderer.

The property stays on the block, since that is the key both sides must be able to name.
"""
import io, os

# ---- 1. drop the probe classes ----------------------------------------------------------------
for probe in ("ProbeModelData.java", "ProbeModelDataEntity.java"):
    path = os.path.join(r"src/main/java/net/fodoth/skina/goldentweaks/debug", probe)
    if os.path.exists(path):
        os.remove(path)
print("probe classes removed")

# ---- 2. the entity answers ---------------------------------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

anchor = '''    /** The casing this port is set into, or {@code null} when it is not part of a machine. */
    @Nullable
    public ResourceLocation casing() {
        return this.casing;
    }'''
assert anchor in s

addition = anchor + '''

    /**
     * The casing, handed to the renderer.
     *
     * <p>This is the hook NeoForge calls while assembling a block's render data. On the block there
     * is no such method at all, so an override there compiles and never runs — which is why the
     * casing never reached the model.</p>
     */
    @Override
    public ModelData getModelData() {
        if (this.casing == null) {
            return ModelData.EMPTY;
        }
        return BuiltInRegistries.BLOCK.getOptional(this.casing)
                .map(Block::defaultBlockState)
                .map(casingState -> ModelData.EMPTY.derive()
                        .with(GTPortBlock.CASING_MODEL, casingState)
                        .build())
                .orElse(ModelData.EMPTY);
    }'''
s = s.replace(anchor, addition, 1)

for imp in (
    "import net.minecraft.core.registries.BuiltInRegistries;",
    "import net.minecraft.world.level.block.Block;",
    "import net.neoforged.neoforge.client.model.data.ModelData;",
):
    if imp not in s:
        s = s.replace("import net.minecraft.nbt.CompoundTag;",
                      imp + "\nimport net.minecraft.nbt.CompoundTag;", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("block entity answers the casing")
