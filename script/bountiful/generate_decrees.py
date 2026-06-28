import json
import os

professions = [
    "ae2_specialist",
    "ae2_engineer",
    "ae2_toolsmith",
    "fluix_researcher",
    "armorer",
    "butcher",
    "mapper",
    "chef",
    "priest",
    "farmer",
    "fisherman",
    "fletcher",
    "leatherer",
    "librarian",
    "mason",
    "pistrinamaster",
    "shepherd",
    "toolsmith",
    "weaponsmith",
    "mechanic",
    "wizard",
    "programmer",
    "adventurer",
    "pilot",
    "admiral",
    "astronaut",
    "photographer",
    "chemical_engineer",
    "painter",
    "musician",
    "bartender",
    "carpenter",
    "creator"
]

output_dir = "output_professions"
os.makedirs(output_dir, exist_ok=True)

split_index = professions.index("mechanic")

for i, prof in enumerate(professions):
    data = {
        "objectives": [f"{prof}_objs"],
        "rewards": ["golden_all_rews"]
    }

    # mechanic 之前保留 linkedProfessions
    if i < split_index:
        data["linkedProfessions"] = [prof]

    file_path = os.path.join(output_dir, f"{prof}.json")

    with open(file_path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=4)

    print(f"generated -> {file_path}")

print("done")