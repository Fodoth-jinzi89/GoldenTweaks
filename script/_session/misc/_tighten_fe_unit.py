import io, json, re

# No space anywhere between a figure and its unit: "%s FE/t" -> "%sFE/t", "%s FE / %s FE" ->
# "%sFE/%sFE". The compact prefix (k, M, ...) is already part of the formatted number, so the
# result reads "100 kFE/t" as one token.
zh = {
    "goldentweaks.chemical_reaction.total_energy": "总能量：%sFE",
    "goldentweaks.chemical_reaction.energy_per_tick": "耗能：%sFE/t",
    "gui.goldentweaks.chemical_reactor.energy": "能量：%sFE/%sFE",
    "gui.goldentweaks.chemical_reactor.energy_input": "最大输入：%sFE/t",
    "gui.goldentweaks.jade.energy": "%s/%sFE",
    "gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch": "此配方需要更高的功率：%sFE/t",
    "gui.goldentweaks.jade.power.value": "%sFE/t",
    "gui.goldentweaks.jade.max_input.value": "%sFE/t",
}
en = {
    "goldentweaks.chemical_reaction.total_energy": "Total energy: %sFE",
    "goldentweaks.chemical_reaction.energy_per_tick": "Energy: %sFE/t",
    "gui.goldentweaks.chemical_reactor.energy": "Energy: %sFE/%sFE",
    "gui.goldentweaks.chemical_reactor.energy_input": "Max input: %sFE/t",
    "gui.goldentweaks.jade.energy": "%s/%sFE",
    "gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch": "This recipe needs more power: %sFE/t",
    "gui.goldentweaks.jade.power.value": "%sFE/t",
    "gui.goldentweaks.jade.max_input.value": "%sFE/t",
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
