import zipfile

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-alltheores-3.2.0-neoforge-1.21.1.jar'
with zipfile.ZipFile(jar) as z:
    names = z.namelist()
    print('=== uranium-ish item models ===')
    for n in names:
        if 'models/item' in n and 'uranium' in n.lower():
            print(' ', n)
    print('\n=== tags containing nuclear/uranium ===')
    for n in names:
        if 'tags/item' in n and ('nuclear' in n.lower() or 'uranium' in n.lower()):
            print(' ', n)
            try:
                print('   ', z.read(n).decode('utf-8')[:300].replace('\n', ' '))
            except Exception:
                pass
