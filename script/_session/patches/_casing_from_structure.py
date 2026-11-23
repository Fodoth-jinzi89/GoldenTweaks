"""Derive the projection's casing from the structure file instead of a per-machine table.

`casingItem` hard-coded "advanced = polypropylene, everything else = polyethylene", which has to
be touched again for every new machine and cannot express a machine that accepts several casings
at once. The structure file already says what each cell is: the casing symbol maps to a tag, and
`GTMultiblockBuilder` already resolves "the block that tag means, ports excluded" for the build
pass. The projection now asks the same question, so there is one answer instead of two.

Nothing is left to keep in sync: a machine whose casing tag lists two acceptable blocks, or a
machine built from a tag shared with another, works without any code change here.
"""
import io

# ---- 1. builder: make the casing resolver public and reachable ---------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTMultiblockBuilder.java"
s = io.open(p, encoding="utf-8").read()

old = '''    private static Block casingBlock(GTMultiblockPattern layout) {'''
new = '''    public static Block casingBlock(GTMultiblockPattern layout) {'''
assert old in s
s = s.replace(old, new, 1)

# the doc comment should say it is shared now
s = s.replace('''     * <p>The casing tag includes the ports tag, so taking "the first block in the tag" could pick a
     * port block and build the whole shell out of it. Members that belong to the ports tag are
     * therefore skipped, and the first remaining block wins.</p>''',
'''     * <p>The casing tag includes the ports tag, so taking "the first block in the tag" could pick a
     * port block and build the whole shell out of it. Members that belong to the ports tag are
     * therefore skipped, and the first remaining block wins.</p>
     *
     * <p>Shared with the projection, which draws its ghost blocks from the same answer: a preview
     * that disagreed with what the builder would place would be worse than no preview.</p>''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("casingBlock is public")

# ---- 2. drop the per-machine casing table ------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTMultiblockBlocks.java"
s2 = io.open(p2, encoding="utf-8").read()

old_helper = '''    /**
     * The casing the projection of a structure is drawn with.
     *
     * <p>One entry per machine, next to {@link #HOST_STRUCTURES}: a machine is only fully
     * described by both its shape and its material, and a preview that showed the wrong casing
     * would mislead exactly where the player is deciding what to place.</p>
     */
    public static DeferredItem<BlockItem> casingItem(ResourceLocation structureId) {
        return ADVANCED_CHEMICAL_REACTOR_STRUCTURE.equals(structureId) ? SHELL_PP_ITEM : SHELL_PE_ITEM;
    }

'''
assert old_helper in s2
s2 = s2.replace(old_helper, "", 1)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("casingItem table removed")

# ---- 3. the renderer asks the structure --------------------------------------------------------
p3 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTMultiblockProjectionClient.java"
s3 = io.open(p3, encoding="utf-8").read()

old_use = '''        ItemStack casing = new ItemStack(GTMultiblockBlocks.casingItem(structureId).get());'''
new_use = '''        // The casing comes from the structure file, the same resolution the build pass uses, so a
        // preview cannot drift from what would actually be placed.
        Block casingBlock = GTMultiblockBuilder.casingBlock(layout);
        if (casingBlock == null) {
            return;
        }
        ItemStack casing = new ItemStack(casingBlock);'''
assert old_use in s3
s3 = s3.replace(old_use, new_use, 1)

if "import net.minecraft.world.level.block.Block;" not in s3:
    lines = s3.split("\n")
    last = max(i for i, l in enumerate(lines) if l.startswith("import net.minecraft"))
    lines.insert(last + 1, "import net.minecraft.world.level.block.Block;")
    s3 = "\n".join(lines)

io.open(p3, "w", encoding="utf-8", newline="\n").write(s3)
print("renderer resolves casing from the layout")
