package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.TribulationState.Trial;
import cn.blockforge.generated.generatedmod.TribulationState.Phase;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 服务端渡劫状态机：倒计时、落雷、持久结算与观战限制均由服务端决定。 */
public final class TribulationManager {
    public static final int COUNTDOWN_TICKS = 15 * 20;
    private static final int STRIKE_INTERVAL = 10; // legacy save compatibility; new schedule uses TribulationPlan
    private static final int THUNDER_RADIUS = 15;
    private static final int CRATER_RADIUS = 1;
    private static final String PLAYER_TRIAL="wuxingxiantu_active_trial";


    private TribulationManager() {}

    public static boolean isActive(ServerPlayer player) {
        Trial trial=TribulationState.get(player.server).trials.get(player.getUUID());
        return trial!=null&&!trial.settled();
    }

    /** Player NBT is saved with catalyst inventory and capability costs, so it wins after interrupted saves. */
    public static void restore(ServerPlayer player) {
        var state=TribulationState.get(player.server);
        var tag=player.getPersistentData();
        if(tag.contains(PLAYER_TRIAL,net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            var trial=Trial.load(tag.getCompound(PLAYER_TRIAL));
            if(!player.getUUID().equals(trial.playerId()))throw new IllegalStateException("Trial owner mismatch");
            if(!trial.settled())state.trials.put(player.getUUID(),trial);
            else state.trials.remove(player.getUUID());
        } else state.trials.remove(player.getUUID());
        state.setDirty();
    }

    private static void checkpoint(ServerPlayer player,Trial trial) {
        var state=TribulationState.get(player.server);
        if(trial.settled()) {
            state.trials.remove(player.getUUID());
            player.getPersistentData().remove(PLAYER_TRIAL);
        } else player.getPersistentData().put(PLAYER_TRIAL,trial.save());
        state.setDirty();
    }

    public static void dimensionChanged(ServerPlayer player) {
        var trial=TribulationState.get(player.server).trials.get(player.getUUID());
        if(trial!=null&&!trial.dimension.equals(player.level().dimension())) {
            finishFailure(player,trial,false);checkpoint(player,trial);
        }
    }

    public static boolean canStart(net.minecraft.world.entity.player.Player player, CultivationData data) {
        if (data == null || player.isCreative() || player.isSpectator() || data.wuxingxiantu$getRealm() >= 8
                || data.wuxingxiantu$isDaoHeartUnstable() || data.wuxingxiantu$getDaoXin() == 5
                || data.wuxingxiantu$getCultivationStage() < 3
                || data.wuxingxiantu$getCultivation() < CultivationLogic.breakthroughRequirement(data)
                || data.wuxingxiantu$getAura() < CultivationLogic.maxAura(data) * 0.70F
                || CultivationLogic.isInCave(player, data)) return false;
        if (!hasCatalyst(player, data.wuxingxiantu$getRealm())) return false;
        return !(player instanceof ServerPlayer serverPlayer) || !isActive(serverPlayer);
    }

    public static boolean start(ServerPlayer player) {
        return start(player, true);
    }

    public static boolean startFromItem(ServerPlayer player) {
        return start(player, false);
    }

    private static boolean start(ServerPlayer player, boolean consumeCatalyst) {
        CultivationData data = GeneratedMod.data(player);
        if (data == null || player.isCreative() || player.isSpectator()) return false;
        if (data.wuxingxiantu$isDaoHeartUnstable() || data.wuxingxiantu$getDaoXin() == 5) return false;
        if (data.wuxingxiantu$getRealm() >= 8) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.immortal"), true);
            return false;
        }
        if (data.wuxingxiantu$getCultivationStage() < 3) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.tribulation_peak_only"), true);
            return false;
        }
        if (CultivationLogic.isInCave(player, data)) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.tribulation_cave"), true);
            return false;
        }
        if (isActive(player)) return false;
        long requirement = CultivationLogic.breakthroughRequirement(data);
        if (data.wuxingxiantu$getCultivation() < requirement) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.breakthrough_cultivation_need", requirement), true);
            return false;
        }
        if (data.wuxingxiantu$getAura() < CultivationLogic.maxAura(data) * 0.70F) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.tribulation_aura_need",
                    Math.round(CultivationLogic.maxAura(data) * 0.70F)), true);
            return false;
        }
        int realm = data.wuxingxiantu$getRealm();
        if (consumeCatalyst && !consumeCatalyst(player, realm)) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.tribulation_need_catalyst"), true);
            return false;
        }
        float previousAuraCap=CultivationLogic.maxAura(data);
        data.wuxingxiantu$setTribulationCount(data.wuxingxiantu$getTribulationCount() + 1);
        var trial=new Trial(player.getUUID(),player.level().dimension(),player.blockPosition(),realm,
                cn.blockforge.generated.generatedmod.balance.TribulationBalance.strikes(realm),player.getMaxHealth(),previousAuraCap);
        TribulationState.get(player.server).trials.put(player.getUUID(),trial);
        checkpoint(player,trial);
        cn.blockforge.generated.generatedmod.alchemy.MedicineProgress.beginTrial(player);
        cn.blockforge.generated.generatedmod.tribulation.TribulationMessages.announce(player,realm);
        maintainWeather(player.server);
        GeneratedMod.sync(player);
        return true;
    }

    public static int countdown(ServerPlayer player) {
        Trial trial=TribulationState.get(player.server).trials.get(player.getUUID());
        return trial==null?0:(trial.countdownTicks+19)/20;
    }

    public static void tick(MinecraftServer server) {
        tickCraters(server);
        maintainWeather(server);
        // Death callbacks can settle a trial during hurt(); iterate a snapshot, not the live map.
        for(Trial trial:new ArrayList<>(TribulationState.get(server).trials.values())) {
            ServerPlayer player=server.getPlayerList().getPlayer(trial.playerId());
            if(player==null)continue; // Offline time never consumes countdown or strike delay.
            try {
                if(trial.settled())continue;
                if(!player.isAlive()||trial.phase==Phase.FAILURE) {finishFailure(player,trial,true);continue;}
                if(!trial.dimension.equals(player.level().dimension())) {finishFailure(player,trial,false);continue;}
                trial.cloudAgeTicks++;
                if(player.level().getGameTime()%20==0){
                    double cloudBottom=Math.max(player.level().dimension()==net.minecraft.world.level.Level.OVERWORLD?192:player.getY()+70,player.getY()+70);
                    visual(player,trial,6,trial.cloudAgeTicks,(int)(player.level().getGameTime()%100000000),new net.minecraft.world.phys.Vec3(player.getX(),cloudBottom,player.getZ()),player.position());
                }
                skyWarning(player,trial);
                if(trial.phase==Phase.SUCCESS) {
                    if(trial.finalTicks>0){tickFinalField(player.serverLevel(),player,trial);if(!player.isAlive())finishFailure(player,trial,true);continue;}
                    if(trial.strikeDelay>0){trial.strikeDelay--;continue;}finishSuccess(player,trial);continue;
                }
                var level=player.serverLevel();
                if(trial.countdownTicks>0) {
                    trial.countdownTicks--;
                    if(trial.countdownTicks%20==0) {
                        player.displayClientMessage(cn.blockforge.generated.generatedmod.tribulation.TribulationMessages.countdown(trial.realm,countdown(player)),true);
                        level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                                player.getX(),player.getY()+1,player.getZ(),10,1,.5,1,.08);
                    }
                    if(trial.countdownTicks==0)trial.phase=Phase.STRIKING;
                    applySpectatorFear(server,trial,player);continue;
                }
                trial.phase=Phase.STRIKING;
                if(trial.strikeDelay>0) {trial.strikeDelay--;applySpectatorFear(server,trial,player);continue;}
                int remaining=trial.totalStrikes()-trial.strikesDone;
                boolean pillar=remaining==1;
                int amount=pillar?1:cn.blockforge.generated.generatedmod.tribulation.TribulationPlan.count(trial.realm,trial.strikesDone);
                if(amount>=remaining)amount=remaining-1;
                if(pillar)amount=1;
                strikeWave(level,player,trial,amount,pillar);
                if(!player.isAlive()) {finishFailure(player,trial,true);continue;}
                if(trial.strikesDone>=trial.totalStrikes()) {trial.phase=Phase.SUCCESS;trial.strikeDelay=0;}
                else if(trial.totalStrikes()-trial.strikesDone==1)trial.strikeDelay=cn.blockforge.generated.generatedmod.tribulation.TribulationPlan.DURATION[trial.realm]+24;
                else trial.strikeDelay=cn.blockforge.generated.generatedmod.tribulation.TribulationPlan.interval(trial.realm,trial.strikesDone)-1+Math.floorMod(cn.blockforge.generated.generatedmod.tribulation.TribulationPlan.seed(trial.visualSeed,trial.strikesDone),5)-2;
                applySpectatorFear(server,trial,player);
            } finally {checkpoint(player,trial);}
        }
        maintainWeather(server);
    }

    private static void strikeWave(ServerLevel level,ServerPlayer player,Trial trial,int amount,boolean pillar) {
        int first=trial.strikesDone+1;float damage=0;
        for(int i=0;i<amount;i++)damage+=cn.blockforge.generated.generatedmod.tribulation.TribulationPlan.damage(trial.realm,first+i);
        trial.strikesDone+=amount;
        var ground=impactGround(player);var end=ground==null?player.position():ground;double cloud=level.dimension()==net.minecraft.world.level.Level.OVERWORLD?193:end.y+48;
        cloud=Math.max(cloud,end.y+24);
        visual(player,trial,pillar?2:0,amount,first,new net.minecraft.world.phys.Vec3(end.x,cloud,end.z),end,ground!=null);
        if(pillar){trial.finalTicks=30;trial.finalGround=ground==null?null:BlockPos.containing(ground);tickFinalField(level,player,trial);}
        else if(!trial.harmless){
            hurtTrial(level,player,trial,damage,ParalysisControl.duration(trial.realm,(trial.strikesDone-1.0)/Math.max(1,trial.totalStrikes()-1)));
            if(ground!=null)destroyStrikeBlocks(level,BlockPos.containing(ground),trial);
        }
        level.playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER,net.minecraft.sounds.SoundSource.WEATHER,2,1);
        if(ground!=null)level.playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_IMPACT,net.minecraft.sounds.SoundSource.WEATHER,1,1);
        player.displayClientMessage(Component.literal((pillar?"终劫·集束天雷 ":"天雷 ")+trial.strikesDone+"/"+trial.totalStrikes()),true);
    }
    private static void hurtTrial(ServerLevel level,ServerPlayer player,Trial trial,float damage,int paralysis){
        var protection=player.getEffect(ModEffects.PILL_TRIBULATION.get());
        if(protection!=null)damage*=1-.1F*Math.min(4,protection.getAmplifier()+1);
        player.getPersistentData().putBoolean("wuxingxiantu_tribulation_strike",true);
        try{player.setAbsorptionAmount(0);player.invulnerableTime=0;if(player.hurt(level.damageSources().lightningBolt(),damage))ParalysisControl.apply(player,paralysis);}
        finally{player.getPersistentData().remove("wuxingxiantu_tribulation_strike");}
    }
    private static void tickFinalField(ServerLevel level,ServerPlayer player,Trial trial){
        int elapsed=30-trial.finalTicks;
        if(!trial.harmless&&elapsed%5==0){
            hurtTrial(level,player,trial,cn.blockforge.generated.generatedmod.tribulation.TribulationPlan.damage(trial.realm,trial.totalStrikes())/6f,
                    Math.max(trial.finalTicks,ParalysisControl.duration(trial.realm,1)));
            // A field that actually struck ground remains anchored to that surface while it excavates.
            if(trial.finalGround!=null)destroyStrikeBlocks(level,trial.finalGround,trial);
        }
        trial.finalTicks--;
    }
    private static void destroyStrikeBlocks(ServerLevel level,BlockPos center,Trial trial) {
        int realm=trial.realm;double scale=cn.blockforge.generated.generatedmod.tribulation.TribulationPlan.impactScale(realm,trial.strikesDone);
        double radius=cn.blockforge.generated.generatedmod.tribulation.TribulationPlan.RADIUS[realm]*scale;
        int cap=Math.max(1,(int)Math.ceil(cn.blockforge.generated.generatedmod.tribulation.TribulationPlan.BLOCKS[realm]*scale)),depth=Math.max(1,(int)Math.ceil(cn.blockforge.generated.generatedmod.tribulation.TribulationPlan.DEPTH[realm]*scale));
        var candidates=new ArrayList<BlockPos>();int bound=(int)Math.ceil(radius);
        for(BlockPos point:BlockPos.betweenClosed(center.offset(-bound,-depth,-bound),center.offset(bound,0,bound))) {
            double dx=point.getX()-center.getX(),dz=point.getZ()-center.getZ(),distance=dx*dx+dz*dz;
            int localDepth=Math.max(1,(int)Math.ceil(depth*(1-.65*distance/(radius*radius))));
            if(distance<=radius*radius&&point.getY()>=center.getY()-localDepth)candidates.add(point.immutable());
        }
        // Excavate central columns first: the budget cannot erase only the shallow rim.
        candidates.sort(java.util.Comparator.<BlockPos>comparingDouble(q->{double x=q.getX()-center.getX(),z=q.getZ()-center.getZ();return x*x+z*z;}).thenComparing(java.util.Comparator.<BlockPos>comparingInt(q->q.getY()).reversed()));int changed=0;
        var journal=TribulationState.get(level.getServer());
        var protectedTag=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,new net.minecraft.resources.ResourceLocation(GeneratedMod.MOD_ID,"tribulation_protected"));
        for(var point:candidates){if(changed>=cap)break;if(!level.hasChunkAt(point))continue;var block=level.getBlockState(point);float hardness=block.getDestroySpeed(level,point);
            if(block.isAir()||!block.getFluidState().isEmpty()||block.hasBlockEntity()||hardness<0||hardness>cn.blockforge.generated.generatedmod.tribulation.TribulationPlan.impactHardness(realm,trial.strikesDone)||block.is(protectedTag))continue;
            var owner=level.getServer().getPlayerList().getPlayer(trial.playerId());if(owner==null)continue;
            if(ParalysisControl.terrainBreak(level,point,block,owner))continue;
            if(journal.recordCrater(level.dimension(),point,block,level.getServer().overworld().getGameTime()+12000)){level.setBlock(point,Blocks.AIR.defaultBlockState(),3);changed++;}
        }
        igniteStrikeRing(level,center,trial,radius,depth,protectedTag);
    }
    private static void igniteStrikeRing(ServerLevel level,BlockPos center,Trial trial,double radius,int depth,net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> protectedTag){
        var owner=level.getServer().getPlayerList().getPlayer(trial.playerId());if(owner==null)return;
        var random=new java.util.Random(cn.blockforge.generated.generatedmod.tribulation.TribulationPlan.seed(trial.visualSeed,trial.strikesDone));
        int count=cn.blockforge.generated.generatedmod.tribulation.TribulationPlan.FIRE[trial.realm];
        for(int i=0;i<count;i++){
            double angle=(i+random.nextDouble()*.6)*Math.PI*2/count,distance=radius*(.7+random.nextDouble()*.45);
            var column=center.offset((int)Math.round(Math.cos(angle)*distance),0,(int)Math.round(Math.sin(angle)*distance));
            for(int y=2;y>=-depth-1;y--){var pos=column.above(y);if(!level.hasChunkAt(pos))break;
                var support=level.getBlockState(pos.below());
                if(!level.isEmptyBlock(pos)||support.is(protectedTag)||support.hasBlockEntity()||support.getDestroySpeed(level,pos.below())<0||!support.getFluidState().isEmpty())continue;
                var fire=net.minecraft.world.level.block.BaseFireBlock.getState(level,pos);
                if(!fire.canSurvive(level,pos))continue;
                var snapshot=net.minecraftforge.common.util.BlockSnapshot.create(level.dimension(),level,pos);
                level.setBlock(pos,fire,3);
                if(net.minecraftforge.event.ForgeEventFactory.onBlockPlace(owner,snapshot,net.minecraft.core.Direction.UP))snapshot.restore(true,false);
                break;
            }
        }
    }
    private static void visual(ServerPlayer player,Trial trial,int kind,int count,int index,net.minecraft.world.phys.Vec3 start,net.minecraft.world.phys.Vec3 end){
        visual(player,trial,kind,count,index,start,end,false);
    }
    private static void visual(ServerPlayer player,Trial trial,int kind,int count,int index,net.minecraft.world.phys.Vec3 start,net.minecraft.world.phys.Vec3 end,boolean grounded){
        long id=trial.visualSeed^((long)index<<8)^kind;var packet=new cn.blockforge.generated.generatedmod.tribulation.TribulationVisualPacket(player.getUUID(),player.level().dimension().location().toString(),id,trial.realm,kind,count,cn.blockforge.generated.generatedmod.tribulation.TribulationPlan.seed(trial.visualSeed,kind==6?60000:index+kind*10000),player.level().getGameTime(),start,end,grounded);
        int range=kind==6||kind==3?512+trial.realm*96:256;
        for(var watcher:player.serverLevel().players())if(watcher.distanceToSqr(player)<(double)range*range)Network.sendToPlayer(watcher,packet);
    }
    /** Resolve actual support, independent of creative flight/riding/onGround flags. */
    public static net.minecraft.world.phys.Vec3 impactGround(ServerPlayer player){
        var b=player.getBoundingBox();double feet=player.getY(),top=Double.NEGATIVE_INFINITY;
        var probe=new net.minecraft.world.phys.AABB(b.minX+.02,feet-.12,b.minZ+.02,b.maxX-.02,feet+.03,b.maxZ-.02);
        for(var shape:player.level().getBlockCollisions(player,probe))for(var box:shape.toAabbs())
            if(box.maxY>=feet-.12&&box.maxY<=feet+.03)top=Math.max(top,box.maxY);
        return Double.isFinite(top)?new net.minecraft.world.phys.Vec3(player.getX(),top,player.getZ()):null;
    }
    private static void skyWarning(ServerPlayer player,Trial trial){
        if(trial.countdownTicks>COUNTDOWN_TICKS-60){if(trial.skyDelay>0)trial.skyDelay--;return;}if(trial.skyDelay-->0)return;trial.skyDelay=35;int t=(int)(player.level().getGameTime()%100000000);
        int count=trial.countdownTicks>200?1:trial.countdownTicks>100?2:3;double y=Math.max(player.level().dimension()==net.minecraft.world.level.Level.OVERWORLD?191:player.getY()+48,player.getY()+24);
        var random=new java.util.Random(trial.visualSeed+t);
        for(int i=0;i<count;i++){double a=random.nextDouble()*Math.PI*2,d=40+random.nextDouble()*80;var start=new net.minecraft.world.phys.Vec3(player.getX()+Math.cos(a)*d,y+random.nextDouble()*3,player.getZ()+Math.sin(a)*d);var end=start.add(Math.cos(a+1.7)*(60+random.nextDouble()*70),random.nextDouble()*3-1.5,Math.sin(a+1.7)*80);visual(player,trial,1,1,-t*8-i,start,end);}
        player.serverLevel().playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER,net.minecraft.sounds.SoundSource.WEATHER,.6F,1);
    }
    private record StormStart(float rain,float thunder) {}
    private static final Map<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>,StormStart> STORM_START=new HashMap<>();
    private static final Map<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>,Float> STORM_SENT=new HashMap<>();
    static float stormProgress(Trial trial){return Math.max(0,Math.min(1,(COUNTDOWN_TICKS-trial.countdownTicks)/60f));}
    private static void maintainWeather(MinecraftServer server){
        var active=new HashMap<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>,Float>();
        for(var trial:TribulationState.get(server).trials.values())if(!trial.settled())active.merge(trial.dimension,stormProgress(trial),Math::max);
        for(var entry:active.entrySet()){var dimension=entry.getKey();var level=server.getLevel(dimension);if(level==null)continue;
            boolean fresh=!STORM_START.containsKey(dimension);var start=STORM_START.computeIfAbsent(dimension,k->new StormStart(level.getRainLevel(1),level.getThunderLevel(1)));
            float progress=entry.getValue(),rain=start.rain()+(1-start.rain())*progress,thunder=start.thunder()+(1-start.thunder())*progress;
            level.setWeatherParameters(0,200,true,true);level.setRainLevel(rain);level.setThunderLevel(thunder);
            Float previous=STORM_SENT.put(dimension,progress);if(fresh||previous==null||Float.compare(previous,progress)!=0)syncWeather(level,true,rain,thunder,fresh);
        }
        for(var dimension:new ArrayList<>(STORM_START.keySet()))if(!active.containsKey(dimension)){var level=server.getLevel(dimension);if(level!=null){level.setWeatherParameters(12000,0,false,false);level.setRainLevel(0);level.setThunderLevel(0);syncWeather(level,false,0,0,true);}STORM_START.remove(dimension);STORM_SENT.remove(dimension);}
    }
    private static void syncWeather(ServerLevel level,boolean storm,float rain,float thunder,boolean flag){
        for(var player:level.players()){
            if(flag)player.connection.send(new net.minecraft.network.protocol.game.ClientboundGameEventPacket(storm?net.minecraft.network.protocol.game.ClientboundGameEventPacket.START_RAINING:net.minecraft.network.protocol.game.ClientboundGameEventPacket.STOP_RAINING,0));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundGameEventPacket(net.minecraft.network.protocol.game.ClientboundGameEventPacket.RAIN_LEVEL_CHANGE,rain));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundGameEventPacket(net.minecraft.network.protocol.game.ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE,thunder));
        }
    }
    public static boolean startTest(ServerPlayer player,int realm,boolean harmful){
        if(isActive(player))return false;realm=Math.max(0,Math.min(7,realm));var data=GeneratedMod.data(player);
        var trial=new Trial(player.getUUID(),player.level().dimension(),player.blockPosition(),realm,18+realm*9,player.getMaxHealth(),data==null?0:CultivationLogic.maxAura(data));trial.testing=true;trial.harmless=!harmful;
        TribulationState.get(player.server).trials.put(player.getUUID(),trial);checkpoint(player,trial);cn.blockforge.generated.generatedmod.tribulation.TribulationMessages.announce(player,realm);
        player.displayClientMessage(Component.literal(harmful?"【真实雷劫】会扣血并破坏落点方块；创造模式保留原版免伤。":"【雷劫预览】仅演出，不扣血、不破坏方块。").withStyle(harmful?ChatFormatting.RED:ChatFormatting.AQUA),false);
        maintainWeather(player.server);return true;
    }
    public static void stopTest(ServerPlayer player){
        var state=TribulationState.get(player.server);var trial=state.trials.get(player.getUUID());if(trial==null||!trial.testing)return;
        trial.settled(true);visual(player,trial,3,0,Integer.MAX_VALUE,player.position(),player.position());checkpoint(player,trial);maintainWeather(player.server);
    }

    private static void finishSuccess(ServerPlayer player, Trial trial) {
        if(trial.testing){trial.settled(true);visual(player,trial,3,0,Integer.MAX_VALUE,player.position(),player.position());player.displayClientMessage(Component.literal("雷劫测试结束"),false);return;}
        CultivationData data = GeneratedMod.data(player);
        if (data == null || trial.settled()) return;
        trial.settled(true);visual(player,trial,3,0,Integer.MAX_VALUE,player.position(),player.position());
        if (data.wuxingxiantu$getCultivationStage() < 3) data.wuxingxiantu$setCultivationStage(data.wuxingxiantu$getCultivationStage() + 1);
        else {
            int nextRealm = trial.realm() + 1;
            data.wuxingxiantu$setRealm(nextRealm);
            // 只有在下界完成大乘到地仙的突破，才记录为鬼仙；进入下界本身不会改变身份。
            data.wuxingxiantu$setGhostImmortal(nextRealm == 8 && trial.dimension.equals(net.minecraft.world.level.Level.NETHER));
            data.wuxingxiantu$setCultivationStage(0);
        }
        data.wuxingxiantu$setCultivation(0L);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.breakthrough_cultivation_reset"),false);
        cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.markUnstable(player);
        float previousAuraCap = trial.previousAuraCap;
        float remainingAura = Math.max(0.0F, data.wuxingxiantu$getAura() - previousAuraCap * 0.50F);
        data.wuxingxiantu$setAura(remainingAura);
        data.wuxingxiantu$setMeridianStatus(0);
        data.wuxingxiantu$setTribulationFailCount(0);
        // A successful breakthrough immediately creates the unified heart-demon invasion state.
        CultivationLogic.applyHealthBonus(player, data);
        player.setHealth(player.getMaxHealth());
        if (player.getRandom().nextInt(100) < 12) {
            player.spawnAtLocation(new ItemStack(WuXingItems.GUANDING_MANUAL.get()));
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.guanding_drop")
                    .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
        player.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                "message.wuxingxiantu.tribulation_success_announce", player.getName(), CultivationLogic.realmStageName(player, data)), false);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.tribulation_success",
                CultivationLogic.realmStageName(player, data)).withStyle(ChatFormatting.BLUE, ChatFormatting.LIGHT_PURPLE), false);
        GeneratedMod.sync(player);
    }

    private static void finishFailure(ServerPlayer player, Trial trial, boolean killed) {
        if (trial.settled()) return;
        if(trial.testing){trial.settled(true);visual(player,trial,3,0,Integer.MAX_VALUE,player.position(),player.position());return;}
        CultivationData data = GeneratedMod.data(player);
        if(data==null){trial.phase=Phase.FAILURE;return;}
        trial.settled(true);visual(player,trial,3,0,Integer.MAX_VALUE,player.position(),player.position());
        if (data != null) {
            data.wuxingxiantu$setTribulationFailCount(data.wuxingxiantu$getTribulationFailCount() + 1);
            data.wuxingxiantu$setDaoXin(5);
            GeneratedMod.sync(player);
        }
        player.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                "message.wuxingxiantu.tribulation_failure_announce", player.getName()), false);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.tribulation_failure")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BLACK), false);
        awardFailureAdvancement(player);
        if (!killed && player.isAlive()) player.hurt(player.damageSources().genericKill(), player.getMaxHealth() + 1.0F);
    }

    private static void awardFailureAdvancement(ServerPlayer player) {
        net.minecraft.advancements.Advancement advancement = player.getServer().getAdvancements().getAdvancement(
                new net.minecraft.resources.ResourceLocation(GeneratedMod.MOD_ID, "tribulation_failure"));
        if (advancement != null) player.getAdvancements().award(advancement, "failure");
    }

    private static void applySpectatorFear(MinecraftServer server, Trial trial, ServerPlayer owner) {
        for (ServerPlayer watcher : server.getPlayerList().getPlayers()) {
            if (watcher == owner || watcher.level() != owner.level()
                    || watcher.distanceToSqr(owner) > THUNDER_RADIUS * THUNDER_RADIUS) continue;
            watcher.addEffect(new MobEffectInstance(ModEffects.TRIBULATION_FEAR.get(), 40, 0, true, false, true));
            watcher.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, true, false, true));
            watcher.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, true, false, true));
        }
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if(event.isCanceled()||!(event.getEntity() instanceof ServerPlayer player))return;
        Trial trial=TribulationState.get(player.server).trials.get(player.getUUID());
        if(trial==null)return;
        finishFailure(player,trial,true);checkpoint(player,trial);
    }

    private static void tickCraters(MinecraftServer server) {
        var state=TribulationState.get(server);long now=server.overworld().getGameTime();
        for(int budget=0;budget<256&&!state.due.isEmpty()&&state.due.peek().restoreAt()<=now;budget++) {
            var block=state.due.poll();var key=block.key();if(state.craters.get(key)!=block)continue;var level=server.getLevel(key.dimension());
            if(level==null||!level.hasChunkAt(key.pos())) {state.retry(block,now+200);continue;}
            if(level.getBlockState(key.pos()).isAir()||level.getBlockState(key.pos()).is(Blocks.FIRE)||level.getBlockState(key.pos()).is(Blocks.SOUL_FIRE)||level.getBlockState(key.pos()).is(Blocks.BLACK_CONCRETE))level.setBlock(key.pos(),block.state(),3);
            state.craters.remove(key);state.setDirty();
        }
    }

    /** The immortal core remains reserved; every required catalyst must have a survival drop. */
    public static int requiredCoreIndex(int realm) {
        return Math.max(0, Math.min(WuXingItems.INNER_CORES.length - 2, realm));
    }

    private static boolean hasCatalyst(net.minecraft.world.entity.player.Player player, int realm) {
        int required = requiredCoreIndex(realm);
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(WuXingItems.INNER_CORES[required].get())) return true;
        }
        return false;
    }

    private static boolean consumeCatalyst(ServerPlayer player, int realm) {
        int required = requiredCoreIndex(realm);
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(WuXingItems.INNER_CORES[required].get())) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static final class EntityTypeHolder {
        private static LightningBolt createLightning(ServerLevel level, BlockPos pos) {
            LightningBolt bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) bolt.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            return bolt;
        }
    }

}
