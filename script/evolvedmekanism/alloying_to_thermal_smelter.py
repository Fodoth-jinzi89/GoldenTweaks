"""将 evolvedmekanism:alloying（合金炉）的配方转换为 thermal:smelter（感应炉）配方。

- 来源：本项目 data/evolvedmekanism/recipe/alloying/ 与 evolvedmekanism jar 内的同名目录
  （项目内的文件覆盖 jar 内的同路径文件）。
- 去重：产物物品已存在于机械感应炉（thermal:smelter）配方中的跳过。
- 输出：src/main/resources/data/evolvedmekanism/recipe/thermal_smelter/<mod>/<name>.json
"""
import json
import shutil
import zipfile
from pathlib import Path

ROOT = Path(__file__).parents[2]
DATA = ROOT / "src/main/resources/data"
ALLOYING = DATA / "evolvedmekanism/recipe/alloying"
OUT = DATA / "evolvedmekanism/recipe/thermal_smelter"
LIBS = ROOT / "libs"

JAR_PREFIX = "data/evolvedmekanism/recipe/alloying/"


def find_evolvedmekanism_jar():
    for path in LIBS.rglob("*.jar"):
        if "evolvedmekanism" in path.name.lower():
            return path
    raise FileNotFoundError("libs/ 下未找到 evolvedmekanism 的 jar")


def load_alloying():
    recipes = {}
    for path in ALLOYING.rglob("*.json"):
        data = json.loads(path.read_text(encoding="utf-8-sig"))
        if data.get("type") == "evolvedmekanism:alloying":
            recipes[path.relative_to(ALLOYING).as_posix()] = data

    with zipfile.ZipFile(find_evolvedmekanism_jar()) as jar:
        for name in jar.namelist():
            if not (name.startswith(JAR_PREFIX) and name.endswith(".json")):
                continue
            rel = name[len(JAR_PREFIX):]
            if rel in recipes:
                continue
            data = json.loads(jar.read(name).decode("utf-8-sig"))
            if data.get("type") == "evolvedmekanism:alloying":
                recipes[rel] = data
    return recipes


def load_smelter_outputs():
    outputs = set()

    def scan(data):
        if isinstance(data, dict) and data.get("type") == "thermal:smelter":
            for result in data.get("result", []):
                if "item" in result:
                    outputs.add(result["item"])

    for jar_path in LIBS.rglob("*.jar"):
        try:
            jar = zipfile.ZipFile(jar_path)
        except zipfile.BadZipFile:
            continue
        try:
            for name in jar.namelist():
                if not (name.endswith(".json") and "/recipe/" in name):
                    continue
                try:
                    scan(json.loads(jar.read(name).decode("utf-8-sig")))
                except (ValueError, UnicodeDecodeError):
                    pass
        finally:
            jar.close()

    for path in DATA.rglob("*.json"):
        try:
            scan(json.loads(path.read_text(encoding="utf-8-sig")))
        except (ValueError, UnicodeDecodeError):
            pass
    return outputs


def sized_ingredient(entry):
    ingredient = {}
    if "tag" in entry:
        ingredient["tag"] = entry["tag"]
    elif "item" in entry:
        ingredient["item"] = entry["item"]
    count = entry.get("count", entry.get("amount", 1))
    if count != 1:
        ingredient["count"] = count
    return ingredient


def convert(recipe):
    ingredients = []
    for key in ("main_input", "extra_input", "second_extra_input"):
        entry = recipe.get(key)
        if entry:
            ingredients.append(sized_ingredient(entry))

    output = recipe["output"]
    result = {"item": output["id"]}
    if output.get("count", 1) != 1:
        result["count"] = output["count"]

    conditions = [{"type": "neoforge:mod_loaded", "modid": "thermal_expansion"}]
    raw = list(recipe.get("neoforge:conditions", []))
    raw += list(recipe.get("conditions", []))
    for condition in raw:
        if condition not in conditions:
            conditions.append(condition)

    return {
        "neoforge:conditions": conditions,
        "type": "thermal:smelter",
        "ingredients": ingredients,
        "result": [result],
    }


def main():
    if OUT.exists():
        shutil.rmtree(OUT)

    alloying = load_alloying()
    existing = load_smelter_outputs()
    seen = set(existing)

    written = 0
    skipped = []
    for rel, recipe in sorted(alloying.items()):
        output = recipe["output"]["id"]
        if output in seen:
            skipped.append((rel, output))
            continue
        seen.add(output)

        parts = rel.split("/")
        if parts[0] == "compat":
            parts = parts[1:]
        dest = OUT.joinpath(*parts)
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_text(
            json.dumps(convert(recipe), ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8",
        )
        written += 1

    print(f"alloying recipes: {len(alloying)}")
    print(f"pre-existing smelter outputs: {len(existing)}")
    print(f"skipped duplicates: {len(skipped)}")
    for rel, output in skipped:
        print(f"  - {rel} -> {output}")
    print(f"written: {written}")


if __name__ == "__main__":
    main()
