"""反汇编 class 文件里的指定方法，按指令序列输出。

只覆盖到能读懂控制流所需的部分：跳转、调用、字段访问、常量。
输出形如 `  12: invokevirtual #45  java/util/List.iterator()`，
足够判断循环嵌套结构、有没有重复计算、能不能安全地搬到后台线程。
"""
import os
import struct
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from dump_class import Reader, parse  # noqa: E402

# 操作码 -> (助记符, 是否为分支目标, 操作数形式)
# 操作数形式: None 无, 'u1' 1 字节, 'u2' 2 字节, 's2' 有符号 2 字节,
#             'cp' 常量池索引(u2), 'cp2' 两个常量池索引
OPS = {
    0x00: ("nop", None), 0x01: ("aconst_null", None),
    0x02: ("iconst_m1", None), 0x03: ("iconst_0", None), 0x04: ("iconst_1", None),
    0x05: ("iconst_2", None), 0x06: ("iconst_3", None), 0x07: ("iconst_4", None),
    0x08: ("iconst_5", None), 0x09: ("lconst_0", None), 0x0A: ("lconst_1", None),
    0x0B: ("fconst_0", None), 0x0C: ("fconst_1", None), 0x0D: ("fconst_2", None),
    0x0E: ("dconst_0", None), 0x0F: ("dconst_1", None),
    0x10: ("bipush", "s1"), 0x11: ("sipush", "s2"), 0x12: ("ldc", "u1"),
    0x13: ("ldc_w", "cp"), 0x14: ("ldc2_w", "cp"),
    0x15: ("iload", "u1"), 0x16: ("lload", "u1"), 0x17: ("fload", "u1"),
    0x18: ("dload", "u1"), 0x19: ("aload", "u1"),
    0x1A: ("iload_0", None), 0x1B: ("iload_1", None), 0x1C: ("iload_2", None),
    0x1D: ("iload_3", None), 0x1E: ("lload_0", None), 0x1F: ("lload_1", None),
    0x20: ("lload_2", None), 0x21: ("lload_3", None), 0x22: ("fload_0", None),
    0x23: ("fload_1", None), 0x24: ("fload_2", None), 0x25: ("fload_3", None),
    0x26: ("dload_0", None), 0x27: ("dload_1", None), 0x28: ("dload_2", None),
    0x29: ("dload_3", None), 0x2A: ("aload_0", None), 0x2B: ("aload_1", None),
    0x2C: ("aload_2", None), 0x2D: ("aload_3", None),
    0x2E: ("iaload", None), 0x2F: ("laload", None), 0x30: ("faload", None),
    0x31: ("daload", None), 0x32: ("aaload", None), 0x33: ("baload", None),
    0x34: ("caload", None), 0x35: ("saload", None),
    0x36: ("istore", "u1"), 0x37: ("lstore", "u1"), 0x38: ("fstore", "u1"),
    0x39: ("dstore", "u1"), 0x3A: ("astore", "u1"),
    0x3B: ("istore_0", None), 0x3C: ("istore_1", None), 0x3D: ("istore_2", None),
    0x3E: ("istore_3", None), 0x3F: ("lstore_0", None), 0x40: ("lstore_1", None),
    0x41: ("lstore_2", None), 0x42: ("lstore_3", None), 0x43: ("fstore_0", None),
    0x44: ("fstore_1", None), 0x45: ("fstore_2", None), 0x46: ("fstore_3", None),
    0x47: ("dstore_0", None), 0x48: ("dstore_1", None), 0x49: ("dstore_2", None),
    0x4A: ("dstore_3", None), 0x4B: ("astore_0", None), 0x4C: ("astore_1", None),
    0x4D: ("astore_2", None), 0x4E: ("astore_3", None),
    0x4F: ("iastore", None), 0x50: ("lastore", None), 0x51: ("fastore", None),
    0x52: ("dastore", None), 0x53: ("aastore", None), 0x54: ("bastore", None),
    0x55: ("castore", None), 0x56: ("sastore", None),
    0x57: ("pop", None), 0x58: ("pop2", None), 0x59: ("dup", None),
    0x5A: ("dup_x1", None), 0x5B: ("dup_x2", None), 0x5C: ("dup2", None),
    0x5D: ("dup2_x1", None), 0x5E: ("dup2_x2", None), 0x5F: ("swap", None),
    0x60: ("iadd", None), 0x61: ("ladd", None), 0x62: ("fadd", None),
    0x63: ("dadd", None), 0x64: ("isub", None), 0x65: ("lsub", None),
    0x66: ("fsub", None), 0x67: ("dsub", None), 0x68: ("imul", None),
    0x69: ("lmul", None), 0x6A: ("fmul", None), 0x6B: ("dmul", None),
    0x6C: ("idiv", None), 0x6D: ("ldiv", None), 0x6E: ("fdiv", None),
    0x6F: ("ddiv", None), 0x70: ("irem", None), 0x71: ("lrem", None),
    0x72: ("frem", None), 0x73: ("drem", None), 0x74: ("ineg", None),
    0x75: ("lneg", None), 0x76: ("fneg", None), 0x77: ("dneg", None),
    0x78: ("ishl", None), 0x79: ("lshl", None), 0x7A: ("ishr", None),
    0x7B: ("lshr", None), 0x7C: ("iushr", None), 0x7D: ("lushr", None),
    0x7E: ("iand", None), 0x7F: ("land", None), 0x80: ("ior", None),
    0x81: ("lor", None), 0x82: ("ixor", None), 0x83: ("lxor", None),
    0x84: ("iinc", "u1u1"),
    0x85: ("i2l", None), 0x86: ("i2f", None), 0x87: ("i2d", None),
    0x88: ("l2i", None), 0x89: ("l2f", None), 0x8A: ("l2d", None),
    0x8B: ("f2i", None), 0x8C: ("f2l", None), 0x8D: ("f2d", None),
    0x8E: ("d2i", None), 0x8F: ("d2l", None), 0x90: ("d2f", None),
    0x91: ("i2b", None), 0x92: ("i2c", None), 0x93: ("i2s", None),
    0x94: ("lcmp", None), 0x95: ("fcmpl", None), 0x96: ("fcmpg", None),
    0x97: ("dcmpl", None), 0x98: ("dcmpg", None),
    0x99: ("ifeq", "s2"), 0x9A: ("ifne", "s2"), 0x9B: ("iflt", "s2"),
    0x9C: ("ifge", "s2"), 0x9D: ("ifgt", "s2"), 0x9E: ("ifle", "s2"),
    0x9F: ("if_icmpeq", "s2"), 0xA0: ("if_icmpne", "s2"), 0xA1: ("if_icmplt", "s2"),
    0xA2: ("if_icmpge", "s2"), 0xA3: ("if_icmpgt", "s2"), 0xA4: ("if_icmple", "s2"),
    0xA5: ("if_acmpeq", "s2"), 0xA6: ("if_acmpne", "s2"),
    0xA7: ("goto", "s2"), 0xA8: ("jsr", "s2"), 0xA9: ("ret", "u1"),
    0xAA: ("tableswitch", "table"), 0xAB: ("lookupswitch", "lookup"),
    0xAC: ("ireturn", None), 0xAD: ("lreturn", None), 0xAE: ("freturn", None),
    0xAF: ("dreturn", None), 0xB0: ("areturn", None), 0xB1: ("return", None),
    0xB2: ("getstatic", "cp"), 0xB3: ("putstatic", "cp"),
    0xB4: ("getfield", "cp"), 0xB5: ("putfield", "cp"),
    0xB6: ("invokevirtual", "cp"), 0xB7: ("invokespecial", "cp"),
    0xB8: ("invokestatic", "cp"), 0xB9: ("invokeinterface", "iface"),
    0xBA: ("invokedynamic", "indy"), 0xBB: ("new", "cp"),
    0xBC: ("newarray", "u1"), 0xBD: ("anewarray", "cp"),
    0xBE: ("arraylength", None), 0xBF: ("athrow", None),
    0xC0: ("checkcast", "cp"), 0xC1: ("instanceof", "cp"),
    0xC2: ("monitorenter", None), 0xC3: ("monitorexit", None),
    0xC4: ("wide", "wide"), 0xC5: ("multianewarray", "multi"),
    0xC6: ("ifnull", "s2"), 0xC7: ("ifnonnull", "s2"),
    0xC8: ("goto_w", "s4"), 0xC9: ("jsr_w", "s4"),
}


