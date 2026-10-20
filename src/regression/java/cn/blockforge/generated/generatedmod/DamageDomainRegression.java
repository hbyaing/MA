package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.balance.DamageDomainBalance;
import cn.blockforge.generated.generatedmod.balance.DamageDomainBalance.Domain;
import cn.blockforge.generated.generatedmod.balance.RealmBalance;

/** Independent budget regressions across all realms, qualities, and live attacks. */
public final class DamageDomainRegression {
    private static int checks;
    private static void require(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
    public static void run(){
        for(var d:Domain.values())for(int q=2;q<=4;q++)for(int realm=0;realm<=8;realm++){
            double a=RealmBalance.attack(realm)+1,m=RealmBalance.mana(realm);
            float normal=DamageDomainBalance.damage(d,q,a),buff=DamageDomainBalance.damage(d,q,a*2);
            require(buff>=normal,"Live attack must increase output");
            require(DamageDomainBalance.manaCost(d,q,a,realm,m,0,true,false)==0,"Empty fields are free");
            int one=DamageDomainBalance.manaCost(d,q,a,realm,m,1,true,false);
            int many=DamageDomainBalance.manaCost(d,q,a,realm,m,999,true,false);
            require(one>0&&many>=one,"Real targets must pay a bounded increasing quote");
            require(many==DamageDomainBalance.manaCost(d,q,a,realm,m,DamageDomainBalance.targetLimit(d,q),true,false),"Target caps bound mana and output together");
            require(DamageDomainBalance.manaCost(d,q,a*2,realm,m,1,true,false)>=one,"Stronger damage cannot become cheaper");
            require(DamageDomainBalance.manaCost(d,q,a,realm,m*2,1,true,false)>=one,"Mana capacity scales upkeep");
            require(DamageDomainBalance.manaCost(d,q,a,realm,m,1,false,false)<=one,"Off-pulse control must not pay damage twice");
        }
        require(DamageDomainBalance.damage(Domain.FIRE,4,100)==33,"Fire formula: base 3 plus 30 percent of attack");
        require(DamageDomainBalance.damage(Domain.THUNDER,4,100)==60,"Thunder formula: base 5 plus 55 percent of attack");
        require(DamageDomainBalance.damage(Domain.TIDE,2,100)==0,"Mid-grade tide remains utility only");
        require(DamageDomainBalance.damage(Domain.GRAVITY,2,100)==0,"Mid-grade gravity remains utility only");
        require(DamageDomainBalance.damageIntervalTicks(Domain.TIDE,3)==40,"High-grade tide retains two-second pulse");
        require(DamageDomainBalance.paralysisTicks(4)<20,"Thunder cannot permanently paralyze on one-second pulses");
        require(DamageDomainBalance.manaCost(Domain.FIRE,4,Double.NaN,8,1e6,1,true,false)==Integer.MAX_VALUE,"Invalid attack refuses release");
        require(DamageDomainBalance.manaCost(Domain.FIRE,4,100,8,Double.POSITIVE_INFINITY,1,true,false)==Integer.MAX_VALUE,"Invalid mana refuses release");
        require(DamageDomainBalance.manaCost(Domain.FIRE,4,Double.MAX_VALUE,8,1e6,12,true,false)==Integer.MAX_VALUE,"Overflow saturates, never becomes free");
        require(DamageDomainBalance.manaCost(Domain.TIDE,4,100,8,1e6,0,false,true)>0,"Water walking pays upkeep without attack targets");
        // Cross-realm burden must stay within one integer-mana rounding step.
        for(var d:Domain.values())for(int q=2;q<=4;q++)for(int count:new int[]{1,12}){
            double reference=DamageDomainBalance.manaCost(d,q,261,8,1_000_000,count,true,false)/1_000_000.0;
            for(int realm=0;realm<=8;realm++){
                double mana=RealmBalance.mana(realm);
                double fraction=DamageDomainBalance.manaCost(d,q,RealmBalance.attack(realm)+1,realm,mana,count,true,false)/mana;
                require(Math.abs(fraction-reference)<=1/mana+1e-6,"Low realms pay disproportionate domain burden: "+d+"/"+q+"/"+realm);
            }
        }
        require(DamageDomainBalance.manaCost(Domain.FIRE,4,3,0,100,1,true,false)==1,"Qi single target should cost one mana instead of three");
        require(DamageDomainBalance.manaCost(Domain.FIRE,4,6,1,300,1,true,false)==3,"Foundation single target should cost three mana instead of six");
        require(DamageDomainBalance.manaCost(Domain.FIRE,4,261,8,1e6,1,true,false)==7130,"Preserve accepted earth-immortal fee");
        System.out.println("PASS: "+checks+" damaging-domain budget checks");
    }
}
