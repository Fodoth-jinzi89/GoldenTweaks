import subprocess
jar = r'libs\implementation\avaritia_more_items-1.21.1-NeoForge-beta-1.0.9.jar'
cls = 'net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.port.MultiblockPortTypes'
out = subprocess.run(['javap','-p','-c','-classpath',jar,cls],capture_output=True,text=True).stdout
lines = out.splitlines()
start = None
for i,l in enumerate(lines):
    if 'ENERGY_TRANSFER_BY_TIER' in l and 'putstatic' in l:
        start = i; break
if start is None:
    print('not found')
else:
    for l in lines[max(0,start-45):start+3]:
        s=l.strip()
        if s: print(s)
