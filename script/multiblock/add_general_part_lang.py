"""Add the two general-assembly lines to the machine tooltips' part lists."""
import json
import os

LANG = r"E:\MCreator\Workspaces\GoldenTweaks\src\main\resources\assets\goldentweaks\lang"

ADDITIONS = {
    "en_us": {
        "tooltip.goldentweaks.chemical_reactor.part.general_input": "General Input Assembly",
        "tooltip.goldentweaks.chemical_reactor.part.general_output": "General Output Assembly",
    },
    "zh_cn": {
        "tooltip.goldentweaks.chemical_reactor.part.general_input": "输入总成",
        "tooltip.goldentweaks.chemical_reactor.part.general_output": "输出总成",
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
