import cn.blockforge.generated.generatedmod.yujian.JinghongBurst;

public final class JinghongBurstTest {
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args){
        var burst=new JinghongBurst();int triggers=0;
        for(int t=0;t<100;t++)if(burst.tick(t,t+1,true,true))triggers++;
        check(triggers==1,"Holding roll must trigger exactly once, even after cooldown");
        check(burst.strength()==0,"Effect must end while roll remains held");
        burst.tick(100,0,true,true);
        check(burst.tick(105,5,true,true),"New roll after cooldown must trigger");
        check(burst.strength()==1,"Full strength at trigger");
        burst.tick(106,0,true,true);
        for(int t=107;t<111;t++)burst.tick(t,t-106,true,true);
        check(!burst.tick(111,5,true,true),"A new roll during cooldown must not stack");
        for(int t=112;t<=121;t++)burst.tick(t,6+t-112,true,true);
        check(burst.strength()==0,"Effect ends in 16 ticks");
        var poor=new JinghongBurst();
        check(!poor.tick(0,5,true,false),"Insufficient mana must not trigger");
        check(!poor.tick(1,6,true,true),"Mana recovery must not retroactively trigger the same roll");
        poor.tick(2,0,true,true);
        check(poor.tick(7,5,true,true),"Failed attempts must not charge cooldown");
        poor.cancelEffect();check(poor.strength()==0,"Collision/phase exit cancels enhancement");
        var locked=new JinghongBurst();
        check(!locked.tick(0,5,false,true),"Locked sword, low mastery or non-sprint state cannot trigger");
        check(!new JinghongBurst().tick(0,4,true,true),"Short taps do not qualify as effective rolls");
        System.out.println("JinghongBurstTest PASS: hold, retrigger, cooldown, mana, gate, cancellation, duration");
    }
}
