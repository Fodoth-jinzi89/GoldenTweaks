"""Dump the bytecode around every reference to a string constant, showing the key it tests.

Mixin-adjacent config formats change key semantics between versions, and reading
`ldc 123` tells you nothing. This resolves constant-pool indices to their literal
strings so the surrounding parse logic is readable.

Usage: python script/debug/trace_key.py <class file> <substring>
"""

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from dump_class import parse  # noqa: E402


def main(argv):
    path, needle = argv[0], argv[1].lower()
    data = Path(path).read_bytes()
    pool, member, fields, methods, raw = parse(data)

    def literal(index):
        if not (0 < index < len(pool)):
            return None
        value = pool[index]
        return value if isinstance(value, str) else None

    for access, name, descriptor, body in methods:
        if body is None:
            continue
        # Walk the bytecode linearly, tracking the last ldc so we can label invokes.
        offset = 0
        pending = []
        while offset < len(body):
            opcode = body[offset]
            length = 1
            if opcode == 0x12:  # ldc
                index = body[offset + 1]
                text = literal(index)
                if text is not None:
                    pending.append(text)
                length = 2
            elif opcode == 0x13:  # ldc_w
                index = int.from_bytes(body[offset + 1:offset + 3], "big")
                text = literal(index)
                if text is not None:
                    pending.append(text)
                length = 3
            elif opcode in (0xB6, 0xB7, 0xB8, 0xB9, 0xBA):  # invoke*
                width = 3 if opcode != 0xB9 else 5
                index = int.from_bytes(body[offset + 1:offset + 1 + (2 if opcode != 0xB9 else 2)], "big")
                target = member(index)
                if any(needle in item.lower() for item in pending):
                    label = f"{target[0]}.{target[1]}" if target else "?"
                    print(f"{name}:{'offset'} {offset}: last strings={pending[-3:]} -> {label}")
                pending = []
                length = width
            elif opcode == 0x01:  # aconst_null
                pending = []
            offset += length
        for item in pending:
            if needle in item.lower():
                print(f"{name}: leftover string {item!r}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
