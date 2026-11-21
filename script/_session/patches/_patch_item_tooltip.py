import io, json

# ---------------- register the custom item ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMultiblock.java'
s = io.open(p, encoding='utf-8').read()
orig = s
s = s.replace('''                    () -> new BlockItem(MACHINE.get(), new Item.Properties()));''',
'''                    () -> new GTChemicalReactorItem(MACHINE.get(), new Item.Properties()));''', 1)
s = s.replace('''import net.minecraft.world.item.BlockItem;''',
'''import net.fodoth.skina.goldentweaks.content.multiblock.GTChemicalReactorItem;
import net.minecraft.world.item.BlockItem;''', 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('multiblock:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- the standalone fallback ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/StandaloneReactor.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2
s2 = s2.replace('''                () -> new BlockItem(reactor.get(), new Item.Properties()));''',
'''                () -> new GTChemicalReactorItem(reactor.get(), new Item.Properties()));''', 1)
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('standalone:', 'changed' if s2 != orig2 else 'NO CHANGE')

# ---------------- language keys ----------------
zh = {
    "tooltip.goldentweaks.chemical_reactor.kind.label": "机器类型：",
    "tooltip.goldentweaks.chemical_reactor.kind.value": "化学反应釜",
    "tooltip.goldentweaks.chemical_reactor.controls": "内置控件：",
    "tooltip.goldentweaks.chemical_reactor.controls.list": "锁定配方 溢出销毁 批量模式 输入隔离",
    "tooltip.goldentweaks.chemical_reactor.circuit.overclock": "4/2超频",
    "tooltip.goldentweaks.chemical_reactor.circuit.parallel": "1并行",
    "tooltip.goldentweaks.chemical_reactor.shift": "按住",
    "tooltip.goldentweaks.chemical_reactor.shift.key": "[shift]",
    "tooltip.goldentweaks.chemical_reactor.shift.suffix": "查看多方块结构信息",
    "tooltip.goldentweaks.chemical_reactor.structure": "多方块结构：",
    "tooltip.goldentweaks.chemical_reactor.structure.any": "（任意聚乙烯机壳）",
    "tooltip.goldentweaks.chemical_reactor.boost": "支持双仓升压",
    "tooltip.goldentweaks.chemical_reactor.hint": "请用多方块结构投影仪搭建",
}
en = {
    "tooltip.goldentweaks.chemical_reactor.kind.label": "Machine type: ",
    "tooltip.goldentweaks.chemical_reactor.kind.value": "Chemical Reaction Kettle",
    "tooltip.goldentweaks.chemical_reactor.controls": "Built-in controls:",
    "tooltip.goldentweaks.chemical_reactor.controls.list": "Lock Recipe / Void Excess / Batch Mode / Isolate Input",
    "tooltip.goldentweaks.chemical_reactor.circuit.overclock": "4/2 Overclock",
    "tooltip.goldentweaks.chemical_reactor.circuit.parallel": "1 Parallel",
    "tooltip.goldentweaks.chemical_reactor.shift": "Hold ",
    "tooltip.goldentweaks.chemical_reactor.shift.key": "[shift]",
    "tooltip.goldentweaks.chemical_reactor.shift.suffix": " for multiblock structure",
    "tooltip.goldentweaks.chemical_reactor.structure": "Multiblock structure:",
    "tooltip.goldentweaks.chemical_reactor.structure.any": "(any PE shell)",
    "tooltip.goldentweaks.chemical_reactor.boost": "Supports dual-hatch boost",
    "tooltip.goldentweaks.chemical_reactor.hint": "Build it with the multiblock structure projector",
}

def patch(path, pairs):
    data = json.load(io.open(path, encoding='utf-8'))
    before = len(data)
    for k, v in pairs.items():
        data[k] = v
    with io.open(path, 'w', encoding='utf-8', newline='\n') as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write('\n')
    print(path.split('/')[-1], before, '->', len(data))

patch(r'src/main/resources/assets/goldentweaks/lang/zh_cn.json', zh)
patch(r'src/main/resources/assets/goldentweaks/lang/en_us.json', en)
