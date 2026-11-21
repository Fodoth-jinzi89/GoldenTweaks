import io, json

TIERS = ["ulv", "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv"]
NOUNS = {
    ("item", "input"): ("输入总线", "Input Bus"),
    ("item", "output"): ("输出总线", "Output Bus"),
    ("fluid", "input"): ("输入仓", "Input Hatch"),
    ("fluid", "output"): ("输出仓", "Output Hatch"),
    ("energy", "input"): ("能源仓", "Energy Hatch"),
    ("energy", "output"): ("动力仓", "Dynamo Hatch"),
}
# Code as it is written on the item, per tier id.
CODES = {
    "ulv": "ULV", "lv": "LV", "mv": "MV", "hv": "HV", "ev": "EV",
    "iv": "IV", "luv": "LuV", "zpm": "ZPM", "uv": "UV",
}

zh, en = {}, {}
for kind in ["item", "fluid", "energy"]:
    for mode in ["input", "output"]:
        zh_name, en_name = NOUNS[(kind, mode)]
        for tier in TIERS:
            key = f"block.goldentweaks.gt_port_{kind}_{mode}_{tier}"
            # Code then the port's own noun, nothing else: the tier is already stated by the code.
            zh[key] = f"{CODES[tier]}{zh_name}"
            en[key] = f"{CODES[tier]} {en_name}"

for path, extra in [
    (r"src/main/resources/assets/goldentweaks/lang/zh_cn.json", zh),
    (r"src/main/resources/assets/goldentweaks/lang/en_us.json", en),
]:
    data = json.load(io.open(path, encoding="utf-8"))
    data.update(extra)
    with io.open(path, "w", encoding="utf-8", newline="\n") as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write("\n")
    print("lang updated", path)

for key in ["block.goldentweaks.gt_port_item_input_ulv",
            "block.goldentweaks.gt_port_fluid_output_ev",
            "block.goldentweaks.gt_port_energy_output_uv"]:
    print(key, "=>", zh[key])
