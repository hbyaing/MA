package cn.blockforge.generated.generatedmod.network;

/** Opt-in serialized payload measurements; excludes channel framing/compression. */
public final class SyncMetrics {
    public static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.profileSync");
    private static long started=System.nanoTime(),packets,bytes,largest;
    private static long snapshots,playerNanos;
    private static final java.util.Map<java.util.UUID,Long> observed=new java.util.HashMap<>();
    public static synchronized void observe(java.util.UUID player) {
        if(!ENABLED)return;
        long now=System.nanoTime();Long previous=observed.put(player,now);
        if(previous!=null)playerNanos+=Math.max(0,now-previous);
    }
    public static synchronized void forget(java.util.UUID player){observed.remove(player);}
    public static synchronized void snapshotAllocated(){if(ENABLED)snapshots++;}
    public static synchronized void reset(){
        started=System.nanoTime();packets=bytes=largest=snapshots=playerNanos=0;observed.clear();
    }
    private SyncMetrics() {}
    public static synchronized void record(int size) {
        if(!ENABLED)return;
        packets++;bytes+=size;largest=Math.max(largest,size);
        long now=System.nanoTime();
        if(now-started<60_000_000_000L)return;
        double seconds=(now-started)/1_000_000_000.0;
        com.mojang.logging.LogUtils.getLogger().info(
                "Cultivation payload (full/vitals only): packets={}, bytes={}, avgBytes={}, maxBytes={}, packetsPerSecond={}, snapshots={}, packetsPerPlayerMinute={}, bytesPerPlayerMinute={}",
                packets,bytes,bytes/packets,largest,packets/seconds,snapshots,
                packets*60_000_000_000.0/Math.max(1,playerNanos),bytes*60_000_000_000.0/Math.max(1,playerNanos));
        started=now;packets=bytes=largest=snapshots=playerNanos=0;
        observed.clear();
    }
}
