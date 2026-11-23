"""Ask the model for its data from the block, which is what makes the hook run.

NeoForge drives {@code BakedModel#getModelData} through {@code Block#getModelData}. A block that
does not override it contributes {@code ModelData.EMPTY} and the model's own hook is never asked —
so the casing never reached the renderer no matter what the model did with it.

Forwarding here costs nothing when there is no block entity (the model returns the data unchanged),
and is what puts the casing in front of the model.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlock.java"
s = io.open(p, encoding="utf-8").read()

anchor = '''    @Nullable
    @Override
    public BlockEntity newBlockEntity(net.minecraft.core.BlockPos pos, BlockState state) {
        return new GTPortBlockEntity(pos, state);
    }'''
assert anchor in s

addition = '''    @Nullable
    @Override
    public BlockEntity newBlockEntity(net.minecraft.core.BlockPos pos, BlockState state) {
        return new GTPortBlockEntity(pos, state);
    }

    /**
     * Asks the model which casing to draw.
     *
     * <p>NeoForge drives {@code BakedModel#getModelData} from here. A block that does not override
     * this contributes {@code ModelData.EMPTY}, and the model's own hook is never consulted — which
     * is why the casing never reached the renderer.</p>
     *
     * <p>Without a block entity there is nothing to say, and the model is left to answer for
     * itself.</p>
     */
    @Override
    public ModelData getModelData(BlockAndTintGetter level, net.minecraft.core.BlockPos pos,
                                  BlockState state, ModelData data) {
        return level.getBlockEntity(pos) instanceof GTPortBlockEntity port && port.casing() != null
                ? data.derive()
                        .with(GTPortCasingModel.CASING, BuiltInRegistries.BLOCK
                                .getOptional(port.casing())
                                .map(Block::defaultBlockState)
                                .orElse(null))
                        .build()
                : data;
    }'''

s = s.replace(anchor, addition, 1)

for imp in (
    "import net.fodoth.skina.goldentweaks.content.multiblock.client.GTPortCasingModel;",
    "import net.minecraft.core.registries.BuiltInRegistries;",
    "import net.minecraft.world.level.BlockAndTintGetter;",
    "import net.neoforged.neoforge.client.model.data.ModelData;",
):
    if imp not in s:
        s = s.replace("import net.minecraft.core.BlockPos;", imp + "\nimport net.minecraft.core.BlockPos;", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("block forwards the casing into model data")
