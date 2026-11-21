#!/usr/bin/env python3
"""Generate OptiFine-standard CTM tiles (0.png .. 46.png) for Continuity / NeoContinuity.

Input is the 2x2 quadrant template used by ConnectedTexturesGenerator:

    +--------+--------+
    | corner | over-  |
    |  (cr)  | flow(of)|
    +--------+--------+
    | edge   | null   |
    |  (ed)  |  (nu)  |
    +--------+--------+

  cr : how a single corner with two joined edges looks
  ed : how a straight joined edge looks
  of : pixels used to fill the corner/edge seams
  nu : a block with no visible edges

Each quadrant is half the input size. The 47 output tiles are assembled from
these four quadrants with the rotations/flips listed in ALIGNMENTS, which is a
verbatim copy of alignments.json from
https://github.com/JustAlittleWolf/ConnectedTexturesGenerator

Usage:
    python script/ctm_generate.py <template.png> <output_dir> [--properties FILE]
                                  [--tile-prefix NS:textures/block/... ] [--match-blocks NS:block]
"""

import argparse
import json
import os
import sys

from PIL import Image

ALIGNMENTS = json.loads(r"""
{
 "0":  {"tl":"cr","tr":"cr","bl":"cr","br":"cr"},
 "1":  {"tl":"cr","tr":"edh","bl":"cr","br":"edh"},
 "2":  {"tl":"edh","tr":"edh","bl":"edh","br":"edh"},
 "3":  {"tl":"edh","tr":"cr","bl":"edh","br":"cr"},
 "4":  {"tl":"cr","tr":"edh","bl":"edv","br":"of"},
 "5":  {"tl":"edh","tr":"cr","bl":"of","br":"edv"},
 "6":  {"tl":"edv","tr":"of","bl":"edv","br":"of"},
 "7":  {"tl":"edh","tr":"edh","bl":"of","br":"of"},
 "8":  {"tl":"of","tr":"nu","bl":"of","br":"of"},
 "9":  {"tl":"of","tr":"of","bl":"of","br":"nu"},
 "10": {"tl":"nu","tr":"of","bl":"nu","br":"of"},
 "11": {"tl":"nu","tr":"nu","bl":"of","br":"of"},
 "12": {"tl":"cr","tr":"cr","bl":"edv","br":"edv"},
 "13": {"tl":"cr","tr":"edh","bl":"edv","br":"nu"},
 "14": {"tl":"edh","tr":"edh","bl":"nu","br":"nu"},
 "15": {"tl":"edh","tr":"cr","bl":"nu","br":"edv"},
 "16": {"tl":"edv","tr":"of","bl":"cr","br":"edh"},
 "17": {"tl":"of","tr":"edv","bl":"edh","br":"cr"},
 "18": {"tl":"of","tr":"of","bl":"edh","br":"edh"},
 "19": {"tl":"of","tr":"edv","bl":"of","br":"edv"},
 "20": {"tl":"nu","tr":"of","bl":"of","br":"of"},
 "21": {"tl":"of","tr":"of","bl":"nu","br":"of"},
 "22": {"tl":"of","tr":"of","bl":"nu","br":"nu"},
 "23": {"tl":"of","tr":"nu","bl":"of","br":"nu"},
 "24": {"tl":"edv","tr":"edv","bl":"edv","br":"edv"},
 "25": {"tl":"edv","tr":"nu","bl":"edv","br":"nu"},
 "26": {"tl":"nu","tr":"nu","bl":"nu","br":"nu"},
 "27": {"tl":"nu","tr":"edv","bl":"nu","br":"edv"},
 "28": {"tl":"edv","tr":"of","bl":"edv","br":"nu"},
 "29": {"tl":"edh","tr":"edh","bl":"nu","br":"of"},
 "30": {"tl":"edv","tr":"nu","bl":"edv","br":"of"},
 "31": {"tl":"edh","tr":"edh","bl":"of","br":"nu"},
 "32": {"tl":"nu","tr":"nu","bl":"nu","br":"of"},
 "33": {"tl":"nu","tr":"nu","bl":"of","br":"nu"},
 "34": {"tl":"of","tr":"nu","bl":"nu","br":"of"},
 "35": {"tl":"nu","tr":"of","bl":"of","br":"nu"},
 "36": {"tl":"edv","tr":"edv","bl":"cr","br":"cr"},
 "37": {"tl":"edv","tr":"nu","bl":"cr","br":"edh"},
 "38": {"tl":"nu","tr":"nu","bl":"edh","br":"edh"},
 "39": {"tl":"nu","tr":"edv","bl":"edh","br":"cr"},
 "40": {"tl":"of","tr":"nu","bl":"edh","br":"edh"},
 "41": {"tl":"nu","tr":"edv","bl":"of","br":"edv"},
 "42": {"tl":"nu","tr":"of","bl":"edh","br":"edh"},
 "43": {"tl":"of","tr":"edv","bl":"nu","br":"edv"},
 "44": {"tl":"nu","tr":"of","bl":"nu","br":"nu"},
 "45": {"tl":"of","tr":"nu","bl":"nu","br":"nu"},
 "46": {"tl":"of","tr":"of","bl":"of","br":"of"}
}
""")

