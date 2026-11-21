import io, json

# The colon belongs to the dim half: the label is white, everything after it — colon included —
# is dark grey. Splitting it out is what lets the two halves differ in colour.
zh = {
    "tooltip.goldentweaks.chemical_reactor.controller.label": "控制器",
    "tooltip.goldentweaks.chemical_reactor.controller.value": "：第%s层，正面中央",
}
en = {
    "tooltip.goldentweaks.chemical_reactor.controller.label": "Controller",
    "tooltip.goldentweaks.chemical_reactor.controller.value": ": layer %s, front centre",
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
