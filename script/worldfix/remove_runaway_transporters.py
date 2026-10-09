#!/usr/bin/env python3
"""
Remove the runaway Mekanism logistical-transporter cluster and the "mega"
Sophisticated Storage barrels from an Anvil region file.

Root cause being fixed:
  Mekanism logistical transporters looping in TransporterStack.recalculatePath
  -> TransporterManager.simulateInsert into a Sophisticated Storage barrel whose
  "stack upgrade omega" allows single-slot stacks in the tens of millions.
  That loop allocates ItemStacks forever and OOMs the server.

Usage:
  python remove_be.py                 # dry run (prints what would change)
  python remove_be.py --apply         # writes the file (makes a .bak first)

IMPORTANT: stop the Minecraft server before using --apply.
"""
import os, io, sys, shutil, struct, zlib, gzip, datetime
import nbtlib

# ----------------------------------------------------------------------------
# CONFIG - targets in this region file (block coordinates)
# ----------------------------------------------------------------------------
REGION = r"E:\MCreator\Workspaces\GoldenTweaks\yuanli-sever-0.6.3-1\world\region\r.-1.0.mca"

# "mega" barrels (block entities deleted + block set to air).
# (96M / 54M / 32M / 2.8M items each; single slots up to 16 million)
BARRELS = [
    (-343, 66, 223),   # 54,138,770 items
    (-397, 65, 236),   # 101,391,213 items
    (-350, 64, 227),   # 32,424,893 items
    (-355, 64, 223),   # 2,826,565 items
]

# the stuck logistical-transporter column at x=-343, z=223 (y 66..74)
# plus the jammed row at z=227, y=65 (fed the now-deleted (-350,64,227) barrel)
TRANSPORTER_COLUMN = (
    [(-343, y, 223) for y in range(66, 75)]
    + [(x, 65, 227) for x in range(-361, -338)]
    + [(-397, 65, z) for z in range(228, 236)]
)

# also delete ANY logistical transporter that still carries cargo (clears the jam)
CLEAR_STUCK_TRANSPORTERS = False

# set the block itself to air too (True) or only delete the block entity (False)
REPLACE_WITH_AIR = True

# ----------------------------------------------------------------------------

BARREL_IDS = ("sophisticatedstorage:barrel",)
TRANSPORT_KEY = "logistical_transporter"


def decompress(data, comp):
    if comp == 1:
        return gzip.decompress(data)
    if comp == 2:
        return zlib.decompress(data)
    if comp == 3:
        return data
    if comp == 4:
        import lz4.block
        return lz4.block.decompress(data[4:], uncompressed_size=struct.unpack("<I", data[:4])[0])
    raise ValueError("unknown compression %r" % comp)


def read_chunks(path):
    chunks = {}
    with open(path, "rb") as f:
        header = f.read(4096)
        ts = f.read(4096)
        if len(header) < 4096 or len(ts) < 4096:
            raise ValueError("not a region file")
        for i in range(1024):
            entry = struct.unpack_from(">I", header, i * 4)[0]
            so = entry >> 8
            if so == 0:
                continue
            f.seek(so * 4096)
            ln = struct.unpack(">I", f.read(4))[0]
            comp = f.read(1)[0]
            payload = f.read(ln - 1)
            timestamp = struct.unpack_from(">I", ts, i * 4)[0]
            chunks[i] = (timestamp, comp, payload)
    return chunks


def write_region(path, chunks):
    header = bytearray(4096)
    ts = bytearray(4096)
    body = bytearray()
    sector = 2
    for i in sorted(chunks):
        timestamp, comp, payload = chunks[i]
        data = bytes([comp]) + payload
        length = len(data)
        need = (length + 4 + 4095) // 4096
        if need > 255:
            raise ValueError("chunk %d too large" % i)
        header[i * 4:i * 4 + 4] = struct.pack(">I", (sector << 8) | need)
        ts[i * 4:i * 4 + 4] = struct.pack(">I", timestamp)
        body += struct.pack(">I", length) + data
        body += b"\x00" * (need * 4096 - (4 + length))
        sector += need
    with open(path, "wb") as f:
        f.write(header)
        f.write(ts)
        f.write(body)


