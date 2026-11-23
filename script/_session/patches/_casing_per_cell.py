"""Resolve a projection's ghost block per cell, from that cell's own symbol.

`casingBlock(layout)` answers one block for the whole machine, which cannot describe a structure
that names more than one casing symbol. Cells are previewed with the block their own symbol means:

  - a plain block id gives that block;
  - a tag gives its first member that is not a port.

The port exclusion is the existing rule, kept for the same reason: the casing tag also lists the
ports tag, so a cell may legally take either, and a port drawn as shell filler would be a
misleading preview.

The build pass keeps using the single-casing form; it places one block type and consumes it from
the player's inventory, so changing it is a separate job from fixing the preview.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTMultiblockBuilder.java"
s = io.open(p, encoding="utf-8").read()

anchor = '''    /**
     * The block used as casing, resolved from whatever the structure calls its casing.'''
assert anchor in s

addition = '''    /**
     * The block a single structure cell stands for.
     *
     * <p>Resolved per symbol rather than once for the whole structure: a shape may name several
     * casing symbols, and each cell should preview with what its own letter means.</p>
     *
     * <p>A tag resolves to its first member that is not a port. The casing tag deliberately also
     * contains the ports tag, so a cell accepting either is legal; drawing a port as shell filler
     * would mislead, and a port is placed by the player with a function in mind.</p>
     *
     * @return the block, or {@code null} when the symbol has no usable member
     */
    @Nullable
    public static Block casingFor(GTMultiblockPattern layout, char symbol) {
        if (layout.isWildcard(symbol)) {
            return null;
        }
        String key = layout.keyAt(symbol);
        if (key == null) {
            return null;
        }
        if (!key.startsWith("#")) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            return id == null ? null : BuiltInRegistries.BLOCK.get(id);
        }

        TagKey<Block> tag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(key.substring(1)));
        if (tag.equals(PORTS)) {
            return null;
        }
        var holders = BuiltInRegistries.BLOCK.getTag(tag);
        if (holders.isEmpty()) {
            return null;
        }
        for (var holder : holders.get()) {
            Block candidate = holder.value();
            if (!candidate.defaultBlockState().is(PORTS)) {
                return candidate;
            }
        }
        return null;
    }

'''
s = s.replace(anchor, addition + anchor, 1)

if "import org.jetbrains.annotations.Nullable;" not in s:
    lines = s.split("\n")
    last = max(i for i, l in enumerate(lines) if l.startswith("import "))
    lines.insert(last + 1, "import org.jetbrains.annotations.Nullable;")
    s = "\n".join(lines)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("casingFor(layout, symbol) added; casingBlock kept for the build pass")

# ---- the renderer decides per cell -------------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTMultiblockProjectionClient.java"
s2 = io.open(p2, encoding="utf-8").read()

old_pre = '''        // The casing comes from the structure file, the same resolution the build pass uses, so a
        // preview cannot drift from what would actually be placed.
        Block casingBlock = GTMultiblockBuilder.casingBlock(layout);
        if (casingBlock == null) {
            return;
        }
        ItemStack casing = new ItemStack(casingBlock);
        ItemRenderer itemRenderer = minecraft.getItemRenderer();'''
new_pre = '''        ItemRenderer itemRenderer = minecraft.getItemRenderer();'''
assert old_pre in s2
s2 = s2.replace(old_pre, new_pre, 1)

old_loop = '''                    if (layout.isWildcard(layout.symbolAt(column, layer, row))) {
                        continue;
                    }
                    BlockPos pos = GTChemicalReactorOrientation.cell('''
new_loop = '''                    char symbol = layout.symbolAt(column, layer, row);
                    // Each cell previews with what its own letter means. A cell that accepts a port
                    // or a casing is shown as the casing: the preview is there to say "something
                    // goes here", and the port is the player's choice to make.
                    Block ghost = GTMultiblockBuilder.casingFor(layout, symbol);
                    if (ghost == null) {
                        continue;
                    }
                    BlockPos pos = GTChemicalReactorOrientation.cell('''
assert old_loop in s2
s2 = s2.replace(old_loop, new_loop, 1)

old_render = '''                    itemRenderer.renderStatic(casing, ItemDisplayContext.NONE, light,'''
new_render = '''                    itemRenderer.renderStatic(new ItemStack(ghost), ItemDisplayContext.NONE, light,'''
assert old_render in s2
s2 = s2.replace(old_render, new_render, 1)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("renderer resolves each cell from its symbol")
