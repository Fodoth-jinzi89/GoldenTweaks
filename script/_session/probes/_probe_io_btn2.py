from PIL import Image

img = Image.open(r'multiblock\gt_port_io.png').convert('RGBA')

def bbox(x0, y0, x1, y1, label):
    pts = []
    for y in range(y0, y1):
        for x in range(x0, x1):
            r, g, b, a = img.getpixel((x, y))
            if a > 8:
                pts.append((x, y, r, g, b))
    if not pts:
        print(f'{label}: empty')
        return
    xs = [p[0] for p in pts]; ys = [p[1] for p in pts]
    colours = {}
    for _x, _y, r, g, b in pts:
        key = 'yellow' if (r > 120 and g > 120 and b < 110) else (
            'red' if (r > 130 and g < 110 and b < 110) else 'other')
        colours[key] = colours.get(key, 0) + 1
    print(f'{label}: x={min(xs)}..{max(xs)} y={min(ys)}..{max(ys)} '
          f'({max(xs)-min(xs)+1}x{max(ys)-min(ys)+1}) colours={colours}')

# The spec says the first button starts at 44,195 and is 21x17; the red twin is to its right.
bbox(40, 190, 70, 220, 'first button (44,195 21x17)')
bbox(65, 190, 95, 220, 'second button')
bbox(0, 190, 40, 220, 'leftmost icons')
