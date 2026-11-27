import subprocess, zipfile, re

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'

# 爆炸倍率默认值
r = subprocess.run(['javap', '-p', '-c', '-constants', '-cp', jar,
                    'com.miophas.singularity_iteration.common.Singularity_Iteration_Config'],
                   capture_output=True, text=True, errors='replace')
t = r.stdout or ''
i = t.find('// String nukeExplosionMultiplier')
if i < 0:
    i = t.find('nukeExplosion')
print('=== around nuke multiplier def ===')
print(t[max(0, i - 500):i + 700] if i >= 0 else 'not found')

# 核弹方块注册名
r2 = subprocess.run(['javap', '-p', '-c', '-constants', '-cp', jar,
                     'com.miophas.singularity_iteration.common.registry.mio_icif_blocks'],
                    capture_output=True, text=True, errors='replace')
t2 = r2.stdout or ''
print('\n=== nuke block registration ===')
for m in re.finditer(r'String ([\w/]*(?:nuke|NUKE)[\w/]*)', t2):
    print(' ', m.group(1))
