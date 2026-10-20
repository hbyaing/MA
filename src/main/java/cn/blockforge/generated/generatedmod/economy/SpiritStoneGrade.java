package cn.blockforge.generated.generatedmod.economy;

import cn.blockforge.generated.generatedmod.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Low-grade retains the old registry ID. Values are in legacy low-stone units. */
public enum SpiritStoneGrade {
    LOW(1),MIDDLE(9),SUPERIOR(81),EXQUISITE(729);
    public final int value;
    SpiritStoneGrade(int value){this.value=value;}
    public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
    public Item item(){return switch(this){
        case LOW->WuXingItems.SPIRIT_STONE.get();case MIDDLE->WuXingItems.SPIRIT_STONE_MIDDLE.get();
        case SUPERIOR->WuXingItems.SPIRIT_STONE_SUPERIOR.get();case EXQUISITE->WuXingItems.SPIRIT_STONE_EXQUISITE.get();};}
    public Block ore(){return switch(this){
        case LOW->WuXingBlocks.SPIRIT_VEIN_ORE.get();case MIDDLE->WuXingBlocks.SPIRIT_VEIN_ORE_MIDDLE.get();
        case SUPERIOR->WuXingBlocks.SPIRIT_VEIN_ORE_SUPERIOR.get();case EXQUISITE->WuXingBlocks.SPIRIT_VEIN_ORE_EXQUISITE.get();};}
    public static SpiritStoneGrade of(Item item){for(var grade:values())if(grade.item()==item)return grade;return null;}
}
