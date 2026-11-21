from PIL import Image

img = Image.open(r'src/main/resources/assets/goldentweaks/textures/gui/gt_multiblock_machine.png').convert('RGBA')
print('machine texture', img.size)

def classify(px):
    r, g, b, a = px
    if a < 8:
        return '.'
    lum = (r * 299 + g * 587 + b * 114) // 1000
    if lum < 70:
        return '#'   # dark outline / box
    if lum > 200:
        return ' '   # panel face
    if lum > 150:
        return '-'
    return 'o'

# top-right quadrant, coarse: sample every pixel in x 150..210, y 0..45
print('--- top-right x=150..210, y=0..44 ---')
for y in range(0, 45):
    row = ''.join(classify(img.getpixel((x, y))) for x in range(150, img.size[0]))
    print(f'{y:3d} {row}')
