"""分析启动阶段：从 JVM 启动到进入世界之前这一段。

与 analyse_phases.py 的区别是锚点：那个以 Server thread 启动为锚点（世界加载开始），
这个以录制起点为锚点（JVM 启动），因此能看到 mod 加载、资源重载、主菜单这些更早的阶段。

输出两件事：
  1. 按线程的采样分布与活跃区间——哪个线程在什么时候真正占着 CPU；
  2. 关键线程的分段热点——把一个阶段归因到具体方法。
"""
import json
import os
import subprocess
import sys
from collections import Counter, defaultdict
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
    path = argv[0] if argv else r"E:\jfr\worldload.jfr"
    window = float(argv[1]) if len(argv) > 1 else 60.0
    span = float(argv[2]) if len(argv) > 2 else 10.0

    starts = dump(path, "jdk.ThreadStart", "threadstart")
    if not starts:
        print("  没有线程启动事件")
        return 1

    # 录制起点 = 最早的时间戳
    times = []
    for event in starts:
        stamp = parse_time((event.get("values") or {}).get("startTime"))
        if stamp is not None:
            times.append(stamp)
    if not times:
        print("  无法确定录制起点")
        return 1
    anchor = min(times)
    print(f"  录制起点: {datetime.fromtimestamp(anchor)}")
    print(f"  分析窗口: 前 {window:.0f} 秒\n")

    # 世界加载从哪里开始
    for event in starts:
        values = event.get("values") or {}
        if (values.get("eventThread") or {}).get("javaName") == "Server thread":
            stamp = parse_time(values.get("startTime"))
            if stamp:
                print(f"  Server thread（世界加载起点）: +{stamp - anchor:.1f}s\n")
            break

    samples = dump(path, "jdk.ExecutionSample", "samples")
    per_thread = Counter()
    first_seen = {}
    last_seen = {}
    total = 0
    for event in samples:
        values = event.get("values") or {}
        stamp = parse_time(values.get("startTime"))
        if stamp is None or stamp < anchor or stamp - anchor > window:
            continue
        total += 1
        name = (values.get("sampledThread") or {}).get("javaName") or "?"
        per_thread[name] += 1
        drift = stamp - anchor
        first_seen[name] = min(first_seen.get(name, drift), drift)
        last_seen[name] = max(last_seen.get(name, drift), drift)

    print(f"  窗口内 {total} 个采样\n")
    print("  线程                                   采样   占比  活跃区间")
    for name, count in per_thread.most_common(18):
        print(f"  {name:36s} {count:5d} {count / total * 100:5.1f}%  "
              f"{first_seen[name]:5.1f}s ~ {last_seen[name]:5.1f}s")

    # 分段热点：挑出最占 CPU 的三个线程
    print()
    for name, count in per_thread.most_common(3):
        print(f"  === [{name}] 分段热点（每 {span:.0f} 秒）===")
        buckets = defaultdict(Counter)
        for event in samples:
            values = event.get("values") or {}
            if (values.get("sampledThread") or {}).get("javaName") != name:
                continue
            stamp = parse_time(values.get("startTime"))
            if stamp is None or stamp < anchor or stamp - anchor > window:
                continue
            bucket = int((stamp - anchor) // span)
            frames = (values.get("stackTrace") or {}).get("frames") or []
            buckets[bucket][frame_name(frames[0]) if frames else "(no stack)"] += 1

        for bucket in sorted(buckets):
            bucket_total = sum(buckets[bucket].values())
            low = bucket * span
            print(f"    {low:4.0f}s~{low + span:4.0f}s ({bucket_total:4d}) "
                  + " | ".join(f"{n.split('.')[-1]} {c * 100 // bucket_total}%"
                               for n, c in buckets[bucket].most_common(3)))
        print()
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
