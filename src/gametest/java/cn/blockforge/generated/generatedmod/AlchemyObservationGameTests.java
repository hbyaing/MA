package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.observation.*;
import cn.blockforge.generated.generatedmod.plants.HerbAge;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;
import java.nio.charset.StandardCharsets;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class AlchemyObservationGameTests {
    private static String json(){try(var stream=AlchemyObservationGameTests.class.getResourceAsStream("/assets/wuxingxiantu/alchemy/observation.json")){return new String(stream.readAllBytes(),StandardCharsets.UTF_8);}catch(Exception e){throw new IllegalStateException(e);}}
    @GameTest(template="empty") public static void realMaterialsExposeYearsSourcesAndPresenceWithoutInventingStrength(GameTestHelper h){
        var defs=ObservationDefinitions.parse(json(),MaterialObservation.providers());var input=new SimpleContainer(8);
        var herb=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(new net.minecraft.resources.ResourceLocation("wuxingxiantu","spirit_sprout_grass"));
        var a=new ItemStack(herb,3);HerbAge.setYears(a,700);var b=new ItemStack(herb,2);HerbAge.setYears(b,1600);input.setItem(0,a);input.setItem(2,b);
        var before=a.save(new net.minecraft.nbt.CompoundTag());var snapshot=MaterialObservation.read(input,8,defs);
        h.assertTrue(MaterialObservation.value("item_count",snapshot)==5&&MaterialObservation.value("occupied_slots",snapshot)==2,"Quantity/occupancy mismatch");
        h.assertTrue(MaterialObservation.value("minimum_years",snapshot)==700&&MaterialObservation.value("maximum_years",snapshot)==1600,"Live year data mismatch");
        h.assertTrue(!snapshot.tags().isEmpty()&&snapshot.tags().stream().allMatch(t->t.sourceSlots().equals(java.util.List.of(1,3))&&t.strength().isEmpty()&&t.budget().isEmpty()),"Presence was confused with strength or lost provenance");
        h.assertTrue(before.equals(a.save(new net.minecraft.nbt.CompoundTag())),"Read-only observation mutated materials");h.succeed();
    }
    @GameTest(template="empty") public static void presentationRejectsUnknownSourcesDuplicatesAndVersions(GameTestHelper h){
        String source=json();var parsed=ObservationDefinitions.parse(source,MaterialObservation.providers());
        for(String bad:java.util.List.of(source.replace("occupied_slots","arbitrary_script"),source.replace("input.quantity","input.slots"),source.replace("\"schema\": 1","\"schema\": 99"),source.replace("\"schema\": 1","\"schema\": 1.5"))){
            boolean rejected=false;try{ObservationDefinitions.parse(bad,MaterialObservation.providers());}catch(RuntimeException expected){rejected=true;}h.assertTrue(rejected,"Invalid catalog accepted");
        }
        var changed=ObservationDefinitions.parse(source.replace("材料总量","自定义显示名"),MaterialObservation.providers());
        h.assertTrue(changed.parameters().get(1).label().equals("自定义显示名")&&changed.parameters().get(1).source().equals(parsed.parameters().get(1).source()),"Data-defined display extension failed");h.succeed();
    }
    @GameTest(template="empty") public static void emptyInputHasNoTagsAndDoesNotPredictGrade(GameTestHelper h){
        var defs=ObservationDefinitions.parse(json(),MaterialObservation.providers());var snapshot=MaterialObservation.read(new SimpleContainer(8),8,defs);
        h.assertTrue(snapshot.tags().isEmpty()&&defs.parameters().stream().allMatch(p->MaterialObservation.value(p.source(),snapshot)==0),"Empty material observation invented data");h.succeed();
    }
}
