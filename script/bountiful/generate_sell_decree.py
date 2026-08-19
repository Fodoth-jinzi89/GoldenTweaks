import argparse
import json
from pathlib import Path


def transform_file(path: Path, output_dir: Path):
    with path.open("r", encoding="utf-8") as f:
        data = json.load(f)

    stem = path.stem
    new_data = {
        "objectives": data.get("rewards", []),
        "rewards": [f"{stem}_sell"],
        "linkedProfessions": data.get("linkedProfessions", [])
    }
    new_path = output_dir / f"{stem}_sell.json"
    with new_path.open("w", encoding="utf-8") as f:
        json.dump(new_data, f, ensure_ascii=False, indent=4)
    print(f"generated: {path} -> {new_path}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--output-dir", type=Path)
    parser.add_argument("files", nargs="+", type=Path)
    args = parser.parse_args()

    for file in args.files:
        output_dir = args.output_dir or file.parent
        output_dir.mkdir(parents=True, exist_ok=True)
        transform_file(file, output_dir)


if __name__ == "__main__":
    main()
