#!/usr/bin/env python3
"""阶段3：深入诊断 TC tainted 实体纹理路径"""
import zipfile, re
from pathlib import Path

ROOT = Path(__file__).parent.parent.parent
TC_JAR = ROOT / "libs" / "implementation" / "[神秘时代]thaumcraft-0.2.2.95-port.152.jar"

def main():
    with zipfile.ZipFile(TC_JAR, 'r') as zf:
        print("=" * 60)
        print("textures/ 目录")
        print("=" * 60)
        for name in sorted(zf.namelist()):
            if 'textures/' in name and not name.endswith('/'):
                print(f"  {name} ({zf.getinfo(name).file_size} bytes)")

        print("\n" + "=" * 60)
        print("LegacyThaumcraftMobRenderer / TCEntityTypes 引用")
        print("=" * 60)
        for name in zf.namelist():
            if not name.endswith('.class'): continue
            cname = name.split('/')[-1]
            if cname not in ['LegacyThaumcraftMobRenderer.class','TCClientSetup.class','TCEntityTypes.class']:
                continue
            try:
                text = zf.read(name).decode('latin-1', errors='ignore')
                pngs = set(re.findall(r'[a-zA-Z0-9_/]+\.png', text))
                taints = set(re.findall(r'TAINT_[A-Z_]+', text))
                models = set(re.findall(r'[A-Z][a-zA-Z]+Model', text))
                print(f"\n  [{cname}]")
                if taints: print(f"    TAINT: {taints}")
                if pngs: print(f"    PNG:   {pngs}")
                if models: print(f"    Model: {models}")
            except: pass
    print("\n完成！")

if __name__ == '__main__':
    main()
