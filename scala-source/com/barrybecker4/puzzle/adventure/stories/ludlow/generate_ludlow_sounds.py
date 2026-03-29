#!/usr/bin/env python3
"""
Synthesize unique .au (Sun/NeXT 16-bit linear PCM) ambience per Ludlow scene.
Uses theme_from_scene from generate_ludlow_art for mood-appropriate sound design.
"""
from __future__ import annotations

import math
import random
import struct
import xml.etree.ElementTree as ET
from pathlib import Path

# Import theme resolution from art generator (single source of truth)
import importlib.util

_ROOT = Path(__file__).resolve().parent
_spec = importlib.util.spec_from_file_location("ludlow_art", _ROOT / "generate_ludlow_art.py")
_art = importlib.util.module_from_spec(_spec)
assert _spec.loader is not None
_spec.loader.exec_module(_art)
theme_from_scene = _art.theme_from_scene

SND_DIR = _ROOT / "sounds"
XML_PATH = _ROOT / "ludlowScript.xml"

SAMPLE_RATE = 22050
DURATION = 2.6  # seconds — short ambient sting


def write_au(path: Path, samples_f: list[float], sample_rate: int = SAMPLE_RATE) -> None:
    """Write Sun .au with 16-bit big-endian linear PCM (encoding 3)."""
    peak = max(abs(s) for s in samples_f) or 1.0
    scale = 0.88 / peak
    data = bytearray()
    for s in samples_f:
        v = int(max(-32768, min(32767, s * scale * 32767.0)))
        data.extend(struct.pack(">h", v))
    hdr = struct.pack(">I", 0x2E736E64)  # .snd
    hdr += struct.pack(">I", 24)
    hdr += struct.pack(">I", len(data))
    hdr += struct.pack(">I", 3)  # 16-bit linear
    hdr += struct.pack(">I", sample_rate)
    hdr += struct.pack(">I", 1)  # mono
    path.write_bytes(hdr + data)


def env_gate(i: int, n: int, attack: float = 0.05, release: float = 0.15) -> float:
    t = i / max(n - 1, 1)
    if t < attack:
        return t / attack
    if t > 1.0 - release:
        return max(0.0, (1.0 - t) / release)
    return 1.0


