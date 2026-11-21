from PIL import Image

img = Image.open(r'src/main/resources/assets/goldentweaks/textures/gui/cc_1.png').convert('RGBA')

def lum(px):
    r, g, b, a = px
    return None if a < 8 else (r * 299 + g * 587 + b * 114) // 1000

# The expanded art starts at v=26. Find the darkest horizontal band (the input field).
# Search only inside the expanded panel interior, below the icon row.
print('=== dark runs per row (expanded area y=26..66) ===')
for y in range(26, 67):
    runs = []
    start = None
    for x in range(0, 62):
        l = lum(img.getpixel((x, y)))
        dark = l is not None and l < 60
        if dark and start is None:
            start = x
        elif not dark and start is not None:
            runs.append((start, x - 1))
            start = None
    if start is not None:
        runs.append((start, 61))
    if runs:
        print(f'y={y}: {runs}')

print()
print('=== exact bbox of pixels darker than 60, y>=40 ===')
pts = [(x, y) for y in range(40, 67) for x in range(0, 62) if (lum(img.getpixel((x, y))) or 999) < 60]
if pts:
    xs = [p[0] for p in pts]; ys = [p[1] for p in pts]
    print(f'x={min(xs)}..{max(xs)}  y={min(ys)}..{max(ys)}  '
          f'size={max(xs)-min(xs)+1}x{max(ys)-min(ys)+1}  '
          f'rel-to-expanded={(min(xs), min(ys)-26)}')
