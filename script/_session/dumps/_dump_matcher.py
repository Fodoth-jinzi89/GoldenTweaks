import subprocess

jar = r'libs\implementation\avaritia_more_items-1.21.1-NeoForge-beta-1.0.9.jar'
cls = 'net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.recipe.MultiblockRecipeMatcher'
out = subprocess.run(['javap', '-p', '-classpath', jar, cls],
                     capture_output=True, text=True).stdout
for line in out.splitlines():
    if 'canInsert' in line or 'canExtract' in line:
        print(line.strip())
