package cn.blockforge.generated.generatedmod.boss.combat;
import java.util.*;
import net.minecraft.world.phys.Vec3;

public final class ReplayMain {
    private static void check(boolean condition,String label){if(!condition)throw new IllegalStateException("Production geometry regression: "+label);}
    private static void regression(){
        check(BossCombatGeometry.cone(0,17.25,0,0,0,1,17.25,95),"cone padded radius included");
        check(!BossCombatGeometry.cone(0,17.251,0,0,0,1,17.25,95),"cone beyond padded radius excluded");
        check(!BossCombatGeometry.cone(0,-1,0,0,0,1,17.25,95),"cone rear excluded");
        check(BossCombatGeometry.segmentDistance(0,-1.15,0,0,0,10)<=1.15,"line round endcap included");
        check(BossCombatGeometry.segmentDistance(0,-1.151,0,0,0,10)>1.15,"line beyond endcap excluded");
        boolean claims30Hit=false,axisHit=false;
        Vec3 point=BossCombatController.rotate(new Vec3(0,0,1),30).scale(6);
        for(int sector=1;sector<6;sector++){
            Vec3 d=BossCombatController.rotate(new Vec3(0,0,1),sector*60);
            Vec3 a=BossCombatController.rotate(d,-25).scale(6),b=BossCombatController.rotate(d,25).scale(6);
            if(BossCombatGeometry.segmentDistance(point.x,point.z,a.x,a.z,b.x,b.z)<=1.05)claims30Hit=true;
            if(BossCombatGeometry.segmentDistance(0,6,a.x,a.z,b.x,b.z)<=1.05)axisHit=true;
        }
        check(claims30Hit,"gap ring source 60-degree comment is not exact at inner round endcaps");
        check(!axisHit,"gap ring central axis remains safe");
    }
    private static String json(Object x){
        if(x==null)return "null";if(x instanceof Number||x instanceof Boolean)return x.toString();
        if(x instanceof Vec3 p)return "["+p.x+","+p.y+","+p.z+"]";
        if(x instanceof String s)return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t")+"\"";
        if(x instanceof Map<?,?> m){var b=new StringJoiner(",","{","}");for(var e:m.entrySet())b.add(json(e.getKey().toString())+":"+json(e.getValue()));return b.toString();}
        if(x instanceof Iterable<?> a){var b=new StringJoiner(",","[","]");for(Object v:a)b.add(json(v));return b.toString();}throw new IllegalArgumentException(x.toString());
    }
    public static void main(String[] args){
        regression();
        var skills=new LinkedHashMap<String,Object>();var profiles=new ArrayList<>(BossCombatCatalog.all());profiles.add(HeartDemonSkillPrograms.PROFILE);
        for(var profile:profiles)for(int index=0;index<profile.skills().size();index++){
            var skill=profile.skills().get(index);var s=new LinkedHashMap<String,Object>();s.put("name",skill.name());s.put("boss",profile.id());s.put("windupTicks",skill.windup());s.put("recoveryTicks",skill.recovery());s.put("minimumPhase",skill.phase());s.put("animation",skill.animation());s.put("hint",skill.hint());
            var scenarios=new ArrayList<Object>();int maxEnd=0;
            for(int phase=skill.phase();phase<=profile.phases();phase++){
                double[] distances=skill.id().equals("yihuo_06")?new double[]{5,10,21}:new double[]{10};
                for(double distance:distances){
                    boolean hasHistory=skill.id().equals("yihuo_03")||skill.id().equals("heart_demon_04");
                    int variants=hasHistory?2:1;
                    for(int h=0;h<variants;h++){
                        var c=new BossCombatController(profile,skill,phase,distance,false,h==1);BossSkillPrograms.cast(c,index+1);var result=c.run();maxEnd=Math.max(maxEnd,(int)result.get("durationTicks"));
                        var input=new LinkedHashMap<String,Object>();input.put("phase",phase);input.put("bossOrigin",Vec3.ZERO);input.put("arenaCenter",Vec3.ZERO);input.put("target",new Vec3(0,0,distance));input.put("targetMotion","stationary");input.put("history",result.remove("history"));input.put("clearSight",true);input.put("collisionFreeBossMotion",true);input.put("nodesDestroyedByPlayer",false);
                        result.put("id","phase_"+phase+"_distance_"+(int)distance+(hasHistory?(h==1?"_history12":"_no_history"):""));result.put("label","阶段 "+phase+" / 目标 "+(int)distance+" 格"+(hasHistory?(h==1?" / 12处旧足迹":" / 无旧足迹"):""));result.put("inputs",input);scenarios.add(result);
                    }
                }
            }
            if(Set.of("muyan_01","ermei_03","ermei_04","bichen_01","shetao_03","shenyin_01","pengju_05","pengjiao_03","pengjiao_08").contains(skill.id())){
                var c=new BossCombatController(profile,skill,profile.phases(),10,true,false);BossSkillPrograms.cast(c,index+1);var result=c.run();result.remove("history");maxEnd=Math.max(maxEnd,(int)result.get("durationTicks"));var input=new LinkedHashMap<String,Object>();input.put("phase",profile.phases());input.put("bossOrigin",Vec3.ZERO);input.put("arenaCenter",Vec3.ZERO);input.put("target",new Vec3(0,0,10));input.put("targetMotion","x=6 sin(t*0.025), z=10+3 sin(t*0.013); y=0");input.put("clearSight",true);input.put("collisionFreeBossMotion",true);input.put("nodesDestroyedByPlayer",false);result.put("id","moving_target");result.put("label","移动目标 / 第 "+profile.phases()+" 阶段");result.put("inputs",input);scenarios.add(result);
            }
            s.put("durationTicks",maxEnd);s.put("scenarios",scenarios);skills.put(skill.id(),s);
        }
        System.out.println(json(skills));
    }
}
