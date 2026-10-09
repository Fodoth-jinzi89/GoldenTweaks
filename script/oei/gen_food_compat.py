#!/usr/bin/env python3
"""Generate OEI food-unification compatibility data.

Two kinds of output are produced for the mods whose id appears in
``food_unification.json``:

1. Recipe overrides (all work blocks / recipe types).  Whenever an ingredient
   or an output references a unified item directly, two datapack files are
   written:

     data/<ns>/recipe/<path>.json
         The original recipe, active only while NOT every unified-result mod is
         loaded.  It overrides the mod's own file because GoldenTweaks loads
         AFTER the origin mods (see neoforge.mods.toml).

     data/<ns>/recipe/<path>_unified.json
         The same recipe with every unified item swapped for its unified result
         (ingredients and outputs).  Active only while ALL unified-result mods
         load.  Outputs are unified too.  Display / load-condition data is
         never touched.

2. Common tag extensions.  When an ingredient is a tag (#tag / {"tag": ...})
   whose members include unified source items, the tag is missing the unified
   result after OEI runs, which makes every recipe using that tag uncraftable.
   The missing results are added to the tag:

     data/<ns>/tags/item/<path>.json   (replace:false, required:false entries)

Usage:
    python gen_food_compat.py                # dry run, prints a report
    python gen_food_compat.py --write        # write the datapack files
"""

import argparse
import copy
import json
import os
import re
import zipfile
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
RES = REPO / "src" / "main" / "resources"
FU_FILE = RES / "data" / "oei" / "replacements" / "food_unification.json"
DEFAULT_MODS = Path(r"E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods")

# Never look into these subtrees (load conditions, rendering, quest data ...).
SKIP_SUBSTR = ("condition", "display", "reward", "loot", "advancement")

# Keys that open an output subtree.
OUT_SUBSTR = ("result", "output", "drop", "bonus")

# Keys that open an ingredient subtree.
ING_SUBSTR = (
    "ingredient",
    "input",
    "seed",
    "key",
    "base",
    "addition",
    "template",
    "catalyst",
    "item",
    "tag",
    "consumed",
    "fuel",
)

TAG_DIRS = ("tags/item", "tags/items")
MODID_RE = re.compile(r'^\s*modId\s*=\s*"([^"]+)"')
RECIPE_DIRS = ("recipe", "recipes")


def load_sources(files, resolver):
    """Build item id -> unified result item id over every replacement file.

    Tag match items (``#c:ingots/copper``) are expanded to their member items,
    exactly like OEI's ``putReplacement`` resolves them through the registry.
    """
    item_map = {}
    conflicts = []
    source_tags = {}
    for f in files:
        try:
            groups = json.loads(Path(f).read_text(encoding="utf-8"))
        except Exception as e:
            print(f"WARNING: cannot read {f}: {e}")
            continue
        for g in groups:
            result = g["resultItems"]
            for m in g["matchItems"]:
                if m.startswith("#"):
                    source_tags[m[1:]] = result
                    members = resolver.resolve(m[1:])
                else:
                    members = {m}
                for item in members:
                    if item in item_map and item_map[item] != result:
                        conflicts.append((item, item_map[item], result))
                    item_map[item] = result  # OEI overwrites -> last entry wins
    covered = {m.split(":")[0] for m in item_map}
    covered.update(r.split(":")[0] for r in item_map.values())
    return item_map, covered, conflicts, source_tags


def jar_modid(path):
    try:
        with zipfile.ZipFile(path) as z:
            data = z.read("META-INF/neoforge.mods.toml").decode("utf-8", "ignore")
    except Exception:
        return None
    for line in data.splitlines():
        m = MODID_RE.match(line)
        if m:
            return m.group(1)
    return None


class TagResolver:
    """Resolves item tag ids to the set of items they contain, across mods."""

    def __init__(self, jars, data_roots):
        self.index = {}
        self.cache = {}
        for jp in jars:
            try:
                with zipfile.ZipFile(jp) as z:
                    for name in z.namelist():
                        if not name.endswith(".json"):
                            continue
                        parts = name.split("/")
                        if len(parts) < 5 or parts[0] != "data" or "/".join(parts[2:4]) not in TAG_DIRS:
                            continue
                        tagid = f"{parts[1]}:" + "/".join(parts[4:])[:-5]
                        self.index.setdefault(tagid, []).append(("zip", jp, name))
            except Exception:
                pass
        for root in data_roots:
            if not Path(root).is_dir():
                continue
            for td in TAG_DIRS:
                for p in Path(root).glob(f"*/{td}/**/*.json"):
                    parts = p.relative_to(root).parts
                    tagid = f"{parts[0]}:" + "/".join(parts[3:])[:-5]
                    self.index.setdefault(tagid, []).append(("dir", str(p), None))

    def _values(self, kind, src, entry):
        try:
            if kind == "zip":
                with zipfile.ZipFile(src) as z:
                    data = json.loads(z.read(entry).decode("utf-8"))
            else:
                data = json.loads(Path(src).read_text(encoding="utf-8"))
        except Exception:
            return []
        vals = data.get("values", []) if isinstance(data, dict) else []
        return vals if isinstance(vals, list) else []

    def resolve(self, tagid, stack=None):
        if tagid in self.cache:
            return self.cache[tagid]
        if stack is None:
            stack = set()
        if tagid in stack:
            return set()
        stack.add(tagid)
        items = set()
        for kind, src, entry in self.index.get(tagid, []):
            for v in self._values(kind, src, entry):
                ref = None
                if isinstance(v, str):
                    ref = v
                elif isinstance(v, dict) and isinstance(v.get("id"), str):
                    ref = v["id"]
                if ref is None:
                    continue
                if ref.startswith("#"):
                    items |= self.resolve(ref[1:], stack)
                else:
                    items.add(ref)
        stack.discard(tagid)
        self.cache[tagid] = items
        return items


