#!/usr/bin/env python3
"""
阶段2：安装Pillow + 纹理尺寸分析 + 对比FA/WS模型差异 + 搜索TC纹理引用
用法: python stage2_deep_analysis.py
"""
import zipfile, json, subprocess, sys, re, io
from pathlib import Path

ROOT = Path(__file__).parent.parent.parent
FA_ZIP  = ROOT / "run" / "resourcepacks" / "FreshAnimations_v1.10.4.zip"
WS_ZIP  = ROOT / "run" / "resourcepacks" / "Whimscape_x_FreshAnimations_1.20-1.21.11_r3.zip"
TC_JAR  = ROOT / "libs" / "implementation" / "[神秘时代]thaumcraft-0.2.2.95-port.152.jar"

subprocess.run([sys.executable, "-m", "pip", "install", "Pillow", "-q"], capture_output=True)
from PIL import Image

def main():
    print("=" * 60)
    print("FA 纹理尺寸")
    print("=" * 60)
    with zipfile.ZipFile(FA_ZIP, 'r') as zf:
        for entity in ['pig','cow','sheep','chicken']:
            for name in sorted(zf.namelist()):
                if entity not in name.lower() or not name.endswith('.png'): continue
                if 'textures/entity' not in name: continue
                if any(x in name.lower() for x in ['mooshroom','piglin','mushroom']): continue
                img = Image.open(io.BytesIO(zf.read(name)))
                print(f"  {name:60s} {img.size} {img.mode}")

    print("\n" + "=" * 60)
    print("Whimscape 纹理尺寸")
    print("=" * 60)
    with zipfile.ZipFile(WS_ZIP, 'r') as zf:
        for entity in ['pig','cow','sheep','chicken']:
            for name in sorted(zf.namelist()):
                if entity not in name.lower() or not name.endswith('.png'): continue
                if 'textures/entity' not in name: continue
                if any(x in name.lower() for x in ['mooshroom','piglin','mushroom']): continue
                img = Image.open(io.BytesIO(zf.read(name)))
                print(f"  {name:60s} {img.size} {img.mode}")

    print("\n" + "=" * 60)
    print(".jem textureSize 对比")
    print("=" * 60)
    with zipfile.ZipFile(FA_ZIP) as fz, zipfile.ZipFile(WS_ZIP) as wz:
        for entity in ['pig','cow','sheep','chicken']:
            jp = f"assets/minecraft/optifine/cem/{entity}.jem"
            fa_ts = json.loads(fz.read(jp)).get('textureSize','?')
            ws_ts = json.loads(wz.read(jp)).get('textureSize','?')
            print(f"  {entity}: FA={fa_ts}, WS={ws_ts}")

    print("\n" + "=" * 60)
    print("TC tainted 纹理引用")
    print("=" * 60)
    with zipfile.ZipFile(TC_JAR, 'r') as zf:
        for cls_name in zf.namelist():
            if not cls_name.endswith('.class'): continue
            cname = cls_name.split('/')[-1]
            if not any(k in cname for k in ['LegacyThaumcraftMobRenderer','TaintCow',
                'TaintPig','TaintSheep','TaintChicken','TCEntityTypes']):
                continue
            try:
                text = zf.read(cls_name).decode('latin-1', errors='ignore')
                paths = re.findall(r'(?:textures?/[a-zA-Z0-9_/.-]+\.png|[a-z]+\.png)', text)
                relevant = {p for p in paths if any(a in p for a in ['cow','pig','sheep','chicken','taint','model'])}
                if relevant: print(f"  {cname}: {sorted(relevant)}")
            except: pass
    print("\n完成！")

if __name__ == '__main__':
    main()
