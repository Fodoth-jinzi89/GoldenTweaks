import subprocess, zipfile

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'

# 哪个类引用了 mio_icif_nuclear_material 的构造器？
with zipfile.ZipFile(jar) as z:
    names = [n for n in z.namelist() if n.endswith('.class')]

hits = []
for n in names:
    if n.startswith('com/miophas/singularity_iteration/common/registry'):
        hits.append(n)
print('registry classes:')
for h in hits:
    print(' ', h)

# 直接暴力搜索哪些 class 的常量池提到 nuclear_material
import re
target = b'mio_icif_nuclear_material'
found = []
with zipfile.ZipFile(jar) as z:
    for n in names:
        if n.startswith('com/miophas') and 'nuclear_material' not in n:
            data = z.read(n)
            if target in data:
                found.append(n)

print('\nclasses referencing mio_icif_nuclear_material:')
for f in found:
    print(' ', f)
