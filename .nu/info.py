import subprocess, zipfile, json, glob, os

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'

# 1. 核材料类
cls = 'com.miophas.singularity_iteration.common.item.resource.mio_icif_nuclear_material'
r = subprocess.run(['javap', '-p', '-constants', '-cp', jar, cls],
                   capture_output=True, text=True, errors='replace')
print('=== mio_icif_nuclear_material ===')
print((r.stdout or r.stderr)[:2000])

# 2. jar 里已有的相关标签
print('\n=== tags mentioning nuclear/uranium ===')
with zipfile.ZipFile(jar) as z:
    for n in z.namelist():
        if n.startswith('data/') and 'tags/item' in n and ('nuclear' in n or 'uranium' in n):
            print(' ', n)
            try:
                print('   ', z.read(n).decode('utf-8')[:400])
            except Exception as e:
                print('   err', e)

# 3. 该模组铀相关物品 id
print('\n=== uranium-ish item assets ===')
with zipfile.ZipFile(jar) as z:
    for n in z.namelist():
        if 'models/item' in n and ('nuclear' in n.lower() or 'uranium' in n.lower()):
            print(' ', n)
