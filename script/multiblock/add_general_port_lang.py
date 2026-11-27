"""Add the language keys the general assembly's tooltip and window need."""
import json
import os

LANG = r"E:\MCreator\Workspaces\GoldenTweaks\src\main\resources\assets\goldentweaks\lang"

ADDITIONS = {
    "en_us": {
        "gui.goldentweaks.port.bytes": "bytes",
        "gui.goldentweaks.port.types": "types",
        "gui.goldentweaks.port.kind.item": "items",
        "gui.goldentweaks.port.kind.fluid": "fluids",
        "gui.goldentweaks.port.kind.energy": "energy",
        "gui.goldentweaks.port.kind.chemical": "chemicals",
        "gui.goldentweaks.port.kind.essentia": "essentia",
        "tooltip.goldentweaks.port.types": "Types: ",
        "tooltip.goldentweaks.port.holds": "Holds: ",
        "tooltip.goldentweaks.port.name.general_input": "General Input Assembly",
        "tooltip.goldentweaks.port.name.general_output": "General Output Assembly",
    },
    "zh_cn": {
        "gui.goldentweaks.port.bytes": "字节",
        "gui.goldentweaks.port.types": "类型",
        "gui.goldentweaks.port.kind.item": "物品",
        "gui.goldentweaks.port.kind.fluid": "流体",
        "gui.goldentweaks.port.kind.energy": "能量",
        "gui.goldentweaks.port.kind.chemical": "化学品",
        "gui.goldentweaks.port.kind.essentia": "源质",
        "tooltip.goldentweaks.port.types": "类型数：",
        "tooltip.goldentweaks.port.holds": "容纳：",
        "tooltip.goldentweaks.port.name.general_input": "输入总成",
        "tooltip.goldentweaks.port.name.general_output": "输出总成",
    },
}


def main():
    for lang, additions in ADDITIONS.items():
        path = os.path.join(LANG, lang + ".json")
        with open(path, encoding="utf-8") as handle:
            data = json.load(handle)
        for key, value in additions.items():
            data[key] = value
        with open(path, "w", encoding="utf-8", newline="\n") as handle:
            json.dump(data, handle, indent=2, ensure_ascii=False)
            handle.write("\n")
        print("%s: %d keys" % (lang, len(data)))


if __name__ == "__main__":
    main()
