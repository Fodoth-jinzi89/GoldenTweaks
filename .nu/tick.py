import subprocess

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'

# getICTNTCount / tick / spawnNukeEntity
r = subprocess.run(['javap', '-p', '-c', '-constants', '-cp', jar,
                    'com.miophas.singularity_iteration.common.blockentity.reactor.mio_icif_reactor_nuke'],
                   capture_output=True, text=True, errors='replace')
t = r.stdout or ''
for name in ['public int getICTNTCount(', 'public static void tick(']:
    i = t.find(name)
    print('=' * 60)
    print(name)
    print('=' * 60)
    print(t[i:i + 2000] if i >= 0 else 'nf')
    print()

# 爆炸倍率配置默认值
r2 = subprocess.run(['javap', '-p', '-c', '-constants', '-cp', jar,
                     'com.miophas.singularity_iteration.common.Singularity_Iteration_Config'],
                    capture_output=True, text=True, errors='replace')
t2 = r2.stdout or ''
i = t2.find('NUKE_EXPLOSION_MULTIPLIER')
print('=== NUKE_EXPLOSION_MULTIPLIER def ===')
print(t2[max(0, i - 400):i + 900] if i >= 0 else 'nf')
