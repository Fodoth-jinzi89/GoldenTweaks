"""Check that the JEI/EMI crucible category hooks still match the installed Thaumcraft.

The aspect layout for the crucible page is a redirect on two helper calls. A redirect whose target no
longer exists is skipped with only a warning, so the page silently reverts to the mod's own layout —
which is exactly the symptom when Thaumcraft changes how it draws aspects. This reports the real
state so that reversion is visible rather than guessed at.

    python script/thaumcraft/check_crucible_hooks.py
"""
import os
import sys
import zipfile

MODS = r"E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods"

CLASSES = [
    "thaumcraft/integration/jei/JeiCategoryDrawing",
    "thaumcraft/integration/jei/CrucibleRecipeCategory",
]

# Names the mixin redirects or calls. A missing one means the hook cannot apply.
WANTED = ["drawAspects", "addAspectTooltip", "aspectStep", "draw", "getTooltip"]

# The mixin's own references, so the report states what is expected of whom.
EXPECTED = {
    "drawAspects": "JeiCategoryDrawing(GuiGraphics, AspectList, int, int, int)",
    "addAspectTooltip": "JeiCategoryDrawing(ITooltipBuilder, AspectList, int, int, int, double, double)",
}


def constant_pool_strings(data):
    """Every Utf8 entry of a class file — member names live there."""
    names = set()
    try:
        count = int.from_bytes(data[8:10], "big")
        offset = 10
        index = 1
        while index < count:
            tag = data[offset]
            offset += 1
            if tag == 1:
                length = int.from_bytes(data[offset:offset + 2], "big")
                offset += 2
                names.add(data[offset:offset + length].decode("utf-8", "replace"))
                offset += length
            elif tag in (7, 8, 16, 19, 20):
                offset += 2
            elif tag == 15:
                offset += 3
            elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
                offset += 4
            elif tag in (5, 6):
                offset += 8
                index += 1
            else:
                break
            index += 1
    except (IndexError, ValueError):
        pass
    return names


def find_jar(prefix):
    for name in os.listdir(MODS):
        if name.endswith(".jar") and name.startswith(prefix):
            return os.path.join(MODS, name)
    return None


def main():
    jar = find_jar("[S]-thaumcraft")
    if not jar:
        print("  thaumcraft jar not found")
        return 1
    print(f"  jar: {os.path.basename(jar)}")

    with zipfile.ZipFile(jar) as archive:
        present = set(archive.namelist())
        for target in CLASSES:
            path = target + ".class"
            if path not in present:
                print(f"  MISSING CLASS {target}")
                continue
            names = constant_pool_strings(archive.read(path))
            print(f"  {target}:")
            for want in WANTED:
                mark = "ok  " if want in names else "GONE"
                note = ""
                if want in EXPECTED:
                    note = f"  expected {EXPECTED[want]}"
                print(f"    {mark} {want}{note}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
