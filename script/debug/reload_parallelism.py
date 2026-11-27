"""统计所有 ResourceReload worker 的负载分布。

资源重载本该由若干 worker 并行分摊。如果某一个 worker 的采样数远超其余，说明那一份工作
无法拆分（或者被链式依赖串起来了），是并行度的瓶颈，而不是总工作量的问题。
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


def main(argv):
    path = argv[0]
    low = float(argv[1]) if len(argv) > 1 else 55.0
    high = float(argv[2]) if len(argv) > 2 else 85.0

    starts = dump(path, "jdk.ThreadStart", "threadstart")
    times = [parse_time((e.get("values") or {}).get("startTime")) for e in starts]
    times = [t for t in times if t]
    anchor = min(times)
    print(f"  录制起点 {datetime.fromtimestamp(anchor)}，窗口 {low:.0f}~{high:.0f}s")

    samples = dump(path, "jdk.ExecutionSample", "samples")
    per_worker = Counter()
    for event in samples:
        values = event.get("values") or {}
        stamp = parse_time(values.get("startTime"))
        if stamp is None:
            continue
        drift = stamp - anchor
        if drift < low or drift > high:
            continue
        name = (values.get("sampledThread") or {}).get("javaName") or "?"
        per_worker[name] += 1

    total = sum(per_worker.values())
    print(f"  窗口内 {total} 个采样\n")

    reload = {n: c for n, c in per_worker.items() if "ResourceReload" in n}
    reload_total = sum(reload.values())
    print(f"  ResourceReload worker 合计 {reload_total} "
          f"({reload_total * 100 // max(total, 1)}%)，共 {len(reload)} 个")
    for name, count in sorted(reload.items(), key=lambda kv: -kv[1])[:14]:
        share = count * 100 / max(reload_total, 1)
        bar = "#" * int(share / 2)
        print(f"    {name:28s} {count:5d} {share:5.1f}%  {bar}")
    print()

    print("  其它线程:")
    for name, count in sorted(per_worker.items(), key=lambda kv: -kv[1])[:8]:
        if "ResourceReload" in name:
            continue
        print(f"    {name:28s} {count:5d} {count * 100 / max(total,1):5.1f}%")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
