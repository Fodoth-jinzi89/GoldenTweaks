"""Test whether a sheet's quadrants are transforms of one another.

The whole feasibility of generating a CT sheet from one template rests on this: if every quadrant is
some rotation or mirror of the first, the sheet can be produced by crop/flip/rotate alone. If not,
something would have to be redrawn, and no amount of code would make it a pure transform.

Compares quadrant A against every transform of quadrant A and reports which transform lands on B, C
and D, plus the exact pixel differences so a near-miss is distinguishable from a hit.

    python script/multiblock/test_quadrant_symmetry.py <image.png>
"""
import sys

from PIL import Image

# Name -> the transform applied to the top-left quadrant.
TRANSFORMS = {
    "identity": lambda im: im,
    "rot90": lambda im: im.transpose(Image.Transpose.ROTATE_90),
    "rot180": lambda im: im.transpose(Image.Transpose.ROTATE_180),
    "rot270": lambda im: im.transpose(Image.Transpose.ROTATE_270),
    "mirror_x": lambda im: im.transpose(Image.Transpose.FLIP_LEFT_RIGHT),
    "mirror_y": lambda im: im.transpose(Image.Transpose.FLIP_TOP_BOTTOM),
    "transpose": lambda im: im.transpose(Image.Transpose.TRANSPOSE),
    "transverse": lambda im: im.transpose(Image.Transpose.TRANSVERSE),
}


def pixel_list(image):
    return list(image.convert("RGBA").getdata())


def diff(a, b):
    return sum(1 for pa, pb in zip(a, b) if pa != pb)


def main(argv):
    if not argv:
        print("usage: test_quadrant_symmetry.py <image.png>")
        return 1

    image = Image.open(argv[0]).convert("RGBA")
    width, height = image.size
    if width % 2 or height % 2:
        print(f"  {argv[0]}: {width}x{height} has no even split")
        return 1
    half_w, half_h = width // 2, height // 2

    quadrants = {
        "topLeft": image.crop((0, 0, half_w, half_h)),
        "topRight": image.crop((half_w, 0, width, half_h)),
        "bottomLeft": image.crop((0, half_h, half_w, height)),
        "bottomRight": image.crop((half_w, half_h, width, height)),
    }

    print(f"  {argv[0]}: {width}x{height}, quadrants {half_w}x{half_h}")
    reference = quadrants["topLeft"]
    reference_pixels = pixel_list(reference)

    # Exact equality between quadrants.
    print()
    print("  identical quadrants:")
    names = list(quadrants)
    for i in range(len(names)):
        for j in range(i + 1, len(names)):
            same = pixel_list(quadrants[names[i]]) == pixel_list(quadrants[names[j]])
            print(f"    {names[i]:12s} == {names[j]:12s}: {same}")

    # Which transform of topLeft reproduces each other quadrant.
    print()
    print("  best transform of topLeft for each quadrant:")
    for name in ("topRight", "bottomLeft", "bottomRight"):
        target = pixel_list(quadrants[name])
        results = []
        for label, transform in TRANSFORMS.items():
            candidate = transform(reference)
            results.append((diff(pixel_list(candidate), target), label))
        results.sort()
        best = results[0]
        exact = [label for count, label in results if count == 0]
        print(f"    {name:12s}: closest {best[1]} ({best[0]} differing pixels)"
              + (f", exact: {exact}" if exact else "  -- no exact transform"))

    # What the corner pixel of each quadrant is, to see how the quadrants are oriented.
    print()
    print("  corner pixels per quadrant (defines orientation):")
    for name, quad in quadrants.items():
        w, h = quad.size
        tl = quad.getpixel((0, 0))
        tr = quad.getpixel((w - 1, 0))
        bl = quad.getpixel((0, h - 1))
        br = quad.getpixel((w - 1, h - 1))
        print(f"    {name:12s} tl={tl} tr={tr}")
        print(f"    {'':12s} bl={bl} br={br}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
