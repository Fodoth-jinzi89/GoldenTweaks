import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- pool the contents before the blocks go away, then hand both clusters over ----
s = s.replace('''        List<ItemStack> parts = new ArrayList<>();
        for (BlockPos pos : shape) {
            collectBlock(parts, pos);
        }''',
'''        List<ItemStack> parts = new ArrayList<>();
        List<FluidStack> fluids = new ArrayList<>();
        // Before the blocks are removed: the handlers live on those block entities.
        collectPortContents(parts, fluids);
        for (BlockPos pos : shape) {
            collectBlock(parts, pos);
        }''', 1)

# ---- delivery takes the fluid cluster too ----
s = s.replace('''    private void giveToPlayer(@Nullable Player player, List<ItemStack> parts) {
        if (this.level == null || parts.isEmpty()) {
            return;
        }

        List<ItemStack> stacks = BetterMatterClusterItem.makeClusters(parts);
        if (stacks.isEmpty()) {
            stacks = parts;
        }''',
'''    private void giveToPlayer(@Nullable Player player, List<ItemStack> parts) {
        giveToPlayer(player, parts, List.of());
    }

    /**
     * Sends the collected parts, and whatever the ports held, packed into matter clusters.
     *
     * <p>Two problems, two clusters: items go into a better matter cluster, fluids into a fluid one.
     * One of each is the whole point of a dismantle — the contents of a dozen hatches come back as
     * two spheres rather than a spill of stacks.</p>
     */
    private void giveToPlayer(
            @Nullable Player player, List<ItemStack> parts, List<FluidStack> fluids) {
        if (this.level == null) {
            return;
        }

        List<ItemStack> stacks = new ArrayList<>();
        if (!parts.isEmpty()) {
            List<ItemStack> packed = BetterMatterClusterItem.makeClusters(parts);
            stacks.addAll(packed.isEmpty() ? parts : packed);
        }
        if (!fluids.isEmpty()) {
            List<ItemStack> packed = FluidMatterClusterItem.makeClusters(fluids);
            stacks.addAll(packed.isEmpty() ? List.of() : packed);
        }
        if (stacks.isEmpty()) {
            return;
        }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
