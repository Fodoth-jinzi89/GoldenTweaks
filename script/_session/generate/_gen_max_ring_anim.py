"""Build the animated rainbow ring MAX uses.

A block's tint is baked into its mesh, so one number cannot animate on a placed block. The
fix is a texture instead: sixteen frames of the same ring, each in a different hue, cycled by
Minecraft's own animation support. Frame one matches the grey mask exactly, so only the
colour changes between frames.
"""
import io
import os
import struct
import zlib

SRC = r"src/main/resources/assets/goldentweaks/textures/block/multiblock/port/level"
DST = os.path.join(SRC, "max.png")
META = os.path.join(SRC, "max.png.mcmeta")

FRAMES = 16
FRAME_MS = 4 * 1000 // FRAMES  # one full cycle in four seconds, like the text


def read_png(path):
    """Minimal PNG reader: enough for the 16x16 RGBA sheet this needs."""
    with open(path, "rb") as handle:
        data = handle.read()
    assert data[:8] == b"\x89PNG\r\n\x1a\n", "not a PNG"

    pos = 8
    width = height = None
    idat = b""
    while pos < len(data):
        length = struct.unpack(">I", data[pos:pos + 4])[0]
        kind = data[pos + 4:pos + 8]
        chunk = data[pos + 8:pos + 8 + length]
        if kind == b"IHDR":
            width, height, depth, colour = struct.unpack(">IIBB", chunk[:10])
            assert depth == 8 and colour == 6, "expected 8-bit RGBA"
        elif kind == b"IDAT":
            idat += chunk
        pos += 12 + length

    raw = zlib.decompress(idat)
    stride = width * 4
    rows = []
    previous = bytearray(stride)
    index = 0
    for _ in range(height):
        filter_type = raw[index]
        index += 1
        line = bytearray(raw[index:index + stride])
        index += stride
        for i in range(stride):
            a = line[i - 4] if i >= 4 else 0
            b = previous[i]
            c = previous[i - 4] if i >= 4 else 0
            if filter_type == 1:
                line[i] = (line[i] + a) & 0xFF
            elif filter_type == 2:
                line[i] = (line[i] + b) & 0xFF
            elif filter_type == 3:
                line[i] = (line[i] + ((a + b) >> 1)) & 0xFF
            elif filter_type == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                pick = a if pa <= pb and pa <= pc else (b if pb <= pc else c)
                line[i] = (line[i] + pick) & 0xFF
        rows.append(line)
        previous = line
    return width, height, rows


def write_png(path, width, height, rows):
    raw = bytearray()
    for row in rows:
        raw.append(0)
        raw.extend(row)

    def chunk(kind, payload):
        return (struct.pack(">I", len(payload)) + kind + payload
                + struct.pack(">I", zlib.crc32(kind + payload) & 0xFFFFFFFF))

    header = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    with open(path, "wb") as handle:
        handle.write(b"\x89PNG\r\n\x1a\n")
        handle.write(chunk(b"IHDR", header))
        handle.write(chunk(b"IDAT", zlib.compress(bytes(raw), 9)))
        handle.write(chunk(b"IEND", b""))


def hsv_to_rgb(h, s, v):
    scaled = (h % 1.0) * 6.0
    sector = int(scaled)
    fraction = scaled - sector
    p = v * (1.0 - s)
    q = v * (1.0 - s * fraction)
    t = v * (1.0 - s * (1.0 - fraction))
    return [
        (v, t, p), (q, v, p), (p, v, t),
        (p, q, v), (t, p, v), (v, p, q),
    ][sector % 6]


def main():
    width, height, rows = read_png(os.path.join(SRC, "input.png"))

    frames = []
    for frame in range(FRAMES):
        hue = frame / float(FRAMES)
        r, g, b = hsv_to_rgb(hue, 1.0, 1.0)
        tint = (int(round(r * 255)), int(round(g * 255)), int(round(b * 255)))

        out = bytearray()
        for row in rows:
            for x in range(width):
                alpha = row[x * 4 + 3]
                if alpha == 0:
                    out.extend((0, 0, 0, 0))
                else:
                    # The mask is greyscale; its brightness becomes the shade of this hue.
                    shade = row[x * 4] / 255.0
                    out.extend((
                        int(round(tint[0] * shade)),
                        int(round(tint[1] * shade)),
                        int(round(tint[2] * shade)),
                        alpha,
                    ))
        frame_rows = [out[y * width * 4:(y + 1) * width * 4] for y in range(height)]
        frames.append(frame_rows)

    # One tall sheet, sixteen squares stacked: the layout Minecraft animates by default.
    stacked = []
    for frame_rows in frames:
        stacked.extend(frame_rows)
    write_png(DST, width, height * FRAMES, stacked)

    with io.open(META, "w", encoding="utf-8", newline="\n") as handle:
        handle.write('{\n')
        handle.write('  "animation": {\n')
        handle.write('    "frametime": %d,\n' % FRAME_MS)
        handle.write('    "interpolate": true\n')
        handle.write('  }\n')
        handle.write('}\n')

    print("wrote %s (%dx%d, %d frames)" % (DST, width, height * FRAMES, FRAMES))
    print("frametime %d ms" % FRAME_MS)


main()
