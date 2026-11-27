import subprocess

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'
cls = 'com.miophas.singularity_iteration.common.blockentity.reactor.mio_icif_reactor_nuke'
r = subprocess.run(['javap', '-p', '-c', '-cp', jar, cls], capture_output=True, text=True, errors='replace')
t = r.stdout or ''

i = t.find('public boolean isMaxYield(')
j = t.find('public static void tick(')
print(t[i:j] if i >= 0 else 'nf')
