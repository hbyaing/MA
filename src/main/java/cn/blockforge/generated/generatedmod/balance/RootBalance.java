package cn.blockforge.generated.generatedmod.balance;

import cn.blockforge.generated.generatedmod.CultivationData;
import net.minecraft.util.RandomSource;

/** Approved root-count tradeoff and independent per-root quality, shared by server and UI. */
public final class RootBalance {
    private static final double[] TRAINING={1,.85,.73,.64,.57};
    private static final float[] CAPACITY={1,1.18F,1.36F,1.54F,1.72F};
    private static final double[] QUALITY_TRAINING={1,1.15,1.35,1.60};
    private static final float[] QUALITY_REGEN={1,1.20F,1.45F,1.80F};
    private RootBalance() {}
    public static int count(CultivationData d){return Integer.bitCount(d.wuxingxiantu$getRootMask()&31);}
    public static int drawCount(RandomSource random){return countForRoll(random.nextInt(100));}
    public static int countForRoll(int roll){if(roll<0||roll>=100)throw new IllegalArgumentException();return roll<45?1:roll<75?2:roll<92?3:roll<99?4:5;}
    public static int drawQuality(RandomSource random){return qualityForRoll(random.nextInt(100));}
    public static int qualityForRoll(int roll){if(roll<0||roll>=100)throw new IllegalArgumentException();return roll<65?1:roll<90?2:roll<99?3:4;}
    public static float capacity(int count){return CAPACITY[Math.max(0,Math.min(4,count-1))];}
    public static float capacity(CultivationData d){return capacity(count(d));}
    public static double qualityTraining(CultivationData d){double total=0;int count=count(d);if(count==0)return 1;for(int i=0;i<5;i++)if((d.wuxingxiantu$getRootMask()&(1<<i))!=0)total+=QUALITY_TRAINING[Math.max(1,d.wuxingxiantu$getRootQuality(i))-1];return total/count;}
    public static double training(CultivationData d){return TRAINING[Math.max(0,count(d)-1)]*qualityTraining(d);}
    public static float recovery(CultivationData d){float total=0;int count=count(d);if(count==0)return 1;for(int i=0;i<5;i++)if((d.wuxingxiantu$getRootMask()&(1<<i))!=0)total+=QUALITY_REGEN[Math.max(1,d.wuxingxiantu$getRootQuality(i))-1];return total/count;}
    public record Gain(long amount,double remainder){}
    public static Gain split(double base,double multiplier,double remainder){
        double carry=Double.isFinite(remainder)&&remainder>=0&&remainder<1?remainder:0;
        if(!Double.isFinite(base)||base<=0||!Double.isFinite(multiplier)||multiplier<=0)return new Gain(0,carry);
        double scaled=base*multiplier+carry;if(scaled>=Long.MAX_VALUE)return new Gain(Long.MAX_VALUE,0);
        double nearest=Math.rint(scaled);if(Math.abs(scaled-nearest)<1E-8)scaled=nearest;
        long whole=(long)Math.floor(scaled);return new Gain(whole,scaled-whole);
    }
}
