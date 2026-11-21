import subprocess

jar = r'libs\compileOnly\cofh_core-11.0.2-scex.dev.30.jar'
cls = 'cofh.core.client.gui.element.panel.PanelBase'
out = subprocess.run(['javap', '-p', '-c', '-classpath', jar, cls],
                     capture_output=True, text=True).stdout
lines = out.splitlines()

for target in ['public void update(', 'public void toggleOpen(', 'public void updateSize(',
               'protected void drawBackground(net.minecraft.client.gui.GuiGraphics)',
               'public void setFullyOpen(', 'protected int sideOffset(']:
    idx = [i for i, l in enumerate(lines) if l.strip().startswith(target)]
    if not idx:
        print(f'--- {target}: NOT FOUND ---\n')
        continue
    start = idx[0]
    end = start + 1
    while end < len(lines):
        s = lines[end]
        if s.startswith('  public ') or s.startswith('  protected ') or s.startswith('  private ') or s.startswith('}'):
            break
        end += 1
    print(f'===== {target} =====')
    for l in lines[start:end]:
        print(l)
    print()
