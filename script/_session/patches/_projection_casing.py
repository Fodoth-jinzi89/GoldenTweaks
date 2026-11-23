"""Show the right casing in a projection.

The ghost blocks were always drawn as a polyethylene casing, so an advanced build — which is
polypropylene — previewed the wrong block. The casing is now looked up from the structure id the
host already reported.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTMultiblockProjectionClient.java"
s = io.open(p, encoding="utf-8").read()

old = '''        ItemStack casing = new ItemStack(GTMultiblockBlocks.SHELL_PE_ITEM.get());'''
new = '''        ItemStack casing = new ItemStack(GTMultiblockBlocks.casingItem(structureId).get());'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("projection casing is looked up")

# ---- the lookup itself ---------------------------------------------------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTMultiblockBlocks.java"
s2 = io.open(p2, encoding="utf-8").read()

anchor = '''    @Nullable
    public static ResourceLocation structureForHost(BlockState state) {'''
addition = '''    /**
     * The casing the projection of a structure is drawn with.
     *
     * <p>One entry per machine, next to {@link #HOST_STRUCTURES}: a machine is only fully
     * described by both its shape and its material, and a preview that showed the wrong casing
     * would mislead exactly where the player is deciding what to place.</p>
     */
    public static DeferredItem<BlockItem> casingItem(ResourceLocation structureId) {
        return ADVANCED_CHEMICAL_REACTOR_STRUCTURE.equals(structureId) ? SHELL_PP_ITEM : SHELL_PE_ITEM;
    }

    @Nullable
    public static ResourceLocation structureForHost(BlockState state) {'''
assert anchor in s2
s2 = s2.replace(anchor, addition, 1)

# name the constant used above rather than repeating the literal
s2 = s2.replace('''            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "advanced_chemical_planet_controller"),''',
                '''            ADVANCED_CHEMICAL_REACTOR_STRUCTURE,''', 1)
s2 = s2.replace('''            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "advanced_chemical_plant_controller"),
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "advanced_chemical_reactor"));''',
'''            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "advanced_chemical_plant_controller"),
            ADVANCED_CHEMICAL_REACTOR_STRUCTURE);''', 1)

s2 = s2.replace('''    private static final Map<ResourceLocation, ResourceLocation> HOST_STRUCTURES = Map.of(''',
'''    /** Structure file backing the advanced kettle. */
    public static final ResourceLocation ADVANCED_CHEMICAL_REACTOR_STRUCTURE =
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "advanced_chemical_reactor");

    private static final Map<ResourceLocation, ResourceLocation> HOST_STRUCTURES = Map.of(''', 1)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("casingItem helper added")
