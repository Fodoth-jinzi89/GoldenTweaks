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

# (44,178) 21x17 backed plate: show it, and the region right of it.
print('    ' + ''.join(str(x % 10) for x in range(40, 92)))
for y in range(176, 197):
    row = ''.join(cls(img.getpixel((x, y))) for x in range(40, 92))
    print(f'{y:3d} {row}')

print()
print('non-transparent bbox of the plate area (44..64, 178..194):')
pts = [(x, y) for y in range(178, 195) for x in range(44, 65)
       if img.getpixel((x, y))[3] > 8]
xs = [p[0] for p in pts]; ys = [p[1] for p in pts]
print(f'x={min(xs)}..{max(xs)} y={min(ys)}..{max(ys)}  ({max(xs)-min(xs)+1}x{max(ys)-min(ys)+1})')
