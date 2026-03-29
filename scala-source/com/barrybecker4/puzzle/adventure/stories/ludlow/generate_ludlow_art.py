#!/usr/bin/env python3
"""
Generate illustrated JPGs for Ludlow adventure scenes from XML descriptions.
Storybook-style: bright readable colors, clear shapes, story-related content per theme.
"""
from __future__ import annotations

import math
import random
import xml.etree.ElementTree as ET
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent
XML_PATH = ROOT / "ludlowScript.xml"
IMG_DIR = ROOT / "images"

W, H = 640, 480


def lerp(a: float, b: float, t: float) -> float:
    return a + (b - a) * t


def gradient_bg(draw: ImageDraw.ImageDraw, top: tuple[int, int, int], bot: tuple[int, int, int]) -> None:
    for y in range(H):
        t = y / max(H - 1, 1)
        r = int(lerp(top[0], bot[0], t))
        g = int(lerp(top[1], bot[1], t))
        b = int(lerp(top[2], bot[2], t))
        draw.line([(0, y), (W, y)], fill=(r, g, b))


def floor_plane(draw: ImageDraw.ImageDraw, wall_rgb: tuple[int, int, int], floor_rgb: tuple[int, int, int]) -> None:
    """Light trapezoid floor + back wall band."""
    horizon = int(H * 0.42)
    draw.polygon([(0, H), (W, H), (int(W * 0.88), horizon), (int(W * 0.12), horizon)], fill=floor_rgb)
    draw.rectangle([0, 0, W, horizon + 8], fill=wall_rgb)


def outline_rect(draw: ImageDraw.ImageDraw, xy: list[int], outline: tuple[int, int, int], fill: tuple[int, int, int], w: int = 2) -> None:
    draw.rectangle(xy, fill=fill, outline=outline, width=w)


def round_rect(draw: ImageDraw.ImageDraw, xy: list[int], r: int, fill: tuple[int, int, int], outline: tuple[int, int, int], w: int = 2) -> None:
    if hasattr(draw, "rounded_rectangle"):
        draw.rounded_rectangle(xy, r, fill=fill, outline=outline, width=w)
    else:
        draw.rectangle(xy, fill=fill, outline=outline, width=w)


