package cn.blockforge.generated.generatedmod;
import cn.blockforge.generated.generatedmod.tribulation.TribulationPlan;
public final class TribulationPlanRegression {
 public static void run(){double previousTime=0;for(int r=0;r<8;r++){
  if(TribulationPlan.impactScale(r,1)!=.25||TribulationPlan.impactScale(r,TribulationPlan.total(r))!=1)throw new AssertionError("Impact endpoints");
  if(TribulationPlan.impactHardness(r,1)!=1.5||TribulationPlan.impactHardness(r,TribulationPlan.total(r))!=TribulationPlan.HARDNESS[r])throw new AssertionError("Impact hardness endpoints");
  if(r>0&&(TribulationPlan.RADIUS[r]<=TribulationPlan.RADIUS[r-1]||TribulationPlan.DEPTH[r]<=TribulationPlan.DEPTH[r-1]||TribulationPlan.BLOCKS[r]<=TribulationPlan.BLOCKS[r-1]))throw new AssertionError("Impact realm growth");
  int done=0,last=0;double damage=0,time=30;java.util.Set<Integer> seeds=new java.util.HashSet<>();
  while(done<TribulationPlan.total(r)){int remaining=TribulationPlan.total(r)-done,n=remaining==1?1:TribulationPlan.count(r,done);if(n>=remaining&&remaining>1)n--;
   if(remaining>1&&n<remaining-1&&n<last)throw new AssertionError("Decreasing ordinary wave");if(remaining>1)last=n;
   for(int i=0;i<n;i++){done++;damage+=TribulationPlan.damage(r,done);if(!seeds.add(TribulationPlan.seed(712309,done)))throw new AssertionError("Repeated seed");}
   if(done<TribulationPlan.total(r))time+=TribulationPlan.total(r)-done==1?TribulationPlan.DURATION[r]+25:TribulationPlan.interval(r,done);
  }
  double expected=cn.blockforge.generated.generatedmod.balance.RealmBalance.health(r)*1.2*TribulationPlan.BUDGET[r];
  if(Math.abs(damage-expected)>.01||done!=18+r*9)throw new AssertionError("Damage/count mismatch");
  if(time<=previousTime)throw new AssertionError("Realm duration not increasing: "+r);previousTime=time;
  System.out.println("Tribulation realm="+r+" count="+done+" damage="+damage+" strikeSeconds="+time/20);
 }}
}