def synth(
    theme: str,
    seed: int,
    scene_name: str = "",
    sr: int = SAMPLE_RATE,
    duration: float = DURATION,
) -> list[float]:
    rng = random.Random(seed)
    n = int(sr * duration)
    out = [0.0] * n
    eg = lambda i: env_gate(i, n)

    def add_tone(freq: float, amp: float, ph: float = 0.0) -> None:
        w = 2 * math.pi * freq / sr
        for i in range(n):
            out[i] += amp * eg(i) * math.sin(w * i + ph)

    def add_noise(amp: float, color: float = 0.5) -> None:
        """color 0=white, 1=pink-ish (simple 1/f approx)"""
        prev = 0.0
        for i in range(n):
            x = rng.gauss(0, 1)
            prev = color * prev + (1 - color) * x
            out[i] += amp * eg(i) * prev * 0.4

    # --- Theme-specific character (story-appropriate) ---
    if theme == "start":
        add_noise(0.35, 0.7)
        f = 420 + (seed % 80)
        add_tone(f, 0.08)
        add_tone(f * 1.5, 0.04)
        for k in range(8):
            t0 = int(n * (0.1 + k * 0.11))
            for j in range(min(800, n - t0)):
                i = t0 + j
                if i >= n:
                    break
                out[i] += 0.12 * math.sin(2 * math.pi * (1800 + seed % 200) * i / sr) * math.exp(-j * 0.004)

    elif theme in ("no_response", "closet"):
        add_tone(55 + seed % 30, 0.12)
        add_noise(0.08, 0.85)

    elif theme == "sky_falls":
        add_noise(0.9, 0.2)
        for i in range(n):
            out[i] += 0.5 * math.sin(2 * math.pi * (seed % 40 + 10) * i / sr) * (i / n)

    elif theme == "quit":
        add_tone(330 + seed % 50, 0.15)
        add_tone(440, 0.08)
        add_noise(0.1, 0.4)

    elif theme in ("entrance", "corridor", "landing", "default"):
        add_tone(100 + (seed % 40), 0.18)
        add_tone(150 + (seed % 30), 0.1)
        for i in range(0, n, int(sr * 0.35)):
            for j in range(80):
                if i + j >= n:
                    break
                out[i + j] += 0.15 * math.exp(-j * 0.04) * math.sin(2 * math.pi * 200 * (i + j) / sr)

    elif theme in ("mirror_hall", "mirror", "mirror_only"):
        f = 880 + seed % 120
        for h in (1, 2, 3, 4, 5):
            add_tone(f * h, 0.04 / h)
        add_noise(0.06, 0.3)

    elif theme in ("gold", "golden_key"):
        for k in range(5):
            t0 = int(n * (0.05 + k * 0.18))
            f = 1200 + k * 200 + seed % 100
            for j in range(min(3000, n - t0)):
                i = t0 + j
                if i >= n:
                    break
                out[i] += 0.25 * math.sin(2 * math.pi * f * i / sr) * math.exp(-j * 0.002)

    elif theme == "silver":
        for k in range(6):
            t0 = int(n * (0.04 + k * 0.15))
            f = 1800 + seed % 300 + k * 150
            for j in range(min(2000, n - t0)):
                i = t0 + j
                if i >= n:
                    break
                out[i] += 0.2 * math.sin(2 * math.pi * f * i / sr) * math.exp(-j * 0.0025)

    elif theme == "copper_room":
        add_tone(220 + seed % 40, 0.2)
        add_tone(275, 0.12)
        add_noise(0.12, 0.5)

    elif theme == "pool":
        add_noise(0.45, 0.6)
        lf = 3 + (seed % 5) * 0.3
        for i in range(n):
            out[i] += 0.2 * math.sin(2 * math.pi * lf * i / sr) * math.sin(2 * math.pi * 0.5 * i / sr)

    elif theme in ("plant", "pills_green"):
        add_noise(0.35, 0.75)
        add_tone(180 + seed % 50, 0.1)
        for i in range(n):
            out[i] += 0.08 * math.sin(2 * math.pi * (2.5 + seed % 3) * i / sr)

    elif theme == "skeleton":
        for k in range(12):
            t0 = int(n * (0.02 + k * 0.08))
            for j in range(min(400, n - t0)):
                i = t0 + j
                if i >= n:
                    break
                out[i] += 0.35 * math.sin(2 * math.pi * (800 + j) * i / sr) * math.exp(-j * 0.015)

    elif theme == "armor":
        for k in range(8):
            t0 = int(n * (0.05 + k * 0.11))
            for j in range(min(600, n - t0)):
                i = t0 + j
                if i >= n:
                    break
                out[i] += 0.22 * math.sin(2 * math.pi * (150 + j * 2) * i / sr) * math.exp(-j * 0.008)

    elif theme in ("weapon_hall", "trophy", "shooting"):
        add_tone(90 + seed % 20, 0.15)
        add_noise(0.2, 0.4)
        for k in range(3):
            t0 = int(n * (0.2 + k * 0.25))
            for j in range(min(150, n - t0)):
                i = t0 + j
                if i >= n:
                    break
                out[i] += 0.4 * math.sin(2 * math.pi * 400 * i / sr) * math.exp(-j * 0.05)

    elif theme in ("fur", "erinyes"):
        add_tone(110 + seed % 30, 0.14)
        add_noise(0.3, 0.8)

    elif theme in ("spiral", "stairs"):
        f = 70 + seed % 25
        for h in range(1, 8):
            add_tone(f * h * 0.5, 0.05 / h)
        for i in range(0, n, 400):
            for j in range(min(200, n - i)):
                out[i + j] += 0.1 * math.sin(2 * math.pi * (3 + seed % 2) * (i + j) / sr)

    elif theme == "rocks":
        for k in range(20):
            t0 = int(n * rng.random())
            for j in range(min(120, n - t0)):
                i = t0 + j
                if i >= n:
                    break
                out[i] += 0.2 * rng.choice([-1, 1]) * math.exp(-j * 0.03)

    elif theme == "statue":
        add_tone(65 + seed % 15, 0.2)
        add_tone(130, 0.1)
        add_noise(0.05, 0.9)

    elif theme in ("rat", "snake"):
        f = 300 + seed % 100
        for i in range(n):
            out[i] += 0.15 * math.sin(2 * math.pi * f * i / sr) * math.sin(2 * math.pi * 6 * i / sr)
        add_noise(0.2, 0.5)

    elif theme == "oak":
        add_tone(80 + seed % 25, 0.18)
        add_tone(120, 0.1)
        add_noise(0.12, 0.55)

    elif theme == "chunks":
        for k in range(40):
            t0 = int(n * rng.random() * 0.9)
            for j in range(min(80, n - t0)):
                i = t0 + j
                if i >= n:
                    break
                out[i] += 0.35 * rng.gauss(0, 1) * math.exp(-j * 0.04)

    elif theme == "spider":
        f = 2000 + seed % 400
        for i in range(n):
            out[i] += 0.12 * math.sin(2 * math.pi * f * i / sr) * (0.5 + 0.5 * math.sin(2 * math.pi * 8 * i / sr))
        add_noise(0.15, 0.4)

    elif theme in ("devil", "old_man", "torture"):
        add_tone(45 + seed % 15, 0.22)
        add_tone(73, 0.18)
        add_tone(98, 0.12)
        add_noise(0.18, 0.65)

    elif theme in ("computer", "electronics", "microscope", "laser"):
        f = 120 + seed % 40
        for i in range(n):
            sq = 1.0 if (i // (sr // (100 + seed % 50))) % 2 == 0 else -1.0
            out[i] += 0.15 * sq * eg(i) * 0.8
        add_tone(f * 4, 0.08)
        add_noise(0.12, 0.35)

    elif theme == "foam":
        add_noise(0.5, 0.5)

    elif theme == "fungus":
        add_noise(0.4, 0.85)
        add_tone(100 + seed % 30, 0.08)

    elif theme == "marble_bed":
        add_tone(200 + seed % 40, 0.12)
        add_noise(0.1, 0.4)
        for k in range(4):
            t0 = int(n * (0.1 + k * 0.22))
            for j in range(min(1500, n - t0)):
                i = t0 + j
                if i >= n:
                    break
                out[i] += 0.1 * math.sin(2 * math.pi * (400 + k * 50) * i / sr) * math.exp(-j * 0.001)

    elif theme == "chemical":
        for i in range(n):
            t = i / sr
            out[i] += 0.2 * math.sin(2 * math.pi * (400 * t + 30 * math.sin(2 * math.pi * 2 * t)))
        add_noise(0.15, 0.45)

    elif theme == "kitchen":
        add_tone(60 + seed % 20, 0.12)
        for k in range(6):
            t0 = int(n * (0.1 + k * 0.14))
            for j in range(min(100, n - t0)):
                i = t0 + j
                if i >= n:
                    break
                out[i] += 0.15 * math.exp(-j * 0.02)

    elif theme in ("bathroom", "medicine"):
        if scene_name == "J.turnOnWater":
            add_noise(0.2, 0.45)
            for k in range(25):
                t0 = int(n * rng.random() * 0.9)
                for j in range(min(120, n - t0)):
                    i = t0 + j
                    if i >= n:
                        break
                    out[i] += 0.35 * math.sin(2 * math.pi * (1200 + j * 5) * i / sr) * math.exp(-j * 0.04)
        else:
            add_noise(0.25, 0.5)
            for k in range(15):
                t0 = int(n * rng.random() * 0.95)
                for j in range(min(3000, n - t0)):
                    i = t0 + j
                    if i >= n:
                        break
                    out[i] += 0.08 * math.sin(2 * math.pi * 800 * i / sr) * math.exp(-j * 0.0008)

    elif theme in ("pink_bath", "pills_pink"):
        add_tone(350 + seed % 60, 0.14)
        add_tone(520, 0.08)
        add_noise(0.12, 0.5)

    elif theme == "theater":
        add_tone(55 + seed % 10, 0.18)
        add_noise(0.1, 0.4)

    elif theme == "robot":
        f = 80 + seed % 30
        for i in range(n):
            out[i] += 0.18 * math.sin(2 * math.pi * f * i / sr + 0.5 * math.sin(2 * math.pi * 0.5 * i / sr))
        add_noise(0.1, 0.4)

    elif theme == "cage":
        add_tone(90 + seed % 25, 0.16)
        for i in range(0, n, int(sr * 0.4)):
            for j in range(min(2000, n - i)):
                if i + j >= n:
                    break
                out[i + j] += 0.12 * math.sin(2 * math.pi * 250 * (i + j) / sr) * math.exp(-j * 0.001)

    elif theme == "shower":
        add_noise(0.55, 0.45)
        add_tone(4000 + seed % 500, 0.04)

    elif theme in ("crate", "storage", "open_crates"):
        for k in range(10):
            t0 = int(n * (0.05 + k * 0.09))
            for j in range(min(500, n - t0)):
                i = t0 + j
                if i >= n:
                    break
                out[i] += 0.2 * math.sin(2 * math.pi * (200 + j) * i / sr) * math.exp(-j * 0.01)

    elif theme == "pink_hall":
        add_tone(250 + seed % 40, 0.16)
        add_tone(330, 0.1)
        add_noise(0.1, 0.5)

    elif theme == "rug":
        add_tone(100 + seed % 30, 0.14)
        add_noise(0.18, 0.65)

    elif theme == "magnet":
        f = 50 + seed % 15
        for i in range(n):
            out[i] += 0.25 * math.sin(2 * math.pi * f * i / sr) * math.sin(2 * math.pi * 1.2 * i / sr)

    elif theme == "disco":
        for i in range(n):
            t = i / sr
            out[i] += 0.15 * math.sin(2 * math.pi * (200 + 100 * math.sin(2 * math.pi * 2 * t)) * i / sr)
        add_noise(0.1, 0.3)

    elif theme == "hellhound":
        add_tone(70 + seed % 20, 0.2)
        add_tone(140, 0.12)
        for i in range(n):
            out[i] += 0.1 * math.sin(2 * math.pi * 3 * i / sr)

    elif theme == "ball":
        for k in range(30):
            t0 = int(n * rng.random() * 0.85)
            for j in range(min(30, n - t0)):
                i = t0 + j
                if i >= n:
                    break
                out[i] += 0.25 * math.sin(2 * math.pi * 2000 * i / sr) * math.exp(-j * 0.2)

    elif theme in ("purple_glow", "purple_heal"):
        f = 150 + seed % 40
        for h in range(1, 6):
            add_tone(f * h * 0.5, 0.06 / h)
        add_noise(0.2, 0.55)

    elif theme == "red_plastic":
        add_tone(110 + seed % 30, 0.15)
        for i in range(n):
            out[i] += 0.12 * math.sin(2 * math.pi * 0.3 * i / sr)

    elif theme == "pipe":
        add_tone(180 + seed % 50, 0.14)
        add_noise(0.15, 0.5)
        for i in range(0, n, int(sr * 0.08)):
            for j in range(min(400, n - i)):
                if i + j >= n:
                    break
                out[i + j] += 0.1 * math.exp(-j * 0.01)

    elif theme == "finale":
        add_tone(100 + seed % 30, 0.12)
        for k in range(3):
            t0 = int(n * (0.2 + k * 0.25))
            for j in range(min(8000, n - t0)):
                i = t0 + j
                if i >= n:
                    break
                out[i] += 0.15 * math.sin(2 * math.pi * (300 + k * 100) * i / sr) * math.exp(-j * 0.0004)

    else:
        # Fallback: unique per seed still
        add_tone(120 + (seed % 100), 0.15)
        add_tone(180 + (seed % 80), 0.08)
        add_noise(0.18, 0.55 + (seed % 30) / 100.0)

    return out


def main() -> None:
    SND_DIR.mkdir(parents=True, exist_ok=True)
    tree = ET.parse(XML_PATH)
    root = tree.getroot()
    for sc in root.findall("scene"):
        name = sc.get("name") or ""
        desc = (sc.findtext("description") or "").strip()
        th = theme_from_scene(name, desc)
        seed = hash(name) % (2**32)
        samples = synth(th, seed, name)
        path = SND_DIR / f"{name}.au"
        write_au(path, samples)
        print(path.name)


if __name__ == "__main__":
    main()
