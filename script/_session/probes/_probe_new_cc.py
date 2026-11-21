from PIL import Image

img = Image.open(r'multiblock\cc_1.png').convert('RGBA')

def cls(px):
    r, g, b, a = px
    if a < 8:
        return '.'
    l = (r * 299 + g * 587 + b * 114) // 1000
    if l < 60:
        return '#'
    if l < 150:
        return 'o'
    if l < 215:
        return '+'
    return ' '

# exact column indices for a few key rows
for y in [0, 2, 3, 4, 22, 23, 24, 26, 28, 29, 30, 49, 50, 51, 60, 63, 64, 65]:
    row = ''.join(cls(img.getpixel((x, y))) for x in range(0, 63))
    print(f'{y:3d} {row}')

print()
# find the interior of each frame: report runs of border colour
def runs(y, x0=0, x1=63):
    out = []
    cur = None
    for x in range(x0, x1):
        c = cls(img.getpixel((x, y)))
        if c != cur:
            out.append((x, c))
            cur = c
    return out

for y in [3, 4, 29, 30, 63]:
    print(f'y={y}: {runs(y)}')