TILE_COUNT = 47


def sample(kind, quadrant, u, v, n):
    """Return the source pixel of `quadrant` for destination (u, v) in a corner cell.

    Mirrors the per-quadrant rotations used by ConnectedTexturesGenerator's
    generator.py so the output matches that tool exactly.
    """
    x, y = u, v
    if kind == "cr":
        coords = {
            "tl": (x, y),
            "tr": (n - 1 - x, y),
            "bl": (x, n - 1 - y),
            "br": (n - 1 - x, n - 1 - y),
        }[quadrant]
    elif kind == "edh":
        coords = {
            "tl": (y, n - 1 - x),
            "tr": (y, x),
            "bl": (n - 1 - y, n - 1 - x),
            "br": (n - 1 - y, x),
        }[quadrant]
    elif kind == "edv":
        coords = {
            "tl": (x, n - 1 - y),
            "tr": (n - 1 - x, n - 1 - y),
            "bl": (x, y),
            "br": (n - 1 - x, y),
        }[quadrant]
    elif kind == "of":
        coords = {
            "tl": (n - 1 - x, y),
            "tr": (x, y),
            "bl": (n - 1 - x, n - 1 - y),
            "br": (x, n - 1 - y),
        }[quadrant]
    elif kind == "nu":
        coords = {
            "tl": (n - 1 - x, n - 1 - y),
            "tr": (x, n - 1 - y),
            "bl": (n - 1 - x, y),
            "br": (x, y),
        }[quadrant]
    else:
        raise ValueError("unknown quadrant kind: %s" % kind)
    return coords


def build_quadrants(source):
    width, height = source.size
    n = height // 2
    px = source.load()

    cr = [[px[x, y] for x in range(n)] for y in range(n)]
    ed = [[px[x, y + n] for x in range(n)] for y in range(n)]
    of = [[px[x + n, y] for x in range(n)] for y in range(n)]
    nu = [[px[x + n, y + n] for x in range(n)] for y in range(n)]
    return {"cr": cr, "edh": ed, "edv": ed, "of": of, "nu": nu}, n, width, height


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("template")
    parser.add_argument("output")
    parser.add_argument("--properties")
    parser.add_argument("--tile-prefix")
    parser.add_argument("--match-blocks")
    parser.add_argument("--match-tiles")
    parser.add_argument("--connect", default="block", choices=("block", "tile", "state"))
    args = parser.parse_args()

    source = Image.open(args.template).convert("RGBA")
    width, height = source.size
    if height % 2 != 0 or width % 2 != 0:
        sys.exit("template must have even dimensions, got %dx%d" % (width, height))

    quadrants, n, width, height = build_quadrants(source)
    os.makedirs(args.output, exist_ok=True)

    for index in range(TILE_COUNT):
        align = ALIGNMENTS[str(index)]
        tile = Image.new("RGBA", (height, height), (0, 0, 0, 0))
        dest = tile.load()

        for quadrant in ("tl", "tr", "bl", "br"):
            kind = align[quadrant]
            data = quadrants[kind]
            ox = 0 if quadrant in ("tl", "bl") else n
            oy = 0 if quadrant in ("tl", "tr") else n
            for v in range(n):
                for u in range(n):
                    sx, sy = sample(kind, quadrant, u, v, n)
                    dest[ox + u, oy + v] = data[sy][sx]

        # 32x16 style templates reserve a second half for seam fill-up.
        if width == 2 * height:
            px = source.load()
            for y in range(height):
                for x in range(height):
                    if dest[x, y][3] == 0:
                        dest[x, y] = px[height + x, y]

        tile.save(os.path.join(args.output, "%d.png" % index))

    print("wrote %d tiles to %s" % (TILE_COUNT, args.output))

    if args.properties:
        if not args.tile_prefix:
            sys.exit("--tile-prefix is required with --properties")
        if not args.match_blocks and not args.match_tiles:
            sys.exit("--match-blocks or --match-tiles is required with --properties")
        tiles = ",".join("%s/%d.png" % (args.tile_prefix, i) for i in range(TILE_COUNT))
        lines = ["method=ctm"]
        if args.match_blocks:
            lines.append("matchBlocks=%s" % args.match_blocks)
        if args.match_tiles:
            lines.append("matchTiles=%s" % " ".join(args.match_tiles.split(",")))
        lines.extend([
            "connect=%s" % args.connect,
            "faces=all",
            "innerSeams=false",
            "tiles=%s" % tiles,
        ])
        os.makedirs(os.path.dirname(os.path.abspath(args.properties)), exist_ok=True)
        with open(args.properties, "w", encoding="ascii", newline="\n") as handle:
            handle.write("\n".join(lines) + "\n")
        print("wrote properties to %s" % args.properties)


if __name__ == "__main__":
    main()
