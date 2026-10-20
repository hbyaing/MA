"""Package the generated, visually reviewed clean plate as Minecraft GUI layers.

Only crops and resampling are performed here; hidden artwork was restored by the
built-in image-generation tool. Do not write runtime resources during preparation.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import shutil
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
SPLIT = ROOT / "source-assets" / "cultivation-ui-reference-split-v1"
OUT = ROOT / "source-assets" / "cultivation-ui-backgrounds-v1"
GENERATED = Path(r"C:\Users\Nostzephert\.codex\generated_images\01a10bd0-890e-7790-8dce-3da06cb25d4c\exec-c1399293-87c7-4731-a24b-20040abc61ca.png")

LAYERS = {
    # Keep the one-pixel strip at the boundary outside individual texture layers.
    "header": {"box": (20,18,1652,92), "size": (1024,48), "label":"顶部背景", "gui_example":(424,19)},
    "navigation": {"box": (21,96,277,916), "size":(128,416),"label":"左侧导航背景", "gui_example":(58,190)},
    "main": {"box":(280,96,1167,916), "size":(512,480),"label":"中央宣纸山水背景", "gui_example":(208,190)},
    "overview": {"box":(1171,96,1651,916), "size":(256,448),"label":"右侧修炼概览背景", "gui_example":(116,190)},
    "player": {"box":(430,204,663,484), "size":(128,160), "label":"角色视窗山水背景", "gui_example":(54,72), "source":"original"},
}


def font(size):
    return ImageFont.truetype(r"C:\Windows\Fonts\msyh.ttc",size)


def package(clean_path: Path, prompt_path: Path):
    clean = Image.open(clean_path).convert("RGBA")
    original = Image.open(SPLIT / "reference-original.png").convert("RGBA")
    if clean.size != original.size or clean.size != (1672,941):
        raise ValueError("Clean-plate boundaries must be visually re-verified for this image size.")
    OUT.mkdir(parents=True, exist_ok=True)
    shutil.copy2(clean_path,OUT/"clean-background-master.png")
    shutil.copy2(SPLIT/"reference-original.png",OUT/"reference-original.png")
    if prompt_path.resolve() != (OUT/"generation-prompt.txt").resolve():
        shutil.copy2(prompt_path,OUT/"generation-prompt.txt")
    assets=[]
    for name,spec in LAYERS.items():
        source=original if spec.get("source")=="original" else clean
        crop=source.crop(spec["box"])
        raw=OUT/"layers-original"/(name+".png")
        texture=OUT/"textures"/"background"/(name+".png")
        raw.parent.mkdir(parents=True,exist_ok=True)
        texture.parent.mkdir(parents=True,exist_ok=True)
        crop.save(raw)
        prepared=crop.resize(spec["size"],Image.Resampling.LANCZOS)
        prepared.save(texture)
        assets.append({
            "name":name,"label":spec["label"],"source":"reference-original.png" if source is original else "clean-background-master.png",
            "source_box_xyxy_exclusive":list(spec["box"]),"source_crop_size":list(crop.size),
            "original_layer":raw.relative_to(OUT).as_posix(),"texture":texture.relative_to(OUT).as_posix(),
            "texture_size":list(prepared.size),"example_gui_size":list(spec["gui_example"]),
            "opaque":True,"contains_baked_text":False,"contains_baked_widgets":False,"contains_frame":False,
            "render_mode":"draw once into panel; do not tile landscape", "nine_slice":False,
            "proposed_resource_location":"wuxingxiantu:textures/gui/cultivation_reference/background/"+name+".png",
            "provenance":"unchanged source crop" if source is original else "image_gen clean-plate restoration, then crop/resample",
            "sha256":hashlib.sha256(texture.read_bytes()).hexdigest(),
        })

    # Readback validation checks actual alpha and source coordinates; visual review
    # must separately confirm that the restored artwork has no labels or controls.
    for asset in assets:
        texture=Image.open(OUT/asset["texture"]).convert("RGBA")
        texture.load()
        assert list(texture.size)==asset["texture_size"]
        assert texture.getchannel("A").getextrema()==(255,255)
        source=original if asset["source"]=="reference-original.png" else clean
        original_crop=Image.open(OUT/asset["original_layer"]).convert("RGBA")
        difference=ImageChops.difference(original_crop,source.crop(asset["source_box_xyxy_exclusive"]))
        assert difference.getbbox() is None
        if asset["name"]=="main":
            assert min(texture.convert("RGB").resize((1,1)).getpixel((0,0)))>140
        elif asset["name"] in {"navigation","overview"}:
            assert max(texture.convert("RGB").resize((1,1)).getpixel((0,0)))<90

    # Recomposition uses original-size layers in original coordinates so no gaps,
    # text or slot placeholders are accidentally introduced into the new base.
    recomposed=Image.new("RGBA",clean.size,(5,16,22,255))
    for name in ["header","navigation","main","overview"]:
        box=LAYERS[name]["box"]
        recomposed.alpha_composite(Image.open(OUT/"layers-original"/(name+".png")).convert("RGBA"),(box[0],box[1]))
    recomposed.convert("RGB").save(OUT/"background-reassembled.png")

    # Compose a limited offline integration preview with source-extracted frames,
    # native 9x3+9 inventory geometry and icons. This is not a game screenshot.
    proof=recomposed.copy()
    frame=Image.open(SPLIT/"components/frame/window_frame.png").convert("RGBA")
    proof.alpha_composite(frame,(18,16))
    for frame_name,xy in [("navigation",(20,95)),("main",(280,96)),("overview",(1179,96))]:
        frame=Image.open(SPLIT/f"components/frame/{frame_name}_frame.png").convert("RGBA")
        proof.alpha_composite(frame,xy)
    native_slot=Image.open(SPLIT/"gui-18/slot/slot_empty.png").convert("RGBA")
    slot=native_slot.resize((72,72),Image.Resampling.NEAREST)
    for y in (572,652,732,824):
        for column in range(9):
            proof.alpha_composite(slot,(362+column*84,y))
    for i,name in enumerate(["inventory","vision","arts","sword","advancements","settings"]):
        icon=Image.open(SPLIT/f"gui-32/icons/navigation/{name}.png").convert("RGBA").resize((64,64),Image.Resampling.NEAREST)
        proof.alpha_composite(icon,(65,153+i*102))
    # The existing unoccluded inset painting is reused for the entity viewport.
    player=Image.open(OUT/"layers-original/player.png").convert("RGBA").resize((244,291),Image.Resampling.LANCZOS)
    proof.alpha_composite(player,(425,199))
    proof.alpha_composite(Image.open(SPLIT/"components/frame/player_frame.png").convert("RGBA"),(418,191))
    d=ImageDraw.Draw(proof)
    d.text((145,165),"背包",font=font(30),fill="#e5d3a5")
    for i,label in enumerate(["内视","功法","灵剑","成就","设置"],1):
        d.text((145,165+i*102),label,font=font(30),fill="#dedbcf")
    d.text((315,529),"行囊",font=font(32),fill="#20252b")
    d.text((1210,120),"修炼概览",font=font(36),fill="#e6debf")
    for i,(label,value) in enumerate([("境界","地仙 · 巅峰"),("修为","1亿 / 1亿"),("灵力","132万 / 132万"),("体质","九窍灵脉"),("心法","混元"),("当前领域","万雷领域"),("当前异火","幽蓝灵焰"),("道心","道心受损"),("心魔","无"),("可渡劫","否")]):
        y=205+i*65
        d.text((1230,y),label,font=font(25),fill="#dad8ce")
        d.text((1405,y),value,font=font(25),fill="#83cadc" if i in {2,3,4,6} else "#e2bc79")
    proof.convert("RGB").save(OUT/"offline-layer-compatibility-preview.png")

    sheet=Image.new("RGB",(1360,730),"#18242b")
    d=ImageDraw.Draw(sheet)
    d.text((20,14),"完整无字背景 · 五个独立分区",font=font(25),fill="#e4dbc3")
    panel_positions={"main":(20,75,600,550),"navigation":(640,75,165,550),"overview":(835,75,300,550),"player":(1165,75,165,240),"header":(640,650,690,33)}
    for name,(x,y,w,h) in panel_positions.items():
        im=Image.open(OUT/"textures/background"/(name+".png")).convert("RGB")
        im.thumbnail((w,h),Image.Resampling.LANCZOS)
        sheet.paste(im,(x,y))
        d.text((x,y-24),LAYERS[name]["label"],font=font(17),fill="#e3d5af")
    sheet.save(OUT/"background-contact-sheet.png")

    manifest={"schema":"wuxingxiantu-ui-backgrounds/1","generator":"built-in image_gen","generated_artwork":"clean-background-master.png",
        "generation_prompt":"generation-prompt.txt","reference_sha256":hashlib.sha256((SPLIT/"reference-original.png").read_bytes()).hexdigest(),
        "generated_sha256":hashlib.sha256(clean_path.read_bytes()).hexdigest(),"assets":assets,
        "review":{"manual_artwork_review":"No text, icons, slots, progress bars, dividers or gold frames in generated master; restored hidden landscape differs from unknowable original layers.",
            "offline_preview_only":True,"game_runtime_tested":False,"runtime_files_changed":False},
        "integration":{"draw_order":["opaque background","frame","slot texture","native player/item rendering","dynamic labels/values","hover/tooltip"],
            "landscapes_are_not_seamless_tiles":True,"for_minecraft":"1.20.1","new_font_or_gameplay_data":False}}
    (OUT/"manifest.json").write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding="utf-8")
    (OUT/"validation.json").write_text(json.dumps({"passed":True,"opaque_backgrounds":len(assets),"original_crops_verified":True,"visual_review_completed":True,"runtime_tested":False},indent=2),encoding="utf-8")
    (OUT/"README.md").write_text("""# 五行仙途完整背景素材 v1

