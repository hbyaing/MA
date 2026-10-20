"""Build source-aligned Boss VFX authoring assets without building or editing the mod."""
from __future__ import annotations
import argparse,copy,csv,hashlib,json,shutil,zipfile
from pathlib import Path
from boss_geometry_v3 import build_geometry
from boss_models_v3 import build_models
from boss_materials_v3 import build_materials
from range_shapes_v3 import prepare_scenario

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'source-assets/skill-particle-library-v3'


def write(path,data,compact=False):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,ensure_ascii=False,indent=None if compact else 2,separators=(',',':') if compact else None)+'\n','utf-8')


def docs(out,geometry,profiles,materials,skills,native):
    rows=['# Boss 粒子库 V3：模型、清晰度与真实范围','',
          '此版本专门重做全部 77 项 Boss 技能；V2 的普通功法、剑技、心法、符箓及异火仍保留在 V2 独立制作库。',
          '', '## 使用入口', '',
          '- 双击 `preview/index.html`，无需网络或安装依赖。',
          '- 选择 Boss、技能、阶段/目标分支；切换俯视检查安全区，正视检查高度。',
          '- 目录前摇、实际生效 tick、碰撞容差及非伤害 cues 分开记录；点表格可跳至该子攻击。',
          '- `recipes/*.json` 是权威参数与制作配置；`photon/projects` 是可编辑原生场景；`resourcepack` 是配套纹理与 FX。',
          '- `source-textures/boss_v3` 保留 256 px 母版，`textures/boss_v3` 为 128 px 清晰版本；全部预着色，粒子 tint 应为白色。',
          '', '## 本次适配','',
          f'- {len(profiles)} 个原始 GEO 模型与贴图，统一为 +Y 向上、+Z 前方、1格=16模型单位；带骨骼锚点和实体箱。',
          f'- {len(skills)} 技能、{sum(len(s["scenarios"]) for s in skills.values())} 个源码回放分支；Boss 本体模型、范围和目标同尺度展示。',
          f'- {len(materials)} 张新专属素材，主体以不透明轮廓呈现，不使用整图模糊，32px 深浅背景检查。',
          '- Photon 地面范围采用 Horizontal 原生模式，开始尺寸为几何全尺寸的一半，避免旧版画面尺寸定义与引擎半尺寸混淆。',
          '- 每个危险子区域独立输出，不为发射器预算删掉边界；装饰纹样先裁入实际区域，安全缺口与空心环保持透明。',
          '- 原生范围贴图向实际区域内缩一个纹素以避免栅格取整溢入安全区；精确边界仍由配方的格坐标轮廓提供，接入建议采用 Mesh/Decal 几何。',
          '', '## 几何约定与容易误读的地方','',
          '- 线段 width 为全宽；服务端使用圆头线段半宽 + 0.25 格。特效实线按实际判定绘制，名义范围用可选虚线。',
          '- 圆与扇区半径包含服务端 0.25 格容差；环的内外边界分别减/加该容差。',
          '- 贴地震环高度约 0.65 格；普通环可高达 8 格。地面贴图只表达平面边界，三维体积另见配方高度。',
          '- 双壁合鸣保留每条墙及其缺口，不用整片声墙贴图覆盖通道。彭矫缺口环按源码胶囊段拼接，注释中的60度不能直接当实际安全角。',
          '- 凝视按视线点积与遮挡判定，不把其装饰性30格 cue 当成伤害上限。',
          '- 蓄力视觉骨骼锚点与服务器伤害原点分别保留；从嘴/心核发出的装饰不能移动真实判定区域。',
          '- 回放输入中的目标、历史路线、阶段及节点存活是明确场景假设；实际地形、破招、目标移动需在游戏接入时重算。',
          '', '## 接入原则','',
          '1. 由 BossCombatFxPacket 或同源服务端事件传入实际 start/end/radius/width/windup/lifetime；不要将固定演示场景直接作为所有战斗的范围。',
          '2. 将配方 footprint 用作地面 Mesh/Decal 边界，按实际碰撞测试生成胶囊/环/扇区；弧线折线化误差只允许在边界的细小弦差。',
          '3. 同步预警、生效、取消、实体移除与遮挡。cancelAtTick 及中断分支不能遗留危险边界。',
          '4. 仅装饰层可降采样；边界、缺口和关键目标标记不得随品质档位删除。大面积连续光带请用 Mesh/Beam，而非把小粒子放大数十倍。',
          '5. 本包的 Photon FX 是固定输入的编辑样片，含所有危险区域。原生速度曲线是源逐tick轨迹的积分近似；它不负责伤害、碰撞或动态追踪。',
          '', '## 验证边界','',
          '构建过程没有修改生产 Java、服务器判定、Boss 模型或现有实例。独立 Java 存根只执行当前技能时间线及纯几何，用于提取制作参数；不是模组编译或 GameTest。',
          '浏览器展示静态绑定模型，没有播放 GeckoLib 技能动画。心魔一期采用玩家动态皮肤，预览明确展示其二期无相模型。',
          '原生 NBT、文件引用、遮罩和浏览器验证不等于 Minecraft 实机渲染、联机与性能验收。',
          '', '## 目录','',
          '`catalog/geometry.json`、`catalog/models.json`、`catalog/materials.json`、`catalog/skills.json`、`catalog/integration-contract.json`、`photon/exports.json`、`photon/decal-mapping.json`、`manifest.json`。',
          '', '生成器位于本包 `tools/vfx_library`；重新从源码提取需放回原工程 tools/vfx_library 下运行 build_v3.py（Pillow、NumPy、nbtlib 和本地 Java 可用）。',
          '实际源文件摘要与精确哈希写入 catalog；浏览器模型转换依据本项目 GeckoLib 4.8.4 依赖字节码。']
    (out/'README.md').write_text('\n'.join(rows)+'\n','utf-8')
    table=['# 十类 Boss 的模型风格依据','', '| Boss | 源码模型体型 | 特征纹样 | 避免 |','|---|---|---|---|']
    for p in profiles.values():table.append(f'| {p["name"]} | 碰撞箱宽{p["width"]} × 高{p["height"]}格 | '+ '、'.join(p['motifs'])+' | '+'、'.join(p['avoid'])+' |')
    (out/'catalog/模型风格依据.md').write_text('\n'.join(table)+'\n','utf-8')
    write(out/'catalog/integration-contract.json',{
        'version':3,'units':'Minecraft blocks / game ticks','forward':[0,0,1],
        'authoritativeFields':['shape','origin','target','radius','width','angleDegrees','height','telegraphStartTick','startTick','durationTicks','cancelAtTick','collision'],
        'nonAuthoritativeFields':['footprint polygon discretization','style','edgeSamples','castAnchor'],
        'width':'full line width in source; actual half width = width/2+0.25',
        'sourceDigest':geometry['extraction'],
        'rendering':{'base':'alpha blend, white tint, depth test, no full-screen glow','boundaries':'never remove on quality reduction',
                     'nativeSize':'TileParticle +/-half extents; startSize.x=fullWidth/2,startSize.y=fullHeight/2',
                     'ground':'Horizontal (+90deg around X); raster u/v map from maxX/maxZ toward minX/minZ',
                     'nativeTrajectory':'integration approximation; JSON path preserves authoritative synthetic replay points'},
        'limits':geometry['limitations'],'minecraftRendered':False,'productionIntegrated':False})
    write(out/'catalog/native-export-statistics.json',native)


