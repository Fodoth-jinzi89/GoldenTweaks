import io, json

zh = {
    "tooltip.goldentweaks.chemical_reactor.kind.label": "机器类型：",
    "tooltip.goldentweaks.chemical_reactor.kind.value": "化学反应釜",
    "tooltip.goldentweaks.chemical_reactor.controls": "内置控件：",
    "tooltip.goldentweaks.chemical_reactor.controls.list": "锁定配方 溢出销毁 批量模式 输入隔离",
    "tooltip.goldentweaks.chemical_reactor.circuit.overclock": "4/2超频",
    "tooltip.goldentweaks.chemical_reactor.circuit.parallel": "1并行",
    "tooltip.goldentweaks.chemical_reactor.shift": "按住",
    "tooltip.goldentweaks.chemical_reactor.shift.key": "[shift]",
    "tooltip.goldentweaks.chemical_reactor.shift.suffix": "查看多方块结构信息",
    "tooltip.goldentweaks.chemical_reactor.structure": "多方块结构：",
    "tooltip.goldentweaks.chemical_reactor.structure.any": "（任意聚乙烯机壳）",
    "tooltip.goldentweaks.chemical_reactor.boost": "支持双仓升压",
    "tooltip.goldentweaks.chemical_reactor.hint": "请用多方块结构投影仪搭建",
    # Names for the structure lines. Not the block names: those say "外壳", while the structure
    # breakdown is written with "机壳", and it also needs the bare part names.
    "tooltip.goldentweaks.chemical_reactor.casing": "聚乙烯机壳",
    "tooltip.goldentweaks.chemical_reactor.part.input_bus": "输入总线",
    "tooltip.goldentweaks.chemical_reactor.part.fluid_input": "输入仓",
    "tooltip.goldentweaks.chemical_reactor.part.output_bus": "输出总线",
    "tooltip.goldentweaks.chemical_reactor.part.fluid_output": "输出仓",
    "tooltip.goldentweaks.chemical_reactor.part.energy_input": "能源仓",
    "message.goldentweaks.chemical_reactor.not_enough_casing": "结构缺少机壳：至少需要 %s 个聚乙烯机壳",
    "message.goldentweaks.chemical_reactor.no_dynamo": "该机器不支持动力仓",
}
en = {
    "tooltip.goldentweaks.chemical_reactor.kind.label": "Machine type: ",
    "tooltip.goldentweaks.chemical_reactor.kind.value": "Chemical Reaction Kettle",
    "tooltip.goldentweaks.chemical_reactor.controls": "Built-in controls:",
    "tooltip.goldentweaks.chemical_reactor.controls.list": "Lock Recipe / Void Excess / Batch Mode / Isolate Input",
    "tooltip.goldentweaks.chemical_reactor.circuit.overclock": "4/2 Overclock",
    "tooltip.goldentweaks.chemical_reactor.circuit.parallel": "1 Parallel",
    "tooltip.goldentweaks.chemical_reactor.shift": "Hold ",
    "tooltip.goldentweaks.chemical_reactor.shift.key": "[shift]",
    "tooltip.goldentweaks.chemical_reactor.shift.suffix": " for multiblock structure",
    "tooltip.goldentweaks.chemical_reactor.structure": "Multiblock structure:",
    "tooltip.goldentweaks.chemical_reactor.structure.any": "(any PE casing)",
    "tooltip.goldentweaks.chemical_reactor.boost": "Supports dual-hatch boost",
    "tooltip.goldentweaks.chemical_reactor.hint": "Build it with the multiblock structure projector",
    "tooltip.goldentweaks.chemical_reactor.casing": "PE Casing",
    "tooltip.goldentweaks.chemical_reactor.part.input_bus": "Input Bus",
    "tooltip.goldentweaks.chemical_reactor.part.fluid_input": "Input Hatch",
    "tooltip.goldentweaks.chemical_reactor.part.output_bus": "Output Bus",
    "tooltip.goldentweaks.chemical_reactor.part.fluid_output": "Output Hatch",
    "tooltip.goldentweaks.chemical_reactor.part.energy_input": "Energy Hatch",
    "message.goldentweaks.chemical_reactor.not_enough_casing":
        "The structure needs at least %s PE casings",
    "message.goldentweaks.chemical_reactor.no_dynamo": "This machine does not accept dynamo hatches",
}

def patch(path, pairs):
    data = json.load(io.open(path, encoding='utf-8'))
    before = len(data)
    data.update(pairs)
    with io.open(path, 'w', encoding='utf-8', newline='\n') as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write('\n')
    print(path.split('/')[-1], before, '->', len(data))

patch(r'src/main/resources/assets/goldentweaks/lang/zh_cn.json', zh)
patch(r'src/main/resources/assets/goldentweaks/lang/en_us.json', en)
