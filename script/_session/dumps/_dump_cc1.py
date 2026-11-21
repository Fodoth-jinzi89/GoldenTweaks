import subprocess, os, sys
sys.path.insert(0, os.path.dirname(__file__))

try:
    from PIL import Image
except ImportError:
    print('NO_PIL')
    raise SystemExit

p = r'multiblock\cc_1.png'
img = Image.open(p).convert('RGBA')
print('size', img.size)

def classify(px):
    r, g, b, a = px
    if a < 8:
        return '.'
    lum = (r * 299 + g * 587 + b * 114) // 1000
    if lum < 60:
        return '#'
    if lum > 215:
        return ' '
    if lum > 140:
        return '+'
    return 'o'

for name, (x0, y0, x1, y1) in [
    ('COLLAPSED', (0, 0, 22, 26)),
    ('EXPANDED', (0, 26, 62, 67)),
]:
    print(f'--- {name} {x1-x0}x{y1-y0} ---')
    for y in range(y0, y1):
        row = ''.join(classify(img.getpixel((x, y))) for x in range(x0, x1))
        print(f'{y:3d} {row}')
