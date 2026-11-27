"""Find the outline colour of a casing so a corner mark can be drawn in it.

A corner mark has to match the casing's own border, so its colour cannot come from a template drawn in
arbitrary colours. This samples the darkest tone actually used by the casing's frame, weighted by
frequency, which is the border's colour rather than an anti-aliasing artefact.

    python script/multiblock/probe_outline_colour.py <frame-or-ctm.png>
"""
import sys
from collections import Counter

from PIL import Image


def luminance(pixel):
    return (pixel[0] * 299 + pixel[1] * 587 + pixel[2] * 114) // 1000


def main(argv):
    if not argv:
        print("usage: probe_outline_colour.py <image.png>")
        return 1

    for path in argv:
        image = Image.open(path).convert("RGBA")
        width, height = image.size
        pixels = image.load()

        opaque = [pixels[x, y] for y in range(height) for x in range(width)
                  if pixels[x, y][3] > 16]
        if not opaque:
            print(f"  {path}: fully transparent")
            continue

        tones = sorted(luminance(p) for p in opaque)
        # The outline is the darkest quartile; the mean of that band is its colour, ignoring the odd
        # anti-aliased pixel that is darker still.
        cutoff = tones[len(tones) // 4]
        dark = Counter((p[0], p[1], p[2]) for p in opaque if luminance(p) <= cutoff)

        print(f"  {path}")
        print(f"    {width}x{height}, opaque {len(opaque)}, luminance {tones[0]}..{tones[-1]}, "
              f"outline band <= {cutoff}")
        for colour, count in dark.most_common(5):
            print(f"    ({colour[0]:3d},{colour[1]:3d},{colour[2]:3d})  x{count}")
        total = sum(dark.values())
        weight = [0, 0, 0]
        for colour, count in dark.items():
            for channel in range(3):
                weight[channel] += colour[channel] * count
        print(f"    mean of band: ({weight[0] // total},{weight[1] // total},{weight[2] // total})")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
