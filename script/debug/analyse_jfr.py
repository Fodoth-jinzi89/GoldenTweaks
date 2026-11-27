"""解析 JFR 录制，找出进世界那一段的 CPU 热点。

结构：
    jfr print --json 输出 {"recording": {"events": [{"type", "values": {...}}]}}
    时间在 values.startTime，是 ISO-8601 字符串。

步骤：
  1. 以 Server thread 启动时刻为锚点（单人游戏里服务端在开世界时才起）；
  2. 锚点后的一段窗口内，按线程聚合 ExecutionSample 的顶栈；
  3. 报告窗口内最长的停顿。

结论因此是「谁在占 CPU」，而不是「日志里谁最后说话」。
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


def dump(path, events, name):
    """把事件导出成 JSON 文件，返回解析后的列表。"""
    target = os.path.join(SCRATCH, f"jfr_{name}.json")
    with open(target, "w", encoding="utf-8") as handle:
        result = subprocess.run(
            [JFR, "print", "--json", "--events", events,
             "--stack-depth", "1", path],
            stdout=handle, stderr=subprocess.PIPE, text=True,
            encoding="utf-8", errors="replace")
    if result.returncode != 0:
        print(f"  jfr 失败: {(result.stderr or '')[:300]}")
        return []
    with open(target, encoding="utf-8") as handle:
        data = json.load(handle)
    return data.get("recording", {}).get("events", [])


def parse_time(text):
    """ISO-8601 -> 秒（浮点）。"""
    if not text:
        return None
    cleaned = text.replace("Z", "+00:00")
    # 截断到微秒，Python 的 fromisoformat 只认 6 位小数
    if "." in cleaned:
        head, rest = cleaned.split(".", 1)
        digits = ""
        for char in rest:
            if char.isdigit():
                digits += char
            else:
                break
        tail = rest[len(digits):]
        cleaned = f"{head}.{digits[:6]}{tail}"
    try:
        return datetime.fromisoformat(cleaned).timestamp()
    except ValueError:
        return None


def thread_name(values, key="sampledThread"):
    return values.get(key, {}).get("javaName") or "?"


def top_frame(values):
    stack = values.get("stackTrace") or {}
    frames = stack.get("frames") or []
    if not frames:
        return "(no stack)"
    frame = frames[0]
    method = frame.get("method") or {}
    type_name = (method.get("type") or {}).get("name") or "?"
    return f"{type_name}.{method.get('name', '?')}"


def main(argv):
    path = argv[0] if argv else r"E:\jfr\worldload.jfr"
    window = float(argv[1]) if len(argv) > 1 else 150.0

    print("  读取线程启动…")
    events = dump(path, "jdk.ThreadStart", "threadstart")
    anchor = None
    for event in events:
        values = event.get("values", {})
        if thread_name(values, "eventThread") == "Server thread":
            anchor = parse_time(values.get("startTime"))
            print(f"  Server thread 启动于 {values.get('startTime')}")
            break
    if anchor is None:
        print("  未找到 Server thread")
        return 1

    print("  读取执行采样…")
    events = dump(path, "jdk.ExecutionSample", "samples")
    print(f"  采样 {len(events)} 个")

    by_thread = defaultdict(Counter)
    inside = 0
    for event in events:
        values = event.get("values", {})
        stamp = parse_time(values.get("startTime"))
        if stamp is None:
            continue
        drift = stamp - anchor
        if drift < 0 or drift > window:
            continue
        inside += 1
        by_thread[thread_name(values)][top_frame(values)] += 1

    print(f"  窗口内 {inside} 个采样（锚点后 {window:.0f} 秒）")
    print()
    for thread, frames in sorted(by_thread.items(),
                                 key=lambda kv: -sum(kv[1].values()))[:6]:
        total = sum(frames.values())
        print(f"  [{thread}]  {total} 个采样")
        for name, count in frames.most_common(8):
            print(f"      {count / total * 100:5.1f}%  {name}")
        print()

    # 窗口内最长的停顿
    print("  窗口内最长停顿：")
    events = dump(path, "jdk.ExecuteVMOperation,jdk.GCPhasePause", "pauses")
    worst = []
    for event in events:
        values = event.get("values", {})
        stamp = parse_time(values.get("startTime"))
        if stamp is None or not (0 <= stamp - anchor <= window):
            continue
        duration = values.get("duration") or "0 ns"
        try:
            nanos = float(duration.split()[0])
        except (ValueError, IndexError):
            continue
        worst.append((nanos, event.get("type"), duration))
    for nanos, kind, text in sorted(worst, reverse=True)[:10]:
        print(f"    {nanos / 1e6:8.2f} ms  {kind}  {text}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
