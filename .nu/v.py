import subprocess

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'

for cls in [
    'com.miophas.singularity_iteration.common.multiblock.mio_icif_fluid_reactor_validator',
    'com.miophas.singularity_iteration.common.reactor.ChamberTopology',
]:
    r = subprocess.run(['javap', '-p', '-c', '-constants', '-cp', jar, cls],
                       capture_output=True, text=True, errors='replace')
    text = r.stdout or r.stderr
    print('=' * 70)
    print(cls.split('.')[-1])
    print('=' * 70)
    # 只挑字符串常量，找方块 id
    for line in text.split('\n'):
        if 'String ' in line or 'Block' in line and 'Field' in line:
            print(' ', line.strip()[:150])
