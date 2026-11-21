import subprocess, sys, os

jar = r'libs\implementation\Jade-1.21.1-NeoForge-15.10.6.jar'
cls = 'snownee.jade.impl.ui.SimpleProgressStyle'
out = subprocess.run(['javap', '-p', '-c', '-classpath', jar, cls],
                     capture_output=True, text=True).stdout
lines = out.splitlines()

start = next(i for i, l in enumerate(lines) if 'public void render(' in l)
# print until the next method signature
end = start + 1
while end < len(lines) and not lines[end].startswith('  public ') and not lines[end].startswith('  private '):
    end += 1

body = lines[start:end]
print('--- method body, dot 380..None ---')
for l in body:
    s = l.strip()
    if not s:
        continue
    head = s.split(':')[0]
    if head.isdigit() and 370 <= int(head) <= 500:
        print(s)
