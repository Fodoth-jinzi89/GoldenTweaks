import io, json

# "X" was the compact figure itself, not a part of the unit — the number is substituted into %s and
# the unit follows it directly.
zh = {
    "gui.goldentweaks.jade.power.value": "%s FE / t",
    "gui.goldentweaks.jade.max_input.value": "%s FE / t",
}
en = {
    "gui.goldentweaks.jade.power.value": "%s FE/t",
    "gui.goldentweaks.jade.max_input.value": "%s FE/t",
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
