#!/usr/bin/env python3
"""
阶段1：分析 FreshAnimations 模型结构（猪/牛/羊/鸡的 .jem/.properties/texture）
用法: python stage1_analyze_fa.py
"""
import zipfile, json
from pathlib import Path

ROOT = Path(__file__).parent.parent.parent  # GoldenTweaks/
FA_ZIP = ROOT / "run" / "resourcepacks" / "FreshAnimations_v1.10.4.zip"

def read_zip_json(zf, name):
    try: return json.loads(zf.read(name))
    except: return None

def read_zip_text(zf, name):
    try: return zf.read(name).decode('utf-8')
    except: return None

def main():
    print("=" * 60)
    print("阶段 1：分析 FreshAnimations 模型结构")
    print("=" * 60)
    with zipfile.ZipFile(FA_ZIP, 'r') as zf:
        for entity in ['pig', 'cow', 'sheep', 'chicken']:
            print(f"\n{'─' * 40}\n  [{entity.upper()}]")
            related = [n for n in zf.namelist() if entity in n.lower()
                       and ('/cem/' in n or '/textures/entity/' in n)
                       and 'mooshroom' not in n and 'piglin' not in n
                       and 'creeper' not in n and 'mushroom' not in n]
            for r in related:
                info = zf.getinfo(r)
                print(f"    {r} ({info.file_size} bytes)")

            jem_path = f"assets/minecraft/optifine/cem/{entity}.jem"
            jem = read_zip_json(zf, jem_path)
            if jem and isinstance(jem, dict):
                print(f"\n    [JEMModel] keys={list(jem.keys())}, "
                      f"textureSize={jem.get('textureSize')}, "
                      f"models={len(jem.get('models',[]))}")

            props_path = f"assets/minecraft/optifine/cem/{entity}.properties"
            props = read_zip_text(zf, props_path)
            if props: print(f"\n    [Properties] {props.strip()}")

            for variant in ['cold', 'warm']:
                v_jem = f"assets/minecraft/optifine/cem/{variant}_{entity}.jem"
                v_props = f"assets/minecraft/optifine/cem/{variant}_{entity}.properties"
                vj = read_zip_json(zf, v_jem)
                vp = read_zip_text(zf, v_props)
                if vj or vp:
                    print(f"\n    [{variant}_{entity}] "
                          f"textureSize={vj.get('textureSize') if vj else '?'}"
                          f"{' | ' + vp.strip() if vp else ''}")

if __name__ == '__main__':
    main()
