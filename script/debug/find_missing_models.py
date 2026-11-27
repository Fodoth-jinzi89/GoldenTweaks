"""算出 allthecompressed 有多少压缩方块的贴图在本整合包里真的存在。

模组的每个方块模型都借用一个外部贴图（如 minecraft:block/iron_block 或
allthemodium:block/allthemodium_block）。整合包没有装某个模组时，那个贴图就不存在，
方块仍然注册成功——注册判断只看父方块在不在注册表里，而模型 JSON 是打包时写死的。
这种不同步就是紫黑格子的来源。

输出按命名空间汇总的缺失清单，最直接地说明是哪些模组的材料在缺。
"""
import json
import os
import sys
import zipfile
from collections import Counter, defaultdict

MODPACK = r"E:\机械动力魔法大冒险\.minecraft\versions\hkx"
MODS = os.path.join(MODPACK, "mods")
CLIENT_JAR = os.path.join(MODPACK, "hkx.jar")


def collect_textures():
    """整合包里所有可用的方块贴图 id（namespace:block/name）。

    原版贴图不在 mods 里，而在版本自身的 hkx.jar 中，必须一并扫描，
    否则 minecraft:block/* 会被误判为缺失。
    """
    available = set()
    archives = [os.path.join(MODS, name) for name in os.listdir(MODS)
                if name.endswith(".jar")]
    if os.path.exists(CLIENT_JAR):
        archives.append(CLIENT_JAR)
    # 原版贴图也可能被拆到 client-extra，一并扫
    for extra in os.listdir(MODPACK):
        if extra.endswith(".jar") and extra != "hkx.jar":
            archives.append(os.path.join(MODPACK, extra))

    for path in archives:
        try:
            with zipfile.ZipFile(path) as archive:
                for entry in archive.namelist():
                    if not entry.endswith(".png"):
                        continue
                    parts = entry.split("/")
                    if len(parts) >= 5 and parts[0] == "assets" and parts[2] == "textures":
                        texture_dir = "/".join(parts[3:-1])
                        available.add(f"{parts[1]}:{texture_dir}/{parts[-1][:-4]}")
        except (zipfile.BadZipFile, OSError):
            continue
    return available


def main():
    target = None
    for name in os.listdir(MODS):
        if "allthecompressed" in name and name.endswith(".jar"):
            target = os.path.join(MODS, name)
            break
    if not target:
        print("  找不到 allthecompressed")
        return 1

    print("  扫描整合包贴图…")
    available = collect_textures()
    print(f"    可用贴图 {len(available)} 个")

    missing_ns = Counter()
    missing_samples = defaultdict(list)
    total = 0
    missing = 0

    with zipfile.ZipFile(target) as archive:
        models = [n for n in archive.namelist()
                  if n.startswith("assets/allthecompressed/models/block/")
                  and n.endswith(".json")]
        for entry in models:
            try:
                data = json.loads(archive.read(entry))
            except (json.JSONDecodeError, UnicodeDecodeError):
                continue
            textures = data.get("textures") or {}
            base = textures.get("all")
            if not base:
                continue
            total += 1
            if base in available:
                continue
            missing += 1
            namespace = base.split(":")[0]
            missing_ns[namespace] += 1
            if len(missing_samples[namespace]) < 3:
                missing_samples[namespace].append(base)

    print(f"\n  模型 {total} 个，其中贴图缺失 {missing} 个"
          f" ({missing * 100 // max(total, 1)}%)\n")
    print("  缺失的贴图所属命名空间：")
    for namespace, count in missing_ns.most_common():
        print(f"    {namespace:24s} {count:5d}")
        for sample in missing_samples[namespace]:
            print(f"        {sample}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
