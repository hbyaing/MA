package cn.blockforge.generated.generatedmod.boss.combat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;
import static cn.blockforge.generated.generatedmod.boss.combat.BossCombatController.*;

/** Explicit timelines; every damaging step is preceded by its own telegraph. */
public final class BossSkillPrograms {
    public static void cast(BossCombatController c,int number) {
        switch(c.profile.id()) {
            case "heart_demon"->HeartDemonSkillPrograms.cast(c,number);
            case "muyan"->muyan(c,number);
            case "ermei"->ermei(c,number);
            case "bichen"->bichen(c,number);
            case "shetao"->shetao(c,number);
            case "shenyin"->shenyin(c,number);
            case "yihuo"->yihuo(c,number);
            case "pengju"->pengju(c,number);
            case "pengzhi"->pengzhi(c,number);
            case "pengjiao"->pengjiao(c,number);
            default->throw new IllegalArgumentException("No combat timeline: "+c.profile.id());
        }
    }
    private static void muyan(BossCombatController c,int n) {
        int w=c.windup();Vec3 p=c.start(),d=c.direction();
        switch(n) {
            case 1->{for(int i=0;i<3;i++)c.later(i*16,()->c.projectile(c.origin(),c.target(),w,.65,false,.075F));}
            case 2->{sweep(c,p,d,22,7,-65,65,w,.07F);if(c.phase()>1)c.later(w+70,()->c.line(c.point(-19,0),c.target(),1.4,28,.08F));}
            case 3->{wave(c,p,w,.085F,false);if(c.phase()>1)c.later(w+65,()->wave(c,p,24,.07F,true));}
            case 4->{mirrors(c,2,w,130);c.later(w+85,()->c.expose(60));}
            case 5->c.gaze(w,.20F);
            default->bad(c,n);
        }
    }
    private static void ermei(BossCombatController c,int n) {
        int w=c.windup();Vec3 p=c.start(),d=c.direction();
        switch(n) {
            case 1->c.cone(p,p.add(d),17,95,w,.09F);
            case 2->{wave(c,p,w,.085F,false);if(c.phase()>1)c.later(w+65,()->wave(c,p,28,.07F,true));}
            case 3->c.projectile(p,c.aim(),w,.28,true,.12F);
            case 4->{for(int i=0;i<3;i++)c.later(i*22,()->c.circle(c.target(),3.2,w,.08F));}
            case 5->walls(c,w,c.aim().z-c.middle().z,7);
            case 6->{wave(c,p,w,.075F,false);c.later(w+60,()->wave(c,p,30,.075F,true));c.later(w+120,()->{wave(c,p,30,.08F,false);c.later(75,()->c.expose(80));});}
            default->bad(c,n);
        }
    }
    private static void bichen(BossCombatController c,int n) {
        int w=c.windup();Vec3 p=c.start(),d=c.direction();
        switch(n) {
            case 1->{for(int i=0;i<3;i++)c.later(i*20,()->{Vec3 at=c.target();c.circle(at,2.5,w,.06F);c.zone(at,2.5,w+6,90,.025F,"poison");});}
            case 2->sweep(c,p,d,15,6,-65,65,w,.075F);
            case 3->{radial(c,p,w,8,0,.07F);if(c.phase()>1)c.later(w+32,()->radial(c,p,24,8,22.5,.065F));}
            case 4->{c.dash(c.aim(),w,14,4,.13F);c.later(w+18,()->c.expose(40));}
            case 5->{for(int i=0;i<5;i++){Vec3 from=c.point(Math.sin(i*Math.PI*2/5)*19,Math.cos(i*Math.PI*2/5)*19);c.line(from,p,2,w,.065F);}c.later(w+8,()->{c.drainZones("poison");c.pressure(c.pressure()+1);radial(c,c.origin(),32,8,15,.075F+c.pressure()*.008F);});}
            case 6->{for(int i=0;i<5;i++){Vec3 at=c.point(Math.sin(i*Math.PI*2/5)*15,Math.cos(i*Math.PI*2/5)*15);c.zone(at,6,w+i*10,140,.03F,"poison");}c.later(w+55,()->c.expose(70));}
            default->bad(c,n);
        }
    }
    private static void shetao(BossCombatController c,int n) {
        int w=c.windup();Vec3 p=c.start(),d=c.direction();
        switch(n) {
            case 1->{c.cone(p,p.add(rotate(d,-40)),7,100,w,.075F);c.cone(p,p.add(rotate(d,40)),8,100,w+25,.075F);c.line(p,p.add(d.scale(16)),1.6,w+55,.10F);if(c.phase()>1)c.line(p,p.add(rotate(d,22).scale(16)),1.5,w+55,.08F);}
            case 2->{c.line(p,c.aim(),1.4,w,.075F);c.node("weak",p.add(d.scale(5)),w+65,()->c.interrupt(70));c.later(w+6,()->{if(horizontal(c.target().subtract(p)).dot(d)>.9)c.pull(p,20,24,.12,16);});}
            case 3->{for(int i=0;i<4;i++)c.later(i*18,()->c.zone(c.target(),3,w,110,.025F,"acid"));}
            case 4->sweep(c,p,d,14,5,-25,25,w,.07F);
            case 5->feast(c,w);
            case 6->{c.dash(c.aim().add(d.scale(5)),w,20,5,.14F);c.later(w+23,()->c.expose(60));}
            case 7->{sectors(c,p,w,5,.085F);if(c.phase()>1)c.later(w+120,()->c.dash(c.target(),36,16,4,.10F));c.later(w+165,()->c.expose(80));}
            default->bad(c,n);
        }
    }
    private static void shenyin(BossCombatController c,int n) {
        int w=c.windup();Vec3 p=c.start(),d=c.direction();
        switch(n) {
            case 1->{for(int i=0;i<4;i++)c.later(i*14,()->c.column(c.target(),2,w,.07F));}
            case 2->{if(c.phase()==1){c.circle(p,6,w,.10F);c.ring(p,13,6,w+35,.10F,false);}else{c.ring(p,13,6,w,.10F,false);c.circle(p,6,w+35,.10F);}}
            case 3->{c.pull(p,w,75,.10,18);c.circle(p,3,w+65,.12F);c.later(w+80,()->c.expose(100));}
            case 4->{for(int i=0;i<3;i++){Vec3 at=c.clamp(c.aim().add(rotate(d,i*120).scale(3)));c.circle(at,2,w+i*15,.085F);c.later(w+i*15+6,()->c.node("crystal",at,360,null));}}
            case 5->{Vec3 at=c.aim();for(int i=0;i<4;i++){Vec3 side=rotate(new Vec3(1,0,0),i*90);if(i!=1)c.line(at.add(side.scale(4)).add(rotate(side,90).scale(-4)),at.add(side.scale(4)).add(rotate(side,90).scale(4)),1.5,w,.09F);}var crack=c.node("knot",at.add(0,0,4),w+100,()->c.interrupt(65));c.later(w,()->{if(crack.active())c.ring(at,4,1.5,35,.13F,false);});}
            case 6->tether(c,p,w,80);
            case 7->{sectors(c,p,w,5,.08F);c.circle(p,7,w+130,.16F);c.later(w+138,()->c.expose(120));}
            default->bad(c,n);
        }
    }
    private static void yihuo(BossCombatController c,int n) {
        int w=c.windup();Vec3 p=c.start(),d=c.direction();
        switch(n) {
            case 1->{c.fx(1,p,p.add(rotate(d,-22).scale(20)),0,1.5F,SAFE,w,w+8,"");c.line(p,p.add(d.scale(24)),1.5,w+16,.09F);c.fx(1,p,p.add(rotate(d,22).scale(20)),0,1.5F,SAFE,w+8,w+16,"");}
            case 2->{Vec3 to=c.clamp(c.aim().add(rotate(d,90).scale(5)));c.fx(1,p,to,0,1,SAFE,w,w+4,"");c.later(w,()->{c.teleport(to);c.cone(c.origin(),c.target(),9,85,28,.12F);});}
            case 3->{List<Vec3> old=c.history();for(int i=1;i<old.size();i++)c.line(old.get(i-1),old.get(i),2,w+i*4,.08F);if(old.size()<2)c.circle(c.aim(),3,w,.09F);if(c.phase()>1)c.later(w+old.size()*4+20,()->{Vec3 to=c.clamp(c.target().add(5,0,0));c.teleport(to);c.cone(to,c.target(),9,75,32,.08F);});}
            case 4->{mirrors(c,2,w,140);c.later(w+90,()->c.expose(70));}
            case 5->walls(c,w,c.aim().z-c.middle().z,6);
            case 6->{double distance=p.distanceTo(c.aim());if(distance<7)c.ring(p,9,7,w,.12F,false);else if(distance<16){c.line(p,c.aim(),2,w,.12F);c.later(w+20,()->c.expose(50));}else{c.column(c.aim(),4,w,.10F);c.later(w+10,()->c.dash(c.target(),30,20,3,.085F));}}
            case 7->{Vec3 knot=c.clamp(c.aim().add(-4,0,3));tether(c,knot,w,90);}
            case 8->{Vec3 mid=c.middle();for(int i=0;i<4;i++){Vec3 side=new Vec3(i%2==0?1:-1,0,0);c.cone(mid,mid.add(side),35,176,w+i*30,.085F);}if(c.phase()>2)c.later(w+55,()->c.circle(c.target(),2.5,32,.07F));c.later(w+126,()->c.expose(110));}
            default->bad(c,n);
        }
    }
    private static void pengju(BossCombatController c,int n) {
        int w=c.windup();Vec3 p=c.start(),d=c.direction();
        switch(n) {
            case 1->{c.cone(p,p.add(d),7,75,w,.085F);c.cone(p,p.add(d),8,90,w+26,.09F);}
            case 2->{c.dash(c.aim().add(d.scale(3)),w,14,4,.13F);if(c.phase()>1)c.later(w+35,()->c.dash(c.target(),32,14,4,.10F));}
            case 3->{for(int i=-2;i<=2;i++)c.projectile(p,p.add(rotate(d,i*16).scale(24)),w,.72,false,.085F);}
            case 4->{Vec3 at=c.aim();c.column(at,3,w,.13F);c.later(w+8,()->c.node("cover",at,260,null));}
            case 5->{for(int i=0;i<3;i++){final int wave=i;c.later(i*26,()->{Vec3 at=c.target();if(c.phase()>1&&wave==2)c.ring(at,6,2,w,.10F,false);else c.column(at,3.2,w,.10F);});}c.later(w+85,()->c.expose(65));}
            case 6->{c.ring(p,8,2.3,w,.085F,false);c.ring(p,16,2.3,w,.085F,false);c.ring(p,11,2.3,w+36,.085F,false);c.ring(p,20,2.3,w+36,.085F,false);}
            case 7->{c.guard(w+35);c.cone(p,p.add(d),12,100,w,.13F);c.later(w+10,()->c.expose(65));}
            case 8->{Vec3 left=c.clamp(c.aim().add(rotate(d,90).scale(7))),right=c.clamp(c.aim().add(rotate(d,-90).scale(7)));c.fx(4,left.add(0,1,0),left,1,1,c.profile.color(),w,w+55,"");c.fx(4,right.add(0,1,0),right,1,1,c.profile.color(),w,w+55,"");c.later(w,()->{Vec3 aim=c.target();c.line(left,aim.add(horizontal(aim.subtract(left)).scale(9)),2,30,.11F);c.line(right,aim.add(horizontal(aim.subtract(right)).scale(9)),2,30,.11F);});}
            case 9->{for(int i=0;i<3;i++)c.column(c.point(-12+i*12,6),5,w+i*22,.09F);c.later(w+70,()->c.dash(c.target(),36,18,5,.12F));c.later(w+130,()->wave(c,c.origin(),26,.085F,false));c.later(w+220,()->c.expose(130));}
            default->bad(c,n);
        }
        if(c.phase()==3&&n==3)c.later(w+50,()->c.dash(c.target(),32,16,4,.10F));
    }
    private static void pengzhi(BossCombatController c,int n) {
        int w=c.windup();Vec3 p=c.start(),d=c.direction();
        switch(n) {
            case 1->{c.cone(p,p.add(rotate(d,-35)),9,75,w,.09F);c.cone(p,p.add(rotate(d,35)),11,100,w+30,.09F);}
            case 2->{wave(c,p,w,.10F,false);c.later(w+55,()->c.expose(50));}
            case 3->{Vec3 at=c.aim();c.circle(at,4,w,.12F);c.later(w,()->c.teleport(at));c.later(w+10,()->wave(c,at,25,.08F,false));}
            case 4->{sweep(c,p,d,23,7,-30,30,w,.075F);c.zone(c.aim(),3,w+20,140,.025F,"wine");}
            case 5->{c.pull(p,w,70,.11,19);c.cone(p,p.add(d),24,70,w+95,.14F);}
            case 6->{c.line(p,c.aim(),1.5,w,.10F);c.node("weak",p.add(d.scale(5)),w+80,()->c.interrupt(75));c.later(w+5,()->{if(horizontal(c.target().subtract(p)).dot(d)>.94){c.pull(p,18,16,.14,16);c.circle(c.target(),2.5,30,.075F);}});}
            case 7->{for(int i=0;i<5;i++){Vec3 from=c.point(Math.sin(i*Math.PI*2/5)*19,Math.cos(i*Math.PI*2/5)*19);c.line(from,p,2,w,.075F);}c.later(w+8,()->{c.drainZones("wine");c.pressure(c.pressure()+1);c.heal(.025F);});}
            case 8->{c.cone(p,p.add(d),10,80,w,.08F);c.ring(p,13,9,w+35,.08F,true);c.later(w+55,()->c.dash(c.target(),30,17,5,.10F));if(c.phase()>2)c.later(w+110,()->c.cone(c.origin(),c.origin().subtract(d),10,110,32,.075F));c.later(w+165,()->c.expose(110));}
            case 9->{c.circle(p,9,w,.12F);c.ring(p,24,12,w+48,.10F,false);c.later(w+75,()->c.cone(c.origin(),c.target(),25,100,36,.13F+c.pressure()*.008F));c.later(w+120,()->{c.pressure(0);c.expose(140);});}
            default->bad(c,n);
        }
    }
    private static void pengjiao(BossCombatController c,int n) {
        int w=c.windup();Vec3 p=c.start(),d=c.direction();
        switch(n) {
            case 1->{c.dash(c.aim(),w,10,4,.10F);c.later(w+14,()->c.expose(35));}
            case 2->c.line(p,p.add(d.scale(34)),1.5,w,.13F);
            case 3->{for(int i=0;i<5;i++)c.later(i*12,()->c.column(c.target(),1.8,w,.07F));if(c.phase()>1)c.later(w+60,()->c.line(c.origin(),c.target(),1.4,32,.10F));}
            case 4->gapRing(c,p,d,w,true);
            case 5->{for(int side:new int[]{-1,1}){Vec3 from=p.add(rotate(d,90).scale(side*5));for(int i=0;i<7;i++){Vec3 at=from.add(rotate(d,side*(i*9-25)).scale(3+i*3));c.circle(at,1.8,w+i*9,.07F);}}}
            case 6->{Vec3 at=c.aim();c.column(at,3.5,w,.12F);for(int i=0;i<3;i++){Vec3 ray=rotate(d,i*120);c.line(at,at.add(ray.scale(18)),1.5,w+22,.08F);}c.later(w+35,()->c.expose(50));}
            case 7->{for(int i=0;i<5;i++){Vec3 from=c.point(Math.sin(i*Math.PI*2/5)*21,Math.cos(i*Math.PI*2/5)*21);c.line(from,c.middle().add(horizontal(c.middle().subtract(from)).scale(12)),2.8,w+i*24,.085F);}}
            case 8->{var left=c.echo(c.point(-10,6),240,true);var right=c.echo(c.point(10,6),240,true);for(int i=0;i<4;i++){final int step=i;c.later(w+i*35,()->{var attacker=step%2==0?left:right;var other=step%2==0?right:left;if(attacker!=null&&attacker.isAlive()&&!attacker.isRemoved())c.projectile(c.ground(attacker.position()),c.target(),24,.32,true,.07F);if(other!=null&&other.isAlive()&&!other.isRemoved())c.line(c.ground(other.position()),c.target(),1.8,32,.08F);});}}
            case 9->{List<Vec3> points=new ArrayList<>();for(int i=0;i<5;i++)points.add(c.point(Math.sin(i*Math.PI*2/5)*16,Math.cos(i*Math.PI*2/5)*16));for(int i=0;i<5;i++){Vec3 a=points.get(i),b=points.get((i+2)%5);c.line(a,b,2,w+i*10,.085F);}}
            case 10->{for(int i=0;i<5;i++){Vec3 from=c.point(Math.sin(i*Math.PI*2/5)*20,Math.cos(i*Math.PI*2/5)*20);c.cone(from,c.middle(),25,45,w+i*22,.08F);}c.circle(c.middle(),8,w+140,.16F);c.later(w+150,()->{c.teleport(c.middle());c.expose(150);});}
            default->bad(c,n);
        }
    }
    private static void wave(BossCombatController c,Vec3 p,int delay,float damage,boolean reverse) {
        for(int i=0;i<6;i++){double radius=reverse?24-i*4:4+i*4;c.ring(p,radius,3.5,delay+i*8,damage,true);}
    }
    private static void sweep(BossCombatController c,Vec3 p,Vec3 direction,double length,int count,double first,double last,int delay,float damage) {
        for(int i=0;i<count;i++){Vec3 ray=rotate(direction,first+(last-first)*i/(count-1));c.cone(p,p.add(ray),length,24,delay+i*10,damage);}
    }
    private static void radial(BossCombatController c,Vec3 p,int delay,int count,double offset,float damage) {
        for(int i=0;i<count;i++){Vec3 d=rotate(new Vec3(0,0,1),offset+i*360.0/count);c.projectile(p,p.add(d.scale(25)),delay,.48,false,damage);}
    }
    private static void sectors(BossCombatController c,Vec3 p,int delay,int count,float damage) {
        for(int i=0;i<count;i++){Vec3 d=rotate(c.direction(),i*360.0/count);c.cone(p,p.add(d),26,360.0/count-12,delay+i*24,damage);}
    }
    private static void walls(BossCombatController c,int delay,double preferredGap,int gapWidth) {
        double gap=Math.max(-14,Math.min(14,preferredGap));
        for(int step=0;step<5;step++)for(int side:new int[]{-1,1}){double x=side*(22-step*4);int when=step*15;c.later(when,()->{c.line(c.point(x,-25),c.point(x,gap-gapWidth*.5),1.8,delay,.085F);c.line(c.point(x,gap+gapWidth*.5),c.point(x,25),1.8,delay,.085F);});}
    }
    private static void mirrors(BossCombatController c,int count,int delay,int lifetime) {
        Vec3 truth=c.clamp(c.start().add(rotate(c.direction(),90).scale(6)));c.teleport(truth);
        c.fx(4,truth.add(0,1,0),truth,1,1,SAFE,0,lifetime,"真身｜符纹稳定");
        for(int i=0;i<count;i++){Vec3 at=c.clamp(c.start().add(rotate(c.direction(),i==0?-90:180).scale(8)));c.echo(at,lifetime,false);c.line(at,c.aim(),1.1,delay+20,.06F);}
        c.line(truth,c.aim(),1.5,delay+20,.09F);
    }
    private static void feast(BossCombatController c,int delay) {
        List<BossCombatController.Node> sweets=new ArrayList<>();
        for(int i=0;i<3;i++){Vec3 at=c.clamp(c.start().add(rotate(c.direction(),i*90-90).scale(7)));sweets.add(c.node("food",at,delay+160,null));}
        Vec3 bitterAt=c.clamp(c.start().add(c.direction().scale(9)));var bitter=c.node("bitter",bitterAt,delay+180,null);
        c.later(delay+50,()->{Vec3 p=c.origin(),aim=c.target();c.line(p,aim,2.6,32,.08F);c.later(33,()->{
            if(bitter.active()&&BossCombatGeometry.segmentDistance(bitter.pos.x,bitter.pos.z,p.x,p.z,aim.x,aim.z)<2){bitter.discard();c.recoil(.03F);c.interrupt(100);return;}
            for(var food:sweets)if(food.active()){food.discard();c.heal(.015F);}
        });});
    }
    private static void tether(BossCombatController c,Vec3 root,int delay,int duration) {
        var knot=c.node("knot",root,delay+duration+20,()->c.interrupt(80));
        c.fx(1,root,c.aim(),0,.4F,c.profile.color(),delay,delay+duration,"");
        for(int i=delay;i<delay+duration;i+=12)c.later(i,()->{
            if(!knot.active())return;
            if(!c.clearSight(root,c.target())){knot.discard();c.interrupt(70);}
        });
        c.later(delay+duration,()->{if(knot.active()){c.line(root,c.target(),1.5,24,.15F);knot.discard();}});
    }
    private static void gapRing(BossCombatController c,Vec3 center,Vec3 gapDirection,int delay,boolean inward) {
        // A genuine, continuous 60-degree safe wedge; no invisible hit through the gap.
        for(int step=0;step<5;step++){double radius=inward?22-step*4:6+step*4;for(int sector=1;sector<6;sector++){Vec3 d=rotate(gapDirection,sector*60);Vec3 a=center.add(rotate(d,-25).scale(radius)),b=center.add(rotate(d,25).scale(radius));c.line(a,b,1.6,delay+step*16,.075F);}}
    }
    private static void bad(BossCombatController c,int n){throw new IllegalArgumentException("Missing timeline "+c.profile.id()+":"+n);}
    private BossSkillPrograms(){}
}
