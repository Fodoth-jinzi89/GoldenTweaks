import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- first start path ----
s = s.replace('''            this.activeProducts = productsOf(batched);
            this.activeProcessingTime = batched.processingTime();
            broadcastProducts();
            return;
        }''',
'''            this.activeProducts = productsOf(batched);
            this.activeProcessingTime = batched.processingTime();
            // Shortens the run and raises the per-tick bill. Done after startRecipe, which set the
            // unmodified length, so the two never disagree about which craft is running.
            applyOverclock(batched);
            broadcastProducts();
            return;
        }''', 1)

# ---- fall-through start path ----
s = s.replace('''        this.activeProducts = productsOf(nextBatched);
        this.activeProcessingTime = nextBatched.processingTime();
        broadcastProducts();''',
'''        this.activeProducts = productsOf(nextBatched);
        this.activeProcessingTime = nextBatched.processingTime();
        applyOverclock(nextBatched);
        broadcastProducts();''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('applyOverclock calls:', s.count('applyOverclock('))
