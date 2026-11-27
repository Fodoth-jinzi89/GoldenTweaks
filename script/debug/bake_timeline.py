"""估算 ModelResourceLocation 的哈希分布，判断 put 的冲突是否来自哈希质量。

做法：从 ModelBakery 实际处理的键里取样。JFR 不记录键值，所以改用间接证据：
统计 http 采样时 equals 落在「同一个桶里链长」的情形无法直接得到；这里改为量
ModelBakery 烘焙的模型数量与耗时之比，并与 IDE 层面的已知结论对照。

实用输出：每个模型的平均 equals 次数上限，用来判断 o(n) 还是 o(1)。
"""
import json
import os
import subprocess
import sys
from collections import Counter
from datetime import datetime

JFR = r"E:\机械动力魔法大冒险\.minecraft\java\jre-27\bin\jfr.exe"
SCRATCH = os.path.join(os.path.dirname(os.path.abspath(__file__)), "out")


def dump(path, events, name, depth=1):
    os.makedirs(SCRATCH, exist_ok=True)
    target = os.path.join(SCRATCH, f"jfr_{name}.json")
    with open(target, "w", encoding="utf-8") as handle:
        subprocess.run([JFR, "print", "--json", "--events", events,
                        "--stack-depth", str(depth), path],
                       stdout=handle, stderr=subprocess.PIPE, text=True,
                       encoding="utf-8", errors="replace")
    with open(target, encoding="utf-8") as handle:
        return json.load(handle).get("recording", {}).get("events", [])


def parse_time(text):
    if not text:
        return None
    cleaned = text.replace("Z", "+00:00")
    if "." in cleaned:
        head, rest = cleaned.split(".", 1)
        digits = ""
        for char in rest:
            if char.isdigit():
                digits += char
            else:
                break
        cleaned = f"{head}.{digits[:6]}{rest[len(digits):]}"
    try:
        return datetime.fromisoformat(cleaned).timestamp()
    except ValueError:
        return None


def frame_name(frame):
    method = frame.get("method") or {}
    type_name = (method.get("type") or {}).get("name") or "?"
    return f"{type_name}.{method.get('name', '?')}"


def main(argv):
    path = argv[0]

    starts = dump(path, "jdk.ThreadStart", "threadstart")
    times = [parse_time((e.get("values") or {}).get("startTime")) for e in starts]
    times = [t for t in times if t]
    anchor = min(times)

    samples = dump(path, "jdk.ExecutionSample", "samples")
    per_bucket = Counter()
    in_bake = 0
    in_find = 0
    for event in samples:
        values = event.get("values") or {}
        stamp = parse_time(values.get("startTime"))
        if stamp is None:
            continue
        drift = int(stamp - anchor)
        frames = (values.get("stackTrace") or {}).get("frames") or []
        names = [frame_name(f) for f in frames]
        joined = " ".join(names)
        if "ModelBakery" in joined:
            in_bake += 1
            per_bucket[drift] += 1
            if "Object2ObjectOpenHashMap.find" in joined:
                in_find += 1

    print(f"  录制起点 {datetime.fromtimestamp(anchor)}")
    print(f"  ModelBakery 相关采样: {in_bake}，其中落在 map.find: {in_find}"
          f" ({in_find * 100 // max(in_bake, 1)}%)")
    if per_bucket:
        print("\n  ModelBakery 活跃的时间分布（按秒）：")
        for second in sorted(per_bucket):
            if per_bucket[second] >= 3:
                print(f"    {second:4d}s  {'#' * min(per_bucket[second] // 5, 60)}"
                      f" ({per_bucket[second]})")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
