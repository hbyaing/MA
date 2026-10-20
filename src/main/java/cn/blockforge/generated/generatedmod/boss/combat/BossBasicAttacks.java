package cn.blockforge.generated.generatedmod.boss.combat;

import java.util.Map;
import net.minecraft.world.phys.Vec3;

/** Ordinary attacks share skill occupancy, telegraphs and damage budgets with the special moves. */
public final class BossBasicAttacks {
    public record Attack(String name,int windup,int recovery,int cooldown,double range,double angle,
                         float damage,boolean ranged) {
        public BossCombatCatalog.Skill skill(String boss) {
            String model=boss.equals("heart_demon")?"xinmo_wuxiang":boss;
            return new BossCombatCatalog.Skill(boss+"_basic",name,1,windup,recovery,cooldown,
                    boss.equals("heart_demon")?"animation.xinmo_wuxiang.echo_weapon_slash":"animation."+model+".basic_attack",ranged?"短蓄力后射击，横移避开":"蓄力后近身攻击，侧移或退出打击范围");
        }
    }
    private static final Map<String,Attack> ATTACKS=Map.ofEntries(
            Map.entry("muyan",new Attack("瞳光点射",18,18,60,14,0,.045F,true)),
            Map.entry("ermei",new Attack("单臂声拍",18,20,60,5.5,75,.055F,false)),
            Map.entry("bichen",new Attack("鼻首顶撞",20,22,64,5,70,.055F,false)),
            Map.entry("shetao",new Attack("短舌抽击",18,22,60,6,75,.055F,false)),
            Map.entry("shenyin",new Attack("单须刺击",20,22,64,6.5,60,.055F,false)),
            Map.entry("yihuo",new Attack("念片掷击",20,20,64,14,0,.045F,true)),
            Map.entry("pengju",new Attack("颅首短啄",22,24,70,6,65,.06F,false)),
            Map.entry("pengzhi",new Attack("独触拍击",24,26,76,7,75,.065F,false)),
            Map.entry("pengjiao",new Attack("单核冲掌",20,22,64,6,70,.055F,false)),
            Map.entry("heart_demon",new Attack("照影平斩",18,22,60,5,80,.055F,false)));
    public static Attack get(String id){return ATTACKS.get(id);}
    public static void cast(BossCombatController c,Attack attack) {
        Vec3 from=c.start(),towards=c.aim();
        if(attack.ranged())c.projectile(from,towards,attack.windup(),.48,false,attack.damage());
        else c.cone(from,towards,attack.range(),attack.angle(),attack.windup(),attack.damage());
    }
    private BossBasicAttacks(){}
}
