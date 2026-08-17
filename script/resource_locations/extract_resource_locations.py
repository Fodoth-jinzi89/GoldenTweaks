import argparse
import json
import re
import zipfile
from collections import defaultdict
from pathlib import Path


DEFAULT_MODS_DIR = Path(r"E:\机械动力魔法大冒险\.minecraft\versions\hkx\mods")
DEFAULT_OUTPUT_DIR = Path(__file__).parent / "output"
LANG_PATH = re.compile(r"^assets/[^/]+/lang/en_us\.json$", re.IGNORECASE)
TRANSLATION_KEY = re.compile(r"^(item|block|entity)\.([a-z0-9_-]+)\.(.+)$")
CATEGORY_NAMES = {"item": "items", "block": "blocks", "entity": "entities"}


def extract(mods_dir: Path, output_dir: Path) -> None:
    results = defaultdict(lambda: {name: {} for name in CATEGORY_NAMES.values()})
    warnings = []
    lang_file_count = 0

    for jar_path in sorted(mods_dir.glob("*.jar"), key=lambda path: path.name.lower()):
        try:
            with zipfile.ZipFile(jar_path) as jar:
                for member in jar.namelist():
                    if not LANG_PATH.match(member):
                        continue
                    lang_file_count += 1
                    try:
                        translations = json.loads(jar.read(member).decode("utf-8-sig"))
                    except (UnicodeDecodeError, json.JSONDecodeError) as error:
                        warnings.append(f"{jar_path.name}!/{member}: {error}")
                        continue

                    for key, value in translations.items():
                        match = TRANSLATION_KEY.match(key)
                        if not match or not isinstance(value, str):
                            continue
                        kind, namespace, path = match.groups()
                        results[namespace][CATEGORY_NAMES[kind]][f"{namespace}:{path}"] = value
        except (OSError, zipfile.BadZipFile) as error:
            warnings.append(f"{jar_path.name}: {error}")

    output_dir.mkdir(parents=True, exist_ok=True)
    for old_file in output_dir.glob("*.json"):
        old_file.unlink()

    entry_count = 0
    for namespace, categories in sorted(results.items()):
        sorted_categories = {
            category: dict(sorted(entries.items()))
            for category, entries in categories.items()
        }
        entry_count += sum(len(entries) for entries in categories.values())
        (output_dir / f"{namespace}.json").write_text(
            json.dumps(sorted_categories, ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8",
        )

    print(
        f"扫描 {len(list(mods_dir.glob('*.jar')))} 个 JAR、{lang_file_count} 个 en_us.json；"
        f"输出 {len(results)} 个模组、{entry_count} 条记录到 {output_dir}"
    )
    for warning in warnings:
        print(f"警告: {warning}")


def main() -> None:
    parser = argparse.ArgumentParser(description="从模组 en_us.json 提取物品、方块和实体资源位置。")
    parser.add_argument("mods_dir", nargs="?", type=Path, default=DEFAULT_MODS_DIR)
    parser.add_argument("-o", "--output", type=Path, default=DEFAULT_OUTPUT_DIR)
    args = parser.parse_args()

    if not args.mods_dir.is_dir():
        parser.error(f"模组目录不存在: {args.mods_dir}")
    extract(args.mods_dir, args.output)


if __name__ == "__main__":
    main()
