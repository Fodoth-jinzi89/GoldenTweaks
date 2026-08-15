# -*- coding: utf-8 -*-
"""
更新 要素列表.txt —— 基于当前注册的全部 Thaumcraft 要素（原版 + 自定义 JSON + forbiddenmagic）。

- 原版要素配方表：来自 thaumcraft-0.2.2.95-port jar 的 Aspect 静态初始化（primal/compound），
  已固化在此脚本中（None 表示原始要素）。
- 自定义要素：data/goldentweaks/thaumcraft/aspects/*.json
- forbiddenmagic 要素：forbiddenmagic-1.0.0-port jar 的 FMDarkAspects（七宗罪/下界系），已固化。
- 中文名：thaumcraft / goldentweaks / forbiddenmagic 三个 zh_cn.json
- 阶数 = 合成深度（原始要素=1阶，每多一层合成 +1），以脚本计算为准。
- 物品来源：沿用旧列表里已有的（按作者用词映射到 tag）；新要素暂不填物品。
- 格式参照：
    风（Aer）原始要素 甘蔗
    余烬（Favilla）= 火（Ignis）+地（Terra） 营火

用法：python update_aspect_list.py
"""
import io
import json
import glob
import re
import os

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SRC = os.path.join(ROOT, 'src', 'main', 'resources')
OUT = os.path.join(ROOT, '要素列表.txt')

# ---- 原版要素配方（tag -> [子要素] 或 None=原始） ----
VANILLA = {
    'aer': None, 'terra': None, 'ignis': None, 'aqua': None, 'ordo': None, 'perditio': None,
    'vacuos': ['aer', 'perditio'], 'lux': ['aer', 'ignis'], 'tempestas': ['aer', 'aqua'],
    'motus': ['aer', 'ordo'], 'gelum': ['ignis', 'perditio'], 'vitreus': ['terra', 'ordo'],
    'victus': ['aqua', 'terra'], 'venenum': ['aqua', 'perditio'], 'potentia': ['ordo', 'ignis'],
    'permutatio': ['perditio', 'ordo'], 'metallum': ['terra', 'vitreus'], 'mortuus': ['victus', 'perditio'],
    'volatus': ['aer', 'motus'], 'tenebrae': ['vacuos', 'lux'], 'spiritus': ['victus', 'mortuus'],
    'sano': ['victus', 'ordo'], 'iter': ['motus', 'terra'], 'alienis': ['vacuos', 'tenebrae'],
    'praecantatio': ['vacuos', 'potentia'], 'auram': ['praecantatio', 'aer'], 'vitium': ['praecantatio', 'perditio'],
    'limus': ['victus', 'aqua'], 'herba': ['victus', 'terra'], 'arbor': ['aer', 'herba'],
    'bestia': ['motus', 'victus'], 'corpus': ['mortuus', 'bestia'], 'exanimis': ['motus', 'mortuus'],
    'cognitio': ['ignis', 'spiritus'], 'sensus': ['aer', 'spiritus'], 'humanus': ['bestia', 'cognitio'],
    'messis': ['herba', 'humanus'], 'perfodio': ['humanus', 'terra'], 'instrumentum': ['humanus', 'ordo'],
    'meto': ['messis', 'instrumentum'], 'telum': ['instrumentum', 'ignis'], 'tutamen': ['instrumentum', 'terra'],
    'fames': ['victus', 'vacuos'], 'lucrum': ['humanus', 'fames'], 'fabrico': ['humanus', 'instrumentum'],
    'pannus': ['instrumentum', 'bestia'], 'machina': ['motus', 'instrumentum'], 'vinculum': ['motus', 'perditio'],
    'sonus': ['aer', 'sensus'], 'imperium': ['humanus', 'ordo'], 'profundum': ['tenebrae', 'vacuos'],
    'aestus': ['aqua', 'motus'], 'fungus': ['herba', 'tenebrae'], 'adhaesio': ['limus', 'vinculum'],
    'fulmen': ['potentia', 'aer'], 'tempus': ['ordo', 'perditio'], 'reliquiae': ['cognitio', 'terra'],
    'vas': ['vacuos', 'fabrico'], 'gravitas': ['terra', 'motus'], 'magnetis': ['metallum', 'motus'],
    'ardor': ['ignis', 'potentia'], 'favilla': ['ignis', 'terra'], 'textus': ['pannus', 'fabrico'],
    'orbita': ['iter', 'machina'], 'illecebra': ['sensus', 'vinculum'],
}

