package cn.blockforge.generated.generatedmod.yujian;

/** A one-shot roll passive, never a flight phase. Timers use server game ticks. */
public final class JinghongBurst {
    public static final int REALM=4, MASTERY=60, EFFECT_TICKS=16, COOLDOWN_TICKS=30;
    public static final double ENTRY_SPEED=0.70, IMPULSE=0.55;
    private long cooldownUntil;
    private int remaining;
    private boolean rollConsumed;

    /** Consume each effective roll once, including failed attempts; a held key cannot repeat. */
    public boolean tick(long now, int rollHoldTicks, boolean eligible, boolean affordable) {
        if(remaining>0)remaining--;
        if(rollHoldTicks==0)rollConsumed=false;
        if(rollHoldTicks<5||rollConsumed)return false;
        rollConsumed=true;
        if(!eligible||!affordable||now<cooldownUntil)return false;
        cooldownUntil=now+COOLDOWN_TICKS;
        remaining=EFFECT_TICKS;
        return true;
    }
    public void cancelEffect(){remaining=0;}
    public float strength(){return remaining/(float)EFFECT_TICKS;}
}
