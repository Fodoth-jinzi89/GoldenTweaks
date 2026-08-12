#!/usr/bin/env python3
"""打包并部署到 run/resourcepacks/"""
import zipfile, shutil
from pathlib import Path

ROOT = Path(__file__).parent.parent.parent
SRC  = ROOT / "script" / "output" / "GT_ThaumcraftTaintFix_FA"
ZIP  = ROOT / "run" / "resourcepacks" / "GT_ThaumcraftTaintFix_FA.zip"
DIR  = ROOT / "run" / "resourcepacks" / "GT_ThaumcraftTaintFix_FA"

def main():
    with zipfile.ZipFile(ZIP, 'w', zipfile.ZIP_DEFLATED) as zf:
        for f in sorted(SRC.rglob("*")):
            if f.is_file():
                zf.write(str(f), str(f.relative_to(SRC)).replace('\\','/'))
    if DIR.exists(): shutil.rmtree(DIR)
    shutil.copytree(SRC, DIR)

    print("部署完成！")
    with zipfile.ZipFile(ZIP, 'r') as zf:
        for n in sorted(zf.namelist()):
            if not n.endswith('/'): print(f"  {n}")

if __name__ == '__main__':
    main()
