import json
import re
from pathlib import Path


ROOT = Path(__file__).parents[2]
INPUT = ROOT / "需添加要素的物品.txt"
RESOURCE_ROOT = ROOT / "script/resource_locations/output"
ASPECT_ROOT = ROOT / "src/main/resources/data/goldentweaks/recipe/thaumcraft/aspects"

ASPECTS = {
    "熔岩": "lava", "物质": "substance", "植被": "vegetatio", "重力": "gravitas",
    "治疗": "sano", "不朽": "alfirin", "率启": "arche", "暗能量": "atrpotentia",
    "邃窟": "spelunca", "宇宙": "universes", "灵气": "auram", "磁力": "magnetis",
    "龙": "dragon", "飞岛": "laputa", "合金": "mixtura", "遗蜕": "priscus",
    "傲慢": "superbia", "黑魔法": "atrpraecantatic", "化石": "fossil", "怠惰": "desidia",
    "欲望": "luxuria", "梦想": "dream", "膨胀": "expand", "妒忌": "invidia",
    "统御": "imperium", "往古": "anteanus", "童趣": "childish", "历史": "history",
    "终结": "terminus", "邪恶": "evil", "乌鲁克": "mornogol", "财富": "treasure",
    "暴怒": "ira",
}

KEYWORDS = {
    "sano": ("heal", "health", "medicine", "medical", "regeneration", "life", "food", "meal", "stew", "soup"),
    "alfirin": ("immortal", "infinity", "eternal", "everlasting", "undying", "regeneration", "catalyst"),
    "arche": ("genesis", "origin", "creation", "creative", "primordial", "first", "protoclay"),
    "atrpotentia": ("dark_energy", "void_energy", "antimatter", "black_hole", "negative", "singularity"),
    "spelunca": ("cave", "cavern", "underground", "deepslate", "stalact", "grotto", "abyss", "deep"),
    "universes": ("cosmic", "universe", "infinity", "star", "stellar", "celestial", "astral", "galaxy"),
    "auram": ("aura", "wisp", "silverwood", "shimmer", "ethereal", "spirit", "soul", "essence"),
    "magnetis": ("magnet", "magnetic", "lodestone", "compass", "electromagnet"),
    "dragon": ("dragon", "draconic", "wyrm", "drake"),
    "laputa": ("sky", "cloud", "floating", "airship", "aerial", "aero", "flight", "levitat", "meteor"),
    "mixtura": ("alloy", "brass", "bronze", "steel", "invar", "electrum", "constantan", "signalum", "lumium"),
    "priscus": ("ancient", "old", "relic", "fossil", "remnant", "primordial", "timeworn"),
    "superbia": ("pride", "royal", "crown", "gold", "star", "supreme", "divine"),
    "atrpraecantatic": ("dark_magic", "black_magic", "forbidden", "occult", "cursed", "tainted", "eldritch"),
    "fossil": ("fossil", "ancient", "bone", "skeleton", "archae", "relic", "remnant"),
    "desidia": ("sloth", "lazy", "sleep", "idle", "slow", "bed", "chair"),
    "luxuria": ("lust", "luxury", "love", "charm", "rose", "perfume", "silk", "jewel"),
    "dream": ("dream", "sleep", "bed", "pillow", "nightmare", "moon", "lunar"),
    "expand": ("expand", "growth", "large", "giant", "colossal", "compression", "compressed", "dense"),
    "invidia": ("envy", "jealous", "eye", "emerald", "green"),
    "imperium": ("crown", "banner", "command", "controller", "dominion", "royal", "king", "leader"),
    "anteanus": ("ancient", "timeworn", "relic", "fossil", "archae", "primordial", "old"),
    "childish": ("child", "toy", "doll", "plush", "candy", "balloon", "crayon", "game"),
    "history": ("ancient", "history", "relic", "archae", "timeworn", "tablet", "tome", "manuscript"),
    "terminus": ("end", "final", "ultimate", "infinity", "neutron", "crystal_matrix", "singularity"),
    "evil": ("evil", "cursed", "demon", "sinister", "dark", "taint", "corrupt", "forbidden", "wrath"),
    "mornogol": ("ancient", "remnant", "relic", "primordial", "eldritch", "uruk", "orc"),
    "treasure": ("treasure", "loot", "gold", "diamond", "emerald", "gem", "jewel", "coin"),
    "ira": ("wrath", "rage", "anger", "fury", "flame", "fire", "blaze", "weapon", "sword", "axe", "hammer"),
}

PRODUCE = (
    "apple", "apricot", "avocado", "banana", "bean", "beet", "berry", "cabbage", "carrot", "cherry",
    "corn", "cowpea", "cucumber", "eggplant", "garlic", "grape", "jujube", "lemon", "lettuce", "lime",
    "loofah", "melon", "mungbean", "onion", "orange", "peach", "pear", "pepper", "pineapple", "plum",
    "potato", "pumpkin", "radish", "rapeseed", "soybean", "spinach", "strawberry", "tomato", "waxgourd",
    "watermelon", "zucchini", "aubergine",
)