def manifest(out):
    files={p.relative_to(out).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(out.rglob('*')) if p.is_file() and p.name!='manifest.json' and '__pycache__' not in p.parts}
    value={'version':'3.0.0','created':'2026-09-30','purpose':'Boss model/style/geometry authoring library','files':files,'minecraftRendered':False,'productionIntegrated':False}
    write(out/'manifest.json',value)
    return value


def package(out):
    manifest(out);delivery=[]
    for name,folder in [('wuxingxiantu-boss-particle-library-v3.zip',out),('wuxingxiantu-boss-vfx-resourcepack-v3.zip',out/'resourcepack')]:
        dest=ROOT/'deliverables'/name;dest.parent.mkdir(exist_ok=True)
        with zipfile.ZipFile(dest,'w',zipfile.ZIP_DEFLATED,compresslevel=7) as archive:
            for file in sorted(folder.rglob('*')):
                if file.is_file() and '__pycache__' not in file.parts:archive.write(file,file.relative_to(folder))
        with zipfile.ZipFile(dest) as archive:
            assert archive.testzip() is None
            assert all(not Path(n).is_absolute() and '..' not in Path(n).parts for n in archive.namelist())
            count=len(archive.namelist())
        delivery.append({'name':name,'bytes':dest.stat().st_size,'files':count,'sha256':hashlib.sha256(dest.read_bytes()).hexdigest(),'zipIntegrity':True})
    write(ROOT/'reports/skill-particle-library-v3/delivery.json',{'artifacts':delivery,'minecraftRendered':False,'productionIntegrated':False})
    return delivery


