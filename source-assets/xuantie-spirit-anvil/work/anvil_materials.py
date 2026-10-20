"""Deterministic pixel materials for the Xuan Tie spirit anvil.

All textures are original procedural artwork.  The working density is 4 pixels
per model unit; returned images are RGBA and never depend on external assets.
"""
from __future__ import annotations

import math
import random
from PIL import Image, ImageDraw


_BASE = {
    "iron": (45, 47, 57),
    "iron_dark": (28, 30, 38),
    "steel": (84, 87, 100),
    "bronze": (204, 177, 143),
    "bronze_dark": (140, 116, 88),
    "blue": (3, 109, 243),
    "cyan": (7, 218, 255),
    "white": (185, 248, 255),
    "socket": (10, 18, 32),
}


def _color(base, amount=0, alpha=255):
    return tuple(max(0, min(255, int(c + amount))) for c in base) + (alpha,)


def paint_surface(width: int, height: int, material: str, seed: int = 0) -> Image.Image:
    """Paint an opaque, pixel-crisp surface using the named material.

    Blue/cyan/white materials have a luminous band down their longer axis.
    This makes the same function useful for upright channels and crossbars.
    """
    width, height = max(1, int(width)), max(1, int(height))
    if material not in _BASE:
        raise ValueError(f"Unknown anvil material: {material}")
    rng = random.Random(seed * 907 + sum(ord(c) for c in material) * 29)
    base = _BASE[material]
    im = Image.new("RGBA", (width, height), _color(base))
    px = im.load()

    if material in {"blue", "cyan", "white"}:
        vertical = height >= width
        span = width if vertical else height
        center = (span - 1) / 2
        for y in range(height):
            for x in range(width):
                pos = x if vertical else y
                t = abs(pos - center) / max(0.5, span / 2)
                if material == "blue":
                    rgb = (0, 223, 255) if t < .29 else ((0, 151, 255) if t < .69 else (6, 72, 198))
                elif material == "cyan":
                    rgb = (136, 250, 255) if t < .36 else (7, 207, 250)
                else:
                    rgb = (206, 255, 255) if t < .48 else (134, 235, 255)
                px[x, y] = _color(rgb, rng.choice([-3, 0, 0, 1, 2]))
        return im

    # Several overlapping, irregular patches give forged metal rather than a
    # regular brick grid.  Fine pixels are intentionally much lower contrast.
    draw = ImageDraw.Draw(im)
    contrast = 8 if material.startswith("iron") else 12
    if material == "socket":
        contrast = 4
    for _ in range(max(2, width * height // 6)):
        x, y = rng.randrange(width), rng.randrange(height)
        w, h = rng.randrange(1, 4), rng.randrange(1, 4)
        shade = rng.randint(-contrast, contrast)
        draw.rectangle((x, y, x + w, y + h), fill=_color(base, shade))
    px = im.load()
    for y in range(height):
        for x in range(width):
            r, g, b, a = px[x, y]
            n = rng.choice([-2, -1, 0, 0, 0, 1, 2])
            px[x, y] = _color((r, g, b), n)

    # Directional lip light and discontinuous wear.  No black outlines between
    # every patch: those made small previews look like tiled brickwork.
    if material != "socket" and width > 2 and height > 2:
        top = 12 if material.startswith("iron") else 22
        left = 5 if material.startswith("iron") else 11
        draw.line((0, 0, width - 1, 0), fill=_color(base, top))
        draw.line((0, 1, 0, height - 1), fill=_color(base, left))
        draw.line((1, height - 1, width - 1, height - 1), fill=_color(base, -9))
        draw.line((width - 1, 1, width - 1, height - 1), fill=_color(base, -6))
        for x in range(1, width - 1):
            if rng.random() < .15:
                draw.point((x, 0), fill=_color(base, top + rng.randint(4, 12)))
        if material in {"bronze", "bronze_dark", "steel"}:
            # Isolated short engraving/scuff marks, not a repeating panel grid.
            for _ in range(max(1, width * height // 170)):
                x, y = rng.randrange(1, width - 1), rng.randrange(1, height - 1)
                draw.line((x, y, min(width - 2, x + rng.randrange(1, 4)), y), fill=_color(base, -15))
    return im


def paint_top(width: int = 96, height: int = 48) -> Image.Image:
    """Iron face with a copper pixel circle, cyan core, and blue cross channels.

    The default ring radius is 14 pixels.  Rendering at another size rescales
    the complete design with nearest-neighbour sampling to keep pixel edges.
    """
    im = paint_surface(96, 48, "iron", seed=337)
    px = im.load()
    cx, cy = 47.5, 23.5
    # Faint electrical spill remains visibly pixelated and dark enough for the
    # surrounding forged iron surface to read as metal, not blue plastic.
    for y in range(48):
        for x in range(96):
            dx, dy = x - cx, y - cy
            r = math.hypot(dx, dy)
            spill = max(0, 1 - abs(dx) / 5.5)
            if 7 <= x <= 88:
                spill = max(spill, max(0, 1 - abs(dy) / 4))
            spill = max(spill * .35, max(0, 1 - abs(r - 5) / 6) * .42)
            if spill > 0:
                old = px[x, y]
                px[x, y] = tuple(int(old[i] * (1 - spill) + (0, 84, 183)[i] * spill) for i in range(3)) + (255,)

    d = ImageDraw.Draw(im)
    # Vertical 5px channel and horizontal 3px channel, with brighter centers.
    d.rectangle((45, 0, 49, 47), fill=(3, 99, 230, 255))
    d.rectangle((46, 0, 48, 47), fill=(0, 166, 255, 255))
    d.line((47, 0, 47, 47), fill=(24, 227, 255, 255))
    d.rectangle((7, 22, 88, 24), fill=(0, 111, 236, 255))
    d.line((9, 23, 86, 23), fill=(12, 205, 255, 255))
    # Step-rounded ring.  Cool inner recess and copper rim form an inset seal.
    for y in range(8, 40):
        for x in range(32, 64):
            dx, dy = x - cx, y - cy
            r = math.hypot(dx, dy)
            if 11.4 <= r < 12.7 and abs(dx) > 2.6 and abs(dy) > 1.6:
                px[x, y] = (32, 56, 84, 255)
            if 12.7 <= r <= 14.0:
                # Four small openings let the cross channel enter the seal.
                if abs(dx) <= 1.6 or abs(dy) <= 1.1:
                    continue
                shade = 13 if dx + dy < 0 else -6
                px[x, y] = _color((168, 142, 111), shade)
            if 6.7 <= r < 8.2 and abs(dx) > 2.5 and abs(dy) > 1.5:
                px[x, y] = (20, 55, 101, 255)
            if 5.7 <= r < 6.7:
                px[x, y] = (6, 90, 196, 255)
            if 4.6 <= r < 5.7:
                px[x, y] = (1, 154, 247, 255)
            if 3.4 <= r < 4.6:
                px[x, y] = (1, 213, 255, 255)
            if 2.0 <= r < 3.4:
                px[x, y] = (69, 242, 255, 255)
            if r < 2.0:
                px[x, y] = (195, 255, 255, 255)
    # Tiny blue pin sockets near the four bronze corner fittings.
    for x, y in ():
        d.rectangle((x - 1, y - 1, x + 2, y + 2), fill=(16, 41, 68, 255))
        d.rectangle((x, y, x + 1, y + 1), fill=(0, 160, 246, 255))
        d.point((x, y), fill=(62, 219, 255, 255))
    width, height = max(1, int(width)), max(1, int(height))
    return im if (width, height) == (96, 48) else im.resize((width, height), Image.Resampling.NEAREST)


if __name__ == "__main__":
    from pathlib import Path
    out = Path(__file__).with_name("top-test.png")
    paint_top().resize((768, 384), Image.Resampling.NEAREST).save(out)
    print(out)
