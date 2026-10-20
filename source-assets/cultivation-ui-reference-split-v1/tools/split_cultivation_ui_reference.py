"""Deterministic extraction of the user-approved raster reference, no repainting.

Keep source-resolution cuts, conservative colour-matted icons, native GUI variants,
and explicit limitations. No runtime resource is overwritten by this script.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import math
import shutil
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_SOURCE = Path(r"C:\Users\NOSTZE~1\AppData\Local\Temp\codex-clipboard-12976004-68a9-4600-8445-3b31c0391caa.png")
DEFAULT_OUT = ROOT / "source-assets" / "cultivation-ui-reference-split-v1"

REGIONS = {
    "window": (18, 16, 1655, 922),
    "header": (20, 18, 1652, 92),
    "navigation_panel": (20, 95, 275, 919),
    "main_panel": (280, 96, 1169, 918),
    "overview_panel": (1179, 96, 1652, 919),
    "status_panel": (1195, 752, 1633, 901),
    "player_viewport": (418, 191, 676, 500),
    "navigation_selected": (25, 136, 270, 230),
    "slot_empty_crafting": (831, 243, 909, 321),
    "slot_empty_inventory": (313, 573, 393, 651),
    "slot_empty_hotbar": (312, 825, 394, 899),
    "close_button": (1586, 23, 1649, 86),
    "crafting_arrow": (1000, 311, 1039, 346),
    "bar_cultivation_full": (1283, 323, 1624, 346),
    "bar_mana_full": (1283, 412, 1624, 436),
    "rule_overview": (1207, 167, 1627, 179),
    "rule_navigation": (35, 327, 250, 339),
    "rule_equipment": (398, 136, 608, 148),
    "rule_crafting": (908, 210, 1114, 222),
    "rule_inventory": (396, 541, 1146, 558),
    "rule_hotbar": (300, 804, 1139, 822),
    "rule_vertical": (798, 195, 811, 514),
    "header_left_rule": (560, 52, 717, 67),
    "header_right_rule": (957, 51, 1114, 66),
    "landscape_navigation": (23, 748, 272, 910),
    "landscape_player": (425, 199, 669, 490),
}

NAVIGATION = {
    "inventory": (58, 152, 114, 216),
    "vision": (56, 250, 113, 316),
    "arts": (55, 350, 116, 412),
    "sword": (57, 449, 113, 517),
    "advancements": (54, 560, 117, 622),
    "settings": (54, 665, 116, 729),
}
SUMMARY = {
    "realm": (1207, 194, 1270, 255),
    "cultivation": (1207, 277, 1270, 345),
    "mana": (1207, 364, 1265, 434),
    "physique": (1212, 485, 1265, 543),
    "heart_method": (1212, 551, 1266, 609),
    "domain": (1212, 620, 1267, 678),
    "exotic_fire": (1212, 685, 1265, 742),
    "dao_heart": (1220, 765, 1257, 804),
    "heart_demon": (1220, 809, 1257, 848),
    "tribulation": (1227, 852, 1257, 887),
}
EQUIPMENT = {
    "helmet": (335, 192, 375, 234),
    "chest": (334, 276, 376, 322),
    "legs": (335, 363, 376, 408),
    "boots": (331, 449, 379, 494),
    "sword": (712, 220, 761, 273),
    "bottle": (721, 304, 754, 357),
    "ring": (715, 395, 762, 445),
}
LABELS = {
    "brand": (106, 34, 292, 81),
    "page_inventory": (776, 27, 899, 83),
    "realm_header": (1393, 39, 1559, 78),
    "section_equipment": (310, 119, 381, 160),
    "section_crafting": (828, 196, 892, 235),
    "section_recipes": (829, 414, 893, 448),
    "section_inventory": (310, 530, 379, 567),
    "section_overview": (1204, 115, 1356, 156),
}


def alpha_matte(image: Image.Image, family: str) -> Image.Image:
    """Conservative colour key. A single composited image has no recoverable true alpha.

    Gold is separated by chroma (R>B, G>B), cyan by G/B>R. Uniform light-gold
    summary icons naturally pass this key too. Preserve original RGB for review.
    """
    result = image.convert("RGBA")
    muted_denominator = 32
    if family == "muted_gold":
        strengths = sorted(min(r-b,g-b)-7 for r,g,b,_ in list(result.get_flattened_data()) if r>70 and g>58 and min(r-b,g-b)>7)
        muted_denominator = max(6, strengths[round((len(strengths)-1)*0.95)]) if strengths else 32
    pixels = result.load()
    for y in range(result.height):
        for x in range(result.width):
            r, g, b, _ = pixels[x, y]
            if family == "cyan":
                strength = min(g - r, b - r)
                a = round(255 * max(0, min(1, (strength - 15) / 75))) if max(g, b) > 110 else 0
            elif family == "dark":
                a = round(255 * max(0, min(1, (92 - max(r, g, b)) / 55)))
            else:
                strength = min(r - b, g - b)
                a = round(255 * max(0, min(1, (strength - 7) / (muted_denominator if family == "muted_gold" else 32)))) if r > 70 and g > 58 else 0
            pixels[x, y] = (r, g, b, a)
    return result


def fit_icon(image: Image.Image, size: int) -> Image.Image:
    result = Image.new("RGBA", (size, size))
    occupied = image.getbbox()
    if occupied:
        artwork = image.crop(occupied)
        ratio = (size - 2) / max(artwork.size)
        artwork = artwork.resize((max(1, round(artwork.width * ratio)), max(1, round(artwork.height * ratio))), Image.Resampling.LANCZOS)
        result.alpha_composite(artwork, ((size-artwork.width)//2, (size-artwork.height)//2))
    return result


def nine_slice(image: Image.Image, source_edge: int, size: tuple[int, int], edge: int,
               center: Image.Image | None = None) -> Image.Image:
    w, h = image.size
    ow, oh = size
    xs, ys = [0, source_edge, w-source_edge, w], [0, source_edge, h-source_edge, h]
    tx, ty = [0, edge, ow-edge, ow], [0, edge, oh-edge, oh]
    result = Image.new("RGBA", size)
    for row in range(3):
        for col in range(3):
            patch = center if center is not None and row == col == 1 else image.crop((xs[col], ys[row], xs[col+1], ys[row+1]))
            target_size = (tx[col+1]-tx[col], ty[row+1]-ty[row])
            if min(target_size) > 0:
                result.paste(patch.resize(target_size, Image.Resampling.LANCZOS), (tx[col], ty[row]))
    return result


def font(size: int):
    for name in [r"C:\Windows\Fonts\msyh.ttc", r"C:\Windows\Fonts\arial.ttf"]:
        if Path(name).exists():
            return ImageFont.truetype(name, size)
    return ImageFont.load_default()


def build(source_path: Path, out: Path):
    source = Image.open(source_path).convert("RGBA")
    if source.size != (1672, 941):
        raise ValueError(f"Reference must be 1672x941, got {source.size}; coordinates must be reviewed.")
    out.mkdir(parents=True, exist_ok=True)
    source_copy = out / "reference-original.png"
    if not source_copy.exists() or source_copy.read_bytes() != source_path.read_bytes():
        shutil.copy2(source_path, source_copy)
    entries = []

    def save(name, image, box=None, kind="raw_crop", usage="reference_only", **extra):
        target = out / (name + ".png")
        target.parent.mkdir(parents=True, exist_ok=True)
        image.convert("RGBA").save(target)
        entry = {"path": target.relative_to(out).as_posix(), "size": list(image.size), "kind": kind, "usage": usage}
        if box:
            entry["source_box_xyxy_exclusive"] = list(box)
        entry.update(extra)
        entries.append(entry)
        return image

    for name, box in REGIONS.items():
        save("raw/components/" + name, source.crop(box), box, contains_baked_content=name in {
            "window", "header", "navigation_panel", "main_panel", "overview_panel", "status_panel", "navigation_selected"})

    for group, mapping in [("navigation", NAVIGATION), ("summary", SUMMARY), ("equipment", EQUIPMENT)]:
        for name, box in mapping.items():
            crop = source.crop(box)
            save(f"raw/icons/{group}/{name}", crop, box)
            family = "muted_gold" if group == "equipment" else "cyan" if group == "summary" and name == "mana" else "gold"
            matte = alpha_matte(crop, family)
            save(f"isolated/icons/{group}/{name}", matte, box, kind="colour_matte", usage="candidate_review_required", alpha_is_estimated=True, alpha_method="adaptive_chroma" if family=="muted_gold" else "fixed_chroma")
            for size in (16, 32):
                save(f"gui-{size}/icons/{group}/{name}", fit_icon(matte, size), box, kind="resampled_matte", usage="candidate_review_required", alpha_is_estimated=True)

    for name, box in LABELS.items():
        save("raw/labels/" + name, source.crop(box), box, contains_baked_text=True)
    for i, (name, box) in enumerate(NAVIGATION.items()):
        text_box = (145, [154, 256, 356, 462, 570, 674][i], 222, [210, 306, 407, 512, 615, 721][i])
        save(f"raw/labels/navigation_{name}", source.crop(text_box), text_box, contains_baked_text=True)
    logo_box=(35,28,86,85)
    logo=source.crop(logo_box)
    save("raw/icons/brand",logo,logo_box)
    logo_matte=alpha_matte(logo,"gold")
    save("isolated/icons/brand",logo_matte,logo_box,kind="colour_matte",usage="candidate_review_required",alpha_is_estimated=True)
    for size in (16,32):
        save(f"gui-{size}/icons/brand",fit_icon(logo_matte,size),logo_box,kind="resampled_matte",usage="candidate_review_required",alpha_is_estimated=True)

    # Actual drawn reference has ten columns. Preserve it in the source coordinate map;
    # do not accidentally turn that into ten functional inventory columns.
    slot_x = [313, 394, 475, 556, 637, 718, 799, 880, 961, 1042]
    reference_slots = []
    for row, (y, h) in enumerate([(573,78), (652,72), (726,73), (825,74)]):
        for col, x in enumerate(slot_x):
            box = (x, y, x+80, y+h)
            reference_slots.append({"section": "hotbar" if row == 3 else "inventory", "row": row if row < 3 else 0, "column": col, "source_box_xyxy_exclusive": list(box)})

    # Clean samples are the only source for repeatable surfaces. No text is in them.
    samples = {
        "paper": (532, 102, 596, 131),
        "dark": (1471, 854, 1503, 889),
        "selected": (116, 153, 144, 221),
    }
    for name, box in samples.items():
        sample = source.crop(box)
        save("raw/background_samples/"+name, sample, box)
        save("components/background/"+name+"_tile", sample.resize((64,64), Image.Resampling.LANCZOS), box,
             kind="sample_resized", usage="candidate_review_required", note="A sampled tile, not reconstructed concealed artwork; seam review needed.")

    # Empty source slot provides a label-free, icon-free component.
    slot_box = REGIONS["slot_empty_crafting"]
    slot = source.crop(slot_box)
    save("components/slot/slot_empty", slot, slot_box, usage="reusable_crop")
    frame = slot.copy()
    ImageDraw.Draw(frame).rectangle((8,8,slot.width-9,slot.height-9), fill=(0,0,0,0))
    save("components/slot/slot_frame", frame, slot_box, kind="interior_mask", usage="reusable_crop", source_edge=8)
    for size in (18,32,64):
        edge = {18:2,32:4,64:7}[size]
        save(f"gui-{size}/slot/slot_empty", nine_slice(slot, 8, (size,size), edge), slot_box,
             kind="nine_slice_resample", usage="candidate_review_required", edge=edge)
        save(f"gui-{size}/slot/slot_frame", nine_slice(frame, 8, (size,size), edge), slot_box,
             kind="nine_slice_resample", usage="candidate_review_required", edge=edge)

    # Frames are exported separately from baked titles/values. Interior alpha avoids
    # placing those raster labels on top of text rendered by Minecraft.
    for name, key, raw_edge, target_size, edge in [
        ("window", "window", 24, (32,32), 6),
        ("navigation", "navigation_panel", 10, (32,32), 4),
        ("main", "main_panel", 12, (32,32), 4),
        ("overview", "overview_panel", 12, (32,32), 4),
        ("player", "player_viewport", 10, (32,32), 4),
        ("status", "status_panel", 10, (32,32), 4),
        ("navigation_selected", "navigation_selected", 12, (32,24), 4),
    ]:
        box = REGIONS[key]
        crop = source.crop(box)
        border = alpha_matte(crop, "gold")
        ImageDraw.Draw(border).rectangle((raw_edge,raw_edge,border.width-raw_edge-1,border.height-raw_edge-1), fill=(0,0,0,0))
        if name=="window":
            # Keep large corner ornaments but only the actual outside line on the
            # long edges; otherwise the wide band would capture central paper.
            pen=ImageDraw.Draw(border)
            pen.rectangle((raw_edge,6,border.width-raw_edge-1,raw_edge-1),fill=(0,0,0,0))
            pen.rectangle((raw_edge,border.height-raw_edge,border.width-raw_edge-1,border.height-7),fill=(0,0,0,0))
            pen.rectangle((6,raw_edge,raw_edge-1,border.height-raw_edge-1),fill=(0,0,0,0))
            pen.rectangle((border.width-raw_edge,raw_edge,border.width-7,border.height-raw_edge-1),fill=(0,0,0,0))
        save("components/frame/"+name+"_frame", border, box, kind="frame_band_colour_matte",
             usage="candidate_review_required", alpha_is_estimated=True, source_edge=raw_edge)
        save("gui-32/frame/"+name+"_frame", nine_slice(border,raw_edge,target_size,edge),box,
             kind="nine_slice_resample",usage="candidate_review_required",edge=edge)
        if name == "navigation_selected":
            background = source.crop(samples["selected"])
            surface = nine_slice(crop,raw_edge,target_size,edge,background)
            save("gui-32/button/selected",surface,box,kind="nine_slice_clean_center",usage="candidate_review_required",edge=edge,
                 note="Original border; text-free center sampled from selected button. Only selected state appears in the reference.")

    for name in ["rule_overview","rule_navigation","header_left_rule","header_right_rule"]:
        box = REGIONS[name]
        save("components/rule/"+name,alpha_matte(source.crop(box),"gold"),box,kind="colour_matte",usage="candidate_review_required",alpha_is_estimated=True)
    for name in ["rule_equipment","rule_crafting","rule_inventory","rule_hotbar","rule_vertical","crafting_arrow"]:
        box = REGIONS[name]
        save("components/rule/"+name,alpha_matte(source.crop(box),"dark"),box,kind="colour_matte",usage="candidate_review_required",alpha_is_estimated=True)

    # Separate full-meter crops and fill strips; labels/numbers remain dynamic.
    for name in ("cultivation", "mana"):
        box = REGIONS["bar_"+name+"_full"]
        bar = source.crop(box)
        save("components/bar/"+name+"_full",bar,box,usage="reusable_crop")
        e=8
        save("gui-32/bar/"+name+"_full",nine_slice(bar,e,(128,8),3),box,kind="nine_slice_resample",usage="candidate_review_required",edge=3)
        fill_box=(box[0]+13,box[1]+5,box[2]-13,box[3]-5)
        save("components/bar/"+name+"_fill",source.crop(fill_box),fill_box,usage="reusable_crop")
        shell=bar.copy()
        ImageDraw.Draw(shell).rounded_rectangle((5,4,bar.width-6,bar.height-5),radius=6,fill=(0,0,0,0))
        save("components/bar/"+name+"_frame",shell,box,kind="interior_mask",usage="candidate_review_required")

    box=REGIONS["close_button"]
    close=alpha_matte(source.crop(box),"gold")
    save("components/button/close",close,box,kind="colour_matte",usage="candidate_review_required",alpha_is_estimated=True)
    save("gui-16/button/close",close.resize((16,16),Image.Resampling.LANCZOS),box,kind="resampled_matte",usage="candidate_review_required")

    # Atlases follow the existing mod's 6x16 navigation and summary strip contracts.
    for group,mapping in [("navigation",NAVIGATION),("summary",dict(list(SUMMARY.items())[:6])),("summary_extended",SUMMARY),("equipment",EQUIPMENT)]:
        source_group="summary" if group=="summary_extended" else group
        atlas=Image.new("RGBA",(16*len(mapping),16))
        for i,name in enumerate(mapping):
            atlas.paste(Image.open(out/f"gui-16/icons/{source_group}/{name}.png"),(i*16,0))
        save("atlases/"+group+"_16",atlas,kind="atlas",usage="candidate_review_required",order=list(mapping))

    manifest={
        "schema":"wuxingxiantu-ui-split/1", "source":"reference-original.png", "source_size":list(source.size),
        "source_sha256":hashlib.sha256(source_path.read_bytes()).hexdigest(),
        "coordinate_convention":"top-left origin, x1/y1 inclusive, x2/y2 exclusive",
        "target":{"minecraft":"1.20.1","loader":"Forge","native_item_pixels":16,"native_slot_pixels":18,"inventory_columns":9,"inventory_rows":3,"hotbar_slots":9},
        "reference_inventory_columns":10,"reference_slots":reference_slots,
        "limitations":[
            "The source is a flattened raster; alpha mattes are estimates, not original layer alpha.",
            "Raw full-panel cuts contain baked labels and controls; they are reference-only.",
            "Covered mountain/paper artwork cannot be recovered by cropping. No content-aware repaint was performed.",
            "Only selected navigation state is provided; normal/hover/pressed/disabled must be defined at integration.",
            "Screenshot equipment bottle/ring semantics differ from current robe/sword/offhand slots. Crops preserve source, not new gameplay.",
            "Extra overview fields need project data binding verified later; no gameplay or rendering code changed.",
            "Source draws ten columns with differing slot proportions; integration must use nine columns and uniform native slot geometry.",
            "Generated font labels are reference-only; runtime font and numeric values must be rendered dynamically.",
        ],
        "assets":entries,
    }
    (out/"manifest.json").write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding="utf-8")
    (out/"README.md").write_text("""# 五行仙途 UI 素材拆分 v1

