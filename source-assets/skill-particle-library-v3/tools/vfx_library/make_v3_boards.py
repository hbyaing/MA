"""Labeled boards from this run's real authoring-preview captures."""
import json
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont

ROOT=Path(__file__).resolve().parents[2]
REPORT=ROOT/'reports/skill-particle-library-v3'
OUT=ROOT/'source-assets/skill-particle-library-v3'
FONT='C:/Windows/Fonts/msyh.ttc'


def board(items,title,subtitle,columns,width,cell_height,name):
    margin=24;gap=16;cw=(width-margin*2-gap*(columns-1))//columns
    rows=(len(items)+columns-1)//columns;height=107+rows*cell_height+(rows-1)*gap+margin
    image=Image.new('RGB',(width,height),'#0b131c');d=ImageDraw.Draw(image)
    d.text((margin,22),title,font=ImageFont.truetype(FONT,28),fill='#e6edf3')
    d.text((margin,64),subtitle,font=ImageFont.truetype(FONT,14),fill='#91afc0')
    for index,(file,label,note) in enumerate(items):
        x=margin+(index%columns)*(cw+gap);y=105+(index//columns)*(cell_height+gap)
        d.rounded_rectangle((x,y,x+cw,y+cell_height),7,fill='#142230')
        im=Image.open(file).convert('RGB');im.thumbnail((cw,cell_height-58),Image.Resampling.LANCZOS)
        image.paste(im,(x+(cw-im.width)//2,y))
        d.text((x+12,y+cell_height-52),label,font=ImageFont.truetype(FONT,19),fill='#e4eeee')
        d.text((x+12,y+cell_height-24),note,font=ImageFont.truetype(FONT,12),fill='#a0b6c4')
    image.save(REPORT/name)


if __name__=='__main__':
    profiles=json.loads((OUT/'catalog/models.json').read_text('utf-8'))
    items=[(REPORT/('model__'+id+'.png'),p['name'],f'实体箱 {p["width"]} × {p["height"]} 格') for id,p in profiles.items()]
    board(items,'V3 · 十类原始 Boss 模型','来自当前源码 GEO 与贴图 · 静态绑定姿势 · 心魔展示二期无相魔躯',5,1370,330,'models-v3.png')
    items=[(REPORT/'ermei_01__iso.png','耳魅 · 声刃扇击','17.25 格判定半径 / 95°扇面'),
           (REPORT/'ermei_05__top.png','耳魅 · 双壁合鸣','每段墙保留真实缺口 / 不跨安全通道'),
           (REPORT/'pengjiao_04__top.png','彭矫 · 水相回环','原始圆头线段拼环 / 保留楔形缺口'),
           (REPORT/'shenyin_02__top.png','身淫 · 触臂横断','阶段切换内外攻击顺序 / 空心环'),
           (REPORT/'pengju_03__iso.png','彭倨 · 散财飞刃','源模型财甲主形 / 实际扇射弹道'),
           (REPORT/'heart_demon_05__front.png','心魔 · 照心凝视','裂面核心与视线标记 / 无固定攻击半径')]
    board(items,'V3 · Boss 风格与技能范围适配','格坐标比例 · 实线为实际判定边界 · 浏览器制作预览，未接入游戏',2,1340,398,'range-style-v3.png')
    print('Created model and source-range boards.')