# 旧列表作者用词 -> tag（lang 名称可能与作者用词不一致）
LEGACY_NAME_TO_TAG = {
    '混沌': 'perditio', '史莱姆': 'limus', '亡灵': 'exanimis', '粘附': 'adhaesio',
}

# ---- forbiddenmagic 要素（tag -> [子要素]）----
FORBIDDEN_MAGIC = {
    'luxuria': ['corpus', 'fames'],      # 欲望 = 肉体 + 饥饿
    'infernus': ['ignis', 'praecantatio'],  # 下界 = 火 + 魔力
    'superbia': ['volatus', 'vacuos'],   # 傲慢 = 飞行 + 虚空
    'gula': ['fames', 'vacuos'],         # 饕餮 = 饥饿 + 虚空
    'invidia': ['sensus', 'fames'],      # 妒忌 = 感官 + 饥饿
    'desidia': ['vinculum', 'spiritus'], # 怠惰 = 陷阱 + 灵魂
    'ira': ['telum', 'ignis'],           # 暴怒 = 武器 + 火
}

# ---- 旧列表物品来源（作者用词 -> 物品列表，按旧 要素列表.txt 固化）----
LEGACY_ITEMS = {
    '风': ['甘蔗'], '水': ['甘蔗'], '地': ['圆石'], '混沌': ['圆石'], '火': ['煤炭'], '秩序': ['木齿轮'],
    '余烬': ['营火'], '寒冰': ['雪球'], '光明': ['火把'], '移动': ['橡木门'], '贸易': ['水银花'],
    '能量': ['煤炭'], '气候': ['旋风棒'], '时间': ['钟', '氧化的铜块'], '虚空': ['碗'],
    '毒药': ['毒马铃薯', '蜘蛛眼'], '生命': ['花'], '水晶': ['玻璃'],
    '灼热': ['干海带'], '炽烈': ['烈焰粉'], '野兽': ['线'], '饥饿': ['西瓜片'], '雷霆': ['铜粉'],
    '重力': ['硅'], '植物': ['甘蔗'], '旅行': ['末影珍珠'], '史莱姆': ['生物质', '粘液球', '鸡蛋'],
    '金属': ['铁锭'], '死亡': ['骨头'], '魔力': ['下界疣'], '治疗': ['柠檬'],
    '黑暗': ['蘑菇', '黑曜石', '末地石'], '陷阱': ['灵魂沙'], '飞行': ['羽毛'],
    '水域': [], '粘附': ['生物质', '蜜啤'], '异域': ['末影珍珠'], '树木': ['原木'],
    '灵气': ['世界盐', '蕴魔种子'], '肉体': ['肉类'], '亡灵': ['僵尸之脑'], '真菌': ['绯红菌', '诡异菌'],
    '饕餮': ['曲奇'], '下界': ['下界疣'], '磁力': ['铜粉'], '深渊': ['回响碎片'], '灵魂': ['灵魂沙'],
    '傲慢': ['金护甲', '下界之星'], '污染': ['腐化果实'],
    '思维': ['书'], '感官': ['染料'], '人类': ['腐肉', '肉块'], '诱惑': ['甜浆果', '发光浆果'],
    '妒忌': ['末影之眼'], '遗物': ['刷子'], '声响': ['回响碎片'], '统御': ['旗帜图案'],
    '工具': ['燧石'], '贪婪': ['金锭'], '作物': ['小麦'], '矿藏': ['镐'],
    '合成': ['工作台'], '机械': ['活板门'], '收获': ['锄'], '布匹': ['线'], '武器': ['箭'],
    '装备': ['皮革'], '终结': ['水晶矩阵锭'],
    '暴怒': ['火焰弹'], '轨迹': ['铁轨'], '织构': ['地毯'], '容器': ['箱子'], '飞升': ['无尽催化剂'],
    '完美': ['超级锭'],
}

