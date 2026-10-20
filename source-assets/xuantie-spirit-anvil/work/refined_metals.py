"""Original 8 px/unit forged-metal artwork for the Xuan Tie anvil.

The texture is intentionally quiet at object scale.  Broken cold and warm
facets, small hammer marks, and sparse edge wear resolve only at close range.
No shared image assets or postfiltering are used; every mark is pixel crisp.
"""
from __future__ import annotations

import math
import random
from pathlib import Path

from PIL import Image, ImageDraw


BASE = {
    "iron": (43, 46, 56),
    "iron_dark": (27, 30, 39),
    "steel": (83, 87, 100),
    "bronze": (199, 171, 137),
    "bronze_dark": (137, 113, 85),
    "socket": (10, 18, 31),
}


def _c(rgb, shade=0, warmth=0):
    return (max(0, min(255, round(rgb[0] + shade + warmth))),
            max(0, min(255, round(rgb[1] + shade))),
            max(0, min(255, round(rgb[2] + shade - warmth))), 255)


def _line(draw, xy, rgb, shade=0, warmth=0):
    draw.line(xy, fill=_c(rgb, shade, warmth))


def paint_metal(width: int, height: int, material: str, seed: int = 0) -> Image.Image:
    """Return an opaque RGBA pixel surface at native atlas resolution.

    Supports iron, iron_dark, steel, bronze, bronze_dark, and socket.  Narrow
    parts get only directional edge highlights, never miniature panel frames.
    """
    width, height = max(1, int(width)), max(1, int(height))
    if material not in BASE:
        raise ValueError(f"Unknown forged-metal material: {material}")
    rng = random.Random(int(seed) * 1607 + sum(map(ord, material)) * 397)
    base = BASE[material]
    copper = material.startswith("bronze")
    steel = material == "steel"
    socket = material == "socket"
    area = width * height
    im = Image.new("RGBA", (width, height), _c(base))
    d = ImageDraw.Draw(im)

    # Broad forging variations have very little contrast.  Each irregular
    # stepped facet merges into its neighbours without a visible tile grid.
    for _ in range(max(2, area // 24)):
        x, y = rng.randrange(width), rng.randrange(height)
        fw = rng.randint(2, 9 if not copper else 7)
        fh = rng.randint(2, 6)
        shade = rng.choice([-5, -3, -2, -1, 0, 1, 2, 3, 5])
        if socket:
            shade *= .4
        warmth = rng.choice([-1, 0, 0, 1])
        pts = [(x+1,y), (x+fw-1,y), (x+fw-1,y+1),
               (x+fw,y+1), (x+fw,y+fh-1), (x+fw-2,y+fh-1),
               (x+fw-2,y+fh), (x,y+fh), (x,y+1), (x+1,y+1)]
        d.polygon(pts, fill=_c(base, shade, warmth))

    # A second layer gives short, flat microfacets rather than camouflage.
    for _ in range(max(1, area // (20 if copper else 16))):
        x, y = rng.randrange(width), rng.randrange(height)
        fw, fh = rng.randint(1, 4), rng.randint(1, 3)
        shade = rng.choice([-6, -4, -2, 2, 4, 6])
        if socket:
            shade *= .4
        d.rectangle((x, y, x+fw-1, y+fh-1), fill=_c(base, shade))

    # Subpixel-scale forge grain, sparse enough not to turn into noisy rock.
    px = im.load()
    for y in range(height):
        for x in range(width):
            if rng.random() < .16:
                px[x,y] = _c(px[x,y][:3], rng.choice([-2,-1,1,2]))

    if socket:
        # Recessed channel walls: a blue-steel reflection on the inner lip.
        if min(width,height) >= 3:
            _line(d, (0,0,width-1,0), base, 7)
            _line(d, (0,1,0,height-1), base, 3)
        return im

    if not copper:
        # Tiny flattened forging dents have a dark upper lip and a shorter
        # reflected lower lip.  They are isolated, with no grid or full boxes.
        if min(width,height) >= 6:
            for _ in range(area // 95):
                x = rng.randint(2, width-4)
                y = rng.randint(2, height-3)
                n = rng.choice([2,2,3,4])
                _line(d, (x,y,min(width-2,x+n),y), base, -8)
                _line(d, (x+1,y+1,min(width-2,x+n-1),y+1), base, -3)
                if rng.random() < .75:
                    _line(d, (x+1,y+2,min(width-2,x+n-1),y+2), base, 5)
            # Light interrupted abrasions suggest a used working surface.
            for _ in range(area // (240 if steel else 360)):
                x, y = rng.randint(1,width-3), rng.randint(1,height-2)
                n = rng.randint(2,4)
                _line(d, (x,y,min(width-2,x+n),y), base, 13 if steel else 10)
                if n >= 4 and y+1 < height:
                    d.point((min(width-2,x+n),y+1), fill=_c(base,5))
    else:
        # Hand-finished brass gets straight, incomplete brushing and a few
        # paired scratches.  These do not form a border around every part.
        if min(width,height) >= 5:
            for _ in range(max(1,area // 90)):
                x, y = rng.randint(1,width-3), rng.randint(1,height-2)
                n = min(width-2-x,rng.randint(2,6))
                _line(d, (x,y,x+n,y), base, rng.choice([-9,-6,7,10]))
                if rng.random() < .28 and y+1 < height-1:
                    _line(d, (x+1,y+1,x+n,y+1), base, 9)
        # A restrained join belongs only to a sufficiently broad plate.
        if min(width,height) >= 12 and max(width,height) >= 26:
            if width >= height:
                x = max(6,min(width-6,int(width * rng.uniform(.31,.69))))
                ya,yb = 3,height-4
                _line(d, (x,ya,x,yb), base,-22)
                _line(d, (x+1,ya+1,x+1,yb), base,12)
            else:
                y = max(6,min(height-6,int(height * rng.uniform(.31,.69))))
                _line(d, (3,y,width-4,y), base,-22)
                _line(d, (4,y+1,width-4,y+1), base,12)
        # Single small peened fasteners are restricted to broad fittings.
        # They never appear on narrow strips or repeat at all four corners.
        if min(width,height) >= 10 and area >= 225 and rng.random() < .32:
            x = width//2 if width < 19 else rng.choice([5,width-6])
            y = height//2 if height < 19 else rng.choice([5,height-6])
            d.rectangle((x-2,y-1,x+2,y+1),fill=_c(base,-28))
            d.rectangle((x-1,y-2,x+1,y+2),fill=_c(base,-28))
            d.rectangle((x-1,y-1,x+1,y+1),fill=_c(base,-4))
            _line(d,(x-1,y-1,x+1,y-1),base,35)
            d.point((x-1,y),fill=_c(base,23))
            d.point((x,y+1),fill=_c(base,-15))

    # A directional bevel is strongest on copper.  Iron has a narrow broken
    # polished arris.  Low-contrast bottom shading avoids a framed-box look.
    if min(width,height) >= 2:
        top = 31 if copper else (19 if steel else 10)
        left = 17 if copper else (9 if steel else 4)
        _line(d,(0,0,width-1,0),base,top)
        _line(d,(0,1,0,height-1),base,left)
        if min(width,height) >= 7:
            _line(d,(1,1,width-2,1),base,15 if copper else 4)
            if copper:
                _line(d,(1,2,1,height-2),base,6)
        _line(d,(1,height-1,width-1,height-1),base,-14 if copper else -6)
        _line(d,(width-1,1,width-1,height-2),base,-8 if copper else -3)
        # Chips interrupt the bevel with localized brighter exposed metal.
        for _ in range(max(0,width // 11)):
            x = rng.randint(1,width-2) if width > 2 else 0
            n = rng.randint(1,2)
            _line(d,(x,0,min(width-1,x+n),0),base,top+13)
            if height > 3 and rng.random() < .4:
                d.point((x,1),fill=_c(base,top-3))
    return im


if __name__ == "__main__":
    swatches = [("iron",192,56),("iron_dark",128,40),("steel",144,12),
                ("bronze",32,20),("bronze",14,46),("bronze_dark",40,16),
                ("socket",24,48)]
    sheet = Image.new("RGBA", (820,650), (24,29,36,255))
    ds = ImageDraw.Draw(sheet)
    positions=[(22,32),(22,296),(22,514),(560,310),(710,310),(560,448),(664,32)]
    for i,((mat,w,h),(x,y)) in enumerate(zip(swatches,positions)):
        ds.text((x,y-18),f"{mat}  {w} x {h}",fill=(190,203,220))
        tile=paint_metal(w,h,mat,337+i*41)
        sheet.alpha_composite(tile.resize((w*3,h*3),Image.Resampling.NEAREST),(x,y))
    out = Path(__file__).with_name("refined-metals-test.png")
    sheet.save(out)
    print(out)
