"""Reproducible Boss VFX geometry from the current Java combat timelines.

This compiles only four production *pure Java* files against a tiny recording
adapter. It never invokes Gradle, builds the mod, launches Minecraft, or copies
the stale build/classes output. Production timeline and catalog source is used
verbatim. Primitive hazard and dash methods are also injected verbatim.

The adapter supplies explicitly recorded synthetic player/world inputs. That
makes dynamic branches demonstrable without pretending to simulate a world.
"""
from __future__ import annotations

import hashlib
import json
import math
import os
from pathlib import Path
import re
import shutil
import subprocess

BASE = Path("src/main/java/cn/blockforge/generated/generatedmod/boss/combat")
SOURCE_FILES = [BASE / x for x in (
    "BossSkillPrograms.java", "HeartDemonSkillPrograms.java", "BossCombatCatalog.java",
    "BossCombatGeometry.java", "BossCombatController.java",
)] + [Path("src/main/java/cn/blockforge/generated/generatedmod/HeartDemonEntity.java")]
ADAPTER = Path(__file__).with_name("geometry_v3_support")


def _method(text: str, name: str) -> str:
    match = re.search(r"public void " + re.escape(name) + r"\([^\n]*?\)\s*\{", text)
    if not match:
        raise ValueError(f"Current controller method missing: {name}")
    start = match.start()
    depth = 1
    i = match.end()
    while depth:
        if text[i] == "{":
            depth += 1
        elif text[i] == "}":
            depth -= 1
        i += 1
    return text[start:i]


def _jdk() -> tuple[Path, Path]:
    candidates: list[Path] = []
    if os.environ.get("JAVA_HOME"):
        candidates.append(Path(os.environ["JAVA_HOME"]) / "bin")
    javac = shutil.which("javac")
    if javac:
        candidates.append(Path(javac).parent)
    for base in (Path("E:/MC-mod制作"), Path("C:/Program Files/Java"), Path.home()/".gradle/jdks"):
        if base.exists():
            candidates += [p.parent for p in base.glob("*/jbr/bin/javac.exe")]
            candidates += [p.parent for p in base.glob("*/bin/javac.exe")]
    for folder in candidates:
        compiler = folder/("javac.exe" if os.name == "nt" else "javac")
        runner = folder/("java.exe" if os.name == "nt" else "java")
        if compiler.exists() and runner.exists():
            return compiler, runner
    raise RuntimeError("Java 17+ JDK required for isolated source timeline replay; set JAVA_HOME.")


def _check_contracts(controller: str) -> None:
    # Do not silently reuse adapter behavior after hit-test semantics change.
    contracts = [
        "case 0->distance<=h.radius+.25",
        "h.width*.5+.25",
        "distance<=h.radius+.25&&distance>=h.radius-h.width-.25",
        "h.radius+.25,h.width",
        "player.getY()>h.a.y+h.height",
        "player.getY()+player.getBbHeight()<h.a.y",
        "clock>h.end",
        "shot.velocity.scale(.9).add(desired.scale(.1))",
        "next.distanceToSqr(middle())>1800",
        "shot.pos.x,shot.pos.z,next.x,next.z)<.75&&p.getY()<shot.pos.y+3",
        "p.getLookAngle().dot(toward)>.45&&clearSight(origin(),p.position())",
        "clock+100",
        "Math.min(35,start.distanceTo(aim)+5)",
        "while(footsteps.size()>12)",
        "clock%8==0",
    ]
    for contract in contracts:
        if contract not in controller:
            raise ValueError(f"Geometry adapter requires review; changed source contract: {contract}")


def _source(path: Path, root: Path, needle: str | None = None) -> dict:
    raw=(root/path).read_bytes()
    text=raw.decode("utf-8-sig")
    line=1 if needle is None else next((i for i,l in enumerate(text.splitlines(),1) if needle in l),1)
    return {"path":path.as_posix(),"line":line,"sha256":hashlib.sha256(raw).hexdigest()}


