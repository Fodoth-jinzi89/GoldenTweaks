import json
from pathlib import Path
from collections import OrderedDict

# =========================
# 配置
# =========================

INPUT_FILE = "en_us.json"
OUTPUT_FILE = "zh_cn.json"

# 是否保留原英文（False = 留空等待翻译）
KEEP_ENGLISH = False

# =========================
# 词典替换
# 可自行继续扩充
# =========================

WORD_MAP = {
    # 材质
    "Amethyst": "紫水晶",
    "Andesite": "安山岩",
    "Asurine": "蔚蓝岩",
    "Basalt": "玄武岩",
    "Blackstone": "黑石",
    "Calcite": "方解石",
    "Copper": "铜",
    "Crimsite": "绯红岩",
    "Deepslate": "深板岩",
    "Diorite": "闪长岩",
    "Dolomite": "白云岩",
    "Dripstone": "滴水石",
    "Gabbro": "辉长岩",
    "Gold": "金",
    "Brass": "黄铜",
    "Granite": "花岗岩",
    "Industrial": "工业",
    "Iron": "铁",
    "Limestone": "石灰岩",
    "Netherite": "下界合金",
    "Netherrack": "下界岩",
    "Ochrum": "赭黄岩",
    "Packed Mud": "泥坯",
    "Scorchia": "焦灼岩",
    "Scoria": "火山渣",
    "Stone": "石头",
    "Tuff": "凝灰岩",
    "Veridium": "翠绿岩",
    "Weathered Limestone": "风化石灰岩",
    "Zinc": "锌",

    # 颜色
    "Black": "黑色",
    "Blue": "蓝色",
    "Brown": "棕色",
    "Cyan": "青色",
    "Gray": "灰色",
    "Green": "绿色",
    "Light Blue": "淡蓝色",
    "Light Gray": "淡灰色",
    "Lime": "黄绿色",
    "Magenta": "品红色",
    "Orange": "橙色",
    "Pink": "粉色",
    "Purple": "紫色",
    "Red": "红色",
    "White": "白色",
    "Yellow": "黄色",

    # 通用方块词汇
    "Block": "方块",
    "Bricks": "砖块",
    "Brick": "砖",
    "Pillar": "柱",
    "Wall": "墙",
    "Stairs": "楼梯",
    "Slab": "台阶",
    "Tiles": "瓦",
    "Plating": "镀层",
    "Floor": "地板",
    "Support": "支架",
    "Girder": "桁架",
    "Chain": "链条",
    "Glass": "玻璃",
    "Pane": "玻璃板",
    "Container": "容器",

    # 机械
    "Cogwheel": "齿轮",
    "Large Cogwheel": "大齿轮",
    "Flywheel": "飞轮",
    "Millstone": "磨盘",
    "Crushing Wheel": "粉碎轮",
    "Mechanical Belt": "机械传送带",
    "Belt": "传送带",

    # 装饰
    "Display Board": "展示板",
    "Frontlight": "前灯",
    "Velvet Block": "天鹅绒方块",
    "Stone Metal": "石金属",
    "Dark Metal": "暗色金属",
    "Checker Tiles": "棋盘瓦",
    "Cross Bolt": "十字铆钉",
    "Dash Bolt": "横纹铆钉",
    "Dot Bolt": "圆点铆钉",
    "Flat Bolt": "平面铆钉",
    "Ornate Grate": "华丽格栅",
    "Layered": "层叠",
    "Cut": "切制",
    "Polished": "磨制",
    "Small": "小型",

    # Tooltip
    "The frontlight lights up": "前灯会亮起",
    "The frontlight gets rotated by 45 degrees": "前灯会旋转45度",
    "The frontlight's type gets changed": "前灯类型会改变",
    "When redstone signal is supplied from back": "从背后输入红石信号时",
    "When wrenched from the front": "从正面使用扳手时",
    "When wrenched from a side": "从侧面使用扳手时",
    "A highly customizable decoration block": "一种高度可自定义的装饰方块",

    # 特殊
    "Storage Container": "储物容器",
    "Design n' Decor": "Design n' Decor",
    "Mechanical Belt (Full)": "机械传送带（完整）",
}

# =========================
# 长词优先替换
# 防止 Large Cogwheel 被 Cogwheel 提前替换
# =========================

SORTED_WORDS = sorted(
    WORD_MAP.items(),
    key=lambda x: len(x[0]),
    reverse=True
)

# =========================
# 翻译函数
# =========================

def translate(text: str) -> str:
    result = text

    for en, zh in SORTED_WORDS:
        result = result.replace(en, zh)

    return result


# =========================
# 主逻辑
# =========================

def main():
    input_path = Path(INPUT_FILE)

    if not input_path.exists():
        print(f"找不到文件: {INPUT_FILE}")
        return

    with open(input_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    output = OrderedDict()

    for key, value in data.items():
        if KEEP_ENGLISH:
            output[key] = translate(value)
        else:
            translated = translate(value)

            # 如果完全没变化，说明词典没有覆盖
            if translated == value:
                output[key] = ""
            else:
                output[key] = translated

    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        json.dump(output, f, ensure_ascii=False, indent=2)

    print(f"生成完成: {OUTPUT_FILE}")
    print(f"共处理 {len(output)} 个键")


if __name__ == "__main__":
    main()
