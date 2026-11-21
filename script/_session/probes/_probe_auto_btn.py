from PIL import Image

img = Image.open(r'multiblock\gt_port_io.png').convert('RGBA')
print('size', img.size)

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

print('--- plate (44,178) 21x17 ---')
for y in range(177, 196):
    print(f'{y:3d} ' + ''.join(cls(img.getpixel((x, y))) for x in range(42, 68)))

print()
print('--- icons row y=205..225, x=38..95 ---')
print('    ' + ''.join(str(x % 10) for x in range(38, 96)))
for y in range(205, 226):
    print(f'{y:3d} ' + ''.join(cls(img.getpixel((x, y))) for x in range(38, 96)))
