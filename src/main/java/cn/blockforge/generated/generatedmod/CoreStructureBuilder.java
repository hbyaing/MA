package cn.blockforge.generated.generatedmod;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Server-owned, held-input construction. No client inventory or placement data is accepted. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class CoreStructureBuilder {
    public static final int BUILD_TICKS=60;
    private static final class Session {
        final BlockPos core; final net.minecraft.resources.ResourceKey<Level> dimension;
        long heartbeat=-100;int progress;
        Session(ServerPlayer p,BlockPos core){this.core=core.immutable();dimension=p.level().dimension();}
    }
    private static final Map<UUID,Session> SESSIONS=new HashMap<>();
    public static boolean core(BlockState state){return AlchemyFurnaceBlock.isCore(state)||RefiningForgeBlock.isCore(state);}
    public static boolean complete(Level level,BlockPos p){return AlchemyFurnaceBlock.isCore(level.getBlockState(p))
            ?AlchemyFurnaceBlock.isFormed(level.getBlockState(p)):RefiningForgeBlock.isStructureComplete(level,p);}
    public static void activate(Level level,BlockPos pos,net.minecraft.world.entity.player.Player player){
        if(level.isClientSide){ClientPacketSink.coreBlueprint.accept(pos);return;}
        if(player instanceof ServerPlayer p){
            var previous=SESSIONS.get(p.getUUID());
            if(previous==null||!previous.core.equals(pos)||previous.dimension!=level.dimension())SESSIONS.put(p.getUUID(),new Session(p,pos));
            p.displayClientMessage(net.minecraft.network.chat.Component.literal("蓝图已开启：松开后，对准核心长按右键搭建（3秒）"),true);
        }
    }
    public static void input(ServerPlayer player,BlockPos pos,boolean held){
        var s=SESSIONS.get(player.getUUID());
        if(s==null||!s.core.equals(pos)||s.dimension!=player.level().dimension())return;
        if(!held){s.progress=0;s.heartbeat=-100;return;}
        if(valid(player,s)){s.heartbeat=player.level().getGameTime();}
    }
    private static boolean valid(ServerPlayer p,Session s){
        if(!p.isAlive()||p.isSpectator()||p.isPassenger()||s.dimension!=p.level().dimension()
                ||!p.serverLevel().hasChunkAt(s.core)||!core(p.level().getBlockState(s.core))||complete(p.level(),s.core)
                ||p.getEyePosition().distanceToSqr(Vec3.atCenterOf(s.core))>36||p.containerMenu!=p.inventoryMenu)return false;
        var hit=p.level().clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),p.getEyePosition().add(p.getLookAngle().scale(6)),
                net.minecraft.world.level.ClipContext.Block.OUTLINE,net.minecraft.world.level.ClipContext.Fluid.NONE,p));
        return hit.getType()==HitResult.Type.BLOCK&&hit.getBlockPos().equals(s.core);
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){
        if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p))return;
        var s=SESSIONS.get(p.getUUID());if(s==null)return;
        if(!valid(p,s)||p.level().getGameTime()-s.heartbeat>6){
            if(s.progress>0)Network.sendToPlayer(p,new cn.blockforge.generated.generatedmod.network.packet.CoreBuildProgressPacket(s.core,0,true));
            s.progress=0;return;
        }
        if(++s.progress>=BUILD_TICKS){s.progress=0;s.heartbeat=-100;
            String result=build(p,s.core);
            p.displayClientMessage(net.minecraft.network.chat.Component.literal(result),true);
            Network.sendToPlayer(p,new cn.blockforge.generated.generatedmod.network.packet.CoreBuildProgressPacket(s.core,0,true));
            if(complete(p.level(),s.core))SESSIONS.remove(p.getUUID());
        }else if(s.progress%5==0)Network.sendToPlayer(p,new cn.blockforge.generated.generatedmod.network.packet.CoreBuildProgressPacket(s.core,s.progress,false));
    }
    public static LinkedHashMap<BlockPos,BlockState> blueprint(Level level,BlockPos core){
        var result=new LinkedHashMap<BlockPos,BlockState>();
        if(AlchemyFurnaceBlock.isCore(level.getBlockState(core))){
            for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)for(int y=0;y<4;y++){
                var want=AlchemyFurnaceStructure.expected(x,z,y);if(want!=null)result.put(core.offset(x,y,z),want);
            }
        }else if(RefiningForgeBlock.isCore(level.getBlockState(core))){
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){
                if(x!=0||z!=0){result.put(core.offset(x,0,z),(Math.abs(x)==1&&Math.abs(z)==1?Blocks.POLISHED_BLACKSTONE_BRICK_SLAB:WuXingBlocks.RUNE_STONE.get()).defaultBlockState());
                    result.put(core.offset(x,1,z),WuXingBlocks.RUNE_STONE.get().defaultBlockState());}
            }
            result.put(core.above(),WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get().defaultBlockState());
        }return result;
    }
    private static boolean matches(BlockState actual,BlockState want){
        if(actual.equals(want))return true;
        if(actual.is(WuXingBlocks.RUNE_STONE.get())&&want.is(WuXingBlocks.RUNE_STONE.get()))return true;
        return false;
    }
    /** Complete preflight before touching inventory or world; existing correct materials are reused. */
    public static String build(ServerPlayer player,BlockPos core){
        var level=player.serverLevel();
        if(!CoreStructureBuilder.core(level.getBlockState(core))||complete(level,core))return "核心已成型或已移除";
        var pattern=blueprint(level,core);var changes=new LinkedHashMap<BlockPos,BlockState>();var costs=new LinkedHashMap<Item,Integer>();
        for(var entry:pattern.entrySet()){
            var pos=entry.getKey();var want=entry.getValue();
            if(!level.hasChunkAt(pos)||level.isOutsideBuildHeight(pos)||!level.getWorldBorder().isWithinBounds(pos))return "搭建范围未加载或超出世界边界";
            var actual=level.getBlockState(pos);if(matches(actual,want))continue;
            if(want.isAir()&&!actual.isAir())return "结构内部有障碍，请先清空内部空间";
            if(want.isAir())continue;
            if(!actual.canBeReplaced()||!actual.getFluidState().isEmpty()||level.getBlockEntity(pos)!=null)return "蓝图被方块阻挡，请先清理";
            if(!level.mayInteract(player,pos)||!player.mayUseItemAt(pos,Direction.UP,new ItemStack(want.getBlock())))return "此处不允许搭建";
            Item item=want.getBlock().asItem();if(item==Items.AIR)return "蓝图材料无对应物品";
            changes.put(pos,want);costs.merge(item,1,Integer::sum);
        }
        if(changes.isEmpty())return "结构仍有不匹配的部件，请检查蓝图";
        var inventory=player.getInventory();var available=new HashMap<Item,Integer>();
        for(var stack:inventory.items)available.merge(stack.getItem(),stack.getCount(),Integer::sum);
        for(var stack:inventory.offhand)available.merge(stack.getItem(),stack.getCount(),Integer::sum);
        var missing=new ArrayList<String>();
        if(!player.getAbilities().instabuild)costs.forEach((item,count)->{int lack=count-available.getOrDefault(item,0);if(lack>0)missing.add(item.getDescription().getString()+"×"+lack);});
        if(!missing.isEmpty())return "缺少材料："+String.join("、",missing);
        int radius=AlchemyFurnaceBlock.isCore(level.getBlockState(core))?2:1,height=radius==2?4:3;
        AABB bounds=new AABB(core.offset(-radius,0,-radius),core.offset(radius+1,height,radius+1));
        var exits=new LinkedHashMap<ServerPlayer,Vec3>();
        for(var p:level.players())if(p.getBoundingBox().intersects(bounds)){
            if(p.isPassenger())return "结构内有骑乘中的玩家，请先离开";
            Vec3 exit=safeExit(p,bounds,core,radius);if(exit==null)return "结构外没有安全落脚点，请先清理周围空间";exits.put(p,exit);
        }
        var snapshots=new ArrayList<net.minecraftforge.common.util.BlockSnapshot>();boolean success=false;
        AlchemyFurnaceStructure.mutating=true;RefiningForgeStructure.mutating=true;
        try{
            // Snapshot rollback preserves replaceable plants and their data; no drops/consumption on failure.
            for(var entry:changes.entrySet()){
                var snapshot=net.minecraftforge.common.util.BlockSnapshot.create(level.dimension(),level,entry.getKey());snapshots.add(snapshot);
                if(!level.setBlock(entry.getKey(),entry.getValue(),2)||net.minecraftforge.event.ForgeEventFactory.onBlockPlace(player,snapshot,Direction.UP))return "搭建被保护规则阻止，材料未消耗";
            }
            for(var entry:exits.entrySet()){
                var p=entry.getKey();var dest=entry.getValue();p.teleportTo(level,dest.x,dest.y,dest.z,p.getYRot(),p.getXRot());p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;
            }
            if(!player.getAbilities().instabuild)for(var entry:costs.entrySet()){
                int left=entry.getValue();for(var list:List.of(inventory.items,inventory.offhand))for(var stack:list)if(stack.is(entry.getKey())){int take=Math.min(left,stack.getCount());stack.shrink(take);left-=take;}
            }
            inventory.setChanged();player.inventoryMenu.broadcastChanges();success=true;
        }finally{try{if(!success)for(int i=snapshots.size()-1;i>=0;i--)snapshots.get(i).restore(true,false);}
            finally{AlchemyFurnaceStructure.mutating=false;RefiningForgeStructure.mutating=false;}}
        if(radius==2)AlchemyFurnaceStructure.refreshAround(level,core);else RefiningForgeStructure.refreshAround(level,core);
        return "结构搭建完成";
    }
    private static Vec3 safeExit(ServerPlayer p,AABB structure,BlockPos core,int radius){
        var level=p.serverLevel();
        for(int r=radius+2;r<=radius+7;r++)for(int dy=0;dy<=4;dy++)for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++){
            if(Math.abs(x)!=r&&Math.abs(z)!=r)continue;var pos=core.offset(x,dy,z);
            if(!level.hasChunkAt(pos)||!level.hasChunkAt(pos.below())||!level.getWorldBorder().isWithinBounds(pos)||level.isOutsideBuildHeight(pos.above()))continue;
            Vec3 target=Vec3.atBottomCenterOf(pos);AABB box=p.getBoundingBox().move(target.subtract(p.position()));
            if(!box.intersects(structure)&&level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(),Direction.UP)
                    &&level.getFluidState(pos).isEmpty()&&level.getFluidState(pos.above()).isEmpty()
                    &&!level.getBlockState(pos.below()).is(Blocks.MAGMA_BLOCK)&&!level.getBlockState(pos).is(Blocks.FIRE)&&level.noCollision(p,box))return target;
        }return null;
    }
    @SubscribeEvent public static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){SESSIONS.remove(e.getEntity().getUUID());}
    @SubscribeEvent public static void stopped(net.minecraftforge.event.server.ServerStoppedEvent e){SESSIONS.clear();}
    private CoreStructureBuilder(){}
}
