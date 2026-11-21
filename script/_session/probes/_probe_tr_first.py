from PIL import Image

img = Image.open(r'multiblock\gt_port_io.png').convert('RGBA')

# Exact bounds of the first plate in the top-right 2x3 block.
xs, ys = [], []
for y in range(20, 50):
    for x in range(148, 180):
        r, g, b, a = img.getpixel((x, y))
        if a > 8:
            lum = (r * 299 + g * 587 + b * 114) // 1000
            if lum >= 150:
                xs.append(x); ys.append(y)
print(f'first plate solid bbox: x={min(xs)}..{max(xs)} y={min(ys)}..{max(ys)} '
      f'({max(xs)-min(xs)+1}x{max(ys)-min(ys)+1})')

# How wide is the gap, i.e. where does the second plate begin?
for y in [35]:
    runs, start = [], None
    for x in range(145, 210):
        r, g, b, a = img.getpixel((x, y))
        solid = a > 8 and ((r * 299 + g * 587 + b * 114) // 1000) >= 150
        if solid and start is None:
            start = x
        elif not solid and start is not None:
            runs.append((start, x - 1)); start = None
    if start is not None:
        runs.append((start, 209))
    print(f'row {y} solid runs: {runs}')
