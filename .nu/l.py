import subprocess

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'

for cls in [
    'com.miophas.singularity_iteration.common.reactor.ReactorLayoutAnalysis',
    'com.miophas.singularity_iteration.common.reactor.ReactorInventory',
]:
    r = subprocess.run(['javap', '-p', '-cp', jar, cls],
                       capture_output=True, text=True, errors='replace')
    print('=' * 70)
    print(cls.split('.')[-1])
    print('=' * 70)
    print((r.stdout or r.stderr)[:2500])
