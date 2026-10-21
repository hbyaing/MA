package cn.blockforge.generated.generatedmod.client.qa.myriad;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.economy.SpiritStoneGrade;
import cn.blockforge.generated.generatedmod.importedcreatures.*;
import cn.blockforge.generated.generatedmod.plants.*;
import cn.blockforge.generated.generatedmod.world.*;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Test source set only: ordinary terrain generation, disk save, and reopening the same world. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class WorldgenAcceptanceCapture {
    private static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.myriadWorldgen");
    private static final long START=System.nanoTime();
    private static final long SEED=7215L;
    private static final String WORLD="myriad-worldgen-"+System.currentTimeMillis();
    private static final Path OUTPUT=Path.of(System.getProperty("wuxingxiantu.myriadCaptureOutput","captures")).resolve(WORLD);
    private static final List<String> FAILURES=new ArrayList<>();
    private static final Map<String,Object> REPORT=new LinkedHashMap<>();
    private static final List<HerbEvidence> HERBS=new ArrayList<>();
    private static final List<VeinEvidence> VEINS=new ArrayList<>();
    private static final List<UUID> NATURAL_BOARS=new ArrayList<>();
    private static final ArrayDeque<ChunkPos> TO_LOAD=new ArrayDeque<>();
    private static final List<ChunkPos> HELD=new ArrayList<>();
    private static final UUID TICKET_OWNER=UUID.randomUUID();
    private static final TicketType<UUID> TICKET=TicketType.create("myriad_worldgen_qa",Comparator.<UUID>naturalOrder());
    private static volatile int phase;
    private static volatile boolean finished;
    private static boolean initialized,pointerExercised,reloadRequested;
    private static int serverTicks,spawnAttempts;
    private static long scanStart;
    private static BlockPos forestCenter,spawnSite;
    private static CompletableFuture<?> loading;
    private record HerbEvidence(long position,String block,String state,int years){}
    private record VeinEvidence(long chunk,int grade,long center,int count,int aura,Map<String,Integer> oreCounts){}

    @SubscribeEvent public static void client(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        try {
            Files.createDirectories(OUTPUT);mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            GLFW.glfwHideWindow(mc.getWindow().getWindow());
            if(elapsed()>710)throw new AssertionError("Worldgen/reload acceptance timed out in phase "+phase);
            if(phase==0&&mc.getOverlay()==null&&(mc.screen instanceof TitleScreen
                    ||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                if(Boolean.getBoolean("wuxingxiantu.myriadCapture")||Boolean.getBoolean("wuxingxiantu.myriadExternalShaders"))
                    throw new AssertionError("Run worldgen acceptance independently, with external shader capture disabled");
                phase=1;mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(30);
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                var rules=new GameRules();rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);
                rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);rules.getRule(GameRules.RULE_RANDOMTICKING).set(0,null);
                // Spawning is enabled. The optional explicit attempts below still use NaturalSpawner's production path.
                rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(true,null);
                mc.createWorldOpenFlows().createFreshLevel(WORLD,
                        new LevelSettings("Myriad natural worldgen acceptance",GameType.CREATIVE,false,net.minecraft.world.Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),
                        new WorldOptions(SEED,true,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(mc.player!=null&&!pointerExercised){pointerExercised=true;mc.mouseHandler.grabMouse();}
            if(phase==2){phase=3;mc.clearLevel(new TitleScreen());}
            if(phase==3&&mc.level==null&&mc.getSingleplayerServer()==null&&mc.getOverlay()==null&&!reloadRequested){
                reloadRequested=true;phase=4;mc.createWorldOpenFlows().loadLevel(new TitleScreen(),WORLD);
            }
            if(phase==6)finish();
        }catch(Throwable error){fail(error);finish();}
    }

    @SubscribeEvent public static void server(TickEvent.ServerTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END||(phase!=1&&phase!=4))return;
        var server=event.getServer();
        // Only the integrated server belonging to this QA world is eligible.
        if(server!=Minecraft.getInstance().getSingleplayerServer()||server.getPlayerList().getPlayers().isEmpty())return;
        try {
            var level=server.overworld();
            if(phase==4){verifyReload(level);phase=6;return;}
            if(!initialized){initialize(level);initialized=true;scanStart=System.nanoTime();}
            serverTicks++;
            if(loading!=null){if(!loading.isDone())return;loading.join();loading=null;}
            if(!TO_LOAD.isEmpty()){
                var pos=TO_LOAD.removeFirst();level.getChunkSource().addRegionTicket(TICKET,pos,2,TICKET_OWNER);HELD.add(pos);
                loading=level.getChunkSource().getChunkFuture(pos.x,pos.z,ChunkStatus.FULL,true).thenAccept(result->{
                    if(result.left().isEmpty())throw new AssertionError("Natural FULL chunk failed: "+pos+" "+result);
                });return;
            }
            if(serverTicks%20==0){
                tryNaturalSpawns(level);observeNaturalBoars(level);
                var wild=SpiritPlantRegions.get(level).save(new CompoundTag()).getList("Wild",Tag.TAG_COMPOUND);
                boolean imported=false;
                for(Tag value:wild){var pos=BlockPos.of(((CompoundTag)value).getLong("Pos"));if(!level.hasChunkAt(pos))continue;
                    if(level.getBlockState(pos).getBlock() instanceof SpiritPlantBlock plant&&plant.definition().id.startsWith("herb_")){imported=true;break;}}
                boolean veins=!SpiritVeinData.get(level).save(new CompoundTag()).getList("Veins",Tag.TAG_COMPOUND).isEmpty();
                long scanSeconds=(System.nanoTime()-scanStart)/1_000_000_000L;
                REPORT.put("observedWildPlantCount",wild.size());REPORT.put("observedImportedHerb",imported);REPORT.put("observedVein",veins);
                if(serverTicks%200==0)com.mojang.logging.LogUtils.getLogger().info("Worldgen QA: scan {} sec, wild {}, imported {}, vein {}, natural boars {}",scanSeconds,wild.size(),imported,veins,NATURAL_BOARS.size());
                if((imported&&veins&&scanSeconds>=75)||scanSeconds>480){
                    snapshot(level);server.saveEverything(false,true,true);REPORT.put("savedToDisk",true);phase=2;
                }
            }
        }catch(Throwable error){fail(error);phase=6;}
    }

    private static void initialize(ServerLevel level){
        REPORT.put("world",WORLD);REPORT.put("seed",level.getSeed());REPORT.put("normalTerrain",level.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator);
        REPORT.put("externalShadersRequested",false);REPORT.put("generationMutations","No offer/register/setBlock or direct entity construction; normal chunk generation and NaturalSpawner only");
        if(level.getSeed()!=SEED||!(level.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator))throw new AssertionError("QA world is not the expected fresh normal world");
        level.setDayTime(6000);level.setWeatherParameters(12000,0,false,false);
        var registered=new TreeSet<String>();var habitatRows=new ArrayList<Map<String,Object>>();
        var registry=level.registryAccess().registryOrThrow(Registries.BIOME);
        for(var entry:registry.entrySet()){
            var biome=entry.getValue();
            biome.getGenerationSettings().features().forEach(set->set.forEach(placed->{
                var config=placed.value().feature().value().config();
                if(config instanceof SpiritPlantFeature.Config c&&c.place.state.getBlock() instanceof SpiritPlantBlock plant&&plant.definition().id.startsWith("herb_"))registered.add(plant.definition().id);
            }));
            if(Set.of("plains","forest","birch_forest","meadow","savanna").contains(entry.getKey().location().getPath())){
                int boar=0,king=0;
                for(var spawn:biome.getMobSettings().getMobs(MobCategory.MONSTER).unwrap()){
                    if(spawn.type==ImportedCreatures.BLACK_BOAR.get())boar=spawn.getWeight().asInt();
                    if(spawn.type==ImportedCreatures.BLACK_BOAR_KING.get())king=spawn.getWeight().asInt();
                }
                habitatRows.add(Map.of("biome",entry.getKey().location().toString(),"boarWeight",boar,"kingWeight",king));
                if(boar!=5||king!=1)throw new AssertionError("Runtime biome lacks actual boar spawn injection: "+entry.getKey());
            }
        }
        REPORT.put("runtimeImportedHerbFeatures",registered);REPORT.put("runtimeBoarHabitatWeights",habitatRows);
        if(registered.size()!=18||habitatRows.size()!=5)throw new AssertionError("Runtime biome registry content incomplete");
        var spawn=level.getSharedSpawnPos();var center=new ChunkPos(spawn);var chunks=new LinkedHashSet<ChunkPos>();
        var found=level.findClosestBiome3d(holder->holder.unwrapKey().map(key->key.location().getPath().equals("forest")).orElse(false),spawn,2048,32,64);
        if(found!=null){forestCenter=found.getFirst();addNeighborhood(chunks,new ChunkPos(forestCenter));REPORT.put("forestLocation",position(forestCenter));}
        int selected=0;
        outer:for(int radius=0;radius<=24;radius++)for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){
            if(Math.max(Math.abs(dx),Math.abs(dz))!=radius)continue;int x=center.x+dx,z=center.z+dz;
            if(SpiritVein.hasVein(level.getSeed(),x,z)){addNeighborhood(chunks,new ChunkPos(x,z));if(++selected==12)break outer;}
        }
        TO_LOAD.addAll(chunks);REPORT.put("candidateNeighborhoods",selected);REPORT.put("explicitNaturalChunksRequested",chunks.size());
    }

    private static void addNeighborhood(Set<ChunkPos> chunks,ChunkPos center){
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)chunks.add(new ChunkPos(center.x+x,center.z+z));
    }

    private static void tryNaturalSpawns(ServerLevel level){
        if(spawnAttempts>=512||forestCenter==null)return;
        if(spawnSite==null){
            var chunk=new ChunkPos(forestCenter);
            for(int x=chunk.getMinBlockX();x<=chunk.getMaxBlockX()&&spawnSite==null;x++)for(int z=chunk.getMinBlockZ();z<=chunk.getMaxBlockZ();z++){
                int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);var p=new BlockPos(x,y,z);
                if(CreatureRegistration.canSpawn(ImportedCreatures.BLACK_BOAR.get(),level,net.minecraft.world.entity.MobSpawnType.NATURAL,p,level.random)){spawnSite=p;break;}
            }
            if(spawnSite==null)return;
            var player=level.getServer().getPlayerList().getPlayers().get(0);player.teleportTo(level,spawnSite.getX()+48.5,spawnSite.getY()+12,spawnSite.getZ()+.5,0,0);
            player.getAbilities().flying=true;player.onUpdateAbilities();REPORT.put("naturalSpawnAttemptSite",position(spawnSite));
        }
        for(int i=0;i<8&&spawnAttempts<512;i++,spawnAttempts++)NaturalSpawner.spawnCategoryForPosition(MobCategory.MONSTER,level,spawnSite);
    }

    private static void observeNaturalBoars(ServerLevel level){
        for(var entity:level.getAllEntities())if(entity instanceof BlackBoarEntity&&!NATURAL_BOARS.contains(entity.getUUID()))NATURAL_BOARS.add(entity.getUUID());
    }

    private static void snapshot(ServerLevel level){
        var regions=SpiritPlantRegions.get(level).save(new CompoundTag());
        for(Tag value:regions.getList("Wild",Tag.TAG_COMPOUND)){
            var pos=BlockPos.of(((CompoundTag)value).getLong("Pos"));if(!level.hasChunkAt(pos))continue;
            var state=level.getBlockState(pos);if(!(state.getBlock() instanceof SpiritPlantBlock plant))throw new AssertionError("Wild provenance points to no plant at "+pos);
            int years=HerbCultivationData.get(level).years(pos);if(years<=0||!state.canSurvive(level,pos))throw new AssertionError("Natural herb lost age or habitat at "+pos);
            HERBS.add(new HerbEvidence(pos.asLong(),BuiltInRegistries.BLOCK.getKey(plant).toString(),state.toString(),years));
        }
        for(Tag value:SpiritVeinData.get(level).save(new CompoundTag()).getList("Veins",Tag.TAG_COMPOUND)){
            var n=(CompoundTag)value;var center=BlockPos.of(n.getLong("Center"));if(!level.hasChunkAt(center))continue;
            var chunk=new ChunkPos(n.getLong("Chunk"));boolean loaded=true;
            for(int x=chunk.x-1;x<=chunk.x+1;x++)for(int z=chunk.z-1;z<=chunk.z+1;z++)if(level.getChunkSource().getChunkNow(x,z)==null)loaded=false;
            if(!loaded)continue;
            var ores=connectedOres(level,center);int count=n.getInt("Count");
            if(ores.values().stream().mapToInt(Integer::intValue).sum()!=count)throw new AssertionError("Actual connected ore count differs from saved vein at "+center);
            VEINS.add(new VeinEvidence(n.getLong("Chunk"),n.getInt("Grade"),center.asLong(),count,SpiritVeinData.get(level).auraAt(center),ores));
        }
        var seen=new TreeSet<String>();HERBS.forEach(e->seen.add(e.block));
        var missing=new TreeSet<String>();ImportedHerbs.DEFINITIONS.forEach(def->{String id=GeneratedMod.MOD_ID+":"+def.id;if(!seen.contains(id))missing.add(id);});
        REPORT.put("naturalHerbsBeforeReload",HERBS);REPORT.put("importedHerbSpeciesNotWitnessed",missing);REPORT.put("naturalVeinsBeforeReload",VEINS);
        REPORT.put("naturalBoarUUIDsObserved",NATURAL_BOARS);REPORT.put("explicitNaturalSpawnAttempts",spawnAttempts);
        REPORT.put("boarNaturalSpawnStatus",NATURAL_BOARS.isEmpty()?"NOT_WITNESSED; runtime habitat weights verified only":"WITNESSED through production NaturalSpawner; no direct entity construction");
        REPORT.put("regionsBeforeReload",regions.getList("Regions",Tag.TAG_COMPOUND).size());
        REPORT.put("herbNaturalGenerationPassed",HERBS.stream().anyMatch(e->e.block.startsWith(GeneratedMod.MOD_ID+":herb_")));
        REPORT.put("veinNaturalGenerationPassed",!VEINS.isEmpty());
        if(HERBS.stream().noneMatch(e->e.block.startsWith(GeneratedMod.MOD_ID+":herb_")))FAILURES.add("No imported herb naturally witnessed within the bounded terrain sample");
        if(VEINS.isEmpty())FAILURES.add("No successfully generated natural vein witnessed within the bounded terrain sample");
    }

    private static Map<String,Integer> connectedOres(ServerLevel level,BlockPos center){
        var ores=new HashSet<net.minecraft.world.level.block.Block>();for(var grade:SpiritStoneGrade.values())ores.add(grade.ore());
        var visited=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();var counts=new TreeMap<String,Integer>();queue.add(center);
        while(!queue.isEmpty()){
            var pos=queue.removeFirst();if(!visited.add(pos)||!level.hasChunkAt(pos))continue;
            var block=level.getBlockState(pos).getBlock();if(!ores.contains(block))continue;
            counts.merge(BuiltInRegistries.BLOCK.getKey(block).toString(),1,Integer::sum);
            if(counts.values().stream().mapToInt(Integer::intValue).sum()>4096)throw new AssertionError("Unbounded natural ore component");
            for(var direction:Direction.values())queue.add(pos.relative(direction));
        }return counts;
    }

    private static void verifyReload(ServerLevel level){
        for(var herb:HERBS){
            var pos=BlockPos.of(herb.position);level.getChunk(pos.getX()>>4,pos.getZ()>>4);
            var state=level.getBlockState(pos);
            if(!state.toString().equals(herb.state)||HerbCultivationData.get(level).years(pos)!=herb.years)throw new AssertionError("Disk reload changed natural herb/age at "+pos);
        }
        var stored=SpiritVeinData.get(level);var saved=stored.save(new CompoundTag());
        var attempted=new HashSet<Long>();for(long key:saved.getLongArray("Attempted"))attempted.add(key);
        for(var vein:VEINS){
            var center=BlockPos.of(vein.center);var chunk=new ChunkPos(vein.chunk);
            for(int x=chunk.x-1;x<=chunk.x+1;x++)for(int z=chunk.z-1;z<=chunk.z+1;z++)level.getChunk(x,z);
            var row=saved.getList("Veins",Tag.TAG_COMPOUND).stream().map(value->(CompoundTag)value).filter(n->n.getLong("Chunk")==vein.chunk).findFirst().orElseThrow();
            if(row.getInt("Grade")!=vein.grade||row.getInt("Count")!=vein.count||row.getLong("Center")!=vein.center
                    ||!connectedOres(level,center).equals(vein.oreCounts)||stored.auraAt(center)!=vein.aura
                    ||stored.pending().contains(vein.chunk)||!attempted.contains(vein.chunk))throw new AssertionError("Disk reload lost/duplicated natural vein at "+center);
        }
        var wild=new HashSet<Long>();for(Tag value:SpiritPlantRegions.get(level).save(new CompoundTag()).getList("Wild",Tag.TAG_COMPOUND))wild.add(((CompoundTag)value).getLong("Pos"));
        if(!HERBS.stream().allMatch(e->wild.contains(e.position)))throw new AssertionError("Disk reload lost natural herb provenance");
        REPORT.put("diskReloadPassed",true);REPORT.put("reloadHerbsChecked",HERBS.size());REPORT.put("reloadVeinsChecked",VEINS.size());
        REPORT.put("reloadBoarsStillPresent",NATURAL_BOARS.stream().filter(id->level.getEntity(id)!=null).map(UUID::toString).toList());
        REPORT.put("reloadScope","Exact plant states and ages, wild provenance, vein grade/center/count, connected actual ores, aura, attempted and pending guards; unwitnessed species are listed separately");
    }

    private static Map<String,Integer> position(BlockPos pos){return Map.of("x",pos.getX(),"y",pos.getY(),"z",pos.getZ());}
    private static long elapsed(){return (System.nanoTime()-START)/1_000_000_000L;}
    private static synchronized void fail(Throwable error){FAILURES.add(error.toString());com.mojang.logging.LogUtils.getLogger().error("Natural worldgen acceptance failed",error);}
    private static void finish(){
        if(finished)return;finished=true;
        try {
            PointerSafety.verify();
        }catch(Throwable error){fail(error);}
        try {
            Files.createDirectories(OUTPUT);REPORT.put("elapsedSeconds",elapsed());REPORT.put("pointerSafety",PointerSafety.snapshot());REPORT.put("failures",List.copyOf(FAILURES));
            REPORT.put("complete",FAILURES.isEmpty()&&Boolean.TRUE.equals(REPORT.get("diskReloadPassed")));
            Files.writeString(OUTPUT.resolve("worldgen-acceptance.json"),new GsonBuilder().setPrettyPrinting().create().toJson(REPORT));
        }catch(Throwable error){com.mojang.logging.LogUtils.getLogger().error("Could not write natural worldgen evidence",error);}
        Minecraft.getInstance().stop();
    }
    private WorldgenAcceptanceCapture(){}
}
