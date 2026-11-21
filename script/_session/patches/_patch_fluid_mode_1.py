import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- swap the AMI buffer for ours, which enforces the mode ----
s = s.replace('''                // One tank per level, each holding 8000 mB per level.
                int tanks = portTier().fluidTanks();
                int capacity = portTier().tankCapacity();
                MultiblockFluidBuffer replacement = new MultiblockFluidBuffer(tanks, capacity);''',
'''                // One tank per level, each holding 8000 mB per level. Ours rather than AMI's,
                // because the stacking mode has to decide which tank a fluid may enter — AMI's
                // buffer spreads a fluid across every tank with room, which is exactly the
                // behaviour multi-cell mode asks for and single-cell mode must prevent.
                int tanks = portTier().fluidTanks();
                int capacity = portTier().tankCapacity();
                GTPortFluidStorage replacement = new GTPortFluidStorage(
                        tanks, capacity, this::isSingleCellMode);''', 1)

s = s.replace('''        } else if (this.fluids instanceof MultiblockFluidBuffer buffer) {
            tag.put(TAG_CONTENT, buffer.save(registries));''',
'''        } else if (this.fluids instanceof GTPortFluidStorage buffer) {
            tag.put(TAG_CONTENT, buffer.save(registries));''', 1)

s = s.replace('''        } else if (this.fluids instanceof MultiblockFluidBuffer buffer) {
            buffer.load(content, registries);''',
'''        } else if (this.fluids instanceof GTPortFluidStorage buffer) {
            buffer.load(content, registries);''', 1)

s = s.replace('import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.buffer.MultiblockFluidBuffer;\n', '')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
