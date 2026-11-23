"""The port's model data, defined where both sides can reach it.

`GTPortCasingModel` lives in a client-only package, and a block class is loaded on the server too.
Naming the model from the block would make the block drag a client model class into a dedicated
server's class space.

The property is therefore declared on the block itself, and the model reads it from there. One
declaration, no cross-package dependency, and no client type in a class the server loads.
"""
import io

# ---- 1. the property moves onto the block ------------------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlock.java"
s = io.open(p, encoding="utf-8").read()

anchor = '''    public static final DirectionProperty FACING = BlockStateProperties.FACING;'''
assert anchor in s

prop = '''    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    /**
     * The casing this port is set into, filled in for the renderer.
     *
     * <p>Declared here rather than next to the model because the block is loaded on both sides and
     * the model is client-only: a server must be able to name this property without pulling in a
     * client class.</p>
     */
    public static final ModelProperty<BlockState> CASING_MODEL = new ModelProperty<>();'''

s = s.replace(anchor, prop, 1)

old_hook = '''    /**
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
new_hook = '''    /**
     * Tells the renderer which casing to draw.
     *
     * <p>NeoForge drives {@code BakedModel#getModelData} from here. A block that does not override
     * this contributes {@code ModelData.EMPTY} and the model's hook is never consulted — which is
     * why the casing never reached the renderer before.</p>
     *
     * <p>Without a block entity, or before a machine has claimed the port, there is nothing to say
     * and the model draws its own plate.</p>
     */
    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos,
                                  BlockState state, ModelData data) {
        if (!(level.getBlockEntity(pos) instanceof GTPortBlockEntity port)
                || port.casing() == null) {
            return data;
        }
        return BuiltInRegistries.BLOCK.getOptional(port.casing())
                .map(Block::defaultBlockState)
                .map(casing -> data.derive().with(CASING_MODEL, casing).build())
                .orElse(data);
    }'''
assert old_hook in s
s = s.replace(old_hook, new_hook, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("casing property lives on the block")

# ---- 2. the model reads the block's property ---------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingModel.java"
s2 = io.open(p2, encoding="utf-8").read()

old_prop = '''    /**
     * The casing a port is set into.
     *
     * <p>Resolved once per position from the block entity and carried through {@link ModelData}:
     * the state cannot hold an open-ended value, and re-reading the entity for every quad would be
     * a lookup per face per frame.</p>
     */
    public static final ModelProperty<BlockState> CASING = new ModelProperty<>();'''
new_prop = '''    /**
     * The casing a port is set into.
     *
     * <p>Declared on {@link GTPortBlock} because the block fills it in, and the block is loaded on
     * a dedicated server where this class is not. This is only a convenient alias.</p>
     */
    public static final ModelProperty<BlockState> CASING = GTPortBlock.CASING_MODEL;'''
assert old_prop in s2
s2 = s2.replace(old_prop, new_prop, 1)

# the model no longer has to resolve it: the block already did
old_fill = s2[s2.index("    /**\n     * Fills in the casing for a placed port."):s2.rindex("}")]
new_fill = '''    /**
     * Nothing to add.
     *
     * <p>The block fills {@link #CASING} in through {@code Block#getModelData}, which NeoForge calls
     * before this one. Repeating the lookup here would be a second registry resolution for the same
     * answer.</p>
     */
'''
s2 = s2[:s2.index("    /**\n     * Fills in the casing for a placed port.")] + new_fill + "}\n"

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("model reads the block's property")
