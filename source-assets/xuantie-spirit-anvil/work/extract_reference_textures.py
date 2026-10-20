from pathlib import Path
import json, math, base64, io, copy, hashlib
from PIL import Image, ImageOps

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'reference-textured'
OUT.mkdir(exist_ok=True)
REF=Image.open(ROOT/'reference.png').convert('RGBA')
MODEL=json.loads((ROOT/'玄铁灵砧_贴图精修.bbmodel').read_text(encoding='utf-8'))
GEOM=json.loads((ROOT/'work/geometry.json').read_text(encoding='utf-8'))

def key(e):return (e['name'],tuple(round(v,5) for v in e['from']),tuple(round(v,5) for v in e['to']))
DESC={key(p):p for p in GEOM['parts']}

def crop(rect,w,h):
    return REF.crop(tuple(round(x) for x in rect)).resize((w,h),Image.Resampling.NEAREST)

def pos_at(e,face,x,y,w,h):
    a=e['from'];b=e['to'];u=(x+.5)/w;v=(y+.5)/h
    if face=='north':return [b[0]-u*(b[0]-a[0]),b[1]-v*(b[1]-a[1]),a[2]]
    if face=='south':return [a[0]+u*(b[0]-a[0]),b[1]-v*(b[1]-a[1]),b[2]]
    if face=='east':return [b[0],b[1]-v*(b[1]-a[1]),b[2]-u*(b[2]-a[2])]
    if face=='west':return [a[0],b[1]-v*(b[1]-a[1]),a[2]+u*(b[2]-a[2])]
    if face=='up':return [a[0]+u*(b[0]-a[0]),b[1],a[2]+v*(b[2]-a[2])]
    return [a[0]+u*(b[0]-a[0]),a[1],b[2]-v*(b[2]-a[2])]

def sample(u,v):return REF.getpixel((max(0,min(REF.width-1,round(u))),max(0,min(REF.height-1,round(v)))))

# Surface swatches are all cropped from the user's reference, not painted.
SWATCHES={
    'iron':[(664,134,814,206),(932,136,1084,208)],
    'iron_dark':[(721,247,756,267),(1000,245,1035,264)],
    'steel':[(119,197,263,215)],
    'bronze':[(589,128,650,160),(1101,129,1161,160),(659,423,725,456)],
    'bronze_dark':[(798,249,810,337),(940,249,953,337)],
    'blue':[(856,130,891,225)],
    'cyan':[(867,134,882,216)],
    'white':[(869,297,880,343)],
    'socket':[(836,279,853,346)],
}

def basic(mat,w,h,e,face):
    choices=SWATCHES[mat];variant=int(hashlib.md5(e['name'].encode()).hexdigest()[:4],16)%len(choices)
    rect=choices[variant]
    im=crop(rect,w,h)
    if mat in ('blue','cyan','white') and w>h:im=crop(rect,h,w).transpose(Image.Transpose.ROTATE_90)
    return im

def project(e,face,w,h,kind):
    out=Image.new('RGBA',(w,h));pix=out.load()
    side=face in ('east','west')
    for y in range(h):
        for x in range(w):
            xx,yy,zz=pos_at(e,face,x,y,w,h)
            if kind=='top':
                u=284.5+xx*20.35;v=312.5+zz*20.35
            elif kind=='head':
                coord=zz if side else xx
                u=(1419.5+coord*30.4) if side else (874.5+coord*24.08)
                # Reference head has a stepped lower outline. Clamp the last
                # valid row at the wings rather than sampling its background.
                norm=abs(coord)/(6 if side else 12)
                v=126+(15.96-yy)*23.8
                stop=235 if norm<.48 else 214
                v=min(v,stop-1)
            elif kind=='column':
                coord=zz if side else xx
                u=(1418.5+coord*33.3) if side else (874.5+coord*25.4)
                v=377-(yy-4.4)*21.45
            elif kind=='base':
                coord=zz if side else xx
                u=(1418+coord*34.9) if side else (874+coord*24)
                v=498-yy*26.2
            elif kind=='platform':
                coord=zz if side else xx
                u=(1418+coord*35) if side else (874+coord*24.1)
                v=409-(yy-3.72)*36
            elif kind=='secondary':
                coord=zz if side else xx
                u=(1418+coord*35) if side else (874+coord*24.1)
                v=420-(yy-3.12)*22
            elif kind=='shoulder':
                coord=zz if side else xx
                u=(1418.5+coord*30.4) if side else (874.5+coord*24.08)
                v=238+(11.35-yy)*18.2
                # Projected shoulder rows contract more than the reconstruction.
                bound=max(4.4,10.8-(v-238)*.11)*(30.4 if side else 24.08)
                center=1418.5 if side else 874.5
                u=max(center-bound,min(center+bound,u))
            elif kind=='cap':
                coord=zz if side else xx
                u=(1419.5+coord*30.4) if side else (874.5+coord*24.08)
                v=126+(15.96-yy)*20.1
            else:raise ValueError(kind)
            pix[x,y]=sample(u,v)
    return out

