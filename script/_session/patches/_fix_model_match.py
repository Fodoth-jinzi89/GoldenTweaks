"""Match the port models by the path they are actually registered under.

`ModifyBakingResult` is keyed by model id, not block id, and a block model's id is
`<namespace>:block/<path>`. The path test looked for a leading `gt_port_`, which never matches
`block/multiblock/gt_port_...`, so no model was ever wrapped — every port then drew with the casing
plates unswapped, and the block entity's casing had nowhere to take effect.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortModelWrapping.java"
s = io.open(p, encoding="utf-8").read()

old = '''    /**
     * Whether a model belongs to a port block.
     *
     * <p>Asked of the id's path rather than of a registry lookup: every port model is registered
     * under its block's own id, and the path test cannot fail on a registry that is still loading.</p>
     */
    private static boolean isPortModel(ModelResourceLocation id) {
        return id.id().getPath().startsWith("gt_port_");
    }'''
new = '''    /**
     * Whether a model belongs to a port block.
     *
     * <p>Matched on the file name rather than the whole path: a block model's id is
     * {@code <namespace>:block/<path>}, so the leading segment is the folder, not the block. Asking
     * for a prefix on the full path silently matched nothing.</p>
     */
    private static boolean isPortModel(ModelResourceLocation id) {
        return id.id().getPath().contains("/gt_port_");
    }'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("port models matched by their real path")

# ---- the getQuads pass-through must never lose the model's own quads --------------------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingModel.java"
s2 = io.open(p2, encoding="utf-8").read()

old2 = '''    private List<BakedQuad> reskin(List<BakedQuad> quads, BlockState casing) {
        TextureAtlasSprite plate = plateSprite(casing);

        List<BakedQuad> result = new ArrayList<>(quads.size());'''
new2 = '''    private List<BakedQuad> reskin(List<BakedQuad> quads, BlockState casing) {
        TextureAtlasSprite plate = plateSprite(casing);
        if (plate == null) {
            // The casing's model has no sprite to offer — a broken or missing model. Leaving the
            // authored plate alone is better than drawing quads with no texture at all.
            return quads;
        }

        List<BakedQuad> result = new ArrayList<>(quads.size());'''
assert old2 in s2
s2 = s2.replace(old2, new2, 1)

old3 = '''    private static TextureAtlasSprite plateSprite(BlockState casing) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(casing);
        return model.getParticleIcon(ModelData.EMPTY);
    }'''
new3 = '''    private static TextureAtlasSprite plateSprite(BlockState casing) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(casing);
        return model.getParticleIcon(ModelData.EMPTY);
    }'''
s2 = s2.replace(old3, new3, 1)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("missing casing sprite falls back to the authored plate")
