import io, re

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()

# Drop the TAG_WARMUP idea: the warm-up is not state that belongs in a save, it is what every load
# does. Saving it also meant an old save (no tag) would skip the wait entirely.
s = s.replace('''    /** Ticks left in the warm-up that follows a load, so nothing starts before the world is ready. */
    private static final String TAG_WARMUP = "Warmup";
''', '')

s = s.replace('''            }
            tag.putInt(TAG_WARMUP, WARMUP_TICKS);''', '''            }''')

s = s.replace('''        this.pendingMode = tag.contains(TAG_ACTIVE_MODE) ? tag.getInt(TAG_ACTIVE_MODE) : null;
        this.warmupTicks = tag.getInt(TAG_WARMUP);''',
'''        this.pendingMode = tag.contains(TAG_ACTIVE_MODE) ? tag.getInt(TAG_ACTIVE_MODE) : null;
        // Unconditional, and not read from the tag: this is what a load *is*, not something a save
        // records. An older save has no field for it and must still get the wait.
        this.warmupTicks = WARMUP_TICKS;''')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('TAG_WARMUP left:', s.count('TAG_WARMUP'))
print('warmupTicks refs:', s.count('warmupTicks'))