def method_code(body):
    reader = Reader(body)
    reader.u2()
    reader.u2()
    length = reader.u4()
    return body[reader.pos:reader.pos + length]


def main(argv):
    path = argv[0]
    wanted = argv[1] if len(argv) > 1 else None

    with open(path, "rb") as handle:
        data = handle.read()
    pool, member, fields, methods, _ = parse(data)

    def utf(offset):
        if 0 < offset < len(pool):
            entry = pool[offset]
            return entry if isinstance(entry, str) else None
        return None

    def describe(operand):
        resolved = member(operand)
        if not resolved:
            value = utf(operand)
            return f"#{operand} {value}" if value else f"#{operand}"
        owner, name, desc = resolved
        return f"#{operand} {owner}.{name}{desc}"

    for access, name, descriptor, body in methods:
        if wanted and wanted not in name:
            continue
        if body is None:
            continue
        code = method_code(body)
        print(f"\n=== {name}{descriptor}  ({len(code)} 字节) ===\n")
        pos = 0
        while pos < len(code):
            start = pos
            opcode = code[pos]
            pos += 1
            entry = OPS.get(opcode)
            if entry is None:
                print(f"  {start:4d}: <未知 0x{opcode:02x}>")
                break
            mnemonic, form = entry

            if form is None:
                print(f"  {start:4d}: {mnemonic}")
            elif form == "u1":
                value = code[pos]
                pos += 1
                print(f"  {start:4d}: {mnemonic} {value}")
            elif form == "s1":
                value = struct.unpack_from(">b", code, pos)[0]
                pos += 1
                print(f"  {start:4d}: {mnemonic} {value}")
            elif form == "s2":
                value = struct.unpack_from(">h", code, pos)[0]
                pos += 2
                target = start + value
                print(f"  {start:4d}: {mnemonic} -> {target}")
            elif form == "cp":
                operand = struct.unpack_from(">H", code, pos)[0]
                pos += 2
                print(f"  {start:4d}: {mnemonic} {describe(operand)}")
            elif form == "u1u1":
                index, const = code[pos], struct.unpack_from(">b", code, pos + 1)[0]
                pos += 2
                print(f"  {start:4d}: {mnemonic} {index}, {const}")
            elif form == "iface":
                operand = struct.unpack_from(">H", code, pos)[0]
                count, zero = code[pos + 2], code[pos + 3]
                pos += 4
                print(f"  {start:4d}: {mnemonic} {describe(operand)} (count={count})")
            elif form == "indy":
                operand = struct.unpack_from(">H", code, pos)[0]
                pos += 4
                print(f"  {start:4d}: {mnemonic} #{operand}")
            elif form == "s4":
                value = struct.unpack_from(">i", code, pos)[0]
                pos += 4
                print(f"  {start:4d}: {mnemonic} -> {start + value}")
            elif form == "table":
                pad = (4 - pos % 4) % 4
                pos += pad
                default, low, high = struct.unpack_from(">iii", code, pos)
                pos += 12
                print(f"  {start:4d}: {mnemonic} low={low} high={high} "
                      f"default->{start + default}")
                for i in range(high - low + 1):
                    offset = struct.unpack_from(">i", code, pos)[0]
                    pos += 4
                    print(f"          case {low + i}: -> {start + offset}")
            elif form == "lookup":
                pad = (4 - pos % 4) % 4
                pos += pad
                default, pairs = struct.unpack_from(">ii", code, pos)
                pos += 8
                print(f"  {start:4d}: {mnemonic} pairs={pairs} default->{start + default}")
                for _ in range(pairs):
                    key, offset = struct.unpack_from(">ii", code, pos)
                    pos += 8
                    print(f"          case {key}: -> {start + offset}")
            elif form == "multi":
                operand, dims = struct.unpack_from(">HB", code, pos)
                pos += 3
                print(f"  {start:4d}: {mnemonic} {describe(operand)} dims={dims}")
            elif form == "wide":
                nxt = code[pos]
                pos += 1
                index = struct.unpack_from(">H", code, pos)[0]
                pos += 2
                print(f"  {start:4d}: wide 0x{nxt:02x} {index}")
            else:
                print(f"  {start:4d}: {mnemonic} <{form}>")
                break
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
