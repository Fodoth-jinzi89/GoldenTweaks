"""Compare every @Mixin injection target in GoldenTweaks against the jar it patches.

Each mixin names a method either by bare name ("drawRow") or by full descriptor
("resolve(Ljava/lang/String;)V"). A stale descriptor is invisible until the class
loads at runtime, which is how three crashes in a row slipped through. This walks
the mixin sources, resolves the target class from the compile-only jars, and reports
every method/field/constant target that does not exist.

Usage: python script/debug/check_mixin_targets.py
"""

import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MIXIN_ROOT = ROOT / "src/main/java/net/fodoth/skina/goldentweaks/mixin"
LIB_DIRS = [ROOT / "libs/compileOnly", ROOT / "libs/implementation", ROOT / "libs/reference"]

sys.path.insert(0, str(ROOT / "script/debug"))
from dump_class import parse  # noqa: E402


def load_classes():
    """Map internal class name -> (fields, methods) for every jar on the compile path."""
    classes = {}
    for directory in LIB_DIRS:
        if not directory.is_dir():
            continue
        for jar in directory.glob("*.jar"):
            try:
                with zipfile.ZipFile(jar) as archive:
                    for entry in archive.namelist():
                        if not entry.endswith(".class"):
                            continue
                        name = entry[:-6]
                        raw = archive.read(entry)
                        try:
                            _, _, fields, methods, _ = parse(raw)
                        except Exception:
                            continue
                        classes[name] = (fields, methods)
            except zipfile.BadZipFile:
                continue
    return classes


def mixin_targets(path):
    """Yield (target class internal name, kind, spec, filename) for each injection in one mixin."""
    source = path.read_text(encoding="utf-8")
    match = re.search(r"@Mixin\(value\s*=\s*([\w.]+)\.class", source)
    if not match:
        return []
    # Resolve the simple name through the source's own imports so we land on the exact class.
    simple = match.group(1).rsplit(".", 1)[-1]
    target = None
    for package in re.findall(r"^import\s+([\w.]+)\." + re.escape(simple) + r";", source, re.M):
        target = package.replace(".", "/") + "/" + simple
    if target is None:
        target = match.group(1).replace(".", "/")
    found = []
    for kind, spec in re.findall(r'@(Inject|Redirect|ModifyVariable|ModifyConstant|ModifyArg|WrapOperation)\(method\s*=\s*"([^"]+)"', source):
        found.append((target, kind, spec, path.name))
    for kind, spec in re.findall(r'@(Inject|Redirect|ModifyVariable|ModifyConstant)\(method\s*=\s*\{([^}]+)\}', source):
        for one in re.findall(r'"([^"]+)"', spec):
            found.append((target, kind, one, path.name))
    return found


def main():
    classes = load_classes()

    problems = []
    checked = 0
    for path in sorted(MIXIN_ROOT.rglob("*.java")):
        for target, kind, spec, filename in mixin_targets(path):
            if target not in classes:
                # Nested classes declared as Outer.Inner in the annotation.
                alternatives = [name for name in classes if name.startswith(target)]
                if len(alternatives) != 1:
                    problems.append(f"{filename}: target class {target} not on the compile path")
                    continue
                target = alternatives[0]
            _, methods = classes[target]
            method_names = {m[1] for m in methods}
            base = spec.split("(", 1)[0] if "(" in spec else spec.rstrip("*")
            checked += 1
            matched = [m for m in method_names if m == base or (spec.endswith("*") and m.startswith(base))]
            if not matched:
                problems.append(f"{filename}: {kind} -> {target.rsplit('/', 1)[-1]}.{base} missing ({len(method_names)} methods in class)")
                continue
            # A full descriptor must match the recorded descriptor exactly.
            if "(" in spec and not spec.endswith("*"):
                descriptors = {m[2] for m in methods if m[1] == base}
                if spec not in descriptors:
                    problems.append(
                        f"{filename}: {kind} -> {target.rsplit('/', 1)[-1]}.{base} descriptor drift\n"
                        f"    wanted {spec}\n"
                        + "".join(f"    actual {d}\n" for d in sorted(descriptors))
                    )

    print(f"checked {checked} injection targets across {len(classes)} classes")
    if problems:
        print(f"\n{len(problems)} problem(s):\n")
        for problem in problems:
            print(problem)
        return 1
    print("all targets resolve")
    return 0


if __name__ == "__main__":
    sys.exit(main())
