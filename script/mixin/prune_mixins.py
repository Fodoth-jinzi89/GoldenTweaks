"""Drop mixin entries that no longer have a source file.

A manifest entry whose class is gone fails at load with an unhelpful "class not found" from the mixin
transformer, so stale entries are removed rather than left behind.

    python script/mixin/prune_mixins.py
"""
import io
import json
import os

MANIFEST = "src/main/resources/goldentweaks.mixins.json"
SOURCE_ROOT = "src/main/java/net/fodoth/skina/goldentweaks/mixin"


def main():
    document = json.loads(io.open(MANIFEST, encoding="utf-8").read())
    removed = []

    # A second config may be declared for other packages; only this one is pruned.
    for section in ("mixins", "client", "server"):
        entries = document.get(section)
        if not entries:
            continue
        kept = []
        for entry in entries:
            path = os.path.join(SOURCE_ROOT, *entry.split(".")) + ".java"
            nested = os.path.join(SOURCE_ROOT, *entry.split(".")[:-1]) + ".java"
            if os.path.exists(path) or os.path.exists(nested):
                kept.append(entry)
            else:
                removed.append(f"{section}: {entry}")
        document[section] = kept

    with io.open(MANIFEST, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(document, handle, indent=2, ensure_ascii=False)
        handle.write("\n")

    for line in removed:
        print(f"  pruned {line}")
    if not removed:
        print("  nothing to prune")


if __name__ == "__main__":
    main()
