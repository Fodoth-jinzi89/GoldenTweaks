"""Report the sizes of the template files and how each quadrant of the outputs relates to the source.

The frame template encodes position through colour: each pixel carries the colour of the place it came
from. So the generated sheet can be explained pixel by pixel — for every output pixel, which (x, y) of
the frame it matches, and whether that mapping is a plain crop, a flip, or a rotation. That is what
proves the generation is transform-only rather than redrawn.

    python script/multiblock/analyse_template_mapping.py
"""
import os
import sys
from collections import Counter

from PIL import Image

FOLDER = os.path.join("arts", "multiblock", "ctm")


def load(name):
    return Image.open(os.path.join(FOLDER, name)).convert("RGBA")


def main():
    for name in ("template_frame.png", "template_ct.png", "template_r.png", "template_base.png"):
        path = os.path.join(FOLDER, name)
        if not os.path.exists(path):
            print(f"  {name}: missing")
            continue
        image = load(name)
        print(f"  {name}: {image.size[0]}x{image.size[1]}")

    frame = load("template_frame.png")
    width, height = frame.size
    half_w, half_h = width // 2, height // 2

    # Every colour in the frame with its unique (x, y), so an output pixel can be traced back.
    origin = {}
    duplicates = Counter()
    for y in range(height):
        for x in range(width):
            pixel = frame.getpixel((x, y))
            if pixel in origin:
                duplicates[pixel] += 1
            else:
                origin[pixel] = (x, y)
    print()
    print(f"  frame colours: {len(origin)}, repeated colours: {len(duplicates)}")

    quadrants = {
        "topLeft": (0, 0),
        "topRight": (half_w, 0),
        "bottomLeft": (0, half_h),
        "bottomRight": (half_w, half_h),
    }

    for name in ("template_ct.png", "template_r.png"):
        path = os.path.join(FOLDER, name)
        if not os.path.exists(path):
            continue
        image = load(name)
        out_w, out_h = image.size
        cell_w, cell_h = out_w // 4, out_h // 4 if out_h >= out_w else out_h
        print()
        print(f"  === {name}: {out_w}x{out_h} ===")

        # Check each output quadrant against the frame's quadrants.
        matches = Counter()
        unknown = 0
        for qy in range(2):
            for qx in range(2):
                for y in range(out_h // 2):
                    for x in range(out_w // 2):
                        pixel = image.getpixel((qx * (out_w // 2) + x, qy * (out_h // 2) + y))
                        source = origin.get(pixel)
                        if source is None:
                            unknown += 1
                            continue
                        matches[(qx, qy, source[0] // half_w, source[1] // half_h)] += 1
        print(f"    pixels traceable to the frame: {sum(matches.values())}, unknown: {unknown}")
        for key in sorted(matches):
            qx, qy, sx, sy = key
            print(f"    out quadrant ({qx},{qy}) <- frame quadrant ({sx},{sy}): {matches[key]} px")
    return 0


if __name__ == "__main__":
    sys.exit(main())
