#!/usr/bin/env python3
"""Mirror our multiblock structure files into `assets/` for the client.

`data/` is the server-side home of the layouts: AMI's pattern reload reads it, and so does the
server-side builder. The client cannot use that copy, though — its resource manager is a
CLIENT_RESOURCES one, which only exposes `assets/`. Without a second copy there, the projection
has no layout to draw and silently shows nothing.

Run from the project root after editing any structure file:

    python script/sync_multiblock_structures.py
"""

import filecmp
import os
import shutil

ROOT = os.path.join("src", "main", "resources")
SOURCE = os.path.join(ROOT, "data", "goldentweaks", "multiblock", "structures")
TARGET = os.path.join(ROOT, "assets", "goldentweaks", "multiblock", "structures")


def main():
    if not os.path.isdir(SOURCE):
        raise SystemExit("missing structure directory: %s" % SOURCE)

    os.makedirs(TARGET, exist_ok=True)

    copied = 0
    for name in sorted(os.listdir(SOURCE)):
        if not name.endswith(".json"):
            continue
        src = os.path.join(SOURCE, name)
        dst = os.path.join(TARGET, name)
        if os.path.exists(dst) and filecmp.cmp(src, dst, shallow=False):
            print("unchanged %s" % name)
            continue
        shutil.copyfile(src, dst)
        print("copied    %s" % name)
        copied += 1

    # Remove client copies whose server original is gone, so a deleted structure does not linger
    # in projections.
    for name in sorted(os.listdir(TARGET)):
        if not name.endswith(".json"):
            continue
        if not os.path.exists(os.path.join(SOURCE, name)):
            os.remove(os.path.join(TARGET, name))
            print("removed   %s (no longer defined in data/)" % name)

    print("done: %d file(s) updated" % copied)


if __name__ == "__main__":
    main()
