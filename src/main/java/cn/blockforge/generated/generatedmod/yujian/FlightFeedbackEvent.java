package cn.blockforge.generated.generatedmod.yujian;

/** Server-side hooks for authored sounds or additional effects. No placeholder vanilla sounds. */
public final class FlightFeedbackEvent extends net.minecraftforge.eventbus.api.Event {
    public enum Kind { SUMMON, ORBIT, MOUNT, WIND, SPRINT, ROLL, ENTITY_IMPACT, BLOCK_IMPACT, DISMOUNT, RECALL }
    public final ControlledSwordEntity sword;
    public final Kind kind;
    public FlightFeedbackEvent(ControlledSwordEntity sword,Kind kind){this.sword=sword;this.kind=kind;}
    public static void emit(ControlledSwordEntity sword,Kind kind){
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new FlightFeedbackEvent(sword,kind));
    }
}
