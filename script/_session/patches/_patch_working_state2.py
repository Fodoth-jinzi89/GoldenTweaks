import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- both start paths must refresh the look now that it tracks the craft, not the switch ----
s = s.replace('''            applyOverclock(batched);
            broadcastProducts();
            return;
        }''',
'''            applyOverclock(batched);
            // The texture follows the craft now, so it has to be refreshed the moment one begins —
            // there is no longer a standing "switch is on" state to make it look right by accident.
            applyRunningState();
            broadcastProducts();
            return;
        }''', 1)

s = s.replace('''        applyOverclock(nextBatched);
        broadcastProducts();''',
'''        applyOverclock(nextBatched);
        applyRunningState();
        broadcastProducts();''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('applyRunningState calls:', s.count('applyRunningState();'))
