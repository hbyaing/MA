package cn.blockforge.generated.generatedmod.importedcontent;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.mindscape.MindscapeManager;
import cn.blockforge.generated.generatedmod.plants.HerbAge;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** One four-herb batch nourishes one eligible player; no root reroll, automatic promotion or second realm data. */
public final class SpiritBathBlockEntity extends BlockEntity {
    public static final int NOURISH_TICKS=200;
    public static final List<String> HERBS=List.of("herb_juling","herb_huayuan","herb_xisui","herb_pozhang");
    private final NonNullList<ItemStack> herbs=NonNullList.withSize(4,ItemStack.EMPTY);
    private record Soak(long tick,int count,int realm,int stage) {}
    private final Map<UUID,Soak> soaking=new HashMap<>();
    public SpiritBathBlockEntity(BlockPos pos,BlockState state) {super(ImportedContent.BATH_ENTITY.get(),pos,state);}
    public NonNullList<ItemStack> herbs() {return herbs;}
    public int depositCount() {return (int)herbs.stream().filter(stack->!stack.isEmpty()).count();}
    public boolean isReady() {return depositCount()==4;}
    public boolean deposit(ItemStack stack) {
        if(stack.isEmpty()||isReady())return false;
        var id=BuiltInRegistries.ITEM.getKey(stack.getItem());
        int index="wuxingxiantu".equals(id.getNamespace())?HERBS.indexOf(id.getPath()):-1;
        if(index<0||!herbs.get(index).isEmpty())return false;
        herbs.set(index,stack.copyWithCount(1));setChanged();return true;
    }
    public int averageGrade() {
        int sum=0,count=0;
        for(var herb:herbs)if(!herb.isEmpty()) {sum+=Math.max(1,HerbAge.legacyEffectBand(herb));count++;}
        return count==0?1:Math.max(1,Math.min(5,Math.round((float)sum/count)));
    }
    public void serverTick() {
        if(!(level instanceof ServerLevel server)||!getBlockState().getValue(SpiritBathBlock.FILLED)||!isReady()) {soaking.clear();return;}
        var players=server.getEntitiesOfClass(ServerPlayer.class,new AABB(worldPosition).inflate(.2),
                player->player.isAlive()&&!player.isSpectator()&&!player.isCreative()&&player.getY()<=worldPosition.getY()+1.05);
        players.sort(Comparator.comparing(player->player.getUUID().toString()));
        Set<UUID> present=new HashSet<>();long now=server.getGameTime();
        for(var player:players) {
            var data=GeneratedMod.data(player);
            if(data==null||!MindscapeManager.canGain(player,data,false)||data.wuxingxiantu$getCultivation()>=CultivationLogic.breakthroughRequirement(data))continue;
            present.add(player.getUUID());var previous=soaking.get(player.getUUID());
            if(previous!=null&&previous.tick()==now)continue;
            int count=previous!=null&&previous.tick()==now-1&&previous.realm()==data.wuxingxiantu$getRealm()
                    &&previous.stage()==data.wuxingxiantu$getCultivationStage()?previous.count()+1:1;
            soaking.put(player.getUUID(),new Soak(now,count,data.wuxingxiantu$getRealm(),data.wuxingxiantu$getCultivationStage()));
            if(count>=NOURISH_TICKS) {
                long before=data.wuxingxiantu$getCultivation(),cap=CultivationLogic.breakthroughRequirement(data);
                long amount=Math.max(1,Math.round(cap*(.02+averageGrade()*.01)));
                CultivationLogic.gainCultivation(player,data,Math.min(cap-before,amount));
                long gain=data.wuxingxiantu$getCultivation()-before;
                if(gain>0) {
                    for(int i=0;i<4;i++)herbs.set(i,ItemStack.EMPTY);
                    soaking.clear();setChanged();server.setBlock(worldPosition,getBlockState().setValue(SpiritBathBlock.FILLED,false),3);
                    GeneratedMod.sync(player);player.displayClientMessage(Component.translatable("content.wuxingxiantu.bath_nourished",gain),true);
                    server.playSound(null,worldPosition,SoundEvents.BREWING_STAND_BREW,SoundSource.BLOCKS,1,1.2F);
                    return;
                }
            } else if(count%20==0)player.displayClientMessage(Component.translatable("content.wuxingxiantu.bath_soaking",(NOURISH_TICKS-count+19)/20),true);
        }
        soaking.keySet().retainAll(present);
    }
    @Override protected void saveAdditional(CompoundTag tag) {super.saveAdditional(tag);ContainerHelper.saveAllItems(tag,herbs);}
    @Override public void load(CompoundTag tag) {
        super.load(tag);for(int i=0;i<4;i++)herbs.set(i,ItemStack.EMPTY);ContainerHelper.loadAllItems(tag,herbs);soaking.clear();
        for(int i=0;i<4;i++) {
            var stack=herbs.get(i);var id=BuiltInRegistries.ITEM.getKey(stack.getItem());
            if(stack.isEmpty()||!"wuxingxiantu".equals(id.getNamespace())||!HERBS.get(i).equals(id.getPath()))herbs.set(i,ItemStack.EMPTY);
            else stack.setCount(1);
        }
    }
}