本包补齐拆分包未能从合成图恢复的背景。使用内置 image_gen 对用户指定图进行去控件补绘，再裁切与尺寸适配。

已备齐顶部、左侧导航、中央宣纸、右侧概览、角色视窗五个独立无字背景，均为不透明 RGBA PNG。角色视窗直接保留原图未被遮挡的山水裁切，其余四区来自补绘图。

- `clean-background-master.png`：完整去字、去控件、去边框的补绘底图，1672×941。
- `layers-original/`：各背景在原尺寸坐标下的切片。
- `textures/background/`：供后续 GuiGraphics 接入的尺寸版本。
- `manifest.json`：来源、源坐标、纹理尺寸、接入路径建议及绘制顺序。
- `generation-prompt.txt`：本轮生图实际提示词。
- `background-contact-sheet.png`：五个背景总览。
- `background-reassembled.png`：分区重组验证图。
- `offline-layer-compatibility-preview.png`：与前轮边框、图标、9×3＋9 槽位组合的离线校验，仅校验图层配合，不是最终界面，也不是游戏截图。

## 接入说明

1. 背景、边框、槽位、物品、玩家、动态文字须分层绘制。此背景包没有固化文字、槽框、按钮或进度条。
2. 山水分区按各自面板目标矩形整块绘制；不要平铺，也不要用九宫格拉伸山峰。窗口缩小时可保持比例裁切，分区位置由布局代码决定。
3. PNG 不要求统一为 2 的幂；Forge 1.20.1 可以使用 manifest 中记录的实际宽高。不要使用 1.20.2+ GUI sprite 元数据。
4. 本次补绘合理续接了遮挡区域；它不是从原图恢复的隐藏原始图层，也不能保证每座山与原来完全相同。
5. 原图是 10 列，当前容器为 9 列。背景不含槽位，允许实装时按 9 列布置。数值与额外状态的绑定另行处理。
6. 目前素材已经备齐并做离线图层检查，未修改 Java、未放入运行时 assets、未进行游戏内验证。

