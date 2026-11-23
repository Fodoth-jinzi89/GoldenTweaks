"""Add the two missing port-name translations.

GTPortItem builds its display name from `tooltip.goldentweaks.port.name.<kind>_<mode>`, and the
two new kinds had no entry — so a chemical hopper showed the raw key instead of a name.
"""
import io
import json

zh = {
    "tooltip.goldentweaks.port.name.chemical_input": "输入料斗",
    "tooltip.goldentweaks.port.name.chemical_output": "输出料斗",
    "tooltip.goldentweaks.port.name.essentia_input": "输入罐",
    "tooltip.goldentweaks.port.name.essentia_output": "输出罐",
}
en = {
    "tooltip.goldentweaks.port.name.chemical_input": "Input Hopper",
    "tooltip.goldentweaks.port.name.chemical_output": "Output Hopper",
    "tooltip.goldentweaks.port.name.essentia_input": "Input Tank",
    "tooltip.goldentweaks.port.name.essentia_output": "Output Tank",
}

def patch(path, pairs):
    data = json.load(io.open(path, encoding='utf-8'))
    data.update(pairs)
    with io.open(path, 'w', encoding='utf-8', newline='\n') as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write('\n')
    print(path.split('/')[-1], len(data))

patch(r'src/main/resources/assets/goldentweaks/lang/zh_cn.json', zh)
patch(r'src/main/resources/assets/goldentweaks/lang/en_us.json', en)
