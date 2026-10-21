package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.yujian.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu")
@PrefixGameTestTemplate(false)
public final class YujianGameTests {
    private static FakePlayer player(GameTestHelper h){
        var p=new FakePlayer(h.getLevel(),new GameProfile(java.util.UUID.randomUUID(),"SwordTest"));
        var pos=h.absolutePos(new BlockPos(3,2,1));p.setPos(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5);h.getLevel().addNewPlayer(p);
        var data=GeneratedMod.data(p);data.wuxingxiantu$setRealm(5);data.wuxingxiantu$learnArt(YujianController.FLIGHT);data.wuxingxiantu$setAura(10000);
        return p;
    }
    private static ItemStack sword(FakePlayer p) {
        var stack=new ItemStack(WuXingItems.TIANGANG_SWORD.get());
        var n=NurtureData.read(stack);n.nurtureStage=6;n.knowledgeMigrated=true;
        for(String id:java.util.List.of("art_sword_sweep","art_sword_guard","art_sword_array")) {
            GeneratedMod.data(p).wuxingxiantu$learnSwordTechnique(id);
            n.equippedTechniques.add(id);
        }
        n.write(stack,SwordProfile.of(stack));return stack;
    }
    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void flightRestoresAndDoesNotDuplicateSword(GameTestHelper h){
        var p=player(h);var sword=sword(p);p.getInventory().setItem(0,sword);
        boolean mayfly=p.getAbilities().mayfly;float speed=p.getAbilities().getFlyingSpeed();
        try{
            float beforeMana=GeneratedMod.data(p).wuxingxiantu$getAura();
            float summonCost=ArtCatalog.finalManaCost(YujianController.FLIGHT,
                    cn.blockforge.generated.generatedmod.items.CultivationLogic.maxAura(GeneratedMod.data(p)));
            YujianController.cast(p,YujianController.FLIGHT,0);
            h.assertTrue(Math.abs(beforeMana-GeneratedMod.data(p).wuxingxiantu$getAura()-summonCost)<.01F,
                    "Summon charged a different cost from the catalogue/UI");
            h.assertTrue(YujianController.riding(p)&&!p.getAbilities().mayfly,"Sword flight must not grant creative flight");
            for (int tick=0; tick<12; tick++) {
                var roots=h.getLevel().getEntitiesOfClass(ControlledSwordEntity.class,p.getBoundingBox().inflate(10),e->e.flightRoot()&&p.getUUID().equals(e.ownerId));
                roots.forEach(root->YujianController.tickFlightEntity(root,p));
            }
            h.assertTrue(p.getVehicle() instanceof ControlledSwordEntity,"Player did not mount the flight root");
            h.assertTrue(cn.blockforge.generated.generatedmod.robe.YujianSlot.activeFlyingSword(p)==sword&&p.getMainHandItem().isEmpty()&&sword.getCount()==1,"Flight consumed/duplicated source");
            YujianController.stop(p);
            h.assertTrue(!YujianController.riding(p)&&p.getAbilities().mayfly==mayfly&&p.getAbilities().getFlyingSpeed()==speed,"Flight failed to restore abilities");
            h.assertTrue(h.getLevel().getEntitiesOfClass(ControlledSwordEntity.class,p.getBoundingBox().inflate(10),e->p.getUUID().equals(e.ownerId)).isEmpty(),"Support entity survived release");
        }finally{YujianController.stop(p);p.discard();}
        h.succeed();
    }
    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void sixSwordSweepAndRecall(GameTestHelper h){
        var p=player(h);
        for(int i=0;i<7;i++)p.getInventory().setItem(i,sword(p));
        var victim=h.spawn(EntityType.COW,new BlockPos(5,2,1));victim.setNoAi(true);victim.setNoGravity(true);
        float health=victim.getHealth();
        try{
            YujianController.cast(p,"art_sword_sweep",0);
            var swords=h.getLevel().getEntitiesOfClass(ControlledSwordEntity.class,p.getBoundingBox().inflate(10),e->p.getUUID().equals(e.ownerId));
            h.assertTrue(swords.size()==6,"Formation is not capped at six implements");
            for(int tick=0;tick<46;tick++)for(var s:swords){s.age=tick;YujianController.tickSword(s,p);}
            h.assertTrue(victim.getHealth()<health,"Physical sweeping swords did no damage");
            h.assertTrue(swords.stream().allMatch(s->s.art.equals("return")||s.art.equals("dock")),"Swords did not enter return lane");
            var bound=swords.get(0);var source=YujianController.find(p,bound.binding);source.setCount(0);YujianController.tickSword(bound,p);
            h.assertTrue(bound.isRemoved(),"Removed inventory source left a usable sword clone");
            swords.forEach(net.minecraft.world.entity.Entity::discard);
        }finally{p.discard();}
        h.succeed();
    }
    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void guardReducesDamageWithoutDamagingSpiritSword(GameTestHelper h){
        var p=player(h);var stack=sword(p);p.getInventory().setItem(0,stack);
        try{
            YujianController.cast(p,"art_sword_guard",0);
            var attacker=h.spawn(EntityType.ZOMBIE,new BlockPos(4,2,1));
            var event=new net.minecraftforge.event.entity.living.LivingHurtEvent(p,p.damageSources().mobAttack(attacker),10);
            YujianController.guard(event);
            h.assertTrue(event.getAmount()<10&&event.getAmount()>0,"Guard did not reduce damage");
            h.assertTrue(!stack.isDamageableItem()&&stack.getDamageValue()==0,"Guard damaged a durability-free spirit sword");
            h.getLevel().getEntitiesOfClass(ControlledSwordEntity.class,p.getBoundingBox().inflate(10),e->p.getUUID().equals(e.ownerId)).forEach(net.minecraft.world.entity.Entity::discard);
        }finally{p.discard();}
        h.succeed();
    }

    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void arrayFinisherReturnsAndFishingRemoved(GameTestHelper h){
        var p=player(h);p.getInventory().setItem(0,sword(p));
        try{
            YujianController.cast(p,"art_sword_array",0);
            var s=h.getLevel().getEntitiesOfClass(ControlledSwordEntity.class,p.getBoundingBox().inflate(10),e->p.getUUID().equals(e.ownerId)).get(0);
            s.age=90;YujianController.tickSword(s,p);h.assertTrue(s.visualScale()==4,"Array giant finisher missing");
            s.age=100;YujianController.tickSword(s,p);h.assertTrue(s.visualScale()==1&&s.art.equals("return"),"Finisher failed to return");s.discard();
            h.assertTrue(!ArtCatalog.isValid("art_spirit_fishing")&&!SwordTechniqueManager.isSwordTechnique("art_spirit_fishing"),"Removed fishing art remains enabled");
            h.assertTrue(!net.minecraftforge.registries.ForgeRegistries.ITEMS.containsKey(new net.minecraft.resources.ResourceLocation(GeneratedMod.MOD_ID,"art_spirit_fishing")),"Fishing manual is still registered");
        }finally{p.discard();}
        h.succeed();
    }
}
