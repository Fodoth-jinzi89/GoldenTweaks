from PIL import Image

img = Image.open(r'multiblock\cc_1.png').convert('RGBA')

def cls(px):
    r, g, b, a = px
    if a < 8:
        return '.'
    l = (r * 299 + g * 587 + b * 114) // 1000
    if l < 60:
        return '#'   # darkest: the drawn slot interior
    if l < 150:
        return 'o'   # mid grey
    if l < 215:
        return '+'
    return ' '

for label, y0, y1 in [('COLLAPSED', 0, 26), ('EXPANDED', 26, 67)]:
    print(f'--- {label} ---')
    for y in range(y0, y1):
        row = ''.join(cls(img.getpixel((x, y))) for x in range(0, 62))
        print(f'{y:3d} {row}')
    print()