def walk(node, parent, key, skip, active, opens, out):
    """Collect item-id strings inside a subtree, honouring skip boundaries."""
    if isinstance(node, dict):
        for k, v in node.items():
            ks = k.lower()
            cskip = skip or any(s in ks for s in SKIP_SUBSTR)
            cactive = active or any(s in ks for s in opens)
            walk(v, node, k, cskip, cactive, opens, out)
    elif isinstance(node, list):
        for i, v in enumerate(node):
            walk(v, node, i, skip, active, opens, out)
    elif isinstance(node, str):
        if active and not skip and not node.startswith("#"):
            out.append((parent, key, node))


def walk_tags(node, skip, active, out):
    """Collect tag ids referenced in ingredient positions."""
    if isinstance(node, dict):
        if active and not skip and isinstance(node.get("tag"), str) and not node["tag"].startswith("#"):
            out.add(node["tag"])
            return
        for k, v in node.items():
            ks = k.lower()
            cskip = skip or any(s in ks for s in SKIP_SUBSTR)
            cactive = active or any(s in ks for s in ING_SUBSTR + OUT_SUBSTR)
            walk_tags(v, cskip, cactive, out)
    elif isinstance(node, list):
        for v in node:
            walk_tags(v, skip, active, out)
    elif isinstance(node, str):
        if active and not skip and node.startswith("#") and len(node) > 1:
            out.add(node[1:])


def analyze(obj, item_map):
    """Return (changes, mods) for every unified item reference."""
    hits = []
    walk(obj, None, None, False, False, ING_SUBSTR + OUT_SUBSTR, hits)
    changes = []
    mods = set()
    for parent, key, value in hits:
        result = item_map.get(value)
        if result is not None and result != value:
            changes.append((parent, key, value, result))
            mods.add(result.split(":")[0])
    return changes, mods


def tag_missing(item_map, resolver, tagid):
    members = resolver.resolve(tagid)
    if not members:
        return []
    return sorted({item_map[m] for m in members if m in item_map and item_map[m] != m} - members)


def mod_loaded(modid):
    return {"type": "neoforge:mod_loaded", "modid": modid}


def cond_all(mods):
    mods = sorted(mods)
    if len(mods) == 1:
        return mod_loaded(mods[0])
    return {"type": "neoforge:and", "values": [mod_loaded(m) for m in mods]}


def with_condition(obj, cond):
    out = dict(obj)
    out["neoforge:conditions"] = list(out.get("neoforge:conditions", [])) + [cond]
    return out


def iter_jar_recipes(mods_dir, covered):
    """Yield (relpath, obj) for the first occurrence of each recipe."""
    seen = set()
    files = [os.path.join(mods_dir, f) for f in os.listdir(mods_dir) if f.endswith(".jar")]
    for jp in sorted(files):
        if jar_modid(jp) not in covered:
            continue
        try:
            z = zipfile.ZipFile(jp)
        except Exception:
            continue
        with z:
            for name in z.namelist():
                if not name.endswith(".json"):
                    continue
                parts = name.split("/")
                if len(parts) < 4 or parts[0] != "data" or parts[2] not in RECIPE_DIRS:
                    continue
                if parts[1] not in covered or name in seen:
                    continue
                seen.add(name)
                try:
                    obj = json.loads(z.read(name).decode("utf-8"))
                except Exception:
                    continue
                yield name, obj


def relpath_variants(relpath):
    path = Path(relpath)
    return RES / relpath, RES / path.with_name(path.stem + "_unified" + path.suffix)


def looks_generated(path):
    """True when a file already carries our generated override marker."""
    try:
        return '"neoforge:not"' in path.read_text(encoding="utf-8")
    except Exception:
        return False


def dump(obj):
    return json.dumps(obj, indent=2, ensure_ascii=False) + "\n"


def result_id(obj):
    r = obj.get("result")
    if isinstance(r, dict):
        return r.get("id") or r.get("item")
    return None


def _is_saplings_ingredient(x):
    if isinstance(x, dict):
        tag = x.get("tag")
        return isinstance(tag, str) and "saplings" in tag
    if isinstance(x, str):
        return "saplings" in x
    return False


