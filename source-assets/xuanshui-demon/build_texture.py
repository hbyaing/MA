"""Build the original, deterministic pixel-texture atlas for 玄水妖.

UV rectangles use pixel coordinates, with right/bottom exclusive:
skin=(0,0,512,512), fin=(512,0,1024,512), belly=(0,512,512,768),
ridge=(512,512,768,768), claw=(768,512,1024,768).
Eight 64px solid-colour swatches occupy y=800..863, x=0..511.
This generates original patterns; it does not use the reference images.
"""
from pathlib import Path
import argparse
import math
import random
from PIL import Image, ImageDraw

OUT = Path(__file__).with_name("xuanshui_atlas.png")
SEED = 861720
SWATCHES = [
    (5, 15, 27), (5, 34, 62), (35, 220, 255), (195, 249, 255),
    (10, 71, 143), (8, 36, 83), (122, 150, 175), (93, 190, 235),
]


def clamp(n):
    return max(0, min(255, round(n)))


def rgb_add(rgb, offset):
    return tuple(clamp(c + d) for c, d in zip(rgb, offset))


def mix(a, b, t):
    t = max(0, min(1, t))
    return tuple(clamp(x + (y - x) * t) for x, y in zip(a, b))


def noise_field(w, h, grid, rng):
    """Smooth low-frequency variation; never uncorrelated grain."""
    tiny = Image.new("L", (grid, grid))
    tiny.putdata([rng.randrange(50, 206) for _ in range(grid * grid)])
    return tiny.resize((w, h), Image.Resampling.BICUBIC)


