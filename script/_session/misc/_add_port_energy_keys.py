import io, json

zh = {
    "gui.goldentweaks.port.energy.stored_value": "%sFE/%sFE",
    "gui.goldentweaks.port.energy.rate_value": "%sFE/t",
}
en = {
    "gui.goldentweaks.port.energy.stored_value": "%sFE/%sFE",
    "gui.goldentweaks.port.energy.rate_value": "%sFE/t",
}

def patch(path, pairs):
    data = json.load(io.open(path, encoding='utf-8'))
    data.update(pairs)
    with io.open(path, 'w', encoding='utf-8', newline='\n') as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write('\n')
    print(path.split('/')[-1], len(data))

patch(r'src/main/resources/assets/goldentweaks/lang/zh_cn.json', zh)
patch(r'src/main/resources/assets/goldentweaks/lang/en_us.json', en)
