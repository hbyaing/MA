package cn.blockforge.generated.generatedmod.boss.combat;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Authored movement beats use server game ticks, including late hits and phase-specific follow-ups. */
public final class BossMotionCatalog {
    public record Beat(int tick,String pose){}
    public record Motion(String skill,int phase,String animation,int windup,int duration,List<Beat> beats){}
    private static final Map<String,Motion> MOTIONS=load();
    private static final Map<String,Motion> BY_ANIMATION=indexAnimations();
    private static Map<String,Motion> indexAnimations() {
        var result=new HashMap<String,Motion>();
        MOTIONS.values().forEach(motion->result.put(motion.animation(),motion));
        return Collections.unmodifiableMap(result);
    }
    private static Map<String,Motion> load() {
        try(var stream=Objects.requireNonNull(BossMotionCatalog.class.getResourceAsStream(
                "/data/wuxingxiantu/boss_motions.json"));
            var reader=new InputStreamReader(stream,StandardCharsets.UTF_8)) {
            var result=new LinkedHashMap<String,Motion>();
            for(var motion:new Gson().fromJson(reader,Motion[].class)) {
                if(motion.windup()<1||motion.duration()<=motion.windup()||motion.beats().isEmpty()
                        ||result.put(motion.skill()+":"+motion.phase(),motion)!=null)
                    throw new IllegalStateException("Invalid boss motion: "+motion.skill());
            }
            return Collections.unmodifiableMap(result);
        }catch(Exception exception){throw new IllegalStateException("Cannot load boss motion timing",exception);}
    }
    public static Motion get(String skill,int phase) {
        return MOTIONS.getOrDefault(skill+":"+phase,MOTIONS.get(skill+":1"));
    }
    public static Motion animation(String clip) {
        return BY_ANIMATION.get(clip);
    }
    private BossMotionCatalog(){}
}
