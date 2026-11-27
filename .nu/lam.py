import subprocess, re

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'
r = subprocess.run(['javap', '-p', '-c', '-constants', '-cp', jar,
                    'com.miophas.singularity_iteration.common.item.resource.mio_icif_resources'],
                   capture_output=True, text=True, errors='replace')
text = r.stdout or ''

# 找 lambda 方法里构造 nuclear_material 的，看 float 常量
for m in re.finditer(r'private static .*?lambda\$\w+\(\);', text):
    start = m.start()
    seg = text[start:start + 900]
    if 'nuclear_material' in seg:
        name = text[start:start + 90].split('\n')[0]
        floats = re.findall(r'// float ([\d.]+)f', seg)
        print(f'{name}\n  floats: {floats}\n')