源图为用户在 2026-10-07 指定的 1672×941 PNG；仅裁切、颜色分离及尺寸适配，不重新生成画面。

## 目录

- `raw/components`：原分辨率组件和面板。整面板含固化文字，只作对照，不能当运行时背景直接铺。
- `raw/icons`、`raw/labels`：逐项原始裁切，保留源像素。
- `isolated/icons`：透明图标候选，采用保守颜色键；单张合成图不能恢复原始透明度。
- `components`：独立槽框、边框、分隔线、进度条、无字背景样本。
- `gui-16`、`gui-18`、`gui-32`、`gui-64`：游戏图标、18px 槽位、九宫格纹理候选。缩小的细线需游戏内复核。
- `atlases`：导航与概览 96×16 图标条，以及扩展概览和装备条。顺序在 manifest 中。
- `manifest.json`：源坐标、尺寸、用途、九宫格边缘、图标顺序及限制。
- `contact-sheet.png`：透明候选和组件总览。
- `reference-map.png`：源图切片位置标注。
- `native-size-preview.png`：16px 图标条及原生槽位／九宫格的整数倍预览。

## 后续接入条件

1. 原图每行 10 格；现有原版容器背包为 9×3＋9。按现有容器实装需重排，不能照原图增加槽位。
2. `CultivationUi` 使用手动九宫格绘制。可沿用 texture 路径和切片机制，不使用 1.20.2+ GUI sprite 元数据。
3. 所有运行时文字、数值、玩家实体、物品和物品状态条须独立绘制；不要粘贴带字整图。
4. 仅选中按钮有源图状态。其他状态、遮挡下完整山水背景和字体资源不属于裁切可恢复内容。
5. 原图瓶子／戒指占位图与项目法衣／御剑／副手不一致；先保留素材，在接入时选择正确语义。
6. 右侧异火、道心、心魔、渡劫信息目前不在主界面概览绘制，接入时核实真实数据。

