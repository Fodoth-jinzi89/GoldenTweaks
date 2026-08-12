#!/usr/bin/env python3
"""
生成 Thaumcraft tainted 兼容纹理（匹配 FreshAnimations UV 布局）
用法: python generate_tainted.py
"""
import zipfile, json, subprocess, sys, io
from pathlib import Path

ROOT = Path(__file__).parent.parent.parent
FA_ZIP = ROOT / "run" / "resourcepacks" / "FreshAnimations_v1.10.4.zip"
OUTPUT = ROOT / "script" / "output" / "GT_ThaumcraftTaintFix_FA"

subprocess.run([sys.executable, "-m", "pip", "install", "Pillow", "-q"], capture_output=True)
from PIL import Image, ImageEnhance

def make_tainted(img):
    img = img.convert('RGBA')
    img = ImageEnhance.Color(img).enhance(0.3)
    img = ImageEnhance.Brightness(img).enhance(0.7)
    img = ImageEnhance.Contrast(img).enhance(1.3)
    r, g, b, a = img.split()
    r = r.point(lambda x: min(255, int(x * 1.0 + 40)))
    g = g.point(lambda x: int(x * 0.55))
    b = b.point(lambda x: min(255, int(x * 1.0 + 30)))
    return Image.merge('RGBA', (r, g, b, a))

MAPPING = {
    'pig':    {'fa':'assets/minecraft/textures/entity/pig/pig.png', 'out':'pig.png',
               'cold':'assets/minecraft/textures/entity/pig/cold_pig.png',
               'warm':'assets/minecraft/textures/entity/pig/warm_pig.png'},
    'cow':    {'fa':'assets/minecraft/textures/entity/cow/cow.png', 'out':'cow.png',
               'cold':'assets/minecraft/textures/entity/cow/cold_cow.png',
               'warm':'assets/minecraft/textures/entity/cow/warm_cow.png'},
    'sheep':  {'fa':'assets/minecraft/textures/entity/sheep/sheep.png', 'out':'sheep.png'},
    'chicken':{'fa':'assets/minecraft/textures/entity/chicken.png', 'out':'chicken.png',
               'cold':'assets/minecraft/textures/entity/chicken/cold_chicken.png',
               'warm':'assets/minecraft/textures/entity/chicken/warm_chicken.png'},
}

def main():
    print("生成 Tainted 兼容纹理")
    print("=" * 60)
    with zipfile.ZipFile(FA_ZIP, 'r') as zf:
        for entity, m in MAPPING.items():
            print(f"\n  [{entity.upper()}]")
            try: img = Image.open(io.BytesIO(zf.read(m['fa'])))
            except Exception as e: print(f"    [!] {e}"); continue

            ti = make_tainted(img)
            base = OUTPUT / "assets" / "thaumcraft" / "textures" / "models"
            (base / m['out']).parent.mkdir(parents=True, exist_ok=True)
            ti.save(str(base / m['out']))
            print(f"    -> {m['out']} ({ti.size})")

            cmp = m['out'].replace('.png', f'_{ti.size[0]}x{ti.size[1]}.png')
            (base / "compat").mkdir(parents=True, exist_ok=True)
            ti.save(str(base / "compat" / cmp))
            print(f"    -> compat/{cmp}")

            for vk, vl in [('cold','cold'), ('warm','warm')]:
                if vk not in m or m[vk] is None: continue
                try:
                    vi = Image.open(io.BytesIO(zf.read(m[vk])))
                    vt = make_tainted(vi)
                    vname = f"{vl}_{entity}.png"
                    vt.save(str(base / vname))
                    print(f"    -> {vname} ({vt.size})")
                    vcmp = f"{vl}_{entity}_{vt.size[0]}x{vt.size[1]}.png"
                    vt.save(str(base / "compat" / vcmp))
                    print(f"    -> compat/{vcmp}")
                except Exception as e: print(f"    [!] {vl}: {e}")

    (OUTPUT / "pack.mcmeta").write_text(json.dumps(
        {"pack":{"pack_format":34,"description":"§5Thaumcraft Tainted Fix §r- FreshAnimations Compat"}},
        indent=2), encoding='utf-8')
    Image.new('RGBA',(128,128),(100,20,120,255)).save(str(OUTPUT / "pack.png"))

    print("\n" + "=" * 60)
    print(f"输出: {OUTPUT}")
    for f in sorted(OUTPUT.rglob("*")):
        if f.is_file(): print(f"  {f.relative_to(OUTPUT)}")

if __name__ == '__main__':
    main()
