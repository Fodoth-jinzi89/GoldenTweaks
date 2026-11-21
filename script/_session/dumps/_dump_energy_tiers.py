import subprocess

jar = r'libs\implementation\avaritia_more_items-1.21.1-NeoForge-beta-1.0.9.jar'
cls = 'net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.MultiblockPortTypes'
out = subprocess.run(['javap', '-p', '-c', '-classpath', jar, cls],
                     capture_output=True, text=True).stdout
lines = out.splitlines()

st = next(i for i, l in enumerate(lines) if l.strip() == 'static {};')
print('=== static initializer: tier tables ===')
for l in lines[st:st + 200]:
    s = l.strip()
    if not s:
        continue
    print(s)
