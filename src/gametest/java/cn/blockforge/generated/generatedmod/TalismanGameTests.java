package cn.blockforge.generated.generatedmod;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;

@GameTestHolder("wuxingxiantu_talismans")
@PrefixGameTestTemplate(false)
public final class TalismanGameTests {
    @GameTest(template="empty") public static void templatesAndEncoding(GameTestHelper h) {
        h.assertTrue(TalismanCatalog.ids().size()==7,"Unexpected candidate count");
        var unique=new java.util.HashSet<String>();
        for(String id:TalismanCatalog.ids()) {
            var glyph=TalismanCatalog.glyph(id);int count=TalismanCatalog.targetCount(id);
            h.assertTrue(count>20&&count<350,"Invalid source-derived glyph: "+id);
            h.assertTrue(TalismanCatalog.score(id,TalismanCatalog.decode(TalismanCatalog.encode(glyph)))==100,"Template round trip lost strokes");
            h.assertTrue(unique.add(java.util.Arrays.deepToString(glyph)),"Shared glyph template");
        }
        h.assertTrue(!TalismanCatalog.ids().contains("shield")&&!TalismanCatalog.ids().contains("heal"),"Legacy choices reintroduced");
        h.assertTrue(TalismanCatalog.has("shield")&&TalismanCatalog.has("heal"),"Saved legacy NBT broken");h.succeed();
    }
    @GameTest(template="empty") public static void purificationProtectsStoryAndWeakensDebuffs(GameTestHelper h) {
        var player=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());
        player.removeAllEffects();player.getCooldowns().removeCooldown(WuXingItems.TALISMAN.get());
        var data=GeneratedMod.data(player);data.wuxingxiantu$setDaoHeartUnstable(true);
        player.addEffect(new MobEffectInstance(MobEffects.POISON,300,1));
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,-1,0));
        var stack=new ItemStack(WuXingItems.TALISMAN.get());
        h.assertTrue(TalismanEffects.activate(TalismanCatalog.Effect.PURGE,player,stack),"Purge failed");
        h.assertTrue(!player.hasEffect(MobEffects.POISON)&&data.wuxingxiantu$isDaoHeartUnstable(),"Purge cleared story or failed cleanup");
        h.assertTrue(player.hasEffect(MobEffects.BLINDNESS),"Purge removed persistent mechanism");
        player.addEffect(new MobEffectInstance(MobEffects.POISON,200,1));
        h.assertTrue(player.getEffect(MobEffects.POISON).getDuration()==100,"Purge did not halve reapplication");
        player.addEffect(new MobEffectInstance(ModEffects.PARALYSIS.get(),200,0));
        h.assertTrue(player.getEffect(ModEffects.PARALYSIS.get()).getDuration()==200,"Purge altered custom mechanism");
        player.removeAllEffects();data.wuxingxiantu$resolveHeartDemonInvasion();h.succeed();
    }
    @GameTest(template="empty") public static void attacksRespectWallsAndLeaveBlocksUntouched(GameTestHelper h) {
        var level=h.getLevel();var player=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(level);
        var origin=h.absolutePos(new BlockPos(81,2,1));level.getChunkAt(origin);
        for(int x=-3;x<=3;x++)for(int y=0;y<5;y++)for(int z=0;z<=13;z++)
            level.setBlock(origin.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);
        player.setPos(origin.getX()+.5,origin.getY(),origin.getZ()+.5);player.setYRot(0);player.setXRot(0);
        Zombie mob=new Zombie(EntityType.ZOMBIE,level);mob.setNoAi(true);
        mob.setPos(origin.getX()+.5,origin.getY(),origin.getZ()+7.5);level.addFreshEntity(mob);
        for(int x=-2;x<=2;x++)for(int y=0;y<4;y++)level.setBlock(origin.offset(x,y,4),Blocks.STONE.defaultBlockState(),3);
        var stack=new ItemStack(WuXingItems.TALISMAN.get());player.getCooldowns().removeCooldown(stack.getItem());
        h.assertTrue(!TalismanEffects.activate(TalismanCatalog.Effect.THUNDER,player,stack),"Thunder hit through wall");
        float health=mob.getHealth();player.getCooldowns().removeCooldown(stack.getItem());
        TalismanEffects.activate(TalismanCatalog.Effect.FIRE,player,stack);
        h.assertTrue(mob.getHealth()==health&&level.getBlockState(origin.offset(0,1,4)).is(Blocks.STONE),"Fire damaged through wall or destroyed block");
        for(int x=-2;x<=2;x++)for(int y=0;y<4;y++)level.setBlock(origin.offset(x,y,4),Blocks.AIR.defaultBlockState(),3);
        player.getCooldowns().removeCooldown(stack.getItem());
        var ray=level.clip(new net.minecraft.world.level.ClipContext(player.getEyePosition(),player.getEyePosition().add(player.getLookAngle().scale(12)),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,player));
        h.assertTrue(TalismanEffects.activate(TalismanCatalog.Effect.THUNDER,player,stack)&&mob.hasEffect(ModEffects.PARALYSIS.get()),"Thunder missed unobstructed target; eye="+player.getEyePosition()+" look="+player.getLookAngle()+" mob="+mob.getBoundingBox()+" ray="+ray.getType()+" at="+ray.getLocation());
        h.assertTrue(mob.getHealth()<health&&!mob.isOnFire(),"Thunder caused no damage or ignited target");mob.discard();h.succeed();
    }
    @GameTest(template="empty") public static void gatheringDoesNotGrantInstantManaOrStack(GameTestHelper h) {
        var player=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());player.removeAllEffects();
        var data=GeneratedMod.data(player);data.wuxingxiantu$setAura(0);var stack=new ItemStack(WuXingItems.TALISMAN.get());
        for(int i=0;i<2;i++){player.getCooldowns().removeCooldown(stack.getItem());TalismanEffects.activate(TalismanCatalog.Effect.GATHER,player,stack);}
        h.assertTrue(data.wuxingxiantu$getAura()==0&&player.getEffect(ModEffects.TALISMAN_GATHERING.get()).getAmplifier()==0,"Gather grants instant mana or stacks power");
        player.removeAllEffects();h.succeed();
    }
    @GameTest(template="empty") public static void itemConsumesOnlyOnSuccessfulServerUse(GameTestHelper h) {
        var player=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());player.removeAllEffects();
        GeneratedMod.data(player).wuxingxiantu$setRealm(9);player.getAbilities().instabuild=false;
        ItemStack stack=new ItemStack(WuXingItems.TALISMAN.get(),2);TalismanData.setTalisman(stack,"gather");
        TalismanData.setQuality(stack,TalismanCatalog.Quality.LOW);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,stack);
        player.getCooldowns().removeCooldown(stack.getItem());stack.getItem().use(h.getLevel(),player,net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(stack.getCount()==1,"Successful use did not consume exactly one");
        stack.getItem().use(h.getLevel(),player,net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(stack.getCount()==1,"Cooldown failure consumed talisman");player.removeAllEffects();h.succeed();
    }
    @GameTest(template="empty") public static void herbTableIsFullCubeWithRecipeAndSelfDrop(GameTestHelper h) {
        var block=WuXingBlocks.HERB_PROCESSING_TABLE.get();var pos=h.absolutePos(new BlockPos(1,2,1));
        for(var direction:net.minecraft.core.Direction.Plane.HORIZONTAL) {
            var state=block.defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,direction);
            h.assertTrue(state.isCollisionShapeFullBlock(h.getLevel(),pos),"Table requires custom geometry");
            var drops=net.minecraft.world.level.block.Block.getDrops(state,h.getLevel(),pos,null);
            h.assertTrue(drops.size()==1&&drops.get(0).is(WuXingBlocks.HERB_PROCESSING_TABLE_ITEM.get()),"Table does not drop itself");
        }
        h.assertTrue(h.getLevel().getRecipeManager().getRecipes().stream().anyMatch(r->r.getResultItem(h.getLevel().registryAccess()).is(WuXingBlocks.HERB_PROCESSING_TABLE_ITEM.get())),"No survival acquisition recipe");h.succeed();
    }
}
