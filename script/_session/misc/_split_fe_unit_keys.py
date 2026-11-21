import io, json

# "%sFE" glued the figure to the unit unconditionally, which reads wrong for a bare number:
# "64FE/t". The space is decided by the formatter now — a compact figure attaches, a plain one gets
# a space — so the translation only supplies the unit itself.
UNIT = {"FE/t": "FE/t", "FE": "FE"}

zh = {
    "goldentweaks.chemical_reaction.total_energy": "总能量：%s%s",
    "goldentweaks.chemical_reaction.energy_per_tick": "耗能：%s%s",
    "gui.goldentweaks.chemical_reactor.energy": "%s%s/%s%s",
    "gui.goldentweaks.chemical_reactor.energy_input": "最大输入：%s%s",
    "gui.goldentweaks.jade.energy": "%s%s/%s%s",
    "gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch": "此配方需要更高的功率：%s%s",
    "gui.goldentweaks.jade.power.value": "%s%s",
    "gui.goldentweaks.jade.max_input.value": "%s%s",
    "gui.goldentweaks.port.energy.stored_value": "%s%s/%s%s",
    "gui.goldentweaks.port.energy.rate_value": "%s%s",
}
en = {
    "goldentweaks.chemical_reaction.total_energy": "Total energy: %s%s",
    "goldentweaks.chemical_reaction.energy_per_tick": "Energy: %s%s",
    "gui.goldentweaks.chemical_reactor.energy": "%s%s/%s%s",
    "gui.goldentweaks.chemical_reactor.energy_input": "Max input: %s%s",
    "gui.goldentweaks.jade.energy": "%s%s/%s%s",
    "gui.goldentweaks.chemical_reactor.status.insufficient_energy_hatch": "This recipe needs more power: %s%s",
    "gui.goldentweaks.jade.power.value": "%s%s",
    "gui.goldentweaks.jade.max_input.value": "%s%s",
    "gui.goldentweaks.port.energy.stored_value": "%s%s/%s%s",
    "gui.goldentweaks.port.energy.rate_value": "%s%s",
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
print("unit keys keep their own spelling:", UNIT)
