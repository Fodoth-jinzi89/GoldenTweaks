"""Deploy the built jar into the modpack, removing any duplicate first.

NeoForge loads every jar in `mods/`, and all three of this mod's build outputs share
`modId = goldentweaks`. Two copies at once means the older one's classes and mixins are
applied as well as the new one's — which is how a deleted mixin kept crashing a build
that no longer contained it. This script enforces exactly one jar before copying.

Usage:
    python script/deploy.py            # build output -> modpack
    python script/deploy.py --dry-run  # report what would happen
"""

import shutil
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODS = Path(r"E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods")
SOURCE = ROOT / "build/libs/goldentweaks-4.7.9.jar"

# Every name this mod has shipped under, so leftovers from older builds are caught too.
PATTERNS = ("goldentweaks-*.jar", "*goldentweaks*.jar", "*goldentweaks*.jar.disabled")


def mod_id_of(jar):
    """The `modId` of the mod a jar declares, or None if it is not readable."""
    try:
        with zipfile.ZipFile(jar) as archive:
            for entry in ("META-INF/neoforge.mods.toml", "META-INF/mods.toml"):
                if entry in archive.namelist():
                    text = archive.read(entry).decode("utf-8", "replace")
                    for line in text.splitlines():
                        stripped = line.strip()
                        if stripped.startswith("modId") and "goldentweaks" in stripped:
                            return "goldentweaks"
    except (zipfile.BadZipFile, OSError):
        return None
    return None


def main(argv):
    dry_run = "--dry-run" in argv
    if not SOURCE.is_file():
        print(f"missing build output: {SOURCE}")
        return 1

    present = sorted({p for pattern in PATTERNS for p in MODS.glob(pattern)})
    foreign = [p for p in present if mod_id_of(p) != "goldentweaks"]
    ours = [p for p in present if mod_id_of(p) == "goldentweaks"]

    print(f"found {len(ours)} goldentweaks jar(s):")
    for jar in ours:
        print(f"  {jar.name}  ({jar.stat().st_size} bytes)")
    for jar in foreign:
        print(f"  skipping {jar.name}: different modId")

    if dry_run:
        print(f"\nwould remove {len(ours)} and copy {SOURCE.name}")
        return 0

    for jar in ours:
        jar.unlink()
        print(f"removed {jar.name}")

    target = MODS / SOURCE.name
    shutil.copy2(SOURCE, target)
    print(f"copied {SOURCE.name} ({target.stat().st_size} bytes)")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
