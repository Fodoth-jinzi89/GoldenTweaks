import io
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s
s = s.replace("        // Read from the stored value, so the line and the box below it can never disagree.\n        this.font.getClass();\n", "        // Read from the stored value, so the line and the box below it can never disagree.\n", 1)
if 'import net.fodoth.skina.goldentweaks.network.packet.C2SMachineOverclockPacket;' not in s:
    s = s.replace('import net.fodoth.skina.goldentweaks.network.packet.C2SMachineNamePacket;',
                  'import net.fodoth.skina.goldentweaks.network.packet.C2SMachineNamePacket;\n'
                  'import net.fodoth.skina.goldentweaks.network.packet.C2SMachineOverclockPacket;', 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
