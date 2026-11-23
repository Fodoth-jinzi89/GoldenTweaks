"""Register shell_pp, the polypropylene casing.

A second shell, so a second recipe tier can build the same shapes. It reuses GTShellBlock:
the block has no behaviour of its own, only a texture, and the CTM definition in
optifine/ctm/shell_pp.properties is what makes it connect to its neighbours.
"""
import io

# ---------------- blocks and items ----------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTMultiblockBlocks.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    public static final DeferredBlock<GTShellBlock> SHELL_PE =
            BLOCKS.register("shell_pe", () -> new GTShellBlock(properties()));''',
'''    public static final DeferredBlock<GTShellBlock> SHELL_PE =
            BLOCKS.register("shell_pe", () -> new GTShellBlock(properties()));

    /** The polypropylene shell: the same part, one material tier up. */
    public static final DeferredBlock<GTShellBlock> SHELL_PP =
            BLOCKS.register("shell_pp", () -> new GTShellBlock(properties()));''', 1)

s = s.replace('''    public static final DeferredItem<BlockItem> SHELL_PE_ITEM =
            ITEMS.register("shell_pe", () -> new BlockItem(SHELL_PE.get(), new Item.Properties()));''',
'''    public static final DeferredItem<BlockItem> SHELL_PE_ITEM =
            ITEMS.register("shell_pe", () -> new BlockItem(SHELL_PE.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> SHELL_PP_ITEM =
            ITEMS.register("shell_pp", () -> new BlockItem(SHELL_PP.get(), new Item.Properties()));''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("shell_pp registered")

# ---------------- creative tab ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTMultiblockCreativeTabs.java"
s2 = io.open(p2, encoding="utf-8").read()
s2 = s2.replace('''                        output.accept(GTMultiblockBlocks.SHELL_PE_ITEM.get());''',
'''                        output.accept(GTMultiblockBlocks.SHELL_PE_ITEM.get());
                        output.accept(GTMultiblockBlocks.SHELL_PP_ITEM.get());''', 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("creative tab lists it")
