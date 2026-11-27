import zipfile

jar = r'E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods\[S]-mio_icif-0.1.11.155-neoforge-1.21.1.jar'
with zipfile.ZipFile(jar) as z:
    print(z.read('data/c/tags/item/nuclear.json').decode('utf-8'))