def _collision(event: dict) -> None:
    kind, shape = event["kind"], event["shape"]
    if kind == "hazard" and shape != "gaze":
        radius, width = event["radius"], event["width"]
        c = {"shape": shape, "minY":event["origin"][1], "maxY":event["origin"][1]+event["height"],
             "verticalTest":"player feet <= maxY AND player head >= minY", "cover":event["cover"]}
        if shape == "line":
            c.update(width=width+.5, radius=width*.5+.25, endCaps="round", predicate="segmentDistance <= width/2 + 0.25")
        elif shape == "ring":
            c.update(radius=radius+.25, innerRadius=max(0,radius-width-.25), predicate="radius-width-0.25 <= distance <= radius+0.25")
        elif shape == "cone":
            c.update(radius=radius+.25, angleDegrees=width, predicate="distance <= radius+0.25 AND directionDot >= cos(angle/2)")
        else:
            c.update(radius=radius+.25, predicate="distance <= radius+0.25")
        event["collision"]=c
    elif kind == "projectile":
        event["collision"]={"shape":"swept_segment","radius":.75,"strictBoundary":True,"maxY":event["origin"][1]+3,
                            "verticalTest":"player feet < projectile.y+3; no lower bound", "cover":True}
        if "endTick" in event:
            event["durationTicks"]=event["endTick"]-event["startTick"]+1
    elif shape == "gaze":
        event["collision"]={"shape":"gaze_condition","radius":None,"lookDotThreshold":.45,"cover":True,
                            "predicate":"player look toward boss eyes > 0.45 and line of sight; no local radius test"}
    event.setdefault("damaging", False)


NOTES={
    "yihuo_01":["两条±22°、20格镜痕是SAFE色纯视觉假线；只有正前方24格、宽1.5的直线造成伤害。"],
    "yihuo_06":["距离分支使用开招时锁定目标：<7格为近环；7≤距离<16格为直线；≥16格为落柱再突进。"],
    "yihuo_08":["四次176°扇面交替朝±X，中心为场地中心，不随Boss脚下移动。"],
    "shenyin_05":["晶牢起手仅三条边；i=1对应的-Z边缺失。之后心结未破会出现外半径4、内半径2.5环击。"],
    "pengjiao_04":["实际是每轮五条直线弦段组成开口环，不是连续圆形环；不要用满圆粒子填住缺口。源码注释写60°安全楔形，但最内圈半径6时，圆头与0.25容差使保证无命中的连续楔形约49.84°，不是60°。"],
    "ermei_05":["声墙由两侧各五步、每步两段直线组成。原始缺口7格，圆头线段+碰撞容差后，沿中心的净安全口为4.7格。"],
    "yihuo_05":["镜墙由两侧各五步、每步两段直线组成。原始缺口6格，圆头线段+碰撞容差后，沿中心的净安全口为3.7格。"],
    "shetao_05":["基准目标在+Z10格，卷舌路径经过+Z9格苦根，回放会触发源码自中断分支；改变目标方向可能吞食甜食并回血。"],
    "heart_demon_08":["开招时10格环只是收镜提示；真正外10、内7格环击在windup+28 tick发生。攻击次数影响伤害，不改变圈形。"],
    "muyan_05":["30格圆仅视觉提示；伤害方法没有30格距离判定。必须显示凝视/视线机制，不能把30格圈画成精确伤害边界。"],
    "heart_demon_05":["30格圆仅视觉提示；实际检查玩家朝向与遮挡，点积阈值0.45，无30格伤害半径。"],
}


