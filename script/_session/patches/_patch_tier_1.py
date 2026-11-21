import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlock.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- the tier becomes a field of the block, not a state value ----
s = s.replace('''    /**
     * Port tier, one to nine.
     *
     * <p>Part of the block state rather than the block entity because the tier ring is chosen by
     * the model, and a model cannot read a block entity. Nine values cover AMI's full tier range,
     * so a port can be upgraded later without changing the block.</p>
     */
    public static final IntegerProperty TIER = IntegerProperty.create("tier", 1, 9);

    private final GTPortKind kind;
    private final PortMode mode;
    private final MapCodec<GTPortBlock> codec;

    public GTPortBlock(Properties properties, GTPortKind kind, PortMode mode) {
        super(properties);
        this.kind = kind;
        this.mode = mode;
        // The codec has to close over this instance's kind and mode: they are fixed per registered
        // block, not part of the serialized state, so a plain reflective codec cannot rebuild them.
        this.codec = simpleCodec(propertiesIn -> new GTPortBlock(propertiesIn, kind, mode));
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TIER, 1));
    }

    public GTPortKind kind() {
        return this.kind;
    }

    public PortMode mode() {
        return this.mode;
    }''',
'''    private final GTPortKind kind;
    private final PortMode mode;
    /**
     * The tier this block was registered at.
     *
     * <p>A field rather than a block state value: each tier is its own block, so the tier is fixed
     * per instance and never changes. Keeping it out of the state also means the item and the block
     * cannot disagree about which tier they are.</p>
     */
    private final GTPortTier tier;
    private final MapCodec<GTPortBlock> codec;

    public GTPortBlock(Properties properties, GTPortKind kind, PortMode mode, GTPortTier tier) {
        super(properties);
        this.kind = kind;
        this.mode = mode;
        this.tier = tier;
        // The codec has to close over this instance's kind, mode and tier: they are fixed per
        // registered block, not part of the serialized state, so a reflective codec cannot rebuild
        // them.
        this.codec = simpleCodec(propertiesIn -> new GTPortBlock(propertiesIn, kind, mode, tier));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public GTPortKind kind() {
        return this.kind;
    }

    public PortMode mode() {
        return this.mode;
    }

    /** The voltage tier this block belongs to. */
    public GTPortTier portTier() {
        return this.tier;
    }''', 1)

s = s.replace('''    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TIER);
    }''',
'''    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }''', 1)

# ---- the state no longer carries a tier, so the helper reads the block ----
s = s.replace('''    /** The tier carried by a state, as AMI's enum. Used by the structure scan. */
    public static MultiblockPortTier tierOf(BlockState state) {
        int tier = state.hasProperty(TIER) ? state.getValue(TIER) : 1;
        MultiblockPortTier[] values = MultiblockPortTier.values();
        return values[Math.max(0, Math.min(values.length - 1, tier - 1))];
    }''',
'''    /** The tier of a state's block, as AMI's enum. Used by the structure scan. */
    public static MultiblockPortTier tierOf(BlockState state) {
        return state.getBlock() instanceof GTPortBlock port
                ? port.portTier().ami() : MultiblockPortTier.values()[0];
    }''', 1)

# unused import now
s = s.replace('import net.minecraft.world.level.block.state.properties.IntegerProperty;\n', '')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('block:', 'changed' if s != orig else 'NO CHANGE')
