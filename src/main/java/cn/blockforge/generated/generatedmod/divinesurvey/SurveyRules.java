package cn.blockforge.generated.generatedmod.divinesurvey;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/** The target account remains the sole authority for realm and spiritual sense. */
public final class SurveyRules {
    public static final int START_COST=8, UPKEEP_COST=1;
    public static boolean unlocked(int realm,boolean seaOpen){return realm>=4&&seaOpen;}
    public static int minimumChunk(int playerChunk){return playerChunk-4;}
    public static boolean containsChunk(int minX,int minZ,int x,int z){return x>=minX&&x<minX+8&&z>=minZ&&z<minZ+8;}
    public static boolean spiritOre(BlockState state){var block=state.getBlock();return block==cn.blockforge.generated.generatedmod.WuXingBlocks.SPIRIT_ORE.get()||block==cn.blockforge.generated.generatedmod.WuXingBlocks.SPIRIT_VEIN_ORE.get()||block==cn.blockforge.generated.generatedmod.WuXingBlocks.SPIRIT_VEIN_ORE_MIDDLE.get()||block==cn.blockforge.generated.generatedmod.WuXingBlocks.SPIRIT_VEIN_ORE_SUPERIOR.get()||block==cn.blockforge.generated.generatedmod.WuXingBlocks.SPIRIT_VEIN_ORE_EXQUISITE.get();}
    private SurveyRules(){}
}
