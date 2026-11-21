import io, json

# Controller position wording needs parts, so it is assembled from two keys rather than baked into
# one format string: "第二层，正面中央" has both a number and a place.
zh = {
    "tooltip.goldentweaks.chemical_reactor.size.label": "大小：",
    "tooltip.goldentweaks.chemical_reactor.size.unit": "×",
    "tooltip.goldentweaks.chemical_reactor.structure": "结构：",
    "tooltip.goldentweaks.chemical_reactor.controller.label": "    控制器：",
    "tooltip.goldentweaks.chemical_reactor.controller.value": "第%s层，%s",
    "tooltip.goldentweaks.chemical_reactor.controller.row.back": "后方",
    "tooltip.goldentweaks.chemical_reactor.controller.row.front": "正面",
    "tooltip.goldentweaks.chemical_reactor.controller.row.middle": "中央",
    "tooltip.goldentweaks.chemical_reactor.structure.any": "：任意聚乙烯机壳",
}
en = {
    "tooltip.goldentweaks.chemical_reactor.size.label": "Size: ",
    "tooltip.goldentweaks.chemical_reactor.size.unit": "x",
    "tooltip.goldentweaks.chemical_reactor.structure": "Structure:",
    "tooltip.goldentweaks.chemical_reactor.controller.label": "    Controller: ",
    "tooltip.goldentweaks.chemical_reactor.controller.value": "layer %s, %s",
    "tooltip.goldentweaks.chemical_reactor.controller.row.back": "back row",
    "tooltip.goldentweaks.chemical_reactor.controller.row.front": "front row",
    "tooltip.goldentweaks.chemical_reactor.controller.row.middle": "middle row",
    "tooltip.goldentweaks.chemical_reactor.structure.any": ": any PE casing",
}

def patch(path, pairs):
    data = json.load(io.open(path, encoding='utf-8'))
    data.update(pairs)
    with io.open(path, 'w', encoding='utf-8', newline='\n') as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write('\n')
    print(path.split('/')[-1], len(data))

patch(r'src/main/resources/assets/goldentweaks/lang/zh_cn.json', zh)
patch(r'src/main/resources/assets/goldentweaks/lang/en_us.json', en)
