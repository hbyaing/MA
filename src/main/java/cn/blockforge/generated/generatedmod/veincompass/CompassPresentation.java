package cn.blockforge.generated.generatedmod.veincompass;

/** Display calculations only: no cultivation multipliers or world data. */
public final class CompassPresentation {
    public static int direction(double x,double z){return Math.floorMod((int)Math.round(Math.toDegrees(Math.atan2(x,-z))/45),8);}
    public static int quality(int aura){return aura<=8?0:aura<=25?1:aura<=45?2:aura<=65?3:aura<=85?4:5;}
    public static int color(int aura){return new int[]{0xFF8A8A92,0xFF9FD8C0,0xFF7FE8D0,0xFF6FE3FF,0xFFB388FF,0xFFFFD54F}[quality(aura)];}
    private CompassPresentation(){}
}
