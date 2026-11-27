"""Attribute log stalls to the thread that was silent, not the one that spoke next.

The line before a gap and the line after it usually belong to different threads, so naming the pair is
misleading: the useful question is which thread stopped producing output. For each gap this lists the
last line from each active thread before it, so the one that had work in flight is identifiable.

    python script/log/attribute_stalls.py <latest.log> [min_seconds]
"""
import io
import re
import sys
from collections import defaultdict
from datetime import datetime

CLOCK = re.compile(r"^\[[^\]]*?(\d{1,2}):(\d{2}):(\d{2})\.(\d{3})\]")
THREAD = re.compile(r"^\[[^\]]*?\]\s+\[([^\]]+?)/(?:INFO|WARN|ERROR|DEBUG|TRACE|FATAL)\]")


def main(argv):
    if not argv:
        print("usage: attribute_stalls.py <latest.log> [min_seconds]")
        return 1
    path = argv[0]
    minimum = float(argv[1]) if len(argv) > 1 else 3.0

    with io.open(path, encoding="utf-8", errors="replace") as handle:
        lines = handle.read().splitlines()

    stamped = []
    for line in lines:
        match = CLOCK.match(line)
        if not match:
            continue
        hour, minute, second, millis = (int(g) for g in match.groups())
        stamped.append((datetime(2000, 1, 1, hour, minute, second, millis * 1000), line))

    if not stamped:
        print("  no clocked lines")
        return 1

    # Which thread last logged what, as of each moment.
    last_by_thread = {}
    stalls = []
    for index in range(1, len(stamped)):
        delta = (stamped[index][0] - stamped[index - 1][0]).total_seconds()
        before_stamp, before_line = stamped[index - 1]

        thread_match = THREAD.match(before_line)
        if thread_match:
            last_by_thread[thread_match.group(1)] = (before_stamp, before_line)

        if delta >= minimum:
            # Threads that had logged before and went quiet through the gap.
            quiet = []
            for name, (when, line) in last_by_thread.items():
                silence = (stamped[index][0] - when).total_seconds()
                if silence >= minimum * 0.9:
                    quiet.append((silence, name, line))
            quiet.sort(key=lambda q: -q[0])
            stalls.append((delta, before_line, stamped[index][1], quiet))

    stalls.sort(key=lambda s: -s[0])
    print(f"  {path}: {len(stalls)} stalls >= {minimum:.0f}s")
    print()
    for delta, before, after, quiet in stalls[:14]:
        print(f"  === {delta:.1f}s ===")
        print(f"    next line: {after[:150]}")
        print(f"    threads silent through it:")
        for silence, name, line in quiet[:6]:
            print(f"      {name:28s} silent {silence:6.1f}s  last: {line[60:190]}")
        print()
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
