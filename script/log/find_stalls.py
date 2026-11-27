"""Find the stalls in a Minecraft log by looking only at wall-clock time.

A gap between two consecutive lines is time the game spent without logging anything — which is what a
long world load looks like. Only the clock is read, never the date: the log's date field varies by
locale (`[24Nov2026 ...]` vs `[2411月2026 ...]`) and by boot (`[11:53:12]`), and mixing those two forms
in one regex is what made earlier attempts match almost nothing.

A clock that jumps backwards means a new boot, so the lines are split into runs and each is analysed
on its own — otherwise the wrap looks like a huge negative gap and hides the real ones.

    python script/log/find_stalls.py <latest.log> [min_seconds]
"""
import io
import re
import sys
from datetime import datetime

# The clock inside the leading bracket, whatever else that bracket holds.
CLOCK = re.compile(r"^\[[^\]]*?(\d{1,2}):(\d{2}):(\d{2})\.(\d{3})\]")
# A boot begins where the clock restarts; `[11:53:12] [main/INFO]` has no date at all.
BOOT = re.compile(r"^\[\d{1,2}:\d{2}:\d{2}\]\s+\[main/(?:INFO|DEBUG)\]")


def read_lines(path):
    # errors="replace": the file holds bytes from more than one encoder, and the timestamps are ASCII.
    with io.open(path, encoding="utf-8", errors="replace") as handle:
        return handle.read().splitlines()


def stamps(lines):
    """Yield (timestamp, line) for lines carrying a clock, splitting runs at each boot."""
    out = []
    previous = None
    for line in lines:
        match = CLOCK.match(line)
        if not match:
            continue
        hour, minute, second, millis = (int(g) for g in match.groups())
        stamp = datetime(2000, 1, 1, hour, minute, second, millis * 1000)
        if previous is not None and stamp < previous:
            # Clock went backwards: a new run. Mark it so the caller can split.
            out.append((None, line))
        out.append((stamp, line))
        previous = stamp
    return out


def main(argv):
    if not argv:
        print("usage: find_stalls.py <latest.log> [min_seconds]")
        return 1
    path = argv[0]
    minimum = float(argv[1]) if len(argv) > 1 else 2.0

    entries = stamps(read_lines(path))
    if not entries:
        print("  no clocked lines found")
        return 1

    # Split into runs at each backwards jump.
    runs = []
    current = []
    for stamp, line in entries:
        if stamp is None:
            if current:
                runs.append(current)
            current = []
            continue
        current.append((stamp, line))
    if current:
        runs.append(current)

    print(f"  {path}")
    print(f"  runs found: {len(runs)}, clocked lines: {sum(len(r) for r in runs)}")
    print()

    for index, run in enumerate(runs):
        if len(run) < 2:
            continue
        span = (run[-1][0] - run[0][0]).total_seconds()
        gaps = []
        for i in range(1, len(run)):
            delta = (run[i][0] - run[i - 1][0]).total_seconds()
            if delta >= minimum:
                gaps.append((delta, run[i - 1][1], run[i][1]))

        total = sum(g[0] for g in gaps)
        print(f"  --- run {index + 1}: {run[0][0]:%H:%M:%S} .. {run[-1][0]:%H:%M:%S} "
              f"({span:.0f}s, {len(run)} lines), stalls >= {minimum:.0f}s: {len(gaps)}, "
              f"total {total:.0f}s")
        gaps.sort(key=lambda g: -g[0])
        for delta, before, after in gaps[:12]:
            print(f"    {delta:7.1f}s")
            print(f"      {before[:170]}")
            print(f"      {after[:170]}")
        print()
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
