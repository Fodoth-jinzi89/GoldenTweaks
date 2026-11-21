import io, json

TIERS = [
    ("ulv", "ULV", 1, "I"), ("lv", "LV", 2, "II"), ("mv", "MV", 3, "III"),
    ("hv", "HV", 4, "IV"), ("ev", "EV", 5, "V"), ("iv", "IV", 6, "VI"),
    ("luv", "LuV", 7, "VII"), ("zpm", "ZPM", 8, "VIII"), ("uv", "UV", 9, "IX"),
]
# The port's own noun, per kind and mode.
NOUNS = {
    ("item", "input"): ("输入总线", "Input Bus"),
    ("item", "output"): ("输出总线", "Output Bus"),
    ("fluid", "input"): ("输入仓", "Input Hatch"),
    ("fluid", "output"): ("输出仓", "Output Hatch"),
    ("energy", "input"): ("能源仓", "Energy Hatch"),
    ("energy", "output"): ("动力仓", "Dynamo Hatch"),
}

zh, en = {}, {}
obsolete = []
for kind in ["item", "fluid", "energy"]:
    for mode in ["input", "output"]:
        zh_name, en_name = NOUNS[(kind, mode)]
        # The tier-agnostic key is replaced by the nine per-tier ones.
        obsolete.append(f"block.goldentweaks.gt_port_{kind}_{mode}")
        for tier_id, code, level, roman in TIERS:
            key = f"block.goldentweaks.gt_port_{kind}_{mode}_{tier_id}"
            zh[key] = f"{roman} {code}{zh_name}"
            en[key] = f"{roman} {code} {en_name}"

# The previously generated energy-only names are superseded by the uniform scheme.
for tier_id, _code, _level, _roman in TIERS:
    for mode in ["input", "output"]:
        obsolete.append(f"block.goldentweaks.gt_port_energy_{mode}.tier_{_level}")

for path, extra in [
    (r"src/main/resources/assets/goldentweaks/lang/zh_cn.json", zh),
    (r"src/main/resources/assets/goldentweaks/lang/en_us.json", en),
]:
    data = json.load(io.open(path, encoding="utf-8"))
    for key in obsolete:
        data.pop(key, None)
    data.update(extra)
    with io.open(path, "w", encoding="utf-8", newline="\n") as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write("\n")
    print("lang updated", path)

for key in ["block.goldentweaks.gt_port_item_input_ulv",
            "block.goldentweaks.gt_port_fluid_output_ev",
            "block.goldentweaks.gt_port_energy_output_uv"]:
    print(key, "=>", zh[key])