def scale_skin(w, h, dark=False):
    rng = random.Random(SEED + dark)
    skin = Image.new("RGB", (w, h))
    fine = noise_field(w, h, max(8, w // 24), rng)
    broad = noise_field(w, h, 6, rng)
    out, fp, bp = skin.load(), fine.load(), broad.load()
    scale_random = {(cx, cy): rng.uniform(-1, 1)
                    for cx in range(-4, w // 10 + 5)
                    for cy in range(-4, h // 8 + 5)}
    for y in range(h):
        for x in range(w):
            # A slight flowing warp breaks the rigid staggered-tile look.
            wy = y + 2.2 * math.sin(x / 37) + 1.2 * math.sin(x / 13)
            row = math.floor(wy / 9.6)
            wx = x + 2.1 * math.sin(y / 34) + 6 * (row % 2)
            col = math.floor(wx / 12)
            sx = (wx % 12 - 6) / 6
            sy = (wy % 9.6) / 9.6
            # Gently scalloped overlapping scales, not rectangle outlines.
            arc = .26 + .68 * math.sqrt(max(0, 1 - sx * sx))
            edge = abs(sy - arc)
            rand = scale_random.get((col, row), 0)
            relief = 2.8 * (1 - sy) + 2 * math.sin(sx * 2 + .3)
            if edge < .085:
                relief -= 5.2
            elif .085 <= sy - arc < .17:
                relief += 2.1
            if .2 < sy < .42 and -.65 < sx < -.05:
                relief += 2.0
            varied = (fp[x, y] - 128) / 20 + (bp[x, y] - 128) / 11 + rand * 3.5
            v = varied + relief
            base = (9, 43, 90) if not dark else (6, 28, 61)
            out[x, y] = rgb_add(base, (v * .30, v * .88, v * 1.35))
    return skin


def fin_membrane():
    rng = random.Random(SEED + 11)
    # Work at 256px for clean two-pixel facets, then nearest-neighbour upscale.
    w = h = 256
    fin = Image.new("RGB", (w, h))
    light = noise_field(w, h, 19, rng).load()
    broad = noise_field(w, h, 7, rng).load()
    p = fin.load()
    stops = [(0., (216, 251, 255)), (.13, (171, 235, 255)),
             (.42, (82, 175, 222)), (.72, (31, 108, 179)),
             (1., (9, 45, 101))]
    for y in range(h):
        t = y / (h - 1)
        for lo, hi in zip(stops, stops[1:]):
            if lo[0] <= t <= hi[0]:
                base = mix(lo[1], hi[1], (t - lo[0]) / (hi[0] - lo[0]))
                break
        for x in range(w):
            # Converging veins radiate from a root below the UV rectangle.
            angle = math.atan2(x - w * .50, h * 1.25 - y)
            wave = math.sin(angle * 96 + .12 * math.sin(y / 21))
            thin_rib = max(0, wave) ** 10
            broad_rib = math.sin(angle * 34 + .55) * 4
            crystalline = (light[x, y] - 128) / 14 + (broad[x, y] - 128) / 18
            v = crystalline + broad_rib + thin_rib * (10 + t * 9)
            p[x, y] = rgb_add(base, (v * .75, v, v * .8))
    return fin.resize((512, 512), Image.Resampling.NEAREST)


def belly_plates():
    rng = random.Random(SEED + 29)
    w, h = 512, 256
    im = Image.new("RGB", (w, h))
    variation = noise_field(w // 2, h // 2, 22, rng).resize((w, h), Image.Resampling.NEAREST).load()
    p = im.load()
    for y in range(h):
        for x in range(w):
            # Each plate ends on an arch with a pale raised lip.
            bow = 8 * math.cos((x - 256) * math.pi / 256)
            line = (y - bow) % 32
            shaded = (1 - line / 32) * 13
            if line < 2:
                shaded = -18
            elif line < 5:
                shaded = 19
            elif line > 27:
                shaded -= 5
            facet = (variation[x, y] - 128) / 13
            side = 6 * math.cos((x - 256) * math.pi / 256)
            v = shaded + facet + side
            p[x, y] = rgb_add((95, 125, 148), (v, v, v * .96))
    return im


def claw_texture():
    rng = random.Random(SEED + 41)
    w = h = 256
    im = Image.new("RGB", (w, h))
    p = im.load()
    fine = noise_field(w // 2, h // 2, 26, rng).resize((w, h), Image.Resampling.NEAREST).load()
    for y in range(h):
        for x in range(w):
            flow = x + 2 * math.sin(y / 41) + math.sin(y / 11)
            vein = math.sin(flow / 5.1) * 4 + math.sin(flow / 2.7) * 1.6
            facet = 12 * math.sin(x * math.pi / 64)
            highlight = (1 - y / h) * 15
            v = vein + facet + highlight + (fine[x, y] - 128) / 14
            p[x, y] = rgb_add((68, 90, 121), (v * .91, v, v * 1.05))
    return im


def irregular_skin_variant(original):
    """Add stepped 20-60px armour facets while keeping the original scale relief."""
    rng = random.Random(SEED + 83)
    cell = 38
    seeds = {}
    for gy in range(-2, 17):
        for gx in range(-2, 17):
            seeds[gx, gy] = (
                (gx + rng.uniform(.15, .85)) * cell,
                (gy + rng.uniform(.15, .85)) * cell,
                rng.uniform(-1, 1),
            )
    # A second coherent field keeps groups of plates related in colour.
    patches = noise_field(128, 128, 8, rng).load()
    shade = Image.new("L", (128, 128))
    sp = shade.load()
    for py in range(128):
        for px in range(128):
            x, y = px * 4, py * 4
            # Quantized, slightly meandering boundaries avoid a regular grid.
            wx = x + 6 * math.sin(y / 47)
            wy = y + 7 * math.sin(x / 61)
            gx, gy = math.floor(wx / cell), math.floor(wy / cell)
            near = min((seeds[i, j] for i in range(gx - 1, gx + 2)
                        for j in range(gy - 1, gy + 2)),
                       key=lambda s: (s[0] - wx) ** 2 + (s[1] - wy) ** 2)
            local = near[2] * .74 + (patches[px, py] - 128) / 128 * .56
            sp[px, py] = clamp(128 + local * 90)
    facets = shade.resize((512, 512), Image.Resampling.NEAREST).load()
    output = original.copy()
    dst, src = output.load(), original.load()
    for y in range(512):
        for x in range(512):
            v = (facets[x, y] - 128) / 90
            dst[x, y] = rgb_add(src[x, y], (v * 3.2, v * 12.4, v * 23))
    return output


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--v2", action="store_true", help="Save a skin-only variant; preserve the original atlas.")
    args = parser.parse_args()
    if args.v2:
        if not OUT.exists():
            raise FileNotFoundError("Generate the original atlas first.")
        original = Image.open(OUT).convert("RGB")
        atlas = original.copy()
        atlas.paste(irregular_skin_variant(original.crop((0, 0, 512, 512))), (0, 0))
        assert atlas.crop((512, 0, 1024, 1024)).tobytes() == original.crop((512, 0, 1024, 1024)).tobytes()
        assert atlas.crop((0, 512, 512, 1024)).tobytes() == original.crop((0, 512, 512, 1024)).tobytes()
        target = OUT.with_name("xuanshui_atlas_v2.png")
        atlas.save(target, optimize=True)
        print(f"Generated {target}; verified all pixels outside skin are unchanged.")
        return
    atlas = Image.new("RGB", (1024, 1024), (8, 27, 53))
    atlas.paste(scale_skin(512, 512), (0, 0))
    atlas.paste(fin_membrane(), (512, 0))
    atlas.paste(belly_plates(), (0, 512))
    atlas.paste(scale_skin(256, 256, dark=True), (512, 512))
    atlas.paste(claw_texture(), (768, 512))
    draw = ImageDraw.Draw(atlas)
    for i, colour in enumerate(SWATCHES):
        draw.rectangle((i * 64, 800, i * 64 + 63, 863), fill=colour)
    # Reserved complementary materials; the primary eight remain exact solids.
    for i, colour in enumerate([(19, 98, 159), (31, 146, 203), (132, 220, 250),
                                (225, 253, 255), (19, 52, 94), (41, 76, 118),
                                (72, 117, 153), (163, 190, 205)], start=8):
        draw.rectangle((i * 64, 800, i * 64 + 63, 863), fill=colour)
    # Padding rows copy their nearest material edge to tolerate nearest/linear filtering.
    atlas.paste(atlas.crop((0, 767, 1024, 768)).resize((1024, 32)), (0, 768))
    atlas.paste(atlas.crop((0, 800, 1024, 864)).resize((1024, 160), Image.Resampling.NEAREST), (0, 864))
    atlas.save(OUT, optimize=True)
    assert atlas.size == (1024, 1024)
    for i, colour in enumerate(SWATCHES):
        assert atlas.getpixel((i * 64 + 32, 832)) == colour
    print(f"Generated {OUT} ({OUT.stat().st_size:,} bytes); 1024x1024 RGB; swatches verified.")


if __name__ == "__main__":
    main()
