import subprocess, re

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'
r = subprocess.run(['javap', '-p', '-c', '-constants', '-cp', jar,
                    'com.miophas.singularity_iteration.common.item.resource.mio_icif_resources'],
                   capture_output=True, text=True, errors='replace')
text = r.stdout or ''

idx = text.find('nuclear_material')
print('first nuclear_material at', idx)
print(text[max(0, idx - 1200): idx + 400])
