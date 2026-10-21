package cn.blockforge.generated.generatedmod.entity;

import cn.blockforge.generated.generatedmod.WuXingItems;
import cn.blockforge.generated.generatedmod.plants.SpiritPlantBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import java.util.*;

/** Low-tier arthropod: timed plant feeding, shared per-plant protection and local retaliation. */
public final class SpiritDevourerEntity extends Monster {
    private static final EntityDataAccessor<Integer> FULL=SynchedEntityData.defineId(SpiritDevourerEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FEED=SynchedEntityData.defineId(SpiritDevourerEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK=SynchedEntityData.defineId(SpiritDevourerEntity.class,EntityDataSerializers.INT);
    private static final UUID SPEED=UUID.fromString("bb174311-a385-4486-a2ac-e5e229a40213");
    private static final Map<Level,Map<BlockPos,Long>> COOLDOWNS=new WeakHashMap<>();
    private BlockPos plant;
    private int scanAt,dodgeAt,repathAt,combatTicks;
    public SpiritDevourerEntity(EntityType<? extends SpiritDevourerEntity> type,Level level){super(type,level);xpReward=3;}
    public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,12).add(Attributes.ATTACK_DAMAGE,3)
            .add(Attributes.ARMOR,2).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.FOLLOW_RANGE,16).add(Attributes.KNOCKBACK_RESISTANCE,.1);}
    @Override public MobType getMobType(){return MobType.ARTHROPOD;}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(FULL,0);entityData.define(FEED,0);entityData.define(ATTACK,0);}
    public boolean full(){return entityData.get(FULL)>0;}
    public int feeding(){return entityData.get(FEED);}
    public int attacking(){return entityData.get(ATTACK);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(1,new MeleeAttackGoal(this,1.1,false){
            @Override protected int getAttackInterval(){return 24;}
            @Override protected double getAttackReachSqr(LivingEntity target){return 1.8;}
        });
        goalSelector.addGoal(2,new FeedGoal());
        goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.7));
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,4));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<Player>(this,Player.class,20,true,false,p->distanceToSqr(p)<9));
    }
    @Override public boolean doHurtTarget(Entity target){entityData.set(ATTACK,10);return super.doHurtTarget(target);}
    @Override public void tick(){
        super.tick();if(level().isClientSide)return;
        if(getTarget()!=null){if(++combatTicks>200 && distanceToSqr(getTarget())>256){setTarget(null);combatTicks=0;}}else combatTicks=0;
        if(entityData.get(ATTACK)>0)entityData.set(ATTACK,entityData.get(ATTACK)-1);
        if(full())entityData.set(FULL,entityData.get(FULL)-1);
        var speed=getAttribute(Attributes.MOVEMENT_SPEED);
        if(full()&&speed.getModifier(SPEED)==null)speed.addTransientModifier(new AttributeModifier(SPEED,"Sated spirit",.1,AttributeModifier.Operation.MULTIPLY_TOTAL));
        if(!full())speed.removeModifier(SPEED);
    }
    @Override public boolean hurt(DamageSource source,float amount){
        boolean result=super.hurt(source,amount);
        if(result&&!level().isClientSide&&source.getEntity() instanceof Player p){
            for(var insect:level().getEntitiesOfClass(SpiritDevourerEntity.class,getBoundingBox().inflate(8),e->e.isAlive()))insect.setTarget(p);
            if(tickCount>=dodgeAt&&random.nextFloat()<.15F){
                double yaw=Math.toRadians(getYRot()),sign=random.nextBoolean()?1:-1;
                var delta=new net.minecraft.world.phys.Vec3(Math.cos(yaw)*.4*sign,0,Math.sin(yaw)*.4*sign);
                var box=getBoundingBox().move(delta);
                var foot=BlockPos.containing(position().add(delta).add(0,-.2,0));
                if(level().noCollision(this,box)&&level().getBlockState(foot).isSolid()&&!level().containsAnyLiquid(box)){
                    setDeltaMovement(getDeltaMovement().add(delta.scale(.5)));dodgeAt=tickCount+40;
                }
            }
        }
        return result;
    }
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putInt("SatedTicks",entityData.get(FULL));}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);entityData.set(FULL,Math.max(0,Math.min(900,tag.getInt("SatedTicks"))));}
    @Override protected void dropCustomDeathLoot(DamageSource source,int looting,boolean hit){
        super.dropCustomDeathLoot(source,looting,hit);
        if(random.nextFloat()<(full()?.40F:.15F))spawnAtLocation(WuXingItems.SPIRIT_DEVOURER_SAC.get());
        if(full()&&random.nextFloat()<.15F)spawnAtLocation(WuXingItems.SPIRIT_LIQUID_BOTTLE.get());
    }
    public boolean feedPlant(BlockPos pos){
        var state=level().getBlockState(pos);
        if(!(state.getBlock() instanceof SpiritPlantBlock)||state.getValue(SpiritPlantBlock.AGE)<=0)return false;
        var map=COOLDOWNS.computeIfAbsent(level(),l->new HashMap<>());long now=level().getGameTime();
        map.entrySet().removeIf(e->e.getValue()<=now);
        if(map.containsKey(pos))return false;
        map.put(pos.immutable(),now+600);
        if(net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(level(),this))
            level().setBlock(pos,state.setValue(SpiritPlantBlock.AGE,state.getValue(SpiritPlantBlock.AGE)-1),3);
        entityData.set(FULL,900);return true;
    }
    private boolean valid(BlockPos pos){return pos!=null&&level().hasChunkAt(pos)&&level().getBlockState(pos).getBlock() instanceof SpiritPlantBlock
            &&level().getBlockState(pos).getValue(SpiritPlantBlock.AGE)>0
            &&COOLDOWNS.getOrDefault(level(),Map.of()).getOrDefault(pos,0L)<=level().getGameTime();}
    private final class FeedGoal extends Goal {
        FeedGoal(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){
            if(getTarget()!=null||full())return false;
            if(valid(plant))return true;
            if(tickCount<scanAt)return false;scanAt=tickCount+40;plant=null;
            // 512 sampled positions per attempt, bounded and staggered across insects.
            double best=-Double.MAX_VALUE;
            for(int i=0;i<512;i++){
                var pos=blockPosition().offset(random.nextInt(23)-11,random.nextInt(5)-2,random.nextInt(23)-11);
                if(!valid(pos))continue;
                var state=level().getBlockState(pos);var block=(SpiritPlantBlock)state.getBlock();
                double score=(block.isMature(state)?1000:0)-pos.distSqr(blockPosition());
                if(score>best){best=score;plant=pos.immutable();}
            }
            return plant!=null;
        }
        @Override public boolean canContinueToUse(){return getTarget()==null&&!full()&&valid(plant)&&tickCount<scanAt+240;}
        @Override public void start(){entityData.set(FEED,0);repathAt=0;}
        @Override public void stop(){entityData.set(FEED,0);plant=null;getNavigation().stop();scanAt=tickCount+20+random.nextInt(20);}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){
            if(plant==null)return;
            getLookControl().setLookAt(plant.getX()+.5,plant.getY()+.3,plant.getZ()+.5);
            if(position().distanceToSqr(net.minecraft.world.phys.Vec3.atBottomCenterOf(plant))<2.25){
                getNavigation().stop();entityData.set(FEED,feeding()+1);
                if(feeding()%12==0&&level() instanceof ServerLevel server)server.sendParticles(cn.blockforge.generated.generatedmod.ModParticles.HEART_MOTE.get(),plant.getX()+.5,plant.getY()+.4,plant.getZ()+.5,0,getX()-plant.getX()-.5,getY()-plant.getY()-.2,getZ()-plant.getZ()-.5,.12);
                if(feeding()>=60){feedPlant(plant);stop();}
            }else if(tickCount>=repathAt){getNavigation().moveTo(plant.getX()+.5,plant.getY(),plant.getZ()+.5,.85);repathAt=tickCount+20;}
        }
    }
}
