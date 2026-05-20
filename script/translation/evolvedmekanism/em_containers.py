import json
import re
from pathlib import Path

# =========================================
# 自动汉化 evolvedmekanism 英文翻译
# 支持：
# - 矿石命名
# - 工厂/机器等级
# - 容器名
# - 描述文本
# - 护盾颜色
# =========================================

INPUT_FILE = "en_us.json"
OUTPUT_FILE = "zh_cn.json"

# =========================================
# 基础词典
# =========================================

WORD_MAP = {
    # 地层
    "Depthrock": "深板岩",
    "End Stone": "末地石",
    "Holystone": "圣石",
    "Netherrack": "下界岩",
    "Shiverstone": "寒栗石",

    # 矿物
    "Fluorite": "萤石",
    "Lead": "铅",
    "Osmium": "锇",
    "Tin": "锡",
    "Uranium": "铀",

    # 通用
    "Ore": "矿石",

    # 等级
    "Basic": "基础",
    "Advanced": "高级",
    "Elite": "精英",
    "Ultimate": "终极",
    "Overclocked": "超频",
    "Quantum": "量子",
    "Dense": "致密",
    "Multiversal": "寰宇",
    "Creative": "创造",

    # 机器
    "Alloyer": "合金机",
    "Chemixer": "化学混合机",
    "Thermalizer": "热熔机",
    "Solidification Chamber": "凝固室",

    "Factory": "工厂",
    "Chemical Tank": "化学品储罐",
    "Energy Cube": "能量立方",
    "Fluid Tank": "流体储罐",
    "Personal Barrel": "私人桶",
    "Personal Chest": "私人箱子",

    "Combining": "融合",
    "Compressing": "压缩",
    "Crushing": "粉碎",
    "Enriching": "富集",
    "Infusing": "灌注",
    "Injecting": "注入",
    "Purifying": "净化",
    "Sawing": "锯切",
    "Smelting": "冶炼",
    "Alloying": "合金",
    "Chemixing": "化学混合",

    "Solar Generator": "太阳能发电机",

    # 护盾
    "Shield": "盾牌",
    "Better Gold": "强化金",
    "Plaslitherite": "等离子合金",
    "Refined Redstone": "精炼红石",

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
}

# =========================================
# 描述文本替换
# =========================================

DESCRIPTION_REPLACEMENTS = [
    ("An advanced generator", "一种高级发电机"),
    ("An elite generator", "一种精英发电机"),
    ("An ultimate generator", "一种终极发电机"),
    ("An overclocked generator", "一种超频发电机"),
    ("A quantum generator", "一种量子发电机"),
    ("A dense generator", "一种致密发电机"),
    ("A multiversal generator", "一种寰宇发电机"),
    ("A creative generator", "一种创造发电机"),

    ("that directly absorbs the sun's rays with little loss to produce energy.",
     "可直接吸收太阳光线，并以极低损耗产生能量。"),
]

# =========================================
# 按长度排序避免短词污染
# =========================================

SORTED_WORDS = sorted(
    WORD_MAP.items(),
    key=lambda x: len(x[0]),
    reverse=True
)

# =========================================
# 智能翻译
# =========================================

def translate_text(text: str) -> str:
    result = text

    # 描述优先
    for en, zh in DESCRIPTION_REPLACEMENTS:
        result = result.replace(en, zh)

    # 通用替换
    for en, zh in SORTED_WORDS:
        result = re.sub(rf"\b{re.escape(en)}\b", zh, result)

    # 清理多余空格
    result = re.sub(r"\s+", " ", result).strip()

    return result


# =========================================
# 主程序
# =========================================

def main():
    input_path = Path(INPUT_FILE)
    output_path = Path(OUTPUT_FILE)

    if not input_path.exists():
        print(f"找不到文件: {INPUT_FILE}")
        return

    with open(input_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    result = {}

    for key, value in data.items():
        if isinstance(value, str):
            result[key] = translate_text(value)
        else:
            result[key] = value

    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)

    print(f"转换完成 -> {OUTPUT_FILE}")


if __name__ == "__main__":
    main()