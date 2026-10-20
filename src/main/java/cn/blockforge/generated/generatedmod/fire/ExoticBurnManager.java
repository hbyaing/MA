package cn.blockforge.generated.generatedmod.fire;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.network.NetworkChannel;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.ExoticBurnPacket;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/** Exotic burn owns its timer and suppresses ordinary burning while active. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class ExoticBurnManager {
    private static final String TAG="WuxingExoticBurn";
    private static final int[] DURATIONS={0,60,80,100,120,160};
    private static final float[] DAMAGE={0,.5F,.65F,.8F,1F,1.2F};
    private static final int[] STACKS={0,1,2,2,3,3};
    private static final AtomicLong SEQUENCE=new AtomicLong();
    private static final java.util.Map<LivingEntity,Long> LAST_SYNC=new java.util.WeakHashMap<>();
    private static final ResourceKey<DamageType> ELEMENTAL=key("exotic_fire");
    private static final ResourceKey<DamageType> CONTACT=key("exotic_fire_contact");
    private static final ResourceKey<DamageType> BURN=key("exotic_fire_burn");
    private static ResourceKey<DamageType> key(String path) {return ResourceKey.create(Registries.DAMAGE_TYPE,new ResourceLocation(GeneratedMod.MOD_ID,path));}
    public static ExoticBurnState state(LivingEntity entity) {
        return ExoticBurnState.load(entity.getPersistentData().getCompound(TAG));
    }
    public static boolean isBurning(LivingEntity entity) {return state(entity)!=null;}
    public static ResourceLocation getFireId(LivingEntity entity) {var s=state(entity);return s==null?null:s.fireId();}
    public static int getRemainingTicks(LivingEntity entity) {var s=state(entity);return s==null?0:s.remainingTicks();}
    public static ServerPlayer owner(LivingEntity entity) {
        var s=state(entity);return s==null||s.sourceUuid()==null||entity.getServer()==null?null:entity.getServer().getPlayerList().getPlayer(s.sourceUuid());
    }
    private static void store(LivingEntity entity,ExoticBurnState state) {entity.getPersistentData().put(TAG,state.save());}
    private static boolean validTarget(LivingEntity entity) {
        return !entity.level().isClientSide&&entity.isAlive()&&!entity.isRemoved()
                &&!(entity instanceof Player p&&(p.isCreative()||p.isSpectator()));
    }
    /** Block/bottle contact retains the previous physical-fire immunity and wetness rules. */
    public static void ignite(LivingEntity entity,ResourceLocation fireId,int duration,Entity source) {
        ignite(entity,fireId,duration,source,1,false,false,false);
    }
    /** Existing enchantment stack and duration tables; domain refreshes pass stack=false. */
    public static void ignite(LivingEntity entity,ResourceLocation fireId,int level,ServerPlayer source,boolean stack) {
        int safe=Math.max(1,Math.min(5,level));
        var old=state(entity);
        if(!stack&&old!=null&&old.elemental()&&old.fireId().equals(fireId)&&Objects.equals(old.sourceUuid(),source.getUUID()))safe=old.enchantmentLevel();
        ignite(entity,fireId,DURATIONS[safe],source,safe,true,stack,false);
    }
    private static void ignite(LivingEntity entity,ResourceLocation id,int ticks,Entity source,int level,boolean elemental,boolean stack,boolean coalesce) {
        if(!validTarget(entity)||id==null||FireCatalog.id(id.toString())==null||ticks<=0)return;
        if(entity instanceof cn.blockforge.generated.generatedmod.SoulBodyEntity body && source!=null
                && body.owner().filter(source.getUUID()::equals).isPresent())return;
        if(!elemental&&(entity.fireImmune()||wet(entity)))return;
        var old=state(entity);var owner=source==null?null:source.getUUID();
        boolean same=old!=null&&old.fireId().equals(id)&&old.elemental()==elemental;
        boolean sameOwner=same&&Objects.equals(owner,old.sourceUuid());
        float intensity=elemental&&sameOwner?Math.min(STACKS[level],old.intensity()+(stack?1:0)):1;
        if(!elemental)ticks=net.minecraft.world.item.enchantment.ProtectionEnchantment.getFireAfterDampener(entity,ticks);
        int duration=Math.min(72000,Math.max(ticks,same?old.remainingTicks():0));
        var next=new ExoticBurnState(id,duration,owner,intensity,sameOwner?old.damageTimer():20,level,elemental);
        store(entity,next);
        entity.clearFire();
        entity.setSharedFlagOnFire(false);
        boolean changed=!sameOwner||old.intensity()!=intensity||old.enchantmentLevel()!=level;
        long now=entity.level().getGameTime();
        // Continuous block contact refreshes every tick, but snapshots are coalesced to twice a second.
        if(!coalesce||changed||now-LAST_SYNC.getOrDefault(entity,Long.MIN_VALUE/2)>=Math.max(1,Math.min(10,duration/2)))sync(entity);
    }
    private static boolean wet(LivingEntity entity) {return entity.isInWaterRainOrBubble()||entity.isInPowderSnow;}
    public static void contact(LivingEntity entity,int tier) {
        var fire=FireCatalog.tier(tier);if(fire==null)return;
        if(entity.fireImmune()||wet(entity)) {
            var previous=state(entity);
            if(previous!=null&&!previous.elemental())clear(entity);
            return;
        }
        ignite(entity,fire.key(),160,null,1,false,false,true);
        var s=state(entity);
        if(s!=null&&!s.elemental()&&s.fireId().equals(fire.key()))damage(entity,s,1,true);
    }
    public static void contact(Entity entity,int tier) {
        if(entity instanceof LivingEntity living){contact(living,tier);return;}
        // Dropped items retain contact damage, without acquiring a vanilla burning timer.
        if(entity.level() instanceof ServerLevel level&&!entity.fireImmune()&&FireCatalog.tier(tier)!=null)
            entity.hurt(new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(CONTACT)),1);
    }
    public static void setRemainingTicks(LivingEntity entity,int ticks) {
        if(entity.level().isClientSide)return;
        var s=state(entity);if(ticks<=0){clear(entity);return;}
        if(s!=null){store(entity,s.withTicks(Math.min(72000,ticks),s.damageTimer()));sync(entity);}
    }
    public static void clear(LivingEntity entity) {
        if(entity.level().isClientSide)return;
        if(entity.getPersistentData().contains(TAG)){entity.getPersistentData().remove(TAG);sync(entity);}
        LAST_SYNC.remove(entity);
    }
    public static void copyState(LivingEntity oldEntity,LivingEntity newEntity) {
        if(newEntity.level().isClientSide)return;
        var s=state(oldEntity);if(s==null)clear(newEntity);else {store(newEntity,s);sync(newEntity);}
    }
    public static void tick(LivingEntity entity) {
        if(entity.level().isClientSide)return;
        var s=state(entity);if(s==null)return;
        if(entity instanceof cn.blockforge.generated.generatedmod.SoulBodyEntity body && s.sourceUuid()!=null
                && body.owner().filter(s.sourceUuid()::equals).isPresent()){clear(entity);entity.clearFire();return;}
        if(!validTarget(entity)||!s.elemental()&&(wet(entity)||entity.fireImmune())){clear(entity);return;}
        entity.clearFire();
        entity.setSharedFlagOnFire(false);
        int timer=s.damageTimer()-1,ticks=s.remainingTicks()-1;
        if(ticks<=0){clear(entity);return;}
        // Publish the next state before hurt: death/phase listeners may clear or replace it.
        store(entity,s.withTicks(Math.max(1,ticks),timer<=0?20:timer));
        if(timer<=0) {
            float amount=s.elemental()?DAMAGE[s.enchantmentLevel()]*s.intensity():1;
            var owner=owner(entity);
            if(s.elemental()&&owner!=null&&owner.getPersistentData().getLong("wuxingxiantu_red_burn_boost")>entity.level().getGameTime())amount*=1.10F;
            damage(entity,s,amount,false);
        }
        if(entity.tickCount%(s.elemental()?100:40)==0&&isBurning(entity))sync(entity);
    }
    private static void damage(LivingEntity entity,ExoticBurnState s,float amount,boolean touching) {
        if(!s.elemental()&&(wet(entity)||entity.fireImmune()||entity.hasEffect(MobEffects.FIRE_RESISTANCE)))return;
        if(!(entity.level() instanceof ServerLevel level))return;
        Entity owner=s.sourceUuid()==null?null:level.getEntity(s.sourceUuid());
        if(owner==null&&s.sourceUuid()!=null)owner=level.getServer().getPlayerList().getPlayer(s.sourceUuid());
        var type=level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(s.elemental()?ELEMENTAL:touching?CONTACT:BURN);
        entity.hurt(new DamageSource(type,owner,owner),amount);
    }
    public static ExoticBurnPacket snapshot(LivingEntity entity,boolean clear) {
        var s=clear?null:state(entity);
        return new ExoticBurnPacket(entity.getId(),entity.getUUID(),entity.level().dimension().location(),
                s==null?"":s.fireId().toString(),s==null?0:s.remainingTicks(),s==null?0:s.intensity(),
                s==null?null:s.sourceUuid(),SEQUENCE.incrementAndGet());
    }
    public static void sync(LivingEntity entity) {
        if(!(entity.level() instanceof ServerLevel))return;
        NetworkChannel.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->entity),snapshot(entity,false));
        LAST_SYNC.put(entity,entity.level().getGameTime());
    }
    private static void send(ServerPlayer player,LivingEntity entity,boolean clear) {
        NetworkChannel.CHANNEL.send(PacketDistributor.PLAYER.with(()->player),snapshot(entity,clear));
    }
    @SubscribeEvent public static void livingTick(LivingEvent.LivingTickEvent event) {tick(event.getEntity());}
    @SubscribeEvent public static void startTracking(PlayerEvent.StartTracking event) {
        if(event.getEntity() instanceof ServerPlayer p&&event.getTarget() instanceof LivingEntity target)send(p,target,false);
    }
    @SubscribeEvent public static void stopTracking(PlayerEvent.StopTracking event) {
        if(event.getEntity() instanceof ServerPlayer p&&event.getTarget() instanceof LivingEntity target)send(p,target,true);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void death(LivingDeathEvent event) {clear(event.getEntity());}
    @SubscribeEvent public static void clone(PlayerEvent.Clone event) {
        // Clone runs before the replacement player's connection is ready; respawn sends the snapshot.
        var previous=event.isWasDeath()?null:state(event.getOriginal());
        if(previous==null)event.getEntity().getPersistentData().remove(TAG);else store(event.getEntity(),previous);
    }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // Explicit policy: preserve independent remaining ticks and publish a destination snapshot.
        if(event.getEntity() instanceof ServerPlayer p)sync(p);
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {if(event.getEntity() instanceof ServerPlayer p)sync(p);}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) {if(event.getEntity() instanceof ServerPlayer p)sync(p);}
    @SubscribeEvent public static void leave(EntityLeaveLevelEvent event) {
        if(!event.getLevel().isClientSide&&event.getEntity() instanceof LivingEntity e)LAST_SYNC.remove(e);
    }
    @SubscribeEvent public static void join(EntityJoinLevelEvent event) {
        if(!event.getLevel().isClientSide&&event.getEntity() instanceof LivingEntity e)migrate(e);
    }
    private static void migrate(LivingEntity entity) {
        var tag=entity.getPersistentData();
        if(!tag.contains(TAG)&&tag.contains("wuxingxiantu_exotic_fire_tier")) {
            var fire=FireCatalog.tier(tag.getInt("wuxingxiantu_exotic_fire_tier"));
            int ticks=(int)Math.min(72000,Math.max(0,tag.getLong("wuxingxiantu_exotic_fire_expires")-entity.level().getGameTime()));
            if(fire!=null&&ticks>0)store(entity,new ExoticBurnState(fire.key(),ticks,
                    tag.hasUUID("wuxingxiantu_exotic_fire_owner")?tag.getUUID("wuxingxiantu_exotic_fire_owner"):null,
                    Math.max(1,Math.min(3,tag.getInt("wuxingxiantu_exotic_fire_stacks"))),20,
                    Math.max(1,Math.min(5,tag.getInt("wuxingxiantu_exotic_fire_level"))),true));
        }
        if(tag.contains("WuxingIgnitionSourceFireId")) {
            // One-time conversion of the old coupled save format; never used by the new timer.
            var fire=FireCatalog.id(tag.getString("WuxingIgnitionSourceFireId"));
            if(!tag.contains(TAG)&&fire!=null)store(entity,new ExoticBurnState(fire.key(),160,null,1,20,1,false));
            entity.clearFire();
        }
        for(String old:new String[]{"wuxingxiantu_exotic_fire_tier","wuxingxiantu_exotic_fire_level","wuxingxiantu_exotic_fire_stacks",
                "wuxingxiantu_exotic_fire_expires","wuxingxiantu_exotic_fire_next_damage","wuxingxiantu_exotic_fire_owner",
                "ExoticBurnSourceFireId","WuxingIgnitionSourceFireId"})tag.remove(old);
    }
    private ExoticBurnManager() {}
}
