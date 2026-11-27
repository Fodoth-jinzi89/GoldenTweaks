import subprocess

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'

r = subprocess.run(['javap', '-p', '-c', '-constants', '-cp', jar,
                    'com.miophas.singularity_iteration.common.item.resource.mio_icif_resources'],
                   capture_output=True, text=True, errors='replace')
text = r.stdout or ''
i = text.find('static {}')
seg = text[i:] if i >= 0 else text
lines = seg.split('\n')

for idx, line in enumerate(lines):
    if 'nuclear_material' in line and '"<init>"' in line:
        # 往前找对应的 ldc / 字段名
        window = lines[max(0, idx - 22):idx + 2]
        txt = '\n'.join(window)
        if any(k in txt.lower() for k in ('uran', 'mox', 'plutonium', 'rtg')):
            print('---')
            for l in window:
                print(' ', l.strip()[:150])