def _range_summary(skill_id: str, scenario: dict) -> dict:
    hazards=[e for e in scenario["events"] if e.get("damaging")]
    ranges=[]
    for event in hazards:
        collision=event.get("collision",{})
        spec={"shape":event["shape"]}
        for key in ("radius","innerRadius","width","angleDegrees","minY","maxY"):
            if key in collision:
                spec[key]=collision[key]
        if spec not in ranges:
            ranges.append(spec)
    result={"damagingEvents":len(hazards),"firstDamageTick":min((e["startTick"] for e in hazards),default=None),
            "lastDamageTick":max((min(e.get("cancelAtTick",10**9)-1,e.get("endTick",e["startTick"]+e["durationTicks"])) for e in hazards),default=None),
            "uniqueCollisionProfiles":ranges,"conditionalWithoutLocalRadius":any(e["shape"]=="gaze" for e in hazards),
            "cueIsNotCollision":True}
    if skill_id in ("ermei_05","yihuo_05"):
        nominal=7 if skill_id=="ermei_05" else 6
        gap=max(-14,min(14,scenario["inputs"]["target"][2]))
        result["safeGap"]={"shape":"split_wall_corridor","gapCenterZ":gap,"nominalWidth":nominal,
                           "lineNominalFullWidth":1.8,"lineCollisionFullWidth":2.3,"minimumClearWidth":nominal-2.3,
                           "clearZIntervalOpen":[gap-nominal/2+1.15,gap+nominal/2-1.15],
                           "note":"胶囊端帽最窄截面；边界等于阈值时仍命中，安全区间为开区间。"}
    elif skill_id=="pengjiao_04":
        result["safeGap"]={"shape":"five_capsule_chords_per_wave","direction":[0,0,1],
                           "sourceCommentDegrees":60,"endpointCenterHalfAngleDegrees":35,"minimumRingRadius":6,
                           "capsuleCollisionRadius":1.05,"guaranteedClearWedgeDegrees":2*(35-math.degrees(math.asin(1.05/6))),
                           "note":"安全楔形逐圈变化；最内圈的真实圆头判定侵入源码注释的60°区域，必须绘制原始五条线段。"}
    elif skill_id=="shenyin_05":
        result["safeGap"]={"shape":"missing_square_side","direction":[0,0,-1],
                           "closesLaterIfKnotActive":True,"note":"开局三边晶牢，不补成四边；后续独立环击保留。"}
    return result


