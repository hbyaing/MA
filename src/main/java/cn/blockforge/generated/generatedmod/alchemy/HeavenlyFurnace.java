package cn.blockforge.generated.generatedmod.alchemy;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.registry.ModRecipes;
import cn.blockforge.generated.generatedmod.alchemy.batch.*;
import cn.blockforge.generated.generatedmod.network.packet.HeavenlyFurnaceStatePacket;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import java.util.*;

/** Server-authoritative portable alchemy. Escrow survives save/reload; visual packets never grant items. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class HeavenlyFurnace {
    public static final String ID="tian_di_wei_lu", ESCROW="WuxingHeavenlyFurnace";
    public static final int GATHER_TICKS=240, CONDENSE_TICKS=40;
    public static final double MOVE_RADIUS=32, MOVE_HEIGHT=24;
    public static boolean withinCastingRange(Vec3 position,Vec3 anchor){var d=position.subtract(anchor);return d.x*d.x+d.z*d.z<=MOVE_RADIUS*MOVE_RADIUS&&Math.abs(d.y)<=MOVE_HEIGHT;}
    public static Vec3 fireballCenter(Vec3 origin,float yaw){
        double angle=Math.toRadians(yaw);return origin.add(-Math.sin(angle)*4.5,4.5,Math.cos(angle)*4.5);
    }
    public static Vec3 fireballCenter(CompoundTag tag){return fireballCenter(new Vec3(tag.getDouble("X"),tag.getDouble("Y"),tag.getDouble("Z")),tag.getFloat("Yaw"));}
    private record Offer(UUID token,long expires) {}
    private static final Map<UUID,Offer> OFFERS=new HashMap<>();
    private static final Map<UUID,CompoundTag> RUNNING=new HashMap<>();
    private HeavenlyFurnace() {}
    public static boolean active(ServerPlayer p){return RUNNING.containsKey(p.getUUID())||PortableBatch.pending(p);}
    public static int furnaceTier(int realm){return realm>=ProgressionGate.SPIRIT_TRANSFORMATION?5:4;}
    public static int duration(AlchemyRecipe r){return GATHER_TICKS+CONDENSE_TICKS+(int)Math.ceil(Math.min(72000,r.cookTime())*1.25);}
    public static int cost(AlchemyRecipe r,CultivationData d){
        long fuel=2L*AlchemyBalance.burnPerSecond(r.furnaceTier())*((duration(r)+19L)/20L);
        return (int)Math.min(Integer.MAX_VALUE,800L+r.auraCost(d)+fuel);
    }
    private static CompoundTag persistent(ServerPlayer p){
        var root=p.getPersistentData();
        if(!root.contains(Player.PERSISTED_NBT_TAG,Tag.TAG_COMPOUND))root.put(Player.PERSISTED_NBT_TAG,new CompoundTag());
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }
    private static void send(ServerPlayer p,CompoundTag tag){Network.CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new HeavenlyFurnaceStatePacket(tag));}
    private static void broadcast(ServerPlayer p,CompoundTag tag){
        Network.CHANNEL.send(PacketDistributor.NEAR.with(()->new PacketDistributor.TargetPoint(p.getX(),p.getY(),p.getZ(),64,p.level().dimension())),new HeavenlyFurnaceStatePacket(tag));
    }
    private static String commonGate(ServerPlayer p){
        var d=GeneratedMod.data(p);
        if(d==null||!d.wuxingxiantu$hasLearnedDivineArt(ID)||!ProgressionGate.isAtLeastRealm(p,DivineArtCatalog.requiredRealm(ID)))return "尚未掌握天地为炉，或境界不足元婴。";
        if(p.isSpectator()||!p.isAlive())return "当前无法炼制。";
        if(!d.wuxingxiantu$getActiveDivineArt().isBlank())return "请先收束正在使用的其他神通。";
        if(!d.wuxingxiantu$hasCreationBlessing()&&d.wuxingxiantu$getDivineCooldown(ID)>p.level().getGameTime())return "天地为炉尚未冷却。";
        int fire=d.wuxingxiantu$getEquippedFire();
        if(fire<1||fire>9||!ProgressionGate.canUseFire(p,fire))return "请先装备本人能够驾驭的异火。";
        return "";
    }
    public static String recipeGate(ServerPlayer p,AlchemyRecipe r){
        var d=GeneratedMod.data(p);
        if(!FormulaKnowledge.unlocked(p))return "请先开启炼丹传承。";
        if(r==null||PillData.read(r.getResultItem(p.level().registryAccess()))==null)return "请选择有效丹方。";
        if(!FormulaKnowledge.knows(p,r.formulaId()))return "尚未学习此丹方。";
        if(!ProgressionGate.isAtLeastRealm(p,r.minimumRealm())||r.furnaceTier()>furnaceTier(d.wuxingxiantu$getRealm()))return "此丹方超出当前境界可炼范围。";
        if(r.cookTime()>72000)return "此丹方炼制时间超出神通承载范围。";
        if(!r.acceptsFire(d.wuxingxiantu$getEquippedFire()))return "当前异火不符合此丹方要求。";
        if(allocationV5(r,p.getInventory())==null)return "背包材料不足。";
        if(Network.isAlchemyBusy(p))return "已有炼丹任务正在进行。";
        if(!p.isCreative()&&d.wuxingxiantu$getAura()<Math.max(cost(r,d),PortableBatch.fuel(p,r)))return "灵力不足，需要 "+Math.max(cost(r,d),PortableBatch.fuel(p,r))+"。";
        return "";
    }
    public static boolean open(ServerPlayer p){
        if(active(p))return false;
        String reason=commonGate(p);if(!reason.isEmpty()){message(p,reason);return false;}
        UUID token=UUID.randomUUID();OFFERS.put(p.getUUID(),new Offer(token,p.level().getGameTime()+1200));
        var tag=new CompoundTag();tag.putString("Kind","open");tag.putUUID("Token",token);var rows=new ListTag();
        var recipes=p.level().getRecipeManager().getAllRecipesFor(ModRecipes.ALCHEMY_TYPE.get()).stream().sorted(Comparator.comparing(r->r.getId().toString())).toList();
        for(var r:recipes){
            if(PillData.read(r.getResultItem(p.level().registryAccess()))==null)continue;
            if(rows.size()>=512)break;
            var row=new CompoundTag();row.putString("Recipe",r.getId().toString());row.put("Result",r.getResultItem(p.level().registryAccess()).save(new CompoundTag()));
            row.putString("Reason",recipeGate(p,r));row.putInt("Cost",Math.max(cost(r,GeneratedMod.data(p)),PortableBatch.fuel(p,r)));row.putInt("Duration",GATHER_TICKS+V5Batch.profile(V5Batch.currentProfile(p.serverLevel()),AlchemyBalance.burnPerSecond(furnaceTier(GeneratedMod.data(p).wuxingxiantu$getRealm()))).steps()*20);row.putBoolean("V5",true);
            row.putInt("Failure",-1);
            var ingredients=new ListTag();for(int i=0;i<r.getIngredients().size();i++){
                var choices=r.getIngredients().get(i).getItems();if(choices.length==0)continue;
                var sample=choices[0].copy();sample.setCount(r.counts().get(i));ingredients.add(sample.save(new CompoundTag()));
            }row.put("Ingredients",ingredients);rows.add(row);
        }
        for(var r:p.level().getRecipeManager().getAllRecipesFor(ModRecipes.REFINING_TYPE.get()).stream().sorted(Comparator.comparing(x->x.getId().toString())).toList()){
            if(rows.size()>=768)break;
            var row=new CompoundTag();row.putString("Mode","refining");row.putString("Recipe",r.getId().toString());row.put("Result",r.getResultItem(p.level().registryAccess()).save(new CompoundTag()));
            row.putString("Reason",refiningGate(p,r));row.putInt("Cost",cost(r,p));row.putInt("Duration",GATHER_TICKS+V5Batch.profile(V5Batch.currentProfile(p.serverLevel()),AlchemyBalance.burnPerSecond(furnaceTier(GeneratedMod.data(p).wuxingxiantu$getRealm()))).steps()*20);row.putBoolean("V5",true);row.putInt("Failure",0);
            var ingredients=new ListTag();for(int i=0;i<r.getIngredients().size();i++){var choices=r.getIngredients().get(i).getItems();if(choices.length>0)ingredients.add(choices[0].copyWithCount(r.counts().get(i)).save(new CompoundTag()));}
            row.put("Ingredients",ingredients);rows.add(row);
        }
        tag.put("Rows",rows);send(p,tag);return true;
    }
    public static boolean start(ServerPlayer p,UUID token,ResourceLocation recipeId){return start(p,token,recipeId,new int[]{-1,-1,-1});}
    public static boolean start(ServerPlayer p,UUID token,ResourceLocation recipeId,int[] auxiliarySlots){
        var offer=OFFERS.get(p.getUUID());if(offer==null||!offer.token.equals(token)||offer.expires<p.level().getGameTime()||active(p))return false;
        String reason=commonGate(p);if(!reason.isEmpty()){message(p,reason);return false;}
        var selected=p.level().getRecipeManager().byKey(recipeId).orElse(null);
        if(selected instanceof RefiningRecipe refining){if(auxiliarySlots==null||auxiliarySlots.length!=3||java.util.Arrays.stream(auxiliarySlots).anyMatch(i->i!=-1))return false;return startRefining(p,refining);}
        var r=p.level().getRecipeManager().byKey(recipeId).filter(AlchemyRecipe.class::isInstance).map(AlchemyRecipe.class::cast).orElse(null);
        reason=recipeGate(p,r);if(!reason.isEmpty()){message(p,reason);open(p);return false;}
        Vec3 origin=p.position();
        Vec3 center=fireballCenter(origin,p.getYRot());
        if(!p.level().hasChunkAt(net.minecraft.core.BlockPos.containing(center))||!p.level().getWorldBorder().isWithinBounds(net.minecraft.core.BlockPos.containing(center))
                ||!p.level().noCollision(p,new AABB(center.x-1.1,center.y-1.1,center.z-1.1,center.x+1.1,center.y+1.1,center.z+1.1))){message(p,"前方上空空间不足，请面向空旷处施法。");return false;}
        var d=GeneratedMod.data(p);int[] used=allocationV5(r,p.getInventory());if(used==null)return false;
        cn.blockforge.generated.generatedmod.alchemy.auxiliary.AuxiliaryAlchemy.Allocation auxiliary;
        try{auxiliary=cn.blockforge.generated.generatedmod.alchemy.auxiliary.AuxiliaryAlchemy.allocate(p.getInventory(),used,auxiliarySlots);}catch(IllegalArgumentException invalid){message(p,"副药选择无效，不能与主药占用同一槽。");return false;}
        used=auxiliary.counts();
        var saved=new CompoundTag();var items=new ListTag();
        for(int i=0;i<used.length;i++)if(used[i]>0)items.add(p.getInventory().getItem(i).copyWithCount(used[i]).save(new CompoundTag()));
        saved.put("Items",items);saved.put("Preview",r.getResultItem(p.level().registryAccess()).save(new CompoundTag()));
        saved.putDouble("X",origin.x);saved.putDouble("Y",origin.y);saved.putDouble("Z",origin.z);saved.putFloat("Yaw",p.getYRot());saved.putInt("Fire",d.wuxingxiantu$getEquippedFire());
        saved.putInt("Duration",GATHER_TICKS+V5Batch.profile(V5Batch.currentProfile(p.serverLevel()),AlchemyBalance.burnPerSecond(furnaceTier(d.wuxingxiantu$getRealm()))).steps()*20);
        saved.putLong("Start",p.level().getGameTime());saved.putString("Dimension",p.level().dimension().location().toString());saved.putUUID("Owner",p.getUUID());saved.putBoolean("V5",true);
        try{if(!PortableBatch.start(p,r,used,saved))return false;}
        catch(RuntimeException failure){message(p,"V5托管未确认，已保留日志待恢复："+failure.getMessage());com.mojang.logging.LogUtils.getLogger().error("Portable V5 admission retained",failure);return false;}
        RUNNING.put(p.getUUID(),saved);OFFERS.remove(p.getUUID());Network.showDivineArtBanner(p,ID);broadcast(p,visual(saved,p.level().getGameTime()));
        message(p,"天地为炉：主药副药已托管至V5；收束将结算有限药渣，不返还原料。");return true;
    }

    public static int duration(RefiningRecipe r){return GATHER_TICKS+CONDENSE_TICKS+(int)Math.ceil(Math.min(72000,r.craftTime())*1.25);}
    public static int cost(RefiningRecipe r,ServerPlayer p){
        int base=cn.blockforge.generated.generatedmod.progression.PlayerCrafts.refiningCost(r.auraCost(),cn.blockforge.generated.generatedmod.progression.PlayerCrafts.bonus(p,"refining"));
        return (int)Math.min(Integer.MAX_VALUE,800L+base+2L*Math.max(1,r.requiredFireTier())*((duration(r)+19L)/20L));
    }
    public static String refiningGate(ServerPlayer p,RefiningRecipe r){
        if(r==null||r.getResultItem(p.level().registryAccess()).isEmpty())return "请选择有效器方。";
        var d=GeneratedMod.data(p);if(d==null)return "角色数据未就绪。";
        if(!ProgressionGate.canUseRefiningForge(p,r.temperature()))return "境界不足以掌握此器方火候。";
        if(d.wuxingxiantu$getEquippedFire()<r.requiredFireTier())return "当前异火品阶不足。";
        if(r.craftTime()>72000)return "此器方超出神通承载时间。";
        int[] allocated=allocation(r,p.getInventory());
        if(allocated==null)return "背包材料不足。";
        var consumed=new java.util.ArrayList<ItemStack>();
        for(int i=0;i<allocated.length;i++)if(allocated[i]>0)consumed.add(p.getInventory().getItem(i).copyWithCount(allocated[i]));
        if(!cn.blockforge.generated.generatedmod.crafting.EquipmentBond.canConsume(p,consumed)
                ||cn.blockforge.generated.generatedmod.crafting.EquipmentCrafting.inherit(r.getResultItem(p.level().registryAccess()).copy(),consumed).isEmpty())
            return "器方材料包含其他主人的装备，或无法保留唯一主人。";
        if(!p.isCreative()&&d.wuxingxiantu$getAura()<cost(r,p))return "灵力不足，需要 "+cost(r,p)+"。";
        return "";
    }
    private static boolean startRefining(ServerPlayer p,RefiningRecipe r){
        String reason=refiningGate(p,r);if(!reason.isEmpty()){message(p,reason);open(p);return false;}
        Vec3 origin=p.position(),center=fireballCenter(origin,p.getYRot());
        if(!p.level().hasChunkAt(net.minecraft.core.BlockPos.containing(center))||!p.level().getWorldBorder().isWithinBounds(net.minecraft.core.BlockPos.containing(center))||!p.level().noCollision(p,new AABB(center.x-1.8,center.y-1.5,center.z-1.8,center.x+1.8,center.y+1.5,center.z+1.8))){message(p,"前方上空空间不足，请面向空旷处施法。");return false;}
        int[] used=allocation(r,p.getInventory());if(used==null)return false;
        var saved=new CompoundTag();var items=new ListTag();
        for(int i=0;i<used.length;i++)if(used[i]>0)items.add(p.getInventory().getItem(i).copyWithCount(used[i]).save(new CompoundTag()));
        var consumed=items.stream().map(t->ItemStack.of((CompoundTag)t)).toList();
        if(!cn.blockforge.generated.generatedmod.crafting.EquipmentBond.canConsume(p,consumed))return false;
        var result=cn.blockforge.generated.generatedmod.crafting.EquipmentCrafting.inherit(r.getResultItem(p.level().registryAccess()).copy(),consumed);
        if(result.isEmpty())return false;
        result=cn.blockforge.generated.generatedmod.importedcreatures.BeastMaterialEffects.infuseRefining(result,consumed);
        result=cn.blockforge.generated.generatedmod.artifacts.ArtifactData.infuseRefining(result,consumed);
        saved.putString("Mode","refining");saved.put("Items",items);saved.put("Result",result.save(new CompoundTag()));saved.put("Preview",result.save(new CompoundTag()));
        saved.putDouble("X",origin.x);saved.putDouble("Y",origin.y);saved.putDouble("Z",origin.z);saved.putFloat("Yaw",p.getYRot());saved.putInt("Fire",GeneratedMod.data(p).wuxingxiantu$getEquippedFire());
        saved.putInt("Duration",duration(r));saved.putLong("Start",p.level().getGameTime());saved.putString("Dimension",p.level().dimension().location().toString());saved.putUUID("Owner",p.getUUID());
        for(int i=0;i<used.length;i++)if(used[i]>0)p.getInventory().getItem(i).shrink(used[i]);
        persistent(p).put(ESCROW,saved.copy());RUNNING.put(p.getUUID(),saved);OFFERS.remove(p.getUUID());p.getInventory().setChanged();
        var d=GeneratedMod.data(p);if(!p.isCreative())d.wuxingxiantu$setAura(d.wuxingxiantu$getAura()-cost(r,p));
        d.wuxingxiantu$setDivineCooldown(ID,p.level().getGameTime()+DivineArtCatalog.cooldownTicks(ID));GeneratedMod.sync(p);p.inventoryMenu.broadcastChanges();Network.showDivineArtBanner(p,ID);broadcast(p,visual(saved,p.level().getGameTime()));
        message(p,"天地为炉：万物入炉，造化铸器；再次发动可收束返材。");return true;
    }
    public static CompoundTag visual(CompoundTag saved){
        var tag=saved.copy();tag.remove("Result");tag.putString("Kind","start");return tag;
    }
    public static CompoundTag visual(CompoundTag saved,long now){var tag=visual(saved);tag.putLong("Elapsed",Math.max(0,now-saved.getLong("Start")));return tag;}
    private static void give(ServerPlayer p,ItemStack stack){if(!p.getInventory().add(stack)&&!stack.isEmpty())p.drop(stack,false);}
    public static void cancel(ServerPlayer p){
        if(PortableBatch.pending(p)){
            PortableBatch.cancel(p);RUNNING.remove(p.getUUID());OFFERS.remove(p.getUUID());
            var end=new CompoundTag();end.putString("Kind","end");end.putUUID("Owner",p.getUUID());broadcast(p,end);
            message(p,PortableBatch.pending(p)?"结算已封存，请腾出背包空位后再次发动领取。":"V5已收束，按守恒结算药渣或成丹，原料不重复返还。");return;
        }
        RUNNING.remove(p.getUUID());OFFERS.remove(p.getUUID());var root=persistent(p);
        if(!root.contains(ESCROW,Tag.TAG_COMPOUND))return;
        var saved=root.getCompound(ESCROW).copy();root.remove(ESCROW);
        for(var value:saved.getList("Items",Tag.TAG_COMPOUND))give(p,ItemStack.of((CompoundTag)value));
        var end=new CompoundTag();end.putString("Kind","end");end.putUUID("Owner",p.getUUID());
        Network.CHANNEL.send(PacketDistributor.ALL.noArg(),new HeavenlyFurnaceStatePacket(end));
        p.inventoryMenu.broadcastChanges();message(p,"天地为炉已收束，材料返还；已消耗灵力不返还。");
    }
    private static void finish(ServerPlayer p,CompoundTag saved){
        RUNNING.remove(p.getUUID());persistent(p).remove(ESCROW);
        var result=ItemStack.of(saved.getCompound("Result"));give(p,result.copy());
        if(!result.is(WuXingItems.PILL_ASH.get()))cn.blockforge.generated.generatedmod.progression.PlayerCrafts.get(p.server).receipt(p.getUUID(),saved.getString("Mode").equals("refining")?"refining":"alchemy",result,cn.blockforge.generated.generatedmod.progression.PlayerCrafts.itemTier(result));p.inventoryMenu.broadcastChanges();
        var tag=new CompoundTag();tag.putString("Kind","complete");tag.putUUID("Owner",p.getUUID());tag.put("Result",result.save(new CompoundTag()));broadcast(p,tag);
        message(p,result.is(WuXingItems.PILL_ASH.get())?"炼制失败，获得丹灰。":(saved.getString("Mode").equals("refining")?"器成：":"凝丹完成：")+result.getHoverName().getString());
    }
    private static void message(ServerPlayer p,String text){p.displayClientMessage(Component.literal(text),true);}
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){
        if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p))return;
        var saved=RUNNING.get(p.getUUID());
        if(saved==null&&p.tickCount%20==0){try{var doc=PortableBatch.restore(p);if(!doc.isEmpty()){saved=doc.getCompound("Visual").copy();RUNNING.put(p.getUUID(),saved);}}catch(RuntimeException failure){com.mojang.logging.LogUtils.getLogger().error("Portable V5 recovery retained",failure);} }
        if(saved==null)return;
        long elapsed=p.level().getGameTime()-saved.getLong("Start");
        if(!p.isAlive()||p.isSpectator()||!p.level().dimension().location().toString().equals(saved.getString("Dimension"))
                ||!withinCastingRange(p.position(),new Vec3(saved.getDouble("X"),saved.getDouble("Y"),saved.getDouble("Z")))
                ||GeneratedMod.data(p)==null||!ProgressionGate.isAtLeastRealm(p,DivineArtCatalog.requiredRealm(ID))
                ||!ProgressionGate.canUseFire(p,saved.getInt("Fire"))
                ||!GeneratedMod.data(p).wuxingxiantu$getActiveDivineArt().isBlank()
                ||GeneratedMod.data(p).wuxingxiantu$getEquippedFire()!=saved.getInt("Fire")) {cancel(p);return;}
        if(saved.getBoolean("V5")){
            if(p.tickCount%20!=0)return;
            try{var doc=PortableBatch.advance(p);if(doc.getString("State").equals("CLAIMED")){
                RUNNING.remove(p.getUUID());var complete=new CompoundTag();complete.putString("Kind","complete");complete.putUUID("Owner",p.getUUID());complete.put("Result",V5Batch.result(doc).save(new CompoundTag()));broadcast(p,complete);message(p,"V5结算完成："+V5Batch.result(doc).getHoverName().getString());
            }else if(!doc.getString("State").equals("ACTIVE")){message(p,"V5结果已封存，请腾出一个背包空位领取。");}
            else broadcast(p,visual(saved,p.level().getGameTime()));
            }catch(RuntimeException failure){com.mojang.logging.LogUtils.getLogger().error("Portable V5 step retained",failure);}return;
        }
        if(elapsed>=saved.getInt("Duration")){finish(p,saved);return;}
        if(elapsed%40==0)broadcast(p,visual(saved,p.level().getGameTime()));
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){if(PortableBatch.pending(p)){try{var doc=PortableBatch.restore(p);if(!doc.isEmpty())RUNNING.put(p.getUUID(),doc.getCompound("Visual").copy());}catch(RuntimeException failure){com.mojang.logging.LogUtils.getLogger().error("Portable login retained",failure);}}else cancel(p);}}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){if(PortableBatch.pending(p))RUNNING.remove(p.getUUID());else cancel(p);}}
    @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p)cancel(p);}
    @SubscribeEvent public static void stopping(ServerStoppingEvent e){for(var p:e.getServer().getPlayerList().getPlayers())if(!PortableBatch.pending(p))cancel(p);RUNNING.clear();OFFERS.clear();}

    /** Capacitated bipartite matching handles overlapping tags without double-spending stacks. */
    public static int[] allocationV5(AlchemyRecipe r,Container inventory){return allocation(r.getIngredients(),r.counts(),inventory);}
    public static int[] allocation(AlchemyRecipe r,Container inventory){
        var used=allocation(r.getIngredients(),r.counts(),inventory);
        return used!=null&&MedicineTier.accepts(r.getResultItem(net.minecraft.core.RegistryAccess.EMPTY),inventory,used)?used:null;
    }
    public static int[] allocation(RefiningRecipe r,Container inventory){return allocation(r.getIngredients(),r.counts(),inventory);}
    private static int[] allocation(java.util.List<net.minecraft.world.item.crafting.Ingredient> ingredients,java.util.List<Integer> counts,Container inventory){
        int slots=Math.min(36,inventory.getContainerSize()),terms=ingredients.size(),sink=slots+terms+1,n=sink+1;
        int[][] capacity=new int[n][n];int need=0;
        for(int s=0;s<slots;s++){
            var stack=inventory.getItem(s);capacity[0][s+1]=stack.getCount();
            if(stack.getItem() instanceof cn.blockforge.generated.generatedmod.items.FireSeedItem)continue;
            for(int t=0;t<terms;t++)if(ingredients.get(t).test(stack))capacity[s+1][slots+1+t]=stack.getCount();
        }
        for(int t=0;t<terms;t++){int count=counts.get(t);capacity[slots+1+t][sink]=count;need+=count;}
        int flow=0;while(true){
            int[] parent=new int[n];Arrays.fill(parent,-1);parent[0]=0;var queue=new ArrayDeque<Integer>();queue.add(0);
            while(!queue.isEmpty()&&parent[sink]<0){int a=queue.remove();for(int b=1;b<n;b++)if(parent[b]<0&&capacity[a][b]>0){parent[b]=a;queue.add(b);}}
            if(parent[sink]<0)break;int amount=Integer.MAX_VALUE;
            for(int v=sink;v!=0;v=parent[v])amount=Math.min(amount,capacity[parent[v]][v]);
            for(int v=sink;v!=0;v=parent[v]){capacity[parent[v]][v]-=amount;capacity[v][parent[v]]+=amount;}flow+=amount;
        }
        if(flow!=need)return null;int[] result=new int[slots];for(int s=0;s<slots;s++)result[s]=capacity[s+1][0];return result;
    }
}
