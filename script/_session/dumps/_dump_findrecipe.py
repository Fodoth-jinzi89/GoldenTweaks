import subprocess

jar = r'libs\implementation\avaritia_more_items-1.21.1-NeoForge-beta-1.0.9.jar'
cls = 'net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.abstracts.AbstractMultiblockTile'
out = subprocess.run(['javap', '-p', '-c', '-classpath', jar, cls],
                     capture_output=True, text=True).stdout
lines = out.splitlines()

targets = ['findRecipe(', 'startRecipe(', 'batchRecipe(']
for t in targets:
    idx = [i for i, l in enumerate(lines) if t in l and 'Code:' not in l]
    if not idx:
        print(f'--- {t}: NOT FOUND ---')
        continue
    start = idx[0]
    end = start + 1
    # a method body ends at the first line that starts a new member declaration
    while end < len(lines):
        s = lines[end]
        if s.startswith('  protected ') or s.startswith('  public ') or s.startswith('  private ') or s.startswith('  static ') or s.startswith('}'):
            break
        end += 1
    print(f'===== {t} (lines {start}-{end}) =====')
    for l in lines[start:end]:
        print(l)
    print()