`cultivation-ui-materials-v2.zip` 合并此前拆分素材与本背景包，保留来源和限制，适合作为下一步接入输入。
""",encoding="utf-8")
    (OUT/"tools").mkdir(exist_ok=True)
    shutil.copy2(Path(__file__),OUT/"tools"/Path(__file__).name)
    background_zip=shutil.make_archive(str(OUT),"zip",root_dir=OUT)
    # A complete materials bundle references both independently maintained packages.
    import zipfile
    complete=ROOT/"source-assets"/"cultivation-ui-materials-v2.zip"
    with zipfile.ZipFile(complete,"w",zipfile.ZIP_DEFLATED) as archive:
        for base,prefix in [(SPLIT,"split-v1"),(OUT,"backgrounds-v1")]:
            for file in sorted(base.rglob("*")):
                if file.is_file():
                    archive.write(file,prefix+"/"+file.relative_to(base).as_posix())
        archive.writestr("README.txt","Complete preparation bundle: split-v1 contains original cuts and candidate UI components; backgrounds-v1 contains reviewed clean background layers. No runtime code modified. See each manifest and README for integration limits.")
    print(json.dumps({"output":str(OUT),"background_layers":len(assets),"background_zip":background_zip,"complete_materials_zip":str(complete),"validation_passed":True},ensure_ascii=False,indent=2))


if __name__=="__main__":
    p=argparse.ArgumentParser()
    p.add_argument("--clean",type=Path,default=GENERATED)
    p.add_argument("--prompt",type=Path,required=True)
    args=p.parse_args()
    package(args.clean,args.prompt)
