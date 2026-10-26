"""Rebuild the plain expanded/rubbing frame without leaking interior dividers."""
from pathlib import Path
from PIL import Image, ImageDraw

root = Path(__file__).resolve().parents[1]
art = root / 'src/main/resources/assets/wuxingxiantu/textures/gui/talisman_v12'
image = Image.open(art / 'frame.png').convert('RGBA')
ImageDraw.Draw(image).polygon([(34, 27), (1372, 27), (1382, 37), (1382, 1077),
                               (1372, 1086), (34, 1086), (24, 1076), (24, 37)],
                              fill=(3, 17, 24, 255))
image.save(art / 'expanded_frame.png', compress_level=9)
