import json
import os
import argparse

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
    "creator",
    "arcanist",
    "alchemist"
]

parser = argparse.ArgumentParser()
parser.add_argument("--output-dir", default="output_professions")
parser.add_argument("professions", nargs="*")
args = parser.parse_args()

if args.professions:
    professions = args.professions

output_dir = args.output_dir
os.makedirs(output_dir, exist_ok=True)

unlinked_professions = {
    "mechanic", "wizard", "programmer", "adventurer", "pilot", "admiral",
    "astronaut", "photographer", "chemical_engineer", "painter", "musician",
    "bartender", "carpenter", "creator", "arcanist", "alchemist"
}

for i, prof in enumerate(professions):
    data = {
        "objectives": [f"{prof}_objs"],
        "rewards": ["golden_all_rews"]
    }

    # mechanic 之前保留 linkedProfessions
    if prof not in unlinked_professions:
        data["linkedProfessions"] = [prof]

    file_path = os.path.join(output_dir, f"{prof}.json")

    with open(file_path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=4)

    print(f"generated -> {file_path}")

print("done")