def build_geometry(root: Path) -> dict:
    root = Path(root).resolve()
    controller=(root/BASE/"BossCombatController.java").read_text(encoding="utf-8-sig")
    _check_contracts(controller)
    sources={p.as_posix():_source(p,root) for p in SOURCE_FILES}
    digest=hashlib.sha256()
    for p in SOURCE_FILES:
        digest.update((root/p).read_bytes())
    for p in sorted(ADAPTER.glob("*.java")):
        digest.update(p.read_bytes())
    digest.update(Path(__file__).read_bytes())
    cache=root/"build/vfx-library-v3-source-replay"/digest.hexdigest()[:16]
    cache.mkdir(parents=True,exist_ok=True)
    replay_file=cache/"replay.json"
    if not replay_file.exists():
        java_sources=[]
        for name in ("BossSkillPrograms.java","HeartDemonSkillPrograms.java","BossCombatCatalog.java","BossCombatGeometry.java"):
            dest=cache/name
            shutil.copyfile(root/BASE/name,dest)
            java_sources.append(dest)
        primitives="\n".join(_method(controller,name) for name in ("line","circle","column","cone","ring","zone","dash"))
        for path in ADAPTER.glob("*.java"):
            text=path.read_text(encoding="utf-8")
            if path.name=="BossCombatController.java":
                text=text.replace("// @SOURCE_PRIMITIVES@ (injected unchanged from the production controller).",primitives)
            dest=cache/path.name;dest.write_text(text,encoding="utf-8");java_sources.append(dest)
        javac,java=_jdk()
        output=cache/"classes";output.mkdir(exist_ok=True)
        result=subprocess.run([str(javac),"-encoding","UTF-8","-g","--release","17","-d",str(output),*[str(p) for p in java_sources]],capture_output=True,text=True,encoding="utf-8")
        if result.returncode:
            raise RuntimeError("Isolated Java source replay compile failed:\n"+result.stderr)
        result=subprocess.run([str(java),"-Dfile.encoding=UTF-8","-Dstdout.encoding=UTF-8","-Dstderr.encoding=UTF-8","-cp",str(output),"cn.blockforge.generated.generatedmod.boss.combat.ReplayMain"],capture_output=True,text=True,encoding="utf-8",timeout=60)
        if result.returncode:
            raise RuntimeError("Source timeline replay failed:\n"+result.stderr)
        # Parse before saving so a partial process result never becomes a cache hit.
        json.loads(result.stdout)
        replay_file.write_text(result.stdout,encoding="utf-8")
    skills=json.loads(replay_file.read_text(encoding="utf-8"))
    for skill_id,skill in skills.items():
        boss=skill["boss"]
        timeline=BASE/("HeartDemonSkillPrograms.java" if boss=="heart_demon" else "BossSkillPrograms.java")
        catalog=timeline if boss=="heart_demon" else BASE/"BossCombatCatalog.java"
        needle=f'skill({int(skill_id.rsplit("_",1)[1])},"{skill["name"]}"' if boss=="heart_demon" else f'skill("{boss}", {int(skill_id.rsplit("_",1)[1])}, "{skill["name"]}"'
        skill["source"]=[_source(catalog,root,needle),_source(timeline,root),_source(BASE/"BossCombatController.java",root,"private void tickHazards"),_source(BASE/"BossCombatGeometry.java",root)]
        skill["notes"]=NOTES.get(skill_id,[])
        for scenario in skill["scenarios"]:
            scenario["notes"]=["目标路径、历史足迹、场地中心是明确的回放输入，不是从源码虚构固定落点。","无墙体阻挡；节点不被玩家击碎；Boss移动假定可通过。真实阻挡/破招/目标移动会改变结果。"]
            for event in scenario["events"]:
                _collision(event)
            scenario["events"].sort(key=lambda e:(e.get("telegraphStartTick",e["startTick"]),e["startTick"]))
            scenario["eventCount"]=len(scenario["events"])
            scenario["rangeSummary"]=_range_summary(skill_id,scenario)
            scenario["durationTicks"]=max(scenario["schedulerFinishTick"],max((min(e.get("cancelAtTick",10**9),e.get("endTick",e["startTick"]+e["durationTicks"])) for e in scenario["events"]),default=0))
            scenario["targetPath"]=[sample for sample in scenario["targetPath"] if sample[0]<=scenario["durationTicks"]]
        skill["durationTicks"]=max(s["durationTicks"] for s in skill["scenarios"])
    report={"schemaVersion":3,"units":{"distance":"Minecraft blocks","time":"ticks (20 per second)","angle":"degrees"},
            "coordinateSystem":{"bossOrigin":[0,0,0],"bossForward":[0,0,1],"groundY":0,"arenaCenter":[0,0,0],"rotation":"BossCombatController.rotate: +angle turns +Z toward -X",
                                "groundNormalization":"source ground/middle y=centerBlock.y+1.05 is subtracted; arena x/z=centerBlock.x/z+0.5; synthetic boss starts at arena center. Real boss can start elsewhere."},
            "timing":{"tickZero":"BossCombatController.begin invokes BossSkillPrograms.cast immediately at cast tick 0",
                      "scheduler":"Each tick increments clock, snapshots due actions once, then executes hazards and projectiles. A later(0) callback therefore executes on the next tick, including nested later(0).",
                      "hazards":"Active on startTick through startTick+durationTicks inclusive; period and hit cooldown may limit damage applications.",
                      "projectilePath":"Samples are [absoluteTick, position]; initial spawn and first movement share the launch tick, matching spawn-before-tickShots. Duplicate launch timestamps are intentional.",
                      "cancelAtTick":"Cancellation runs before hazard processing at this tick; render no active hazard from cancelAtTick onward."},
            "extraction":{"method":"verbatim Java timeline/catalog/geometry execution against isolated recording adapter; verbatim line/circle/column/cone/ring/zone/dash methods", "sourceDigest":digest.hexdigest(),"modBuilt":False,"minecraftRun":False},
            "collisionPolicy":{"horizontalPadding":.25,"lineWidthMeaning":"full width; round segment caps","ringRadiusMeaning":"outer radius","ringWidthMeaning":"outer minus inner radius","hazardEndInclusive":True,"hazardVertical":"player feet <= ground+height and player head >= ground","projectileRadius":.75,"projectileVertical":"player feet < shotY+3; no lower bound","terrainOcclusion":"world collision ray at from.y+1.1; crystal/cover node within <1 block of ray may also block","gaze":"look dot > .45 and line of sight; displayed 30-block ring is not a damage range","damageGuard":"10-tick owner hit cooldown and 30% per-cast health budget are independent of geometry"},
            "limitations":["只记录单次技能，不计算跨技能残留区、玩家破招、地形碰撞或网络延迟。","投射物路径逐tick执行源码0.9/0.1转向规则；命中的是场景明确设置的合成目标。","玩家受拉后真实物理轨迹、移动碰撞与多事件伤害结算未模拟。","心魔普通实体AI与魂仆行为不属于77项脚本Boss技能范围。","同一技能随阶段、开招距离、历史足迹与当前目标变化；有限场景不是穷尽所有世界状态。"],
            "sourceFiles":list(sources.values()),"skills":skills}
    report["validation"]=validate_geometry(report)
    return report


