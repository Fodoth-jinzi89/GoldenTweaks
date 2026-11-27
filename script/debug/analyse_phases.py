"""把窗口切成若干段，逐段统计各线程的顶栈热点。

加载是分阶段的：前半段可能在等资源、后半段才真正跑初始化逻辑。把整段混在一起统计，
平均值会掩盖掉「某 10 秒里全是某一个方法」这种决定性信息。
"""
import json
import os
import subprocess
import sys
from collections import Counter, defaultdict
from datetime import datetime

JFR = r"E:\机械动力魔法大冒险\.minecraft\java\jre-27\bin\jfr.exe"
SCRATCH = os.environ.get(
    "COMMANDCODE_SCRATCHPAD",
    r"C:\Users\peiranyu\AppData\Local\Temp\commandcode"
    r"\E--MCreator-Workspaces-GoldenTweaks"
    r"\b7e16c7b-fba8-498c-a919-c3a4c9172725\scratchpad")


def dump(path, events, name, depth=1):
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
    span = float(argv[1]) if len(argv) > 1 else 16.0
    total_window = float(argv[2]) if len(argv) > 2 else 80.0
    focus = argv[3] if len(argv) > 3 else "Server thread"

    starts = dump(path, "jdk.ThreadStart", "threadstart")
    anchor = None
    for event in starts:
        values = event.get("values", {})
        if (values.get("eventThread") or {}).get("javaName") == "Server thread":
            anchor = parse_time(values.get("startTime"))
            break
    if anchor is None:
        print("  未找到 Server thread")
        return 1

    samples = dump(path, "jdk.ExecutionSample", "samples")
    buckets = defaultdict(Counter)
    counts = Counter()
    for event in samples:
        values = event.get("values", {})
        if (values.get("sampledThread") or {}).get("javaName") != focus:
            continue
        stamp = parse_time(values.get("startTime"))
        if stamp is None or stamp < anchor or stamp - anchor > total_window:
            continue
        drift = stamp - anchor
        bucket = int(drift // span)
        counts[bucket] += 1
        buckets[bucket][frame_name((values.get("stackTrace") or {}).get("frames", [{}])[0])] += 1

    print(f"  [{focus}] 按 {span:.0f} 秒分段，前 {total_window:.0f} 秒\n")
    for bucket in sorted(buckets):
        total = counts[bucket]
        low = bucket * span
        print(f"  --- {low:5.0f}s ~ {low + span:5.0f}s ：{total} 个采样 ---")
        for name, count in buckets[bucket].most_common(5):
            print(f"        {count / total * 100:5.1f}%  {name}")
        print()
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
