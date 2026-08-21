import json
import re
from collections import defaultdict
from pathlib import Path


ROOT = Path(__file__).parents[2]
ASPECT_ROOT = ROOT / "src/main/resources/data/goldentweaks/recipe/thaumcraft/aspects"
MINIMUM = 20
MAX_ASPECTS = 6
EXACT_ASPECTS = {
    "lzxnonefate:ea": {"mornogol": 64},
    "lzxnonefate:key": {"mornogol": 32},
}

KEYWORDS = {
    "exanimis": ("undead", "zombie", "skeleton", "wither", "corpse", "revenant", "draugr", "necrom", "soul"),
    "mornogol": ("ancient", "remnant", "relic", "primordial", "eldritch", "uruk", "orc"),
    "favilla": ("ash", "ember", "cinder", "charcoal", "coal", "soot", "campfire", "blaze"),
    "fossil": ("fossil", "ancient", "bone", "skeleton", "archae", "relic", "remnant"),
    "history": ("ancient", "history", "relic", "archae", "timeworn", "tablet", "tome", "manuscript"),
    "mixtura": ("alloy", "brass", "bronze", "steel", "invar", "electrum", "constantan", "signalum", "lumium"),
    "universes": ("cosmic", "universe", "infinity", "star", "stellar", "celestial", "astral", "galaxy"),
    "meto": ("harvest", "harvester", "sickle", "scythe", "hoe", "crop", "reaping"),
    "dream": ("dream", "sleep", "bed", "pillow", "nightmare", "moon", "lunar"),
    "vegetatio": ("crop", "sapling", "seed", "leaves", "grass", "flower", "moss", "vine"),
    "destroy": ("destroy", "destruct", "annihil", "bomb", "tnt", "explosive", "warhead", "nuke"),
    "venenum": ("poison", "venom", "toxic", "acid", "spider_eye", "hemlock", "arsenic"),
    "tempestas": ("weather", "storm", "tempest", "hurricane", "tornado", "thunder", "rain", "wind"),
    "waters": ("water", "ocean", "sea", "river", "tidal", "aquatic", "coral", "kelp"),
    "sano": ("heal", "health", "medical", "medicine", "bandage", "regeneration", "restor", "hospital", "food", "meal", "stew", "soup", "bread", "cake", "pie", "juice", "salad", "sandwich", "baked", "cooked", "jerky", "pasta", "pizza", "toast", "rice", "noodle", "croptopia"),
    "auram": ("aura", "wisp", "silverwood", "shimmer", "ethereal", "spirit", "soul"),
    "spiritus": ("spirit", "soul", "ghost", "spectral", "phantom", "wisp", "essence"),
    "aestus": ("tidal", "tide", "wave", "surge", "current", "flow", "water_wheel", "water_thruster", "torpedo"),
    "lava": ("lava", "molten", "magma", "volcan", "blaze", "infernal"),
    "magnetis": ("magnet", "magnetic", "lodestone", "compass", "electromagnet", "copper"),
    "textus": ("cloth", "fabric", "wool", "carpet", "canvas", "textile", "silk", "thread"),
    "imperium": ("crown", "banner", "command", "controller", "dominion", "royal", "king", "leader"),
    "expand": ("expand", "growth", "large", "giant", "colossal", "compression", "compressed"),
    "illecebra": ("lure", "bait", "charm", "sweet", "honey", "tempt", "attract", "fishing"),
    "treasure": ("treasure", "loot", "gold", "diamond", "emerald", "gem", "jewel", "coin"),
    "orbita": ("rail", "track", "route", "train", "locomotive", "minecart", "railway"),
    "priscus": ("ancient", "old", "relic", "fossil", "remnant", "primordial", "timeworn"),
    "spelunca": ("cave", "cavern", "underground", "deepslate", "stalact", "spelunk", "grotto"),
    "evil": ("evil", "cursed", "demon", "sinister", "dark", "taint", "corrupt", "forbidden"),
    "fulmen": ("lightning", "thunder", "electric", "tesla", "charged", "electro", "storm"),
    "laputa": ("sky", "cloud", "floating", "airship", "aerial", "aero", "flight", "levitat"),
    "praecantaticherba": ("magic_flower", "magic_plant", "arcane_herb", "shimmerleaf", "cinderpearl", "silverwood", "mana", "spellblossom"),
    "limus": ("slime", "goo", "sludge", "mucus", "gel", "honey", "resin", "sticky"),
    "dragon": ("dragon", "draconic", "wyrm", "drake"),
    "atrsubstance": ("antimatter", "dark_matter", "void_matter", "negative_matter", "black_matter", "singularity"),
    "atrpraecantatic": ("dark_magic", "black_magic", "forbidden", "occult", "cursed", "tainted", "eldritch"),
    "arche": ("genesis", "primordial", "origin", "creation", "creative", "protoclay", "first_"),
    "anteanus": ("ancient", "timeworn", "relic", "fossil", "archae", "primordial", "old_"),
    "atrpotentia": ("dark_energy", "void_energy", "antimatter", "black_hole", "negative_energy", "singularity"),
    "alfirin": ("immortal", "infinity", "eternal", "everlasting", "undying", "regeneration", "life"),
}


def load_entries():
    entries = []
    for path in ASPECT_ROOT.rglob("*.json"):
        data = json.loads(path.read_text(encoding="utf-8-sig"))
        item = data.get("item", {}).get("id")
        if not item or item.split(":", 1)[1].startswith(("phial_of_essentia_", "wisp_essence_")):
            continue
        entries.append((path, data, item))
    return entries


def matches(item, keywords):
    path = item.split(":", 1)[1]
    return sum(1 for keyword in keywords if keyword in path)


entries = load_entries()
for path, data, item in entries:
    for aspect, amount in EXACT_ASPECTS.get(item, {}).items():
        data["aspects"][aspect] = amount
        path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

sources = defaultdict(set)
for _, data, item in entries:
    for aspect in data.get("aspects", {}):
        sources[aspect].add(item)

for aspect, keywords in KEYWORDS.items():
    needed = max(0, MINIMUM - len(sources[aspect]))
    ranked = sorted(
        (
            (-matches(item, keywords), len(data.get("aspects", {})), item, path, data)
            for path, data, item in entries
            if item not in sources[aspect]
            and len(data.get("aspects", {})) < MAX_ASPECTS
            and matches(item, keywords)
        )
    )
    candidates = []
    candidate_items = set()
    for candidate in ranked:
        item = candidate[2]
        if item not in candidate_items:
            candidates.append(candidate)
            candidate_items.add(item)
    if len(candidates) < needed:
        raise RuntimeError(f"{aspect} only has {len(candidates)} candidates for {needed} missing sources")
    for _, _, item, path, data in candidates[:needed]:
        data["aspects"][aspect] = 2
        path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        sources[aspect].add(item)

for aspect in KEYWORDS:
    if len(sources[aspect]) < MINIMUM:
        raise RuntimeError(f"{aspect} has only {len(sources[aspect])} sources")

print(f"Supplemented {len(KEYWORDS)} rare aspects to at least {MINIMUM} ordinary item/block sources each.")
