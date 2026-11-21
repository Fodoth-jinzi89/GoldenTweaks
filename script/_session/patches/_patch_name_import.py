import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s
if 'import net.minecraft.locale.Language;' not in s:
    s = s.replace('import net.minecraft.nbt.CompoundTag;',
                  'import net.minecraft.locale.Language;\nimport net.minecraft.nbt.CompoundTag;', 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('import:', 'changed' if s != orig else 'NO CHANGE')
