import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace("""import java.util.ArrayList;
import java.util.List;
import java.util.Optional;""",
"""import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;""", 1)

s = s.replace("""import net.fodoth.skina.goldentweaks.GoldenTweaks;""",
"""import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortBlockEntity;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortChannels;""", 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