# --- theme_from_scene unchanged from prior version (abbreviated for size) ---
def theme_from_scene(name: str, desc: str) -> str:
    n = name.lower()
    d = desc.lower()
    explicit: dict[str, str] = {
        "start": "start",
        "no_response": "no_response",
        "sky_falls": "sky_falls",
        "quit": "quit",
        "open_crates": "crate",
        "a.mirror": "mirror_only",
        "a.inside_entrance": "entrance",
        "mirrorhall": "mirror_hall",
        "goldhall": "gold",
        "silverhall": "silver",
        "aa.pool": "pool",
        "bb.copper": "copper_room",
        "cc.corridor": "corridor",
        "dd.blackspiral": "spiral",
        "ee.sheetroom": "foam",
        "j.bathroom": "bathroom",
        "t.pinkbathroom": "pink_bath",
        "p.plant_room": "plant",
        "p.inspect_plants": "plant",
        "q.skeleton_room": "skeleton",
        "vii.chunksroom": "chunks",
        "storyfinale": "finale",
        "k.firerifle": "trophy",
        "k.trophyroom": "trophy",
        "floor3.x": "hellhound",
        "floor3.xi": "ball",
        "floor3.xii": "microscope",
        "floor3.xiii": "purple_glow",
        "floor3.xiv": "robot",
        "floor3.xv": "red_plastic",
        "floor2.i": "spider",
        "floor2.ii": "chemical",
        "floor2.iii": "devil",
        "floor2.iv": "disco",
        "floor2.v": "rug",
        "floor2.vi": "magnet",
        "floor2.viii": "landing",
        "floor2.ix": "electronics",
        "floor2.x": "marble_bed",
        "floor2.xi": "shooting",
        "floor2.xii": "erinyes",
        "floor2.xiii": "purple_heal",
        "floor2.xiv": "cage",
        "floor2.xv": "chemical",
        "floor2.xvi": "shower",
        "floor2.xvii": "pipe",
        "floor2.xxi": "torture",
        "floor2.xxii": "theater",
        "floor2.xxiii": "laser",
        "j.takegreenpill": "pills_green",
        "j.takepinkpill": "pills_pink",
        "j.turnonwater": "bathroom",
        "j.medicine_cabinet": "medicine",
        "b.rat_attack": "rat",
        "b.key": "golden_key",
        "e.crazy_man_attacks": "old_man",
        "i.talk_to_statues": "statue",
        "i.attack_statues": "statue",
        "h.inspect_wall": "fur",
        "f.rocks": "rocks",
        "f.stairway": "stairs",
        "x.spiralstaircase": "spiral",
        "c.pink_hallway": "pink_hall",
        "g.passageway": "weapon_hall",
        "d.crate_room_leaves": "snake",
        "e.gray_storage_room": "storage",
    }
    if n in explicit:
        return explicit[n]
    rules = [
        ("pool", "pool" in d or "aa.pool" in n),
        ("gold", "gold" in n),
        ("silver", "silver" in n and "hall" in n),
        ("mirror_hall", "mirrorhall" == n or "lined with mirrors" in d),
        ("mirror_only", "secret door" in d and "mirror" in d),
        ("plant", "plant" in d or "stirge" in d),
        ("skeleton", "skeleton" in d),
        ("bathroom", "bathroom" in d and "pink" not in d),
        ("pink_bath", "pink" in d and "fiberglass" in d),
        ("kitchen", "kitchen" in d or "stove" in d),
        ("armor", "armor" in d),
        ("weapon_hall", "passageway" in n or "mace" in d),
        ("fur", "fur" in d and "purple" in d),
        ("spiral", "spiral" in d or "spiral" in n),
        ("rocks", "rock" in d or "mineral" in d or "sample" in d),
        ("stairs", "stair" in d),
        ("statue", "statue" in d),
        ("trophy", "trophy" in d or "rifle" in d),
        ("rat", "rat" in d or ("couch" in d and "barn" in d)),
        ("snake", "snake" in d or "rattlesnake" in d),
        ("oak", "oak" in d and "rock" in d),
        ("chunks", "chunk" in d),
        ("spider", "spider" in d),
        ("devil", "devil" in d or "barbed" in d),
        ("computer", "computer" in d),
        ("foam", "foam" in d),
        ("fungus", "fungus" in d),
        ("marble_bed", "marble" in d and "bed" in d),
        ("torture", "torture" in d),
        ("theater", "projector" in d),
        ("laser", "laser" in d),
        ("robot", "robot" in d),
        ("cage", "cage" in d or "hill giant" in d),
        ("shower", "shower" in d),
        ("magnet", "magnet" in d),
        ("rug", "persian" in d or "rug" in d),
        ("disco", "dazzling" in d),
        ("electronics", "transistor" in d or "electronics" in d),
        ("microscope", "microscope" in d),
        ("purple_glow", "ultraviolet" in d or ("purple" in d and "glow" in d)),
        ("red_plastic", "red plastic" in d or "stick" in d),
        ("finale", "recording" in d or "finale" in n),
        ("crate", "crate" in d),
        ("storage", "barrel" in d),
        ("entrance", "entrance" in n),
        ("pink_hall", "pink" in d and "hall" in d),
        ("shooting", "shooting" in d or "air rifle" in d),
        ("erinyes", "winged woman" in d or "erinyes" in d),
        ("purple_heal", "heal" in d and "purple" in d),
        ("pipe", "pipe" in d and "thread" in d),
        ("closet", "closet" in d or "coats" in d),
        ("default", True),
    ]
    for key, cond in rules:
        if cond:
            return key
    return "default"


