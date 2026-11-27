"""Point the familiar-based recipes at the typed eggs that replaced the shards.

The shard items are gone from alshanex_familiars: `ItemRegistry` no longer declares them, and the
`_shard` lang entries still in the mod are leftovers. What exists now is one egg item,
`familiarslib:familiar_egg`, carrying `familiarslib:familiar_type` — the entity type it hatches. This
script rewrites every reference in this mod's data so a shard becomes the egg for the same familiar.

Two shapes are handled:

* the crafting-table pattern in `inverted_familiar_spellbook.json`, where each shard is a key;
* the per-shard aspect files, which are renamed and re-pointed at the egg, because the result is a
  data-driven map from item to aspects and the item id inside it is what identifies the entry.

    python script/data/shards_to_eggs.py [--dry-run]
"""
import glob
import io
import json
import os
import sys

SHARD_DIR = "src/main/resources/data/goldentweaks/recipe/thaumcraft/aspects/alshanex_familiars"
SPELLBOOK = ("src/main/resources/data/goldentweaks/recipe/alshanex_familiars"
             "/extendedcrafting/inverted_familiar_spellbook.json")
RITUAL_DIR = "src/main/resources/data/alshanex_familiars/recipe/ritual_recipes"
EGG = "familiarslib:familiar_egg"
TYPE_COMPONENT = "familiarslib:familiar_type"

# Shard suffix -> the entity the matching egg hatches. The shard names are the older vocabulary for
# the same familiars, so each maps one to one.
FAMILIARS = {
    "archmage": "archmage_pet",
    "bard": "bard_pet",
    "cleric": "cleric_pet",
    "dragon_warrior": "dragon_warrior_pet",
    "druid": "druid_pet",
    "frostling": "frostling_pet",
    "hunter": "hunter_pet",
    "illusionist": "illusionist_pet",
    "lightning_mage": "mage_pet",
    "necromancer": "necromancer_pet",
    "plague": "plague_pet",
    "scorcher": "scorcher_pet",
    "summoner": "summoner_pet",
}


def egg_of(shard_suffix):
    """A recipe ingredient for the typed egg of one familiar."""
    return {
        "id": EGG,
        "components": {TYPE_COMPONENT: f"alshanex_familiars:{FAMILIARS[shard_suffix]}"},
    }


def main(argv):
    dry_run = "--dry-run" in argv
    changed = []

    # ---- the spellbook pattern ---------------------------------------------------------------
    if os.path.exists(SPELLBOOK):
        with io.open(SPELLBOOK, encoding="utf-8") as handle:
            document = json.load(handle)
        touched = 0
        for letter, ingredient in document.get("key", {}).items():
            item = ingredient.get("item", "")
            prefix = "alshanex_familiars:"
            if not item.startswith(prefix) or not item.endswith("_shard"):
                continue
            suffix = item[len(prefix):-len("_shard")]
            if suffix not in FAMILIARS:
                continue
            document["key"][letter] = egg_of(suffix)
            touched += 1
        if touched:
            changed.append(f"{os.path.basename(SPELLBOOK)}: {touched} key(s)")
            if not dry_run:
                with io.open(SPELLBOOK, "w", encoding="utf-8", newline="\n") as handle:
                    json.dump(document, handle, indent=2, ensure_ascii=False)
                    handle.write("\n")

    # ---- the ritual recipes ------------------------------------------------------------------
    # These override alshanex's own rituals so a familiar can be produced with this pack's
    # ingredients. The produced item was a shard; it becomes the egg for the same familiar, keeping
    # the file name (the recipe id) unchanged so the override still replaces the original.
    for path in sorted(glob.glob(f"{RITUAL_DIR}/*_shard.json")):
        name = os.path.basename(path)
        suffix = name[:-len("_shard.json")]
        if suffix not in FAMILIARS:
            continue
        with io.open(path, encoding="utf-8") as handle:
            document = json.load(handle)
        result = document.get("result")
        if not isinstance(result, dict):
            continue
        document["result"] = egg_of(suffix)
        changed.append(f"{name}: result -> egg")
        if not dry_run:
            with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
                json.dump(document, handle, indent=2, ensure_ascii=False)
                handle.write("\n")

    # ---- the per-shard aspect files ----------------------------------------------------------
    for path in sorted(glob.glob(f"{SHARD_DIR}/*_shard.json")):
        name = os.path.basename(path)
        suffix = name[:-len("_shard.json")]
        if suffix not in FAMILIARS:
            continue
        with io.open(path, encoding="utf-8") as handle:
            document = json.load(handle)
        document["item"] = egg_of(suffix)
        target = os.path.join(SHARD_DIR, f"{FAMILIARS[suffix]}.json")
        changed.append(f"{name} -> {os.path.basename(target)}")
        if not dry_run:
            with io.open(target, "w", encoding="utf-8", newline="\n") as handle:
                json.dump(document, handle, indent=2, ensure_ascii=False)
                handle.write("\n")
            os.remove(path)

    for line in changed:
        print(f"  {line}")
    print(f"  total: {len(changed)}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