def load_json(path):
    try:
        return json.load(io.open(path, encoding='utf-8'))
    except Exception:
        return {}

def main():
    aspects = dict(VANILLA)

    # 自定义要素
    custom_dir = os.path.join(SRC, 'data', 'goldentweaks', 'thaumcraft', 'aspects')
    for f in glob.glob(os.path.join(custom_dir, '*.json')):
        d = load_json(f)
        tag = d.get('tag')
        if tag:
            aspects[tag] = d.get('components') or None

    # forbiddenmagic 要素
    aspects.update(FORBIDDEN_MAGIC)

    # 中文名（thaumcraft / goldentweaks / forbiddenmagic，后者可能带 BOM）
    names = {}
    for langfile in [
        os.path.join(SRC, 'assets', 'thaumcraft', 'lang', 'zh_cn.json'),
        os.path.join(SRC, 'assets', 'goldentweaks', 'lang', 'zh_cn.json'),
        os.path.join(SRC, 'assets', 'forbiddenmagic', 'lang', 'zh_cn.json'),
    ]:
        try:
            d = json.load(io.open(langfile, encoding='utf-8-sig'))
        except Exception:
            d = load_json(langfile)
        for k, v in d.items():
            if k.startswith('tc.aspect.') and not k.startswith('tc.aspect.help'):
                names[k.replace('tc.aspect.', '')] = v

    # 旧列表物品（固化映射）-> tag 物品集
    legacy_items = {}
    for author_name, items in LEGACY_ITEMS.items():
        tag = LEGACY_NAME_TO_TAG.get(author_name) or next(
            (t for t, n in names.items() if n == author_name), None)
        if tag is None or not items:
            continue
        legacy_items.setdefault(tag, set()).update(items)

    # 旧列表出现顺序（只用于同阶内排序）
    legacy_order = []
    if os.path.exists(OUT):
        for line in io.open(OUT, encoding='utf-8').read().split('\n'):
            line = line.strip()
            if not line or line.endswith('阶') or line == '要素来源':
                continue
            m = re.match(r'([\u4e00-\u9fff]+)', line)
            if not m:
                continue
            author_name = m.group(1)
            tag = LEGACY_NAME_TO_TAG.get(author_name) or next(
                (t for t, n in names.items() if n == author_name), None)
            if tag is not None and tag not in legacy_order:
                legacy_order.append(tag)

    def display(tag):
        return '%s（%s）' % (names.get(tag, tag), tag[0].upper() + tag[1:])

    # 合成深度
    depth = {}

    def compute(tag, visiting=None):
        if tag in depth:
            return depth[tag]
        if visiting is None:
            visiting = set()
        if tag in visiting:
            return 1
        comps = aspects.get(tag)
        if not comps:
            depth[tag] = 1
            return 1
        d = max(compute(c, visiting | {tag}) for c in comps) + 1
        depth[tag] = d
        return d

    for tag in aspects:
        compute(tag)

    lines = ['要素来源']
    max_tier = max(depth.values())
    for tier in range(1, max_tier + 1):
        tier_tags = [t for t in aspects if depth[t] == tier]
        # 旧顺序优先，其余按 tag 排
        tier_tags.sort(key=lambda t: (legacy_order.index(t) if t in legacy_order else len(legacy_order), t))
        lines.append('%d阶' % tier)
        for tag in tier_tags:
            comps = aspects[tag]
            line = '%s原始要素' % display(tag) if not comps \
                else '%s= %s' % (display(tag), '+'.join(display(c) for c in comps))
            # 物品来源：沿用旧列表里已有的，新要素暂不填
            items = legacy_items.get(tag)
            if items:
                line += ' ' + '、'.join(sorted(items))
            lines.append(line)
        lines.append('')

    io.open(OUT, 'w', encoding='utf-8').write('\n'.join(lines).rstrip() + '\n')
    print('written:', OUT)
    print('total aspects:', len(aspects))

if __name__ == '__main__':
    main()
