"""打印命中某个方法的最深一条调用栈，用来确定真正的发起者。

who_calls_model_key 只给「上一步」，而 invokedynamic 与框架层会连成一条长链。
这里直接打印一条完整栈，从顶到底，人眼一看就知道是谁。
"""
import json
import os
import subprocess
import sys
from collections import Counter

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


def frame_name(frame):
    method = frame.get("method") or {}
    type_name = (method.get("type") or {}).get("name") or "?"
    return f"{type_name}.{method.get('name', '?')}"


def main(argv):
    path = argv[0]
    needle = argv[1] if len(argv) > 1 else "equalator0"
    depth = int(argv[2]) if len(argv) > 2 else 40
    wanted = int(argv[3]) if len(argv) > 3 else 3

    samples = dump(path, "jdk.ExecutionSample", "samples_stack", depth)

    # 收集含目标帧的完整栈，按「非框架部分」去重计数
    stacks = Counter()
    representatives = {}
    for event in samples:
        values = event.get("values") or {}
        frames = (values.get("stackTrace") or {}).get("frames") or []
        if not frames:
            continue
        names = [frame_name(f) for f in frames]
        if not any(needle in n for n in names):
            continue
        # 只要业务帧，去掉 java.* / jdk.* / 方法句柄
        business = [n for n in names
                    if not n.startswith("java.") and not n.startswith("jdk.")
                    and "MethodHandle" not in n and "LambdaForm" not in n
                    and "Invokers$Holder" not in n]
        key = " <- ".join(business[:8])
        stacks[key] += 1
        representatives[key] = business

    total = sum(stacks.values())
    print(f"  命中 {total} 个采样，{len(stacks)} 种业务栈\n")
    for key, count in stacks.most_common(wanted):
        share = count * 100 / max(total, 1)
        print(f"  [{count} 次, {share:.1f}%]")
        for name in representatives[key][:20]:
            print(f"      {name}")
        print()
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
