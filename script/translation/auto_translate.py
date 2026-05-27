from pathlib import Path
import json
import time

from deep_translator import GoogleTranslator

# ========= 配置 =========

SOURCE_DIR = Path(r"./source")   # 原文件夹
OUTPUT_DIR = Path(r"./translated")  # 输出文件夹

# 要翻译的字段
TRANSLATE_KEYS = {
    "name",
    "description",
    "text",
    "title",
    "landing_text"
}

translator = GoogleTranslator(source="en", target="zh-CN")


# ========= 翻译逻辑 =========

def translate_text(text: str) -> str:
    if not text.strip():
        return text

    try:
        result = translator.translate(text)
        time.sleep(0.2)  # 防止请求太快
        return result
    except Exception as e:
        print(f"翻译失败: {text}")
        print(e)
        return text


def process_obj(obj):
    if isinstance(obj, dict):
        new_obj = {}

        for key, value in obj.items():

            # 翻译指定字段
            if key in TRANSLATE_KEYS and isinstance(value, str):
                print(f"翻译 [{key}] -> {value}")

                new_obj[key] = translate_text(value)

            else:
                new_obj[key] = process_obj(value)

        return new_obj

    elif isinstance(obj, list):
        return [process_obj(v) for v in obj]

    else:
        return obj


# ========= 扫描文件 =========

json_files = list(SOURCE_DIR.rglob("*.json"))

print(f"发现 {len(json_files)} 个 json 文件")

for file in json_files:

    relative = file.relative_to(SOURCE_DIR)
    output_file = OUTPUT_DIR / relative

    output_file.parent.mkdir(parents=True, exist_ok=True)

    try:
        with open(file, "r", encoding="utf-8") as f:
            data = json.load(f)

        translated = process_obj(data)

        with open(output_file, "w", encoding="utf-8") as f:
            json.dump(
                translated,
                f,
                ensure_ascii=False,
                indent=2
            )

        print(f"完成: {relative}")

    except Exception as e:
        print(f"处理失败: {file}")
        print(e)

print("全部完成")