import io, json

# Labels keep only the label; the value key carries the number and its unit, the way "energy" does.
zh = {
    "gui.goldentweaks.jade.power": "机器功率：",
    "gui.goldentweaks.jade.power.value": "%s XFE / t",
    "gui.goldentweaks.jade.max_input": "最大输入：",
    "gui.goldentweaks.jade.max_input.value": "%s FE / t",
}
en = {
    "gui.goldentweaks.jade.power": "Machine power: ",
    "gui.goldentweaks.jade.power.value": "%s XFE/t",
    "gui.goldentweaks.jade.max_input": "Max input: ",
    "gui.goldentweaks.jade.max_input.value": "%s FE/t",
}

def patch(path, pairs):
    data = json.load(io.open(path, encoding='utf-8'))
    data.update(pairs)
    # Drop the half-written keys from the previous attempt.
    for stale in ("gui.goldentweaks.jade.unit_power", "gui.goldentweaks.jade.unit_input"):
        data.pop(stale, None)
    with io.open(path, 'w', encoding='utf-8', newline='\n') as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write('\n')
    print(path.split('/')[-1], len(data))

patch(r'src/main/resources/assets/goldentweaks/lang/zh_cn.json', zh)
patch(r'src/main/resources/assets/goldentweaks/lang/en_us.json', en)
