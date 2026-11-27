"""Replay GTScexAspectBridge's reflection against the real jars, outside the game.

The bridge swallows every exception it hits, so a broken lookup only shows up as "aspects
cannot be looked up in EMI" with nothing in the log. This runs the same steps here, where
failures are visible, so the class/method names can be checked without launching Minecraft.

Usage:
    python script/debug/check_aspect_bridge.py
"""

import re
import subprocess
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LIBS = ROOT / "libs"

CONSTANTS = {
    "SCEX_INGREDIENT": "thaumcraft.integration.jei.JeiAspect",
    "SCEX_HELPER": "thaumcraft.integration.jei.JeiAspectHelper",
    "SCEX_RENDERER": "thaumcraft.integration.jei.JeiAspectRenderer",
    "ASPECT": "thaumcraft.api.aspects.Aspect",
    "TMRV_STACK": "dev.nolij.toomanyrecipeviewers.impl.ingredient.TMRVStack",
}


def find_class(name):
    """Locate a class entry across every jar on the compile path."""
    entry = name.replace(".", "/") + ".class"
    for jar in LIBS.rglob("*.jar"):
        try:
            with zipfile.ZipFile(jar) as archive:
                if entry in archive.namelist():
                    return jar, archive.read(entry)
        except zipfile.BadZipFile:
            continue
    return None, None


def members(raw):
    """Field and method descriptors, by name, using the project's own class reader."""
    sys.path.insert(0, str(ROOT / "script/debug"))
    from dump_class import parse
    _, _, fields, methods, _ = parse(raw)
    return (
        {name: (access, desc) for access, name, desc, _ in fields},
        {name: (access, desc) for access, name, desc, _ in methods},
    )


def main():
    problems = []

    def check(label, ok, detail=""):
        print(f"  {'OK  ' if ok else 'FAIL'}  {label}{('  ' + detail) if detail else ''}")
        if not ok:
            problems.append(label)

    # 1. Every class the bridge names must exist.
    raws = {}
    for const, name in CONSTANTS.items():
        jar, raw = find_class(name)
        check(f"{const} -> {name}", raw is not None, f"[{jar.name}]" if jar else "[not found]")
        if raw is not None:
            raws[const] = raw

    # 2. The members the bridge reaches for, with the access they need.
    if "SCEX_INGREDIENT" in raws:
        fields, methods = members(raws["SCEX_INGREDIENT"])
        check("JeiAspect.TYPE (public static)",
              "TYPE" in fields and fields["TYPE"][0] & 0x9 == 0x9)
        ctor = methods.get("<init>")
        check("JeiAspect(Aspect) public ctor",
              bool(ctor) and ctor[0] & 1 and ctor[1] == "(Lthaumcraft/api/aspects/Aspect;)V",
              ctor[1] if ctor else "missing")
        check("JeiAspect.aspect() method", "aspect" in methods)

    if "SCEX_HELPER" in raws:
        _, methods = members(raws["SCEX_HELPER"])
        ctor = methods.get("<init>")
        # getDeclaredConstructor + setAccessible is what the bridge uses, so any access works.
        check("JeiAspectHelper() no-arg ctor", bool(ctor) and ctor[1] == "()V",
              f"access=0x{ctor[0]:04x}" if ctor else "missing")

    if "SCEX_RENDERER" in raws:
        _, methods = members(raws["SCEX_RENDERER"])
        ctor = methods.get("<init>")
        check("JeiAspectRenderer() no-arg ctor", bool(ctor) and ctor[1] == "()V",
              f"access=0x{ctor[0]:04x}" if ctor else "missing")

    if "ASPECT" in raws:
        _, methods = members(raws["ASPECT"])
        check("Aspect.getAspect(String)", "getAspect" in methods)
        check("Aspect.getTag()", "getTag" in methods)

    if "TMRV_STACK" in raws:
        fields, methods = members(raws["TMRV_STACK"])
        check("TMRVStack.ingredient field (public)", "ingredient" in fields and fields["ingredient"][0] & 1)
        create = methods.get("create")
        check("TMRVStack.create(type,helper,renderer,Object)",
              bool(create) and create[1].endswith("Ljava/lang/Object;)Ldev/emi/emi/api/stack/EmiStack;"))

    print()
    if problems:
        print(f"{len(problems)} problem(s) - the bridge would report usable=false")
        return 1
    print("all lookups the bridge performs resolve; usable should be true")
    return 0


if __name__ == "__main__":
    sys.exit(main())
