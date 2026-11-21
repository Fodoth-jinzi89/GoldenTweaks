import io, json

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s
if 'import net.fodoth.skina.goldentweaks.network.packet.C2SMachineParallelPacket;' not in s:
    s = s.replace('import net.fodoth.skina.goldentweaks.network.packet.C2SMachineOverclockPacket;',
                  'import net.fodoth.skina.goldentweaks.network.packet.C2SMachineOverclockPacket;\n'
                  'import net.fodoth.skina.goldentweaks.network.packet.C2SMachineParallelPacket;', 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('import:', 'changed' if s != orig else 'NO CHANGE')

zh = {
    'gui.goldentweaks.chemical_reactor.sidebar.parallel': '并行控制',
    'gui.goldentweaks.chemical_reactor.sidebar.parallel.title': '并行控制',
    'gui.goldentweaks.chemical_reactor.sidebar.parallel.max': '最大并行：1',
    'gui.goldentweaks.chemical_reactor.sidebar.parallel_limit': '最大并行数',
}
en = {
    'gui.goldentweaks.chemical_reactor.sidebar.parallel': 'Parallel control',
    'gui.goldentweaks.chemical_reactor.sidebar.parallel.title': 'Parallel control',
    'gui.goldentweaks.chemical_reactor.sidebar.parallel.max': 'Max parallel: 1',
    'gui.goldentweaks.chemical_reactor.sidebar.parallel_limit': 'Max parallel count',
}
for path, extra in [
    (r'src/main/resources/assets/goldentweaks/lang/zh_cn.json', zh),
    (r'src/main/resources/assets/goldentweaks/lang/en_us.json', en),
]:
    data = json.load(io.open(path, encoding='utf-8'))
    data.update(extra)
    with io.open(path, 'w', encoding='utf-8', newline='\n') as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write('\n')
    print('lang updated', path)
