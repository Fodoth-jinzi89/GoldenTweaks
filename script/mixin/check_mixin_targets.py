"""Check that every alshanex/familiarslib mixin target still exists in the installed jars.

A mixin breaks at load time when a `@Shadow` field or an `@Inject(method = ...)` target has been
renamed or removed upstream — and the message names a method the target no longer has, which is
indirect. This walks the mixin sources, pulls out the target class plus the method and field names
they reference, and reports anything the installed jars do not declare.

Usage:
    python script/mixin/check_mixin_targets.py
"""
import glob
import io
import os
import re
import struct
import zipfile

MODS = r"E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods"
JARS = [
    os.path.join(MODS, "[S]-alshanex_familiars-1.21.1_v5.1-neoforge-1.21.1.jar"),
    os.path.join(MODS, "[S]-familiarslib-2.1-neoforge-1.21.1.jar"),
]
SOURCES = [
    "src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/alshanex_familiars",
    "src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/alshanex_familiars",
]


def load_class_index():
    """Map every class name to the jar holding it, and its raw bytes."""
    index = {}
    for jar in JARS:
        if not os.path.exists(jar):
            continue
        with zipfile.ZipFile(jar) as archive:
            for entry in archive.namelist():
                if entry.endswith(".class"):
                    name = entry[:-len(".class")].replace("/", ".")
                    index[name] = (jar, archive.read(entry))
    return index


def members(data):
    """The method and field names a class file declares, read from its constant pool and members."""
    names = set()
    try:
        # Constant pool: collect Utf8 strings. Member names are among them; over-inclusive but safe
        # for an existence check, and it avoids writing a full class parser.
        count = struct.unpack(">H", data[8:10])[0]
        offset = 10
        index = 1
        while index < count:
            tag = data[offset]
            offset += 1
            if tag == 1:
                length = struct.unpack(">H", data[offset:offset + 2])[0]
                offset += 2
                names.add(data[offset:offset + length].decode("utf-8", "replace"))
                offset += length
            elif tag in (7, 8, 16, 19, 20):
                offset += 2
            elif tag in (15,):
                offset += 3
            elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
                offset += 4
            elif tag in (5, 6):
                offset += 8
                index += 1
            else:
                break
            index += 1
    except (struct.error, IndexError):
        pass
    return names


def main():
    index = load_class_index()
    cache = {}
    problems = []

    paths = sorted(path for source in SOURCES for path in glob.glob(f"{source}/*.java"))
    for path in paths:
        source = io.open(path, encoding="utf-8").read()

        # Either `@Mixin(Foo.class)` or the string form `@Mixin(targets = "a.b.Foo")`, which is what a
        # target is written as when the class is not visible at compile time.
        target = re.search(r"@Mixin\(\s*(?:value\s*=\s*)?([A-Za-z0-9_.]+)\.class", source)
        if target:
            simple = target.group(1)
        else:
            string_target = re.search(r'@Mixin\(\s*targets\s*=\s*"([A-Za-z0-9_.$]+)"', source)
            if not string_target:
                continue
            simple = string_target.group(1).replace("$", ".")
        # A nested class is written `Outer.Inner` in source and stored as `Outer$Inner` in the jar.
        candidates = {simple}
        if "." in simple:
            outer, inner = simple.rsplit(".", 1)
            candidates.add(f"{outer}${inner}")
        matches = [name for name in index
                   if name in candidates or any(name.endswith("." + c) for c in candidates)]
        if not matches:
            problems.append(f"{os.path.basename(path)}: target class {simple} not found")
            continue
        class_name = matches[0]
        if class_name not in cache:
            cache[class_name] = members(index[class_name][1])
        declared = cache[class_name]

        # Every `method = "name"` / `@Shadow` name the mixin expects to find on the target.
        for method in re.findall(r'method\s*=\s*"([A-Za-z0-9_$<>]+)"', source):
            if method.startswith("<"):
                continue
            if method not in declared:
                problems.append(f"{os.path.basename(path)}: method '{method}' missing on {class_name}")

        # Shadowed fields: `@Shadow private int foo;` and `@Shadow @Final private List<?> bar;`.
        for field in re.findall(r"@Shadow[^\n;]*?\b([A-Za-z_$][A-Za-z0-9_$]*)\s*(?:=|;)", source):
            if field not in declared:
                problems.append(f"{os.path.basename(path)}: field '{field}' missing on {class_name}")

    for line in problems:
        print(f"  BROKEN {line}")
    if not problems:
        print("  all method targets present")
    print(f"  checked {len(paths)} mixin sources")


if __name__ == "__main__":
    main()
