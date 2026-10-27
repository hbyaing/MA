package cn.blockforge.generated.generatedmod.plants;

import cn.blockforge.generated.generatedmod.world.SpiritVeinData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

/** Age remains on the planted primary specimen; extra propagules start at zero years. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=cn.blockforge.generated.generatedmod.GeneratedMod.MOD_ID)
public final class HerbCultivationData extends SavedData {
    private record Growth(int years,long ticks,long observed){}
    private final Map<Long,Growth> plants=new HashMap<>();
    private final Map<Long,Integer> ashUses=new HashMap<>();
    private final Set<Long> removed=new HashSet<>();
    public static HerbCultivationData get(ServerLevel level){synchronized(level.getDataStorage()){
        return level.getDataStorage().computeIfAbsent(HerbCultivationData::load,HerbCultivationData::new,"wuxingxiantu_herb_years");}}
    public int years(BlockPos pos){var g=plants.get(pos.asLong());return g==null?0:g.years();}
    public void plant(ServerLevel level,BlockPos pos,SpiritPlantDefinition def,ItemStack stack){
        removed.remove(pos.asLong());
        ashUses.put(pos.asLong(),stack.hasTag()?Math.max(0,Math.min(3,stack.getTag().getInt(HerbAge.ASH_USES))):0);
        plants.put(pos.asLong(),new Growth(HerbAge.boundedYears(def,HerbAge.years(stack)),0,level.getGameTime()));setDirty();
    }
    public void wild(ServerLevel level,BlockPos pos,SpiritPlantDefinition def){
        removed.remove(pos.asLong());
        ashUses.remove(pos.asLong());
        int years=HerbAge.randomYears(def,level.random);
        plants.put(pos.asLong(),new Growth(years,0,level.getGameTime()));setDirty();
    }
    public int ashUses(BlockPos pos){return ashUses.getOrDefault(pos.asLong(),0);}
    public boolean nourish(BlockPos pos){int used=ashUses(pos);if(used>=3)return false;ashUses.put(pos.asLong(),used+1);setDirty();return true;}
    public void remove(BlockPos pos){if(plants.remove(pos.asLong())!=null|ashUses.remove(pos.asLong())!=null)setDirty();}
    /** Player harvesting computes drops after onRemove; retain years until the tick ends. */
    public void removeAfterDrops(BlockPos pos){removed.add(pos.asLong());}
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void cleanup(net.minecraftforge.event.TickEvent.LevelTickEvent event){
        if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END||!(event.level instanceof ServerLevel level))return;
        var data=get(level);
        for(long key:data.removed)data.remove(BlockPos.of(key));
        data.removed.clear();
    }
    public static int cultivationMaximumYears(SpiritPlantDefinition def,boolean spiritSoil,int aura){
        return !spiritSoil?2499:aura<28?4499:aura<65?6499:aura<85?8499:10499;
    }
    public void advance(ServerLevel level,BlockPos pos,SpiritPlantBlock plant){
        long now=level.getGameTime();var old=plants.get(pos.asLong());
        if(old==null){plants.put(pos.asLong(),new Growth(0,0,now));setDirty();return;}
        // Clamp sparse observations: unloaded/offline elapsed time cannot age a specimen.
        long delta=Math.max(0,Math.min(600,now-old.observed()));
        int age=old.years();long ticks=old.ticks();var def=plant.definition();
        if(plant.isMature(level.getBlockState(pos))&&def.growthOneIn(level,pos)>0){
            int cap=cultivationMaximumYears(def,Substrate.isSpiritSoil(level.getBlockState(pos.below())),SpiritVeinData.get(level).auraAt(pos));
            ticks+=delta;
            while(ticks>=24000&&age<cap){age++;ticks-=24000;}
            if(age>=cap)ticks=0;
        }
        plants.put(pos.asLong(),new Growth(age,ticks,now));setDirty();
    }
    public static HerbCultivationData load(CompoundTag tag){
        var data=new HerbCultivationData();for(Tag value:tag.getList("Plants",Tag.TAG_COMPOUND)){
            var n=(CompoundTag)value;data.plants.put(n.getLong("Pos"),new Growth(Math.max(0,Math.min(HerbAge.MAX,n.getInt("Years"))),Math.max(0,Math.min(23999,n.getLong("Ticks"))),n.getLong("Observed")));data.ashUses.put(n.getLong("Pos"),Math.max(0,Math.min(3,n.getInt("AshUses"))));}return data;
    }
    @Override public CompoundTag save(CompoundTag tag){var list=new ListTag();plants.forEach((key,g)->{
        var n=new CompoundTag();n.putLong("Pos",key);n.putInt("Years",g.years());n.putLong("Ticks",g.ticks());n.putLong("Observed",g.observed());n.putInt("AshUses",ashUses.getOrDefault(key,0));list.add(n);});tag.put("Plants",list);return tag;}
}
