"""打印顶栈为 ThreadLocal 的完整调用链（前后各若干层），用来判断是谁在反复读它。

顶栈只能说明「卡在查表」，完整链才能看出「查的是哪张表、由谁发起」。
这里按完整链去重统计，一条链一票，按出现次数排序。
"""
import json
import os
import subprocess
import sys
from collections import Counter
from datetime import datetime

JFR = r"E:\机械动力魔法大冒险\.minecraft\java\jre-27\bin\jfr.exe"
SCRATCH = os.environ.get(
    "COMMANDCODE_SCRATCHPAD",
    r"C:\Users\peiranyu\AppData\Local\Temp\commandcode"
    r"\E--MCreator-Workspaces-GoldenTweaks"
    r"\b7e16c7b-fba8-498c-a919-c3a4c9172725\scratchpad")


def dump(path, events, name, depth):
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

    starts = dump(path, "jdk.ThreadStart", "threadstart", 1)
    anchor = None
    for event in starts:
        values = event.get("values", {})
        if (values.get("eventThread") or {}).get("javaName") == "Server thread":
            anchor = parse_time(values.get("startTime"))
            break

    samples = dump(path, "jdk.ExecutionSample", "samples_chain", 40)
    chains = Counter()
    for event in samples:
        values = event.get("values", {})
        if (values.get("sampledThread") or {}).get("javaName") != "Server thread":
            continue
        stamp = parse_time(values.get("startTime"))
        if stamp is None or anchor is None or stamp < anchor:
            continue
        frames = ((values.get("stackTrace") or {}).get("frames") or [])
        if not frames or "ThreadLocal" not in frame_name(frames[0]):
            continue
        # 取前 6 层作为链的指纹
        chains[" <- ".join(frame_name(f) for f in frames[:6])] += 1

    print(f"  {len(chains)} 种调用链，按出现次数排序：")
    for chain, count in chains.most_common(6):
        print(f"\n  [{count} 次]")
        for part in chain.split(" <- "):
            print(f"      {part}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
