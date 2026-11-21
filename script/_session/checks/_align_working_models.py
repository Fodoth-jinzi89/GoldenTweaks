import io, json, os

MODELS = r"src/main/resources/assets/goldentweaks/models/block/multiblock"


def load(name):
    return json.load(io.open(os.path.join(MODELS, name), encoding="utf-8"))


def save(name, data):
    with io.open(os.path.join(MODELS, name), "w", encoding="utf-8", newline="\n") as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write("\n")


# Each working variant takes its geometry and its arrow/name textures from the matching idle
# variant — those already carry the roll rotation and the mirrored reflection — and swaps only the
# overlay, which is what marks the machine as running.
pairs = [
    ("controller_working.json", "controller_idle.json"),
    ("controller_working_roll1.json", "controller_idle_roll1.json"),
    ("controller_working_roll2.json", "controller_idle_roll2.json"),
    ("controller_working_roll3.json", "controller_idle_roll3.json"),
    ("controller_working_mirrored.json", "controller_idle_mirrored.json"),
    ("controller_working_mirrored_roll1.json", "controller_idle_mirrored_roll1.json"),
    ("controller_working_mirrored_roll2.json", "controller_idle_mirrored_roll2.json"),
    ("controller_working_mirrored_roll3.json", "controller_idle_mirrored_roll3.json"),
]

running = load("controller_working.json")
overlay = running["textures"]["overlay"]

for target, source in pairs:
    data = load(source)
    data["textures"]["overlay"] = overlay
    # The running look draws an arrow where the idle look draws the name plate, so the second
    # overlay element follows the arrow, as the authored working model does.
    arrow = load("controller_working_mirrored.json"
                  if "mirrored" in target else "controller_working.json")["textures"]["arrow"]
    data["textures"]["arrow"] = arrow
    elements = data["elements"]
    if len(elements) >= 3:
        elements[2]["faces"]["north"]["texture"] = "#arrow"
    save(target, data)
    print("aligned:", target, "<-", source)

# Report what each variant now says.
for target, _ in pairs:
    data = load(target)
    print(target, "->", data["textures"].get("overlay"), "/", data["textures"].get("arrow"))
