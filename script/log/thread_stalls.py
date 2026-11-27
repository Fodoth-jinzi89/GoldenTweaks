"""Find the longest silences of one specific thread.

Stalls attributed by "who spoke next" mix threads together, which hides the one that actually blocked.
Filtering to a single thread first, then measuring gaps, answers the narrower question: what was this
thread doing (or waiting on) between its own two consecutive lines.

    python script/log/thread_stalls.py <latest.log> <thread-name> [min_seconds]
"""
import io
import re
import sys
from datetime import datetime

CLOCK = re.compile(r"^\[[^\]]*?(\d{1,2}):(\d{2}):(\d{2})\.(\d{3})\]")
THREAD = re.compile(r"^\[[^\]]*?\]\s+\[([^\]]+?)/(?:INFO|WARN|ERROR|DEBUG|TRACE|FATAL)\]")


def main(argv):
    if len(argv) < 2:
        print("usage: thread_stalls.py <latest.log> <thread-name> [min_seconds]")
        return 1
    path, wanted = argv[0], argv[1]
    minimum = float(argv[2]) if len(argv) > 2 else 1.0

    with io.open(path, encoding="utf-8", errors="replace") as handle:
        lines = handle.read().splitlines()

    entries = []
    for line in lines:
        thread_match = THREAD.match(line)
        if not thread_match or thread_match.group(1) != wanted:
            continue
        clock = CLOCK.match(line)
        if not clock:
            continue
        hour, minute, second, millis = (int(g) for g in clock.groups())
        entries.append((datetime(2000, 1, 1, hour, minute, second, millis * 1000), line))

    if not entries:
        print(f"  no lines from thread '{wanted}'")
        return 1

    span = (entries[-1][0] - entries[0][0]).total_seconds()
    print(f"  thread '{wanted}': {len(entries)} lines over {span:.0f}s")
    print()

    gaps = []
    for index in range(1, len(entries)):
        delta = (entries[index][0] - entries[index - 1][0]).total_seconds()
        if delta >= minimum:
            gaps.append((delta, entries[index - 1][1], entries[index][1]))

    total = sum(g[0] for g in gaps)
    print(f"  gaps >= {minimum:.0f}s: {len(gaps)}, total {total:.0f}s of {span:.0f}s")
    print()
    gaps.sort(key=lambda g: -g[0])
    for delta, before, after in gaps[:20]:
        print(f"  === {delta:.1f}s ===")
        print(f"    from: {before[60:230]}")
        print(f"    to  : {after[60:230]}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
