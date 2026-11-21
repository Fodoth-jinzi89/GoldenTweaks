import subprocess

jar = r'libs\compileOnly\cofh_core-11.0.2-scex.dev.30.jar'
cls = 'cofh.core.client.gui.element.panel.PanelBase'
out = subprocess.run(['javap', '-p', '-c', '-classpath', jar, cls],
                     capture_output=True, text=True).stdout
lines = out.splitlines()

# static initializer: which value goes into panelExpandSpeed
try:
    st = next(i for i, l in enumerate(lines) if l.strip() == 'static {};')
    print('===== static init =====')
    for l in lines[st:st + 30]:
        print(l)
except StopIteration:
    print('no static init found')

# does updateSize animate height too?
idx = [i for i, l in enumerate(lines) if 'public void updateSize(' in l][0]
end = idx + 1
while end < len(lines) and not lines[end].startswith('  public ') and not lines[end].startswith('  protected ') and not lines[end].startswith('  private '):
    end += 1
print(f'\n===== updateSize (lines {end-idx}) =====')
for l in lines[idx:end]:
    s = l.strip()
    if 'height' in s or 'Height' in s or s.startswith('putfield') or 'Field' in s:
        print(s)
