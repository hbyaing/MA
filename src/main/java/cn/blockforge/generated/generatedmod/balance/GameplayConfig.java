package cn.blockforge.generated.generatedmod.balance;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Server config is synchronized by Forge; pure balance classes retain standalone defaults. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class GameplayConfig {
    private static final ForgeConfigSpec.Builder BUILDER=new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.IntValue SERVANT_LIMIT=BUILDER.defineInRange("maxActiveServantsPerPlayer",32,1,128);
    public static final ForgeConfigSpec.IntValue MAX_ACTIVE_ALCHEMY_JOBS=BUILDER.defineInRange("maxActiveAlchemyJobsPerPlayer",1,1,64);
    public static final ForgeConfigSpec.IntValue MAX_ACTIVE_REFINING_JOBS=BUILDER.defineInRange("maxActiveRefiningJobsPerPlayer",1,1,64);
    public static final ForgeConfigSpec.IntValue COMPASS_SEARCH_TICKS=BUILDER.defineInRange("compassSearchCooldownTicks",60,20,1200);
    public static final ForgeConfigSpec.IntValue COMPASS_TELEPORT_TICKS=BUILDER.defineInRange("compassTeleportCooldownTicks",200,20,12000);
    public static final ForgeConfigSpec.DoubleValue COMPASS_COST=BUILDER.defineInRange("compassTeleportManaFraction",.04,.0,.5);
    private static final ForgeConfigSpec.DoubleValue TRIBULATION_BUDGET=BUILDER.defineInRange("tribulationHealthBudget",2.3,.5,10);
    private static final ForgeConfigSpec.IntValue[] MANA=new ForgeConfigSpec.IntValue[9];
    public static final ForgeConfigSpec SPEC;
    static {
        for(int realm=0;realm<MANA.length;realm++)MANA[realm]=BUILDER.defineInRange("realm"+realm+"Mana",RealmBalance.mana(realm),1,100000000);
        SPEC=BUILDER.build();
    }
    @SubscribeEvent public static void change(ModConfigEvent event){
        if(event.getConfig().getSpec()!=SPEC)return;
        if(event instanceof ModConfigEvent.Unloading){RealmBalance.resetMana();TribulationBalance.healthBudget=2.3F;return;}
        int[] values=new int[9];for(int i=0;i<9;i++)values[i]=MANA[i].get();
        for(int i=1;i<9;i++)if(values[i]<values[i-1]){
            com.mojang.logging.LogUtils.getLogger().error("Realm mana config must be nondecreasing; keeping previous table");return;
        }
        RealmBalance.configureMana(values);TribulationBalance.healthBudget=TRIBULATION_BUDGET.get().floatValue();
        var server=net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if(server!=null)server.execute(()->{
            for(var player:server.getPlayerList().getPlayers()){
                var data=GeneratedMod.data(player);if(data==null)continue;
                data.wuxingxiantu$setMaxMana(cn.blockforge.generated.generatedmod.items.CultivationLogic.calculateMaxMana(data));
                GeneratedMod.sync(player);
            }
        });
    }
    private GameplayConfig(){}
}
