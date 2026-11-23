"""The tooltip lists the two extra port kinds the advanced kettle accepts.

Hopper and canister ports now fit both machines, so the structure summary has to name them —
otherwise a player reading the list would think only buses and tanks were allowed.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTChemicalReactorItem.java"
s = io.open(p, encoding="utf-8").read()

old = '''            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.input_bus"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.fluid_input"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.output_bus"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.fluid_output"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.energy_input"));'''
new = '''            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.input_bus"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.fluid_input"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.chemical_input"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.essentia_input"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.output_bus"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.fluid_output"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.chemical_output"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.essentia_output"),
            new Part(0, "tooltip.goldentweaks.chemical_reactor.part.energy_input"));'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("tooltip lists hopper and canister ports")

# ---- language keys ---------------------------------------------------------------------------
import json, collections
for path, values in [
    (r"src/main/resources/assets/goldentweaks/lang/zh_cn.json", {
        "tooltip.goldentweaks.chemical_reactor.part.chemical_input": "输入料斗",
        "tooltip.goldentweaks.chemical_reactor.part.chemical_output": "输出料斗",
        "tooltip.goldentweaks.chemical_reactor.part.essentia_input": "输入罐",
        "tooltip.goldentweaks.chemical_reactor.part.essentia_output": "输出罐",
    }),
    (r"src/main/resources/assets/goldentweaks/lang/en_us.json", {
        "tooltip.goldentweaks.chemical_reactor.part.chemical_input": "Input Hopper",
        "tooltip.goldentweaks.chemical_reactor.part.chemical_output": "Output Hopper",
        "tooltip.goldentweaks.chemical_reactor.part.essentia_input": "Input Canister",
        "tooltip.goldentweaks.chemical_reactor.part.essentia_output": "Output Canister",
    }),
]:
    data = json.load(io.open(path, encoding="utf-8"), object_pairs_hook=collections.OrderedDict)
    anchor = "tooltip.goldentweaks.chemical_reactor.part.fluid_input"
    out = collections.OrderedDict()
    for k, v in data.items():
        out[k] = v
        if k == anchor:
            out["tooltip.goldentweaks.chemical_reactor.part.chemical_input"] = values[
                "tooltip.goldentweaks.chemical_reactor.part.chemical_input"]
            out["tooltip.goldentweaks.chemical_reactor.part.essentia_input"] = values[
                "tooltip.goldentweaks.chemical_reactor.part.essentia_input"]
    anchor2 = "tooltip.goldentweaks.chemical_reactor.part.fluid_output"
    final = collections.OrderedDict()
    for k, v in out.items():
        final[k] = v
        if k == anchor2:
            final["tooltip.goldentweaks.chemical_reactor.part.chemical_output"] = values[
                "tooltip.goldentweaks.chemical_reactor.part.chemical_output"]
            final["tooltip.goldentweaks.chemical_reactor.part.essentia_output"] = values[
                "tooltip.goldentweaks.chemical_reactor.part.essentia_output"]
    io.open(path, "w", encoding="utf-8", newline="\n").write(
        json.dumps(final, indent=2, ensure_ascii=False) + "\n")
    print("lang updated:", path.split("/")[-1])
