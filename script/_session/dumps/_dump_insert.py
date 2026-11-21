import subprocess
jar = r'libs\implementation\avaritia_more_items-1.21.1-NeoForge-beta-1.0.9.jar'
cls = 'net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.recipe.MultiblockRecipeMatcher'
out = subprocess.run(['javap','-p','-classpath',jar,cls],capture_output=True,text=True).stdout
for l in out.splitlines():
    if 'insert' in l.lower() or 'consume' in l.lower():
        print(l.strip())
