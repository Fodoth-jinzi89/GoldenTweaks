import subprocess

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'
cls = 'com.miophas.singularity_iteration.common.blockentity.reactor.mio_icif_reactor_nuke'

r = subprocess.run(['javap', '-p', '-c', '-cp', jar, cls], capture_output=True, text=True, errors='replace')
text = r.stdout or ''

for name in ['public float getTotalRadioactivity(',
             'public float calculateBaseExplosionPower(',
             'public float calculateExplosionPower(',
             'public boolean isMaxYield(']:
    i = text.find(name)
    print('=' * 66)
    print(name)
    print('=' * 66)
    print(text[i:i + 1600] if i >= 0 else '(nf)')
    print()
