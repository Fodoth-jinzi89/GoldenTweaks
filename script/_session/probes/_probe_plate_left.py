from PIL import Image

img = Image.open(r'multiblock\gt_port_io.png').convert('RGBA')

# Find the left edge of the first plate in the top-right 2x3 block precisely.
# The plate is the light '+' fill bounded by 'o' outlines; scan for the first column
# inside the panel that starts a continuous light run at plate-row heights.
for y in [30, 35, 40]:
    prev = None
    edges = []
    for x in range(140, 205):
        r, g, b, a = img.getpixel((x, y))
        lum = None if a < 8 else (r * 299 + g * 587 + b * 114) // 1000
        band = 'empty' if lum is None else ('fill' if lum >= 150 else 'line')
        if band != prev:
            edges.append((x, band))
            prev = band
    print(f'y={y}: {edges}')
