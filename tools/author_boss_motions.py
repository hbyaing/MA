"""Author skeletal moves from combat tick beats; never repaint or replace the boss models.

Run explicitly to update runtime JSON. Existing idle/showcase clips are preserved.
Every beat is a body action (not every ring sample or projectile travel tick).
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/cn/blockforge/generated/generatedmod"
RES = ROOT / "src/main/resources"
ASSETS = RES / "assets/wuxingxiantu"
COLORS = ["gold", "jade", "azure", "crimson", "violet"]

# Offsets are relative to the actual server windup. Sweep/volley/chain timing follows BossSkillPrograms.
# Negative values are intentional non-damaging preparations; execution beats never precede windup.
MOVES = {
    "muyan": [
        [(0, "beam"), (16, "beam"), (32, "beam")],
        [(i * 10, f"sweep:{-65 + i * 130 / 6:g}") for i in range(7)],
        [(0, "ring"), (40, "ring")],
        [(0, "clone"), (20, "beam"), (85, "open")],
        [(0, "gaze"), (20, "open")]],
    "ermei": [
        [(0, "fan")], [(0, "ring"), (40, "ring")], [(0, "beam")],
        [(0, "left"), (22, "right"), (44, "slam")],
        [(i * 15, "walls") for i in range(5)],
        [(0, "ring"), (90, "ring"), (150, "ring"), (195, "open")]],
    "bichen": [
        [(0, "spit"), (20, "spit"), (40, "spit")],
        [(i * 10, f"sweep:{-65 + i * 26:g}") for i in range(6)],
        [(0, "radial")], [(0, "dash"), (14, "slam"), (18, "open")],
        [(0, "pull"), (40, "radial")],
        [(i * 10, "fog") for i in range(5)]],
    "shetao": [
        [(0, "left"), (25, "right"), (55, "thrust")],
        [(0, "thrust"), (6, "pull"), (30, "coil")],
        [(i * 18, "rain") for i in range(4)],
        [(i * 10, f"sweep:{-25 + i * 12.5:g}") for i in range(5)],
        [(0, "coil"), (50, "aim"), (82, "thrust"), (110, "chew")],
        [(0, "roll"), (20, "slam"), (23, "open")],
        [(i * 24, f"sector:{i * 72}") for i in range(5)] + [(130, "slam")]],
    "shenyin": [
        [(i * 14, f"tendril:{i}") for i in range(4)],
        [(0, "near"), (35, "far")],
        [(0, "pull"), (65, "slam"), (80, "open")],
        [(i * 15, f"throw:{i}") for i in range(3)],
        [(0, "cage"), (35, "cage")],
        [(0, "tether"), (104, "thrust")],
        [(i * 24, f"tendril:{i}") for i in range(5)] + [(130, "slam"), (138, "open")]],
    "yihuo": [
        [(0, "left"), (8, "right"), (16, "thrust")],
        [(0, "blink"), (28, "thrust")],
        [(i * 4, "echo") for i in range(1, 12)],
        [(0, "clone"), (20, "thrust"), (90, "open")],
        [(i * 15, "walls") for i in range(5)],
        [(0, "read"), (40, "thrust")],
        [(0, "tether"), (114, "thrust")],
        [(i * 30, f"sector:{180 * (i % 2)}") for i in range(4)] + [(126, "open")]],
    "pengju": [
        [(0, "peck"), (26, "peck")], [(0, "dash"), (14, "slam")],
        [(0, "spit")], [(0, "stamp")],
        [(0, "rain"), (26, "rain"), (52, "rain"), (85, "open")],
        [(0, "ring"), (36, "ring")],
        [(0, "guard"), (10, "open")], [(0, "walls"), (30, "thrust")],
        [(0, "rain"), (22, "rain"), (44, "rain"), (106, "dash"), (124, "slam"),
         (156, "ring"), (196, "ring"), (220, "open")]],
    "pengzhi": [
        [(0, "left"), (30, "right")], [(0, "sweep:-65"), (40, "sweep:65"), (55, "open")],
        [(0, "slam"), (35, "ring"), (75, "ring")],
        [(i * 10, f"sweep:{-30 + i * 10}") for i in range(7)],
        [(0, "pull"), (70, "aim"), (95, "spit")],
        [(0, "thrust"), (23, "pull"), (35, "slam")],
        [(0, "pull"), (8, "open")],
        [(0, "slam"), (35, "sweep:65"), (85, "dash"), (102, "slam"), (165, "open")],
        [(0, "slam"), (48, "ring"), (111, "spit"), (120, "open")]],
    "pengjiao": [
        [(0, "dash"), (10, "slam"), (14, "open")],
        [(0, "gold")], [(i * 12, "jade") for i in range(5)],
        [(i * 16, "azure") for i in range(5)],
        [(i * 9, "crimson") for i in range(7)],
        [(0, "violet"), (22, "radial"), (35, "open")],
        [(i * 24, color) for i, color in enumerate(COLORS)],
        [(0, "split")] + [(offset + i * 35, COLORS[(i + actor) % 5])
                          for i in range(4) for offset, actor in [(24, 0), (32, 1)]],
        [(i * 10, f"swap:{i}") for i in range(5)],
        [(i * 22, color) for i, color in enumerate(COLORS)] + [(140, "slam"), (150, "open")]],
    "heart_demon": [
        [(0, "left"), (24, "right"), (52, "thrust")],
        [(0, "dash"), (14, "thrust"), (20, "open")],
        [(0, "ring"), (45, "ring")],
        [(i * 5, "echo") for i in range(1, 12)],
        [(0, "gaze")], [(0, "tether"), (124, "thrust")],
        [(i * 18, f"mirror:{i}") for i in range(4)] + [(100, "open")],
        [(0, "guard"), (28, "ring"), (34, "open")],
        [(0, "fan"), (32, "cross"), (42, "open")],
        [(i * 20, f"sector:{i * 60}") for i in range(6)] + [(145, "slam"), (154, "open")]],
}


def skill_rows():
    rows = []
    catalog = (JAVA / "boss/combat/BossCombatCatalog.java").read_text(encoding="utf-8")
    pattern = r'skill\("([a-z]+)", (\d+), "([^"]+)", (\d+), (\d+), (\d+), (\d+),'
    phases = {"muyan": 2, "ermei": 2, "bichen": 2, "shetao": 2, "shenyin": 2,
              "yihuo": 3, "pengju": 3, "pengzhi": 3, "pengjiao": 3, "heart_demon": 2}
    for boss, n, name, unlock, w, r, cool in re.findall(pattern, catalog):
        rows.append((boss, int(n), name, int(unlock), int(w), int(r), phases[boss]))
    heart = (JAVA / "boss/combat/HeartDemonSkillPrograms.java").read_text(encoding="utf-8")
    for n, name, unlock, w, r, cool in re.findall(r'skill\((\d+),"([^"]+)",(\d+),(\d+),(\d+),(\d+),', heart):
        rows.append(("heart_demon", int(n), name, int(unlock), int(w), int(r), 2))
    if len(rows) != 77:
        raise ValueError(f"Expected 77 authored skills, found {len(rows)}")
    return rows


def beats_for(boss, n, phase, history=12, variant=""):
    beats = list(MOVES[boss][n - 1])
    if boss == "muyan" and phase > 1:
        if n == 2: beats += [(98, "beam")]
        if n == 3: beats += [(89, "ring"), (129, "ring")]
    if boss == "ermei" and n == 2 and phase > 1:
        beats += [(93, "ring"), (133, "ring")]
    if boss == "bichen" and n == 3 and phase > 1:
        beats += [(56, "radial")]
    if boss == "shetao" and n == 7:
        beats = beats[:-1]
        if phase > 1: beats += [(156, "dash"), (172, "slam")]
        beats += [(165 if phase == 1 else 180, "open")]
    if boss == "shenyin" and n == 2 and phase > 1:
        beats = [(0, "far"), (35, "near")]
    if boss == "yihuo":
        if n == 3:
            beats = [(i * 4, "echo") for i in range(1, history)] or [(0, "echo")]
            if phase > 1: beats += [(history * 4 + 20, "blink"), (history * 4 + 52, "thrust")]
        if n == 6:
            beats = {"near": [(0, "ring")], "middle": [(0, "thrust"), (20, "open")],
                     "far": [(0, "stamp"), (40, "dash"), (60, "thrust")]}[variant or "near"]
        if n == 8 and phase > 2: beats += [(87, "stamp")]
    if boss == "pengju":
        if n == 2 and phase > 1: beats += [(67, "dash"), (81, "slam")]
        if n == 3 and phase > 2: beats += [(82, "dash"), (98, "slam")]
    if boss == "pengzhi" and n == 8 and phase > 2: beats += [(142, "fan")]
    if boss == "pengjiao" and n == 3 and phase > 1: beats += [(92, "gold")]
    if boss == "heart_demon":
        if n == 1 and phase > 1: beats += [(72, "thrust")]
        if n == 2 and phase > 1: beats = [(0, "dash"), (14, "thrust"), (52, "fan"), (64, "open")]
        if n == 3 and phase > 1: beats += [(93, "ring"), (138, "ring")]
        if n == 4:
            beats = [(i * 5, "echo") for i in range(1, history)] or [(0, "echo")]
            if phase > 1: beats += [(history * 5 + 56, "stamp")]
        if n == 5 and phase > 1: beats += [(44, "thrust"), (50, "open")]
    return sorted(beats)


def pose(boss, descriptor, preparing=False):
    """Use the actual anatomy of each model, with a distinct silhouette for each action family."""
    kind, _, arg = descriptor.partition(":")
    number = float(arg or 0)
    side = -1 if kind == "left" else 1
    twist = number if kind in {"sweep", "sector"} else side * 28 if kind in {"left", "right"} else 0
    factor = -.55 if preparing else 1
    p = {}

    def put(bone, channel, values):
        p.setdefault(bone, {})[channel] = [round(v, 4) for v in values]

    def rotate(bone, x=0, y=0, z=0): put(bone, "rotation", [x * factor, y * factor, z * factor])
    def pos(bone, x=0, y=0, z=0): put(bone, "position", [x * factor, y * factor, z * factor])
    def scale(bone, x=1, y=None, z=None):
        put(bone, "scale", [x, x if y is None else y, x if z is None else z])

    strike = kind in {"left", "right", "thrust", "fan", "tendril", "gold", "single", "cross"}
    shooting = kind in {"spit", "beam", "rain", "fog", "gaze", "read"}
    impact = kind in {"slam", "stamp", "peck", "violet"}
    expanding = kind in {"ring", "radial", "cage", "far", "walls", "sector"}
    inward = kind in {"pull", "coil", "guard", "near", "charge", "tether"}
    open_pose = kind == "open"
    rotate("root", -18 if kind in {"dash", "roll"} else 0, twist * .16)
    pos("root", 0, (-2 if impact else 2 if shooting or expanding else 0), 0)

    if boss == "muyan":
        rotate("eyeball", -12 if shooting else 10 if impact else 0, twist)
        scale("iris_spin", .45 if preparing else 1.2 if shooting else 1.05,
              .45 if preparing else 1.2 if shooting else 1.05, .9 if preparing else 1.1)
        scale("eyeball", 1.08 if expanding else 1, 1.08 if expanding else 1,
              1.16 if shooting and not preparing else .93)
        rotate("optic_roots", 0, 50 if kind == "clone" else -30 if inward else 0)
        for i in range(1, 13):
            spread = (40 if expanding or open_pose else -24 if preparing or inward else 15)
            rotate(f"nerve_{i:02d}", spread, 0, (-1 if i % 2 else 1) * spread)
        if kind == "clone": scale("eyeball", .82 if preparing else 1.12)
    elif boss == "ermei":
        rotate("resonance_torso", 18 if impact else -12 if shooting else 0, twist * .4)
        scale("resonance_torso", 1.12 if preparing or inward else .92 if impact else 1.04,
              .94 if impact else 1.05, 1.18 if preparing or shooting else 1)
        rotate("faceless_head", -18 if shooting else 16 if impact else 0, twist * .35)
        for label, sign in [("left", -1), ("right", 1)]:
            active = strike and (kind == "fan" or sign == side) or expanding or kind == "walls"
            rotate(label + "_arm", -65 if active else 40 if impact else -28,
                   sign * (45 if kind == "walls" else 0), sign * (65 if expanding else 35 if active else 15))
            rotate(label + "_forearm", -35 if preparing else 22 if active else 0)
        rotate("pelvis", 12 if impact else 0)
    elif boss == "bichen":
        rotate("bloated_body", 22 if impact or kind == "dash" else -12 if shooting else 0, twist * .5)
        rotate("single_nostril_face", -24 if preparing else 30 if shooting or impact else 0, twist)
        scale("single_nostril_face", 1.16 if preparing and shooting else 1,
              1.08 if preparing else 1, 1.2 if preparing and shooting else .94)
        scale("breathing_belly", 1.2 if inward or preparing else .88 if shooting else 1.06)
        for i in range(6):
            size = 1.25 if preparing else .82 if kind == "radial" else 1.08 if kind == "fog" else 1
            scale(f"belly_gland_{i}", size)
            scale(f"dorsal_gland_{i}", size)
        rotate("left_arm", -20, 0, -30 if expanding else -12)
        rotate("right_arm", -20, 0, 30 if expanding else 12)
    elif boss == "shetao":
        rotate("waterbag_body", -12 if shooting else 30 if impact else 0, twist * .3,
               145 if kind == "roll" else 0)
        scale("waterbag_body", 1.15 if inward or preparing else 1, .82 if kind == "roll" else 1.05,
              1.18 if inward else 1)
        rotate("belly_lower_jaw", 35 if preparing else 48 if shooting else 12 if open_pose else 24)
        rotate("crown_mouth", -20 if shooting else 0)
        rotate("long_tongue", -25 if kind == "rain" else 12 if impact else -8, twist,
               side * 12 if strike else 0)
        scale("long_tongue", .85 if strike else 1, 1, .55 if preparing or inward else 2.5 if kind == "thrust" else 1.65 if strike else 1)
        rotate("sentient_tongue_tip", 55 if kind in {"coil", "chew", "pull"} else -12, twist * .3)
        for label, sign in [("left", -1), ("right", 1)]:
            rotate(label + "_arm", -45 if preparing else 25 if strike else -12, 0, sign * (45 if expanding else 12))
    elif boss == "shenyin":
        rotate("prismatic_body", 25 if impact else -10 if shooting else 0, twist * .2)
        scale("protective_crystal_shell", .85 if inward else 1.14 if expanding or open_pose else 1)
        scale("exposed_weakpoint_core", 1.25 if preparing or open_pose else .88 if impact else 1)
        for i in range(5):
            active = kind in {"tendril", "throw"} and i == int(number) % 5 or strike and i == 0
            rotate(f"crystal_tendril_{i}", -55 if active else 25 if inward else -24 if expanding else 0,
                   twist * .5, (i - 2) * (16 if expanding else 4))
            rotate(f"tendril_{i}_joint_1", 55 if preparing or inward else -45 if active else 12)
            rotate(f"tendril_{i}_joint_2", 40 if preparing else -20 if active else 0)
            if active: scale(f"crystal_tendril_{i}", 1, 1, .85 if preparing else 1.35)
        for label, sign in [("left", -1), ("right", 1)]:
            rotate(label + "_front_shell", 0, sign * (32 if open_pose or expanding else -15 if inward else 0))
    elif boss == "yihuo":
        rotate("formless_violet_cloud", 18 if strike else -12 if shooting else 0, twist,
               side * 16 if strike else 0)
        scale("formless_violet_cloud", .65 if preparing or kind == "blink" else 1.35 if expanding else 1,
              1.4 if strike else .8 if kind == "blink" else 1,
              1.35 if strike else 1)
        rotate("flickering_memories", 0, -55 if preparing else 80 if kind in {"clone", "echo", "blink"} else twist)
        for i in range(8):
            rotate(f"memory_fragment_{i:02d}", 0, (i - 4) * (14 if expanding or kind == "tether" else 5), 35 if strike else 0)
            pos(f"memory_fragment_{i:02d}", (i - 4) * (.6 if expanding else .15), 0, -5 if kind == "tether" else -3 if strike else 0)
    elif boss == "pengju":
        rotate("rotting_colossal_head", -28 if preparing else 38 if impact else -12 if shooting else 0, twist)
        rotate("massive_broken_jaw", 48 if preparing or shooting else 5 if impact else 22)
        rotate("armored_cranium", -12 if inward else 10 if open_pose else 0)
        scale("resentment_treasure_shield", .75 if preparing else 1.25 if inward else 1.1 if expanding else .9)
        rotate("resentment_treasure_shield", 0, -50 if preparing else 70 if expanding else 0)
        rotate("left_hoarded_pendant", 0, -40 if kind == "walls" else 0, -25 if expanding else 0)
        rotate("right_hoarded_pendant", 0, 40 if kind == "walls" else 0, 25 if expanding else 0)
        rotate("hoarded_treasure_armor", 0, 0, 12 if open_pose else 0)
    elif boss == "pengzhi":
        rotate("ruptured_abdomen", 25 if impact else -18 if shooting else 0, twist * .35)
        scale("ruptured_abdomen", 1.22 if preparing or inward else .88 if shooting or impact else 1.05,
              1.12 if inward else .92 if impact else 1, 1.22 if preparing else 1)
        rotate("blind_face_maw_jaw", 50 if shooting or inward else 8 if impact else 20)
        rotate("buried_eyeless_head", -18 if preparing else 22 if shooting else 0, twist)
        for i in range(6):
            active = i == 0 if kind == "single" else strike and i % 2 == (0 if side == -1 else 1) or expanding or kind == "sweep"
            rotate(f"tentacle_{i}_segment_0", -60 if preparing and (active or impact) else 55 if active or impact else 10,
                   twist, (-1 if i % 2 else 1) * (35 if expanding else 10))
            rotate(f"tentacle_{i}_segment_1", -30 if preparing else 28 if active else 10)
            rotate(f"tentacle_{i}_segment_2", -20 if preparing else 18 if active else 0)
        for label, sign in [("left", -1), ("right", 1)]:
            rotate(label + "_weighted_arm", -65 if preparing else 45 if impact else 0, 0, sign * 18)
    elif boss == "pengjiao":
        scale("five_mirage_orbit", .5 if preparing or kind in {"dash", "slam"} else 1.35 if kind == "split" else 1.1)
        scale("fivefold_light_core", 1.2 if preparing else 1.35 if expanding or impact else .9)
        rotate("rotating_spectral_shell", 0, -40 if preparing else 70 if expanding or kind == "swap" else twist)
        for i, color in enumerate(COLORS):
            selected = kind == color or kind == "swap" and i == int(number) % 5 or strike and i == 0
            rotate(color + "_apparition", -24 if preparing else 22 if selected else 0,
                   (i - 2) * (15 if kind == "split" else 0))
            pos(color + "_apparition", 0, 2 if selected else 0, -7 if selected and not preparing else 3 if preparing else 0)
            rotate(color + "_right_arm", -75 if selected else -20 if preparing else 0, 0, -25 if selected else 0)
            rotate(color + "_left_arm", -55 if selected and kind in {"azure", "violet"} else 0)
            if selected: scale(color + "_apparition", 1.08 if not preparing else .9)
    elif boss == "heart_demon":
        rotate("body", 22 if impact else -12 if shooting else 0, twist * .6)
        rotate("chest", -12 if preparing else 18 if impact else 0)
        rotate("head", -18 if shooting else 12 if impact else 0, twist * .25)
        rotate("right_arm", -100 if preparing else -45 if kind == "thrust" else 55 if strike else -70 if expanding else -25,
               -35 if kind == "left" else 35 if kind == "right" else 0, -38 if strike else -20)
        rotate("right_forearm", -48 if preparing else -8 if strike else 20)
        rotate("left_arm", -80 if inward or kind in {"tether", "walls"} else 55 if kind == "cross" else -65 if expanding else -20,
               25 if kind == "tether" else 0, 45 if expanding else 15)
        rotate("left_forearm", -45 if preparing or inward else 15)
        scale("heart_core", 1.25 if preparing or open_pose else .88 if impact else 1.08)
        rotate("heart_wheel_root", 0, -30 if preparing else 65 if expanding or kind == "mirror" else twist)
        rotate("face_left", 0, -30 if shooting else 0)
        rotate("face_right", 0, 30 if shooting else 0)
        for i in range(1, 4): rotate(f"chain_{i}", -25 if preparing else 35 if kind == "tether" else 0, i * 12)
    else:
        raise ValueError(boss)
    if open_pose:
        # Exposed pose reads as recovery rather than an extra strike.
        p = {bone: channels for bone, channels in p.items() if bone in {
            "root", "heart_core", "exposed_weakpoint_core", "protective_crystal_shell", "hoarded_treasure_armor"}}
        rotate("root", 14, 0, 5)
        pos("root", 0, -2, 0)
    return p


def clip(boss, w, recovery, beats):
    duration = max(w + recovery, max(t for t, _ in beats) + max(16, recovery))
    samples = {0: {}, duration: {}}
    first = beats[0][1]
    samples[max(1, round(w * .65))] = pose(boss, first, True)
    samples[max(2, w - 3)] = pose(boss, first, True)
    previous = w - 3
    for tick, action in beats:
        if tick - previous >= 9:
            samples[tick - 6] = pose(boss, action, True)
        samples[tick] = pose(boss, action)
        next_tick = min((t for t, _ in beats if t > tick), default=duration)
        samples[min(tick + 3, next_tick - 1)] = pose(boss, action)
        if next_tick - tick >= 16:
            samples[tick + min(12, (next_tick - tick) // 2)] = {}
        previous = tick
    channels = {(b, c) for sample in samples.values() for b, data in sample.items() for c in data}
    bones = {}
    for b, c in sorted(channels):
        neutral = [1, 1, 1] if c == "scale" else [0, 0, 0]
        track = {}
        for tick, sample in sorted(samples.items()):
            track[f"{tick / 20:.3f}"] = sample.get(b, {}).get(c, neutral)
        bones.setdefault(b, {})[c] = track
    return {"loop": False, "animation_length": duration / 20, "bones": bones}, duration


def main():
    definitions_path = RES / "data/wuxingxiantu/boss_visuals.json"
    definitions = json.loads(definitions_path.read_text(encoding="utf-8"))
    models = {d["id"]: d for d in definitions}
    files, motions = {}, []
    for row in skill_rows():
        boss, n, name, unlock, w, recovery, phases = row
        model = "xinmo_wuxiang" if boss == "heart_demon" else boss
        definition = models["heart_demon_phase2" if boss == "heart_demon" else boss]
        path = ASSETS / definition["animation"]
        data = files.setdefault(path, json.loads(path.read_text(encoding="utf-8")))
        for phase in range(unlock, phases + 1):
            variants = [""]
            if boss == "yihuo" and n == 6: variants = ["near", "middle", "far"]
            if boss == "yihuo" and n == 3 or boss == "heart_demon" and n == 4:
                variants = [f"history_{i}" for i in range(13)]
            for variant in variants:
                history = int(variant.removeprefix("history_")) if variant.startswith("history_") else 12
                suffix = f"_{variant}" if variant else ""
                skill = f"{boss}_{n:02d}{suffix}"
                animation = f"animation.{model}.combat_{n:02d}{suffix}" + (f"_p{phase}" if phase > 1 else "")
                relative = beats_for(boss, n, phase, history, variant)
                beats = [(w + offset, action) for offset, action in relative]
                authored, duration = clip(boss, w, recovery, beats)
                data["animations"][animation] = authored
                definition["clips"][animation] = duration / 20
                motions.append(dict(skill=skill, phase=phase, animation=animation, windup=w, duration=duration,
                                    beats=[dict(tick=t, pose=p) for t, p in beats]))
                # Keep existing debug names valid, even when live combat selects a contextual variant.
                if variant and (variant == "near" or variant == "history_12"):
                    alias = f"animation.{model}.combat_{n:02d}" + (f"_p{phase}" if phase > 1 else "")
                    data["animations"][alias] = authored
                    definition["clips"][alias] = duration / 20
                if phase == unlock and (not variant or variant in {"near", "history_12"}):
                    alias = f"animation.{model}.combat_{n:02d}"
                    data["animations"][alias] = authored
                    definition["clips"][alias] = duration / 20
    basics = (JAVA / "boss/combat/BossBasicAttacks.java").read_text(encoding="utf-8")
    names = re.findall(r'Map.entry\("([a-z_]+)",new Attack\("([^"]+)",(\d+),(\d+)', basics)
    actions = {"muyan": "beam", "ermei": "left", "bichen": "peck", "shetao": "left",
               "shenyin": "tendril:0", "yihuo": "beam", "pengju": "peck", "pengzhi": "single",
               "pengjiao": "gold", "heart_demon": "right"}
    for boss, name, w, r in names:
        w, r = int(w), int(r)
        model = "xinmo_wuxiang" if boss == "heart_demon" else boss
        d = models["heart_demon_phase2" if boss == "heart_demon" else boss]
        animation = f"animation.{model}.basic_attack"
        data = files[ASSETS / d["animation"]]
        authored, duration = clip(boss, w, r, [(w, actions[boss])])
        data["animations"][animation] = authored
        d["clips"][animation] = duration / 20
        motions.append(dict(skill=boss + "_basic", phase=1, animation=animation, windup=w, duration=duration,
                            beats=[dict(tick=w, pose=actions[boss])]))
    if len(names) != 10: raise ValueError("All ten bosses need a basic attack")
    models["heart_demon_phase1"]["clips"].update({
        m["animation"]: m["duration"] / 20 for m in motions
        if m["skill"].startswith("heart_demon_") and m["phase"] == 1})
    # Validate before writing; wrong bone names otherwise fail silently at runtime.
    for path, data in files.items():
        d = next(d for d in definitions if d["animation"] and ASSETS / d["animation"] == path)
        geo = json.loads((ASSETS / d["model"]).read_text(encoding="utf-8"))
        actual = {b["name"] for b in geo["minecraft:geometry"][0]["bones"]}
        for motion in motions:
            if motion["animation"] not in data["animations"]: continue
            missing = set(data["animations"][motion["animation"]]["bones"]) - actual
            if missing: raise ValueError((motion["animation"], missing))
    for path, data in files.items(): path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    definitions_path.write_text(json.dumps(definitions, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    (RES / "data/wuxingxiantu/boss_motions.json").write_text(json.dumps(motions, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Authored {len(motions)} contextual motion timelines in {len(files)} existing animation files; 10 basic attacks.")


if __name__ == "__main__":
    main()
