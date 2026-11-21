import io

# ---------------- register the custom port item ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTMultiblockBlocks.java'
s = io.open(p, encoding='utf-8').read()
orig = s
s = s.replace('''                    tieredItems.put(tier, ITEMS.register(name,
                            () -> new BlockItem(block.get(), new Item.Properties())));''',
'''                    tieredItems.put(tier, ITEMS.register(name,
                            () -> new GTPortItem(block.get(), new Item.Properties())));''', 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('blocks:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- port block exposes the tier ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlock.java'
s2 = io.open(p2, encoding='utf-8').read()
before = s2
print('block kind/mode/tier accessors present:', all(t in s2 for t in ('GTPortKind kind()', 'PortMode mode()', 'GTPortTier portTier()')))
