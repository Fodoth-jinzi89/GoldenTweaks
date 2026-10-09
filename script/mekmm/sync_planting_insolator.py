"""双向同步「有机灌注器」(thermal:insolator) 与「种植站」(mekmm:planting) 的配方。

- 正向：以种植站为中心，为每条 planting 配方生成对应的 insolator 配方（数量对齐，并返还种子）。
- 反向：把只存在于灌注器里的种子补一份 planting 配方。
- 全量重写：先删除所有旧的 te_insolator 目录，再按种植站重新生成。
"""
import json
import shutil
from pathlib import Path

DATA = Path(__file__).parents[2] / "src/main/resources/data"
PLANTING_DIR = DATA / "mekmm/recipe/planting"
INSOLATOR_DIRNAME = "te_insolator"

SEED_CHANCE = 1.1
WATER_MOD = 0.5
DEFAULT_COUNT = 4


def mod_loaded(modid):
    return {"type": "neoforge:mod_loaded", "modid": modid}


def condition_modid(recipe, default):
    for c in recipe.get("neoforge:conditions", []):
        if c.get("type") == "neoforge:mod_loaded" and c.get("modid"):
            return c["modid"]
    return default


def load_planting():
    entries = []
    for path in sorted(PLANTING_DIR.rglob("*.json")):
        data = json.loads(path.read_text(encoding="utf-8-sig"))
        seed = data["item_input"]["item"]
        entries.append({
            "seed": seed,
            "produce": data["main_output"]["id"],
            "produce_count": data["main_output"].get("count", 1),
            "secondary": data.get("secondary_output", {}).get("id"),
            "secondary_count": data.get("secondary_output", {}).get("count", 1),
            "modid": condition_modid(data, seed.split(":", 1)[0]),
            "stem": path.stem,
            "ns": seed.split(":", 1)[0],
        })
    return entries


def load_insolator():
    entries = []
    for path in sorted(DATA.glob(f"*/recipe/{INSOLATOR_DIRNAME}/*.json")):
        data = json.loads(path.read_text(encoding="utf-8-sig"))
        results = data.get("result", [])
        if not results:
            continue
        entries.append({
            "seed": data["ingredient"]["item"],
            "produce": results[0]["item"],
        })
    return entries


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def build_insolator(entry):
    result = [{"item": entry["produce"], "chance": float(entry["produce_count"])}]
    secondary = entry.get("secondary")
    if secondary and secondary != entry["produce"]:
        result.append({"item": secondary, "chance": float(entry["secondary_count"])})
    if entry["seed"] not in (entry["produce"], secondary):
        result.append({"item": entry["seed"], "chance": SEED_CHANCE})
    return {
        "type": "thermal:insolator",
        "ingredient": {"item": entry["seed"]},
        "result": result,
        "neoforge:conditions": [mod_loaded("thermal_expansion"), mod_loaded(entry["modid"])],
        "water_mod": WATER_MOD,
    }


def build_planting(entry):
    data = {
        "neoforge:conditions": [mod_loaded(entry["modid"])],
        "type": "mekmm:planting",
        "item_input": {"count": 1, "item": entry["seed"]},
        "chemical_input": {"amount": 1, "chemical": "mekmm:nutrient_solution"},
        "main_output": {"count": entry["produce_count"], "id": entry["produce"]},
        "per_tick_usage": True,
    }
    if entry.get("secondary"):
        data["secondary_output"] = {"count": entry["secondary_count"], "id": entry["secondary"]}
        data["secondary_chance"] = 0.8
    return data


def main():
    planting = load_planting()
    insolator = load_insolator()

    seen = {e["seed"] for e in planting}
    added_planting = []
    for e in insolator:
        if e["seed"] in seen:
            continue
        seen.add(e["seed"])
        ns = e["seed"].split(":", 1)[0]
        added_planting.append({
            "seed": e["seed"],
            "produce": e["produce"],
            "produce_count": DEFAULT_COUNT,
            "secondary": None,
            "secondary_count": 1,
            "modid": ns,
            "stem": e["seed"].split(":", 1)[1],
            "ns": ns,
        })

    # 全量重写：删掉所有旧 te_insolator 目录
    removed = 0
    for d in DATA.glob(f"*/recipe/{INSOLATOR_DIRNAME}"):
        removed += len(list(d.glob("*.json")))
        shutil.rmtree(d)

    for e in planting:
        write_json(DATA / e["ns"] / "recipe" / INSOLATOR_DIRNAME / f"insolator_{e['stem']}.json", build_insolator(e))

    for e in added_planting:
        write_json(PLANTING_DIR / e["ns"] / f"{e['stem']}.json", build_planting(e))
        write_json(DATA / e["ns"] / "recipe" / INSOLATOR_DIRNAME / f"insolator_{e['stem']}.json", build_insolator(e))

    print(f"planting entries: {len(planting)}")
    print(f"insolator entries read: {len(insolator)}")
    print(f"planting added from insolator-only seeds: {len(added_planting)}")
    print(f"old insolator files removed: {removed}")
    print(f"insolator written: {len(planting) + len(added_planting)}")


if __name__ == "__main__":
    main()
