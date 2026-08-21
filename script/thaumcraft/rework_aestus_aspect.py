import json
from pathlib import Path


ROOT = Path(__file__).parents[2]
ASPECT_ROOT = ROOT / "src/main/resources/data/goldentweaks/recipe/thaumcraft/aspects"
SOURCES = {
    "hazennstuff:tidal_wave": 16,
    "cataclysm:tidal_claws": 12,
    "avaritia:infinity_trident": 8,
    "cataclysm:azure_sea_shield": 6,
    "cataclysm:coral_spear": 6,
    "cataclysm:coral_bardiche": 6,
    "create_fantasizing:unprocessed_heart_of_the_sea": 6,
    "bakeries:salt_water_bucket": 4,
    "cbcmoreshells:reinforced_torpedo_head": 4,
    "cbcmoreshells:torpedo_head": 3,
    "apothic_enchanting:inert_trident": 2,
    "silentgear:trident": 2,
    "silentgear:trident_prongs": 1,
    "thaumcraft:phial_of_essentia_aestus": 8,
}


def write(item, amount):
    namespace, name = item.split(":", 1)
    path = ASPECT_ROOT / namespace / f"{name}.json"
    data = json.loads(path.read_text(encoding="utf-8-sig"))
    data.setdefault("aspects", {})["aestus"] = amount
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main():
    removed = 0
    for path in ASPECT_ROOT.rglob("*.json"):
        data = json.loads(path.read_text(encoding="utf-8-sig"))
        item = data.get("item", {}).get("id")
        aspects = data.get("aspects", {})
        if item not in SOURCES and aspects.pop("aestus", None) is not None:
            removed += 1
            if aspects:
                path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
            else:
                path.unlink()

    for item, amount in SOURCES.items():
        write(item, amount)

    print(f"Replaced {removed} old aestus sources with {len(SOURCES)} surge-related sources.")


if __name__ == "__main__":
    main()
