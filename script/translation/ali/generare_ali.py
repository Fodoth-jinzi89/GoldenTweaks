import json
import re

input_file = "input2.txt"
output_file = "output.json"

pattern = re.compile(r"\[ALI\] ellipsis text=(.+)$")

lang = {}

with open(input_file, "r", encoding="utf-8") as f:
    for line in f:
        match = pattern.search(line)
        if not match:
            continue

        key = match.group(1).strip()

        # entity.minecraft.villager.ae_engineer -> AE Engineer
        value = key.split(".")[-1]
        value = value.replace("_", " ")
        value = value.title()

        lang[key] = value

with open(output_file, "w", encoding="utf-8") as f:
    json.dump(lang, f, ensure_ascii=False, indent=2)

print(f"已生成 {output_file}，共 {len(lang)} 条")