"""Register the two performance mixins in this mod's own mixin config.

Both live under `net.fodoth.skina.goldentweaks.mixin`, so a single config covers them. The mio_icif
one targets another mod's class by string and touches its package-private wrapper only as `Object`,
which is why it does not need a package of its own.

Idempotent: re-running adds nothing.
"""
import io
import json

MANIFEST = "src/main/resources/goldentweaks.mixins.json"

ENTRIES = [
    "fix.mioicif.GTDisguiseModelsDedupMixin",
    "fix.ponderer.PonderPackInfoMixin",
]


def main():
    document = json.loads(io.open(MANIFEST, encoding="utf-8").read())
    client = document["client"]

    changed = []
    for entry in ENTRIES:
        if entry not in client:
            client.insert(0, entry)
            changed.append(entry)

    with io.open(MANIFEST, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(document, handle, indent=2, ensure_ascii=False)
        handle.write("\n")

    for entry in changed:
        print(f"  client <- {entry}")
    if not changed:
        print("  already registered")


if __name__ == "__main__":
    main()
