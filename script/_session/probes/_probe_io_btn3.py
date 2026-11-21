from PIL import Image

img = Image.open(r'multiblock\gt_port_io.png').convert('RGBA')

def cls(px):
    r, g, b, a = px
    if a < 8:
        return '.'
    if r > 120 and g > 120 and b < 110:
        return 'Y'
    if r > 130 and g < 110 and b < 110:
        return 'R'
    l = (r * 299 + g * 587 + b * 114) // 1000
    if l < 60:
        return '#'
    if l < 150:
        return 'o'
    if l < 215:
        return '+'
    return ' '

print('    ' + ''.join(str(x % 10) for x in range(40, 100)))
for y in range(185, 215):
    row = ''.join(cls(img.getpixel((x, y))) for x in range(40, 100))
    print(f'{y:3d} {row}')
