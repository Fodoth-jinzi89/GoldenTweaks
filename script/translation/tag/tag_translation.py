import re
import json
from pathlib import Path


# =========================
# 配置
# =========================

# 输入日志文件
INPUT_FILE = "latest.log"

# 输出语言文件
OUTPUT_FILE = "zh_cn.json"


# =========================
# 提取 Untranslated tag
# =========================

pattern = re.compile(
    r"Untranslated tag #([a-zA-Z0-9_.-]+):([a-zA-Z0-9_./-]+)"
)


def tag_to_translation_key(namespace: str, tag: str) -> str:
    """
    Minecraft 标签翻译键：

    #minecraft:logs
    ->
    tag.minecraft.logs
    """

    return f"tag.{namespace}.{tag}"


def tag_to_default_text(tag: str) -> str:
    """
    将标签 ID 转换成一个适合作为中文翻译文件初始值的文本。

    例如：
        extended_charm_duration
        -> extended charm duration

        has_structure/end_laboratory
        -> has structure/end laboratory
    """

    return tag.replace("_", " ")


def main():
    input_path = Path(INPUT_FILE)
    output_path = Path(OUTPUT_FILE)

    if not input_path.exists():
        print(f"找不到输入文件：{input_path}")
        return

    text = input_path.read_text(encoding="utf-8", errors="ignore")

    translations = {}

    for match in pattern.finditer(text):
        namespace = match.group(1)
        tag = match.group(2)

        key = tag_to_translation_key(namespace, tag)
        value = tag_to_default_text(tag)

        translations[key] = value

    # 排序，方便后续维护
    translations = dict(sorted(translations.items()))

    with output_path.open("w", encoding="utf-8") as f:
        json.dump(
            translations,
            f,
            ensure_ascii=False,
            indent=2
        )

    print(f"提取到 {len(translations)} 个未翻译标签")
    print(f"已生成：{output_path}")


if __name__ == "__main__":
    main()