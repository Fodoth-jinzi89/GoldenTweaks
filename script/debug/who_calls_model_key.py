"""量 ModelGroupKey.equals 的调用规模：它在哪个方法之下被调用，上层是谁。

顶栈只说明卡在 equals，看不出是谁在批量比较。这里把每一次命中按「调用它的方法」归类，
能直接看出是原版去重循环，还是某个模组在反复触发。
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
    needle = argv[1] if len(argv) > 1 else "ModelGroupKey"
    depth = int(argv[2]) if len(argv) > 2 else 16

    samples = dump(path, "jdk.ExecutionSample", "samples_caller", depth)
    print(f"  采样 {len(samples)} 个，搜索 '{needle}'")

    # 找到目标帧后，取它上面第一个不属于「equals 家族」的帧
    callers = Counter()
    matched = 0
    for event in samples:
        values = event.get("values") or {}
        frames = (values.get("stackTrace") or {}).get("frames") or []
        if not frames:
            continue
        names = [frame_name(f) for f in frames]
        hit = None
        for index, name in enumerate(names):
            if needle in name:
                hit = index
                break
        if hit is None:
            continue
        matched += 1
        caller = "(none)"
        for name in names[hit + 1:]:
            short = name.rsplit(".", 1)[-1]
            # 跳过 equals/hashCode 与 JVM 的方法句柄适配帧 —— 后者只是 invokedynamic 的
            # 链接产物，继续往上才能见到真正的业务调用点。
            if short in ("equals", "hashCode", "compare", "rawCompare"):
                continue
            if ("MethodHandle" in name or "LambdaForm" in name
                    or "$$Equalator" in name or "Invokers$Holder" in name
                    or name.startswith("java/lang/invoke")):
                continue
            caller = name
            break
        callers[caller] += 1

    print(f"  命中 {matched} 个采样\n")
    print("  谁在调用它：")
    for name, count in callers.most_common(12):
        share = count * 100 / max(matched, 1)
        print(f"    {count:5d} {share:5.1f}%  {name}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