def build(out,skip_native=False):
    out.mkdir(parents=True,exist_ok=True);(out/'preview').mkdir(exist_ok=True)
    print('Reading source geometry...',flush=True);geometry=build_geometry(ROOT)
    print('Reading original models...',flush=True);profiles=build_models(ROOT,out)
    print('Drawing clear, model-specific materials...',flush=True);materials=build_materials(ROOT,out)
    for p in profiles.values():
        anchor=p.get('suggestedAnchors',{}).get('core')
        p['castAnchor']=anchor.get('position',[0,p['eyeHeight'],0]) if isinstance(anchor,dict) else [0,p['eyeHeight'],0]
    for m in materials:
        dest=out/'resourcepack/assets/wuxingxiantu_vfx/textures/boss_v3'/Path(m['file']).name;dest.parent.mkdir(parents=True,exist_ok=True)
        shutil.copyfile(out/m['file'],dest);shutil.copyfile((out/m['file']).with_suffix('.png.mcmeta'),dest.with_suffix('.png.mcmeta'))
        m['texture']='wuxingxiantu_vfx:textures/boss_v3/'+dest.name
    skills=copy.deepcopy(geometry['skills'])
    for ident,skill in skills.items():
        skill['id']=ident
        skill['style']={'palette':profiles[skill['boss']]['palette'],'materials':{m['role']:m['id'] for m in materials if m['boss']==skill['boss']},
                        'opacity':'solid body with alpha blend','rangePolicy':'Source collision region; decoration clipped inside; no geometric rescale',
                        'anchorPolicy':'model cast anchor is decoration only; hazards remain ground-space source coordinates'}
        for scene in skill['scenarios']:prepare_scenario(scene)
        write(out/'recipes'/(ident+'.json'),skill)
    write(out/'catalog/geometry.json',geometry);write(out/'catalog/models.json',profiles,True);write(out/'catalog/materials.json',materials);write(out/'catalog/skills.json',skills,True)
    write(out/'resourcepack/pack.mcmeta',{'pack':{'pack_format':15,'description':'五行仙途 Boss V3 · 原始模型风格 / 清晰边界 / 源码范围'}})
    native={}
    if not skip_native:
        print('Writing native Photon scenes with every hazard...',flush=True)
        from photon_v3 import export
        _,native=export(out,skills,profiles,materials)
    elif (out/'catalog/native-export-statistics.json').exists():native=json.loads((out/'catalog/native-export-statistics.json').read_text('utf-8'))
    docs(out,geometry,profiles,materials,skills,native)
    model={'version':'3.0.0','skills':skills,'profiles':profiles,'materials':materials,'sourceDigest':geometry['extraction'],'native':native}
    (out/'preview/library-data.js').write_text('window.BOSS_VFX_LIBRARY='+json.dumps(model,ensure_ascii=False,separators=(',',':')).replace('</','<\\/')+';\n','utf-8')
    shutil.copyfile(Path(__file__).with_name('preview-v3.html'),out/'preview/index.html');shutil.copyfile(Path(__file__).with_name('player-v3.js'),out/'preview/player-v3.js')
    tools=out/'tools/vfx_library';tools.mkdir(parents=True,exist_ok=True)
    for file in Path(__file__).parent.iterdir():
        if file.is_file() and ('v3' in file.name or file.name in ('photon.py','photon_particle_template.snbt','requirements.txt')):shutil.copyfile(file,tools/file.name)
    shutil.copytree(Path(__file__).with_name('geometry_v3_support'),tools/'geometry_v3_support',dirs_exist_ok=True,ignore=shutil.ignore_patterns('*.class','__pycache__'))
    source_count=len(geometry['sourceFiles'])
    validation={'bosses':len(profiles),'skills':len(skills),'scenarios':sum(len(s['scenarios']) for s in skills.values()),'events':sum(len(sc['events']) for s in skills.values() for sc in s['scenarios']),
                'materials':len(materials),'sourceFiles':source_count,'geometryValidation':geometry['validation'],'native':native,'minecraftRendered':False,'productionIntegrated':False}
    write(out/'validation.json',validation);manifest(out)
    print(json.dumps(validation,ensure_ascii=False),flush=True)
    return validation


if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--out',type=Path,default=OUT);parser.add_argument('--skip-native',action='store_true');parser.add_argument('--package',action='store_true');parser.add_argument('--package-only',action='store_true');args=parser.parse_args()
    if not args.package_only:build(args.out,args.skip_native)
    if args.package or args.package_only:print(json.dumps(package(args.out),ensure_ascii=False))