def load_resources():
    resources = {}
    for path in RESOURCE_ROOT.glob("*.json"):
        data = json.loads(path.read_text(encoding="utf-8-sig"))
        for kind in ("items", "blocks"):
            resources.update(data.get(kind, {}))
    return resources


def sections(text):
    found = []
    for name in ASPECTS:
        match = re.search(rf"(?m)^{re.escape(name)}\s*$", text)
        if match:
            found.append((match.start(), name))
    found.sort()
    for index, (start, name) in enumerate(found):
        end = found[index + 1][0] if index + 1 < len(found) else len(text)
        yield name, text[start:end]


def container_items(section):
    items = set()
    for slot in re.finditer(r"\{Slot:\s*\d+,(.*?)(?=\},\s*\{Slot:|\}\]\})", section, re.DOTALL):
        ids = re.findall(r'\bid: "([a-z0-9_.-]+:[a-z0-9_./-]+)"', slot.group(1))
        if ids:
            items.add(ids[-1])
    return items


def semantic_amounts(aspect, items, resources):
    keywords = KEYWORDS.get(aspect, ())
    ranked = []
    for item in items:
        haystack = f"{item.replace(':', ' ')} {resources.get(item, '')}".lower()
        score = sum(3 if keyword.replace("_", " ") in haystack else 2 if keyword in haystack else 0 for keyword in keywords)
        score += min(2, haystack.count(aspect))
        ranked.append((score, item))
    ranked.sort(key=lambda entry: (entry[0], entry[1]))

    count = len(ranked)
    amounts = {}
    bands = ((0.20, 1), (0.30, 2), (0.70, 4), (0.90, 6), (1.00, 8))
    for index, (_, item) in enumerate(ranked):
        position = (index + 1) / count
        amounts[item] = next(amount for boundary, amount in bands if position <= boundary)

    difference = 4 * count - sum(amounts.values())
    middle = sorted(ranked, key=lambda entry: (abs(entry[0] - 1), entry[1]))
    while difference:
        changed = False
        for _, item in middle:
            step = 1 if difference > 0 else -1
            if 1 <= amounts[item] + step <= 16:
                amounts[item] += step
                difference -= step
                changed = True
                if not difference:
                    break
        if not changed:
            raise RuntimeError(f"Cannot normalize {aspect} amounts")
    return amounts


def add_aspect(item, aspect, amount):
    namespace, path = item.split(":", 1)
    output = ASPECT_ROOT / namespace / f"{path}.json"
    if output.exists():
        data = json.loads(output.read_text(encoding="utf-8-sig"))
    else:
        data = {"type": "goldentweaks:item_aspect", "item": {"id": item}, "aspects": {}}
    data.setdefault("aspects", {})[aspect] = max(amount, data.get("aspects", {}).get(aspect, 0))
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main():
    text = INPUT.read_text(encoding="utf-8-sig")
    resources = load_resources()
    additions = {}

    for name, section in sections(text):
        aspect = ASPECTS[name]
        explicit = {item: int(amount) for item, amount in re.findall(
            r"(?m)^([a-z0-9_.-]+:[a-z0-9_./-]+)\s+(\d+)\s*$", section
        )}
        items = container_items(section)
        amounts = semantic_amounts(aspect, items, resources) if items else {}
        amounts.update(explicit)
        additions[aspect] = amounts

    vegetation = additions["vegetatio"]
    for item, display in resources.items():
        path = item.split(":", 1)[1]
        name = f"{path} {display}".lower()
        plant = any(word in name for word in ("leaves", "leaf", "flower", "grass", "bush", "shrub"))
        fid_produce = item.startswith("flavor_immersed_daily:") and any(word in name for word in PRODUCE)
        prepared = any(word in name for word in ("juice", "jam", "soup", "stew", "fried", "cooked", "salad", "cake", "pie", "popsicle", "icecream", "seed", "sapling", "wood", "leaves"))
        if plant or (fid_produce and not prepared):
            vegetation[item] = 2

    gravity = additions["gravitas"]
    for item, display in resources.items():
        path = item.split(":", 1)[1]
        name = f"{path} {display}".lower()
        if (re.search(r"(^|[_ /-])(sand|gravel)([_ /-]|$)", name) and "sandstone" not in name):
            gravity[item] = max(gravity.get(item, 0), 2)
        if re.search(r"(^|[_ /-])anvil([_ /-]|$)", name):
            gravity[item] = max(gravity.get(item, 0), 8)

    missing = sorted(item for amounts in additions.values() for item in amounts if item not in resources)
    if missing:
        print(f"Warning: {len(missing)} requested IDs absent from resource listings")

    total = 0
    for aspect, amounts in additions.items():
        for item, amount in amounts.items():
            add_aspect(item, aspect, amount)
            total += 1
        print(f"{aspect}: {len(amounts)}")
    print(f"Updated {total} item-aspect assignments across {len(additions)} aspects.")


if __name__ == "__main__":
    main()
