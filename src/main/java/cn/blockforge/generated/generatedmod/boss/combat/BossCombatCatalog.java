package cn.blockforge.generated.generatedmod.boss.combat;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable combat tuning shared by the server scheduler and client presentation. */
public final class BossCombatCatalog {
    /** All durations are game ticks; phase is the first phase in which this skill is available. */
    public record Skill(String id, String name, int phase, int windup, int recovery,
                        int cooldown, String animation, String hint) {
        public Skill {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(animation, "animation");
            Objects.requireNonNull(hint, "hint");
            if (id.isBlank() || name.isBlank() || animation.isBlank() || hint.isBlank()
                    || phase < 1 || phase > 3 || windup < 1 || recovery < 1 || cooldown < 1) {
                throw new IllegalArgumentException("Invalid boss skill: " + id);
            }
        }
    }

    /** color is RGB; healthMultiplier multiplies the encounter's reference heart-sword damage. */
    public record Profile(String id, int color, int phases, double healthMultiplier,
                          List<Skill> skills) {
        public Profile {
            Objects.requireNonNull(id, "id");
            skills = List.copyOf(skills);
            if (id.isBlank() || phases < 1 || phases > 3 || !Double.isFinite(healthMultiplier)
                    || healthMultiplier <= 0 || skills.isEmpty()) {
                throw new IllegalArgumentException("Invalid boss profile: " + id);
            }
            for (int i = 0; i < skills.size(); i++) {
                Skill skill = skills.get(i);
                if (!skill.id().equals(id + "_" + String.format(java.util.Locale.ROOT, "%02d", i + 1))
                        || skill.phase() > phases) {
                    throw new IllegalArgumentException("Invalid skill order/phase: " + skill.id());
                }
            }
        }
    }

    private static final Map<String, Profile> PROFILES;

