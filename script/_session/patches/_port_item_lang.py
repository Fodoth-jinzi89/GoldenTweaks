import io, json

zh = {
    # Part names without the voltage prefix: the prefix is written by the item in the tier's colour.
    "tooltip.goldentweaks.port.name.item_input": "输入总线",
    "tooltip.goldentweaks.port.name.item_output": "输出总线",
    "tooltip.goldentweaks.port.name.fluid_input": "输入仓",
    "tooltip.goldentweaks.port.name.fluid_output": "输出仓",
    "tooltip.goldentweaks.port.name.energy_input": "能源仓",
    "tooltip.goldentweaks.port.name.energy_output": "动力仓",

    "tooltip.goldentweaks.port.capacity": "容量：",
    "tooltip.goldentweaks.port.cells": "格数：",
    "tooltip.goldentweaks.port.max_input": "最大输入：",
    "tooltip.goldentweaks.port.max_output": "最大输出：",
    "tooltip.goldentweaks.port.controls": "内置构件：",
    "tooltip.goldentweaks.port.controls.single_cell": "单格模式",
    "tooltip.goldentweaks.port.controls.auto_io": "自动IO",
}
en = {
    "tooltip.goldentweaks.port.name.item_input": "Input Bus",
    "tooltip.goldentweaks.port.name.item_output": "Output Bus",
    "tooltip.goldentweaks.port.name.fluid_input": "Input Hatch",
    "tooltip.goldentweaks.port.name.fluid_output": "Output Hatch",
    "tooltip.goldentweaks.port.name.energy_input": "Energy Hatch",
    "tooltip.goldentweaks.port.name.energy_output": "Dynamo Hatch",

    "tooltip.goldentweaks.port.capacity": "Capacity: ",
    "tooltip.goldentweaks.port.cells": "Cells: ",
    "tooltip.goldentweaks.port.max_input": "Max input: ",
    "tooltip.goldentweaks.port.max_output": "Max output: ",
    "tooltip.goldentweaks.port.controls": "Built-in components:",
    "tooltip.goldentweaks.port.controls.single_cell": "Single-cell mode",
    "tooltip.goldentweaks.port.controls.auto_io": "Auto IO",
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
