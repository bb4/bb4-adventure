#!/usr/bin/env python3
"""Generate a family of app icons for bb4-adventure (PNG + ICO + ICNS).

Visual language (same as the other bb4 installer repos):
- 1024 canvas, macOS-style rounded squircle
- Deep navy fill (#0D2438), 1px inner rim in muted teal
- Cream glyphs (#F4F1DE) with a shared accent palette (cyan / gold / coral)
- No wordmarks; one distinctive geometric motif per app

ICNS is written as PNG-encoded chunks so this runs on Linux CI as well as macOS.
"""

from __future__ import annotations

import io
import math
import os
import struct
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent
SIZE = 1024
NAVY = (13, 36, 56, 255)
TEAL_RIM = (58, 140, 150, 255)
CREAM = (244, 241, 222, 255)
CYAN = (78, 205, 196, 255)
GOLD = (232, 184, 74, 255)
CORAL = (224, 122, 95, 255)
SOFT = (90, 140, 170, 255)


def new_canvas() -> tuple[Image.Image, ImageDraw.ImageDraw]:
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    pad = 48
    radius = 220
    draw.rounded_rectangle([pad, pad, SIZE - pad, SIZE - pad], radius=radius, fill=NAVY)
    draw.rounded_rectangle(
        [pad + 18, pad + 18, SIZE - pad - 18, SIZE - pad - 18],
        radius=radius - 12,
        outline=TEAL_RIM,
        width=8,
    )
    return img, draw


def save_all(app_id: str, img: Image.Image) -> None:
    out = ROOT / app_id
    out.mkdir(parents=True, exist_ok=True)
    img.save(out / "icon.png", "PNG")

    ico_sizes = [(16, 16), (32, 32), (48, 48), (64, 64), (128, 128), (256, 256)]
    img.save(out / "icon.ico", sizes=ico_sizes)
    write_icns(img, out / "icon.icns")


def write_icns(img: Image.Image, dest: Path) -> None:
    """PNG-based icns (macOS 10.7+). Chunk sizes match iconutil's retina set."""
    entries = [
        ("ic11", 32),   # 16@2x
        ("ic12", 64),   # 32@2x
        ("ic07", 128),
        ("ic08", 256),
        ("ic13", 256),  # 128@2x
        ("ic09", 512),
        ("ic14", 512),  # 256@2x
        ("ic10", 1024), # 512@2x
    ]
    chunks = []
    for ostype, px in entries:
        resized = img.resize((px, px), Image.Resampling.LANCZOS)
        buf = io.BytesIO()
        resized.save(buf, format="PNG")
        data = buf.getvalue()
        chunks.append(ostype.encode("ascii") + struct.pack(">I", len(data) + 8) + data)
    body = b"".join(chunks)
    dest.write_bytes(b"icns" + struct.pack(">I", len(body) + 8) + body)


def try_font(size: int) -> ImageFont.ImageFont:
    for name in (
        "/System/Library/Fonts/Supplemental/Arial Bold.ttf",
        "/System/Library/Fonts/Helvetica.ttc",
        "/Library/Fonts/Arial.ttf",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
    ):
        if os.path.exists(name):
            try:
                return ImageFont.truetype(name, size=size)
            except OSError:
                continue
    return ImageFont.load_default()


def draw_adventure(draw: ImageDraw.ImageDraw) -> None:
    # House: Ludlow's haunted-house story.
    draw.polygon([(250, 470), (512, 250), (774, 470)], fill=CREAM)
    draw.rectangle([300, 470, 724, 780], fill=SOFT)
    draw.rectangle([450, 560, 574, 780], fill=CORAL)
    draw.ellipse([530, 650, 552, 672], fill=GOLD)
    draw.rectangle([340, 520, 420, 600], fill=CYAN)
    draw.rectangle([604, 520, 684, 600], fill=CYAN)
    draw.line([(512, 250), (250, 470), (774, 470), (512, 250)], fill=NAVY, width=10)


def draw_text_adventure(draw: ImageDraw.ImageDraw) -> None:
    # Open book.
    draw.polygon([(220, 340), (500, 300), (500, 760), (220, 720)], fill=CREAM)
    draw.polygon([(804, 340), (524, 300), (524, 760), (804, 720)], fill=CREAM)
    draw.line([(512, 300), (512, 760)], fill=NAVY, width=14)
    for y in (420, 480, 540, 600):
        draw.line([(270, y), (450, y - 12)], fill=SOFT, width=8)
        draw.line([(574, y - 12), (754, y)], fill=SOFT, width=8)
    draw.polygon([(470, 300), (512, 270), (554, 300), (512, 340)], fill=GOLD)


def draw_binary_tutorial(draw: ImageDraw.ImageDraw) -> None:
    font = try_font(280)
    for label, color, x in (("0", CYAN, 280), ("1", GOLD, 620)):
        bbox = draw.textbbox((0, 0), label, font=font)
        tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
        draw.text((x - tw / 2, 512 - th / 2 - 20), label, fill=color, font=font)
    draw.rounded_rectangle([260, 740, 764, 788], radius=16, fill=CORAL)


def draw_text_binary(draw: ImageDraw.ImageDraw) -> None:
    # Terminal with a binary prompt.
    draw.rounded_rectangle([230, 280, 794, 760], radius=36, fill=CREAM)
    draw.rounded_rectangle([230, 280, 794, 360], radius=36, fill=SOFT)
    draw.rectangle([230, 330, 794, 360], fill=SOFT)
    draw.ellipse([270, 306, 304, 340], fill=CORAL)
    draw.ellipse([324, 306, 358, 340], fill=GOLD)
    draw.ellipse([378, 306, 412, 340], fill=CYAN)
    font = try_font(72)
    draw.text((280, 430), "10110", fill=NAVY, font=font)
    draw.text((280, 540), "01101", fill=CYAN, font=font)
    draw.rectangle([280, 660, 360, 708], fill=GOLD)


def draw_aikido_editor(draw: ImageDraw.ImageDraw) -> None:
    # Open circle (enso) with a sweeping throw arc.
    box = [250, 250, 774, 774]
    draw.arc(box, start=40, end=340, fill=CREAM, width=36)
    draw.arc([340, 360, 700, 700], start=200, end=20, fill=GOLD, width=22)
    draw.ellipse([470, 470, 554, 554], fill=CORAL)
    # small motion ticks
    for angle in (120, 150, 180):
        rad = math.radians(angle)
        x = 512 + 210 * math.cos(rad)
        y = 512 + 210 * math.sin(rad)
        draw.ellipse([x - 14, y - 14, x + 14, y + 14], fill=CYAN)


DRAWERS = {
    "adventure": draw_adventure,
    "textAdventure": draw_text_adventure,
    "binaryTutorial": draw_binary_tutorial,
    "textBinaryTutorial": draw_text_binary,
    "aikidoEditor": draw_aikido_editor,
}


def main() -> None:
    os.chdir(ROOT)
    for app_id, drawer in DRAWERS.items():
        img, draw = new_canvas()
        drawer(draw)
        save_all(app_id, img)
        print(f"wrote {app_id}")


if __name__ == "__main__":
    main()
