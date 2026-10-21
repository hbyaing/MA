package cn.blockforge.generated.generatedmod.importedcreatures;

import cn.blockforge.generated.generatedmod.ProgressionGate;
import cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem;
import net.minecraft.sounds.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Xiantu's hostile boar behaviour; ordinary damage goes through the target's normal combat events. */
public final class BlackBoarEntity extends Monster {
    public BlackBoarEntity(EntityType<? extends BlackBoarEntity> type,Level level) {super(type,level);xpReward=king()?24:8;}
    public boolean king() {return getType()==ImportedCreatures.BLACK_BOAR_KING.get();}
    public int cultivationRealm() {return king()?ProgressionGate.FOUNDATION:ProgressionGate.QI_CONDENSATION;}
    public static AttributeSupplier.Builder attributes(boolean king) {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,king?160:64).add(Attributes.ATTACK_DAMAGE,king?12:6)
                .add(Attributes.ARMOR,king?12:5).add(Attributes.MOVEMENT_SPEED,king?.27:.25).add(Attributes.FOLLOW_RANGE,24)
                .add(Attributes.KNOCKBACK_RESISTANCE,king?.6:.25);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(2,new MeleeAttackGoal(this,1.15,false));
        goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.8));goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,8));
        goalSelector.addGoal(7,new RandomLookAroundGoal(this));targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    @Override protected SoundEvent getAmbientSound() {return SoundEvents.PIG_AMBIENT;}
    @Override protected SoundEvent getHurtSound(DamageSource source) {return SoundEvents.PIG_HURT;}
    @Override protected SoundEvent getDeathSound() {return SoundEvents.PIG_DEATH;}
    @Override protected void dropCustomDeathLoot(DamageSource source,int looting,boolean recentlyHit) {
        super.dropCustomDeathLoot(source,looting,recentlyHit);
        // Captured, repeatedly revived soul servants must not become a renewable material duplicator.
        if(!recentlyHit||HumanEmperorBannerItem.isServant(this))return;
        var blood=BeastMaterials.create(true,king());blood.setCount((king()?2:1)+random.nextInt(Math.min(3,Math.max(0,looting))+1));
        spawnAtLocation(blood);
        var hide=BeastMaterials.create(false,king());hide.setCount(king()?2:1);spawnAtLocation(hide);
    }
}
