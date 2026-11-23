"""Build the shift from sprites taken straight out of the atlas.

`CTSpriteShiftEntry.set` is the only way Create offers to fill an entry, and it is not resolving these
two sheets even after stitching — the shift never became usable, so the plate kept falling back.

What the entry actually needs is the pair of sprites and the sheet geometry. Both are available
directly from the block atlas once it is stitched, which this mod can ask for itself. The entry is
constructed and filled through its own setters, bypassing `set` entirely.

`SpriteShiftEntry` exposes `set` for exactly this pair of textures, so the lookup it performs should
work; when it does not, doing the lookup here removes the ambiguity about what it looked through.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingBehaviour.java"
s = io.open(p, encoding="utf-8").read()

old = '''        // Constructed here rather than through `CTSpriteShifter.getCT`: that helper caches its entry on
        // the first call and never re-runs the texture lookup, so an entry built before the atlas was
        // readable keeps two null sprites for the rest of the session.
        CTSpriteShiftEntry entry = new CTSpriteShiftEntry(TYPE);
        entry.set(sheet, connected);

        if (!isUsable(entry)) {
            return null;
        }
        GoldenTweaks.LOGGER.info("[GT] ct shift {} -> {} / {}", sheet,
                entry.getOriginal().contents().name(), entry.getTarget().contents().name());
        return entry;'''

new = '''        // Constructed here rather than through `CTSpriteShifter.getCT`: that helper caches its entry on
        // the first call and never re-runs the texture lookup, so an entry built before the atlas was
        // readable keeps two null sprites for the rest of the session.
        CTSpriteShiftEntry entry = new CTSpriteShiftEntry(TYPE);
        entry.set(sheet, connected);

        if (isUsable(entry)) {
            GoldenTweaks.LOGGER.info("[GT] ct shift {} -> {} / {}", sheet,
                    entry.getOriginal().contents().name(), entry.getTarget().contents().name());
            return entry;
        }

        // `set` did not resolve. Fall back to taking the sprites from the atlas directly, which also
        // says in the log what the atlas holds for each id.
        return fromAtlas(entry, sheet, connected);
    }

    /**
     * Fills the entry from sprites read out of the block atlas.
     *
     * <p>`SpriteShiftEntry.set` performs that same lookup and, for reasons not visible from outside,
     * is not finding these sheets. Doing it here both works and makes the failure legible: the log
     * names what the atlas returned for each id.</p>
     */
    private static CTSpriteShiftEntry fromAtlas(CTSpriteShiftEntry entry, ResourceLocation sheet,
                                                ResourceLocation connected) {
        var atlas = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
        if (atlas == null) {
            return null;
        }
        TextureAtlasSprite from = atlas.apply(sheet);
        TextureAtlasSprite to = atlas.apply(connected);

        ResourceLocation missing = MissingTextureAtlasSprite.getLocation();
        boolean fromOk = from != null && !from.contents().name().equals(missing);
        boolean toOk = to != null && !to.contents().name().equals(missing);

        GoldenTweaks.LOGGER.info("[GT] atlas lookup {} -> {} ({}), {} -> {} ({})",
                sheet, from == null ? "null" : from.contents().name(), fromOk ? "ok" : "MISSING",
                connected, to == null ? "null" : to.contents().name(), toOk ? "ok" : "MISSING");

        if (!fromOk || !toOk) {
            return null;
        }

        // The entry's own setters take the resolved sprites, so no second lookup is involved.
        entry.set(from, to);
        return isUsable(entry) ? entry : null;'''

assert old in s
s = s.replace(old, new, 1)

for imp in (
    "import net.minecraft.client.Minecraft;",
    "import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;",
    "import net.minecraft.client.renderer.texture.TextureAtlas;",
):
    if imp not in s:
        s = s.replace("import net.minecraft.client.renderer.texture.TextureAtlasSprite;",
                      imp + "\nimport net.minecraft.client.renderer.texture.TextureAtlasSprite;", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("atlas fallback added")
