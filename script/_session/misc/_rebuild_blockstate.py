import io, json

BLOCKSTATE = r"src/main/resources/assets/goldentweaks/blockstates/chemical_plant_controller.json"
data = json.load(io.open(BLOCKSTATE, encoding="utf-8"))

variants = {}
for facing in ["north", "east", "south", "west", "up", "down"]:
    rotation = {"north": {}, "east": {"y": 90}, "south": {"y": 180},
                "west": {"y": 270}, "up": {"x": 270}, "down": {"x": 90}}[facing]
    for mirrored in ["false", "true"]:
        suffix = "_mirrored" if mirrored == "true" else ""
        for roll in range(4):
            rollSuffix = f"_roll{roll}" if roll else ""
            for running in ["false", "true"]:
                for crafting in ["false", "true"]:
                    # Four looks, and the order they are judged in matters:
                    #   unformed            -> unconstructed
                    #   formed + crafting   -> working
                    #   formed + switch on  -> idle
                    #   formed + switch off -> off
                    formed = "true"
                    if crafting == "true":
                        base = "controller_working"
                    elif running == "true":
                        base = "controller_idle"
                    else:
                        base = "controller_off"
                    model = f"{base}{suffix}{rollSuffix}"
                    entry = {"model": f"goldentweaks:block/multiblock/{model}"}
                    entry.update(rotation)
                    variants[f"facing={facing},mirrored={mirrored},running={running},"
                             f"crafting={crafting},roll={roll},formed={formed}"] = entry

            # The unformed shape ignores every other flag.
            for running in ["false", "true"]:
                for crafting in ["false", "true"]:
                    entry = {"model": f"goldentweaks:block/multiblock/controller_unconstructed{suffix}{rollSuffix}"}
                    entry.update(rotation)
                    variants[f"facing={facing},mirrored={mirrored},running={running},"
                             f"crafting={crafting},roll={roll},formed=false"] = entry

data["variants"] = variants
with io.open(BLOCKSTATE, "w", encoding="utf-8", newline="\n") as h:
    json.dump(data, h, indent=2, ensure_ascii=False)
    h.write("\n")

print("variants:", len(variants))

seen = {}
for k, v in variants.items():
    state = tuple(p for p in k.split(",") if p.startswith(("crafting", "running", "formed")))
    seen.setdefault(state, set()).add(v["model"].split("/")[-1].replace("_mirrored", "").split("_roll")[0])
for s, m in sorted(seen.items()):
    print(s, "->", sorted(m))