重建：运行 `tools/split_cultivation_ui_reference.py --source <源PNG> --out <新目录>`。
本包没有覆盖 `src/main/resources`，没有修改界面 Java，也没有启动游戏。
""",encoding="utf-8")

    # Human-readable QA sheet on a checkerboard (not a runtime asset).
    preview_entries=[e for e in entries if e["path"].startswith("isolated/") or e["path"].startswith("gui-32/frame/") or e["path"].startswith("components/slot/") or e["path"].startswith("components/bar/") or e["path"].startswith("gui-32/button/") or e["path"].startswith("atlases/")]
    cols,cw,ch=5,240,156
    sheet=Image.new("RGBA",(cols*cw,56+math.ceil(len(preview_entries)/cols)*ch),(234,233,225,255))
    d=ImageDraw.Draw(sheet)
    d.text((18,14),"五行仙途 · 素材切片与透明候选",font=font(22),fill="#253b32")
    for i,entry in enumerate(preview_entries):
        x=(i%cols)*cw;y=56+(i//cols)*ch
        for yy in range(y+4,y+110,10):
            for xx in range(x+8,x+cw-8,10):
                d.rectangle((xx,yy,xx+9,yy+9),fill="#334238" if ((xx-x)//10+(yy-y)//10)%2 else "#44544b")
        im=Image.open(out/entry["path"]).convert("RGBA")
        im.thumbnail((cw-32,94),Image.Resampling.NEAREST)
        sheet.alpha_composite(im,(x+(cw-im.width)//2,y+8+(94-im.height)//2))
        label=entry["path"].split("/")[-1]
        d.text((x+10,y+115),label,font=font(13),fill="#24342d")
        d.text((x+10,y+134),f'{entry["size"][0]}×{entry["size"][1]}',font=font(12),fill="#526055")
    sheet.convert("RGB").save(out/"contact-sheet.png")
    annotated=source.copy();d=ImageDraw.Draw(annotated)
    for i,(name,box) in enumerate(REGIONS.items()):
        if name in {"window","header","navigation_panel","main_panel","overview_panel","status_panel"}:
            continue
        d.rectangle((box[0],box[1],box[2]-1,box[3]-1),outline="#ff895f",width=2)
        d.rectangle((box[0],box[1]-13,box[0]+20,box[1]+2),fill="#26352d")
        d.text((box[0]+2,box[1]-13),str(i+1),font=font(11),fill="#ffffff")
    annotated.convert("RGB").save(out/"reference-map.png")
    (out/"reference-map-key.json").write_text(json.dumps({i+1:{"name":name,"source_box":list(box)} for i,(name,box) in enumerate(REGIONS.items())},indent=2),encoding="utf-8")

    # Show native texture candidates at an integer display scale, including actual
    # atlas widths, to expose thin-line loss rather than hiding it in large crops.
    native=Image.new("RGBA",(960,640),(38,47,43,255))
    pen=ImageDraw.Draw(native)
    pen.text((20,15),"原生 GUI 尺寸候选 · 4 倍最近邻显示",font=font(21),fill="#e6dec3")
    for row,name in enumerate(["navigation","summary","summary_extended","equipment"]):
        im=Image.open(out/f"atlases/{name}_16.png").convert("RGBA")
        y=65+row*100
        native.alpha_composite(im.resize((im.width*4,64),Image.Resampling.NEAREST),(20,y))
        pen.text((700,y+18),name+" / 16px",font=font(15),fill="#c3c0ad")
    x=20
    for name,size,rel in [
        ("槽位 / 18px",18,"gui-18/slot/slot_empty.png"),
        ("选中 / 50×24",32,"gui-32/button/selected.png"),
        ("人物框 / 54×72",32,"gui-32/frame/player_frame.png"),
    ]:
        im=Image.open(out/rel).convert("RGBA")
        if "selected" in rel:
            im=nine_slice(im,4,(50,24),4)
        elif "player" in rel:
            im=nine_slice(im,4,(54,72),4)
        scale=4 if size==18 else 2
        native.alpha_composite(im.resize((im.width*scale,im.height*scale),Image.Resampling.NEAREST),(x,470))
        pen.text((x,445),name,font=font(14),fill="#c3c0ad")
        x+=180
    native.convert("RGB").save(out/"native-size-preview.png")

    # Verify actual bytes, dimensions, alpha and transparent centers, not only existence.
    for entry in entries:
        path=out/entry["path"]
        with Image.open(path) as check:
            check.load()
            assert check.mode=="RGBA", path
            assert list(check.size)==entry["size"],path
            if "source_box_xyxy_exclusive" in entry:
                x1,y1,x2,y2=entry["source_box_xyxy_exclusive"]
                assert 0<=x1<x2<=source.width and 0<=y1<y2<=source.height,path
                if entry["kind"]=="raw_crop":
                    assert ImageChops.difference(check,source.crop((x1,y1,x2,y2))).getbbox() is None,f"Raw pixels differ: {path}"
            if entry["kind"] in {"colour_matte","resampled_matte","frame_band_colour_matte"}:
                assert check.getchannel("A").getextrema()[1]>0, f"Empty extraction: {path}"
                assert check.getchannel("A").getextrema()[0]==0, f"Opaque matte: {path}"
    for name in ["window","navigation","main","overview","player","status","navigation_selected"]:
        check=Image.open(out/f"gui-32/frame/{name}_frame.png")
        assert check.getpixel((check.width//2,check.height//2))[3]==0,name
    report={"passed":True,"png_assets":len(entries),"reference_slots":len(reference_slots),"navigation_atlas_order":list(NAVIGATION),"runtime_resources_changed":False}
    (out/"validation.json").write_text(json.dumps(report,indent=2),encoding="utf-8")
    (out/"tools").mkdir(exist_ok=True)
    shutil.copy2(Path(__file__),out/"tools"/Path(__file__).name)
    archive=shutil.make_archive(str(out),"zip",root_dir=out)
    print(json.dumps({"output":str(out),"archive":archive,**report},ensure_ascii=False,indent=2))


if __name__ == "__main__":
    parser=argparse.ArgumentParser()
    parser.add_argument("--source",type=Path,default=DEFAULT_SOURCE)
    parser.add_argument("--out",type=Path,default=DEFAULT_OUT)
    args=parser.parse_args()
    build(args.source,args.out)
