import io

# ---------- block entity: name the tier, since each tier is its own block now ----------
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /**
     * The name to show, either the player's or the block's own.
     *
     * <p>Energy ports carry their tier in the name, because the nine tiers are otherwise identical
     * in the world and in the inventory: a player looking at two hatches has no other way to tell a
     * ULV one from a UV one. The tier-specific key falls back to the plain one so an older or
     * partially translated build still names the block rather than showing a raw key.</p>
     */
    @Override
    public Component getDisplayName() {
        if (this.customName != null) {
            return this.customName;
        }
        String base = "block.goldentweaks.gt_port_"
                + kind().id() + "_" + mode().name().toLowerCase(java.util.Locale.ROOT);
        if (kind() != GTPortKind.ENERGY) {
            return Component.translatable(base);
        }
        String key = base + ".tier_" + (tier().ordinal() + 1);
        // A build without the tier-specific entry still has to name the block rather than print the
        // raw key, so the presence check decides which of the two keys is used.
        return Language.getInstance().has(key)
                ? Component.translatable(key) : Component.translatable(base);
    }''',
'''    /**
     * The name to show, either the player's or the block's own.
     *
     * <p>Every tier has its own key: each tier is a separate block, so the name states which one it
     * is — a player looking at two ports has no other way to tell a ULV one from a UV one. The
     * tier-agnostic key remains as a fallback so a partially translated build still names the block
     * rather than printing the raw key.</p>
     */
    @Override
    public Component getDisplayName() {
        if (this.customName != null) {
            return this.customName;
        }
        String base = "block.goldentweaks.gt_port_"
                + kind().id() + "_" + mode().name().toLowerCase(java.util.Locale.ROOT);
        String key = base + "_" + portTier().id();
        return Language.getInstance().has(key)
                ? Component.translatable(key) : Component.translatable(base);
    }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('entity:', 'changed' if s != orig else 'NO CHANGE')
