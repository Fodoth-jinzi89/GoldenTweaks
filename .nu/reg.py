import subprocess

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'

# 找核材料注册处
r = subprocess.run(['javap', '-p', '-cp', jar,
                    'com.miophas.singularity_iteration.common.registry.mio_icif_items'],
                   capture_output=True, text=True, errors='replace')
print('=== mio_icif_items (grep nuclear) ===')
t = r.stdout or r.stderr
for line in t.split('\n'):
    if any(k in line.lower() for k in ('uran', 'nuclear', 'mox', 'plutonium', 'rtg')):
        print(' ', line.strip()[:150])

# 找 block_uranium 的注册
for cls in ['com.miophas.singularity_iteration.common.registry.mio_icif_blocks']:
    r2 = subprocess.run(['javap', '-p', '-cp', jar, cls], capture_output=True, text=True, errors='replace')
    print(f'\n=== {cls.split(".")[-1]} (grep uran) ===')
    for line in (r2.stdout or '').split('\n'):
        if 'uran' in line.lower():
            print(' ', line.strip()[:150])
