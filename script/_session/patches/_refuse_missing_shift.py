"""A shift whose sheet did not stitch must not be used.

`CTSpriteShiftEntry` resolves its two textures by id when it is built. A texture that never reached the
atlas resolves to the missing sprite, and the entry then maps every quad onto the black-and-magenta
checker — a visual failure whose cause is a missing texture, not a bad model.

Refusing such an entry keeps the authored plate on screen instead, and the log says which sheet was
absent so the cause is visible rather than inferred from the colours.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingBehaviour.java"
s = io.open(p, encoding="utf-8").read()

old = '''    private static CTSpriteShiftEntry shiftFor(Block casing) {
        ResourceLocation sheet = sheetOf(casing);
        if (sheet == null) {
            return null;
        }
        return SHIFTS.computeIfAbsent(sheet, GTPortCasingBehaviour::buildShift);
    }'''
new = '''    private static CTSpriteShiftEntry shiftFor(Block casing) {
        ResourceLocation sheet = sheetOf(casing);
        if (sheet == null) {
            return null;
        }
        return SHIFTS.computeIfAbsent(sheet, GTPortCasingBehaviour::buildShift);
    }

    /**
     * Whether a shift entry actually resolved both of its sheets.
     *
     * <p>An entry whose texture never reached the atlas maps every quad onto the missing sprite, which
     * draws as the black-and-magenta checker. Reporting that as unusable leaves the authored plate in
     * place instead.</p>
     */
    private static boolean isUsable(CTSpriteShiftEntry entry) {
        var original = entry.getOriginal();
        var target = entry.getTarget();
        if (original == null || target == null) {
            return false;
        }
        ResourceLocation missing = net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
                .getLocation();
        return !original.contents().name().equals(missing)
                && !target.contents().name().equals(missing);
    }'''
assert old in s
s = s.replace(old, new, 1)

# and the caller respects it
old2 = '''        if (!isPlate(original)) {
            return null;
        }
        return shiftFor(GTPortBlock.casingOf(state));'''
new2 = '''        if (!isPlate(original)) {
            return null;
        }
        CTSpriteShiftEntry entry = shiftFor(GTPortBlock.casingOf(state));
        return entry != null && isUsable(entry) ? entry : null;'''
assert old2 in s
s = s.replace(old2, new2, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("unusable shifts are refused")