def draw_theme(
    draw: ImageDraw.ImageDraw,
    theme: str,
    name: str,
    desc: str,
) -> None:
    """Draw story-related foreground content in bright colors (RGB)."""
    cx, cy = W // 2, int(H * 0.38)
    O = (40, 35, 30)
    rnd = random.Random(hash(name) % (2**32))

    def mansion_exterior() -> None:
        # Night meadow, big spooky mansion, moon, pines
        gradient_bg(draw, (70, 85, 130), (25, 30, 55))
        for i in range(8):
            x = 40 + i * 75
            draw.polygon([(x, 340), (x + 25, 220), (x + 50, 340)], fill=(35, 55, 35), outline=O, width=2)
        draw.rectangle([180, 200, 460, 360], fill=(85, 75, 95), outline=O, width=3)
        for wx in range(200, 440, 55):
            draw.rectangle([wx, 230, wx + 35, 270], fill=(200, 220, 255), outline=O, width=1)
        draw.polygon([(300, 200), (340, 140), (380, 200)], fill=(110, 90, 110), outline=O, width=2)
        draw.ellipse([500, 60, 580, 140], fill=(240, 240, 210), outline=(200, 200, 180), width=2)

    def indoor_hall(red_carpet: bool) -> None:
        gradient_bg(draw, (200, 185, 165), (155, 138, 118))
        floor_plane(draw, (165, 140, 115), (120, 85, 65))
        outline_rect(draw, [40, 40, W - 40, H - 30], O, (190, 165, 130), 3)
        if red_carpet:
            draw.polygon([(cx - 50, 360), (cx + 50, 360), (cx + 40, H - 25), (cx - 40, H - 25)], fill=(160, 40, 45), outline=O, width=2)
        # mirror at far wall
        outline_rect(draw, [cx - 90, 80, cx + 90, 320], O, (210, 215, 225), 2)
        draw.line([(cx, 85), (cx, 315)], fill=(255, 255, 255), width=2)

    if theme == "start":
        mansion_exterior()

    elif theme == "no_response":
        gradient_bg(draw, (90, 90, 100), (50, 52, 58))
        outline_rect(draw, [140, 120, 500, 360], O, (120, 118, 125), 3)
        draw.arc([260, 200, 380, 320], 0, 180, fill=(80, 80, 85), width=6)

    elif theme == "sky_falls":
        gradient_bg(draw, (180, 100, 80), (60, 40, 90))
        for i in range(25):
            x = rnd.randint(0, W)
            draw.line([(x, 0), (x + rnd.randint(-30, 30), H)], fill=(220, 140, 90), width=4)

    elif theme == "quit":
        gradient_bg(draw, (130, 170, 210), (90, 130, 100))
        draw.ellipse([180, 70, 460, 200], fill=(255, 240, 150), outline=(220, 180, 80), width=2)
        draw.polygon([(0, 380), (W, 380), (W, H), (0, H)], fill=(70, 110, 70), outline=O, width=2)

    elif theme == "entrance":
        indoor_hall(True)

    elif theme == "mirror" or theme == "mirror_only":
        floor_plane(draw, (175, 165, 155), (140, 125, 115))
        outline_rect(draw, [80, 60, W - 80, 380], O, (230, 232, 238), 2)
        outline_rect(draw, [cx - 100, 70, cx + 100, 340], O, (200, 210, 225), 3)

    elif theme == "mirror_hall":
        gradient_bg(draw, (200, 205, 215), (160, 165, 175))
        floor_plane(draw, (185, 180, 175), (150, 140, 130))
        for i in range(5):
            x = 30 + i * 115
            outline_rect(draw, [x, 50, x + 100, 360], O, (220, 225, 235), 2)

    elif theme == "gold":
        gradient_bg(draw, (220, 190, 100), (180, 150, 70))
        floor_plane(draw, (200, 170, 90), (160, 130, 60))
        for i in range(6):
            for j in range(3):
                outline_rect(draw, [25 + i * 100, 95 + j * 95, 95 + i * 100, 165 + j * 95], O, (240, 210, 90), 2)

    elif theme == "silver":
        gradient_bg(draw, (210, 215, 220), (175, 180, 190))
        floor_plane(draw, (195, 198, 205), (165, 168, 175))
        for i in range(9):
            outline_rect(draw, [20 + i * 68, 75, 75 + i * 68, H - 50], O, (210, 212, 218), 2)

    elif theme == "copper_room":
        gradient_bg(draw, (200, 130, 90), (150, 85, 55))
        floor_plane(draw, (175, 110, 75), (130, 75, 50))
        outline_rect(draw, [80, 80, W - 80, 360], O, (195, 125, 85), 3)

    elif theme == "corridor":
        gradient_bg(draw, (185, 175, 155), (140, 130, 115))
        floor_plane(draw, (160, 150, 140), (120, 110, 100))
        draw.polygon([(120, 120), (520, 100), (500, 360), (140, 380)], fill=(175, 165, 150), outline=O, width=2)

    elif theme == "pool":
        gradient_bg(draw, (140, 150, 175), (90, 100, 130))
        floor_plane(draw, (160, 155, 145), (130, 125, 118))
        draw.polygon([(100, 220), (540, 220), (520, 380), (120, 380)], fill=(90, 40, 100), outline=(60, 30, 70), width=3)
        draw.ellipse([160, 250, 480, 360], fill=(120, 50, 130), outline=(200, 160, 210), width=2)
        draw.text((cx - 80, 300), "grape juice pool", fill=(230, 200, 240))  # may not render without font - skip text
        # skip text - use sparkle instead
        for _ in range(30):
            px, py = rnd.randint(170, 470), rnd.randint(260, 350)
            draw.ellipse([px, py, px + 4, py + 4], fill=(255, 220, 255))

    elif theme == "plant":
        gradient_bg(draw, (60, 90, 55), (35, 55, 35))
        floor_plane(draw, (90, 85, 70), (65, 60, 50))
        for i in range(14):
            x = 25 + i * 42
            h = rnd.randint(100, 220)
            outline_rect(draw, [x, H - h - 40, x + 32, H - 35], O, (40, 110, 45), 2)
            draw.ellipse([x - 8, H - h - 80, x + 40, H - h - 35], fill=(55, 140, 60), outline=O, width=1)
        for i in range(12):
            x0, y0 = rnd.randint(20, 480), rnd.randint(180, 320)
            draw.ellipse([x0, y0, x0 + rnd.randint(50, 100), y0 + rnd.randint(35, 70)], fill=(130, 50, 160), outline=(90, 40, 120), width=1)

    elif theme == "skeleton":
        gradient_bg(draw, (130, 125, 120), (90, 88, 85))
        floor_plane(draw, (145, 140, 135), (110, 105, 100))
        # child skeleton + skull apart + gold ring
        draw.ellipse([cx - 50, 240, cx + 50, 300], fill=(245, 235, 210), outline=O, width=2)
        draw.line([(cx, 200), (cx, 250)], fill=(245, 235, 210), width=8)
        draw.ellipse([cx - 35, 160, cx + 35, 220], fill=(245, 235, 210), outline=O, width=2)
        draw.ellipse([120, 200, 180, 260], fill=(245, 235, 210), outline=O, width=2)
        draw.ellipse([cx - 8, 255, cx + 8, 270], fill=(255, 215, 50), outline=(180, 140, 20), width=2)

    elif theme == "armor":
        gradient_bg(draw, (160, 110, 85), (120, 75, 55))
        floor_plane(draw, (150, 100, 75), (110, 70, 50))
        for row in range(2):
            for col in range(6):
                x = 45 + col * 92 + row * 8
                y = 95 + row * 130
                outline_rect(draw, [x, y, x + 50, y + 125], O, (160, 155, 150), 2)
                draw.line([(x + 25, y + 15), (x + 25, y + 115)], fill=(90, 95, 100), width=4)

    elif theme == "weapon_hall":
        gradient_bg(draw, (120, 115, 125), (85, 80, 90))
        floor_plane(draw, (90, 85, 95), (55, 50, 58))
        for i in range(10):
            x = 50 + i * 58
            draw.line([(x, 70), (x + 12, 340)], fill=(140, 135, 125), width=6)
            draw.rectangle([x - 5, 65, x + 18, 85], fill=(160, 150, 130), outline=O, width=1)

    elif theme == "fur" or theme == "erinyes":
        gradient_bg(draw, (120, 70, 150), (80, 45, 100))
        floor_plane(draw, (100, 60, 120), (70, 40, 85))
        for _ in range(500):
            draw.point((rnd.randint(0, W - 1), rnd.randint(40, H - 1)), fill=(rnd.randint(100, 180), rnd.randint(40, 100), rnd.randint(140, 220)))
        outline_rect(draw, [cx - 60, 200, cx + 60, 380], O, (90, 50, 120), 2)

    elif theme == "stairs":
        gradient_bg(draw, (120, 100, 80), (80, 65, 50))
        floor_plane(draw, (110, 90, 70), (75, 60, 45))
        for i in range(14):
            y0 = 280 - i * 18
            w = 200 + i * 22
            outline_rect(
                draw,
                [cx - w // 2, y0, cx + w // 2, y0 + 14],
                O,
                (140 + i * 3, 110 + i * 2, 75 + i * 2),
                2,
            )

    elif theme == "rocks":
        gradient_bg(draw, (140, 120, 95), (100, 85, 65))
        floor_plane(draw, (130, 110, 85), (90, 75, 55))
        colors = [(120, 95, 70), (90, 110, 130), (140, 130, 90), (100, 85, 110), (130, 115, 75)]
        for i in range(5):
            for j in range(4):
                outline_rect(
                    draw,
                    [50 + i * 110 + j * 8, 90 + j * 55, 130 + i * 110 + j * 8, 150 + j * 55],
                    O,
                    colors[(i + j) % len(colors)],
                    2,
                )

    elif theme == "spiral":
        gradient_bg(draw, (70, 70, 85), (45, 45, 55))
        floor_plane(draw, (85, 85, 95), (55, 55, 65))
        for a in range(0, 900, 10):
            rad = a * 0.32
            px = int(cx + math.cos(math.radians(a)) * rad)
            py = int(cy + 50 + math.sin(math.radians(a)) * rad)
            draw.ellipse([px - 5, py - 5, px + 5, py + 5], fill=(160, 160, 185), outline=O, width=1)

    elif theme == "statue":
        gradient_bg(draw, (85, 120, 75), (55, 85, 55))
        floor_plane(draw, (95, 125, 90), (65, 95, 65))
        for i in range(5):
            x = 70 + i * 115
            outline_rect(draw, [x, 130, x + 48, 340], O, (110, 130, 105), 2)
            draw.ellipse([x + 8, 85, x + 40, 125], fill=(130, 140, 125), outline=O, width=2)

    elif theme == "trophy" or theme == "shooting":
        gradient_bg(draw, (140, 110, 75), (100, 75, 50))
        floor_plane(draw, (130, 100, 70), (95, 70, 48))
        outline_rect(draw, [100, 95, 540, 210], O, (95, 70, 45), 3)
        for i in range(4):
            draw.line([(140 + i * 100, 130), (170 + i * 100, 130)], fill=(50, 50, 55), width=8)

    elif theme == "rat":
        gradient_bg(draw, (140, 95, 70), (100, 65, 45))
        floor_plane(draw, (130, 90, 65), (95, 60, 42))
        outline_rect(draw, [170, 200, 470, 310], O, (120, 80, 55), 3)
        for _ in range(100):
            draw.point((rnd.randint(175, 465), rnd.randint(210, 300)), fill=(85, 110, 55))

    elif theme == "snake":
        gradient_bg(draw, (110, 120, 80), (75, 85, 55))
        floor_plane(draw, (120, 125, 85), (85, 90, 60))
        for i in range(18):
            ang = i * 0.45
            sx = int(180 + math.cos(ang) * 130 + i * 10)
            sy = int(260 + math.sin(ang) * 35)
            draw.ellipse([sx, sy, sx + 28, sy + 14], fill=(170, 185, 70), outline=O, width=1)

    elif theme == "oak":
        gradient_bg(draw, (150, 120, 85), (110, 85, 55))
        floor_plane(draw, (140, 110, 75), (100, 75, 50))
        outline_rect(draw, [35, 70, 210, 400], O, (120, 85, 50), 3)
        for i in range(6):
            outline_rect(draw, [50 + i * 85, 95 + i * 12, 110 + i * 85, 145 + i * 12], O, (95, 75, 50), 1)
        outline_rect(draw, [390, 190, 530, 380], O, (100, 80, 55), 2)

    elif theme == "chunks":
        gradient_bg(draw, (140, 145, 155), (100, 105, 115))
        floor_plane(draw, (130, 132, 138), (95, 97, 102))
        for _ in range(100):
            s = rnd.randint(6, 22)
            x, y = rnd.randint(20, W - s), rnd.randint(180, H - s)
            outline_rect(draw, [x, y, x + s, y + s], O, (rnd.randint(140, 190), rnd.randint(140, 190), rnd.randint(145, 195)), 1)

    elif theme == "spider":
        gradient_bg(draw, (190, 175, 130), (140, 125, 90))
        floor_plane(draw, (175, 155, 115), (130, 110, 80))
        for _ in range(35):
            x1, y1 = rnd.randint(0, W), rnd.randint(0, 120)
            x2, y2 = rnd.randint(0, W), rnd.randint(150, 280)
            draw.line([(x1, y1), (x2, y2)], fill=(40, 35, 30), width=2)
        draw.ellipse([cx - 40, 95, cx + 40, 145], fill=(50, 45, 40), outline=O, width=2)
        for i in range(8):
            ang = i * math.pi / 4
            draw.line([(cx, 120), (cx + math.cos(ang) * 70, 120 + math.sin(ang) * 70)], fill=(45, 40, 35), width=5)

    elif theme == "devil" or theme == "old_man":
        gradient_bg(draw, (160, 60, 55), (90, 35, 35))
        floor_plane(draw, (140, 55, 50), (95, 40, 38))
        if theme == "old_man":
            draw.ellipse([cx - 40, 140, cx + 40, 260], fill=(200, 180, 160), outline=O, width=2)
            draw.line([(cx - 30, 200), (cx + 50, 180)], fill=(180, 180, 185), width=4)
        else:
            draw.ellipse([180, 110, 420, 320], fill=(160, 45, 40), outline=O, width=3)
            draw.polygon([(cx, 60), (cx - 45, 125), (cx + 45, 125)], fill=(120, 30, 30), outline=O, width=2)
            draw.line([(cx - 90, 230), (cx + 90, 230)], fill=(200, 200, 210), width=4)

    elif theme == "computer":
        gradient_bg(draw, (100, 120, 150), (60, 75, 100))
        floor_plane(draw, (90, 105, 130), (55, 65, 85))
        outline_rect(draw, [35, 90, 310, 390], O, (70, 80, 95), 3)
        outline_rect(draw, [330, 100, 610, 370], O, (75, 85, 100), 3)
        outline_rect(draw, [400, 290, 560, 350], O, (30, 35, 40), 2)
        for i in range(10):
            draw.line([(410, 300 + i * 4), (550, 300 + i * 4)], fill=(80, 220, 120), width=2)

    elif theme == "foam":
        gradient_bg(draw, (200, 190, 210), (170, 160, 185))
        floor_plane(draw, (185, 175, 195), (155, 145, 165))
        for _ in range(800):
            draw.point((rnd.randint(0, W - 1), rnd.randint(80, H - 1)), fill=(230, 225, 240))
        draw.polygon([(cx - 40, 120), (cx + 40, 120), (cx + 30, 360), (cx - 30, 360)], fill=(140, 130, 150), outline=O, width=2)

    elif theme == "fungus":
        gradient_bg(draw, (100, 130, 80), (65, 90, 55))
        floor_plane(draw, (110, 135, 85), (75, 95, 60))
        for _ in range(250):
            x, y = rnd.randint(0, W - 15), rnd.randint(70, H - 10)
            draw.ellipse([x, y, x + 18, y + 12], fill=(rnd.randint(60, 120), rnd.randint(120, 180), rnd.randint(50, 90)), outline=(40, 80, 40), width=1)

    elif theme == "marble_bed":
        gradient_bg(draw, (220, 222, 228), (190, 192, 200))
        floor_plane(draw, (210, 210, 215), (180, 180, 186))
        outline_rect(draw, [95, 195, 545, 375], O, (235, 235, 238), 2)
        outline_rect(draw, [115, 155, 485, 215], O, (200, 210, 195), 2)
        outline_rect(draw, [400, 85, 510, 370], O, (210, 212, 218), 2)

    elif theme == "chemical":
        gradient_bg(draw, (130, 170, 110), (85, 120, 75))
        floor_plane(draw, (120, 155, 95), (80, 110, 65))
        for i in range(8):
            x = 45 + i * 72
            draw.ellipse([x, 155, x + 42, 225], fill=(80, 180, 90), outline=O, width=2)
            outline_rect(draw, [x + 8, 120, x + 32, 160], O, (140, 150, 160), 2)

    elif theme == "kitchen":
        gradient_bg(draw, (240, 230, 150), (210, 190, 110))
        floor_plane(draw, (230, 215, 130), (195, 175, 95))
        outline_rect(draw, [50, 115, 590, 210], O, (200, 180, 100), 3)
        outline_rect(draw, [450, 130, 570, 370], O, (160, 155, 150), 3)

    elif theme == "bathroom":
        gradient_bg(draw, (90, 95, 105), (55, 58, 65))
        floor_plane(draw, (80, 82, 88), (45, 46, 52))
        outline_rect(draw, [95, 130, 545, 370], O, (70, 72, 78), 3)
        outline_rect(draw, [220, 270, 410, 360], O, (35, 38, 45), 2)

    elif theme == "pink_bath":
        gradient_bg(draw, (240, 160, 200), (220, 120, 170))
        floor_plane(draw, (235, 150, 190), (210, 110, 160))
        outline_rect(draw, [70, 95, 570, 410], O, (250, 150, 190), 3)
        round_rect(draw, [140, 215, 500, 365], 22, (255, 170, 200), O, 2)

    elif theme == "medicine":
        gradient_bg(draw, (110, 115, 125), (75, 78, 85))
        floor_plane(draw, (100, 102, 108), (65, 66, 72))
        outline_rect(draw, [180, 100, 460, 280], O, (200, 205, 215), 2)
        outline_rect(draw, [200, 140, 280, 220], O, (80, 180, 90), 2)
        outline_rect(draw, [300, 140, 380, 220], O, (240, 140, 180), 2)

    elif theme == "pills_green":
        gradient_bg(draw, (100, 150, 110), (70, 110, 75))
        draw.ellipse([cx - 40, cy - 30, cx + 40, cy + 30], fill=(60, 200, 90), outline=O, width=2)

    elif theme == "pills_pink":
        gradient_bg(draw, (200, 150, 170), (170, 110, 140))
        draw.ellipse([cx - 40, cy - 30, cx + 40, cy + 30], fill=(255, 140, 180), outline=O, width=2)

    elif theme == "golden_key":
        gradient_bg(draw, (140, 130, 100), (100, 95, 75))
        draw.rectangle([cx - 60, cy - 15, cx + 20, cy + 15], fill=(255, 215, 60), outline=(160, 120, 20), width=2)
        draw.ellipse([cx + 15, cy - 25, cx + 55, cy + 25], fill=(255, 220, 80), outline=(160, 120, 20), width=2)

    elif theme == "torture":
        gradient_bg(draw, (120, 75, 70), (80, 45, 42))
        floor_plane(draw, (110, 65, 60), (70, 40, 38))
        outline_rect(draw, [200, 75, 360, 400], O, (100, 60, 55), 3)
        draw.arc([170, 190, 430, 420], 0, 180, fill=(90, 60, 55), width=8)

    elif theme == "theater":
        gradient_bg(draw, (90, 85, 110), (55, 50, 70))
        floor_plane(draw, (80, 75, 95), (50, 45, 60))
        outline_rect(draw, [cx - 130, 70, cx + 130, 230], O, (40, 38, 50), 3)
        for i in range(7):
            outline_rect(draw, [70 + i * 85, 310, 125 + i * 85, 400], O, (70, 68, 75), 2)

    elif theme == "laser":
        gradient_bg(draw, (40, 50, 70), (25, 30, 45))
        floor_plane(draw, (50, 55, 70), (35, 38, 50))
        for i in range(6):
            draw.line([(80, 130 + i * 45), (560, 140 + i * 45)], fill=(255, 80, 80), width=4)

    elif theme == "robot":
        gradient_bg(draw, (120, 130, 145), (85, 92, 105))
        floor_plane(draw, (110, 118, 130), (75, 80, 90))
        outline_rect(draw, [210, 115, 430, 370], O, (110, 118, 130), 3)
        draw.ellipse([250, 75, 390, 145], fill=(95, 100, 110), outline=O, width=2)
        draw.line([(cx, 150), (cx, 340)], fill=(80, 85, 95), width=16)

    elif theme == "cage":
        gradient_bg(draw, (150, 130, 110), (110, 95, 80))
        floor_plane(draw, (140, 120, 100), (100, 85, 72))
        for i in range(8):
            draw.line([(175 + i * 42, 95), (175 + i * 42, 400)], fill=(90, 85, 75), width=4)
        draw.ellipse([230, 175, 410, 370], fill=(160, 130, 110), outline=O, width=2)

    elif theme == "shower":
        gradient_bg(draw, (180, 200, 215), (130, 150, 170))
        floor_plane(draw, (170, 185, 200), (120, 135, 150))
        for i in range(9):
            draw.line([(85 + i * 62, 75), (85 + i * 62, 410)], fill=(160, 185, 210), width=3)

    elif theme == "crate" or theme == "storage":
        gradient_bg(draw, (150, 150, 155), (115, 115, 120))
        floor_plane(draw, (140, 140, 145), (105, 105, 110))
        for i in range(5):
            for j in range(3):
                outline_rect(draw, [45 + i * 105, 170 + j * 75, 125 + i * 105, 230 + j * 75], O, (130, 125, 118), 2)

    elif theme == "pink_hall":
        gradient_bg(draw, (220, 140, 170), (180, 100, 130))
        floor_plane(draw, (210, 130, 160), (175, 95, 125))
        draw.polygon([(0, 110), (W, 95), (W, H), (0, H)], fill=(230, 120, 150), outline=O, width=2)

    elif theme == "rug":
        gradient_bg(draw, (160, 120, 95), (120, 85, 65))
        floor_plane(draw, (150, 110, 85), (110, 75, 58))
        draw.polygon([(cx - 130, cy - 20), (cx + 130, cy - 20), (cx + 110, cy + 100), (cx - 110, cy + 100)], fill=(160, 80, 50), outline=(100, 50, 30), width=3)
        for i in range(10):
            draw.arc([cx - 110 + i * 8, cy - 10, cx + 110 - i * 8, cy + 90], 0, 180, fill=(200, 150, 100), width=2)

    elif theme == "magnet":
        gradient_bg(draw, (130, 135, 150), (90, 92, 105))
        floor_plane(draw, (120, 122, 135), (80, 82, 92))
        outline_rect(draw, [90, 35, 550, 125], O, (100, 105, 120), 3)
        for i in range(5):
            draw.line([(cx, 140), (cx + rnd.randint(-90, 90), 360)], fill=(180, 185, 200), width=3)

    elif theme == "disco":
        gradient_bg(draw, (200, 190, 160), (150, 130, 100))
        floor_plane(draw, (180, 165, 140), (130, 115, 95))
        for i in range(24):
            draw.line([(cx, cy), (rnd.randint(0, W), rnd.randint(0, H))], fill=(rnd.randint(200, 255), rnd.randint(180, 240), rnd.randint(100, 200)), width=2)

    elif theme == "electronics":
        gradient_bg(draw, (110, 125, 145), (75, 88, 105))
        floor_plane(draw, (100, 115, 135), (65, 75, 90))
        for i in range(10):
            outline_rect(draw, [35 + i * 58, 125, 88 + i * 58, 205], O, (85, 95, 110), 2)

    elif theme == "hellhound":
        gradient_bg(draw, (200, 160, 90), (140, 100, 50))
        floor_plane(draw, (185, 145, 80), (125, 90, 45))
        draw.rectangle([cx - 35, 35, cx + 35, 400], fill=(255, 230, 140), outline=(200, 170, 80), width=2)
        draw.ellipse([cx - 55, 195, cx + 55, 290], fill=(50, 35, 25), outline=O, width=2)

    elif theme == "ball":
        gradient_bg(draw, (150, 155, 165), (110, 115, 125))
        floor_plane(draw, (140, 145, 155), (100, 105, 115))
        draw.ellipse([cx - 90, 70, cx + 90, 200], fill=(130, 135, 145), outline=O, width=2)
        for _ in range(200):
            draw.point((rnd.randint(190, 450), rnd.randint(220, 400)), fill=(200, 205, 215))

    elif theme == "microscope":
        gradient_bg(draw, (140, 150, 160), (100, 110, 120))
        floor_plane(draw, (130, 140, 150), (90, 100, 110))
        for i in range(6):
            x = 40 + i * 95
            outline_rect(draw, [x, 195, x + 78, 285], O, (95, 100, 108), 2)
            draw.line([(x + 39, 195), (x + 39, 130)], fill=(70, 75, 82), width=4)

    elif theme == "purple_glow" or theme == "purple_heal":
        gradient_bg(draw, (160, 100, 200), (110, 60, 150))
        floor_plane(draw, (150, 90, 190), (100, 55, 140))
        # glowing plants / UV room — not empty circles
        for i in range(16):
            x = 30 + i * 38
            h = rnd.randint(80, 200)
            draw.rectangle([x, H - h, x + 28, H - 40], fill=(50, 140, 60), outline=(30, 90, 35), width=1)
            draw.ellipse([x - 5, H - h - 50, x + 33, H - h], fill=(180, 80, 220), outline=(140, 50, 180), width=2)
        draw.ellipse([cx - 100, 80, cx + 100, 200], outline=(220, 150, 255), width=4)

    elif theme == "red_plastic":
        gradient_bg(draw, (200, 90, 90), (160, 50, 55))
        floor_plane(draw, (190, 80, 80), (150, 45, 48))
        draw.polygon([(0, 110), (W, 130), (W, H), (0, H)], fill=(200, 60, 65), outline=(120, 30, 35), width=2)

    elif theme == "pipe":
        gradient_bg(draw, (150, 145, 135), (115, 110, 100))
        floor_plane(draw, (140, 135, 125), (105, 100, 92))
        for i in range(8):
            round_rect(draw, [45 + i * 72, 150, 108 + i * 72, 330], 22, (160, 155, 148), O, 2)

    elif theme == "finale":
        gradient_bg(draw, (80, 70, 100), (45, 40, 65))
        floor_plane(draw, (70, 60, 90), (40, 35, 55))
        outline_rect(draw, [140, 170, 500, 280], O, (60, 55, 70), 3)
        draw.ellipse([cx - 30, 195, cx + 30, 255], fill=(90, 85, 100), outline=O, width=2)
        draw.line([(cx - 50, 220), (cx - 70, 200)], fill=(200, 200, 210), width=2)
        draw.line([(cx + 50, 220), (cx + 70, 200)], fill=(200, 200, 210), width=2)

    elif theme == "closet":
        gradient_bg(draw, (140, 125, 110), (100, 90, 80))
        floor_plane(draw, (130, 115, 100), (90, 80, 70))
        for i in range(5):
            draw.line([(75 + i * 115, 55), (75 + i * 115, 410)], fill=(95, 85, 75), width=10)
        for i in range(4):
            draw.ellipse([90 + i * 120, 320, 150 + i * 120, 360], fill=(70, 55, 45), outline=O, width=1)

    elif theme == "landing":
        gradient_bg(draw, (140, 140, 150), (100, 100, 110))
        floor_plane(draw, (130, 130, 140), (90, 90, 100))
        outline_rect(draw, [80, 120, 560, 380], O, (150, 148, 155), 2)
        draw.polygon([(120, 320), (200, 260), (280, 320)], fill=(110, 108, 115), outline=O, width=2)

    else:
        # default — generic lit hallway
        indoor_hall(False)


def render(name: str, desc: str) -> Image.Image:
    random.seed(hash(name) % (2**32))
    theme = theme_from_scene(name, desc)
    img = Image.new("RGB", (W, H), (245, 242, 235))
    draw = ImageDraw.Draw(img)
    draw_theme(draw, theme, name, desc)
    return img


def main() -> None:
    tree = ET.parse(XML_PATH)
    root = tree.getroot()
    IMG_DIR.mkdir(parents=True, exist_ok=True)
    for sc in root.findall("scene"):
        name = sc.get("name") or ""
        desc = (sc.findtext("description") or "").strip()
        img = render(name, desc)
        path = IMG_DIR / f"{name}.jpg"
        img.save(path, "JPEG", quality=92, optimize=True)
        print(path.name)


if __name__ == "__main__":
    main()
