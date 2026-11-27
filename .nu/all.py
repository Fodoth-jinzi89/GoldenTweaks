import subprocess, re

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'
r = subprocess.run(['javap', '-p', '-c', '-constants', '-cp', jar,
                    'com.miophas.singularity_iteration.common.item.resource.mio_icif_resources'],
                   capture_output=True, text=True, errors='replace')
text = r.stdout or ''

# 每个 lambda 方法：名字 + 是否构造 nuclear_material + 其中的 float 常量
blocks = re.split(r'\n  (?=private static |public static |static \{)', text)
rows = []
for b in blocks:
    m = re.match(r'(\S.*?)\(', b) or re.match(r'(\S.*?);', b)
    head = b.strip().split('\n')[0][:90]
    if 'nuclear_material' in b:
        floats = re.findall(r'// float ([\d.]+)f', b)
        rows.append((head, floats))

print(f'nuclear_material constructions: {len(rows)}')
for head, floats in rows:
    print(f'  {head}\n     -> {floats}')