def validate_geometry(report: dict) -> dict:
    skills=report["skills"]
    expected={"muyan":5,"ermei":6,"bichen":6,"shetao":7,"shenyin":7,"yihuo":8,"pengju":9,"pengzhi":9,"pengjiao":10,"heart_demon":10}
    assert len(skills)==77
    for boss,count in expected.items():
        assert all(f"{boss}_{i:02d}" in skills for i in range(1,count+1))
    def ev(sid,shape=None,scenario=0):
        return [e for e in skills[sid]["scenarios"][scenario]["events"] if e["kind"]=="hazard" and (shape is None or e["shape"]==shape)]
    assert ev("pengjiao_02","line")[0]["collision"]["width"]==2
    assert ev("pengjiao_02","line")[0]["target"]==[0,0,34]
    assert len(ev("pengjiao_04","line"))==25
    assert 49<skills["pengjiao_04"]["scenarios"][0]["rangeSummary"]["safeGap"]["guaranteedClearWedgeDegrees"]<50
    assert len(ev("ermei_05","line"))==20
    assert len(ev("yihuo_01","line"))==1
    assert ev("yihuo_01","line")[0]["startTick"]==42
    assert ev("muyan_05","gaze")[0]["collision"]["radius"] is None
    assert ev("heart_demon_08","ring")[0]["startTick"]==80
    assert ev("ermei_02","ring")[0]["height"]==.65 or abs(ev("ermei_02","ring")[0]["height"]-.65)<1e-6
    assert ev("pengju_06","ring")[0]["collision"]["innerRadius"]==5.45
    assert {e["shape"] for e in ev("yihuo_06",scenario=0)}=={"ring"}
    assert {e["shape"] for e in ev("yihuo_06",scenario=1)}=={"line"}
    assert "circle" in {e["shape"] for e in ev("yihuo_06",scenario=2)}
    scenario_count=sum(len(s["scenarios"]) for s in skills.values())
    all_events=[e for s in skills.values() for sc in s["scenarios"] for e in sc["events"]]
    assert all(sc["traceComplete"] for s in skills.values() for sc in s["scenarios"])
    assert all(e["startTick"]>=0 and e["durationTicks"]>=0 for e in all_events)
    return {"skills":77,"bosses":10,"scenarios":scenario_count,"events":len(all_events),"keyGeometryAssertions":14,"productionGeometryAssertions":7,"sourceReplayComplete":True,"minecraftValidated":False}


if __name__=="__main__":
    import argparse
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root",type=Path,default=Path(__file__).resolve().parents[2])
    parser.add_argument("--output",type=Path)
    args=parser.parse_args()
    result=build_geometry(args.root)
    out=args.output or args.root/"reports/skill-particle-library-v3/source-geometry/boss-geometry.json"
    out.parent.mkdir(parents=True,exist_ok=True)
    out.write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding="utf-8")
    print(json.dumps({"output":str(out),**result["validation"]},ensure_ascii=False))
