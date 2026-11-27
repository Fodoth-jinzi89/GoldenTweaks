"""用系统安装的 Chrome headless 把 data_sheets 里的 HTML 截成 PNG。

不依赖 playwright 的浏览器下载：本机 Chrome 的 --headless --screenshot 就够用。

截图走 ?shot 模式（页面自己去掉背景与投影），并用透明底色渲染后再按非透明像素
裁边，所以既不需要手量窗口尺寸，也不会在四周留白。

用法：
    python script/docs/shot_html.py "奇点迭代_UU物质生成机.html"
    python script/docs/shot_html.py "a.html" "b.html"
不传参数时用 DEFAULT_JOBS。
"""

import subprocess
import sys
import tempfile
from pathlib import Path

from PIL import Image

CHROME = r'C:\Program Files\Google\Chrome\Application\chrome.exe'
SHEETS = Path(__file__).resolve().parent.parent.parent / 'data_sheets'

DEFAULT_JOBS = ['奇点迭代_UU物质生成机.html']

# 足够高的视口，让整页内容都排得下；多余部分靠裁边去掉。
VIEWPORT = (1400, 2600)
SCALE = 2


def render(html: Path, out: Path) -> None:
    with tempfile.TemporaryDirectory() as tmp:
        subprocess.run([
            CHROME,
            '--headless', '--disable-gpu', '--no-sandbox', '--hide-scrollbars',
            f'--force-device-scale-factor={SCALE}',
            '--default-background-color=00000000',
            f'--window-size={VIEWPORT[0]},{VIEWPORT[1]}',
            f'--user-data-dir={tmp}/prof',
            f'--screenshot={out}',
            '--virtual-time-budget=2000',
            f'{html.as_uri()}?shot',
        ], capture_output=True, timeout=180)


def trim(source: Path, target: Path) -> None:
    """按非透明像素裁边，去掉视口剩余的空间。"""
    image = Image.open(source).convert('RGBA')
    box = image.getbbox()
    if box is None:
        raise SystemExit(f'渲染结果全透明，检查页面是否加载成功：{source.name}')
    image.crop(box).save(target)


def shoot(name: str) -> None:
    html = SHEETS / name
    out = html.with_suffix('.png')
    with tempfile.TemporaryDirectory() as tmp:
        raw = Path(tmp) / 'raw.png'
        render(html, raw)
        trim(raw, out)
    print(f'{out.name}: {out.stat().st_size:,} bytes')


def main() -> None:
    for name in (sys.argv[1:] or DEFAULT_JOBS):
        shoot(name)


if __name__ == '__main__':
    main()