def surface(e,face,desc,w,h):
    a=e['from'];b=e['to'];mat=desc['material'];g=desc['group'];n=e['name']
    vertical=face in ('north','south','east','west')
    if face=='up' and a[1]>=15.7:
        return project(e,face,w,h,'top'),'top_projection'
    if vertical:
        if '顶部包角' in g:
            return project(e,face,w,h,'cap'),'cap_projection'
        if '灵核' in g:
            return project(e,face,w,h,'column'),'crystal_projection'
        if '中柱' in g:
            if mat.startswith('bronze'):
                if '侧' in n:return basic(mat,w,h,e,face),'copper_crop'
                return project(e,face,w,h,'column'),'column_projection'
            return project(e,face,w,h,'column'),'column_projection'
        if '砧体' in g and a[1]>11.2:
            return project(e,face,w,h,'head'),'head_projection'
        if '承托' in g or n=='下缘_暗铁压边':
            return basic('iron_dark',w,h,e,face),'shoulder_crop'
        if '底座包角' in g:
            return project(e,face,w,h,'platform' if '承台' in n else 'base'),'base_cap_projection'
        if '底座' in g:
            if a[1]>=3.72:return project(e,face,w,h,'platform'),'platform_projection'
            if a[1]>=3.12:return project(e,face,w,h,'secondary'),'secondary_projection'
            if '磨损' in n:return basic('steel',w,h,e,face),'edge_crop'
            return project(e,face,w,h,'base'),'base_projection'
        if '贯穿灵纹' in g:
            if '砧面' in n or '砧口' in n:return project(e,face,w,h,'head'),'head_conduit_projection'
            if '中柱' in n:return project(e,face,w,h,'column'),'column_conduit_projection'
            if '底座' in n:return project(e,face,w,h,'base'),'base_conduit_projection'
            if '托肩' in n:return basic(mat,w,h,e,face),'conduit_crop'
            if '二阶' in n or '第二阶' in n:return project(e,face,w,h,'secondary'),'secondary_conduit_projection'
            if '承台' in n:return project(e,face,w,h,'platform'),'platform_conduit_projection'
    if face=='up' and '底座' in g and mat not in ('steel','bronze','bronze_dark'):
        # A clean close-up of the layered base top, bounded by the brass and blue.
        return crop((1270,397,1351,412),w,h),'base_top_crop'
    return basic(mat,w,h,e,face),'source_swatch'

patches={};rows=[];methods={}
for e in MODEL['elements']:
    desc=DESC[key(e)];dims=[e['to'][i]-e['from'][i] for i in range(3)];record={'uuid':e['uuid'],'faces':{}}
    for face,axes in [('north',(0,1)),('south',(0,1)),('east',(2,1)),('west',(2,1)),('up',(0,2)),('down',(0,2))]:
        w,h=[max(2,round(dims[i]*10)) for i in axes]
        im,method=surface(e,face,desc,w,h)
        # All sampled source colors already include directional light and glow.
        # Keep these as baked-color textures so the diffuse is not lit twice.
        digest=hashlib.sha1(im.tobytes()+str(im.size).encode()).hexdigest()
        if digest not in patches:patches[digest]={'image':im}
        record['faces'][face]={'patch':digest}
        methods[method]=methods.get(method,0)+1
    rows.append(record)

W=1024;x=y=3;row=0
for digest,v in sorted(patches.items(),key=lambda kv:(-kv[1]['image'].height,-kv[1]['image'].width)):
    im=v['image'];w,h=im.size
    if x+w+3>W:x=3;y+=row+6;row=0
    v['uv']=[x,y,x+w,y+h];x+=w+6;row=max(row,h)
H=2**math.ceil(math.log2(y+row+3))
atlas=Image.new('RGBA',(W,H),(27,29,35,255))
for v in patches.values():
    u,t,r,b=v['uv'];im=v['image'];atlas.paste(im,(u,t))
    atlas.paste(im.crop((0,0,1,im.height)).resize((3,im.height)),(u-3,t))
    atlas.paste(im.crop((im.width-1,0,im.width,im.height)).resize((3,im.height)),(r,t))
    atlas.paste(im.crop((0,0,im.width,1)).resize((im.width,3)),(u,t-3))
    atlas.paste(im.crop((0,im.height-1,im.width,im.height)).resize((im.width,3)),(u,b))
atlas.save(OUT/'reference_albedo.png')
# Moderate inverse-alpha emission keeps the baked source illumination readable
# while retaining color precision when Blockbench copies pixels through canvas.
preview=atlas.copy();preview.putalpha(128);preview.save(OUT/'reference_preview.png')
for rect,name in [((44,196,526,430),'top_extracted.png'),((585,126,1164,238),'front_extracted.png'),((1235,127,1604,238),'side_extracted.png'),((658,422,1090,498),'base_extracted.png')]:
    REF.crop(rect).save(OUT/name)

data={'resolution':[W,H],'assignments':[]}
out_model=copy.deepcopy(MODEL)
lookup={e['uuid']:e for e in out_model['elements']}
first=out_model['textures'][0];first['name']='玄铁灵砧_参考图提取';first['path']='';first['source']='data:image/png;base64,'+base64.b64encode((OUT/'reference_preview.png').read_bytes()).decode();first['mode']='bitmap';first['render_mode']='emissive';first['width']=W;first['height']=H;first['uv_width']=W;first['uv_height']=H
out_model['textures']=[first];out_model['resolution']={'width':W,'height':H}
for record in rows:
    e=lookup[record['uuid']];e['shade']=False
    for face,d in record['faces'].items():
        d['uv']=patches[d.pop('patch')]['uv'];e['faces'][face]['uv']=d['uv'];e['faces'][face]['texture']=0
    data['assignments'].append(record)
out_model['name']='玄铁灵砧_原图贴图'
(OUT/'玄铁灵砧_原图贴图.bbmodel').write_text(json.dumps(out_model,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
(OUT/'reference_uv.json').write_text(json.dumps(data,ensure_ascii=False),encoding='utf-8')
(OUT/'extraction_report.json').write_text(json.dumps({'source':'reference.png','source_size':REF.size,'texture_size':[W,H],'cubes':len(rows),'unique_patches':len(patches),'methods':methods,'generated_pixels':0,'geometry_preserved':True},ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'cubes':len(rows),'patches':len(patches),'atlas':[W,H],'methods':methods}))
