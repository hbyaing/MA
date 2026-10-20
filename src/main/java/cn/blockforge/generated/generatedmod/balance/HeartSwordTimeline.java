package cn.blockforge.generated.generatedmod.balance;

/** Packet action ordinals are save/protocol-independent but must remain stable. */
public final class HeartSwordTimeline {
    private HeartSwordTimeline() {}
    public static int duration(int action) {
        return switch(action) { case 0 -> 10; case 1 -> 5; case 2 -> 21; case 3 -> 13; default -> 0; };
    }
    public static float clamp(float value) { return Math.max(0,Math.min(1,value)); }
    public static float smooth(float value) { float p=clamp(value);return p*p*(3-2*p); }
    public static float reveal(int action,float progress) {
        return action==0?smooth((progress-.12F)/.70F):action==1?1-smooth(progress/.82F):1;
    }
    /** Unforge samples the exact reverse of the forge path. */
    public static float sweep(int action,float progress) { return smooth(action==3?1-progress:progress); }
    public static float poseWeight(float progress) {
        return smooth(progress/.18F)*smooth((1-progress)/.18F);
    }
}
