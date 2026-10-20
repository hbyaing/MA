package cn.blockforge.generated.generatedmod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemSmeltedEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.List;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.GeneratedMod.*;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class WorldEvents {
    @SubscribeEvent public static void sources(cn.blockforge.generated.generatedmod.acquisition.AcquisitionSourcesEvent event) {
        event.register("world-events",sink->{
            var chest=new cn.blockforge.generated.generatedmod.acquisition.SourceEvidence("loot_injection",
                    new ResourceLocation(GeneratedMod.MOD_ID,"world_chest_pool"),List.of("minecraft:chests/*; random loot-table roll"),
                    -1,null,List.of());
            for(var divine:DivineArtCatalog.ENTRIES)sink.add(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                    new ResourceLocation(GeneratedMod.MOD_ID,divine.itemId())),chest);
            sink.add(WuXingItems.INSIGHT.get(),chest);sink.add(WuXingItems.GUANDING_MANUAL.get(),chest);
            sink.add(WuXingItems.PILL_ASH.get(),new cn.blockforge.generated.generatedmod.acquisition.SourceEvidence(
                    "alchemy_failure",new ResourceLocation(GeneratedMod.MOD_ID,"failed_batch_ash"),
                    List.of("failed formula/free batch or exhausted infusion; consumes reserved ingredients; collect from furnace"),
                    -1,true,List.of("wuxingxiantu:alchemy_furnace")));
            sink.add(WuXingItems.YUE_COIN.get(),new cn.blockforge.generated.generatedmod.acquisition.SourceEvidence(
                    "conversion",new ResourceLocation(GeneratedMod.MOD_ID,"anvil_crush_spirit_stone"),
                    List.of("falling anvil lands on dropped spirit stone; one stone per conversion"),
                    -1,true,List.of("wuxingxiantu:spirit_stone","minecraft:anvil")));
        });
    }

    @SubscribeEvent public static void caveTokenChunkLoad(ChunkEvent.Load event) {
        if (!(event.getChunk() instanceof LevelChunk chunk)
                || !(chunk.getLevel() instanceof net.minecraft.server.level.ServerLevel level)) return;
        ExoticFireState.restoreChunk(level, chunk);
        CaveTokenLocations.queueReconciliation(level, chunk);
    }

    @SubscribeEvent public static void exoticFireChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getChunk() instanceof LevelChunk chunk)
                || !(chunk.getLevel() instanceof net.minecraft.server.level.ServerLevel level)) return;
        ExoticFireState.unloadChunk(level, chunk.getPos());
    }

    @SubscribeEvent public static void anvilCrushesSpiritStone(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof FallingBlockEntity falling)
                || event.getLevel().isClientSide
                || !(falling.getBlockState().is(BlockTags.ANVIL))
                || (!falling.onGround() && !falling.verticalCollision)) return;

        net.minecraft.world.phys.AABB impactBox = falling.getBoundingBox().inflate(0.25D, 0.35D, 0.25D);
        List<ItemEntity> spiritStones = event.getLevel().getEntities(
                falling, impactBox,
                entity -> entity instanceof ItemEntity item && item.getItem().is(WuXingItems.SPIRIT_STONE.get()))
                .stream().filter(ItemEntity.class::isInstance).map(ItemEntity.class::cast).toList();
        if (spiritStones.isEmpty()) return;

        ItemEntity spiritStone = spiritStones.get(0);
        ItemStack stack = spiritStone.getItem();
        stack.shrink(1);
        if (stack.isEmpty()) spiritStone.discard();
        else spiritStone.setItem(stack);

        ItemEntity coin = new ItemEntity(event.getLevel(), falling.getX(), falling.getY() + 0.15D,
                falling.getZ(), new ItemStack(WuXingItems.YUE_COIN.get()));
        coin.setDefaultPickUpDelay();
        event.getLevel().addFreshEntity(coin);
        event.getLevel().playSound(null, falling.blockPosition(),
                net.minecraft.sounds.SoundEvents.ANVIL_LAND, net.minecraft.sounds.SoundSource.BLOCKS,
                0.8F, 1.15F);
    }

    @SubscribeEvent public static void breakBlock(BlockEvent.BreakEvent event) {
        if(event.isCanceled()||cn.blockforge.generated.generatedmod.ParalysisControl.isTerrainBreak())return;
        Player player = event.getPlayer();
        if (isSoulProjection(player)) {
            event.setCanceled(true);
            return;
        }
        if (player.level().isClientSide) return;
        BlockState state = event.getState();
        if (state.is(net.minecraft.world.level.block.Blocks.SAND)
                && player.getRandom().nextInt(100) == 0) {
            player.spawnAtLocation(new ItemStack(WuXingItems.GLAZED_CRYSTAL.get()));
        }
        if (player.getRandom().nextInt(20) != 0) return;
        int luck = cn.blockforge.generated.generatedmod.balance.ElementMaterialDrops.grade(player.getLuck(),player.getRandom().nextDouble());
        if (state.is(BlockTags.LOGS)) player.spawnAtLocation(new ItemStack(WuXingItems.WOOD_MATERIALS[Math.min(3, luck)].get()));
        else if (state.is(BlockTags.IRON_ORES) || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.DIAMOND_ORES)) player.spawnAtLocation(new ItemStack(WuXingItems.GOLD_MATERIALS[Math.min(3, luck)].get()));
        else if (state.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,new ResourceLocation(GeneratedMod.MOD_ID,"earth_core_soils")))) player.spawnAtLocation(new ItemStack(WuXingItems.EARTH_MATERIALS[Math.min(3, luck)].get()));
    }

    @SubscribeEvent public static void injectChestInsight(LootTableLoadEvent event) {
        ResourceLocation id = event.getName();
        if (!"minecraft".equals(id.getNamespace()) || !id.getPath().startsWith("chests/")) return;
        event.getTable().addPool(LootPool.lootPool()
                .name("wuxingxiantu_cultivation_insight")
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(WuXingItems.INSIGHT.get()).setWeight(1))
                .when(LootItemRandomChanceCondition.randomChance(cn.blockforge.generated.generatedmod.balance.ChestBalance.forPath(id.getPath()).insight()))
                .build());
        event.getTable().addPool(LootPool.lootPool()
                .name("wuxingxiantu_guanding_manual")
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(WuXingItems.GUANDING_MANUAL.get()).setWeight(1))
                .when(LootItemRandomChanceCondition.randomChance(cn.blockforge.generated.generatedmod.balance.ChestBalance.forPath(id.getPath()).manual()))
                .build());
    }

    @SubscribeEvent public static void injectDivineInheritance(LootTableLoadEvent event) {
        ResourceLocation id = event.getName();
        if (!"minecraft".equals(id.getNamespace()) || !id.getPath().startsWith("chests/")) return;
        event.getTable().addPool(LootPool.lootPool().name("wuxingxiantu_divine_inheritance")
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(WuXingItems.FUNIAO_FU_FRAGMENT.get()).setWeight(1))
                .add(LootItem.lootTableItem(WuXingItems.DAODE_JING_FRAGMENT.get()).setWeight(1))
                .add(LootItem.lootTableItem(WuXingItems.GANGDOU_STARMAP.get()).setWeight(1))
                .add(LootItem.lootTableItem(WuXingItems.ZHONGLV_FRAGMENT.get()).setWeight(1))
                .add(LootItem.lootTableItem(WuXingItems.YIN_FU_JING_YI_SHEN_FRAGMENT.get()).setWeight(1))
                 .add(LootItem.lootTableItem(WuXingItems.RI_YUE_TING_LUN_FRAGMENT.get()).setWeight(1))
                .add(LootItem.lootTableItem(WuXingItems.DA_PIN_TIAN_XIAN_JUE_FRAGMENT_ONE.get()).setWeight(1))
                .add(LootItem.lootTableItem(WuXingItems.DA_PIN_TIAN_XIAN_JUE_FRAGMENT_TWO.get()).setWeight(1))
                .when(LootItemRandomChanceCondition.randomChance(cn.blockforge.generated.generatedmod.balance.ChestBalance.forPath(id.getPath()).divine())).build());
    }

    @SubscribeEvent public static void fishing(ItemFishedEvent event) {
        if (!(event.getHookEntity().getPlayerOwner() instanceof ServerPlayer player)) return;
        if (player.getRandom().nextInt(20) == 0) event.getDrops().add(new ItemStack(WuXingItems.WATER_MATERIALS[0].get()));
    }

    @SubscribeEvent public static void smeltOre(ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack smelting = event.getSmelting();
        int chance = smelting.is(WuXingItems.SPIRIT_STONE.get()) ? 20
                : smelting.is(WuXingItems.AURA_CRYSTAL.get()) ? 50 : 0;
        if (chance == 0) return;
        for (int i = 0; i < smelting.getCount(); i++) {
            if (player.getRandom().nextInt(100) < chance) {
                player.getInventory().placeItemBackInInventory(new ItemStack(WuXingItems.AURA_CRYSTAL.get()));
            }
        }
    }

    @SubscribeEvent public static void spawnCheck(net.minecraftforge.event.entity.living.MobSpawnEvent.PositionCheck event) {
        if (event.getEntity().getType().getCategory() != net.minecraft.world.entity.MobCategory.MONSTER) return;
        net.minecraft.world.entity.MobSpawnType spawnType = event.getSpawnType();
        if (spawnType == net.minecraft.world.entity.MobSpawnType.COMMAND
                || spawnType == net.minecraft.world.entity.MobSpawnType.SPAWN_EGG
                || spawnType == net.minecraft.world.entity.MobSpawnType.DISPENSER) return;
        net.minecraft.server.level.ServerLevel level = event.getLevel().getLevel();
        BlockPos spawnPos = event.getEntity().blockPosition();
        if (CaveTokenLocations.get(level).protects(spawnPos)) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
        }
    }

    @SubscribeEvent public static void preventServantDespawn(net.minecraftforge.event.entity.living.MobSpawnEvent.AllowDespawn event) {
        if (cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.isServant(event.getEntity())) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
        }
    }
    private WorldEvents() {}
}
