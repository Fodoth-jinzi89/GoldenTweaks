import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- the partial path pools contents too, exactly like the full one ----
s = s.replace('''    private boolean dismantleHostOnly(Player player) {
        List<BlockPos> area = partialShapeBlocks();
        List<ItemStack> parts = new ArrayList<>();

        for (BlockPos pos : area) {
            collectBlock(parts, pos);
        }''',
'''    private boolean dismantleHostOnly(Player player) {
        // The same detail scan the full path uses, so a half-built machine's hatches give back what
        // they hold instead of being removed with the contents still inside.
        List<BlockPos> area = partialShapeBlocks();
        List<ItemStack> parts = new ArrayList<>();
        List<FluidStack> fluids = new ArrayList<>();
        collectPortContents(area, parts, fluids);

        for (BlockPos pos : area) {
            collectBlock(parts, pos);
        }''', 1)

s = s.replace('''        this.running = false;
        giveToPlayer(player, parts);

        // Blocks go last, so the tile is still valid while the parts are being collected above.''',
'''        this.running = false;
        giveToPlayer(player, parts, fluids);

        // Blocks go last, so the tile is still valid while the parts are being collected above.''', 1)

# ---- the pooled scan takes the positions to examine ----
s = s.replace('''    private void collectPortContents(List<ItemStack> items, List<FluidStack> fluids) {
        if (this.level == null || this.structure == null) {
            return;
        }
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (!(this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity)) {
                continue;
            }''',
'''    private void collectPortContents(List<ItemStack> items, List<FluidStack> fluids) {
        if (this.level == null) {
            return;
        }
        List<BlockPos> positions = new ArrayList<>();
        if (this.structure != null) {
            for (MultiblockPortBinding port : this.structure.getPorts()) {
                positions.add(port.pos());
            }
        }
        collectPortContents(positions, items, fluids);
    }

    /**
     * Pools the contents of the ports at {@code positions}.
     *
     * <p>Driven by positions rather than by the assembled structure, because a half-built machine
     * has no structure object yet — only blocks that answer to the pattern. Both teardown paths find
     * their own positions and hand them here, so neither can forget the contents.</p>
     */
    private void collectPortContents(
            List<BlockPos> positions, List<ItemStack> items, List<FluidStack> fluids) {
        if (this.level == null) {
            return;
        }
        for (BlockPos pos : positions) {
            if (!(this.level.getBlockEntity(pos) instanceof GTPortBlockEntity entity)) {
                continue;
            }''', 1)

# the loop body used `port.pos()` before; the reassignment above renamed it to `pos`
s = s.replace('''        for (BlockPos pos : positions) {
            if (!(this.level.getBlockEntity(pos) instanceof GTPortBlockEntity entity)) {
                continue;
            }
            if (entity.itemHandler() != null) {''',
'''        for (BlockPos pos : positions) {
            if (!(this.level.getBlockEntity(pos) instanceof GTPortBlockEntity entity)) {
                continue;
            }
            if (entity.itemHandler() != null) {''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
