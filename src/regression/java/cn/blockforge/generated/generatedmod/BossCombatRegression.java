package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.boss.combat.BossCombatCatalog;
import cn.blockforge.generated.generatedmod.boss.combat.BossCombatGeometry;
import java.util.HashSet;

final class BossCombatRegression {
    private static int checks;
    private static void require(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    private static void near(double expected,double actual,String message){require(Math.abs(expected-actual)<.0001,message);}
    static void run() {
        int[] expected={5,6,6,7,7,8,9,9,10};int i=0,total=0;var ids=new HashSet<String>();
        for(var profile:BossCombatCatalog.all()) {
            require(profile.skills().size()==expected[i++],"Boss move count changed: "+profile.id());
            for(var skill:profile.skills()) {
                total++;require(ids.add(skill.id()),"Duplicate move: "+skill.id());
                require(skill.windup()>=16,"Attack has no usable reaction window: "+skill.id());
                require(skill.recovery()>=16,"Attack has no punish window: "+skill.id());
            }
        }
        require(total==67&&i==9,"All nine bosses and 67 moves must be represented");
        near(3,BossCombatGeometry.segmentDistance(5,3,0,0,10,0),"Perpendicular distance");
        near(5,BossCombatGeometry.segmentDistance(-3,4,0,0,10,0),"Segment must not extend behind caster");
        near(5,BossCombatGeometry.segmentDistance(13,4,0,0,10,0),"Segment must not extend past endpoint");
        near(5,BossCombatGeometry.segmentDistance(3,4,0,0,0,0),"Degenerate ray finite");
        require(BossCombatGeometry.cone(0,5,0,0,0,1,6,90),"Cone forward hit");
        require(!BossCombatGeometry.cone(0,-5,0,0,0,1,6,90),"Cone rear is safe");
        require(!BossCombatGeometry.cone(5,0,0,0,0,1,6,90),"Cone flank is safe");
        require(!BossCombatGeometry.cone(0,7,0,0,0,1,6,90),"Cone range boundary");
        near(10,BossCombatGeometry.budget(20,20,30),"Multihit damage is capped");
        near(0,BossCombatGeometry.budget(20,35,30),"Exhausted budget cannot heal or damage");
        near(0,BossCombatGeometry.budget(Float.NaN,0,30),"Reject invalid damage");
        require(BossCombatGeometry.phase(.61F,2)==1&&BossCombatGeometry.phase(.60F,2)==2,"Two-stage threshold");
        require(BossCombatGeometry.phase(.66F,3)==1&&BossCombatGeometry.phase(.65F,3)==2&&BossCombatGeometry.phase(.3F,3)==3,"Three-stage thresholds");
        System.out.println("PASS: "+checks+" boss catalog/geometry/budget checks");
    }
}
