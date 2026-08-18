import re
from pathlib import Path


ROOT = Path(__file__).parents[2]
LOG = ROOT / "aspect skip.log"
DATA = ROOT / "src/main/resources/data/goldentweaks"
PATTERN = re.compile(
    r"Skipped invalid Thaumcraft (?:item|entity) aspect recipe "
    r"'goldentweaks:(recipe/thaumcraft/(?:aspects|entity_aspects)/[^']+\.json)'\."
)


paths = list(dict.fromkeys(PATTERN.findall(LOG.read_text(encoding="utf-8-sig", errors="replace"))))
for relative in paths:
    target = (DATA / relative).resolve()
    target.relative_to(DATA.resolve())
    target.unlink()

print(f"Removed {len(paths)} skipped aspect entries.")
