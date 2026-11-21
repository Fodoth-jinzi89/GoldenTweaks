from PIL import Image

img = Image.open(r'multiblock\cc_1.png').convert('RGBA')

def lum(px):
    r, g, b, a = px
    return None if a < 8 else (r * 299 + g * 587 + b * 114) // 1000

def find_dark_boxes(x0, y0, x1, y1, label):
    """Locate the darkest inner block: the drawn slot."""
    pts = []
    for y in range(y0, y1):
        for x in range(x0, x1):
            l = lum(img.getpixel((x, y)))
            if l is not None and l < 60:
                pts.append((x, y))
    if not pts:
        print(f'{label}: no dark pixels')
        return
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    print(f'{label}: dark bbox x={min(xs)}..{max(xs)} y={min(ys)}..{max(ys)} '
          f'({max(xs)-min(xs)+1}x{max(ys)-min(ys)+1}) rel=({min(xs)-x0},{min(ys)-y0})')

    # the drawn frame is the run of '+' just outside; report its bbox by scanning outward
    print(f'  -> slot inner size {max(xs)-min(xs)+1} x {max(ys)-min(ys)+1}')

print('--- collapsed (0,0,22,26) ---')
find_dark_boxes(0, 0, 22, 26, 'collapsed')
print('--- expanded (0,26,62,67) ---')
find_dark_boxes(0, 26, 62, 67, 'expanded')
