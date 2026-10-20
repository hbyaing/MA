"""Source-grounded, opaque-core Boss motifs for the independent V3 VFX library.

This is an art asset generator, not gameplay code. Shapes are deliberately broad
enough to retain their silhouette at 32 px. No Gaussian blur, fog field, global
sharpen filter, borrowed sprite or additive-only silhouette is used.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import math
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont


ROLES = ("projectile", "impact", "core", "edge", "cut")
ROLE_NAMES = ("飞行主形", "命中碎形", "蓄力核心", "边界节段", "扫击轨迹")
STYLES = {
    "muyan": dict(name="目魇", model="muyan", colors=("#312538", "#8a668c", "#cdbeda", "#efffff"),
        bones=("eyeball", "iris_spin", "nerve_01", "watcher_1"),
        motifs=("虹瞳神经矢", "虹膜裂放", "七色虹膜", "观测眼节", "视神经钩"),
        basis="球眼、旋转虹膜、神经须和附生观测眼；彩虹只留在虹膜组织内。"),
    "ermei": dict(name="耳魅", model="ermei", colors=("#203c37", "#4c7167", "#65cbbb", "#cdd0b9"),
        bones=("spiral_pinna", "resonance_torso", "left_forearm_soundmarks", "wave_ring_0"),
        motifs=("螺耳声锥", "三阶震音", "骨刻螺耳", "青纹声脊", "回音双弧"),
        basis="暗青共鸣躯、骨白螺耳、青绿声纹；不用通用霓虹频谱棒。"),
    "bichen": dict(name="鼻嗔", model="bichen", colors=("#4b363e", "#ad979b", "#97646d", "#e9ffbb"),
        bones=("single_nostril_face", "dorsal_gland_0", "breathing_belly", "nostril_venom_drips"),
        motifs=("腺毒坠滴", "破腺溅毒", "三房毒腺", "脓腺毒脊", "鼻息毒梭"),
        basis="灰粉肉体、血粉腺囊与黄绿毒液，生物腺体语汇；不用叶片或植物根系。"),
    "shetao": dict(name="舌饕", model="shetao", colors=("#332229", "#879c91", "#a04453", "#e3dcc0"),
        bones=("waterbag_body", "belly_lower_jaw", "long_tongue", "left_palm_mouth"),
        motifs=("根舌獠牙", "饕口噬痕", "水囊腹口", "颚齿边脊", "长舌卷鞭"),
        basis="灰绿水囊皮、暗红长舌、骨牙和多处嘴；酸唾仅作小点缀。"),
    "shenyin": dict(name="身淫", model="shenyin", colors=("#233e48", "#318aa0", "#d571a2", "#75bfa8"),
        bones=("protective_crystal_shell", "exposed_weakpoint_core", "rose_crown_crystal", "crystal_tendril_0"),
        motifs=("三棱晶矛", "甲壳碎晶", "三色棱冠", "感触晶节", "折节晶须"),
        basis="真实多臂棱晶甲壳、青玉/玫瑰/金冠、关节亮节点及折节晶须。"),
    "yihuo": dict(name="意惑", model="yihuo", colors=("#282139", "#8066a4", "#b29ad4", "#e0ceee"),
        bones=("formless_violet_cloud", "mist_voxel_00", "memory_fragment_00", "broken_recollection_0"),
        motifs=("断忆方片", "云忆散片", "妄忆云心", "旧忆阶痕", "离散念带"),
        basis="体素紫云、逃逸云块和方形记忆碎片；用明确团块外轮廓而非镜晶或满屏透明雾。"),
    "pengju": dict(name="彭倨", model="pengju", colors=("#44473b", "#c4c0a0", "#cfaa4b", "#ffe393"),
        bones=("rotting_colossal_head", "massive_broken_jaw", "avarice_crown", "resentment_treasure_shield"),
        motifs=("财甲盾矢", "碎颚金屑", "腐颅空眶", "怨宝护节", "残颚金斩"),
        basis="腐败巨颅、破颚、财宝甲与护盾；暗金点骨，不使用金翅或仙鹤。"),
    "pengzhi": dict(name="彭质", model="pengzhi", colors=("#251d2a", "#514052", "#aa5788", "#e8a0d1"),
        bones=("ruptured_abdomen", "rupture_edges", "tentacle_0_terminal_maw", "left_palm_maw"),
        motifs=("触口肉矛", "裂腹噬瓣", "破腹饥渊", "口须边节", "多口触鞭"),
        basis="紫黑裂腹、褶肉、终端多齿口触手；腐酒不作为所有技能的主形。"),
    "pengjiao": dict(name="彭矫", model="pengjiao", colors=("#292535", "#746785", "#c597dc", "#f1f5e9"),
        bones=("white_soul_heart", "crimson_nuo_ghost_mask", "jade_flowing_dao_robe", "broken_orbit_ring_0"),
        motifs=("傩面游魂", "五相裂袍", "白心五傩", "游魂轨节", "五色袍带"),
        basis="白色魂心、五色傩面道袍幽魂、破轨和地须；保留面具/袍摆识别，不只画五行符号。"),
    "heart_demon": dict(name="心魔·无相", model="xinmo_wuxiang", colors=("#1d1c2b", "#5a4b6c", "#b72952", "#ffa3c3"),
        bones=("face_mirror", "face_rift_seam", "heart_core", "heart_wheel_root", "chain_1"),
        motifs=("裂面心矢", "赤心镜裂", "无相裂面", "妄链锁节", "镜刃血缝"),
        basis="黑曜裂面、赤心、镜轮、妄链与消解肢体；主轮廓为黑曜实面、细红裂缝。"),
}


def _rgb(value):
    return tuple(int(value[i:i + 2], 16) for i in (1, 3, 5))


def _hex(rgb):
    return "#" + "".join(f"{int(v):02x}" for v in rgb[:3])


def _sample_palette(texture, targets):
    """Select actual opaque texels nearest the chosen semantic source swatches."""
    a = np.asarray(texture.convert("RGBA"))
    colors = np.unique(a[..., :3][a[..., 3] > 220], axis=0).astype(np.int32)
    if not len(colors):
        raise ValueError("Boss source texture has no opaque palette samples")
    return [tuple(colors[np.argmin(np.sum((colors - _rgb(t)) ** 2, axis=1))].tolist()) for t in targets]


class Pen:
    """Normalised coordinates, crisp opaque silhouette with thick internal cuts."""
    def __init__(self, palette, size=768):
        self.size = size
        self.im = Image.new("RGBA", (size, size))
        self.d = ImageDraw.Draw(self.im)
        self.dark, self.body, self.accent, self.light = [(*c, 255) for c in palette]

    def xy(self, point):
        # Reserve a real transparent gutter for atlas filtering and wide strokes.
        return ((point[0] * .94 + 1) * self.size / 2, (point[1] * .94 + 1) * self.size / 2)

    def poly(self, points, color=None, outline=None, width=.027):
        pts = [self.xy(p) for p in points]
        self.d.polygon(pts, fill=color or self.body)
        if outline:
            self.d.line(pts + pts[:1], fill=outline, width=max(1, round(width * self.size / 2)), joint="curve")

    def line(self, points, color=None, width=.04):
        self.d.line([self.xy(p) for p in points], fill=color or self.light,
                    width=max(1, round(width * self.size / 2)), joint="curve")

    def ellipse(self, x, y, rx, ry=None, fill=None, outline=None, width=.026):
        ry = rx if ry is None else ry
        self.d.ellipse([self.xy((x - rx, y - ry)), self.xy((x + rx, y + ry))],
                       fill=fill, outline=outline, width=max(1, round(width * self.size / 2)))

    def arc(self, x, y, rx, ry, start, end, color=None, width=.055):
        ts = np.linspace(start, end, max(20, round(abs(end - start) * 22)))
        self.line([(x + rx * math.cos(t), y + ry * math.sin(t)) for t in ts], color, width)

    def curve(self, points, color=None, width=.1):
        if len(points) != 4:
            raise ValueError("Cubic curve needs four points")
        pts = []
        for t in np.linspace(0, 1, 70):
            w = ((1 - t) ** 3, 3 * (1 - t) ** 2 * t, 3 * (1 - t) * t * t, t ** 3)
            pts.append(tuple(sum(wi * p[j] for wi, p in zip(w, points)) for j in range(2)))
        self.line(pts, color, width)
        return pts

    def stroke(self, points, width=.12, fill=None, rim=None):
        self.line(points, rim or self.dark, width + .04)
        self.line(points, fill or self.body, width)

    def diamond(self, x, y, rx, ry, fill=None, outline=None):
        self.poly([(x, y - ry), (x + rx, y), (x, y + ry), (x - rx, y)], fill, outline)


def _rot(p, a):
    return (p[0] * math.cos(a) - p[1] * math.sin(a), p[0] * math.sin(a) + p[1] * math.cos(a))


def _map(points, x=0, y=0, s=1, angle=0):
    return [(x + q[0] * s, y + q[1] * s) for q in (_rot(p, angle) for p in points)]


def _iris(p, x, y, r, colors):
    p.ellipse(x, y, r, fill=p.dark, outline=p.light, width=r * .09)
    for i, color in enumerate(colors):
        a = i * math.tau / len(colors)
        pts = [(x + r * rr * math.cos(t), y + r * rr * math.sin(t))
               for rr, t in ((.93, a), (.93, a + .78), (.40, a + 1.14), (.29, a + .15))]
        p.poly(pts, (*color, 255))
    p.ellipse(x, y, r * .31, fill=p.dark, outline=p.light, width=r * .065)
    p.ellipse(x - r * .18, y - r * .22, r * .1, fill=p.light)


def _eye(p, x, y, s, colors):
    pts = [(x + s * t, y - s * .42 * math.sin(math.pi * (t + 1) / 2)) for t in np.linspace(-1, 1, 22)]
    pts += [(x + s * t, y + s * .42 * math.sin(math.pi * (t + 1) / 2)) for t in np.linspace(1, -1, 22)]
    p.poly(pts, p.body, p.light, .035)
    _iris(p, x, y, s * .33, colors)


def _gland(p, x, y, rx, ry):
    p.ellipse(x, y, rx, ry, p.body, p.dark, .055)
    p.ellipse(x, y - ry * .06, rx * .69, ry * .68, p.accent, p.dark, .023)
    p.ellipse(x + rx * .05, y + ry * .05, rx * .29, ry * .33, p.light)
    p.arc(x, y - ry * .08, rx * .8, ry * .8, 3.7, 5.2, p.light, .027)


def _maw(p, x, y, rx, ry, teeth=4, violet=False):
    p.ellipse(x, y, rx, ry, p.accent, p.dark, .05)
    p.ellipse(x, y, rx * .74, ry * .71, p.dark)
    for i in range(teeth):
        xx = x + (i - (teeth - 1) / 2) * rx * 1.3 / teeth
        w = rx * .40 / teeth
        yy = y - ry * .65 + abs(xx - x) * .2
        p.poly([(xx - w, yy), (xx + w, yy), (xx + w * .2, yy + ry * .54)], p.light)
        yy = y + ry * .65 - abs(xx - x) * .2
        p.poly([(xx - w, yy), (xx + w, yy), (xx - w * .2, yy - ry * .46)], p.light)


def _crystal(p, x, y, s=1, angle=0, accent=None):
    pts = _map([(0, -.78), (.28, -.19), (.21, .55), (0, .74), (-.21, .55), (-.28, -.19)], x, y, s, angle)
    p.poly(pts, accent or p.body, p.dark, .035)
    p.poly(_map([(0, -.78), (.28, -.19), (0, .05), (0, .74), (-.21, .55), (-.10, -.12)], x, y, s, angle), p.light)
    p.line(_map([(0, -.72), (0, .69)], x, y, s, angle), p.dark, .028)


def _mask(p, x, y, s, color):
    pts = _map([(-.37, -.43), (0, -.56), (.37, -.43), (.44, -.02), (.23, .38), (0, .57), (-.23, .38), (-.44, -.02)], x, y, s)
    p.poly(pts, color, p.dark, .04)
    p.poly(_map([(-.34, -.16), (-.07, -.02), (-.17, .09), (-.34, .01)], x, y, s), p.dark)
    p.poly(_map([(.34, -.16), (.07, -.02), (.17, .09), (.34, .01)], x, y, s), p.dark)
    p.line(_map([(0, -.43), (0, -.13)], x, y, s), p.light, .03)
    p.line(_map([(-.14, .28), (0, .20), (.14, .28)], x, y, s), p.light, .045)


def render_material(boss, role, palette, secondary, size=256):
    p = Pen(palette)
    if boss == "muyan":
        rainbow = secondary
        if role == "projectile":
            for dy in (-.17, .13):
                p.stroke([(-.15, dy), (-.40, dy - .11), (-.69, dy + .08), (-.88, dy - .16)], .075, p.accent)
            _eye(p, .24, 0, .63, rainbow)
        elif role == "impact":
            for i in range(8):
                a = i * math.tau / 8
                p.poly(_map([(.30, -.10), (.82, -.045), (.61, .13), (.36, .09)], angle=a), (*rainbow[i % len(rainbow)], 255), p.dark)
            _iris(p, 0, 0, .35, rainbow)
        elif role == "core":
            _iris(p, 0, 0, .76, rainbow)
            for i in range(5):
                a = .2 + i * math.tau / 5
                p.line(_map([(.72, 0), (.88, .05), (.87, .18)], angle=a), p.accent, .06)
        elif role == "edge":
            p.stroke([(-.9, .10), (-.64, -.13), (-.2, .03), (.24, -.04), (.64, .10), (.90, -.07)], .10, p.accent)
            for x in (-.59, .0, .59):
                _eye(p, x, -.02, .24, rainbow)
        else:
            pts = p.curve([(-.82, .54), (.69, .80), (.52, -.55), (.13, -.63)], p.dark, .19)
            p.line(pts, p.accent, .13)
            p.line(pts[8:50], p.light, .035)
            for j in (12, 25, 36):
                x, y = pts[j]
                p.line([(x, y), (x - .13, y + .15), (x - .30, y + .13)], p.body, .05)
            _eye(p, .12, -.63, .31, rainbow)
    elif boss == "ermei":
        if role == "projectile":
            p.poly([(-.68, -.42), (.01, -.3), (.70, 0), (.01, .3), (-.68, .42), (-.48, 0)], p.body, p.light)
            p.arc(-.35, 0, .20, .23, -.8, 4.4, p.accent, .085)
            for xx, rr in ((.20, .30), (.43, .42), (.61, .54)):
                p.arc(xx - .25, 0, rr * .5, rr, -1.0, 1.0, p.accent, .045)
        elif role == "impact":
            for r, w in ((.33, .11), (.57, .095), (.82, .065)):
                p.arc(0, .15, r, r * .83, 3.4, 6.03, p.dark, w + .055)
                p.arc(0, .15, r, r * .83, 3.4, 6.03, p.accent, w)
            p.poly([(-.18, .25), (0, -.03), (.18, .25), (0, .58)], p.light, p.dark)
        elif role == "core":
            spiral = [(math.cos(t) * (.08 + .058 * t) - .03, math.sin(t) * (.08 + .058 * t)) for t in np.linspace(-.3, 10.7, 170)]
            p.line(spiral, p.dark, .21)
            p.line(spiral, p.light, .15)
            p.line([(x + .01, y + .045) for x, y in spiral], p.body, .047)
            p.arc(.10, -.01, .72, .68, -.92, .95, p.accent, .066)
        elif role == "edge":
            pts = [(x, .17 * math.sin(x * math.pi * 3)) for x in np.linspace(-.91, .91, 100)]
            p.stroke(pts, .075, p.accent)
            p.line([(-.88, .33), (.88, .33)], p.light, .05)
            for x in (-.65, 0, .65):
                p.line([(x, -.28), (x, -.16)], p.light, .06)
        else:
            for x, y, rx, ry in ((-.08, -.05, .7, .62), (.09, .10, .47, .45)):
                p.arc(x, y, rx, ry, 3.43, 6.4, p.dark, .16)
                p.arc(x, y, rx, ry, 3.43, 6.4, p.accent, .10)
                p.arc(x, y, rx - .035, ry - .03, 4.1, 5.8, p.light, .031)
            for i in range(3):
                p.line([(-.48 + i * .22, .54), (-.35 + i * .22, .29)], p.light, .06)
    elif boss == "bichen":
        if role == "projectile":
            p.poly([(0, -.88), (.25, -.27), (.37, .17), (.29, .49), (0, .65), (-.29, .49), (-.37, .17), (-.25, -.27)], p.light, p.dark, .045)
            p.poly([(0, -.59), (.16, -.11), (.19, .39), (.02, .50)], p.accent)
            p.line([(-.17, -.14), (-.24, .16), (-.12, .39)], p.body, .065)
            _gland(p, .38, .61, .16, .17)
        elif role == "impact":
            for i, r in enumerate((.69, .55, .73, .58, .67)):
                a = i * math.tau / 5 - .2
                x, y = math.cos(a) * r, math.sin(a) * r
                p.line([(0, .05), (x * .9, y * .9)], p.dark, .15)
                p.line([(0, .05), (x * .9, y * .9)], p.light, .095)
                p.ellipse(x, y, .13, .16, p.light, p.dark)
            _gland(p, 0, .04, .39, .32)
        elif role == "core":
            for x, y, rx, ry in ((-.28, .28, .39, .43), (.30, .29, .39, .43), (0, -.29, .40, .48)):
                _gland(p, x, y, rx, ry)
            p.ellipse(0, -.34, .065, .17, p.dark)
        elif role == "edge":
            p.stroke([(-.86, .23), (-.64, .10), (-.28, .12), (.12, .16), (.56, .05), (.85, .20)], .22, p.accent)
            for x, y, s in ((-.67, -.08, .2), (-.17, -.07, .26), (.4, -.11, .23), (.76, .02, .12)):
                _gland(p, x, y, s, s * 1.07)
        else:
            pts = p.curve([(-.82, .45), (-.29, -.06), (.36, .33), (.73, -.43)], p.dark, .28)
            p.line(pts, p.light, .21)
            p.line(pts[12:49], p.body, .075)
            for x, y, s in ((-.66, -.02, .13), (.09, -.33, .17), (.70, .21, .11)):
                p.ellipse(x, y, s, s * 1.2, p.light, p.accent)
    elif boss == "shetao":
        if role == "projectile":
            p.poly([(-.24, .52), (-.30, -.16), (-.09, -.80), (.16, -.32), (.24, .19), (.13, .52)], p.light, p.dark, .047)
            p.poly([(-.04, -.61), (.07, -.22), (.08, .34), (-.02, .43)], p.body)
            p.poly([(-.28, .28), (-.39, .49), (-.22, .69), (.28, .72), (.42, .51), (.20, .31)], p.accent, p.dark)
            p.line([(.34, .53), (.54, .67), (.72, .48)], p.body, .06)
        elif role == "impact":
            _maw(p, 0, .01, .60, .47, 5)
            for a in (.2, 1.7, 3.0, 4.7):
                p.poly(_map([(.59, -.09), (.86, -.04), (.7, .12)], angle=a), p.light, p.dark)
        elif role == "core":
            p.ellipse(0, 0, .67, .80, p.body, p.dark, .065)
            _maw(p, 0, .04, .52, .69, 4)
            p.poly([(-.08, .15), (.12, .14), (.16, .53), (.0, .70), (-.16, .50)], p.accent)
            p.line([(0, .19), (0, .58)], p.light, .034)
        elif role == "edge":
            p.stroke([(-.89, .24), (-.49, .15), (.02, .18), (.53, .12), (.90, .23)], .20, p.accent)
            for i, x in enumerate((-.67, -.23, .22, .66)):
                tip = -.28 - (i % 2) * .17
                p.poly([(x - .15, .23), (x + .15, .22), (x + .09, -.02), (x - .02, tip)], p.light, p.dark)
        else:
            pts = p.curve([(-.81, .41), (.70, .88), (.81, -.85), (.20, -.63)], p.dark, .28)
            p.line(pts, p.accent, .21)
            p.line([(x - .028, y - .023) for x, y in pts[6:]], p.body, .051)
            p.ellipse(.18, -.62, .17, .12, p.accent, p.dark)
            p.ellipse(.47, -.19, .055, .12, p.light)
    elif boss == "shenyin":
        if role == "projectile":
            _crystal(p, 0, -.05, 1.06)
            p.poly([(.01, -.66), (.28, -.24), (.20, .46), (.03, .17)], p.accent, p.dark)
        elif role == "impact":
            for x, y, s, a in ((-.49, -.34, .43, -.48), (.48, -.34, .52, .52), (-.46, .40, .31, -2.3), (.36, .46, .35, 2.0)):
                _crystal(p, x, y, s, a, p.accent if x > 0 else p.body)
            p.diamond(0, 0, .23, .30, p.light, p.dark)
        elif role == "core":
            for x, y, s, a in ((-.49, .03, .72, -.56), (.49, .03, .72, .56), (-.27, -.07, .85, -.18), (.27, -.07, .85, .18), (0, -.1, 1.0, 0)):
                _crystal(p, x, y, s, a, p.accent if x > 0 else p.body)
            p.diamond(0, .34, .30, .32, p.accent, p.dark)
            p.diamond(0, .29, .12, .16, p.light)
        elif role == "edge":
            p.line([(-.88, .17), (.88, .17)], p.dark, .18)
            p.line([(-.88, .17), (.88, .17)], p.body, .09)
            for x, y in ((-.66, .02), (-.22, -.10), (.22, .03), (.66, -.10)):
                p.poly([(x - .17, y), (x, y - .24), (x + .17, y), (x + .13, y + .28), (x - .13, y + .28)], p.accent if x > 0 else p.body, p.dark)
                p.line([(x, y - .15), (x, y + .20)], p.light, .043)
        else:
            pts = [(-.79, .52), (-.42, .23), (-.07, .31), (.18, -.20), (.55, -.28), (.76, -.63)]
            for i in range(len(pts) - 1):
                x, y = pts[i]; nx, ny = pts[i + 1]
                p.poly([(x - .075, y - .07), (x + .08, y + .12), (nx + .08, ny + .06), (nx - .075, ny - .13)], p.accent if i % 2 else p.body, p.dark)
                p.line([(x, y), (nx, ny)], p.light, .035)
            for x, y in pts[1:-1]:
                p.diamond(x, y, .11, .12, p.light, p.dark)
    elif boss == "yihuo":
        def tile(x, y, w, h, color):
            p.poly([(x - w, y - h), (x + w, y - h), (x + w, y + h), (x - w, y + h)], color, p.dark, .035)
        if role == "projectile":
            for x, y, w, h, c in ((-.57, .32, .15, .18, p.body), (-.16, .04, .28, .30, p.accent), (.36, -.30, .28, .35, p.light)):
                tile(x, y, w, h, c)
            p.line([(.20, -.15), (.51, -.15)], p.body, .075)
            p.line([(.20, -.35), (.42, -.35)], p.body, .04)
        elif role == "impact":
            pts = [(-.47,-.48),(-.12,-.48),(-.12,-.64),(.25,-.64),(.25,-.35),(.55,-.35),(.55,-.02),(.69,-.02),(.69,.31),(.31,.31),(.31,.57),(-.05,.57),(-.05,.39),(-.47,.39),(-.47,.13),(-.69,.13),(-.69,-.20),(-.47,-.20)]
            p.poly(pts, p.body, p.dark, .045)
            tile(.03, -.06, .22, .25, p.light)
            for x, y, s in ((-.72, -.68, .09), (.71, .65, .13), (.73, -.56, .11), (-.68, .65, .1)):
                tile(x, y, s, s, p.accent)
        elif role == "core":
            for x, y, r in ((-.41,.21,.36),(.36,.23,.38),(-.22,-.23,.42),(.28,-.24,.34),(0,.39,.33)):
                p.ellipse(x,y,r,r*.94,p.body,p.dark,.06)
            for x,y,w,h,c in ((-.29,-.16,.16,.20,p.light),(.22,-.07,.18,.15,p.accent),(.0,.34,.13,.14,p.light)):
                tile(x,y,w,h,c)
            p.line([(-.67,.17),(-.53,.09),(-.35,.12)],p.accent,.07)
        elif role == "edge":
            for x,y,w,h,c in ((-.69,.13,.18,.17,p.body),(-.26,-.07,.20,.21,p.light),(.23,.14,.18,.19,p.accent),(.69,-.09,.18,.16,p.body)):
                tile(x,y,w,h,c)
            p.line([(-.41,.36),(.51,.36)],p.accent,.035)
        else:
            p.poly([(-.83,.32),(-.48,.32),(-.48,.08),(-.04,.08),(-.04,-.18),(.38,-.18),(.38,-.47),(.80,-.47),(.80,-.23),(.57,-.23),(.57,.04),(.15,.04),(.15,.31),(-.31,.31),(-.31,.54),(-.83,.54)],p.accent,p.dark,.045)
            p.line([(-.75,.40),(-.40,.40),(-.4,.20),(.05,.20),(.05,-.06),(.47,-.06),(.47,-.32),(.70,-.32)],p.light,.045)
            tile(.57,.49,.13,.10,p.body);tile(-.58,-.32,.09,.12,p.body)
    elif boss == "pengju":
        if role == "projectile":
            p.poly([(-.51,-.53),(.0,-.75),(.51,-.53),(.43,.30),(0,.80),(-.43,.30)],p.accent,p.dark,.06)
            p.poly([(-.31,-.34),(0,-.52),(.31,-.34),(.25,.22),(0,.54),(-.25,.22)],p.body,p.dark,.07)
            p.diamond(0,-.07,.14,.21,p.light,p.dark)
            p.line([(-.35,.25),(0,.64),(.35,.25)],p.light,.04)
        elif role == "impact":
            p.poly([(-.72,-.10),(-.52,.51),(-.20,.69),(.21,.64),(.70,.23),(.55,.0),(.31,.29),(-.19,.36),(-.37,-.04)],p.body,p.dark,.06)
            for x,y,a in ((-.47,.2,-.4),(-.1,.41,0),(.27,.32,.35)):
                p.poly(_map([(-.09,.12),(.10,.12),(.08,-.22),(-.02,-.28)],x,y,angle=a),p.light,p.dark)
            for x,y in ((-.64,-.56),(.03,-.65),(.63,-.45)):
                p.poly([(x-.14,y-.08),(x+.12,y-.13),(x+.09,y+.13),(x-.13,y+.10)],p.accent,p.dark)
        elif role == "core":
            p.poly([(-.57,-.26),(-.42,-.54),(0,-.65),(.42,-.54),(.58,-.23),(.51,.30),(.27,.64),(-.27,.64),(-.51,.30)],p.body,p.dark,.065)
            p.poly([(-.48,-.46),(-.67,-.77),(-.25,-.60),(0,-.91),(.23,-.58),(.67,-.77),(.48,-.46)],p.accent,p.dark,.055)
            for x in (-.27,.27):
                p.ellipse(x,-.11,.17,.19,p.dark)
                p.ellipse(x,-.06,.074,.13,p.light)
            p.poly([(-.08,.09),(.08,.09),(.13,.25),(-.13,.25)],p.dark)
            p.line([(-.31,.43),(-.10,.38),(.12,.45),(.30,.36)],p.dark,.10)
            p.line([(-.18,.35),(-.18,.59)],p.light,.065)
            p.line([(.12,.35),(.12,.55)],p.light,.065)
        elif role == "edge":
            for i,x in enumerate((-.63,0,.63)):
                yy=.05 if i%2 else -.05
                p.poly([(x-.24,yy-.25),(x+.20,yy-.27),(x+.24,yy+.17),(x,yy+.39),(x-.24,yy+.15)],p.accent,p.dark,.06)
                p.poly([(x-.11,yy-.13),(x+.1,yy-.13),(x+.1,yy+.1),(x-.11,yy+.1)],p.dark)
                p.line([(x-.18,yy-.20),(x+.12,yy-.20)],p.light,.04)
        else:
            p.poly([(-.83,.24),(-.60,.57),(-.18,.69),(.26,.50),(.66,.13),(.80,-.52),(.58,-.27),(.36,.01),(-.07,.29),(-.49,.34)],p.accent,p.dark,.06)
            for x,y,a in ((-.38,.33,-.3),(.0,.30,.2),(.36,.02,.7),(.59,-.3,1.1)):
                p.poly(_map([(-.12,.14),(.11,.11),(.0,-.27)],x,y,angle=a),p.body,p.dark)
            p.line([(-.56,.51),(-.18,.57),(.20,.40),(.50,.10)],p.light,.05)
    elif boss == "pengzhi":
        if role == "projectile":
            pts=p.curve([(-.72,.62),(-.24,.48),(-.47,-.47),(.16,-.45)],p.dark,.30)
            p.line(pts,p.body,.24);p.line(pts[8:50],p.accent,.055)
            _maw(p,.37,-.36,.36,.31,3,True)
        elif role == "impact":
            for i in range(5):
                a=i*math.tau/5+.2
                p.poly(_map([(.19,-.15),(.58,-.25),(.85,-.02),(.66,.24),(.35,.16)],angle=a),p.body,p.dark,.05)
                p.line(_map([(.37,0),(.65,.04)],angle=a),p.accent,.08)
            _maw(p,0,0,.34,.31,3,True)
        elif role == "core":
            p.poly([(-.48,-.58),(-.16,-.75),(.14,-.56),(.46,-.68),(.62,-.30),(.43,-.05),(.65,.20),(.34,.56),(.03,.74),(-.34,.61),(-.64,.29),(-.48,.0),(-.65,-.30)],p.body,p.dark,.06)
            p.poly([(-.25,-.50),(0,-.37),(.28,-.49),(.35,-.12),(.22,.10),(.35,.29),(0,.58),(-.32,.26),(-.20,-.04),(-.37,-.18)],p.dark,p.accent,.085)
            for x,y in ((-.32,-.3),(.3,-.15),(-.27,.19),(.19,.37)):
                p.poly([(x-.07,y-.14),(x+.09,y-.09),(x*.4,y+.06)],p.light)
        elif role == "edge":
            pts=[(-.91,.14),(-.61,.23),(-.26,.07),(.18,.20),(.55,.02),(.89,.14)]
            p.stroke(pts,.17,p.body)
            for x,y,rx in ((-.63,-.03,.22),(-.06,.13,.24),(.57,-.10,.22)):
                _maw(p,x,y,rx,rx*.69,3,True)
        else:
            # Leave a transparent gutter for the thick maw-stroke after downsampling.
            pts=p.curve([(-.76,.61),(-.57,-.45),(.59,.68),(.62,-.54)],p.dark,.30)
            p.line(pts,p.body,.24);p.line(pts,p.accent,.055)
            for k,s in ((17,.20),(39,.22),(65,.25)):
                x,y=pts[k];_maw(p,x,y,s,s*.69,3,True)
    elif boss == "pengjiao":
        five=[(*c,255) for c in secondary]
        if role == "projectile":
            p.poly([(-.29,-.08),(-.57,.77),(-.14,.50),(0,.83),(.18,.5),(.49,.72),(.24,-.08)],five[2],p.dark,.045)
            p.line([(-.33,.60),(-.16,.03),(.14,.14),(.29,.59)],p.light,.036)
            _mask(p,0,-.36,.71,five[0])
        elif role == "impact":
            for i,c in enumerate(five):
                a=i*math.tau/5-.3
                p.poly(_map([(.24,-.13),(.58,-.20),(.83,-.09),(.69,.07),(.82,.25),(.38,.17)],angle=a),c,p.dark,.05)
                p.line(_map([(.37,-.05),(.64,-.01)],angle=a),p.light,.035)
            p.diamond(0,0,.25,.30,p.light,p.dark)
        elif role == "core":
            for i,c in enumerate(five):
                a=i*math.tau/5-math.pi/2
                x,y=math.cos(a)*.57,math.sin(a)*.57
                _mask(p,x,y,.49,c)
            p.diamond(0,0,.26,.34,p.light,p.dark)
            p.line([(-.06,-.19),(-.13,.04),(0,.23),(.13,.04),(.06,-.19)],p.body,.035)
        elif role == "edge":
            for i,c in enumerate(five):
                x=-.74+i*.37; yy=.055 if i%2 else -.045
                p.poly([(x-.12,yy-.2),(x+.12,yy-.20),(x+.15,yy+.07),(x+.02,yy+.3),(x-.15,yy+.08)],c,p.dark,.032)
                p.line([(x-.06,yy-.04),(x+.05,yy-.04)],p.dark,.042)
            p.line([(-.83,.37),(-.38,.39),(-.24,.30),(.22,.33),(.37,.40),(.83,.36)],p.light,.034)
        else:
            for i,c in enumerate(five):
                yy=(i-2)*.19
                pts=p.curve([(-.76,yy+.16),(-.26,yy-.29),(.27,yy+.29),(.78,yy-.15)],p.dark,.125)
                p.line(pts,c,.084)
            p.line([(-.69,.23),(-.44,.37)],p.light,.04)
    elif boss == "heart_demon":
        if role == "projectile":
            p.poly([(0,-.84),(.27,-.30),(.23,.41),(0,.68),(-.34,.34),(-.27,-.24)],p.body,p.light,.037)
            p.poly([(-.21,-.35),(-.04,-.21),(-.18,-.04),(-.3,-.18)],p.dark)
            p.poly([(.09,-.27),(.27,-.39),(.30,-.15),(.15,-.02)],p.dark)
            p.line([(0,-.75),(-.06,-.35),(.04,-.03),(-.04,.26),(.02,.60)],p.accent,.10)
            p.line([(-.04,-.39),(.04,-.07),(-.04,.20)],p.light,.026)
        elif role == "impact":
            for i in range(6):
                a=i*math.tau/6+.3
                p.poly(_map([(.34,-.1),(.77,-.16),(.88,.10),(.47,.19)],angle=a),p.body,p.light,.035)
                p.line(_map([(.39,-.06),(.71,.03)],angle=a),p.accent,.054)
            p.poly([(-.25,-.1),(-.15,-.27),(0,-.18),(.15,-.27),(.25,-.1),(0,.32)],p.accent,p.dark,.046)
            p.line([(-.02,-.18),(.07,-.01),(-.04,.16)],p.light,.046)
        elif role == "core":
            p.poly([(-.50,-.58),(-.13,-.73),(.10,-.68),(.49,-.50),(.53,.12),(.26,.58),(0,.77),(-.32,.53),(-.56,.04)],p.body,p.light,.05)
            p.poly([(-.43,-.2),(-.13,-.14),(-.24,.02),(-.45,-.02)],p.dark)
            p.poly([(.15,-.10),(.42,-.24),(.44,.02),(.23,.08)],p.dark)
            p.line([(.06,-.68),(-.08,-.37),(.06,-.10),(-.07,.15),(.03,.36),(-.06,.63)],p.dark,.18)
            p.line([(.06,-.68),(-.08,-.37),(.06,-.10),(-.07,.15),(.03,.36),(-.06,.63)],p.accent,.083)
            p.ellipse(.0,.14,.1,.13,p.light)
        elif role == "edge":
            for i,x in enumerate((-.63,0,.63)):
                p.ellipse(x,0,.29,.19,p.body,p.light,.042)
                p.ellipse(x,0,.17,.09,(0,0,0,0))
                p.poly([(x-.1,-.14),(x-.02,-.39),(x+.13,-.16)],p.accent,p.dark)
                p.line([(x+.16,.06),(x+.22,.13)],p.accent,.05)
        else:
            p.poly([(-.85,.48),(-.46,.58),(-.13,.46),(.11,.26),(.44,.05),(.70,-.32),(.85,-.72),(.42,-.35),(.08,-.01),(-.37,.25)],p.body,p.light,.042)
            p.line([(-.68,.40),(-.35,.36),(-.08,.25),(.16,.02),(.47,-.13),(.67,-.49)],p.accent,.091)
            p.line([(-.34,.34),(-.08,.23),(.16,.00)],p.light,.027)
    else:
        raise ValueError(boss)
    # Premultiplied downsample prevents dark fringes; alpha still stores straight.
    return p.im.convert("RGBa").resize((size, size), Image.Resampling.LANCZOS).convert("RGBA")


def _dilate_transparent_rgb(im, passes=2):
    """Extend colour into alpha-zero gutters without adding visible pixels."""
    a = np.array(im)
    # Discard sub-visible Lanczos lobes; these are not authored glow.
    a[..., 3][a[..., 3] < 4] = 0
    known = a[..., 3] > 0
    rgb = a[..., :3].copy()
    for _ in range(passes):
        old = known.copy()
        for dy, dx in ((0,-1),(0,1),(-1,0),(1,0)):
            source = np.roll(old,(dy,dx),(0,1))
            if dy == -1: source[-1,:] = False
            if dy == 1: source[0,:] = False
            if dx == -1: source[:,-1] = False
            if dx == 1: source[:,0] = False
            take = (~known) & source
            rgb[take] = np.roll(rgb,(dy,dx),(0,1))[take]
            known[take] = True
    a[..., :3] = rgb
    return Image.fromarray(a, "RGBA")


def _metrics(im):
    a=np.array(im.getchannel("A"))
    small=im.convert("RGBa").resize((32,32),Image.Resampling.LANCZOS).convert("RGBA")
    sa=np.array(small.getchannel("A"))
    visible=a>16; solid=a>=224
    partial=(a>16)&(a<224)
    assert np.any(solid), "No solid core"
    assert not np.any(a[:3]) and not np.any(a[-3:]) and not np.any(a[:,:3]) and not np.any(a[:,-3:]), "Clipped sprite"
    assert int((sa>=192).sum())>=18, "No readable opaque silhouette at 32px"
    return {
        "transparentFraction":round(float((a==0).mean()),4),
        "solidCorePixels128":int(solid.sum()),
        "solidCoreShareOfVisible":round(float(solid.sum()/max(1,visible.sum())),4),
        "partialAlphaShareOfVisible":round(float(partial.sum()/max(1,visible.sum())),4),
        "opaqueCorePixels32":int((sa>=192).sum()),
        "maxAlpha32":int(sa.max()),
        "alphaMaskSha256":hashlib.sha256(a.tobytes()).hexdigest(),
        "noClippedBorder":True,
        "criterion":"32px非空且alpha>=192核心不少于18像素；这属于技术可读性下限，不能替代人工审美验收。",
    }


def _font(size):
    for path in ("C:/Windows/Fonts/msyh.ttc", "C:/Windows/Fonts/simhei.ttf", "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"):
        if Path(path).exists():
            return ImageFont.truetype(path,size)
    return ImageFont.load_default()


def _contact(out, materials):
    w,h=1450,1600
    im=Image.new("RGB",(w,h),"#111823");d=ImageDraw.Draw(im)
    f=_font(19);small=_font(14);head=_font(25)
    d.text((22,17),"Boss V3 · 实体纹样 / 128px 与 32px 清晰度检查",font=head,fill="#eef3fa")
    d.text((22,55),"主体高不透明度 · 禁止整图模糊 · 深浅背景双检 · 色彩来自实际模型贴图",font=small,fill="#adb8c6")
    for row,(boss,style) in enumerate(STYLES.items()):
        y=94+row*148
        d.text((17,y+4),style['name'],font=f,fill="#f2f5fa")
        for col,role in enumerate(ROLES):
            m=next(v for v in materials if v['boss']==boss and v['role']==role)
            x=145+col*256
            d.rounded_rectangle((x,y,x+241,y+139),radius=7,fill="#202b37")
            d.text((x+8,y+5),m['name'],font=small,fill="#dce8f1")
            asset=Image.open(out/m['file']).convert('RGBA')
            im.paste(asset,(x+5,y+23),asset)
            tiny=asset.convert('RGBa').resize((32,32),Image.Resampling.LANCZOS).convert('RGBA')
            d.rectangle((x+149,y+33,x+194,y+77),fill="#090f19")
            d.rectangle((x+149,y+87,x+194,y+131),fill="#bcc1b7")
            im.paste(tiny,(x+156,y+39),tiny);im.paste(tiny,(x+156,y+93),tiny)
            d.text((x+203,y+46),"32",font=small,fill="#c0c9d2")
            d.text((x+203,y+100),"32",font=small,fill="#c0c9d2")
    path=out/'preview/materials-boss-v3.png';path.parent.mkdir(parents=True,exist_ok=True);im.save(path)
    return path


def build_materials(root:Path,out:Path)->list[dict]:
    """Write 50 motifs, source PNGs, mcmeta, source evidence and clarity report.

    ``file`` and ``sourceFile`` are relative to ``out``. Every image is already
    coloured; use white particle tint and alpha blend to preserve the model's
    palette and dark silhouette. Ground-edge files are repeatable motifs, NOT a
    baked range ring. Bind placements to authoritative collision geometry.
    """
    root=Path(root);out=Path(out)
    assets=root/'src/main/resources/assets/wuxingxiantu'
    visual_path=root/'src/main/resources/data/wuxingxiantu/boss_visuals.json'
    visuals=json.loads(visual_path.read_text('utf-8'))
    texture_dir=out/'textures/boss_v3';source_dir=out/'source-textures/boss_v3'
    texture_dir.mkdir(parents=True,exist_ok=True);source_dir.mkdir(parents=True,exist_ok=True)
    materials=[]
    for boss,style in STYLES.items():
        visual=next(v for v in visuals if v.get('modelId')==style['model'])
        geo_path=assets/visual['model'];texture_path=assets/visual['texture']
        geo=json.loads(geo_path.read_text('utf-8'))['minecraft:geometry'][0]
        bones={b['name'] for b in geo['bones']}
        for bone in style['bones']:
            if bone not in bones:
                raise ValueError(f"Missing source bone: {boss}/{bone}")
        texture=Image.open(texture_path).convert('RGBA')
        palette=_sample_palette(texture,style['colors'])
        targets=("#fa9e7e","#fff581","#80dc99","#67d6cb","#69b6ff","#b084ee","#e083bf") if boss=='muyan' else ("#e49aad","#9addcf","#9bc8f5","#cdadeb","#f1dfa0")
        secondary=_sample_palette(texture,targets)
        for i,role in enumerate(ROLES):
            ident=f"{boss}_{role}_v3"
            source=render_material(boss,role,palette,secondary,256)
            production=_dilate_transparent_rgb(source.convert('RGBa').resize((128,128),Image.Resampling.LANCZOS).convert('RGBA'))
            _dilate_transparent_rgb(source).save(source_dir/f'{ident}.png')
            production.save(texture_dir/f'{ident}.png')
            (texture_dir/f'{ident}.png.mcmeta').write_text(json.dumps({'texture':{'blur':False,'clamp':True}}),encoding='utf-8')
            metrics=_metrics(production)
            sizes={'projectile':[.30,.85],'impact':[.40,1.0],'core':[.55,1.0],'edge':[.20,.55],'cut':[.35,.90]}
            materials.append(dict(
                id=ident,name=style['motifs'][i],boss=boss,bossName=style['name'],role=role,roleName=ROLE_NAMES[i],
                file=f'textures/boss_v3/{ident}.png',sourceFile=f'source-textures/boss_v3/{ident}.png',
                frames=1,blend='alpha',colorMode='authored-rgba',tint='#ffffff',width=128,height=128,sourceWidth=256,sourceHeight=256,
                alphaMode='straight',opaqueCore=True,blur=False,mipmap=False,filter='nearest',textureClamp=True,
                transparentRgbDilationPixels=2,recommendedSizeBlocks=sizes[role],recommendedMinScreenPixels=24,
                recommendedPlacement='world-oriented / repeat at true range boundary' if role=='edge' else ('path-aligned billboard' if role in ('projectile','cut') else 'billboard / anchored to source bone'),
                sourcePalette=[_hex(c) for c in palette],sourcePalettePolicy='Opaque source texels selected by nearest semantic swatch; no palette inferred from Boss name.',
                secondaryPalette=[_hex(c) for c in secondary] if boss in ('muyan','pengjiao') else [],
                styleBasis=style['basis'],sourceEvidence=dict(
                    visuals=visual_path.relative_to(root).as_posix(),model=geo_path.relative_to(root).as_posix(),
                    texture=texture_path.relative_to(root).as_posix(),bones=list(style['bones']),modelSha256=hashlib.sha256(geo_path.read_bytes()).hexdigest(),
                    textureSha256=hashlib.sha256(texture_path.read_bytes()).hexdigest()),
                sizePolicy='尺寸是单个纹样的美术建议；技能总范围必须由源码形状/radius/width/length驱动，禁止将贴图缩放当成伤害范围。',
                compositing='Use alpha blend with white tint; opaque body first. Optional separate glow may decorate ≤12% alpha and must not replace boundary/body.',
                clarity=metrics,
            ))
    masks=[m['clarity']['alphaMaskSha256'] for m in materials]
    assert len(set(masks))==len(masks),'Duplicate silhouette mask'
    contact=_contact(out,materials)
    report={'version':3,'materialCount':len(materials),'sourceSize':256,'runtimeSize':128,'distinctAlphaMasks':len(set(masks)),
            'smallSizeCheck':32,'minOpaquePixels32':min(m['clarity']['opaqueCorePixels32'] for m in materials),
            'minSolidShare':min(m['clarity']['solidCoreShareOfVisible'] for m in materials),
            'maxPartialAlphaShare':max(m['clarity']['partialAlphaShareOfVisible'] for m in materials),
            'globalBlurUsed':False,'spriteSourceVerified':True,'minecraftRendered':False,
            'contactSheet':contact.relative_to(out).as_posix(),'materials':[{'id':m['id'],**m['clarity']} for m in materials]}
    catalog=out/'catalog';catalog.mkdir(parents=True,exist_ok=True)
    (catalog/'boss-materials-v3.json').write_text(json.dumps(materials,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    (catalog/'boss-material-clarity-v3.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    return materials


if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--root',type=Path,default=Path(__file__).resolve().parents[2])
    parser.add_argument('--out',type=Path)
    args=parser.parse_args()
    destination=args.out or args.root/'build/vfx-v3-materials'
    result=build_materials(args.root,destination)
    print(json.dumps({'materials':len(result),'output':str(destination),'contactSheet':str(destination/'preview/materials-boss-v3.png'),'minOpaquePixels32':min(m['clarity']['opaqueCorePixels32'] for m in result)},ensure_ascii=False))
