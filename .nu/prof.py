import subprocess

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'
for cls in [
    'com.miophas.singularity_iteration.core.runtime.reactor.NuclearBlastProfile',
]:
    r = subprocess.run(['javap', '-p', '-c', '-constants', '-cp', jar, cls],
                       capture_output=True, text=True, errors='replace')
    print('=' * 66)
    print(cls.split('.')[-1])
    print('=' * 66)
    print((r.stdout or r.stderr)[:3500])
