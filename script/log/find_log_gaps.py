"""Report the largest time gaps between consecutive log lines.

A slow startup shows up as silence: the server thread blocks and nothing is logged until whatever it
is waiting on finishes. Sorting by gap rather than reading the tail finds those stalls wherever they
are, including early ones that scroll past long before the end of the file.

    python script/log/find_log_gaps.py <latest.log> [min_seconds]
"""
import io
import re
import sys
from datetime import datetime

# The stamp is `[24Nov2026 11:55:54.831]` or, when the system locale is Chinese, `[2411月2026 ...]`.
# The day and month run together in the second form, so the pattern matches "digits, then anything
# that is not a digit, then the four-digit year" and takes the leading digits as the day. Only the
# clock and year are needed to measure a gap; the date itself is irrelevant within one run.
STAMP = re.compile(r"\[(\d{1,2})\D*(\d{4})\s+(\d{1,2}):(\d{2}):(\d{2})\.(\d{3})\]")


def parse(line):
    """Return (datetime, rest) for a dated stamp, else (None, None)."""
    match = STAMP.search(line)
    if not match:
        return None, None
    day, year, hour, minute, second, millis = match.groups()
    # The month is unknown and irrelevant to measuring gaps within a single run.
    return (datetime(int(year), 1, int(day), int(hour), int(minute),
                     int(second), int(millis) * 1000),
            line[match.end():])


def main(argv):
    if not argv:
        print("usage: find_log_gaps.py <latest.log> [min_seconds]")
        return 1
    path = argv[0]
    minimum = float(argv[1]) if len(argv) > 1 else 1.0

    entries = []
    # `errors="replace"` because the file mixes encodings: a line written by the console encoder may
    # not decode as UTF-8. Timestamps are pure ASCII, so they survive either way.
    with io.open(path, encoding="utf-8", errors="replace") as handle:
        for line in handle:
            stamp, rest = parse(line)
            if stamp:
                entries.append((stamp, rest.strip()))

    if not entries:
        print("  no timestamped lines found")
        return 1

    first, last = entries[0][0], entries[-1][0]
    print(f"  {path}")
    print(f"  lines with a timestamp: {len(entries)}")
    print(f"  span: {first} .. {last}  ({(last - first).total_seconds():.1f}s)")
    print()

    gaps = []
    for index in range(1, len(entries)):
        delta = (entries[index][0] - entries[index - 1][0]).total_seconds()
        if delta >= minimum:
            gaps.append((delta, entries[index - 1], entries[index]))

    gaps.sort(key=lambda g: -g[0])
    print(f"  gaps of {minimum:.1f}s or more: {len(gaps)}")
    print(f"  total time in those gaps: {sum(g[0] for g in gaps):.1f}s")
    print()
    for delta, before, after in gaps[:25]:
        print(f"  {delta:7.2f}s")
        print(f"      before: {before[1][:150]}")
        print(f"      after : {after[1][:150]}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
