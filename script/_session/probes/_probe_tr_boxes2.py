from PIL import Image

img = Image.open(r'multiblock\gt_port_io.png').convert('RGBA')

# Find the exact bounds of the four big plates in the top-right block by scanning the
# light 'o' outline against the panel's '+' fill.
def is_plate_outline(px):
    r, g, b, a = px
    if a < 8:
        return False
    l = (r * 299 + g * 587 + b * 114) // 1000
    return 60 <= l < 150

# Row 28 shows '+' from 168; look for the vertical gap columns.
for y in [28, 43, 48, 64, 69]:
    runs = []
    start = None
    for x in range(160, 200):
        r, g, b, a = img.getpixel((x, y))
        lum = (r * 299 + g * 587 + b * 114) // 1000
        solid = a > 8 and lum >= 150
        if solid and start is None:
            start = x
        elif not solid and start is not None:
            runs.append((start, x - 1))
            start = None
    if start is not None:
        runs.append((start, 199))
    print(f'y={y}: solid runs {runs}')

# column profile over the top-right block
print()
print('column solid counts (x=160..200, y=28..76):')
out = []
for x in range(160, 200):
    n = 0
    for y in range(28, 77):
        r, g, b, a = img.getpixel((x, y))
        lum = (r * 299 + g * 587 + b * 114) // 1000
        if a > 8 and lum >= 150:
            n += 1
    out.append(f'{x}:{n}')
print(' '.join(out))
