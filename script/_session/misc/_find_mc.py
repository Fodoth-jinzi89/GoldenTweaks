import subprocess, glob, os
# Find the Minecraft client jar in the gradle cache to inspect Component's API.
hits = []
for root in [os.path.expanduser(r"~\.gradle\caches\modules-2"), os.path.expanduser(r"~\.gradle\caches\neoformruntime")]:
    if os.path.isdir(root):
        for dirpath, _, files in os.walk(root):
            for f in files:
                if f.startswith("client-") and f.endswith(".jar"):
                    hits.append(os.path.join(dirpath, f))
print("candidates:", len(hits))
for h in hits[:3]:
    print(h)
