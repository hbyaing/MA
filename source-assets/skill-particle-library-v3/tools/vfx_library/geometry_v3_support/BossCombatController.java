package cn.blockforge.generated.generatedmod.boss.combat;

import java.util.*;
import net.minecraft.world.phys.Vec3;

/** Isolated VFX recorder. Executes real timeline sources; never loads Minecraft. */
public final class BossCombatController {
    public static final int SAFE=0x48DBC4,DANGER=0xF46A48;
    public final BossCombatCatalog.Profile profile;
    public final BossCombatCatalog.Skill skill;
    public final FakeBoss boss=new FakeBoss();
    public final List<Map<String,Object>> events=new ArrayList<>();
    private final List<Scheduled> actions=new ArrayList<>();
    private final List<Shot> shots=new ArrayList<>();
    private final List<Node> nodes=new ArrayList<>();
    private final int stage;
    private final Vec3 castTarget;
    private final boolean moving,withHistory;
    private int clock=0,pressure=0,finishAt=0,castId=1;
    private boolean interrupted;
    private List<Map<String,Object>> activeSource=List.of();
    private record Scheduled(int at,Runnable action,List<Map<String,Object>> source){}
    public BossCombatController(BossCombatCatalog.Profile p,BossCombatCatalog.Skill s,int phase,double distance,boolean moving,boolean history){
        profile=p;skill=s;stage=phase;castTarget=new Vec3(0,0,distance);this.moving=moving;withHistory=history;finishAt=s.windup()+s.recovery();
    }
    public static final class FakeBoss { private Vec3 pos=Vec3.ZERO;public Vec3 position(){return pos;} }
    public int phase(){return stage;}
    public int windup(){return skill.windup();}
    public int time(){return clock;}
    public Vec3 origin(){return boss.pos;}
    public Vec3 start(){return Vec3.ZERO;}
    public Vec3 aim(){return castTarget;}
    public Vec3 target(){return targetAt(clock);}
    public Vec3 targetAt(int t){return moving?new Vec3(6*Math.sin(t*.025),0,castTarget.z+3*Math.sin(t*.013)):castTarget;}
    public Vec3 middle(){return Vec3.ZERO;}
    public Vec3 point(double x,double z){return new Vec3(x,0,z);}
    public Vec3 ground(Vec3 p){return new Vec3(p.x,0,p.z);}
    public Vec3 direction(){return horizontal(aim().subtract(start()));}
    public static Vec3 horizontal(Vec3 v){double n=v.horizontalDistance();return n<.001?new Vec3(0,0,1):new Vec3(v.x/n,0,v.z/n);}
    public static Vec3 rotate(Vec3 v,double angle){double a=Math.toRadians(angle);return new Vec3(v.x*Math.cos(a)-v.z*Math.sin(a),v.y,v.x*Math.sin(a)+v.z*Math.cos(a));}
    public Vec3 clamp(Vec3 p){return new Vec3(Math.max(-23,Math.min(23,p.x)),0,Math.max(-23,Math.min(23,p.z)));}
    public List<Vec3> history(){if(!withHistory)return List.of();var out=new ArrayList<Vec3>();for(int i=0;i<12;i++)out.add(new Vec3(5*Math.sin(i*.5),0,4+i*.65));return out;}
    public int pressure(){return pressure;}
    public void pressure(int value){pressure=Math.max(0,Math.min(5,value));}
    public void later(int delay,Runnable action){if(actions.size()>=160)return;var trace=sourceTrace();actions.add(new Scheduled(clock+Math.max(0,delay),action,trace.isEmpty()?activeSource:trace));finishAt=Math.max(finishAt,clock+delay+skill.recovery()+20);}
    public void hold(int ticks){finishAt=Math.max(finishAt,clock+ticks);}
    public void move(Vec3 delta){Vec3 from=origin();boss.pos=clamp(boss.pos.add(delta));var e=event("move","dash_step",clock,1,from,origin());e.put("collisionConditional",true);}
    public void teleport(Vec3 p){Vec3 from=origin();boss.pos=clamp(p);var e=event("move","teleport",clock,0,from,origin());e.put("collisionConditional",true);}
    public boolean clearSight(Vec3 a,Vec3 b){return true;}
    public void guard(int ticks){event("control","guard",clock,ticks,origin(),origin());}
    public void expose(int ticks){event("control","expose",clock,ticks,origin(),origin());}
    public void interrupt(int ticks){interrupted=true;actions.clear();shots.clear();for(var e:events)if(!"control".equals(e.get("kind"))&&(int)e.get("startTick")+(int)e.get("durationTicks")>=clock)e.put("cancelAtTick",clock);nodes.forEach(Node::discard);event("control","interrupt",clock,ticks,origin(),origin());expose(ticks);}
    public void recoil(float f){var e=event("control","recoil",clock,0,origin(),origin());e.put("fraction",f);}
    public void heal(float f){var e=event("control","heal",clock,0,origin(),origin());e.put("fraction",f);}
    public void drainZones(String tag){var e=event("control","drain_zones",clock,0,origin(),origin());e.put("tag",tag);for(var old:events)if(tag.equals(old.get("tag"))&&"hazard".equals(old.get("kind"))){int end=(int)old.get("startTick")+(int)old.get("durationTicks");if(end>=clock)old.put("cancelAtTick",clock);}}
    public int fx(int shape,Vec3 a,Vec3 b,float r,float w,int color,int delay,int life,String label){var e=event("cue",shapeName(shape),clock,life,a,b);e.put("radius",r);e.put("width",w);e.put("windupTicks",delay);e.put("color",String.format("#%06X",color));e.put("label",label);e.put("damaging",false);return events.size();}
    private static String shapeName(int shape){return switch(shape){case 0->"circle";case 1->"line";case 2->"ring";case 3->"cone";default->"marker";};}
    private Map<String,Object> event(String kind,String shape,int start,int duration,Vec3 a,Vec3 b){
        var e=new LinkedHashMap<String,Object>();e.put("kind",kind);e.put("shape",shape);e.put("startTick",start);e.put("durationTicks",duration);e.put("origin",a);e.put("target",b);
        var src=sourceTrace();
        if(src.isEmpty())src=activeSource;
        if(!src.isEmpty())e.put("source",src);events.add(e);return e;
    }
    private List<Map<String,Object>> sourceTrace(){
        var src=new ArrayList<Map<String,Object>>();
        for(var s:Thread.currentThread().getStackTrace())if(s.getClassName().endsWith("BossSkillPrograms")||s.getClassName().endsWith("HeartDemonSkillPrograms")){var f=new LinkedHashMap<String,Object>();f.put("path","src/main/java/cn/blockforge/generated/generatedmod/boss/combat/"+s.getFileName());f.put("line",s.getLineNumber());f.put("method",s.getMethodName());src.add(f);}
        return src;
    }
    private void hazard(int shape,Vec3 a,Vec3 b,double radius,double width,int delay,int duration,int period,float fraction,float height,boolean cover,String tag,int ignored){
        var e=event("hazard",shapeName(shape),clock+delay,duration,a,b);e.put("telegraphStartTick",clock);e.put("radius",radius);e.put("width",width);e.put("height",height);e.put("cover",cover);e.put("damageFraction",fraction);e.put("periodTicks",period);e.put("tag",tag);e.put("endInclusive",true);e.put("collisionPadding",.25);e.put("damaging",true);
        if(shape==2){e.put("innerRadius",Math.max(0,radius-width));e.put("jumpable",height<1);}
        if(shape==3)e.put("angleDegrees",width);
        if(duration<=40)hold(delay+duration+skill.recovery());
    }
    // @SOURCE_PRIMITIVES@ (injected unchanged from the production controller).
    public void projectile(Vec3 start,Vec3 aim,int delay,double speed,boolean homing,float damage){
        Vec3 end=start.add(horizontal(aim.subtract(start)).scale(Math.min(35,start.distanceTo(aim)+5)));
        fx(1,start,end,0,.8F,DANGER,delay,delay,"");
        var e=event("projectile","projectile",clock+Math.max(delay,1),101,start,aim);e.put("telegraphStartTick",clock);e.put("speedBlocksPerTick",speed);e.put("homing",homing);e.put("turnBlend",homing?.1:0);e.put("collisionRadius",.75);e.put("heightUpper",3);e.put("lowerHeightBound",false);e.put("endInclusive",true);e.put("expiresAfterTicks",100);e.put("arenaDistanceSquaredLimit",1800);e.put("cover",true);e.put("damageFraction",damage);e.put("damaging",true);
        later(delay,()->{if(shots.size()<24)shots.add(new Shot(start,aim,speed,homing,e));});
    }
    private final class Shot {Vec3 pos,velocity;final boolean homing;final int expires;final Map<String,Object> event;final List<Object> path=new ArrayList<>();Shot(Vec3 p,Vec3 aim,double speed,boolean h,Map<String,Object> e){pos=p;velocity=horizontal(aim.subtract(p)).scale(speed);homing=h;expires=clock+100;event=e;e.put("path",path);path.add(List.of(clock,p));}}
    private void tickShots(){
        for(var shot:List.copyOf(shots)){
            if(clock>shot.expires){shot.event.put("termination","lifetime");shot.event.put("endTick",shot.expires);shots.remove(shot);continue;}
            if(shot.homing){Vec3 desired=horizontal(target().subtract(shot.pos)).scale(shot.velocity.length());shot.velocity=horizontal(shot.velocity.scale(.9).add(desired.scale(.1))).scale(shot.velocity.length());}
            Vec3 next=shot.pos.add(shot.velocity);
            if(next.distanceToSqr(middle())>1800){shot.event.put("termination","arena_limit");shot.event.put("endTick",clock-1);shots.remove(shot);continue;}
            shot.path.add(List.of(clock,next));
            if(BossCombatGeometry.segmentDistance(target().x,target().z,shot.pos.x,shot.pos.z,next.x,next.z)<.75){shot.event.put("termination","hit_synthetic_target");shot.event.put("endTick",clock);shots.remove(shot);continue;}
            shot.pos=next;
        }
    }
    public void pull(Vec3 point,int delay,int duration,double strength,double radius){
        var e=event("control","pull",clock+delay,duration,point,point);e.put("telegraphStartTick",clock);e.put("radius",radius);e.put("strengthPerApplication",strength);e.put("periodTicks",2);e.put("cover",true);e.put("distanceMode","3d sphere");e.put("damaging",false);
        for(int t=delay;t<delay+duration;t+=2)later(t,()->{});
    }
    public void gaze(int delay,float damage){gaze(delay,damage,true);}
    public void gaze(int delay,float damage,boolean exposeAfter){
        fx(0,origin(),origin(),30,0,profile.color(),delay,delay+8,"");
        var e=event("hazard","gaze",clock+delay,1,origin(),target());e.put("telegraphStartTick",clock);e.put("radius",null);e.put("lookDotThreshold",.45);e.put("viewHalfAngleDegrees",Math.toDegrees(Math.acos(.45)));e.put("cover",true);e.put("damageFraction",damage);e.put("damaging",true);e.put("rangeType","no radius test in gaze; encounter owner validity still applies");
        later(delay,()->{if(exposeAfter)expose(60);});
    }
    public final class Node {
        public final String kind;public final Vec3 pos;private final int expires;private boolean used;private final Map<String,Object> event;
        Node(String k,Vec3 p,int lifetime,Runnable callback){kind=k;pos=p;expires=clock+lifetime;event=event("node","node",clock,lifetime,p,p);event.put("nodeType",k);event.put("breakable",true);event.put("interruptsOnBreak",callback!=null);event.put("blocksSight",k.equals("crystal")||k.equals("cover"));}
        public boolean active(){return !used&&clock<expires;}
        public void discard(){used=true;event.put("cancelAtTick",clock);}
    }
    public Node node(String kind,Vec3 position,int lifetime,Runnable callback){var n=new Node(kind,clamp(position),lifetime,callback);nodes.add(n);return n;}
    public final class Echo {private final Vec3 pos;private final int expires;Echo(Vec3 p,int lifetime,boolean shared){pos=p;expires=clock+lifetime;var e=event("node","echo",clock,lifetime,p,p);e.put("sharesHealth",shared);}public boolean isAlive(){return clock<expires;}public boolean isRemoved(){return clock>=expires;}public Vec3 position(){return pos;}}
    public Echo echo(Vec3 p,int lifetime,boolean shared){return new Echo(clamp(p),lifetime,shared);}
    public Map<String,Object> run(){
        for(clock=1;clock<=1200;clock++){
            var due=actions.stream().filter(a->a.at<=clock).toList();actions.removeAll(due);for(var action:due){if(interrupted)break;activeSource=action.source;action.action.run();}activeSource=List.of();tickShots();
            if(actions.isEmpty()&&shots.isEmpty()&&clock>finishAt)break;
        }
        int end=0;for(var e:events)end=Math.max(end,(int)e.get("startTick")+(int)e.get("durationTicks"));
        var targetPath=new ArrayList<Object>();for(int t=0;t<=Math.max(end,finishAt);t++)targetPath.add(List.of(t,targetAt(t)));
        var r=new LinkedHashMap<String,Object>();r.put("events",events);r.put("durationTicks",Math.max(end,finishAt));r.put("schedulerFinishTick",finishAt);r.put("traceComplete",actions.isEmpty()&&shots.isEmpty());r.put("history",history());r.put("targetPath",targetPath);return r;
    }
}
