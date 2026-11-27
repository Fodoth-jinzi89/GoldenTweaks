"""直接读 class 文件，列出类里的字段与方法签名，以及方法中引用到的常量。

只为回答一个问题：OEI 到底用了几张 ThreadLocal、字段名是什么、在哪些方法里读写。
没有反编译器时，常量池加方法表已经够用——外部引用和字段引用都写在常量池里。
"""
import struct
import sys


class Reader:
    def __init__(self, data):
        self.data = data
        self.pos = 0

    def u1(self):
        value = self.data[self.pos]
        self.pos += 1
        return value

    def u2(self):
        value = struct.unpack_from(">H", self.data, self.pos)[0]
        self.pos += 2
        return value

    def u4(self):
        value = struct.unpack_from(">I", self.data, self.pos)[0]
        self.pos += 4
        return value

    def skip(self, count):
        self.pos += count


def parse(data):
    reader = Reader(data)
    assert reader.u4() == 0xCAFEBABE
    reader.u2()  # minor
    reader.u2()  # major

    count = reader.u2()
    pool = [None] * count
    index = 1
    while index < count:
        tag = reader.u1()
        if tag == 1:  # Utf8
            length = reader.u2()
            pool[index] = data[reader.pos:reader.pos + length].decode("utf-8", "replace")
            reader.skip(length)
        elif tag in (7, 8, 16, 19, 20):  # Class, String, MethodType, Module, Package
            pool[index] = ("ref", reader.u2())
        elif tag in (15,):  # MethodHandle
            reader.skip(3)
        elif tag in (3, 4):  # Integer, Float
            reader.skip(4)
        elif tag in (5, 6):  # Long, Double
            reader.skip(8)
            index += 1
        elif tag in (9, 10, 11, 12, 17, 18):  # Field/Method/InterfaceMethod/NameAndType...
            pool[index] = ("pair", reader.u2(), reader.u2())
        else:
            raise ValueError(f"未知常量标签 {tag} @ {index}")
        index += 1

    def utf(offset):
        entry = pool[offset]
        return entry if isinstance(entry, str) else None

    def member(offset):
        entry = pool[offset]
        if not isinstance(entry, tuple) or entry[0] != "pair":
            return None
        class_index, name_type = entry[1], entry[2]
        class_entry = pool[class_index]
        name_entry = pool[name_type]
        if not isinstance(class_entry, tuple) or not isinstance(name_entry, tuple):
            return None
        return (utf(class_entry[1]), utf(name_entry[1]), utf(name_entry[2]))

    reader.skip(6)  # access, this, super

    interfaces = reader.u2()
    reader.skip(interfaces * 2)

    def skip_members():
        total = reader.u2()
        result = []
        for _ in range(total):
            access = reader.u2()
            name = utf(reader.u2())
            descriptor = utf(reader.u2())
            attributes = reader.u2()
            body = None
            for _ in range(attributes):
                attr_name = utf(reader.u2())
                length = reader.u4()
                if attr_name == "Code":
                    raw = data[reader.pos:reader.pos + length]
                    body = raw
                reader.skip(length)
            result.append((access, name, descriptor, body))
        return result

    fields = skip_members()
    methods = skip_members()
    return pool, member, fields, methods, data


def main(argv):
    path = argv[0]
    with open(path, "rb") as handle:
        data = handle.read()
    pool, member, fields, methods, raw = parse(data)

    print(f"  {path}")
    print("  字段:")
    for access, name, descriptor, _ in fields:
        print(f"    {name}  {descriptor}  (access 0x{access:04x})")

    print("  方法:")
    for access, name, descriptor, body in methods:
        print(f"    {name}{descriptor}  (access 0x{access:04x}, "
              f"{'有字节码' if body else '无'})")

    print()
    print("  常量池里引用的外部成员（前 60 条）:")
    seen = set()
    shown = 0
    for entry in pool:
        if not isinstance(entry, tuple) or entry[0] != "pair":
            continue
        resolved = member(pool.index(entry) if entry in pool else 0)
    for i, entry in enumerate(pool):
        if not isinstance(entry, tuple) or entry[0] != "pair":
            continue
        resolved = member(i)
        if not resolved:
            continue
        owner, name, descriptor = resolved
        key = (owner, name)
        if key in seen:
            continue
        seen.add(key)
        if owner and ("ThreadLocal" in owner or "oneenoughitem" in owner):
            print(f"    {owner}.{name}{descriptor}")
        shown += 1
        if shown > 60:
            break
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
