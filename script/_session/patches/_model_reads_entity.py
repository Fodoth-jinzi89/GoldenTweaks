"""The re-skinning model reads the casing from the port's block entity."""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingModel.java"
s = io.open(p, encoding="utf-8").read()

# ---- the model data now carries the resolved casing block state ------------------------------
s = s.replace('''    /** The casing a port is set into, as a block state. Written by the machine when it forms. */
    public static final ModelProperty<BlockState> CASING = new ModelProperty<>();''',
'''    /**
     * The casing a port is set into.
     *
     * <p>Resolved once per position from the block entity and carried through {@link ModelData}:
     * the state itself cannot hold an open-ended value, and re-reading the entity for every quad
     * would be a lookup per face per frame.</p>
     */
    public static final ModelProperty<BlockState> CASING = new ModelProperty<>();''', 1)

# ---- the fill reads the entity ----------------------------------------------------------------
old_fill = '''    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                  ModelData data) {
        if (!(state.getBlock() instanceof GTPortBlock) || data.has(CASING)) {
            return data;
        }
        return data.derive()
                .with(CASING, GTMultiblockBlocks.casingFor(state))
                .build();
    }'''
new_fill = '''    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                  ModelData data) {
        if (!(state.getBlock() instanceof GTPortBlock) || data.has(CASING)) {
            return data;
        }
        if (!(level.getBlockEntity(pos) instanceof GTPortBlockEntity port)
                || port.casing() == null) {
            return data;
        }
        BlockState casing = BuiltInRegistries.BLOCK.getOptional(port.casing())
                .map(Block::defaultBlockState)
                .orElse(null);
        if (casing == null) {
            // The id names a block this pack does not have; the authored plate is the safe answer.
            return data;
        }
        return data.derive().with(CASING, casing).build();
    }'''
assert old_fill in s
s = s.replace(old_fill, new_fill, 1)

# ---- the missing overload ---------------------------------------------------------------------
s = s.replace('''    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        return this.original.getParticleIcon(data);
    }''',
'''    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.original.getParticleIcon();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        return this.original.getParticleIcon(data);
    }''', 1)

for imp in (
    "import net.fodoth.skina.goldentweaks.content.multiblock.GTPortBlockEntity;",
    "import net.minecraft.core.registries.BuiltInRegistries;",
    "import net.minecraft.world.level.block.Block;",
):
    if imp not in s:
        s = s.replace("import net.minecraft.core.BlockPos;", imp + "\nimport net.minecraft.core.BlockPos;", 1)

# the helper it used is gone
s = s.replace("import net.fodoth.skina.goldentweaks.content.multiblock.GTMultiblockBlocks;\n", "", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("model reads the casing from the entity")

# ---- GTMultiblockBlocks: the resolver is no longer used ---------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTMultiblockBlocks.java"
s2 = io.open(p2, encoding="utf-8").read()
start = s2.find("    /**\n     * The casing block state a port is set into")
if start >= 0:
    end = s2.index("    }\n\n", s2.index("orElse(null));", start)) + len("    }\n\n")
    s2 = s2[:start] + s2[end:]
    io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
    print("unused resolver removed")
else:
    print("resolver already gone")
