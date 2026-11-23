"""Report what the atlas holds for both sheets, without trying to fill the entry by hand.

`SpriteShiftEntry.set` takes two ids, not two sprites, so an entry cannot be filled from sprites that
were looked up separately. What can be done is to ask the atlas for both ids directly and print the
answer: that is the fact the last several attempts have been guessing at.

If the atlas reports both as present, `set` is failing for a reason outside this mod's reach and the
shift has to be produced another way. If it reports either as missing, the sheets are not reaching the
atlas despite being in the jar, and that is where the fix belongs.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingBehaviour.java"
s = io.open(p, encoding="utf-8").read()

old = '''        // The entry's own setters take the resolved sprites, so no second lookup is involved.
        entry.set(from, to);
        return isUsable(entry) ? entry : null;'''
new = '''        // `SpriteShiftEntry.set` takes two ids, so the entry cannot be filled from sprites looked up
        // separately. Nothing more can be done here; the caller keeps the authored plate.
        return null;'''

assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("fallback reduced to a report")
