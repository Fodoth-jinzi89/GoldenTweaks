import io, json

# formatWithUnit already appends the unit, so the translation must not add another one.
zh = {
    "gui.goldentweaks.chemical_reactor.energy": "%s/%s",
    "gui.goldentweaks.jade.energy": "%s/%s",
    "gui.goldentweaks.port.energy.stored_value": "%s/%s",
    "gui.goldentweaks.port.energy.rate_value": "%s",
    "goldentweaks.chemical_reaction.total_energy": "总能量：%s",
    "goldentweaks.chemical_reaction.energy_per_tick": "耗能：%s",
    "gui.goldentweaks.chemical_reactor.energy_input": "最大输入：%s",
    "gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch": "此配方需要更高的功率：%s",
    "gui.goldentweaks.jade.power.value": "%s",
    "gui.goldentweaks.jade.max_input.value": "%s",
}
en = {
    "gui.goldentweaks.chemical_reactor.energy": "%s/%s",
    "gui.goldentweaks.jade.energy": "%s/%s",
    "gui.goldentweaks.port.energy.stored_value": "%s/%s",
    "gui.goldentweaks.port.energy.rate_value": "%s",
    "goldentweaks.chemical_reaction.total_energy": "Total energy: %s",
    "goldentweaks.chemical_reaction.energy_per_tick": "Energy: %s",
    "gui.goldentweaks.chemical_reactor.energy_input": "Max input: %s",
    "gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch": "This recipe needs more power: %s",
    "gui.goldentweaks.jade.power.value": "%s",
    "gui.goldentweaks.jade.max_input.value": "%s",
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
