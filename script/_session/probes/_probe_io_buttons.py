from PIL import Image

img = Image.open(r'multiblock\gt_port_io.png').convert('RGBA')
print('size', img.size)

def cls(px):
    r, g, b, a = px
    if a < 8:
        return '.'
    l = (r * 299 + g * 587 + b * 114) // 1000
    # flag saturated colours separately, the buttons are yellow and red
    if r > 120 and g > 120 and b < 110:
        return 'Y'
    if r > 130 and g < 110 and b < 110:
        return 'R'
    if l < 60:
        return '#'
    if l < 150:
        return 'o'
    if l < 215:
        return '+'
    return ' '

# the button strip sits along the bottom; scan a generous band
print('--- x=0..140, y=185..230 ---')
for y in range(185, 231):
    row = ''.join(cls(img.getpixel((x, y))) for x in range(0, 141))
    print(f'{y:3d} {row}')
