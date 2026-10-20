package cn.blockforge.generated.generatedmod.boss.combat;

import java.util.List;
import net.minecraft.world.phys.Vec3;
import static cn.blockforge.generated.generatedmod.boss.combat.BossCombatController.*;

/** Mirror form teaches five patterns; the unmasked form adds five and changes the old timing. */
public final class HeartDemonSkillPrograms {
    public static final BossCombatCatalog.Profile PROFILE=new BossCombatCatalog.Profile("heart_demon",0xC786ED,2,190,List.of(
            skill(1,"照影三斩",1,28,30,180,"echo_weapon_slash","横移躲过左右斩，末段直刺后反击"),
            skill(2,"踏影穿心",1,34,36,240,"echo_weapon_slash","影线锁定后侧移，让突进落空"),
            skill(3,"心潮震环",1,36,38,300,"heart_pulse","起跳越过贴地心潮，注意二阶段回流"),
            skill(4,"旧念追身",1,42,40,360,"mirror_flower","离开先前走过的足迹，避免原路折返"),
            skill(5,"照心凝视",1,60,50,480,"face_rift","聚光结束前背对心魔，或借掩体遮断视线"),
            skill(6,"妄念锁链",2,44,48,460,"delusion_chains","击碎锁链根部心结，或用掩体切断连线"),
            skill(7,"镜花劫阵",2,48,46,500,"mirror_flower","避开交叉镜线，击碎明亮阵心使心魔露出破绽"),
            skill(8,"业火反照",2,52,50,520,"karma_reversal","收镜期间停手，避开环击后再攻击暴露心核"),
            skill(9,"无相裂界",2,48,46,480,"face_rift","扇面先裂、直线后贯，绕到侧后方反击"),
            skill(10,"万念俱灰",2,76,70,900,"all_thoughts_ash","沿依次熄灭的扇区换位，末段撤出中心再反击")
    ));

    private static BossCombatCatalog.Skill skill(int index,String name,int phase,int windup,int recovery,int cooldown,String clip,String hint) {
        return new BossCombatCatalog.Skill(String.format(java.util.Locale.ROOT,"heart_demon_%02d",index),name,phase,
                windup,recovery,cooldown,"animation.xinmo_wuxiang."+clip,hint);
    }

    public static void cast(BossCombatController c,int number) {
        int w=c.windup();Vec3 p=c.start(),d=c.direction();
        switch(number) {
            case 1->{
                c.cone(p,p.add(rotate(d,-35)),8,90,w,.075F);
                c.cone(p,p.add(rotate(d,35)),9,90,w+24,.075F);
                c.line(p,p.add(d.scale(18)),1.8,w+52,.10F);
                if(c.phase()>1)c.line(p,p.add(rotate(d,28).scale(18)),1.6,w+72,.08F);
            }
            case 2->{
                c.dash(c.aim(),w,14,3.5,.12F);
                if(c.phase()>1)c.later(w+22,()->c.cone(c.origin(),c.target(),10,85,30,.085F));
                c.later(w+(c.phase()>1?64:20),()->c.expose(50));
            }
            case 3->{
                wave(c,p,w,false);
                if(c.phase()>1)c.later(w+65,()->wave(c,p,28,true));
            }
            case 4->{
                var history=c.history();
                if(history.size()<2)c.circle(c.aim(),3,w,.10F);
                else for(int i=1;i<history.size();i++)c.line(history.get(i-1),history.get(i),2,w+i*5,.075F);
                if(c.phase()>1)c.later(w+history.size()*5+24,()->c.column(c.target(),3,32,.10F));
            }
            case 5->{
                c.gaze(w,.18F,c.phase()==1);
                if(c.phase()>1){c.later(w+12,()->c.line(c.origin(),c.target(),1.6,32,.08F));c.later(w+50,()->c.expose(60));}
            }
            case 6->{
                Vec3 root=c.clamp(c.aim().add(rotate(d,90).scale(6)));
                var knot=c.node("knot",root,w+150,()->c.interrupt(90));
                c.fx(1,root,c.aim(),0,.45F,c.profile.color(),w,w+110,"");
                for(int t=w;t<w+96;t+=12)c.later(t,()->{
                    if(knot.active()&&!c.clearSight(root,c.target())){knot.discard();c.interrupt(75);}
                });
                c.later(w+96,()->{if(knot.active()){c.line(root,c.target(),2,28,.16F);knot.discard();}});
            }
            case 7->{
                var core=c.node("knot",c.middle(),w+140,()->c.interrupt(100));
                for(int i=0;i<4;i++) {
                    Vec3 from=c.middle().add(rotate(d,i*90+45).scale(15));
                    c.fx(4,from.add(0,1,0),from,1,1,c.profile.color(),w,w+100,"");
                    c.line(from,c.aim().add(horizontal(c.aim().subtract(from)).scale(6)),1.8,w+i*18,.085F);
                }
                c.later(w+100,()->{core.discard();c.expose(70);});
            }
            case 8->{
                c.pressure(0);c.guard(w);
                c.fx(2,p,p,10,3,c.profile.color(),w,w+6,"");
                c.later(w,()->{
                    c.ring(p,10,3,28,.10F+c.pressure()*.01F,false);
                    c.later(34,()->{c.pressure(0);c.expose(100);});
                });
            }
            case 9->{
                c.cone(p,p.add(d),22,110,w,.12F);
                c.line(p,p.add(rotate(d,65).scale(26)),2,w+32,.085F);
                c.line(p,p.add(rotate(d,-65).scale(26)),2,w+32,.085F);
                c.later(w+42,()->c.expose(70));
            }
            case 10->{
                Vec3 mid=c.middle();
                for(int i=0;i<6;i++)c.cone(mid,mid.add(rotate(d,i*60)),28,48,w+i*20,.075F);
                c.circle(mid,8,w+145,.15F);
                c.later(w+154,()->{c.teleport(mid);c.expose(140);});
            }
            default->throw new IllegalArgumentException("Missing heart demon skill: "+number);
        }
    }

    private static void wave(BossCombatController c,Vec3 p,int delay,boolean inward) {
        for(int i=0;i<6;i++)c.ring(p,inward?24-i*4:4+i*4,3,delay+i*9,.075F,true);
    }
    private HeartDemonSkillPrograms(){}
}
