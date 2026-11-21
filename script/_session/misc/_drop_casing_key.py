import io, json

stale = ("tooltip.goldentweaks.chemical_reactor.casing",)

def patch(path):
    data = json.load(io.open(path, encoding='utf-8'))
    removed = [k for k in stale if data.pop(k, None) is not None]
    with io.open(path, 'w', encoding='utf-8', newline='\n') as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write('\n')
    print(path.split('/')[-1], len(data), 'removed', removed)

patch(r'src/main/resources/assets/goldentweaks/lang/zh_cn.json')
patch(r'src/main/resources/assets/goldentweaks/lang/en_us.json')
