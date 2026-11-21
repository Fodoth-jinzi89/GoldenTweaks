import io, json

# The size line now names the axes literally, so the old per-character "x" and the row words are
# replaced by a single multiplication sign plus a spelled-out controller position.
zh = {
    "tooltip.goldentweaks.chemical_reactor.size.label": "大小：",
    "tooltip.goldentweaks.chemical_reactor.size.axis": "×",
    "tooltip.goldentweaks.chemical_reactor.structure": "结构：",
    "tooltip.goldentweaks.chemical_reactor.controller.label": "控制器：",
    "tooltip.goldentweaks.chemical_reactor.controller.value": "第%s层，正面中央",
    "tooltip.goldentweaks.chemical_reactor.structure.any": "：任意聚乙烯机壳",
}
en = {
    "tooltip.goldentweaks.chemical_reactor.size.label": "Size: ",
    "tooltip.goldentweaks.chemical_reactor.size.axis": "x",
    "tooltip.goldentweaks.chemical_reactor.structure": "Structure:",
    "tooltip.goldentweaks.chemical_reactor.controller.label": "Controller: ",
    "tooltip.goldentweaks.chemical_reactor.controller.value": "layer %s, front centre",
    "tooltip.goldentweaks.chemical_reactor.structure.any": ": any PE casing",
}

# Drop the keys the hard-coded version made obsolete.
stale = ("tooltip.goldentweaks.chemical_reactor.size.unit",
         "tooltip.goldentweaks.chemical_reactor.controller.row.back",
         "tooltip.goldentweaks.chemical_reactor.controller.row.front",
         "tooltip.goldentweaks.chemical_reactor.controller.row.middle")

def patch(path, pairs):
    data = json.load(io.open(path, encoding='utf-8'))
    data.update(pairs)
    removed = [k for k in stale if data.pop(k, None) is not None]
    with io.open(path, 'w', encoding='utf-8', newline='\n') as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write('\n')
    print(path.split('/')[-1], len(data), 'removed', removed)

patch(r'src/main/resources/assets/goldentweaks/lang/zh_cn.json'.replace('\\', '/'), zh)
patch(r'src/main/resources/assets/goldentweaks/lang/en_us.json', en)
