import subprocess

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'

# 找注册 mio_icif_nuclear_material 的地方，看构造时给的辐射值
r = subprocess.run(['javap', '-p', '-c', '-constants', '-cp', jar,
                    'com.miophas.singularity_iteration.common.registry.mio_icif_items'],
                   capture_output=True, text=True, errors='replace')
text = r.stdout or ''
i = text.find('static {}')
seg = text[i:] if i >= 0 else text

# 打印含 nuclear_material 的片段
lines = seg.split('\n')
for idx, line in enumerate(lines):
    if 'nuclear_material' in line:
        print('--- around line', idx, '---')
        for l in lines[max(0, idx - 14):idx + 4]:
            print(' ', l.strip()[:140])
        print()
