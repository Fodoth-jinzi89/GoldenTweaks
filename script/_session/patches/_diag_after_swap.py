"""Report the sprite after the swap, not before it.

The diagnostic printed the quads as the wrapped model returned them, which is the input to the
swap. Whatever the code then did was invisible, so "did the replacement happen" could not be read
off the log at all.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingModel.java"
s = io.open(p, encoding="utf-8").read()

old = '''        BlockState casing = data.get(CASING);
        if (DIAG.getAndIncrement() < 6) {
            String firstSprite = quads.isEmpty() || quads.getFirst().getSprite() == null
                    ? "none"
                    : quads.getFirst().getSprite().contents().name().toString();
            net.fodoth.skina.goldentweaks.GoldenTweaks.LOGGER.info(
                    "[GT/diag] getQuads side={} state={} casing={} quads={} firstSprite={}",
                    side, state == null ? "null" : state.getBlock(), casing, quads.size(), firstSprite);
        }
        if (casing == null) {
            // No override applies: the item form, or a part not yet built into a machine. GT's model
            // behaves the same way, and it is why an inventory icon keeps its authored plate.
            return quads;
        }

        TextureAtlasSprite plate = plateSprite(casing);

        return retexture(quads, plate);'''

new = '''        BlockState casing = data.get(CASING);
        if (casing == null) {
            // No override applies: the item form, or a part not yet built into a machine. GT's model
            // behaves the same way, and it is why an inventory icon keeps its authored plate.
            return quads;
        }

        TextureAtlasSprite plate = plateSprite(casing);
        List<BakedQuad> result = plate == null ? quads : retexture(quads, plate);

        if (DIAG.getAndIncrement() < 8) {
            String before = quads.isEmpty() || quads.getFirst().getSprite() == null
                    ? "none" : quads.getFirst().getSprite().contents().name().toString();
            String after = result.isEmpty() || result.getFirst().getSprite() == null
                    ? "none" : result.getFirst().getSprite().contents().name().toString();
            net.fodoth.skina.goldentweaks.GoldenTweaks.LOGGER.info(
                    "[GT/diag] getQuads side={} state={} casing={} plate={} before={} after={}",
                    side, state == null ? "null" : state.getBlock(),
                    casing.getBlock(), plate == null ? "null" : plate.contents().name(),
                    before, after);
        }

        return result;'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("diagnostic now reports the result")
