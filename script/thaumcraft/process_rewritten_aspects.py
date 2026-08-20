import json
from pathlib import Path


ROOT = Path(__file__).parents[2]
ASPECT_ROOT = ROOT / "src/main/resources/data/goldentweaks/recipe/thaumcraft/aspects"
MEK_NAMESPACES = {
    "mekanism",
    "mekanism_extras",
    "mekanismadvancedgenerators",
    "mekanismgenerators",
    "mekanismtools",
    "mekmm",
}
FOOD_ITEMS_WITH_ACCESSORIES = {
    "flavor_immersed_daily:stirfriedstringbeans",
    "spectrum:glistering_jelly_tea",
}
FAMES_KEYWORDS = {
    "feast": 8, "banquet": 8, "platter": 7, "roast": 7, "steak": 7, "burger": 7,
    "pizza": 7, "cake": 6, "pie": 6, "bread": 5, "sandwich": 6, "stew": 6,
    "soup": 5, "rice": 5, "noodle": 5, "pasta": 5, "meat": 6, "pork": 6,
    "beef": 6, "chicken": 6, "mutton": 6, "fish": 5, "dumpling": 5, "meal": 6,
    "salad": 4, "fruit": 3, "vegetable": 3, "bean": 3, "egg": 4, "cheese": 4,
    "cookie": 3, "candy": 2, "tea": 2, "juice": 2, "seed": 1,
}


def normalize_fames(entries):
    ranked = []
    for item, aspects, path in entries:
        name = item.split(":", 1)[1]
        score = max((amount for keyword, amount in FAMES_KEYWORDS.items() if keyword in name), default=2)
        if any(keyword in name for keyword in ("block", "crate", "basket", "seed", "sapling", "crop")):
            score = min(score, 2)
        if any(keyword in name for keyword in ("feast", "banquet", "platter")):
            score = max(score, 8)
        ranked.append((score, item, aspects, path))

    ranked.sort(key=lambda entry: (entry[0], entry[1]))
    amounts = {}
    count = len(ranked)
    bands = ((0.15, 1), (0.35, 2), (0.70, 4), (0.90, 6), (0.98, 8), (1.00, 16))
    for index, (_, item, _, _) in enumerate(ranked):
        position = (index + 1) / count
        amounts[item] = next(amount for boundary, amount in bands if position <= boundary)

    difference = 4 * count - sum(amounts.values())
    for _, item, _, _ in sorted(ranked, key=lambda entry: (abs(entry[0] - 4), entry[1])):
        while difference and 1 <= amounts[item] + (1 if difference > 0 else -1) <= 16:
            step = 1 if difference > 0 else -1
            amounts[item] += step
            difference -= step
    assert difference == 0

    for _, item, aspects, path in ranked:
        aspects["fames"] = amounts[item]
        path.write_text(json.dumps({
            "type": "goldentweaks:item_aspect",
            "item": {"id": item},
            "aspects": aspects,
        }, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return len(ranked)


def main():
    crystal_seeds = ultimate_mek = accessories = deleted = 0
    fames_entries = []

    for path in ASPECT_ROOT.rglob("*.json"):
        data = json.loads(path.read_text(encoding="utf-8-sig"))
        item = data.get("item", {}).get("id")
        aspects = data.get("aspects")
        if not item or not isinstance(aspects, dict):
            continue

        namespace, name = item.split(":", 1)
        changed = False

        if "crystal_seed" in name and aspects.pop("vegetatio", None) is not None:
            crystal_seeds += 1
            changed = True

        if namespace in MEK_NAMESPACES and "ultimate" in name and aspects.pop("terminus", None) is not None:
            ultimate_mek += 1
            changed = True

        if item in FOOD_ITEMS_WITH_ACCESSORIES and aspects.pop("accessories", None) is not None:
            accessories += 1
            changed = True

        if "fames" in aspects:
            fames_entries.append((item, aspects, path))

        if not changed:
            continue
        if aspects:
            path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        else:
            path.unlink()
            deleted += 1

    fames = normalize_fames(fames_entries)
    print(
        f"Rewrote {crystal_seeds} crystal seeds, {ultimate_mek} ultimate Mek items, "
        f"{accessories} food accessory entries, and {fames} fames amounts; deleted {deleted} empty entries."
    )


if __name__ == "__main__":
    main()