    static {
        Map<String, Profile> profiles = new LinkedHashMap<>();
        add(profiles, "muyan", 0xDA78FF, 2, 80, List.of(
                skill("muyan", 1, "七彩幻光", 1, 24, 20, 160, "瞳孔收缩后射出光束，横移离开亮线"),
                skill("muyan", 2, "扫界虹光", 1, 32, 28, 220, "看清光束扫动方向，绕到目魇侧后方"),
                skill("muyan", 3, "虹环震荡", 1, 30, 24, 260, "虹环贴地扩散，起跳越过环线"),
                skill("muyan", 4, "幻影分身", 2, 40, 32, 400, "稳定瞳光与脚下符纹指向真身，先避交叉射线"),
                skill("muyan", 5, "目魇凝视", 2, 60, 50, 600, "聚光结束前背对目魇，或躲入棱镜背后")
        ));
        add(profiles, "ermei", 0x76DAE8, 2, 90, List.of(
                skill("ermei", 1, "声刃扇击", 1, 24, 20, 160, "双臂后张即将释放扇形声刃，向侧后方躲开"),
                skill("ermei", 2, "低鸣震环", 1, 30, 24, 220, "腹部收紧后音环扩散，起跳越过地面波纹"),
                skill("ermei", 3, "声纹追猎", 1, 32, 24, 260, "缓转声弹持续追踪，横向绕行或借石笋挡住声弹"),
                skill("ermei", 4, "三拍震地", 1, 36, 40, 300, "按三次抬臂节奏换位，避开依次亮起的震区"),
                skill("ermei", 5, "双壁合鸣", 2, 44, 32, 400, "两侧声墙向内合拢，从波纹缺口穿过"),
                skill("ermei", 6, "寂静终曲", 2, 64, 56, 660, "辨认波纹前奏，起跳越过音环，或启动场边符台打断")
        ));
        add(profiles, "bichen", 0x8FC657, 2, 100, List.of(
                skill("bichen", 1, "腐液三吐", 1, 26, 24, 180, "鼻孔鼓起后连吐三弹，向空地横移引导落点"),
                skill("bichen", 2, "瘴息横扫", 1, 34, 28, 240, "头部偏转后横扫毒息，绕到背侧或离开扇面"),
                skill("bichen", 3, "毒腺排射", 1, 32, 28, 280, "毒腺依次鼓起，从放射毒刺之间的缺口通过"),
                skill("bichen", 4, "循香扑压", 1, 36, 34, 300, "闻香低伏后锁定扑击落点，横移让扑击落空"),
                skill("bichen", 5, "吸瘴逆流", 2, 44, 40, 420, "瘴气沿地面收回，避开流路并攻击鼓胀毒腺"),
                skill("bichen", 6, "百瘴封庭", 2, 64, 54, 680, "毒雾分区逼近，朝净化炉和未亮起的地面转移")
        ));
        add(profiles, "shetao", 0xF18A86, 2, 110, List.of(
                skill("shetao", 1, "舌刃三叠", 1, 24, 34, 160, "沿未出舌的一侧绕行，最后直刺落空后反击"),
                skill("shetao", 2, "卷舌夺食", 1, 30, 28, 220, "舌尖卷起后伸出，横移躲开或攻击外伸舌头"),
                skill("shetao", 3, "酸雨落盘", 1, 36, 28, 280, "把酸液落点引向外圈，离开标记后不要折返"),
                skill("shetao", 4, "辣火长嗝", 1, 40, 34, 320, "腹部发红后喷火，绕到侧面或借食台挡火"),
                skill("shetao", 5, "贪尝百味", 1, 44, 42, 440, "击毁供养甜食，将舌饕的卷舌引向苦根"),
                skill("shetao", 6, "滚腹压席", 2, 36, 38, 380, "身体收圆后翻滚，横移或诱导它撞向食台"),
                skill("shetao", 7, "五味翻涌", 2, 64, 56, 700, "五味扇区依次喷发，转移到已经喷发完的区域")
        ));
        add(profiles, "shenyin", 0xEE9DDF, 2, 120, List.of(
                skill("shenyin", 1, "晶须点刺", 1, 24, 22, 160, "晶纹记录落脚点，持续移动并避开旧标记"),
                skill("shenyin", 2, "触臂横断", 1, 30, 30, 220, "观察触臂抬起位置，按先近或先远顺序内外换位"),
                skill("shenyin", 3, "引力心潮", 1, 40, 38, 360, "核心收拢后牵引，借晶簇阻挡并在收招时攻击核心"),
                skill("shenyin", 4, "掷晶成林", 1, 34, 28, 280, "把晶块落点引到场边，落地晶簇可作为掩体"),
                skill("shenyin", 5, "晶牢合拢", 1, 46, 36, 420, "晶牢升起前离开圈线，或从明显裂口脱出"),
                skill("shenyin", 6, "缠身刺链", 2, 40, 40, 460, "晶链亮起后靠近攻击根部，或借晶柱切断连线"),
                skill("shenyin", 7, "五触莲华", 2, 66, 58, 720, "沿五次穿刺的空隙转移，最后一击前退出中心")
        ));
        add(profiles, "yihuo", 0xAB83E8, 3, 130, List.of(
                skill("yihuo", 1, "念刃追问", 1, 26, 22, 160, "稳定符纹标记实体念刃，横移避开实线方向"),
                skill("yihuo", 2, "镜步袭心", 1, 32, 28, 220, "先看镜痕终点，侧移避开显形后的突袭"),
                skill("yihuo", 3, "旧影回放", 1, 38, 32, 300, "旧影将重走先前路线，离开地面记录的足迹"),
                skill("yihuo", 4, "真假易位", 1, 40, 34, 360, "换位后稳定脚下符纹才是真身，先避交叉攻击"),
                skill("yihuo", 5, "妄界割裂", 2, 46, 36, 420, "镜墙成形前进入明亮通道，避开碎裂的墙面"),
                skill("yihuo", 6, "观心反制", 2, 40, 38, 400, "按施法时远近变招，看清圈线或落点后换位反击"),
                skill("yihuo", 7, "念锁同心", 3, 46, 42, 480, "念锁连线亮起后攻击心结，或借镜墙截断连线"),
                skill("yihuo", 8, "心境倒映", 3, 70, 60, 760, "跟随预告的实体化区域转移，倒映结束后攻击真身")
        ));
        add(profiles, "pengju", 0xFFD167, 3, 145, List.of(
                skill("pengju", 1, "金颅啄击", 1, 24, 24, 160, "头颅后仰后连啄两次，绕侧并在第二击后反击"),
                skill("pengju", 2, "逐利冲锋", 1, 30, 28, 220, "金甲亮起后锁定冲线，横移让头颅撞空"),
                skill("pengju", 3, "散财飞刃", 1, 28, 24, 240, "口中聚金后扇射飞刃，穿过间隙或绕到侧后方"),
                skill("pengju", 4, "金印镇身", 1, 36, 28, 300, "金印固定落点后立即离开，落地重印可作掩体"),
                skill("pengju", 5, "落金连雨", 1, 40, 42, 360, "三批落金追逐旧位置，持续移动且不要走回旧落点"),
                skill("pengju", 6, "聚宝旋环", 1, 42, 36, 380, "两条金环之间留有安全带，按轨迹调整远近"),
                skill("pengju", 7, "宝甲反震", 2, 38, 34, 400, "正面金甲合拢将反震，绕背或等冲击落空后反击"),
                skill("pengju", 8, "贪念索命", 2, 46, 40, 460, "左右金印夹击前先诱导朝向，再穿过侧面缺口"),
                skill("pengju", 9, "万金崩殿", 3, 70, 60, 800, "依次躲分区落金、俯冲和金环，甲裂后集中反击")
        ));
        add(profiles, "pengzhi", 0xCC786A, 3, 155, List.of(
                skill("pengzhi", 1, "双触重拍", 1, 26, 28, 180, "左右触手交替重拍，第一拍后换到另一侧空隙"),
                skill("pengzhi", 2, "拖地横扫", 1, 32, 30, 240, "触手压低后横扫，起跳或提前退到扫击范围外"),
                skill("pengzhi", 3, "压腹震地", 1, 38, 38, 300, "腹部鼓起后跃压，先离开落点再越过扩散冲击"),
                skill("pengzhi", 4, "腐酿喷柱", 1, 34, 32, 280, "喉部发亮后缓慢转向喷吐，绕侧或持续横移"),
                skill("pengzhi", 5, "吞天吸息", 1, 42, 40, 380, "张口吸气后正面喷吐，横向脱离锥面或借掩体截断"),
                skill("pengzhi", 6, "探触擒摔", 2, 36, 34, 360, "独立触手抬起后直抓，侧移诱使触手插地"),
                skill("pengzhi", 7, "腐宴回流", 2, 48, 44, 460, "酒液沿地面流回腹部，避开流路并攻击暴露腹腔"),
                skill("pengzhi", 8, "酒狂三叠", 2, 42, 48, 500, "依次避前拍、横扫、腹撞，最后踉跄时反击"),
                skill("pengzhi", 9, "五脏翻江", 3, 72, 60, 820, "先退出近圈、再回内圈、最后绕侧躲过重吐")
        ));
        add(profiles, "pengjiao", 0x97DCF2, 3, 170, List.of(
                skill("pengjiao", 1, "合流撞击", 1, 24, 26, 160, "五核收拢后短距撞击，侧移后趁核心展开反击"),
                skill("pengjiao", 2, "金相·贯心", 1, 32, 26, 240, "金核瞄准线停止移动时横移，躲开贯穿光矛"),
                skill("pengjiao", 3, "木相·逐步生棘", 1, 34, 30, 280, "木核记录落脚点，持续换位且不沿原路折返"),
                skill("pengjiao", 4, "水相·回环", 1, 38, 32, 320, "水环向内收缩，从显眼缺口穿出"),
                skill("pengjiao", 5, "火相·赤轮", 1, 38, 34, 340, "双火轮沿弧线行进，看旋向从轮间空隙通过"),
                skill("pengjiao", 6, "土相·坠岳", 1, 42, 38, 380, "土核膨胀后重砸，离开落点并站在放射裂隙之间"),
                skill("pengjiao", 7, "五方连袭", 2, 48, 48, 460, "五核按闪光顺序突袭，横移进入上一击留下的空隙"),
                skill("pengjiao", 8, "分化追猎", 2, 48, 42, 500, "优先攻击正在追击的活动核心，击停一个可降低压力"),
                skill("pengjiao", 9, "移宫换位", 2, 44, 40, 460, "相邻核心连线亮起后离开切割带，换位停止时反击"),
                skill("pengjiao", 10, "五气归元", 3, 76, 60, 900, "沿分批汇聚的缺口换位，躲过合体落击后集中输出")
        ));
        PROFILES = Collections.unmodifiableMap(profiles);
    }

    private BossCombatCatalog() {}

    /** Returns null for visual-only entities and unknown IDs. */
    public static Profile get(String id) {
        return PROFILES.get(id);
    }

    /** Stable progression order; neither the collection nor its skill lists can be modified. */
    public static Collection<Profile> all() {
        return PROFILES.values();
    }

    private static Skill skill(String boss, int number, String name, int phase, int windup,
                               int recovery, int cooldown, String hint) {
        String suffix = String.format(java.util.Locale.ROOT, "%02d", number);
        return new Skill(boss + "_" + suffix, name, phase, windup, recovery, cooldown,
                "animation." + boss + ".combat_" + suffix, hint);
    }

    private static void add(Map<String, Profile> profiles, String id, int color, int phases,
                            double healthMultiplier, List<Skill> skills) {
        if (profiles.put(id, new Profile(id, color, phases, healthMultiplier, skills)) != null) {
            throw new IllegalArgumentException("Duplicate boss: " + id);
        }
    }
}
