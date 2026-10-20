"""Extract the project's actual GeckoLib boss models into portable bind-pose meshes.

No Minecraft process or production resources are changed.  Coordinates follow
GeckoLib 4.8.4's BakedModelFactory/RenderUtils and GeoEntityRenderer at entity yaw
zero: +Y is up and +Z is forward.  These are static meshes, not animation captures.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import math
import shutil
from collections import Counter
from pathlib import Path

import numpy as np
from PIL import Image


STYLE = {
    'muyan': dict(body='#d1d8e0', accent='#69dfcf', core='#f3e9c8', shadow='#191524',
                  colors=['#edba69', '#ed76b7', '#69dfcf', '#8d72e0'],
                  motifs=['虹彩螺旋虹膜', '球形眼白', '分叉视神经', '细长凝视光路'],
                  avoid=['统一紫色烟团', '无眼部结构的普通星芒'],
                  anchorBones=dict(core='iris_spin', mouth='eyeball', left='nerve_01', right='nerve_07'),
                  evidence=['eyeball', 'iris_spin', 'optic_roots', 'watcher_1']),
    'ermei': dict(body='#203c37', accent='#65cbbb', core='#b1ffee', shadow='#243330',
                  colors=['#cdd0b9', '#41594f', '#eee7cc'],
                  motifs=['骨白无面长躯', '巨型螺耳', '青绿声纹', '分拍传播的窄波脊'],
                  avoid=['通用电音霓虹墙', '蓝紫晶体'],
                  anchorBones=dict(core='spiral_pinna', mouth='faceless_head', left='left_forearm_soundmarks', right='right_forearm_soundmarks'),
                  evidence=['spiral_pinna', 'resonance_torso_soundmarks', 'faceless_head_soundmarks']),
    'bichen': dict(body='#ad979b', accent='#8d9f40', core='#e9ffbb', shadow='#7f5664',
                   colors=['#97646d', '#c1aba7', '#947d89'],
                   motifs=['灰粉肉体', '腹部与背部毒腺', '单孔鼻口', '黄绿黏毒与腺胞'],
                   avoid=['纯绿色树叶植物', '大团不透明绿雾'],
                   anchorBones=dict(core='breathing_belly', mouth='single_nostril_face', left='left_shoulder_gland', right='right_shoulder_gland'),
                   evidence=['single_nostril_face', 'nostril_venom_drips', 'belly_gland_0', 'dorsal_gland_0']),
    'shetao': dict(body='#879c91', accent='#a04453', core='#e3dcc0', shadow='#81313f',
                   colors=['#e7f8e7', '#b65764', '#70867e'],
                   motifs=['灰绿水囊躯体', '暗红活舌', '多重骨牙口器', '唾液丝与食屑'],
                   avoid=['鲜红火焰', '规则金属刀片'],
                   anchorBones=dict(core='waterbag_body', mouth='belly_lower_jaw', left='left_palm_mouth', right='right_palm_mouth'),
                   evidence=['long_tongue', 'sentient_tongue_tip', 'belly_saliva', 'left_palm_mouth']),
    'shenyin': dict(body='#318aa0', accent='#d571a2', core='#ffffff', shadow='#1c3549',
                    colors=['#75bfa8', '#d9ba6b', '#318aa0', '#d571a2'],
                    motifs=['多臂棱晶', '玫瑰金与玉青晶壳', '掌肘发光节点', '分节晶须'],
                    avoid=['单色冰雪', '粗大模糊光球'],
                    anchorBones=dict(core='exposed_weakpoint_core', mouth='faceless_crystal_crown', left='left_hand_0', right='right_hand_0'),
                    evidence=['prismatic_body', 'protective_crystal_shell', 'exposed_weakpoint_core', 'crystal_tendril_0']),
    'yihuo': dict(body='#8354ad', accent='#dda5e8', core='#efccf8', shadow='#21172f',
                  colors=['#c9fff0', '#fff0bb', '#e5e9ff'],
                  motifs=['紫色无定形云躯', '方形旧忆碎片', '消散边缘', '断续重现的记忆窗口'],
                  avoid=['硬质镜甲人形', '整片均匀紫雾遮挡地面'],
                  anchorBones=dict(core='formless_violet_cloud', mouth='flickering_memories', left='broken_recollection_0', right='broken_recollection_2'),
                  evidence=['formless_violet_cloud', 'dissolving_cloud_edges', 'flickering_memories']),
    'pengju': dict(body='#626e5b', accent='#cfaa4b', core='#ffe393', shadow='#403c2c',
                   colors=['#c4c0a0', '#b6c8c4', '#f7e29d'],
                   motifs=['腐败巨颅', '破碎下颚', '赃宝护甲', '分块财宝护盾与坠金'],
                   avoid=['金色羽翼神祇', '整块金黄饱和光墙'],
                   anchorBones=dict(core='golden_empty_socket_light', mouth='massive_broken_jaw', left='left_eye_glow', right='right_eye_glow'),
                   evidence=['rotting_colossal_head', 'massive_broken_jaw', 'hoarded_treasure_armor', 'resentment_treasure_shield']),
    'pengzhi': dict(body='#514052', accent='#aa5788', core='#e8a0d1', shadow='#352937',
                    colors=['#e4d5af', '#e9caff', '#705369'],
                    motifs=['紫黑裂腹', '无眼埋头', '末端口器触手', '黏液与骨牙'],
                    avoid=['所有技能都使用酒带', '紫色水晶爆炸'],
                    anchorBones=dict(core='ruptured_abdomen', mouth='blind_face_maw', left='left_palm_maw', right='right_palm_maw'),
                    evidence=['ruptured_abdomen', 'tentacle_0_terminal_maw', 'left_back_maw', 'blind_face_maw']),
    'pengjiao': dict(body='#32233f', accent='#b9f4de', core='#fff8ed', shadow='#1b1929',
                     colors=['#ffd0d9', '#b9f4de', '#c5e8ff', '#e9cdff', '#ffe7a6'],
                     motifs=['白色魂心', '五色傩面道袍幽魂', '破碎运行环', '地面游走长须'],
                     avoid=['只有五行图标', '统一圆环换五种颜色'],
                     anchorBones=dict(core='white_soul_heart', mouth='fivefold_light_core', left='jade_nuo_ghost_mask', right='gold_nuo_ghost_mask'),
                     evidence=['white_soul_heart', 'crimson_nuo_ghost_mask', 'jade_flowing_dao_robe', 'ground_roaming_tendrils']),
    'heart_demon': dict(body='#12121d', accent='#e65774', core='#ffbfcc', shadow='#0f0f17',
                        colors=['#e5cfee', '#2b2337', '#252836'],
                        motifs=['黑曜残面', '裸露红心', '心能细线', '镜轮碎片与左臂锁链'],
                        avoid=['整身红烟', '无差别血色爆炸'],
                        anchorBones=dict(core='heart_core', mouth='face_rift_seam', left='left_claw', right='weapon_anchor'),
                        evidence=['heart_core', 'face_mirror', 'face_rift_seam', 'heart_wheel_root', 'chain_1']),
}


def _matrix(pivot, rotation):
    """GeckoLib baked (-x, y, z) pivots and (-rx, -ry, rz) rotation."""
    p = np.array([-pivot[0], pivot[1], pivot[2]], dtype=float) / 16
    x, y, z = np.radians([-rotation[0], -rotation[1], rotation[2]])
    cx, sx, cy, sy, cz, sz = math.cos(x), math.sin(x), math.cos(y), math.sin(y), math.cos(z), math.sin(z)
    rx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
    ry = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    rz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    r = rz @ ry @ rx
    m = np.eye(4); m[:3, :3] = r; m[:3, 3] = p - r @ p
    return m


def _point(m, p):
    return (m @ np.array([*p, 1.0]))[:3]


def _rounded(v):
    return [round(float(x), 6) for x in v]


def _box_uv(cube):
    u, v = cube['uv']; x, y, z = map(math.floor, cube['size'])
    return {'west': ((u+z+x, v+z), (z, y)), 'east': ((u, v+z), (z, y)),
            'north': ((u+z, v+z), (x, y)), 'south': ((u+z+x+z, v+z), (x, y)),
            'up': ((u+z, v), (x, z)), 'down': ((u+z+x, v+z), (x, -z))}


def _surface_color(texture, uv):
    """Alpha-weighted source texels; not the mostly unused atlas background."""
    h, w, _ = texture.shape
    xs = [u[0] * w for u in uv]; ys = [u[1] * h for u in uv]
    left, top = max(0, math.floor(min(xs))), max(0, math.floor(min(ys)))
    right, bottom = min(w, max(left+1, math.ceil(max(xs)))), min(h, max(top+1, math.ceil(max(ys))))
    crop = texture[top:bottom, left:right]
    if crop.size == 0: return '#000000', 0
    weights = crop[:, :, 3].astype(float)
    alpha = float(weights.mean()/255)
    if weights.sum() < 1e-10: return '#000000', 0
    rgb = np.round((crop[:, :, :3] * weights[:, :, None]).sum(axis=(0, 1))/weights.sum()).astype(int)
    return '#%02x%02x%02x' % tuple(rgb), round(alpha, 5)


def _mesh(geometry, visual, texture):
    bones = geometry['bones']; by_name = {b['name']: b for b in bones}; transforms = {}; active = set()
    scale = float(visual.get('scale', 1)); final = np.diag([-scale, scale, -scale, 1.0])
    final[1, 3] = float(visual.get('renderOffsetY', 0)) * scale
    def bone_matrix(name):
        if name in transforms: return transforms[name]
        if name in active: raise ValueError(f'Bone parent cycle: {name}')
        active.add(name); b = by_name[name]
        local = _matrix(b.get('pivot', [0, 0, 0]), b.get('rotation', [0, 0, 0]))
        parent = b.get('parent'); transforms[name] = bone_matrix(parent) @ local if parent else local
        active.remove(name); return transforms[name]
    desc = geometry['description']; width = float(desc['texture_width']); height = float(desc['texture_height'])
    faces = []; anchors = {}; cube_count = 0; uv_cache = {}
    axes = ['west', 'east', 'north', 'south', 'up', 'down']
    face_indices = {'west': [3, 2, 0, 1], 'east': [4, 5, 7, 6], 'north': [2, 4, 6, 0],
                    'south': [5, 3, 1, 7], 'up': [3, 5, 4, 2], 'down': [0, 6, 7, 1]}
    normals = {'west': [-1, 0, 0], 'east': [1, 0, 0], 'north': [0, 0, -1],
               'south': [0, 0, 1], 'up': [0, 1, 0], 'down': [0, -1, 0]}
    for bone in bones:
        name = bone['name']; bm = final @ bone_matrix(name)
        pivot = bone.get('pivot', [0, 0, 0]); pp = [-pivot[0]/16, pivot[1]/16, pivot[2]/16]
        anchors[name] = {'position': _rounded(_point(bm, pp)), 'parent': bone.get('parent'),
                         'sourcePivot': pivot, 'sourceRotation': bone.get('rotation', [0, 0, 0])}
        if bone.get('neverRender'): continue
        for ci, cube in enumerate(bone.get('cubes', [])):
            cube_count += 1; origin = cube['origin']; size = cube['size']
            inflate = float(cube.get('inflate', bone.get('inflate', 0)) or 0)/16
            x0 = -(origin[0]+size[0])/16-inflate; x1 = -origin[0]/16+inflate
            y0 = origin[1]/16-inflate; y1 = (origin[1]+size[1])/16+inflate
            z0 = origin[2]/16-inflate; z1 = (origin[2]+size[2])/16+inflate
            verts = [[x0,y0,z0],[x0,y0,z1],[x0,y1,z0],[x0,y1,z1],
                     [x1,y1,z0],[x1,y1,z1],[x1,y0,z0],[x1,y0,z1]]
            transform = bm @ _matrix(cube.get('pivot', [0, 0, 0]), cube.get('rotation', [0, 0, 0]))
            verts = [_rounded(_point(transform, v)) for v in verts]
            is_box = isinstance(cube.get('uv'), list); box_uv = _box_uv(cube) if is_box else None
            mirror = bool(cube.get('mirror', False))
            for side in axes:
                if not is_box and side not in cube.get('uv', {}): continue
                if is_box: coords, uv_size = box_uv[side]; rot = 0
                else:
                    face = cube['uv'][side]; coords = face['uv']; uv_size = face['uv_size']; rot = int(face.get('uv_rotation', 0))
                u, v = coords[0]/width, coords[1]/height
                u1, v1 = (coords[0]+uv_size[0])/width, (coords[1]+uv_size[1])/height
                if not mirror: u, u1 = u1, u
                uv = [[u,v],[u1,v],[u1,v1],[u,v1]]; shift = (rot%360)//90; uv = uv[shift:]+uv[:shift]
                side_for_vertices = side
                if mirror:
                    side_for_vertices = {'west':'east', 'east':'west'}.get(side, side)
                    if not is_box: side_for_vertices = {'up':'down', 'down':'up'}.get(side_for_vertices, side_for_vertices)
                p = [verts[i] for i in face_indices[side_for_vertices]]
                n = np.array(normals[side], dtype=float)
                if mirror: n[0] *= -1
                n = transform[:3,:3] @ n; n /= np.linalg.norm(n)
                key = tuple(tuple(t) for t in uv)
                if key not in uv_cache: uv_cache[key] = _surface_color(texture, uv)
                color, alpha = uv_cache[key]
                if alpha <= 0: continue
                faces.append({'p': p, 'uv': [_rounded(t) for t in uv], 'n': _rounded(n),
                              'bone': name, 'cube': ci, 'color': color, 'alpha': alpha})
    points = np.array([p for f in faces for p in f['p']], dtype=float)
    bounds = {'min': _rounded(points.min(axis=0)), 'max': _rounded(points.max(axis=0)),
              'size': _rounded(points.max(axis=0)-points.min(axis=0))}
    return {'faces': faces, 'cubeCount': cube_count, 'boneCount': len(bones), 'faceCount': len(faces)}, anchors, bounds


def build_models(root: Path, out: Path) -> dict:
    """Copy source models/textures and return ten catalog-keyed model profiles.

    Each face has p[4][3] positions in Minecraft blocks, normalized uv[4][2],
    outward n[3], source bone and color/alpha fallback for distant previews.
    A complete profile JSON is also saved in out/models/boss-profiles.json.
    """
    root = Path(root); out = Path(out); source_root = root/'src/main/resources/assets/wuxingxiantu'
    source_defs = root/'src/main/resources/data/wuxingxiantu/boss_visuals.json'
    definitions = json.loads(source_defs.read_text('utf-8'))
    dest = out/'models'; dest.mkdir(parents=True, exist_ok=True)
    profiles = {}; failures = []
    for visual in definitions:
        if not visual.get('model') or not visual.get('texture'): continue
        catalog_id = 'heart_demon' if visual['id'] == 'heart_demon_phase2' else visual['id']
        style = STYLE[catalog_id]; source_model = source_root/visual['model']; source_texture = source_root/visual['texture']
        geo = json.loads(source_model.read_text('utf-8'))['minecraft:geometry'][0]
        texture_image = Image.open(source_texture).convert('RGBA'); texture = np.asarray(texture_image)
        mesh, anchors, bounds = _mesh(geo, visual, texture)
        for ext, src in [('geo.json', source_model), ('png', source_texture),
                         ('animation.json', source_root/visual['animation'])]:
            shutil.copy2(src, dest/f'{catalog_id}.{ext}')
        source = [source_defs, source_model, source_texture,
                  root/'src/main/java/cn/blockforge/generated/generatedmod/client/boss/BossGeoRenderer.java',
                  root/'src/main/java/cn/blockforge/generated/generatedmod/client/boss/BossGeoModel.java']
        suggested = {}
        for role, bone in style['anchorBones'].items():
            if bone not in anchors: failures.append(f'{catalog_id}: missing {bone}'); continue
            suggested[role] = {'bone': bone, 'position': anchors[bone]['position'],
                               'basis': '静态绑定姿势下的命名骨骼 pivot；接入时需取动画后骨骼矩阵'}
        suggested['ground'] = {'bone': None, 'position': [0, 0, 0], 'basis': '实体脚底世界原点，不使用模型包围盒底部'}
        for bone in style['evidence']:
            if bone not in anchors: failures.append(f'{catalog_id}: missing style evidence {bone}')
        tex_colors = Counter(tuple(c) for c in texture.reshape(-1, 4) if c[3] > 127)
        chosen_colors = [style[k] for k in ('body','accent','core','shadow')]+style['colors']
        palette_evidence = []
        for color in chosen_colors:
            target = np.array([int(color[1:3],16),int(color[3:5],16),int(color[5:7],16)])
            nearest = min(tex_colors, key=lambda c: sum((int(c[i])-int(target[i]))**2 for i in range(3)))
            palette_evidence.append({'designColor': color, 'nearestSourcePixel':'#%02x%02x%02x'%nearest[:3],
                                     'exactMatch': tuple(target)==nearest[:3], 'pixelCount': tex_colors[nearest]})
        profile = {k: visual[k] for k in ('scale','width','height','eyeHeight','renderOffsetY','floating','renderRadius')}
        profile.update({'id':catalog_id, 'visualDefinitionId':visual['id'], 'modelId':visual['modelId'],
                        'name':visual['displayName'], 'texture':f'models/{catalog_id}.png',
                        'sourceModel':f'models/{catalog_id}.geo.json', 'sourceAnimation':f'models/{catalog_id}.animation.json',
                        'textureSize':list(texture_image.size), 'mesh':mesh, 'bounds':bounds,
                        'anchors':anchors, 'suggestedAnchors':suggested,
                        'collisionBounds':{'min':[-visual['width']/2,0,-visual['width']/2],
                                           'max':[visual['width']/2,visual['height'],visual['width']/2]},
                        'palette':{k:style[k] for k in ('body','accent','core','shadow','colors')},
                        'paletteEvidence':palette_evidence,'motifs':style['motifs'],'avoid':style['avoid'],
                        'styleBoneEvidence':style['evidence'],
                        'source':[{'path':p.relative_to(root).as_posix(),'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in source],
                        'coordinateSystem':'+Y up, +Z entity forward at yaw=0, Minecraft blocks',
                        'pose':'static-bind-pose',
                        'limits':['真实资源静态绑定姿势；未应用待机/技能动画、动态缩放、转头或实体位移。',
                                  'bounds 是可见网格包围盒，collisionBounds 是源码实体箱；二者都不是技能伤害范围。',
                                  'renderRadius 是实体渲染裁剪半径，不可当作技能半径。',
                                  '面均色是远景预览降级数据；uv 与原贴图保留供纹理渲染。']})
        if catalog_id == 'heart_demon':
            phase1 = next(v for v in definitions if v['id']=='heart_demon_phase1')
            profile['phase1'] = {'id':phase1['id'],'modelId':'player','width':phase1['width'],
                                 'height':phase1['height'],'eyeHeight':phase1['eyeHeight'],
                                 'note':'一期使用目标玩家动态外观，本包展示二期无相魔躯，未伪造玩家皮肤。'}
        profiles[catalog_id] = profile
    if failures: raise ValueError('; '.join(failures))
    (dest/'boss-profiles.json').write_text(json.dumps(profiles, ensure_ascii=False, separators=(',',':')), encoding='utf-8')
    return profiles


if __name__ == '__main__':
    parser = argparse.ArgumentParser(); parser.add_argument('--root', type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument('--out', type=Path); args = parser.parse_args()
    out = args.out or args.root/'source-assets/skill-particle-library-v3'
    profiles = build_models(args.root, out)
    print(json.dumps({k:{'bones':v['mesh']['boneCount'],'cubes':v['mesh']['cubeCount'],'faces':v['mesh']['faceCount'],
                         'bounds':v['bounds'],'height':v['height']} for k,v in profiles.items()}, ensure_ascii=False, indent=2))
