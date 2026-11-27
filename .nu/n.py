import subprocess

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'

for cls in [
    'com.miophas.singularity_iteration.common.block.reactor.mio_icif_Block_Nuke',
    'com.miophas.singularity_iteration.common.blockentity.reactor.mio_icif_reactor_nuke',
]:
    r = subprocess.run(['javap', '-p', '-constants', '-cp', jar, cls],
                       capture_output=True, text=True, errors='replace')
    print('=' * 70)
    print(cls.split('.')[-1])
    print('=' * 70)
    print((r.stdout or r.stderr)[:3000])
