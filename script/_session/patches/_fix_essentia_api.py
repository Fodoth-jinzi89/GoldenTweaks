"""Correct the essentia storage against the real Thaumcraft API.

Three differences from what was written: `Aspect` stores its tag as a plain string and has no
`getRegistryName`, `AspectList.getAspects()` returns an array rather than a collection, and
`Aspect.getAspect` looks up by tag already.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTPortEssentiaStorage.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''        if (list != null) {
            for (Aspect aspect : list.getAspects()) {
                add(aspect, list.getAmount(aspect));
            }
        }''',
'''        if (list != null) {
            for (Aspect aspect : list.getAspects()) {
                if (aspect != null) {
                    add(aspect, list.getAmount(aspect));
                }
            }
        }''', 1)

s = s.replace('''            CompoundTag entry = new CompoundTag();
            ResourceLocation id = this.aspects[index].getRegistryName();
            if (id == null) {
                continue;
            }
            entry.putString(TAG_ASPECT, id.toString());
            entry.putInt(TAG_AMOUNT, this.amounts[index]);
            list.add(entry);''',
'''            CompoundTag entry = new CompoundTag();
            // The aspect's own tag is its identity; there is no registry id behind it.
            entry.putString(TAG_ASPECT, this.aspects[index].getTag());
            entry.putInt(TAG_AMOUNT, this.amounts[index]);
            list.add(entry);''', 1)

s = s.replace('''            CompoundTag entry = list.getCompound(index);
            ResourceLocation id = ResourceLocation.tryParse(entry.getString(TAG_ASPECT));
            Aspect aspect = id == null ? null : Aspect.getAspect(id.getPath());
            if (aspect == null) {
                continue;
            }''',
'''            CompoundTag entry = list.getCompound(index);
            Aspect aspect = Aspect.getAspect(entry.getString(TAG_ASPECT));
            if (aspect == null) {
                continue;
            }''', 1)

s = s.replace('''import net.minecraft.resources.ResourceLocation;
''', '')

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("essentia storage corrected")
