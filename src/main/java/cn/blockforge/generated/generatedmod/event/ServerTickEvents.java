package cn.blockforge.generated.generatedmod.event;

import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraft.util.Mth;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.List;
import java.util.Set;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.GeneratedMod.*;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class ServerTickEvents {
    @SubscribeEvent public static void sources(cn.blockforge.generated.generatedmod.acquisition.AcquisitionSourcesEvent event) {
        event.register("spirit-liquid",sink->{
            var source=new net.minecraft.resources.ResourceLocation(GeneratedMod.MOD_ID,"spirit_liquid_conversion");
            sink.add(WuXingItems.SPIRIT_COAL.get(),new cn.blockforge.generated.generatedmod.acquisition.SourceEvidence(
                    "conversion",source,List.of("remain in spirit liquid for "+SPIRIT_LIQUID_CONVERSION_TICKS+" ticks"),
                    -1,true,List.of("minecraft:coal")));
            sink.add(WuXingBlocks.SPIRIT_COAL_BLOCK_ITEM.get(),new cn.blockforge.generated.generatedmod.acquisition.SourceEvidence(
                    "conversion",source,List.of("remain in spirit liquid for "+SPIRIT_LIQUID_CONVERSION_TICKS+" ticks"),
                    -1,true,List.of("minecraft:coal_block")));
            sink.add(WuXingItems.SPIRIT_FERTILIZER.get(),new cn.blockforge.generated.generatedmod.acquisition.SourceEvidence(
                    "conversion",source,List.of("consume a source fluid block; up to "+BONE_MEAL_PER_SPIRIT_SOURCE+" bone meal"),
                    -1,true,List.of("minecraft:bone_meal","wuxingxiantu:spirit_liquid")));
        });
    }
    private static final String SPIRIT_LIQUID_CONVERSION_TIME = "SpiritLiquidConversionTime";
    private static final int SPIRIT_LIQUID_CONVERSION_TICKS = 200;
    private static final int BONE_MEAL_CONVERSION_TICKS = 60;
    private static final int BONE_MEAL_PER_SPIRIT_SOURCE = 8;
    private static final Set<ItemEntity> SPIRIT_LIQUID_CANDIDATES = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());

    @SubscribeEvent public static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;
        CaveTokenLocations.tickPending(event.getServer());
        SpiritShrineManager.tickPending(event.getServer());
        Network.tickAlchemy(event.getServer());
        TribulationManager.tick(event.getServer());
         TimeStopManager.tick(event.getServer());
        cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.tickServants(event.getServer());
        tickSpiritLiquidConversion(event.getServer());
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % 20L == 0L) {
            for (net.minecraft.server.level.ServerLevel level : event.getServer().getAllLevels()) {
                ExoticFireState.cleanup(level);
                ExoticFireState.keepEternalFires(level);
                // Spread inherits tier inside ExoticFireBlock.tick; no global nearby-fire sweep.
            }
        }
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            // Dead players remain in PlayerList until respawn; their capabilities may already be invalidated.
            if (!player.isAlive() || player.isRemoved() || data(player) == null) continue;
            cn.blockforge.generated.generatedmod.event.CultivationTicker.tickPlayer(player, gameTime);
            CultivationLogic.advanceMinorStage(player,data(player));
            // 放在统一服务端玩家循环末尾，确保持续技能、领域和飞行消耗结算后仍必定恢复灵力。
            if (!player.isSpectator()) {
                CultivationLogic.tickManaRegen(player);
            }
            if (gameTime % 5L == 0L) sync(player);
            if (gameTime % 10L == 0L) Network.sendFireState(player);
            if (gameTime % 20L == 0L) {
                Network.sendTribulationState(player);
                cn.blockforge.generated.generatedmod.network.service.AlchemyService.deliverRefund(player);
            }
        }
    }

    @SubscribeEvent public static void serverStopping(ServerStoppingEvent event) {
        cn.blockforge.generated.generatedmod.network.CultivationSync.clear();
        cn.blockforge.generated.generatedmod.network.service.CompassService.clear();
         TimeStopManager.clearAll(event.getServer());
        SPIRIT_LIQUID_CANDIDATES.clear();
        Network.clearPending(event.getServer());
        CaveTokenLocations.clearPending(event.getServer());
        SpiritShrineManager.clearPending(event.getServer());
    }

    private static void tickSpiritLiquidConversion(net.minecraft.server.MinecraftServer server) {
        for (ItemEntity itemEntity : List.copyOf(SPIRIT_LIQUID_CANDIDATES)) {
                if (!itemEntity.isAlive() || !(itemEntity.level() instanceof net.minecraft.server.level.ServerLevel level)
                        || !isConvertibleInSpiritLiquid(itemEntity)) { SPIRIT_LIQUID_CANDIDATES.remove(itemEntity); continue; }
                ItemStack stack = itemEntity.getItem();
                if (!isInSpiritLiquid(itemEntity, level)) {
                    itemEntity.getPersistentData().remove(SPIRIT_LIQUID_CONVERSION_TIME);
                    continue;
                }

                boolean boneMeal = stack.is(net.minecraft.world.item.Items.BONE_MEAL);
                int required = boneMeal ? BONE_MEAL_CONVERSION_TICKS : SPIRIT_LIQUID_CONVERSION_TICKS;
                int elapsed = itemEntity.getPersistentData().getInt(SPIRIT_LIQUID_CONVERSION_TIME) + 1;
                if (elapsed < required) {
                    itemEntity.getPersistentData().putInt(SPIRIT_LIQUID_CONVERSION_TIME, elapsed);
                    if (level.getGameTime() % 20L == 0L) {
                        level.sendParticles(net.minecraft.core.particles.ParticleTypes.BUBBLE,
                                itemEntity.getX(), itemEntity.getY() + 0.18D, itemEntity.getZ(),
                                2, 0.08D, 0.08D, 0.08D, 0.015D);
                    }
                    continue;
                }

                if (boneMeal) {
                    // 灵肥转化必须付出灵液成本：附近找不到可消耗的灵液水源则继续等待。
                    if (!consumeSpiritLiquid(itemEntity, level)) {
                        itemEntity.getPersistentData().remove(SPIRIT_LIQUID_CONVERSION_TIME);
                        continue;
                    }
                    int converted = Math.min(stack.getCount(), BONE_MEAL_PER_SPIRIT_SOURCE);
                    ItemStack fertilizer = new ItemStack(WuXingItems.SPIRIT_FERTILIZER.get(), converted);
                    if (converted == stack.getCount()) {
                        itemEntity.setItem(fertilizer);
                    } else {
                        ItemStack remaining = stack.copy();
                        remaining.shrink(converted);
                        itemEntity.setItem(remaining);
                        ItemEntity output = new ItemEntity(level, itemEntity.getX(), itemEntity.getY(),
                                itemEntity.getZ(), fertilizer);
                        output.setDeltaMovement(itemEntity.getDeltaMovement());
                        output.setDefaultPickUpDelay();
                        level.addFreshEntity(output);
                    }
                } else {
                    ItemStack result = stack.is(net.minecraft.world.item.Items.COAL)
                            ? new ItemStack(WuXingItems.SPIRIT_COAL.get(), stack.getCount())
                            : new ItemStack(WuXingBlocks.SPIRIT_COAL_BLOCK_ITEM.get(), stack.getCount());
                    itemEntity.setItem(result);
                }
                itemEntity.getPersistentData().remove(SPIRIT_LIQUID_CONVERSION_TIME);
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                        itemEntity.getX(), itemEntity.getY() + 0.18D, itemEntity.getZ(),
                        10, 0.18D, 0.18D, 0.18D, 0.08D);
                level.playSound(null, itemEntity.blockPosition(), net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.25F);
        }
    }

    private static boolean isConvertibleInSpiritLiquid(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        return stack.is(net.minecraft.world.item.Items.COAL)
                || stack.is(net.minecraft.world.level.block.Blocks.COAL_BLOCK.asItem())
                || stack.is(net.minecraft.world.item.Items.BONE_MEAL);
    }

    private static boolean consumeSpiritLiquid(ItemEntity itemEntity,
                                               net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.phys.AABB box = itemEntity.getBoundingBox();
        int minX = Mth.floor(box.minX);
        int maxX = Mth.floor(box.maxX);
        int minY = Mth.floor(box.minY);
        int maxY = Mth.floor(box.maxY);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.floor(box.maxZ);
        for (int y = maxY; y >= minY; y--) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    var fluidState = level.getFluidState(pos);
                    if (fluidState.is(ModFluids.SPIRIT_LIQUID_SOURCE.get()) && fluidState.isSource()) {
                        return level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
        return false;
    }

    private static boolean isInSpiritLiquid(ItemEntity itemEntity, net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.phys.AABB box = itemEntity.getBoundingBox();
        int minX = Mth.floor(box.minX);
        int maxX = Mth.floor(box.maxX);
        int minY = Mth.floor(box.minY);
        int maxY = Mth.floor(box.maxY);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.floor(box.maxZ);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos fluidPos = new BlockPos(x, y, z);
                    var fluidState = level.getFluidState(fluidPos);
                    if ((fluidState.is(cn.blockforge.generated.generatedmod.registry.ModFluids.SPIRIT_LIQUID_SOURCE.get())
                            || fluidState.is(cn.blockforge.generated.generatedmod.registry.ModFluids.SPIRIT_LIQUID_FLOWING.get()))
                            && box.minY <= fluidPos.getY() + fluidState.getHeight(level, fluidPos) + 0.08D
                            && box.maxY >= fluidPos.getY()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SubscribeEvent public static void trackConversionItem(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity item && !event.getLevel().isClientSide()
                && isConvertibleInSpiritLiquid(item)) SPIRIT_LIQUID_CANDIDATES.add(item);
        if (!event.getLevel().isClientSide()
                && cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.isServant(event.getEntity()))
            cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.trackServant(event.getEntity());
    }

    @SubscribeEvent public static void untrackConversionItem(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity item) SPIRIT_LIQUID_CANDIDATES.remove(item);
        cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.untrackServant(event.getEntity());
    }
    private ServerTickEvents() {}
}
