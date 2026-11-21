from PIL import Image

img = Image.open(r'multiblock\gt_port_io.png').convert('RGBA')

def bbox(x0, y0, x1, y1, label):
    pts = [(x, y) for y in range(y0, y1) for x in range(x0, x1)
           if img.getpixel((x, y))[3] > 8]
    if not pts:
        print(f'{label}: empty')
        return
    ys = [p[1] for p in pts]
    print(f'{label}: content y={min(ys)}..{max(ys)}  relative-to-plate-top={min(ys)-178}')

# Two icons declared at (44,212) and (67,212), 21x17 each.
bbox(44, 205, 65, 232, 'icon A (44,212 21x17)')
bbox(67, 205, 88, 232, 'icon B (67,212 21x17)')

# What is the icon above them (the mode switch at 195)?
bbox(44, 190, 65, 206, 'mode icon A (44,195)')
bbox(65, 190, 86, 206, 'mode icon B (65,195)')
