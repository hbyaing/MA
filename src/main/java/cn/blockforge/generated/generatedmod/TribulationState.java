package cn.blockforge.generated.generatedmod;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

/** World journal plus rebuildable crater deadlines. Online trials also live in the owning player save. */
public final class TribulationState extends SavedData {
    enum Phase { COUNTDOWN, STRIKING, SUCCESS, FAILURE, SETTLED }
    static final class Trial {
        final UUID playerId;
        final ResourceKey<Level> dimension;
        final BlockPos origin;
        final int realm,totalStrikes;
        final float maximumHealth,previousAuraCap;
        int countdownTicks,strikesDone,strikeDelay;
        int skyDelay=60;
        int finalTicks;
        BlockPos finalGround;
        long visualSeed=java.util.concurrent.ThreadLocalRandom.current().nextLong();
        int cloudAgeTicks;
        boolean testing, harmless;
        Phase phase=Phase.COUNTDOWN;
        Trial(UUID owner,ResourceKey<Level> dimension,BlockPos origin,int realm,int strikes,float health,float aura) {
            this.playerId=owner;this.dimension=dimension;this.origin=origin.immutable();
            this.realm=realm;this.totalStrikes=strikes;this.maximumHealth=health;this.previousAuraCap=aura;
            countdownTicks=TribulationManager.COUNTDOWN_TICKS;
        }
        UUID playerId(){return playerId;}
        int realm(){return realm;}
        int totalStrikes(){return totalStrikes;}
        int strikesDone(){return strikesDone;}
        boolean settled(){return phase==Phase.SETTLED;}
        void settled(boolean value){if(value)phase=Phase.SETTLED;}
        CompoundTag save() {
            var t=new CompoundTag();t.putUUID("Owner",playerId);t.putString("Dimension",dimension.location().toString());
            t.putLong("Origin",origin.asLong());t.putInt("Realm",realm);t.putInt("Total",totalStrikes);
            t.putFloat("Health",maximumHealth);t.putFloat("AuraCap",previousAuraCap);
            t.putLong("VisualSeed",visualSeed);t.putInt("CloudAge",cloudAgeTicks);t.putBoolean("Testing",testing);t.putBoolean("Harmless",harmless);
            t.putInt("SkyDelay",skyDelay);t.putInt("Countdown",countdownTicks);t.putInt("Strikes",strikesDone);
            t.putInt("FinalTicks",finalTicks);if(finalGround!=null)t.putLong("FinalGround",finalGround.asLong());
            t.putInt("Delay",strikeDelay);t.putString("Phase",phase.name());return t;
        }
        static Trial load(CompoundTag t) {
            var trial=new Trial(t.getUUID("Owner"),dimension(t),BlockPos.of(t.getLong("Origin")),
                    Math.max(0,Math.min(7,t.getInt("Realm"))),Math.max(1,t.getInt("Total")),
                    t.getFloat("Health"),t.getFloat("AuraCap"));
            if(!Float.isFinite(trial.maximumHealth)||trial.maximumHealth<=0
                    ||!Float.isFinite(trial.previousAuraCap)||trial.previousAuraCap<0)
                throw new IllegalArgumentException("Invalid trial health/mana");
            if(t.contains("VisualSeed"))trial.visualSeed=t.getLong("VisualSeed");trial.testing=t.getBoolean("Testing");trial.harmless=t.getBoolean("Harmless");
            trial.countdownTicks=Math.max(0,t.getInt("Countdown"));
            trial.cloudAgeTicks=t.contains("CloudAge")?Math.max(0,t.getInt("CloudAge")):Math.max(0,TribulationManager.COUNTDOWN_TICKS-trial.countdownTicks);
            trial.skyDelay=t.contains("SkyDelay")?Math.max(0,t.getInt("SkyDelay")):Math.max(0,60-(TribulationManager.COUNTDOWN_TICKS-trial.countdownTicks));
            trial.strikesDone=Math.max(0,Math.min(trial.totalStrikes,t.getInt("Strikes")));
            trial.strikeDelay=Math.max(0,t.getInt("Delay"));
            trial.finalTicks=Math.max(0,Math.min(30,t.getInt("FinalTicks")));if(t.contains("FinalGround"))trial.finalGround=BlockPos.of(t.getLong("FinalGround"));
            trial.phase=Phase.valueOf(t.getString("Phase"));return trial;
        }
    }
    record CraterKey(ResourceKey<Level> dimension,BlockPos pos) {}
    record CraterBlock(CraterKey key,BlockState state,long restoreAt) {}
    final Map<UUID,Trial> trials=new HashMap<>();
    final Map<CraterKey,CraterBlock> craters=new HashMap<>();
    final PriorityQueue<CraterBlock> due=new PriorityQueue<>(Comparator.comparingLong(CraterBlock::restoreAt));
    public static TribulationState get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TribulationState::load,TribulationState::new,"wuxingxiantu_tribulations");
    }
    boolean recordCrater(ResourceKey<Level> dimension,BlockPos pos,BlockState original,long restoreAt) {
        var key=new CraterKey(dimension,pos.immutable());
        if(craters.containsKey(key))return false;
        var entry=new CraterBlock(key,original,restoreAt);craters.put(key,entry);due.add(entry);setDirty();return true;
    }
    void retry(CraterBlock entry,long time) {
        var retry=new CraterBlock(entry.key(),entry.state(),time);
        craters.put(retry.key(),retry);due.add(retry);setDirty();
    }
    @Override public CompoundTag save(CompoundTag root) {
        root.putInt("Schema",1);var active=new ListTag();
        trials.values().forEach(t->active.add(t.save()));root.put("Trials",active);
        var blocks=new ListTag();
        craters.values().forEach(c->{
            var t=new CompoundTag();t.putString("Dimension",c.key().dimension().location().toString());
            t.putLong("Pos",c.key().pos().asLong());t.putLong("RestoreAt",c.restoreAt());
            t.put("State",NbtUtils.writeBlockState(c.state()));blocks.add(t);
        });root.put("Craters",blocks);return root;
    }
    private static ResourceKey<Level> dimension(CompoundTag t) {
        return ResourceKey.create(Registries.DIMENSION,new ResourceLocation(t.getString("Dimension")));
    }
    public static TribulationState load(CompoundTag root) {
        var data=new TribulationState();
        for(var element:root.getList("Trials",Tag.TAG_COMPOUND)) {
            var trial=Trial.load((CompoundTag)element);
            if(!trial.settled())data.trials.put(trial.playerId,trial);
        }
        for(var element:root.getList("Craters",Tag.TAG_COMPOUND)) {
            var t=(CompoundTag)element;
            data.recordCrater(dimension(t),BlockPos.of(t.getLong("Pos")),
                    NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),t.getCompound("State")),t.getLong("RestoreAt"));
        }
        return data;
    }
}
