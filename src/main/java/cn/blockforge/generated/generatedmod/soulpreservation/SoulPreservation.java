package cn.blockforge.generated.generatedmod.soulpreservation;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.TribulationManager;
import cn.blockforge.generated.generatedmod.mindscape.MindscapeManager;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import javax.annotation.Nullable;

/** D29: the source escape only; target damage, shields and active soul arts remain authoritative. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class SoulPreservation {
    public static final int COOLDOWN_TICKS=25*60*20;
    public static final String COOLDOWN_KEY="NascentSoulEscapeUntil";
    private record EscapeHit(ServerLevel level,long tick,Vec3 landing) {}
    private static final java.util.Map<ServerPlayer,EscapeHit> ESCAPE_HITS=new java.util.WeakHashMap<>();
    private SoulPreservation() {}

    public static int cooldown(ServerPlayer player) {
        long now=player.server.overworld().getGameTime();
        long until=PlayerCrafts.get(player.server).account(player.getUUID()).getLong(COOLDOWN_KEY);
        return until<=now?0:(int)Math.min(COOLDOWN_TICKS,until-now);
    }

    public static boolean eligible(ServerPlayer player,DamageSource source) {
        var data=GeneratedMod.data(player);
        if(data==null||data.wuxingxiantu$getRealm()<3||!player.isAlive()||player.isCreative()||player.isSpectator()
                ||source.is(DamageTypes.GENERIC_KILL)||source.is(DamageTypes.FELL_OUT_OF_WORLD)
                ||source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                ||player.getY()<player.level().getMinBuildHeight()
                ||TribulationManager.isActive(player)
                ||player.getPersistentData().getBoolean("wuxingxiantu_tribulation_strike")
                ||player.level().dimension()==MindscapeManager.DIMENSION
                ||data.wuxingxiantu$getMindscapeData().hasUUID("session"))return false;
        String active=data.wuxingxiantu$getActiveDivineArt();
        return !"yuan_shen_chu_qiao".equals(active)&&!"yi_hun_ji_po".equals(active)
                &&!player.getPersistentData().hasUUID("wuxingxiantu_soul_body")&&cooldown(player)==0;
    }

    /** LivingDamageEvent is after armor, absorption and the target's ordinary damage modifiers. */
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void damage(LivingDamageEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        // A subsequent damage calculation is another hit, even if it uses the same DamageSource.
        ESCAPE_HITS.remove(player);
        if(event.isCanceled()||!Float.isFinite(event.getAmount())||event.getAmount()<=0
                ||event.getAmount()<player.getHealth()||!eligible(player,event.getSource()))return;
        Vec3 landing=findSafeSpot(player);
        if(landing==null)return;
        Vec3 from=player.position();
        var accounts=PlayerCrafts.get(player.server);
        long now=player.server.overworld().getGameTime();
        accounts.account(player.getUUID()).putLong(COOLDOWN_KEY,
                now>Long.MAX_VALUE-COOLDOWN_TICKS?Long.MAX_VALUE:now+COOLDOWN_TICKS);
        accounts.setDirty();
        // Cancellation is deliberate: later true-damage numeric floors must not reintroduce this hit.
        event.setCanceled(true);
        ESCAPE_HITS.put(player,new EscapeHit(player.serverLevel(),player.serverLevel().getGameTime(),landing));
        player.stopRiding();
        player.teleportTo(landing.x,landing.y,landing.z);
        player.setDeltaMovement(Vec3.ZERO);player.fallDistance=0;player.invulnerableTime=20;
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,160,1,false,false,true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,4,false,false,true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,160,4,false,false,true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,200,4,false,false,true));
        decoy(player.serverLevel(),player,from);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.soul_escape"),false);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.soul_escape_retreat"),true);
    }

    /** Cancel only the outer hurt/attack continuation of this tick's escaped fatal hit. */
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void knockback(LivingKnockBackEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        EscapeHit hit=ESCAPE_HITS.get(player);
        if(hit!=null&&player.serverLevel()==hit.level()&&hit.tick()==hit.level().getGameTime())event.setCanceled(true);
        else if(hit!=null)ESCAPE_HITS.remove(player);
    }

    /** Player.attack may restore its pre-hit velocity after the damage callback returns. */
    @SubscribeEvent public static void settle(TickEvent.ServerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        var iterator=ESCAPE_HITS.entrySet().iterator();
        while(iterator.hasNext()) {
            var entry=iterator.next();var player=entry.getKey();var hit=entry.getValue();
            if(player.server!=event.getServer())continue;
            iterator.remove();
            long age=hit.level().getGameTime()-hit.tick();
            // Network-driven hits can precede the world's time increment of this server tick.
            if(!player.isAlive()||player.isRemoved()||player.serverLevel()!=hit.level()||age<0||age>1)continue;
            if(safeLanding(player,hit.landing()))player.teleportTo(hit.landing().x,hit.landing().y,hit.landing().z);
            player.setDeltaMovement(Vec3.ZERO);player.fallDistance=0;
            player.hurtMarked=true;
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(player));
        }
    }

    @SubscribeEvent public static void death(LivingDeathEvent event) {
        if(event.getEntity() instanceof ServerPlayer player)ESCAPE_HITS.remove(player);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.getEntity() instanceof ServerPlayer player)ESCAPE_HITS.remove(player);
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) {ESCAPE_HITS.clear();}

    @Nullable public static Vec3 findSafeSpot(ServerPlayer player) {
        var level=player.serverLevel();var origin=player.blockPosition();
        // Read only loaded columns: fleeing must never synchronously create exploration chunks.
        for(int attempt=0;attempt<64;attempt++) {
            double angle=level.getRandom().nextDouble()*Math.PI*2;
            double distance=6+level.getRandom().nextDouble()*8;
            int x=origin.getX()+(int)Math.round(Math.cos(angle)*distance);
            int z=origin.getZ()+(int)Math.round(Math.sin(angle)*distance);
            for(int dy=3;dy>=-6;dy--) {
                Vec3 feet=new Vec3(x+.5,origin.getY()+dy,z+.5);
                if(safeLanding(player,feet))return feet;
            }
        }
        for(int attempt=0;attempt<16;attempt++) {
            double angle=level.getRandom().nextDouble()*Math.PI*2;
            double distance=6+level.getRandom().nextDouble()*8;
            int x=origin.getX()+(int)Math.round(Math.cos(angle)*distance);
            int z=origin.getZ()+(int)Math.round(Math.sin(angle)*distance);
            if(!level.hasChunk(x>>4,z>>4))continue;
            Vec3 surface=new Vec3(x+.5,level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z),z+.5);
            if(safeLanding(player,surface))return surface;
        }
        // Source fallback is retained only when the present location itself is safe.
        return safeLanding(player,player.position())?player.position():null;
    }

    public static boolean safeLanding(ServerPlayer player,Vec3 feet) {
        if(!Double.isFinite(feet.x)||!Double.isFinite(feet.y)||!Double.isFinite(feet.z))return false;
        var level=player.serverLevel();var pos=BlockPos.containing(feet);
        AABB box=player.getBoundingBox().move(feet.subtract(player.position()));
        var border=level.getWorldBorder();
        if(box.minY<=level.getMinBuildHeight()||box.maxY>=level.getMaxBuildHeight()
                ||box.minX<border.getMinX()||box.maxX>border.getMaxX()
                ||box.minZ<border.getMinZ()||box.maxZ>border.getMaxZ())return false;
        for(int cx=((int)Math.floor(box.minX))>>4;cx<=((int)Math.floor(box.maxX))>>4;cx++)
            for(int cz=((int)Math.floor(box.minZ))>>4;cz<=((int)Math.floor(box.maxZ))>>4;cz++)
                if(!level.hasChunk(cx,cz))return false;
        if(!level.hasChunkAt(pos.below())||!level.getBlockState(pos).isAir()
                ||!level.getBlockState(pos.above()).isAir()
                ||!level.getFluidState(pos).isEmpty()||!level.getFluidState(pos.above()).isEmpty())return false;
        var ground=level.getBlockState(pos.below());
        if(!ground.isFaceSturdy(level,pos.below(),Direction.UP)||ground.is(BlockTags.FIRE)
                ||ground.is(Blocks.MAGMA_BLOCK)||ground.is(Blocks.CACTUS)
                ||ground.is(Blocks.CAMPFIRE)||ground.is(Blocks.SOUL_CAMPFIRE))return false;
        return level.noCollision(player,box)&&!level.containsAnyLiquid(box);
    }

    private static void decoy(ServerLevel level,ServerPlayer player,Vec3 from) {
        level.sendParticles(ParticleTypes.SOUL,from.x,from.y+1,from.z,40,.4,.8,.4,.02);
        level.sendParticles(ParticleTypes.END_ROD,from.x,from.y+1,from.z,24,.3,.7,.3,.03);
        level.sendParticles(ParticleTypes.CLOUD,from.x,from.y+.8,from.z,16,.3,.4,.3,.02);
        level.playSound(null,BlockPos.containing(from),SoundEvents.ENDERMAN_TELEPORT,SoundSource.PLAYERS,1,1.4F);
        level.playSound(null,BlockPos.containing(from),SoundEvents.SOUL_ESCAPE,SoundSource.PLAYERS,1.2F,.9F);
        for(Mob mob:level.getEntitiesOfClass(Mob.class,new AABB(from,from).inflate(12))) {
            if(mob.getTarget()==player)mob.setTarget(null);
            if(mob.getLastHurtByMob()==player)mob.setLastHurtByMob(null);
        }
    }
}
