"""Encode actual Minecraft framebuffer PNGs, without synthesizing or retouching frames."""
from pathlib import Path
import argparse
from PIL import Image

parser = argparse.ArgumentParser()
parser.add_argument("--frames", type=Path, required=True)
parser.add_argument("--output", type=Path, required=True)
args = parser.parse_args()
files = sorted(args.frames.glob("frame-*.png"))
if len(files) < 10:
    raise SystemExit(f"Need at least 10 actual frames, found {len(files)}")
frames = []
for path in files:
    with Image.open(path) as source:
        # Smaller delivery artifact; geometry/content are the captured game frames.
        frame = source.convert("RGB")
        frame.thumbnail((800, 450), Image.Resampling.LANCZOS)
        frames.append(frame)
args.output.parent.mkdir(parents=True, exist_ok=True)
if args.output.suffix.lower() == ".webp":
    frames[0].save(args.output, save_all=True, append_images=frames[1:], duration=200,
                   loop=0, quality=88, method=4)
else:
    frames[0].save(args.output, save_all=True, append_images=frames[1:], duration=200,
                   loop=0, optimize=False)
print(f"Encoded {len(frames)} actual frames at 5 FPS: {args.output.resolve()}")
