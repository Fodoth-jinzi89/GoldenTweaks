import subprocess

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'
r = subprocess.run(['javap', '-p', '-c', '-constants', '-cp', jar,
                    'com.miophas.singularity_iteration.common.item.resource.mio_icif_resources'],
                   capture_output=True, text=True, errors='replace')
text = r.stdout or ''
i = text.find('static {}')
seg = text[i:] if i >= 0 else text

# 只输出前 6000 字符，看结构
print(seg[:6000])
