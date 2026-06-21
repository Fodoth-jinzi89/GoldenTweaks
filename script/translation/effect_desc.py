import json
from collections import OrderedDict

input_file = "zh_cn.json"
output_file = "zh_cn_fixed.json"

# 读取 JSON（保序）
with open(input_file, "r", encoding="utf-8") as f:
    data = json.load(f, object_pairs_hook=OrderedDict)

keys = list(data.keys())

to_add = OrderedDict()

for k in keys:
    # 只处理 effect.xxx.xxx
    if k.startswith("effect.") and not k.endswith(".description"):
        desc_key = k + ".description"

        if desc_key not in data:
            to_add[desc_key] = ""

# 合并：原数据 + 新增项（追加到末尾）
for k, v in to_add.items():
    data[k] = v

# 写回文件（保序输出）
with open(output_file, "w", encoding="utf-8") as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print(f"完成，共新增 {len(to_add)} 条 description")