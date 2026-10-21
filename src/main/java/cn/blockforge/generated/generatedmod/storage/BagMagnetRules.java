package cn.blockforge.generated.generatedmod.storage;
import java.util.UUID;
public final class BagMagnetRules {
 public static boolean within(double dx,double dy,double dz){return Math.abs(dx)<=4.5&&Math.abs(dy)<=4.5&&Math.abs(dz)<=4.5;}
 public static boolean permitted(boolean alive,boolean delay,boolean bag,UUID owner,UUID player){return alive&&!delay&&!bag&&(owner==null||owner.equals(player));}
}
