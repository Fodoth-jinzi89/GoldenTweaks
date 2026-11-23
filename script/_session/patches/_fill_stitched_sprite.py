"""Fill the shift's sprites as soon as it is created.

`SpriteShiftEntry.set` does not store sprites. It stores two `StitchedSprite` proxies:

    original = new StitchedSprite(originalLocation);

and a `StitchedSprite` is only populated by catnip's stitch callback:

    public static void onTextureStitchPost(TextureAtlas atlas) { ... sprite.loadSprite(atlas); }

That callback runs once per atlas build, over the instances registered at that moment. An entry created
afterwards — which is every entry this mod builds at render time — joins the registry too late and its
`TextureAtlasSprite` field stays null for the rest of the session. `getOriginal()` then hands back null,
which is exactly what the diagnostics reported while the atlas itself answered "ok".

So the fill is done here: `onTextureStitchPost` is public and re-runs over every registered entry, which
includes the one just created. Calling it right after `set` fills this entry and any earlier ones, at
the cost of one loop over a short list.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingBehaviour.java"
s = io.open(p, encoding="utf-8").read()

old = '''        CTSpriteShiftEntry entry = new CTSpriteShiftEntry(TYPE);
        entry.set(sheet, connected);

        if (PROBES.getAndIncrement() < 6) {
            GoldenTweaks.LOGGER.info("[GT/diag] after set: original={} target={} usable={}",
                    entry.getOriginal() == null ? "NULL" : entry.getOriginal().contents().name(),
                    entry.getTarget() == null ? "NULL" : entry.getTarget().contents().name(),
                    isUsable(entry));
        }

        if (isUsable(entry)) {
            GoldenTweaks.LOGGER.info("[GT] ct shift {} -> {} / {}", sheet,
                    entry.getOriginal().contents().name(), entry.getTarget().contents().name());
            return entry;
        }

        // `set` did not resolve. Fall back to taking the sprites from the atlas directly, which also
        // says in the log what the atlas holds for each id.
        return fromAtlas(entry, sheet, connected);
    }'''

new = '''        CTSpriteShiftEntry entry = new CTSpriteShiftEntry(TYPE);
        entry.set(sheet, connected);

        // `set` only registers proxies; they are filled by this callback, which otherwise ran before
        // this entry existed. Re-running it now fills this one from the current atlas.
        TextureAtlas atlas = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
        if (atlas != null) {
            StitchedSprite.onTextureStitchPost(atlas);
        }

        if (!isUsable(entry)) {
            return null;
        }
        GoldenTweaks.LOGGER.info("[GT] ct shift {} -> {} / {}", sheet,
                entry.getOriginal().contents().name(), entry.getTarget().contents().name());
        return entry;
    }'''
assert old in s
s = s.replace(old, new, 1)

# the atlas-report fallback is no longer needed
start = s.find('''    /**
     * Fills the entry from sprites read out of the block atlas.''')
if start >= 0:
    end = s.index("\n    }\n", s.index("return null;", start)) + len("\n    }\n")
    s = s[:start] + s[end:]

s = s.replace('''
    /** Throttles the probe that reports the entry's state right after `set`. */
    private static final java.util.concurrent.atomic.AtomicInteger PROBES =
            new java.util.concurrent.atomic.AtomicInteger();
''', "")

if "import net.createmod.catnip.render.StitchedSprite;" not in s:
    s = s.replace("import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;",
                  "import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;\n"
                  "import net.createmod.catnip.render.StitchedSprite;", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("sprites are filled right after set")
