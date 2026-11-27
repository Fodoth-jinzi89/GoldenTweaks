"""Update the two language files for the IO assembly change.

Drops the four retired blocks' names and adds the general assemblies: one key per tier and
mode, plus the two caption words the assembly's window uses.
"""
import json
import os

ROOT = r"E:\MCreator\Workspaces\GoldenTweaks\src\main\resources"
LANG = os.path.join(ROOT, r"assets\goldentweaks\lang")

TIERS = [
    ("ulv", "ULV"), ("lv", "LV"), ("mv", "MV"), ("hv", "HV"), ("ev", "EV"),
    ("iv", "IV"), ("luv", "LuV"), ("zpm", "ZPM"), ("uv", "UV"), ("uhv", "UHV"),
    ("uev", "UEV"), ("uiv", "UIV"), ("umv", "UMV"), ("uxv", "UXV"), ("max", "MAX"),
]

RETIRED = ["input_bus", "auto_input_bus", "output_bus", "input_catalyst"]

# Per-language words for the new keys.
WORDS = {
    "en_us": {"name": "General %s Assembly", "in": "Input", "out": "Output",
              "bytes": "bytes", "types": "types", "cell_word": "types"},
    "zh_cn": {"name": "%s总成", "in": "输入", "out": "输出",
              "bytes": "字节", "types": "类型", "cell_word": "类型"},
}


def main():
    for lang in ("en_us", "zh_cn"):
        path = os.path.join(LANG, lang + ".json")
        with open(path, encoding="utf-8") as handle:
            data = json.load(handle)

        for name in RETIRED:
            data.pop("block.goldentweaks." + name, None)

        words = WORDS[lang]
        added = 0
        for tier_id, code in TIERS:
            for mode, word in (("input", words["in"]), ("output", words["out"])):
                key = "block.goldentweaks.gt_port_general_%s_%s" % (mode, tier_id)
                if key in data:
                    continue
                if lang == "en_us":
                    data[key] = "%s %s" % (code, words["name"] % word)
                else:
                    data[key] = "%s%s" % (code, words["name"] % word)
                added += 1

        data["gui.goldentweaks.port.bytes"] = (
            "bytes" if lang == "en_us" else "字节")
        data["gui.goldentweaks.port.types"] = (
            "types" if lang == "en_us" else "类型")
        data["tooltip.goldentweaks.port.types"] = (
            "Types: " if lang == "en_us" else "类型数：")
        data["tooltip.goldentweaks.port.name.general_input"] = (
            "General Input Assembly" if lang == "en_us" else "输入总成")
        data["tooltip.goldentweaks.port.name.general_output"] = (
            "General Output Assembly" if lang == "en_us" else "输出总成")

        with open(path, "w", encoding="utf-8", newline="\n") as handle:
            json.dump(data, handle, indent=2, ensure_ascii=False)
            handle.write("\n")

        print("%s: removed %d, added %d block keys, now %d keys"
              % (lang, len(RETIRED), added, len(data)))


if __name__ == "__main__":
    main()
