"""Validate the complete boss rig/tick contract without Gradle or a game process."""
import json
import math
import unittest
from pathlib import Path
from author_boss_motions import skill_rows

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
ASSETS = RES / "assets/wuxingxiantu"


def read(path):
    return json.loads(path.read_text(encoding="utf-8"))


class BossMotions(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.motions = read(RES / "data/wuxingxiantu/boss_motions.json")
        cls.by_key = {(m["skill"], m["phase"]): m for m in cls.motions}
        cls.defs = {d["id"]: d for d in read(RES / "data/wuxingxiantu/boss_visuals.json")}

    def test_all_ten_basics_and_all_unlocked_skill_phases(self):
        self.assertEqual(len(self.motions), len(self.by_key), "Duplicate motion key")
        bosses = {row[0] for row in skill_rows() if row[0] != "heart_demon"}
        # 心魔保留上一版的 12 个无相动作；它不使用本轮九个 Boss 的 authored combat clips。
        self.assertEqual(len(bosses), 9)
        for boss in bosses:
            self.assertIn((boss + "_basic", 1), self.by_key)
        for boss, n, _, unlock, w, recovery, phases in skill_rows():
            if boss == "heart_demon": continue
            for phase in range(unlock, phases + 1):
                suffixes = [""]
                if boss == "yihuo" and n == 6: suffixes = ["_near", "_middle", "_far"]
                if boss == "yihuo" and n == 3 or boss == "heart_demon" and n == 4:
                    suffixes = [f"_history_{i}" for i in range(13)]
                for suffix in suffixes:
                    motion = self.by_key[(f"{boss}_{n:02d}{suffix}", phase)]
                    self.assertEqual(motion["windup"], w)
                    self.assertGreaterEqual(motion["duration"], max(b["tick"] for b in motion["beats"]) + recovery)

    def test_rig_names_valid_and_no_missing_clips(self):
        for motion in self.motions:
            if motion["skill"].startswith("heart_demon_"): continue
            name = motion["animation"].split(".")[1]
            d = self.defs["heart_demon_phase2" if name == "xinmo_wuxiang" else name]
            geo = read(ASSETS / d["model"])["minecraft:geometry"][0]
            actual = {b["name"] for b in geo["bones"]}
            clip = read(ASSETS / d["animation"])["animations"][motion["animation"]]
            self.assertTrue(set(clip["bones"]) <= actual, motion["animation"])
            self.assertEqual(clip["animation_length"], motion["duration"] / 20)
            self.assertEqual(d["clips"][motion["animation"]], clip["animation_length"])
            self.assertIs(clip["loop"], False)

    def test_keyframes_finite_ordered_and_recover_to_neutral(self):
        for motion in self.motions:
            if motion["skill"].startswith("heart_demon_"): continue
            model = motion["animation"].split(".")[1]
            d = self.defs["heart_demon_phase2" if model == "xinmo_wuxiang" else model]
            clip = read(ASSETS / d["animation"])["animations"][motion["animation"]]
            self.assertEqual(sorted(b["tick"] for b in motion["beats"]), [b["tick"] for b in motion["beats"]])
            self.assertGreaterEqual(min(b["tick"] for b in motion["beats"]), motion["windup"])
            for bone, channels in clip["bones"].items():
                for channel, track in channels.items():
                    times = [float(t) for t in track]
                    self.assertEqual(times, sorted(times))
                    self.assertEqual(times[0], 0)
                    self.assertEqual(times[-1], clip["animation_length"])
                    neutral = [1, 1, 1] if channel == "scale" else [0, 0, 0]
                    self.assertEqual(next(iter(track.values())), neutral)
                    self.assertEqual(list(track.values())[-1], neutral)
                    self.assertTrue(all(len(v) == 3 and all(math.isfinite(x) for x in v) for v in track.values()))
                    if channel == "scale": self.assertTrue(all(x > 0 for v in track.values() for x in v))

    def test_multihit_timing_against_known_server_strikes(self):
        # These independent fixtures lock the real damaging moments from server programs.
        def ticks(skill, phase): return [b["tick"] for b in self.by_key[(skill, phase)]["beats"]]
        self.assertEqual(ticks("shetao_01", 1), [24, 49, 79])
        self.assertEqual(ticks("ermei_04", 1), [36, 58, 80])
        self.assertEqual(ticks("bichen_01", 1), [26, 46, 66])
        self.assertEqual(ticks("pengju_01", 1), [24, 50])
        self.assertEqual(ticks("pengzhi_01", 1), [26, 56])
        self.assertIn(30 + 67, ticks("pengju_02", 2))
        self.assertIn(28 + 82, ticks("pengju_03", 3))
        self.assertEqual(ticks("yihuo_06_near", 2), [40])
        self.assertEqual(ticks("yihuo_06_middle", 2), [40, 60])
        self.assertEqual(ticks("yihuo_06_far", 2), [40, 80, 100])

    def test_basics_are_distinct_and_mirror_form_has_timing(self):
        seen = set()
        for motion in self.motions:
            if motion["skill"].startswith("heart_demon_") or not motion["skill"].endswith("_basic"): continue
            model = motion["animation"].split(".")[1]
            d = self.defs["heart_demon_phase2" if model == "xinmo_wuxiang" else model]
            clip = read(ASSETS / d["animation"])["animations"][motion["animation"]]
            signature = json.dumps(clip, sort_keys=True)
            self.assertNotIn(signature, seen)
            seen.add(signature)
            self.assertGreaterEqual(motion["windup"], 18)
            self.assertEqual(len(motion["beats"]), 1)
        self.assertEqual(len(seen), 9)


if __name__ == "__main__":
    unittest.main(verbosity=2)
