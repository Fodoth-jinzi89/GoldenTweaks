"""列出某个 class 里所有方法引用到「别的类」的成员，按调用出现的先后分组。

用来回答「这个方法到底调了什么」。常量池给出成员，方法上的 Code 属性给出该方法实际用到的
常量编号，两者交叉就能定位到具体调用了谁，不必反编译。
"""
import struct
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from dump_class import Reader, parse  # noqa: E402


def method_code(body):
    reader = Reader(body)
    reader.u2()
    reader.u2()
    length = reader.u4()
    return body[reader.pos:reader.pos + length]


def collect_operands(code):
    """扫一遍字节码，收集所有常量池操作数。"""
    operands = []
    pos = 0
    length = len(code)
    while pos < length:
        opcode = code[pos]
        if opcode in (0xB2, 0xB3, 0xB4, 0xB5, 0xBB, 0xBD, 0xC0, 0xC1, 0xB6, 0xB7, 0xB8, 0xB9):
            operands.append(struct.unpack_from(">H", code, pos + 1)[0])
            pos += 3 if opcode != 0xB9 else 5
        elif opcode in (0x12, 0x10, 0x15, 0x16, 0x17, 0x18, 0x19,
                        0x36, 0x37, 0x38, 0x39, 0x3A, 0xA9, 0xBC):
            pos += 2
        elif opcode in (0x11, 0x13, 0x14, 0x84):
            pos += 3
        elif opcode in (0x99, 0x9A, 0x9B, 0x9C, 0x9D, 0x9E, 0x9F, 0xA0, 0xA1, 0xA2, 0xA3,
                        0xA4, 0xA5, 0xA6, 0xA7, 0xA8, 0xC6, 0xC7):
            pos += 3
        elif opcode in (0xC8, 0xC9):
            pos += 5
        elif opcode == 0xAA:
            pos += 1
            pos += (4 - pos % 4) % 4
            _, low, high = struct.unpack_from(">iii", code, pos)
            pos += 12 + (high - low + 1) * 4
        elif opcode == 0xAB:
            pos += 1
            pos += (4 - pos % 4) % 4
            _, pairs = struct.unpack_from(">ii", code, pos)
            pos += 8 + pairs * 8
        elif opcode == 0xC5:
            pos += 4
        else:
            pos += 1
    return operands


def main(argv):
    path = argv[0]
    wanted = argv[1] if len(argv) > 1 else None
    only_foreign = "--all" not in argv

    with open(path, "rb") as handle:
        data = handle.read()
    pool, member, fields, methods, _ = parse(data)

    for access, name, descriptor, body in methods:
        if wanted and wanted not in name:
            continue
        if body is None:
            continue
        code = method_code(body)
        print(f"  {name}{descriptor}")
        seen = set()
        for operand in collect_operands(code):
            resolved = member(operand)
            if not resolved:
                continue
            owner, member_name, desc = resolved
            if not owner:
                continue
            if only_foreign and ("mastermarisa" in owner or "maid_restaurant" in owner):
                pass
            key = (owner, member_name)
            if key in seen:
                continue
            seen.add(key)
            print(f"      {owner}.{member_name}{desc}")
        print()
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
