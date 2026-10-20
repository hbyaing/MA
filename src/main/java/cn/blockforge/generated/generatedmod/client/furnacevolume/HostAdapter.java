package cn.blockforge.generated.generatedmod.client.furnacevolume;
import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.PillData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Host-native access. Track loads; never scan the render-distance grid per frame. */
public final class HostAdapter {
    private static final Set<AlchemyFurnaceBlockEntity> TRACKED=Collections.newSetFromMap(new IdentityHashMap<>());
    private static net.minecraft.client.multiplayer.ClientLevel world;
    private static boolean bootstrapped;
    public record Furnace(BlockEntity entity,BlockPos core,Vec3 anchor,double yaw,boolean brewing,
        long startTick,long finishTick,boolean hasResult,float completionAge,String activeFireId,long visualSeed,int tier,int grade){
        public double progress(double tick){return ((AlchemyFurnaceBlockEntity)entity).brewingProgress();}
        public boolean visible(){return brewing||hasResult;}
    }
    public static void track(AlchemyFurnaceBlockEntity furnace){
        var level=Minecraft.getInstance().level;if(level==null||furnace.getLevel()!=level)return;
        if(world!=level){TRACKED.clear();world=level;bootstrapped=false;}
        TRACKED.add(furnace);
    }
    static void clear(){TRACKED.clear();world=null;bootstrapped=false;}
    static boolean available(){return true;}
    static boolean hookInstalled(){return true;}
    public static Furnace read(Object value,float partial){
        if(!(value instanceof AlchemyFurnaceBlockEntity furnace)||furnace.isRemoved()||furnace.getLevel()!=Minecraft.getInstance().level||!AlchemyFurnaceBlock.isFormed(furnace.getBlockState()))return null;
        var p=furnace.getBlockPos();var result=PillData.read(furnace.completedResult());
        return new Furnace(furnace,p,new Vec3(p.getX()+.5,p.getY(),p.getZ()+.5),Math.toRadians(AlchemyFurnaceVisualLayout.rotation(AlchemyFurnaceStructure.inwardOf(furnace.getBlockState()))),
            furnace.brewing(),furnace.startTick(),furnace.finishTick(),furnace.hasResult(),furnace.completionAge(partial),furnace.activeFireId(),furnace.visualSeed(),furnace.tier(),result==null?1:Math.max(1,Math.min(9,result.grade().value())));
    }
    public static java.util.List<AlchemyFurnaceBlockEntity> warningFurnaces(){
        var level=Minecraft.getInstance().level;
        if(level==null||world!=level)return java.util.List.of();
        return TRACKED.stream().filter(f->!f.isRemoved()&&f.getLevel()==level&&f.explosionPending()).toList();
    }
    static List<Furnace> loaded(float partial){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null){clear();return List.of();}
        if(world!=mc.level){clear();world=mc.level;}
        int radius=mc.options.renderDistance().get()+1,cx=mc.player.blockPosition().getX()>>4,cz=mc.player.blockPosition().getZ()>>4;
        if(!bootstrapped){bootstrapped=true;for(int x=cx-radius;x<=cx+radius;x++)for(int z=cz-radius;z<=cz+radius;z++){
            var chunk=mc.level.getChunkSource().getChunk(x,z,ChunkStatus.FULL,false);if(chunk!=null)for(var be:chunk.getBlockEntities().values())if(be instanceof AlchemyFurnaceBlockEntity f)TRACKED.add(f);
        }}
        TRACKED.removeIf(f->f.isRemoved()||f.getLevel()!=mc.level);
        // Preserve the previous chunk/map traversal order and eight-furnace budget.
        var chunks=new TreeMap<ChunkPos,net.minecraft.world.level.chunk.LevelChunk>(Comparator.comparingInt((ChunkPos p)->p.x).thenComparingInt(p->p.z));
        for(var furnace:TRACKED){var p=new ChunkPos(furnace.getBlockPos());if(Math.abs(p.x-cx)>radius||Math.abs(p.z-cz)>radius||chunks.containsKey(p))continue;
            var chunk=mc.level.getChunkSource().getChunk(p.x,p.z,ChunkStatus.FULL,false);if(chunk!=null)chunks.put(p,chunk);
        }
        List<Furnace> list=new ArrayList<>();for(var chunk:chunks.values())for(var be:chunk.getBlockEntities().values()){
            var furnace=read(be,partial);if(furnace!=null&&furnace.visible())list.add(furnace);
        }return list;
    }
    private HostAdapter(){}
}
