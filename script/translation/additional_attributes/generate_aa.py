import json
import re


INPUT_FILE = "input.json"
OUTPUT_FILE = "output.json"


SPELL_DESC = "赋予此法术（等级基于属性值）"
SPELL_LEVEL_DESC = "修改此法术的等级"
SCHOOL_DESC = "赋予此学派的所有法术（等级基于属性值）"


# 不应该作为法术属性生成的末级字段
IGNORE_SPELL_SUFFIXES = {
    "guide",
    "description",
    "lore",
    "tooltip",
    "warning",
    "miss_warning"
}


def convert_keys(input_data):
    output_data = {}

    for key, value in input_data.items():

        # =========================
        # spell.xxx.xxx
        # =========================
        spell_match = re.match(r"^spell\.(.+)$", key)

        if spell_match:
            parts = spell_match.group(1).split(".")

            # 跳过 spell.xxx.guide 等说明字段
            if parts[-1] in IGNORE_SPELL_SUFFIXES:
                continue

            path = "/".join(parts)

            # innate_spell
            innate_key = (
                f"attribute.additional_attributes.innate_spell/{path}"
            )

            output_data[innate_key] = f"精通法术：{value}"
            output_data[f"{innate_key}.desc"] = SPELL_DESC

            # spell等级
            spell_key = (
                f"attribute.additional_attributes.spell/{path}"
            )

            output_data[spell_key] = f"{value}法术等级"
            output_data[f"{spell_key}.desc"] = SPELL_LEVEL_DESC

            continue


        # =========================
        # school.xxx.xxx
        # =========================
        school_match = re.match(r"^school\.(.+)$", key)

        if school_match:
            parts = school_match.group(1).split(".")

            # 同样跳过说明字段
            if parts[-1] in IGNORE_SPELL_SUFFIXES:
                continue

            path = "/".join(parts)

            school_key = (
                f"attribute.additional_attributes.innate_school/{path}"
            )

            output_data[school_key] = f"精通学派：{value}"
            output_data[f"{school_key}.desc"] = SCHOOL_DESC

            continue

    return output_data


def main():
    with open(INPUT_FILE, "r", encoding="utf-8") as f:
        input_json = json.load(f)

    output_json = convert_keys(input_json)

    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        json.dump(
            output_json,
            f,
            ensure_ascii=False,
            indent=2
        )

    print(
        f"转换完成，共生成 {len(output_json)} 个键值对，"
        f"已保存到 {OUTPUT_FILE}"
    )


if __name__ == "__main__":
    main()