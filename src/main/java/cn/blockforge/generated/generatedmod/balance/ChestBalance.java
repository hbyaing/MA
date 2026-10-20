package cn.blockforge.generated.generatedmod.balance;

public final class ChestBalance {
    public record Chances(float insight,float manual,float divine){}
    public static final Chances LOW=new Chances(.04F,.005F,.005F);
    public static final Chances MEDIUM=new Chances(.07F,.01F,.015F);
    public static final Chances HIGH=new Chances(.12F,.02F,.042F);
    public static Chances forPath(String path){
        if(path.startsWith("chests/bastion_")||path.startsWith("chests/stronghold_")
                ||path.equals("chests/end_city_treasure")||path.startsWith("chests/ancient_city")
                ||path.equals("chests/woodland_mansion")||path.equals("chests/nether_bridge"))return HIGH;
        if(path.equals("chests/abandoned_mineshaft")||path.equals("chests/simple_dungeon")
                ||path.equals("chests/desert_pyramid")||path.startsWith("chests/jungle_temple")
                ||path.equals("chests/pillager_outpost")||path.equals("chests/buried_treasure"))return MEDIUM;
        return LOW;
    }
    private ChestBalance(){}
}
