"""Translate the three multiblock messages AMI leaves untranslated.

`message.avaritia_more_items.multiblock.{already_formed,unformed,missing_port}` are keys our
own code emits, but AMI ships no strings for them — so a player who builds a shape wrong sees
the raw key. They live under AMI's namespace because that is the id our code already asks for;
adding them here is the smallest fix that does not change the call sites.

`missing_port` takes one argument: the description of the requirement that is not satisfied.
"""
import io
import json

zh = {
    "message.avaritia_more_items.multiblock.already_formed": "多方块结构已经成型",
    "message.avaritia_more_items.multiblock.unformed": "多方块结构不完整",
    "message.avaritia_more_items.multiblock.missing_port": "结构缺少端口：%s",
}
en = {
    "message.avaritia_more_items.multiblock.already_formed":
        "The multiblock is already formed",
    "message.avaritia_more_items.multiblock.unformed":
        "The multiblock structure is incomplete",
    "message.avaritia_more_items.multiblock.missing_port":
        "The structure is missing a port: %s",
}

def patch(path, pairs):
    data = json.load(io.open(path, encoding='utf-8'))
    data.update(pairs)
    with io.open(path, 'w', encoding='utf-8', newline='\n') as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write('\n')
    print(path.split('/')[-1], len(data))

patch(r'src/main/resources/assets/goldentweaks/lang/zh_cn.json', zh)
patch(r'src/main/resources/assets/goldentweaks/lang/en_us.json', en)
