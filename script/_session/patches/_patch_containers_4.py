import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlock.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /**
     * Spills the port's contents the way the machine spills its controller slot.
     *
     * <p>{@code Containers.dropContentsOnDestroy} is the same helper the controller uses, and it is
     * the right one for the same reasons: it guards on the block actually changing, so a state
     * update drops nothing, and it reads the tile's container view instead of iterating anything by
     * hand.</p>
     *
     * <p>Fluids have no container view, so they are packed here. Energy is deliberately dropped —
     * a broken hatch comes back as the hatch alone, since charge has no item to travel in.</p>
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
                         boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof GTPortBlockEntity port) {
            port.spillFluids();
        }
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }''',
'''    /**
     * Drops the port's contents, the way the machine drops its controller slot.
     *
     * <p>Same helper and same guard as the controller: {@code Containers.dropContentsOnDestroy}
     * only acts when the block actually changes, so a state update drops nothing. Contents are taken
     * out through the container view here rather than at the moment of the drop, because they have
     * to be packed into matter clusters first — dropping them one slot at a time would scatter a
     * hatch's whole inventory across the ground.</p>
     *
     * <p>Fluids are packed the same way, from the tanks. Energy is deliberately not carried: a
     * broken hatch comes back as the hatch alone, since charge has no item to travel in.</p>
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
                         boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof GTPortBlockEntity port) {
            port.dropContentsPacked();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('block:', 'changed' if s != orig else 'NO CHANGE')

# ---- the packed drop on the entity ----
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2

s2 = s2.replace('''    /**
     * Drops whatever the tanks hold, packed into fluid clusters.
     *
     * <p>Called from the block's removal, because a fluid buffer is not a {@code Container} and the
     * vanilla helper cannot see it. Packed rather than poured out stack by stack: the same reason
     * the machine packs its parts.</p>
     */
    public void spillFluids() {
        if (this.level == null || this.level.isClientSide || this.fluids == null) {
            return;
        }''',
'''    /**
     * Empties the port onto the ground, packed into matter clusters.
     *
     * <p>One cluster for the items it held and one for the fluids, matching what a dismantled
     * machine gives back. Called from the block's removal, which is the only point where the
     * contents still exist.</p>
     */
    public void dropContentsPacked() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        List<ItemStack> items = new ArrayList<>();
        if (this.items != null) {
            for (int slot = 0; slot < this.items.getSlots(); slot++) {
                ItemStack stack = this.items.extractItem(slot, Integer.MAX_VALUE, false);
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
        }
        if (!items.isEmpty()) {
            List<ItemStack> packed = BetterMatterClusterItem.makeClusters(items);
            for (ItemStack cluster : packed.isEmpty() ? items : packed) {
                Block.popResource(this.level, this.worldPosition, cluster);
            }
        }
        spillFluids();
    }

    /** Drops whatever the tanks hold, packed into fluid clusters. */
    private void spillFluids() {
        if (this.level == null || this.level.isClientSide || this.fluids == null) {
            return;
        }''', 1)

s2 = s2.replace('''        java.util.List<ItemStack> clusters = net.weibai.avaritia_more_items.common.items.matter_cluster
                .FluidMatterClusterItem.makeClusters(held);''',
'''        List<ItemStack> clusters = FluidMatterClusterItem.makeClusters(held);''', 1)

for imp, anchor in [
    ('import net.weibai.avaritia_more_items.common.items.matter_cluster.BetterMatterClusterItem;',
     'import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.MultiblockPortTier;'),
    ('import net.weibai.avaritia_more_items.common.items.matter_cluster.FluidMatterClusterItem;',
     'import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.MultiblockPortTier;'),
]:
    if imp not in s2:
        s2 = s2.replace(anchor, imp + '\n' + anchor, 1)

io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('entity:', 'changed' if s2 != orig2 else 'NO CHANGE')
