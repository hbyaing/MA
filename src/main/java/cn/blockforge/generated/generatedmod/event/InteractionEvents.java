package cn.blockforge.generated.generatedmod.event;

import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.items.ArtManualItem;
import cn.blockforge.generated.generatedmod.items.HeartMethodItem;
import cn.blockforge.generated.generatedmod.items.GuandingItem;
import cn.blockforge.generated.generatedmod.items.SoulBindingPearlItem;
import cn.blockforge.generated.generatedmod.items.SoulCapturePearlItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.GeneratedMod.*;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class InteractionEvents {

    @SubscribeEvent public static void rightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) return;
        ItemStack stack = player.getItemInHand(event.getHand());
        if (isSoulProjection(player)) {
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
            event.setCanceled(true);
            return;
        }
        if (stack.getItem() instanceof HeartMethodItem method) {
            method.use(player.level(), player, event.getHand());
            event.setCancellationResult(net.minecraft.world.InteractionResult.CONSUME);
            event.setCanceled(true);
        } else if (stack.getItem() instanceof cn.blockforge.generated.generatedmod.items.DivineManualItem manual) {
            manual.use(player.level(), player, event.getHand());
            event.setCancellationResult(net.minecraft.world.InteractionResult.CONSUME);
            event.setCanceled(true);
        } else if (stack.getItem() instanceof GuandingItem) {
            // 灌顶必须作用于实体，空气右键不应进入普通物品使用流程。
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
            event.setCanceled(true);
        } else if (stack.getItem() instanceof ArtManualItem manual) {
            manual.use(player.level(), player, event.getHand());
            event.setCancellationResult(net.minecraft.world.InteractionResult.CONSUME);
            event.setCanceled(true);
        } else if (stack.getItem() instanceof cn.blockforge.generated.generatedmod.items.FireSeedItem seed
                && seed.tier() > 0) {
            seed.use(player.level(), player, event.getHand());
            event.setCancellationResult(net.minecraft.world.InteractionResult.CONSUME);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = player.getItemInHand(event.getHand());
        // 装备异火时直接放置对应品阶的灵火，避免先出现原版橙火再换色。
        // 永燃灵煤块保留自己的交互（不可在这里提前取消）。
        if (stack.is(net.minecraft.world.item.Items.FLINT_AND_STEEL)
                && !(player.level().getBlockState(event.getPos()).getBlock() instanceof EternalSpiritCoalBlock)) {
            CultivationData fireData = data(player);
            // 驭使资格由统一门槛复核：境界不足的高阶异火即使“已装备”也按未装备处理，
            // 打火石退回原版点火，防止借高阶异火跨阶点燃/驭使。
            int fireTier = fireData == null ? 0
                    : ProgressionGate.effectiveFire(fireData, fireData.wuxingxiantu$getEquippedFire());
            BlockPos firePos = event.getPos().relative(event.getFace());
            if (fireTier > 0 && player.level().getBlockState(firePos).isAir()
                    && net.minecraft.world.level.block.BaseFireBlock.canBePlacedAt(
                    player.level(), firePos, player.getDirection())) {
                if (!player.level().isClientSide) {
                    EternalSpiritCoalBlock.placeExoticFire(player.level(), firePos, fireTier,
                            player, event.getHand(), stack, true, player.getDirection());
                } else {
                    // 预登记品阶，让本地客户端在服务端方块同步前就使用异火着色。
                    ExoticFireState.predictClient(player.level(), firePos,
                            ExoticFireColorRegistry.flintVisualTier(fireTier));
                }
                event.setCancellationResult(net.minecraft.world.InteractionResult.sidedSuccess(
                        player.level().isClientSide));
                event.setCanceled(true);
                return;
            }
        }
        if (stack.getItem() instanceof SoulCapturePearlItem capture) {
            // 手持摄魄珠右键可打开 UI 的方块时，优先让方块打开界面（尤其炼魂炉/化魄炉/愈魂室），
            // 而不是先触发摄魄珠的提示或射线。
            net.minecraft.world.level.block.Block targetBlock = player.level().getBlockState(event.getPos()).getBlock();
            if (targetBlock instanceof SoulFurnaceBlock
                    || targetBlock instanceof SoulHealingRoomBlock) {
                return;
            }
            event.setCancellationResult(capture.useOn(new net.minecraft.world.item.context.UseOnContext(
                    player, event.getHand(), event.getHitVec())));
            event.setCanceled(true);
            return;
        }
        if (isSoulProjection(player)) {
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
            event.setCanceled(true);
            return;
        }
        if (player.level().isClientSide || !(player instanceof ServerPlayer)) return;
        if (stack.getItem() instanceof cn.blockforge.generated.generatedmod.items.DivineManualItem manual) {
            manual.use(player.level(), player, event.getHand());
            event.setCancellationResult(net.minecraft.world.InteractionResult.CONSUME);
            event.setCanceled(true);
            return;
        }
        if (stack.getItem() instanceof cn.blockforge.generated.generatedmod.items.FireSeedItem seed
                && seed.tier() > 0
                && !(player.level().getBlockState(event.getPos()).getBlock()
                instanceof cn.blockforge.generated.generatedmod.EternalSpiritCoalBlock)) {
            seed.use(player.level(), player, event.getHand());
            event.setCancellationResult(net.minecraft.world.InteractionResult.CONSUME);
            event.setCanceled(true);
            return;
        }
        if (stack.getItem() instanceof HeartMethodItem method) {
            method.use(player.level(), player, event.getHand());
            event.setCancellationResult(net.minecraft.world.InteractionResult.CONSUME);
            event.setCanceled(true);
            return;
        }
        if (stack.getItem() instanceof ArtManualItem manual) {
            manual.use(player.level(), player, event.getHand());
            event.setCancellationResult(net.minecraft.world.InteractionResult.CONSUME);
            event.setCanceled(true);
            return;
        }
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        // 炼丹炉成型后组成方块仍是金块/高炉/符文石/永燃灵煤块，核心也照常显示；
        // 这里让成型结构内任意组件右键都打开炼丹界面。
        BlockPos alchemyCore = AlchemyFurnaceStructure.findCore(player.level(), event.getPos());
        if (alchemyCore != null && AlchemyFurnaceBlock.isFormed(player.level().getBlockState(alchemyCore))) {
            AlchemyFurnaceBlock.openAlchemy(player.level(), alchemyCore, player);
            event.setCancellationResult(net.minecraft.world.InteractionResult.CONSUME);
            event.setCanceled(true);
            return;
        }
        CultivationData data = data(player);
        if (data != null && player.isShiftKeyDown() && CultivationLogic.canGoldDomain(data)
                && data.wuxingxiantu$isGoldDomainEnabled()
                && player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()) {
            handleGoldDomain((ServerPlayer) player, data, event.getPos());
            event.setCanceled(true);
            return;
        }
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) return;
        if (data == null) return;
        if (!hasRoot(data, 1) || !data.wuxingxiantu$isWoodDomainEnabled()) return;
        // 天雷灵根显化后，木灵领域的催生被万雷领域取代。
        if (CultivationLogic.isThunderForm(data)) return;
        BlockPos clicked = event.getPos();
        BlockState state = player.level().getBlockState(clicked);
        if (isSwitchableBlock(state) || !(state.getBlock() instanceof BonemealableBlock)) return;
        int quality = data.wuxingxiantu$getRootQuality(1);
        int radius = quality >= 4 ? 3 : quality >= 3 ? 2 : quality >= 2 ? 1 : 0;
        int cost = quality >= 4 ? 5 : quality >= 3 ? 3 : quality >= 2 ? 2 : 0;
        if (cost == 0) return;
        if (data.wuxingxiantu$getAura() < cost) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.low_mana"), true);
            return;
        }
        var serverLevel = (net.minecraft.server.level.ServerLevel) player.level();
        boolean changed = false;
        for (BlockPos pos : BlockPos.betweenClosed(clicked.offset(-radius, 0, -radius), clicked.offset(radius, 0, radius))) {
            BlockState targetState = serverLevel.getBlockState(pos);
            if (targetState.getBlock() instanceof BonemealableBlock target
                    && target.isValidBonemealTarget(serverLevel, pos, targetState, false)) {
                target.performBonemeal(serverLevel, serverLevel.random, pos, targetState);
                changed = true;
            }
        }
        if (changed) {
            data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - cost);
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.wood_growth", radius * 2 + 1), true);
            sync((ServerPlayer) player);
        }
    }

    private static void handleGoldDomain(ServerPlayer player, CultivationData data, BlockPos clicked) {
        long now = player.level().getGameTime();
        if (data.wuxingxiantu$getGoldDomainCooldown() > now) return;
        BlockState state = player.level().getBlockState(clicked);
        int quality = data.wuxingxiantu$getRootQuality(0);
        if (quality < 2) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.gold_domain_invalid"), true);
            return;
        }
        BlockState result = CultivationLogic.goldDomainResult(state, quality);
        if (result == null) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.gold_domain_invalid"), true);
            return;
        }
        int cost = CultivationLogic.goldDomainCost(data);
        if (data.wuxingxiantu$getAura() < cost) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.gold_domain_missing", cost), true);
            return;
        }
        if (quality == 3) {
            ItemStack drop = mineralDropFor(state);
            if (drop.isEmpty()) return;
            ItemStack output = drop.copyWithCount(player.getRandom().nextInt(100) < 25 ? 2 : 1);
            giveOrDrop(player, output);
            player.level().setBlock(clicked, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        } else {
            player.level().setBlock(clicked, result, 3);
        }
        data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - cost);
        data.wuxingxiantu$setGoldDomainCooldown(now + CultivationLogic.goldDomainCooldown(data));
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.gold_domain_done"), true);
        sync(player);
    }

    private static boolean isCustomGoldDomainOre(BlockState state) {
        return state.is(WuXingBlocks.SPIRIT_COAL_ORE.get()) || state.is(WuXingBlocks.SPIRIT_ORE.get());
    }

    private static ItemStack mineralDropFor(BlockState state) {
        if (state.is(WuXingBlocks.SPIRIT_COAL_ORE.get())) return new ItemStack(WuXingItems.SPIRIT_COAL.get());
        if (state.is(WuXingBlocks.SPIRIT_ORE.get())) return new ItemStack(WuXingItems.RAW_DARK_IRON.get());
        if (state.is(net.minecraft.world.level.block.Blocks.NETHER_QUARTZ_ORE)) return new ItemStack(net.minecraft.world.item.Items.QUARTZ);
        if (state.is(net.minecraft.tags.BlockTags.IRON_ORES)) return new ItemStack(net.minecraft.world.item.Items.IRON_INGOT);
        if (state.is(net.minecraft.tags.BlockTags.GOLD_ORES)) return new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT);
        if (state.is(net.minecraft.tags.BlockTags.COPPER_ORES)) return new ItemStack(net.minecraft.world.item.Items.COPPER_INGOT);
        if (state.is(net.minecraft.tags.BlockTags.DIAMOND_ORES)) return new ItemStack(net.minecraft.world.item.Items.DIAMOND);
        if (state.is(net.minecraft.tags.BlockTags.EMERALD_ORES)) return new ItemStack(net.minecraft.world.item.Items.EMERALD);
        if (state.is(net.minecraft.tags.BlockTags.COAL_ORES)) return new ItemStack(net.minecraft.world.item.Items.COAL);
        if (state.is(net.minecraft.tags.BlockTags.REDSTONE_ORES)) return new ItemStack(net.minecraft.world.item.Items.REDSTONE);
        if (state.is(net.minecraft.tags.BlockTags.LAPIS_ORES)) return new ItemStack(net.minecraft.world.item.Items.LAPIS_LAZULI);
        return ItemStack.EMPTY;
    }

    @SubscribeEvent public static void rightClickEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        CultivationData projectionData = data(player);
        if(projectionData!=null&&("yi_hun_ji_po".equals(projectionData.wuxingxiantu$getActiveDivineArt())
                ||"yuan_shen_chu_qiao".equals(projectionData.wuxingxiantu$getActiveDivineArt()))) {
            if(player.level().isClientSide)return;
            if(player instanceof ServerPlayer server && event.getTarget() instanceof SoulBodyEntity body
                    && body.returnSoul(server)) {
                event.setCancellationResult(net.minecraft.world.InteractionResult.CONSUME);
                event.setCanceled(true);
                return;
            }
        }
        // 只在服务端处理寄魄。客户端提前取消 EntityInteract 会阻断原版交互包，
        // 导致服务端根本收不到右键目标，也就不会建立控制状态或记录形态。
        LivingEntity possessionTarget = DivineArtLogic.possessionTarget(event.getTarget());
        if (!player.level().isClientSide && projectionData != null
                && "yi_hun_ji_po".equals(projectionData.wuxingxiantu$getActiveDivineArt())
                && possessionTarget != null) {
            boolean possessed = player instanceof ServerPlayer serverPlayer
                    && DivineArtLogic.tryPossess(serverPlayer, possessionTarget);
            event.setCancellationResult(possessed ? net.minecraft.world.InteractionResult.CONSUME
                    : net.minecraft.world.InteractionResult.FAIL);
            event.setCanceled(true);
            return;
        }
        if (isSoulProjection(player)) {
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
            event.setCanceled(true);
            return;
        }
        ItemStack held = player.getItemInHand(event.getHand());
        if (held.getItem() instanceof cn.blockforge.generated.generatedmod.items.SoulBindingPearlItem binding
                && event.getTarget() instanceof net.minecraft.world.entity.npc.AbstractVillager villager) {
            event.setCancellationResult(binding.interactLivingEntity(held, player, villager, event.getHand()));
            event.setCanceled(true);
            return;
        }
        if (held.getItem() instanceof cn.blockforge.generated.generatedmod.items.SoulUtilityPearlItem utility
                && event.getTarget() instanceof LivingEntity target && target != player
                && SoulPearlData.isCreativeMyriad(held)) {
            event.setCancellationResult(utility.interactLivingEntity(held, player, target, event.getHand()));
            event.setCanceled(true);
            return;
        }
        if (held.getItem() instanceof cn.blockforge.generated.generatedmod.items.SoulCapturePearlItem capture
                && event.getTarget() instanceof LivingEntity target && target != player) {
            event.setCancellationResult(capture.interactLivingEntity(held, player, target, event.getHand()));
            event.setCanceled(true);
            return;
        }
        if (held.getItem() instanceof GuandingItem guanding
                && event.getTarget() instanceof Player target) {
            event.setCancellationResult(guanding.interactLivingEntity(held, player, target, event.getHand()));
            event.setCanceled(true);
            return;
        }
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)
                || !(event.getTarget() instanceof Animal animal)
                || !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) return;
        CultivationData data = data(serverPlayer);
        // 御兽心法按实际生效心法判定：境界不足时保留学习记录但不产生被动效果。
        if (data == null || !"beast_taming".equals(ProgressionGate.effectiveHeartMethod(serverPlayer, data))) return;
        applyBeastTaming(serverPlayer, data, animal);
        event.setCancellationResult(net.minecraft.world.InteractionResult.CONSUME);
        event.setCanceled(true);
    }

    private static void applyBeastTaming(ServerPlayer player, CultivationData data, Animal animal) {
        final int cost = 10;
        if (data.wuxingxiantu$getAura() < cost) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.beast_taming_missing", cost), true);
            return;
        }
        data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - cost);
        int realm = data.wuxingxiantu$getRealm();
        int quality = Math.max(1, data.wuxingxiantu$getRootQuality(1));
        int growthDays = 1 + realm + quality;
        if (animal.getAge() < 0) {
            animal.ageUp(growthDays * 24000, true);
        }
        animal.setInLoveTime(600 + realm * 100);
        int tameChance = Math.min(80, 10 + realm * 7 + quality * 5);
        boolean tamed = false;
        if (animal instanceof net.minecraft.world.entity.TamableAnimal tamable && !tamable.isTame()
                && player.getRandom().nextInt(100) < tameChance) {
            tamable.tame(player);
            tamed = true;
        } else if (animal instanceof net.minecraft.world.entity.animal.horse.AbstractHorse horse && !horse.isTamed()
                && player.getRandom().nextInt(100) < tameChance) {
            horse.setTamed(true);
            horse.setOwnerUUID(player.getUUID());
            tamed = true;
        }
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.beast_taming_done",
                growthDays, tameChance, tamed), true);
        sync(player);
    }

    @SubscribeEvent public static void leftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (ExoticFireState.extinguishEternalFire(player.level(), event.getPos())) {
            if (!player.level().isClientSide) {
                player.level().playSound(null, event.getPos(), net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            event.setCanceled(true);
            event.setUseBlock(net.minecraftforge.eventbus.api.Event.Result.DENY);
            event.setUseItem(net.minecraftforge.eventbus.api.Event.Result.DENY);
            return;
        }
    }

    private static boolean isSwitchableBlock(BlockState state) {
        return state.getBlock() instanceof net.minecraft.world.level.block.DoorBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.ButtonBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.FenceGateBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.TrapDoorBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.LeverBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.BasePressurePlateBlock;
    }

    @SubscribeEvent public static void useFood(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getItem();
        if (!stack.isEdible() || stack.getItem() instanceof cn.blockforge.generated.generatedmod.items.BasePillItem) return;
        CultivationData data = data(player);
        if (data == null) return;
        var food = stack.getFoodProperties(player);
        if (food != null) {
            data.wuxingxiantu$setAura(Math.min(CultivationLogic.maxAura(data), data.wuxingxiantu$getAura() + food.getNutrition() * 5.0F));
            if (hasRoot(data, 1) && data.wuxingxiantu$getRootQuality(1) >= 3) player.heal(4.0F);
            sync(player);
        }
    }
    private InteractionEvents() {}
}