def set_block_air(tag, x, y, z):
    sy = y >> 4
    lx, ly, lz = x & 15, y & 15, z & 15
    index = (ly << 8) | (lz << 4) | lx
    for s in tag.get("sections", []):
        if int(s.get("Y", 999)) != sy:
            continue
        bs = s.get("block_states")
        if bs is None:
            return False
        palette = bs.get("palette")
        if palette is None:
            return False
        n = len(palette)
        bits = max(4, (n - 1).bit_length())
        epl = 64 // bits
        data = bs.get("data")
        vals = [0] * 4096
        if data is not None:
            for k in range(4096):
                vals[k] = (int(data[k // epl]) >> ((k % epl) * bits)) & ((1 << bits) - 1)
        air = None
        for k, p in enumerate(palette):
            if str(p.get("Name")) == "minecraft:air":
                air = k
                break
        if air is None:
            palette.append(nbtlib.Compound({"Name": nbtlib.String("minecraft:air")}))
            air = len(palette) - 1
        vals[index] = air
        new_n = len(palette)
        new_bits = max(4, (new_n - 1).bit_length())
        new_epl = 64 // new_bits
        total = (4096 + new_epl - 1) // new_epl
        out = [0] * total
        for k in range(4096):
            v = vals[k]
            if v:
                out[k // new_epl] |= (v << ((k % new_epl) * new_bits))
        signed = [v - (1 << 64) if v >= (1 << 63) else v for v in out]
        bs["data"] = nbtlib.LongArray(signed)
        return True
    return False


def main():
    apply = "--apply" in sys.argv
    path = REGION
    if "--region" in sys.argv:
        path = sys.argv[sys.argv.index("--region") + 1]

    barrel_set = set(BARRELS)
    column_set = set(TRANSPORTER_COLUMN)
    all_targets = barrel_set | column_set

    # which chunks contain targets
    need_chunks = {}
    for (x, y, z) in BARRELS:
        need_chunks.setdefault((x >> 4, z >> 4), []).append(("barrel", (x, y, z)))
    for (x, y, z) in TRANSPORTER_COLUMN:
        need_chunks.setdefault((x >> 4, z >> 4), []).append(("transporter", (x, y, z)))

    chunks = read_chunks(path)
    print("region:", path)
    print("chunks present:", len(chunks))
    print("target chunks:", sorted(need_chunks.keys()))

    removed_be = 0
    air_blocks = 0
    touched_chunks = 0

    for i in sorted(chunks):
        timestamp, comp, payload = chunks[i]
        raw = decompress(payload, comp)
        tag = nbtlib.File.parse(io.BytesIO(raw))
        cx = int(tag.get("xPos", -99999))
        cz = int(tag.get("zPos", -99999))
        if (cx, cz) not in need_chunks:
            continue

        changed = False
        bes = tag.get("block_entities")
        if bes is not None:
            kept = []
            for be in bes:
                iid = str(be.get("id", ""))
                pos = (int(be.get("x")), int(be.get("y")), int(be.get("z")))
                drop = False
                if pos in barrel_set and any(iid.startswith(b) for b in BARREL_IDS):
                    drop = True
                if pos in column_set and TRANSPORT_KEY in iid:
                    drop = True
                if CLEAR_STUCK_TRANSPORTERS and TRANSPORT_KEY in iid:
                    its = be.get("items")
                    if its is not None and len(its) > 0:
                        drop = True
                if drop:
                    print("  remove BE %-42s %s" % (iid, pos))
                    removed_be += 1
                    changed = True
                    if REPLACE_WITH_AIR:
                        if set_block_air(tag, *pos):
                            air_blocks += 1
                else:
                    kept.append(be)
            if changed:
                tag["block_entities"] = nbtlib.List[nbtlib.Compound](kept)

        if changed:
            buf = io.BytesIO()
            tag.write(buf)
            new_payload = zlib.compress(buf.getvalue(), 6)
            chunks[i] = (timestamp, 2, new_payload)
            touched_chunks += 1

    print("-" * 60)
    print("block entities removed:", removed_be)
    print("blocks set to air     :", air_blocks)
    print("chunks rewritten      :", touched_chunks)

    if not apply:
        print("\nDRY RUN - nothing written. Re-run with --apply to write.")
        return
    if touched_chunks == 0:
        print("\nNothing to do.")
        return

    bak = path + ".bak-" + datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
    shutil.copy2(path, bak)
    print("backup written:", bak)
    write_region(path, chunks)
    print("region written OK")


if __name__ == "__main__":
    main()
