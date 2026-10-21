package cn.blockforge.generated.generatedmod.event;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.ModEffects;
import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.ShieldImpactPacket;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.ShieldShatterPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraftforge.event.TickEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class SpiritShieldImpactEvents {
    private record Contact(long tick, int sourceId) {}
    private static final java.util.Map<ServerPlayer,Contact> CONTACTS = new java.util.WeakHashMap<>();
    private record PushHit(long tick,float blocked) {}
    private static final java.util.Map<ServerPlayer,java.util.Map<Integer,PushHit>> PUSH_HITS=new java.util.WeakHashMap<>();
    /** Arrow contact also occurs when vanilla rejects damage (e.g. creative mode). */
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void projectile(ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof EntityHitResult hit && hit.getEntity() instanceof ServerPlayer player && hasShell(player)) {
            Vec3 direction=incomingDirection(player,event.getProjectile(),null);
            if(player.getAbsorptionAmount()<=0){
                shatterDepleted(player,event.getProjectile(),direction);
                // A depleted shell is cosmetic. Let vanilla resolve this contact.
                return;
            }
            emit(player,event.getProjectile(),direction);
            if(event.getProjectile() instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow){
                float damage=(float)Math.max(1,Math.ceil(arrow.getBaseDamage()*arrow.getDeltaMovement().length()));
                if(arrow instanceof net.minecraft.world.entity.projectile.ThrownTrident)damage=8;
                float excess=spend(player,damage);
                event.setCanceled(true);arrow.discard();
                if(excess>0)player.hurt(player.damageSources().arrow(arrow,arrow.getOwner()),excess);
            }
        }
    }
    public static float spend(ServerPlayer player,float damage){
        float blocked=Math.min(player.getAbsorptionAmount(),Math.max(0,damage));
        player.setAbsorptionAmount(player.getAbsorptionAmount()-blocked);
        return Math.max(0,damage-blocked);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void hurt(LivingHurtEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || event.getAmount()<=0
                || !player.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get())) return;
        var direct=event.getSource().getDirectEntity();
        var pushed=direct==null?null:PUSH_HITS.getOrDefault(player,java.util.Map.of()).get(direct.getId());
        if(pushed!=null&&pushed.blocked()>0&&player.level().getGameTime()-pushed.tick()<=1
                &&!event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)){
            // The collision and following melee strike are one interception.
            event.setAmount(spend(player,Math.max(0,event.getAmount()-pushed.blocked())));
            PUSH_HITS.get(player).put(direct.getId(),new PushHit(pushed.tick(),0));
            if(event.getAmount()<=0)event.setCanceled(true);
            return;
        }
        if(player.getAbsorptionAmount()<=0){
            shatterDepleted(player,direct,incomingDirection(player,direct,event.getSource()));
            return;
        }
        emit(player,event.getSource().getDirectEntity(),incomingDirection(player,event.getSource().getDirectEntity(),event.getSource()));
        // Resolve close contact against the finite shield pool before a melee
        // source can bypass vanilla absorption. Excess damage still reaches HP.
        if(event.getSource().getDirectEntity()!=null
                && !event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setAmount(spend(player,event.getAmount()));
            if(event.getAmount()<=0)event.setCanceled(true);
        }
    }
    public static boolean active(ServerPlayer player) {
        return hasShell(player) && player.getAbsorptionAmount()>0;
    }
    private static boolean hasShell(ServerPlayer player) {
        return player.isAlive() && !player.isSpectator() && player.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get());
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        for(var player:event.getServer().getPlayerList().getPlayers()) repel(player);
    }
    /** Collision-aware separation also works when the shield is cast inside a mob. */
    public static void repel(ServerPlayer player) {
        if(!active(player))return;
        double radius=Math.max(1.15,player.getBbHeight()*.64);
        for(var living:player.level().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(radius+1),
                e->e!=player && e.isAlive() && !e.isSpectator() && !(e instanceof net.minecraft.world.entity.decoration.ArmorStand)
                        && !e.isPassengerOfSameVehicle(player))) {
            if(Math.abs(living.getY()+living.getBbHeight()*.5-player.getY()-player.getBbHeight()*.5)>radius+living.getBbHeight()*.5)continue;
            Vec3 delta=living.position().subtract(player.position()).multiply(1,0,1);
            double distance=delta.length(),boundary=radius+living.getBbWidth()*.5;
            if(distance>=boundary)continue;
            Vec3 outward=distance>.0001?delta.scale(1/distance):new Vec3(1,0,0);
            living.move(MoverType.SELF,outward.scale(Math.min(.35,boundary-distance+.02)));
            Vec3 velocity=living.getDeltaMovement();
            double inward=velocity.dot(outward);
            if(inward<0)velocity=velocity.subtract(outward.scale(inward));
            living.setDeltaMovement(velocity.add(outward.scale(.2)));
            living.hurtMarked=true;
            // Blocking a hostile mob's approach is a real shield interception,
            // even though separation prevents its normal melee damage event.
            if(living instanceof net.minecraft.world.entity.Mob mob && mob.getTarget()==player){
                long now=player.level().getGameTime();
                var hits=PUSH_HITS.computeIfAbsent(player,p->new java.util.HashMap<>());
                if(now-hits.getOrDefault(living.getId(),new PushHit(-1000L,0)).tick()>=20){
                    emit(player,living,outward);
                    var attack=mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                    float before=player.getAbsorptionAmount();
                    spend(player,(float)Math.max(1,attack==null?2:attack.getValue()));
                    hits.put(living.getId(),new PushHit(now,before-player.getAbsorptionAmount()));
                    hits.entrySet().removeIf(entry->now-entry.getValue().tick()>100);
                }
            }
        }
    }
    public static Vec3 incomingDirection(ServerPlayer player, Entity direct, DamageSource source) {
        if (direct instanceof net.minecraft.world.entity.projectile.Projectile
                && Double.isFinite(direct.getDeltaMovement().lengthSqr()) && direct.getDeltaMovement().lengthSqr()>.001)
            return direct.getDeltaMovement().scale(-1).normalize();
        Vec3 position = source == null ? direct == null ? null : direct.position() : source.getSourcePosition();
        Vec3 direction = position == null ? Vec3.ZERO : position.subtract(player.position().add(0,player.getBbHeight()*.5,0));
        return Double.isFinite(direction.lengthSqr()) && direction.lengthSqr()>.001 ? direction.normalize() : new Vec3(0,1,0);
    }
    private static void emit(ServerPlayer player, Entity source, Vec3 direction) {
        if (!player.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get()) || player.getAbsorptionAmount()<=0) return;
        long tick=player.level().getGameTime(); int sourceId=source==null?-1:source.getId();
        // One projectile can produce both contact and hurt events on the same server tick.
        if(sameContact(player,source))return;
        CONTACTS.put(player,new Contact(tick,sourceId));
        Network.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->player),new ShieldImpactPacket(player.getId(),direction));
    }
    private static boolean sameContact(ServerPlayer player,Entity source) {
        Contact previous=CONTACTS.get(player);
        return previous!=null && previous.tick()==player.level().getGameTime()
                && previous.sourceId()==(source==null?-1:source.getId());
    }
    /** The exhausting hit leaves a red shell; a later distinct hit breaks it once. */
    private static void shatterDepleted(ServerPlayer player,Entity source,Vec3 direction) {
        if(!hasShell(player)||player.getAbsorptionAmount()>0
                ||source instanceof net.minecraft.world.entity.projectile.Projectile&&sameContact(player,source))return;
        // Send an explicit event: absorption/effect metadata order must not choose the trigger.
        Network.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->player),new ShieldShatterPacket(player.getId(),direction));
        player.removeEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get());
        CONTACTS.remove(player);
        PUSH_HITS.remove(player);
    }
    private SpiritShieldImpactEvents() {}
}
