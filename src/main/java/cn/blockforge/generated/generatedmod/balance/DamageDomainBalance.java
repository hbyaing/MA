package cn.blockforge.generated.generatedmod.balance;

/** Shared pulse damage, target budgets and mana quotes for damaging player domains. */
public final class DamageDomainBalance {
    public enum Domain { FIRE, THUNDER, EXOTIC, TIDE, GRAVITY }
    public static final int AREA_TARGET_LIMIT=12;
    private DamageDomainBalance() {}
    private static int grade(int quality){return Math.max(2,Math.min(4,quality));}
    public static int radius(Domain domain,int quality){
        int q=grade(quality);
        return switch(domain){
            case FIRE,EXOTIC -> q==4?7:q==3?5:3;
            case THUNDER -> q==4?6:q==3?4:3;
            case TIDE,GRAVITY -> q==4?3:q==3?2:1;
        };
    }
    public static int targetLimit(Domain domain,int quality){return domain==Domain.THUNDER?grade(quality)-1:AREA_TARGET_LIMIT;}
    public static int damageIntervalTicks(Domain domain,int quality){return domain==Domain.TIDE&&grade(quality)==3?40:20;}
    public static double attackRatio(Domain domain,int quality){
        if(quality<2)return 0;
        int q=grade(quality);
        return switch(domain){
            case FIRE -> q==4?.30:q==3?.22:.16;
            case EXOTIC -> q==4?.34:q==3?.25:.18;
            case THUNDER -> q==4?.55:q==3?.45:.35;
            case TIDE -> q==4?.10:q==3?.07:0;
            case GRAVITY -> q==4?.14:q==3?.08:0;
        };
    }
    public static float damage(Domain domain,int quality,double currentAttack){
        if(quality<2||!Double.isFinite(currentAttack)||currentAttack<0)return 0;
        int q=grade(quality);
        double base=switch(domain){
            case FIRE,EXOTIC -> q-1;
            case THUNDER -> q+1;
            case TIDE -> q>=3?.5:0;
            case GRAVITY -> q==4?1:q==3?.5:0;
        };
        return (float)Math.min(Float.MAX_VALUE,base+attackRatio(domain,q)*currentAttack);
    }
    /** Ongoing fire ticks are reserved in the quote; existing burn mechanics stay independent. */
    public static double burnReserve(Domain domain){return domain==Domain.FIRE||domain==Domain.THUNDER?1:domain==Domain.EXOTIC?.5:0;}
    public static int paralysisTicks(int quality){return 2+2*grade(quality);}
    /**
     * One atomic per-second quote: base upkeep + output budget + control targets.
     * Live attack is normalized against its realm; fixed damage uses the endgame
     * reference so low realms do not pay disproportionate flat-damage overhead.
     * Empty areas are free; tide water-walking pays upkeep with no attack budget.
     */
    public static int manaCost(Domain domain,int quality,double currentAttack,int realm,
                               double maximumMana,int targetCount,boolean damagingPulse,boolean waterWalking){
        if(quality<2)return 0;
        if(!Double.isFinite(currentAttack)||currentAttack<0||!Double.isFinite(maximumMana)||maximumMana<0)return Integer.MAX_VALUE;
        int count=Math.max(0,Math.min(targetLimit(domain,quality),targetCount));
        if(count==0&&!waterWalking)return 0;
        int q=grade(quality);
        double scale=q==4?1.6:q==3?1.3:1;
        double base=maximumMana*.0015*scale;
        double perTarget=0;
        if(damagingPulse){
            double realmAttack=RealmBalance.attack(realm)+1;
            double referenceAttack=RealmBalance.attack(8)+1;
            double fixedOutput=damage(domain,q,0)+burnReserve(domain);
            double outputBudget=attackRatio(domain,q)*currentAttack/realmAttack
                    +fixedOutput/referenceAttack;
            perTarget=outputBudget*maximumMana*.015;
        }
        if(domain==Domain.TIDE||domain==Domain.GRAVITY)perTarget+=maximumMana*.0006*scale;
        double total=base+count*perTarget;
        return total>=Integer.MAX_VALUE?Integer.MAX_VALUE:(int)Math.ceil(Math.max(1,total));
    }
}
