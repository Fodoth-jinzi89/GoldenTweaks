"""Make the reactor entity configurable, so a second machine can extend it.

Four things differ between machines that are otherwise identical: the structure file, the
casing block, the block entity type, and the display name. Everything else — the recipe
schedule, the ports, the overclock, the batch logic, the save/load — is shared.

The constructor moves to a protected take that accepts a type, which is what lets a subclass
register its own BlockEntityType while keeping one implementation.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

# ---------------- constructor: let a subclass supply its own type ----------------
s = s.replace('''    public GTChemicalReactorMachineBlockEntity(BlockPos pos, BlockState state) {
        super(GTChemicalReactorMultiblock.MACHINE_ENTITY.get(), pos, state);
    }

    @Override
    protected ResourceLocation getPatternId() {
        return ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, MACHINE_ID);
    }

    @Override
    public String getMachineId() {
        return MACHINE_ID;
    }''',
'''    public GTChemicalReactorMachineBlockEntity(BlockPos pos, BlockState state) {
        this(GTChemicalReactorMultiblock.MACHINE_ENTITY.get(), pos, state);
    }

    /**
     * The constructor a second machine uses.
     *
     * <p>Everything below this line is shared; a subclass exists only to answer the four
     * questions in {@link #getPatternId}, {@link #getMachineId}, {@link #casingBlock} and
     * {@link #getDisplayName}.</p>
     */
    protected GTChemicalReactorMachineBlockEntity(
            net.minecraft.world.level.block.entity.BlockEntityType<?> type,
            BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected ResourceLocation getPatternId() {
        return ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, getMachineId());
    }

    @Override
    public String getMachineId() {
        return MACHINE_ID;
    }

    /**
     * The wall block this machine's shape is built from.
     *
     * <p>Read here rather than compared inline so a subclass can answer with its own casing without
     * the structure check needing to know which machine it is running for.</p>
     */
    protected net.minecraft.world.level.block.Block casingBlock() {
        return GTMultiblockBlocks.SHELL_PE.get();
    }''', 1)

# ---------------- the casing count uses the hook ----------------
s = s.replace('''                    if (here.is(GTMultiblockBlocks.SHELL_PE.get())) {
                        casings++;
                    }''',
'''                    if (here.is(casingBlock())) {
                        casings++;
                    }''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("entity is subclassable")
