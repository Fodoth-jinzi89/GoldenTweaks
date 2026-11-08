"""Regenerate canonical / corrected loot tables from the local mod jars.

背景
----
1.21.1 的 ``ItemPredicate`` 编解码器只认 ``items`` / ``count`` / ``components`` /
``predicates`` 四个字段（见 ``net.minecraft.advancements.critereon.ItemPredicate``）。部分第三方
构建仍然使用 1.20 的写法：

    "predicate": { "enchantments": [ { "enchantment": "minecraft:silk_touch", ... } ] }

该字段在新版会被直接忽略，于是 predicate 退化成“匹配任意物品”的空谓词：``match_tool`` 恒为真。
矿石会因此用普通镐也掉整块，树叶 / 玻璃 / 书架等也会无视精准采集或剪刀直接掉落自身，
实体战利品表里的“掠夺加成”也会无条件生效。

正确写法是：

    "predicate": { "predicates": { "minecraft:enchantments": [ { "enchantments": "minecraft:silk_touch", ... } ] } }

本脚本从 ``libs/compileOnly`` 的模组 jar 读取战利品表，把谓词统一成 1.21.1 的写法，
再写入本模组资源目录，使 GoldenTweaks 覆盖任何已安装构建自带的版本。

两种处理模式
------------
* ``copy``   —— 无条件复制（可按文件名后缀过滤），用于给 AllTheOres 提供规范副本。
* ``legacy`` —— 只复制仍在使用旧式 ``predicate.enchantments`` 写法的表。
"""

import json
import re
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LIBS = ROOT / "libs" / "compileOnly"
RESOURCES = ROOT / "src" / "main" / "resources"

# (jar 文件名正则, 模式, 文件名后缀过滤)
TARGETS = [
    (re.compile(r"^alltheores-.*\.jar$"), "copy", "_ore.json"),
    (re.compile(r"^thermal.*\.jar$"), "legacy", None),
    (re.compile(r"^spectrum-.*\.jar$"), "legacy", None),
    (re.compile(r"^irons_spellbooks-.*\.jar$"), "legacy", None),
]

LOOT_TABLE_RE = re.compile(r"^data/(?P<ns>[^/]+)/loot_table/(?P<rest>.+\.json)$")
MATCH_TOOL = "minecraft:match_tool"


def find_jars(pattern):
    matches = sorted((p for p in LIBS.glob("*.jar") if pattern.match(p.name)), key=lambda p: p.name)
    if not matches:
        raise FileNotFoundError(f"找不到匹配 {pattern.pattern} 的 jar")
    return matches


def normalize_predicates(node):
    """把 ``match_tool`` 里旧的 ``predicate.enchantments`` 升级为 1.21.1 的 ``predicate.predicates``。

    返回是否发生了修改。
    """

    changed = False
    if isinstance(node, dict):
        predicate = node.get("predicate")
        if (
            node.get("condition") == MATCH_TOOL
            and isinstance(predicate, dict)
            and "enchantments" in predicate
            and "predicates" not in predicate
        ):
            entries = []
            for entry in predicate["enchantments"]:
                if isinstance(entry, dict) and "enchantment" in entry and "enchantments" not in entry:
                    entry = {("enchantments" if k == "enchantment" else k): v for k, v in entry.items()}
                entries.append(entry)
            predicate.clear()
            predicate["predicates"] = {"minecraft:enchantments": entries}
            changed = True
        for value in node.values():
            changed = normalize_predicates(value) or changed
    elif isinstance(node, list):
        for value in node:
            changed = normalize_predicates(value) or changed
    return changed


def process(jar_path, mode, suffix):
    written = []
    with zipfile.ZipFile(jar_path) as jar:
        for name in sorted(jar.namelist()):
            match = LOOT_TABLE_RE.match(name)
            if not match:
                continue
            if mode == "copy" and suffix and not name.endswith(suffix):
                continue
            payload = json.loads(jar.read(name).decode("utf-8"))
            changed = normalize_predicates(payload)
            if mode == "legacy" and not changed:
                continue
            target = RESOURCES / "data" / match.group("ns") / "loot_table" / match.group("rest")
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(json.dumps(payload, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
            written.append(target)
    return written


def main():
    total = 0
    for pattern, mode, suffix in TARGETS:
        for jar in find_jars(pattern):
            written = process(jar, mode, suffix)
            total += len(written)
            print(f"{jar.name}: {len(written)} tables ({mode})")
    print(f"total {total}")


if __name__ == "__main__":
    main()
