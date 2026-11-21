from PIL import Image

img = Image.open(r'multiblock\gt_port_io.png').convert('RGBA')
W, H = img.size

def lum(px):
    r, g, b, a = px
    return None if a < 8 else (r * 299 + g * 587 + b * 114) // 1000

# Map the whole sheet coarsely to find the plate blocks.
print('size', img.size)
print('    ' + ''.join(str((x // 10) % 10) for x in range(150, 210)))
print('    ' + ''.join(str(x % 10) for x in range(150, 210)))
for y in range(0, 140):
    row = ''
    for x in range(150, 210):
        l = lum(img.getpixel((x, y)))
        if l is None:
            row += '.'
        elif l < 60:
            row += '#'
        elif l < 150:
            row += 'o'
        elif l < 215:
            row += '+'
        else:
            row += ' '
    print(f'{y:3d} {row}')
