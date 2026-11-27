"""定位某个方法来自哪个类：在采样里找出含该名字的完整栈。

只看方法名（如 equalator0）无法判断归属，必须看它上层的类。这里搜索所有采样，
把含目标名字的栈按「所属类」聚合，直接给出是谁在调用。
"""
import json
import os
import subprocess
import sys
from collections import Counter
from datetime import datetime

JFR = r"E:\机械动力魔法大冒险\.minecraft\java\jre-27\bin\jfr.exe"
SCRATCH = os.path.join(os.path.dirname(os.path.abspath(__file__)), "out")


def dump(path, events, name, depth):
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
    needle = argv[1]
    depth = int(argv[2]) if len(argv) > 2 else 30

    samples = dump(path, "jdk.ExecutionSample", f"samples_{needle}", depth)
    print(f"  采样 {len(samples)} 个，搜索 '{needle}'")

    by_owner = Counter()
    by_thread = Counter()
    examples = {}
    for event in samples:
        values = event.get("values") or {}
        frames = (values.get("stackTrace") or {}).get("frames") or []
        if not frames:
            continue
        names = [frame_name(f) for f in frames]
        if not any(needle in n for n in names):
            continue
        thread = (values.get("sampledThread") or {}).get("javaName") or "?"
        by_thread[thread] += 1
        # 找最靠近底部的第一个非 java.* 帧，作为归属
        owner = "(unknown)"
        for name in reversed(names):
            if not name.startswith("java.") and not name.startswith("jdk."):
                owner = name.rsplit(".", 1)[0]
                break
        by_owner[owner] += 1
        if owner not in examples:
            examples[owner] = names[:8]

    total = sum(by_owner.values())
    print(f"  命中 {total} 个采样\n")
    print("  按线程:")
    for name, count in by_thread.most_common(6):
        print(f"    {count:5d}  {name}")
    print()
    print("  按归属类:")
    for name, count in by_owner.most_common(8):
        print(f"    {count:5d}  {name}")
    print()
    for owner, names in list(examples.items())[:3]:
        print(f"  [{owner}] 的调用链:")
        for name in names:
            print(f"      {name}")
        print()
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
