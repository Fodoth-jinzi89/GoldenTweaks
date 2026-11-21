import io, json

# "%s FE / t" and "%s FE/t" both become "%s FE/t": no space around the slash. The compact prefix
# (k, M, ...) is already inside the formatted number, so "100 k" + " FE/t" reads "100 kFE/t".
keys = {
    "goldentweaks.chemical_reaction.energy_per_tick": "耗能：%s FE/t",
    "gui.goldentweaks.chemical_reactor.energy_input": "最大输入：%s FE/t",
    "gui.goldentweaks.jade.power.value": "%s FE/t",
    "gui.goldentweaks.jade.max_input.value": "%s FE/t",
}
keys_en = {
    "goldentweaks.chemical_reaction.energy_per_tick": "Energy: %s FE/t",
    "gui.goldentweaks.chemical_reactor.energy_input": "Max input: %s FE/t",
    "gui.goldentweaks.jade.power.value": "%s FE/t",
    "gui.goldentweaks.jade.max_input.value": "%s FE/t",
}

def patch(path, pairs):
    data = json.load(io.open(path, encoding='utf-8'))
    data.update(pairs)
    with io.open(path, 'w', encoding='utf-8', newline='\n') as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write('\n')
    print(path.split('/')[-1], len(data))

patch(r'src/main/resources/assets/goldentweaks/lang/zh_cn.json', keys)
patch(r'src/main/resources/assets/goldentweaks/lang/en_us.json', keys_en)
