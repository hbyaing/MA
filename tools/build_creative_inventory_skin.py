"""Compose native creative UVs from the existing survival inventory artwork."""
from pathlib import Path
from PIL import Image, ImageEnhance, ImageOps

ROOT = Path(__file__).resolve().parents[1]
GUI = ROOT / "src/main/resources/assets/wuxingxiantu/textures/gui"
OUT = GUI / "creative"
OUT.mkdir(parents=True, exist_ok=True)


def asset(name, size):
    return Image.open(GUI / name).convert("RGBA").resize(size, Image.Resampling.LANCZOS)


def frame(image, x, y, width, height, corner=5, edge=1):
    """Use the same clean outer rail and mirrored corner as CultivationOuterFrame."""
    top = asset("wuxia/outer_frame_v2/top.png", (width - 2 * corner, edge))
    left = asset("wuxia/outer_frame_v2/left.png", (edge, height - 2 * corner))
    image.alpha_composite(top, (x + corner, y))
    image.alpha_composite(ImageOps.flip(top), (x + corner, y + height - edge))
    image.alpha_composite(left, (x, y + corner))
    image.alpha_composite(ImageOps.mirror(left), (x + width - edge, y + corner))
    tile = Image.open(GUI / "wuxia/outer_frame_v2/tl.png").convert("RGBA")
    tile.paste((0, 0, 0, 0), (15, 15, 39, 39))
    tile = tile.resize((corner, corner), Image.Resampling.LANCZOS)
    for flip_x, flip_y in ((False, False), (True, False), (False, True), (True, True)):
        piece = ImageOps.mirror(tile) if flip_x else tile
        piece = ImageOps.flip(piece) if flip_y else piece
        image.alpha_composite(piece, (x + (width - corner if flip_x else 0), y + (height - corner if flip_y else 0)))


panel = Image.new("RGBA", (256, 256))
panel.alpha_composite(asset("wuxia/reference/background/main.png", (195, 136)), (0, 0))
frame(panel, 0, 0, 195, 136, corner=6, edge=2)
slot = asset("wuxia/reference/slot/empty.png", (18, 18))
for row in range(5):
    for column in range(9):
        # Native slot item origin is one pixel inside this 18px tile.
        panel.alpha_composite(slot, (8 + column * 18, 18 + row * 18))
for column in range(9):
    panel.alpha_composite(slot, (8 + column * 18, 112))
panel.alpha_composite(asset("wuxia/reference/borders/rule_ink.png", (162, 2)), (8, 108))
track = asset("wuxia/reference/background/navigation.png", (14, 112))
panel.alpha_composite(track, (174, 18))
frame(panel, 174, 18, 14, 112, corner=3)
panel.save(OUT / "panel.png")

# Vanilla/Forge reads seven 26x32 columns, four states at V=0/32/64/96,
# and the active/disabled scrollbar thumbs at U=232/244, V=0.
tabs = Image.new("RGBA", (256, 256))
for bottom in (False, True):
    for selected in (False, True):
        y = (64 if bottom else 0) + (32 if selected else 0)
        for column in range(7):
            tile = asset("wuxia/reference/background/main.png", (26, 32))
            if selected:
                tile = ImageEnhance.Color(tile).enhance(.75)
                overlay = Image.new("RGBA", tile.size, (190, 157, 75, 42))
                tile.alpha_composite(overlay)
            frame(tile, 0, 0, 26, 32, corner=4)
            tabs.alpha_composite(tile, (column * 26, y))
for disabled, x in ((False, 232), (True, 244)):
    thumb = asset("wuxia/reference/background/main.png", (12, 15))
    if disabled:
        thumb = ImageEnhance.Color(thumb).enhance(.1)
        thumb = ImageEnhance.Brightness(thumb).enhance(.7)
    frame(thumb, 0, 0, 12, 15, corner=3)
    tabs.alpha_composite(thumb, (x, 0))
tabs.save(OUT / "tabs.png")
for name in ("panel", "tabs"):
    (OUT / (name + ".png.mcmeta")).write_text('{"texture":{"blur":false,"clamp":true}}\n', encoding="utf-8")

if __name__ == "__main__":
    print("Creative skin: survival parchment, native 195x136 panel in 256x256 UVs; 54 slots, seven tab columns and both scroll thumbs")
