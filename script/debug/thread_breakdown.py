"""按线程汇总整个窗口的 CPU 占用，并给出各线程的起止时间。

用来回答「进世界那段时间，CPU 到底花在哪几个线程上」。只看单个线程的热点会漏掉
「某个后台线程一直占着一个核」这类问题。
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
    window = float(argv[1]) if len(argv) > 1 else 150.0

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
    print(f"  锚点（Server thread 启动）: {datetime.fromtimestamp(anchor)}")

    samples = dump(path, "jdk.ExecutionSample", "samples")
    per_thread = Counter()
    first_seen = {}
    last_seen = {}
    total = 0
    for event in samples:
        values = event.get("values", {})
        stamp = parse_time(values.get("startTime"))
        if stamp is None or stamp < anchor or stamp - anchor > window:
            continue
        total += 1
        name = (values.get("sampledThread") or {}).get("javaName") or "?"
        per_thread[name] += 1
        drift = stamp - anchor
        first_seen[name] = min(first_seen.get(name, drift), drift)
        last_seen[name] = max(last_seen.get(name, drift), drift)

    print(f"  窗口内共 {total} 个采样（{window:.0f} 秒）\n")
    print("  线程                              采样   占比   首次出现   最后出现")
    for name, count in per_thread.most_common(15):
        share = count / total * 100
        print(f"  {name:32s} {count:6d} {share:5.1f}%   "
              f"{first_seen[name]:6.1f}s  {last_seen[name]:7.1f}s")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
