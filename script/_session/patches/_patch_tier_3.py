import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTMultiblockBlocks.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- maps become kind -> mode -> tier -> holder ----
s = s.replace('''    /** Port blocks keyed by kind and mode, so the capability registration can find them. */
    public static final Map<GTPortKind, Map<PortMode, DeferredBlock<GTPortBlock>>> PORT_BLOCKS =
            new EnumMap<>(GTPortKind.class);

    /** Port block items, in the same shape, so the creative tab can list them. */
    public static final Map<GTPortKind, Map<PortMode, DeferredItem<BlockItem>>> PORT_ITEMS =
            new EnumMap<>(GTPortKind.class);''',
'''    /** Port blocks keyed by kind, then mode, then tier. */
    public static final Map<GTPortKind,
            Map<PortMode, Map<GTPortTier, DeferredBlock<GTPortBlock>>>> PORT_BLOCKS =
            new EnumMap<>(GTPortKind.class);

    /** Port block items, in the same shape, so the creative tab can list them. */
    public static final Map<GTPortKind,
            Map<PortMode, Map<GTPortTier, DeferredItem<BlockItem>>>> PORT_ITEMS =
            new EnumMap<>(GTPortKind.class);''', 1)

# ---- registration fills the inner map properly ----
s = s.replace('''            Map<PortMode, DeferredBlock<GTPortBlock>> modes = new EnumMap<>(PortMode.class);
            Map<PortMode, DeferredItem<BlockItem>> items = new EnumMap<>(PortMode.class);
            for (PortMode mode : PortMode.values()) {
                for (GTPortTier tier : GTPortTier.values()) {
                    String name = "gt_port_" + kind.id() + "_"
                            + mode.name().toLowerCase(java.util.Locale.ROOT)
                            + "_" + tier.id();
                    DeferredBlock<GTPortBlock> block = BLOCKS.register(name,
                            () -> new GTPortBlock(properties(), kind, mode, tier));
                    modes.put(mode, block);
                    items.put(mode, ITEMS.register(name,
                            () -> new BlockItem(block.get(), new Item.Properties())));
                }
            }''',
'''            Map<PortMode, Map<GTPortTier, DeferredBlock<GTPortBlock>>> modes =
                    new EnumMap<>(PortMode.class);
            Map<PortMode, Map<GTPortTier, DeferredItem<BlockItem>>> items =
                    new EnumMap<>(PortMode.class);
            for (PortMode mode : PortMode.values()) {
                Map<GTPortTier, DeferredBlock<GTPortBlock>> tieredBlocks =
                        new EnumMap<>(GTPortTier.class);
                Map<GTPortTier, DeferredItem<BlockItem>> tieredItems =
                        new EnumMap<>(GTPortTier.class);
                for (GTPortTier tier : GTPortTier.values()) {
                    String name = "gt_port_" + kind.id() + "_"
                            + mode.name().toLowerCase(java.util.Locale.ROOT)
                            + "_" + tier.id();
                    DeferredBlock<GTPortBlock> block = BLOCKS.register(name,
                            () -> new GTPortBlock(properties(), kind, mode, tier));
                    tieredBlocks.put(tier, block);
                    tieredItems.put(tier, ITEMS.register(name,
                            () -> new BlockItem(block.get(), new Item.Properties())));
                }
                modes.put(mode, tieredBlocks);
                items.put(mode, tieredItems);
            }''', 1)

# ---- the entity type needs every block across all three levels ----
s = s.replace('''                                PORT_BLOCKS.values().stream()
                                        .flatMap(modes -> modes.values().stream())
                                        .map(DeferredBlock::get)''',
'''                                PORT_BLOCKS.values().stream()
                                        .flatMap(modes -> modes.values().stream())
                                        .flatMap(tiers -> tiers.values().stream())
                                        .map(DeferredBlock::get)''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('blocks:', 'changed' if s != orig else 'NO CHANGE')
