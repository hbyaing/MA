package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.simulation.*;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Dedicated-server execution of the kernel and real packaged rule data; not furnace gameplay acceptance. */
@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class AlchemySimulationGameTests {
    private static String json(){try(var in=AlchemySimulationGameTests.class.getResourceAsStream("/data/wuxingxiantu/alchemy_simulation/reference.json")){return new String(in.readAllBytes(),StandardCharsets.UTF_8);}catch(Exception e){throw new IllegalStateException(e);}}
    @GameTest(template="empty") public static void packagedRulesMatchIndependentHandSpecification(GameTestHelper h){
        var r=SimulationRuleLoader.parse(json());var initial=AlchemySimulation.begin(r,"server-test",Map.of("cold",10L,"hot",8L,"mediator",3L),0,21,7,Map.of());
        var s=AlchemySimulation.step(r,initial,List.of()).snapshot();
        h.assertTrue(s.stocks().equals(Map.of("cold",4L,"hot",2L,"product",9L,"residue",6L)),"Independent hand inventory mismatch");
        h.assertTrue(s.ledger().lostPotential()==3&&s.ledger().initialMass()==21,"Mass and potential conflated");h.succeed();
    }
    @GameTest(template="empty") public static void strictDataRejectsUnknownDuplicateAndUnfundedRules(GameTestHelper h){
        String source=json();for(String bad:List.of(source.replace("\"schema\": 1","\"schema\": 1, \"schema\": 1"),source.replace("\"schema\": 1","\"schema\": 1.1"),source.replace("\"schema\": 1","\"schema\": 2"),source.replace("\"windows\": []","\"windows\": [], \"script\": \"grant\""),source.replace("\"potential\": 0","\"potential\": 100"),source.replace("\"mass\": 1","\"mass\": -1"))){
            boolean rejected=false;try{SimulationRuleLoader.parse(bad);}catch(RuntimeException expected){rejected=true;}h.assertTrue(rejected,"Unsafe data accepted");
        }h.succeed();
    }
    @GameTest(template="empty") public static void serverPrivateCheckpointBindsRuleHashAndReplays(GameTestHelper h){
        var r=SimulationRuleLoader.parse(json());var s=AlchemySimulation.begin(r,"saved",Map.of("cold",10L,"hot",8L,"mediator",3L),0,21,123,Map.of());
        byte[] bytes=SimulationSnapshotCodec.encodeServerSnapshot(r,s);var restored=SimulationSnapshotCodec.decodeServerSnapshot(r,bytes);
        h.assertTrue(AlchemySimulation.step(r,s,List.of()).equals(AlchemySimulation.step(r,restored,List.of())),"Reload rerolled or lost inventory");
        boolean rejected=false;try{SimulationSnapshotCodec.decodeServerSnapshot(SimulationRuleLoader.parse(json().replace("reference-1","reference-2")),bytes);}catch(IllegalArgumentException expected){rejected=true;}
        h.assertTrue(rejected,"Rules drift silently rewrote old batch");h.succeed();
    }
}
