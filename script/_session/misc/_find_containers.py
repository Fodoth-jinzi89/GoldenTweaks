import os, subprocess, glob
# Locate the patched Minecraft sources to read Containers.
hits = []
for root in [os.path.expanduser(r"~\.gradle\caches"), r"E:\MCreator\Workspaces\GoldenTweaks\.gradle"]:
    if not os.path.isdir(root):
        continue
    for dirpath, dirnames, files in os.walk(root):
        for f in files:
            if f in ("Containers.java", "Containers.class"):
                hits.append(os.path.join(dirpath, f))
print(len(hits))
for h in hits[:5]:
    print(h)
