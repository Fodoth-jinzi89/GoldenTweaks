"""Replace the aspect tag `alkimia` with `permutatio` in this mod's item-aspect recipes.

`alkimia` is not one of Thaumcraft 0.3.0's 44 aspects, so every entry naming it is rejected at load
with `unknown aspect 'alkimia'` — 301 warnings per startup, and those items silently carry no aspects
at all. Thaumcraft's own aspect table is the authority here; `permutatio` (exchange, transformation) is
the concept `alkimia` stood for, which is why it is the replacement rather than a drop.

    python script/thaumcraft/fix_alkimia_aspect.py [--dry-run]
"""
import glob
import io
import json
import sys

ROOT = "src/main/resources/data/goldentweaks/recipe/thaumcraft/aspects"
OLD = "alkimia"
NEW = "permutatio"


def main(argv):
    dry_run = "--dry-run" in argv

    files = glob.glob(f"{ROOT}/**/*.json", recursive=True)
    touched = 0
    entries = 0

    for path in files:
        with io.open(path, encoding="utf-8") as handle:
            document = json.load(handle)

        aspects = document.get("aspects")
        if not isinstance(aspects, dict) or OLD not in aspects:
            continue

        amount = aspects.pop(OLD)
        # Merged rather than overwritten: a file that already lists the replacement keeps its own
        # amount, and the two are added so the item does not lose the weight it had.
        aspects[NEW] = aspects.get(NEW, 0) + amount
        touched += 1
        entries += 1

        if not dry_run:
            with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
                json.dump(document, handle, indent=2, ensure_ascii=False)
                handle.write("\n")

    print(f"  files scanned: {len(files)}")
    print(f"  files {'would be ' if dry_run else ''}changed: {touched}")
    print(f"  aspect entries moved: {OLD} -> {NEW}: {entries}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
