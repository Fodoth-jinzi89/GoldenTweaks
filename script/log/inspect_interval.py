"""Show what ran during one gap between two known log lines.

Naming the thread is not enough when the gap is inside a single thread's own work: the thread is busy,
not blocked. This prints the timeline of one interval — markers, thread mix, and the last lines before
the next marker — so the shape of the work is visible rather than inferred.

    python script/log/inspect_interval.py <log> "<start marker>" "<end marker>"
"""
import io
import re
import sys
from collections import Counter

CLOCK = re.compile(r"^\[[^\]]*?(\d{1,2}:\d{2}:\d{2}\.\d{3})\]")
THREAD = re.compile(r"^\[[^\]]*?\]\s+\[([^\]/]+)/")


def stamp(line):
    match = CLOCK.match(line)
    return match.group(1) if match else "?"


def main(argv):
    if len(argv) < 3:
        print('usage: inspect_interval.py <log> "<start marker>" "<end marker>"')
        return 1
    path, start_marker, end_marker = argv[0], argv[1], argv[2]

    with io.open(path, encoding="utf-8", errors="replace") as handle:
        lines = handle.read().splitlines()

    start = end = -1
    for index, line in enumerate(lines):
        if start < 0 and start_marker in line:
            start = index
        elif start >= 0 and end_marker in line:
            end = index
            break

    if start < 0 or end < 0:
        print(f"  markers not found (start={start}, end={end})")
        return 1

    print(f"  start: line {start}  {stamp(lines[start])}  {lines[start][30:150]}")
    print(f"  end  : line {end}  {stamp(lines[end])}  {lines[end][30:150]}")
    print(f"  lines between: {end - start - 1}")
    print()

    counts = Counter()
    for line in lines[start + 1:end]:
        match = THREAD.match(line)
        if match:
            counts[match.group(1)] += 1
    print("  threads active in between:")
    for name, count in counts.most_common(12):
        print(f"    {count:5d}  {name}")

    print()
    print("  first 6 lines after start:")
    for line in lines[start + 1:start + 7]:
        print(f"    {stamp(line)}  {line[30:170]}")
    print("  last 6 lines before end:")
    for line in lines[max(start, end - 6):end]:
        print(f"    {stamp(line)}  {line[30:170]}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
