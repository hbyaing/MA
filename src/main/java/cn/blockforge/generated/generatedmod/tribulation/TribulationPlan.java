package cn.blockforge.generated.generatedmod.tribulation;
/** Pure authoritative wave schedule, including one sustained final lightning field. */
public final class TribulationPlan {
 private static final int[][] WAVES={
 {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
 {1,1,1,1,1,2,2,2,2,2,3,3,3,3},
 {1,1,1,1,1,1,2,2,2,2,2,2,3,3,3,3,3,3},
 {1,1,1,2,2,2,3,3,3,4,4,4,5,5,5},
 {1,1,1,1,2,2,2,2,3,3,3,3,4,4,4,4,7,7},
 {1,1,1,1,2,2,2,2,2,3,3,3,3,3,4,4,4,4,6,6,6},
 {1,1,1,2,2,2,3,3,3,4,4,4,6,6,6,8,8,8},
 {1,1,1,2,2,2,3,3,3,5,5,5,8,8,10,11,11}};
 public static final double[] BUDGET={1.25,1.35,1.45,1.55,1.65,1.75,1.85,2};
 public static final double[] RADIUS={2,2.75,3.5,4.25,5,6,7,8},HARDNESS={2,2.5,3,3.5,4,4.5,5,6};
 public static final int[] DEPTH={2,3,4,5,6,7,8,9},FIRE={2,3,4,6,8,10,12,16};
 public static final int[] BLOCKS={24,64,128,224,360,540,768,1024},DURATION={22,22,16,30,18,32,28,36};
 public static double impactScale(int r,int index){return .25+.75*Math.pow(Math.max(0,Math.min(1,(index-1.0)/(total(r)-1))),1.35);}
 public static double impactHardness(int r,int index){return 1.5+(HARDNESS[realm(r)]-1.5)*(impactScale(r,index)-.25)/.75;}
 public static int realm(int r){return Math.max(0,Math.min(7,r));}
 public static int total(int r){return 18+realm(r)*9;}
 public static int count(int r,int completed){int sum=0;for(int n:WAVES[realm(r)]){if(completed<sum+n)return sum+n-completed;sum+=n;}return 0;}
 public static int interval(int r,int completed){r=realm(r);int i=0,sum=0;for(int n:WAVES[r]){sum+=n;if(sum>=completed)break;i++;}int gaps=WAVES[r].length-1;double weight=1.08-.16*Math.min(i,gaps-1)/Math.max(1.0,gaps-1);return Math.max(DURATION[r]+4,(int)Math.round(((25+r*5)*20.0-60)/gaps*weight));}
 public static int seed(long s,int index){long x=s+index*0x9E3779B97F4A7C15L;x=(x^(x>>>30))*0xBF58476D1CE4E5B9L;x=(x^(x>>>27))*0x94D049BB133111EBL;return (int)(x^(x>>>31));}
 public static float damage(int realm,int index){int r=realm(realm);double h=cn.blockforge.generated.generatedmod.balance.RealmBalance.health(r)*1.2;return (float)(h*BUDGET[r]/total(r)*(.75+.5*(index-1.0)/(total(r)-1)));}
 private TribulationPlan(){}
}