def normalize_recipe(obj):
    """Balance: <fruit> + <fruit> + sapling -> sapling becomes one fruit + sapling."""
    rid = result_id(obj)
    if not (isinstance(rid, str) and rid.endswith("_sapling")):
        return obj
    if obj.get("type") != "minecraft:crafting_shapeless":
        return obj
    ings = obj.get("ingredients")
    if not isinstance(ings, list) or len(ings) < 2:
        return obj
    keys = [json.dumps(x, sort_keys=True, ensure_ascii=False) for x in ings]
    if len(set(keys)) == len(keys) or not any(_is_saplings_ingredient(x) for x in ings):
        return obj
    seen, deduped = set(), []
    for k, x in zip(keys, ings):
        if k not in seen:
            seen.add(k)
            deduped.append(x)
    out = dict(obj)
    out["ingredients"] = deduped
    return out


def write_tag_file(tagid, missing):
    ns, _, path = tagid.partition(":")
    target = RES / "data" / ns / "tags" / "item" / (path + ".json")
    if target.exists():
        try:
            data = json.loads(target.read_text(encoding="utf-8"))
        except Exception:
            data = {}
        if not isinstance(data, dict):
            data = {}
        values = list(data.get("values", []))
    else:
        values = []
    present = {v if isinstance(v, str) else v.get("id") for v in values if isinstance(v, (str, dict))}
    added = [r for r in missing if r not in present]
    if not added:
        return 0
    values.extend({"id": r, "required": False} for r in added)
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(dump({"replace": False, "values": values}), encoding="utf-8")
    return len(added)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--mods", default=str(DEFAULT_MODS))
    ap.add_argument("--write", action="store_true")
    ap.add_argument("--limit", type=int, default=8, help="examples to print, -1 for all")
    args = ap.parse_args()

    mods_dir = Path(args.mods)
    jars = [str(mods_dir / f) for f in os.listdir(mods_dir) if f.endswith(".jar")]
    data_roots = [RES / "data"]
    if mods_dir.parent.is_dir():
        data_roots += [mods_dir.parent / "kubejs" / "data", mods_dir.parent / "data"]
    resolver = TagResolver(jars, data_roots)
    print(f"tag definitions indexed: {len(resolver.index)}")

    files = sorted((RES / "data" / "oei" / "replacements").glob("*.json"))
    item_map, covered, conflicts, source_tags = load_sources(files, resolver)
    print("replacement files: " + ", ".join(p.name for p in files))
    print(f"unified sources: {len(item_map)}  covered namespaces: {len(covered)}")
    for c in conflicts[:20]:
        print(f"WARNING: {c[0]} matched by {c[1]} then {c[2]} (last wins)")
    if len(conflicts) > 20:
        print(f"WARNING: {len(conflicts)} conflicts total (showing first 20)")

    total = selected = skipped = 0
    per_ns = {}
    shown = 0
    used_tags = set()
    for relpath, obj in iter_jar_recipes(args.mods, covered):
        total += 1
        obj = normalize_recipe(obj)
        walk_tags(obj, False, False, used_tags)
        changes, mods = analyze(obj, item_map)
        if not changes:
            continue
        override_path, unified_path = relpath_variants(relpath)
        if override_path.exists() and not looks_generated(override_path):
            skipped += 1
            print(f"  skip (hand-written override): {relpath}")
            continue
        selected += 1
        ns = relpath.split("/")[1]
        per_ns[ns] = per_ns.get(ns, 0) + 1

        if args.limit < 0 or shown < args.limit:
            shown += 1
            print(f"  {relpath}")
            for _, _, src, result in changes:
                print(f"      {src} -> {result}")

        if args.write:
            not_cond = {"type": "neoforge:not", "value": cond_all(mods)}
            override_path.parent.mkdir(parents=True, exist_ok=True)
            override_path.write_text(dump(with_condition(obj, not_cond)), encoding="utf-8")
            unified = copy.deepcopy(obj)
            for parent, key, _, result in analyze(unified, item_map)[0]:
                parent[key] = result
            unified_path.write_text(dump(with_condition(unified, cond_all(mods))), encoding="utf-8")

    # Tag extensions: add missing unified results to every referenced tag, and
    # make sure every source tag actually contains its unified result.
    tag_files = tag_entries = 0
    for tagid in sorted(used_tags):
        missing = tag_missing(item_map, resolver, tagid)
        if not missing:
            continue
        tag_files += 1
        if args.write:
            tag_entries += write_tag_file(tagid, missing)
        else:
            print(f"  tag {tagid} += {', '.join(missing)}")
    for tagid, result in sorted(source_tags.items()):
        if result in resolver.resolve(tagid):
            continue
        tag_files += 1
        if args.write:
            tag_entries += write_tag_file(tagid, [result])
        else:
            print(f"  source tag {tagid} += {result}")

    print(f"recipes scanned: {total}")
    print(f"recipes needing compat: {selected}")
    print(f"skipped (existing hand-written override): {skipped}")
    for ns, n in sorted(per_ns.items(), key=lambda kv: -kv[1]):
        print(f"  {n:5d}  {ns}")
    print(f"tags referenced: {len(used_tags)}  tags needing extension: {tag_files}"
          + (f"  entries added: {tag_entries}" if args.write else ""))
    print("written" if args.write else "dry run (use --write)")


if __name__ == "__main__":
    main()
