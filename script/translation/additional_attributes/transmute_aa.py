import json


INPUT_FILE = "input.json"
OUTPUT_FILE = "output.json"


def convert_spell_level_names(data):
    output = {}

    suffix = "法术等级"

    for key, value in data.items():

        # 只处理 spell 属性
        if (
            key.startswith("attribute.additional_attributes.spell/")
            and not key.endswith(".desc")
            and isinstance(value, str)
            and value.endswith(suffix)
        ):
            spell_name = value[:-len(suffix)]

            output[key] = f"法术等级：{spell_name}"
        else:
            output[key] = value

    return output


def main():
    with open(INPUT_FILE, "r", encoding="utf-8") as f:
        data = json.load(f)

    result = convert_spell_level_names(data)

    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        json.dump(
            result,
            f,
            ensure_ascii=False,
            indent=2
        )

    print(f"转换完成，共处理 {len(result)} 个键值对")


if __name__ == "__main__":
    main()