import subprocess
jar = r'libs\implementation\avaritia_more_items-1.21.1-NeoForge-beta-1.0.9.jar'
cls = 'net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.recipe.MultiblockRecipeMatcher'
out = subprocess.run(['javap','-p','-c','-classpath',jar,cls],capture_output=True,text=True).stdout
lines = out.splitlines()
idx = [i for i,l in enumerate(lines) if 'canInsertItems(java.util.List<net.minecraft.world.item.ItemStack>, java.util.List' in l]
start = idx[0]
for l in lines[start+45:start+110]:
    print(l.strip())
