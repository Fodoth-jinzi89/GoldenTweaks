import io, json

TIERS = ["ULV", "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV"]

zh, en = {}, {}
for index, code in enumerate(TIERS, start=1):
    for suffix, zh_name, en_name in [
        ("input", "能源仓", "Energy Hatch"),
        ("output", "动力仓", "Dynamo Hatch"),
    ]:
        key = f"block.goldentweaks.gt_port_energy_{suffix}.tier_{index}"
        # Roman numeral on the left, code in the middle, then the port's own name.
        zh[key] = f"{'I' * index if index <= 3 else ['IV', 'V', 'VI', 'VII', 'VIII', 'IX'][index - 4]} {code} {zh_name}"
        en[key] = f"{'I' * index if index <= 3 else ['IV', 'V', 'VI', 'VII', 'VIII', 'IX'][index - 4]} {code} {en_name}"

for path, extra in [
    (r'src/main/resources/assets/goldentweaks/lang/zh_cn.json', zh),
    (r'src/main/resources/assets/goldentweaks/lang/en_us.json', en),
]:
    data = json.load(io.open(path, encoding='utf-8'))
    data.update(extra)
    with io.open(path, 'w', encoding='utf-8', newline='\n') as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write('\n')
    print('lang updated', path)

for key in list(zh)[:4]:
    print(key, '=>', zh[key])
