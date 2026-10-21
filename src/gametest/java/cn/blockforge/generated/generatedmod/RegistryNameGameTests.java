package cn.blockforge.generated.generatedmod;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.item.Items;
@GameTestHolder("wuxingxiantu_talismans") @PrefixGameTestTemplate(false)
public final class RegistryNameGameTests {
    @GameTest(template="empty") public static void renamedIdsResolveToCurrentRegistrations(GameTestHelper h){
        for(var alias:RegistryNameMigration.ALIASES.entrySet()){
            var old=new ResourceLocation(GeneratedMod.MOD_ID,alias.getKey());var id=RegistryNameMigration.canonical(old);
            h.assertTrue(id.getPath().equals(alias.getValue()),"Wrong canonical alias "+old);
            if(alias.getValue().equals("dunguang"))h.assertTrue(ForgeRegistries.MOB_EFFECTS.getValue(id)==ModEffects.REALM_FLIGHT.get(),"Dunguang registration missing");
            else h.assertTrue(ForgeRegistries.ITEMS.getValue(id)!=null&&ForgeRegistries.ITEMS.getValue(id)!=Items.AIR,"Renamed item missing "+id);
        }
        h.assertTrue(ForgeRegistries.BLOCKS.getKey(WuXingBlocks.INSCRIPTION_TABLE.get()).getPath().equals("ming_ke_tai"),"Engraving block ID wrong");h.assertTrue(ForgeRegistries.BLOCKS.getKey(WuXingBlocks.REFORGE_TABLE.get()).getPath().equals("jing_zhu_tai"),"Casting block ID wrong");h.succeed();
    }
}
