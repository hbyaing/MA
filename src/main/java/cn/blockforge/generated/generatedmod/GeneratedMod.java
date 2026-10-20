package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.registry.ModEntities;
import cn.blockforge.generated.generatedmod.registry.ModBlockEntities;
import cn.blockforge.generated.generatedmod.registry.ModMenus;
import cn.blockforge.generated.generatedmod.registry.ModFluids;
import cn.blockforge.generated.generatedmod.registry.ModFeatures;
import cn.blockforge.generated.generatedmod.registry.ModRecipes;
import cn.blockforge.generated.generatedmod.registry.ModTabs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

/** 五行仙途主类：注册内容，并集中处理服务端修炼规则。 */
@Mod(GeneratedMod.MOD_ID)
public final class GeneratedMod {
    public static final String MOD_ID = "wuxingxiantu";
    public static final net.minecraft.resources.ResourceLocation CULTIVATION_CAP = new net.minecraft.resources.ResourceLocation(MOD_ID, "cultivation_data");
    private static final long TIDE_PERIOD = 144000L;
    private static final long TIDE_LENGTH = 12000L;
    private static final long STORM_LENGTH = 6000L;
    /** 骨粉浸润约 3 秒即转化为灵肥（文档建议 2~3 秒）。 */

    public GeneratedMod() {
        cn.blockforge.generated.generatedmod.balance.HealthCompatibility.initialize();
        software.bernie.geckolib.GeckoLib.initialize();
        var modContext = net.minecraftforge.fml.ModLoadingContext.get();
        modContext.registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT,
                HudConfig.SPEC, "wuxingxiantu-hud-client.toml");
        modContext.registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT,
                JindouCloudConfig.SPEC, "wuxingxiantu-cloud-client.toml");
        modContext.registerConfig(net.minecraftforge.fml.config.ModConfig.Type.SERVER,
                cn.blockforge.generated.generatedmod.yujian.FlyingSwordConfig.SERVER_SPEC,"wuxingxiantu-flight-server.toml");
        modContext.registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT,
                cn.blockforge.generated.generatedmod.yujian.FlyingSwordConfig.CLIENT_SPEC,"wuxingxiantu-flight-client.toml");
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modContext.registerConfig(net.minecraftforge.fml.config.ModConfig.Type.SERVER,
                cn.blockforge.generated.generatedmod.balance.GameplayConfig.SPEC,"wuxingxiantu-gameplay-server.toml");

        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        WuXingBlocks.BLOCKS.register(modEventBus);
        WuXingProfessions.POI_TYPES.register(modEventBus);
        WuXingProfessions.PROFESSIONS.register(modEventBus);
        WuXingItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModTabs.TABS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModRecipes.RECIPE_TYPES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        SpiritEnchantment.ENCHANTMENTS.register(modEventBus);
        // 触发 SpiritPlantRegistry 静态初始化：14 株灵植的方块与物品挂进
        // WuXingBlocks.BLOCKS / WuXingItems.ITEMS，必须早于注册事件触发。
        cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry.bootstrap();

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // The domain UI needs the same equipment/buff attack value as the server.
            net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE.setSyncable(true);
            Network.register();
            ForgeChunkManager.setForcedChunkLoadingCallback(MOD_ID,
                    (level, helper) -> CaveTokenLocations.get(level).ensureAllForcedChunks(level));
        });
    }

    public static CultivationData data(Player player) {
        return player == null ? null : player.getCapability(CultivationCapability.INSTANCE).orElse(null);
    }
    public static void sync(ServerPlayer player) {
        cn.blockforge.generated.generatedmod.network.CultivationSync.publish(player);
    }
    public static boolean isSpiritTide(long gameTime) { long phase = Math.floorMod(gameTime, TIDE_PERIOD); return phase >= TIDE_PERIOD - TIDE_LENGTH; }
    public static boolean isSpiritStorm(long gameTime) { long phase = Math.floorMod(gameTime, TIDE_PERIOD); return phase >= TIDE_PERIOD - TIDE_LENGTH - STORM_LENGTH && phase < TIDE_PERIOD - TIDE_LENGTH; }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        @SubscribeEvent public static void registerCapabilities(RegisterCapabilitiesEvent event) { event.register(CultivationData.class); }

        @SubscribeEvent public static void registerAttributes(net.minecraftforge.event.entity.EntityAttributeCreationEvent event) {
            event.put(ModEntities.SPIRIT_DEVOURER.get(),cn.blockforge.generated.generatedmod.entity.SpiritDevourerEntity.attributes().build());
            event.put(ModEntities.SOUL_BODY.get(), net.minecraft.world.entity.Mob.createMobAttributes().build());
            event.put(ModEntities.HEART_DEMON.get(), HeartDemonEntity.createAttributes().build());
            ModEntities.VISUAL_BOSSES.values().forEach(type -> event.put(type.get(),
                    cn.blockforge.generated.generatedmod.boss.VisualBossEntity.createAttributes().build()));
        }

    }

    /** 旧版注册表迁移属于 Forge 总线，不能和 Mod 总线生命周期事件放在同一订阅类中。 */
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeEvents {
        /** 将旧版铸剑台核心迁移到唯一的炼器台核心，避免旧存档出现缺失方块。 */
        @SubscribeEvent public static void remapLegacyForgeCore(MissingMappingsEvent event) {
            event.getMappings(ForgeRegistries.Keys.ITEMS, MOD_ID).stream()
                    .filter(mapping -> mapping.getKey().getPath().equals(LegacyDivineMigration.OLD_ID+"_fragment"))
                    .forEach(mapping -> mapping.remap(WuXingItems.RI_YUE_TING_LUN_FRAGMENT.get()));
            event.getMappings(ForgeRegistries.Keys.BLOCKS, MOD_ID).stream()
                    .filter(mapping -> mapping.getKey().getPath().equals("sword_forge_core"))
                    .forEach(mapping -> mapping.remap(WuXingBlocks.REFINING_FORGE_CORE.get()));
            // 旧版独立异火方块改为原版 FireBlock；避免旧存档加载成空气。
            event.getMappings(ForgeRegistries.Keys.BLOCKS, MOD_ID).stream()
                    .filter(mapping -> mapping.getKey().getPath().equals("eternal_spirit_fire"))
                    .forEach(mapping -> mapping.remap(net.minecraft.world.level.block.Blocks.FIRE));
            event.getMappings(ForgeRegistries.Keys.ITEMS, MOD_ID).stream()
                    .filter(mapping -> mapping.getKey().getPath().equals("sword_forge_core"))
                    .forEach(mapping -> mapping.remap(WuXingBlocks.REFINING_FORGE_CORE_ITEM.get()));
            event.getMappings(ForgeRegistries.Keys.ITEMS, MOD_ID).stream()
                    .filter(mapping -> mapping.getKey().getPath().equals("yi_hun_ji_po_fragment"))
                    .forEach(mapping -> mapping.remap(WuXingItems.YIN_FU_JING_YI_SHEN_FRAGMENT.get()));
        }
    }

    @SubscribeEvent public void divineBodySize(net.minecraftforge.event.entity.EntityEvent.Size event) {
        if (!(event.getEntity() instanceof Player player)
                || !player.isAddedToWorld()
                || !player.hasEffect(ModEffects.FA_TIAN_XIANG_DI.get())) return;
        // 巨体期间统一使用站立尺寸；下蹲尺寸会让客户端和服务端反复重算碰撞箱。
        event.setNewSize(event.getOriginalSize().scale(5.0F), true);
        event.setNewEyeHeight(player.getStandingEyeHeight(net.minecraft.world.entity.Pose.STANDING,
                event.getOriginalSize()) * 5.0F);
    }

    /** 法天象地期间跳跃高度改为 5 倍：高度与起跳速度平方成正比，故速度乘以 √5。 */
    @SubscribeEvent public void giantJump(net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent event) {
        LivingEntity living = event.getEntity();
        if (!(living instanceof Player) || !living.hasEffect(ModEffects.FA_TIAN_XIANG_DI.get())) return;
        Vec3 motion = living.getDeltaMovement();
        living.setDeltaMovement(motion.x, motion.y * 2.2360679775D, motion.z);
    }

    private static final UUID GIANT_REACH_MODIFIER = UUID.nameUUIDFromBytes(
            "wuxingxiantu:giant_reach".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    /** 法天象地期间触及距离（方块交互 / 实体攻击）改为 5 倍：3 格提升至 15 格。 */
    public static void applyGiantReach(ServerPlayer player) {
        for (net.minecraft.world.entity.ai.attributes.Attribute attribute
                : new net.minecraft.world.entity.ai.attributes.Attribute[]{
                net.minecraftforge.common.ForgeMod.BLOCK_REACH.get(),
                net.minecraftforge.common.ForgeMod.ENTITY_REACH.get()}) {
            net.minecraft.world.entity.ai.attributes.AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null || instance.getModifier(GIANT_REACH_MODIFIER) != null) continue;
            instance.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    GIANT_REACH_MODIFIER, "wuxingxiantu_giant_reach", 12.0D,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
        }
    }

    public static void removeGiantReach(ServerPlayer player) {
        for (net.minecraft.world.entity.ai.attributes.Attribute attribute
                : new net.minecraft.world.entity.ai.attributes.Attribute[]{
                net.minecraftforge.common.ForgeMod.BLOCK_REACH.get(),
                net.minecraftforge.common.ForgeMod.ENTITY_REACH.get()}) {
            net.minecraft.world.entity.ai.attributes.AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null && instance.getModifier(GIANT_REACH_MODIFIER) != null) {
                instance.removeModifier(GIANT_REACH_MODIFIER);
            }
        }
    }

    /** 炼器台：玩家摆上任一结构组件的瞬间检查是否成型。 */

    /** 炼丹炉：玩家摆放组件后检查 5x5x4 结构。 */

    /** 原版磨制黑石砖台阶没有模组钩子，用邻居更新兜住拆除与放置两种变化。 */

    /** 区块加载回调只恢复该区块的异火登记，并把洞府扫描延后到服务端 Tick。 */

    /** 区块卸载时清理运行时索引，但保留 SavedData，避免下次加载前读取默认方块状态。 */

    /** 铁砧落地并砸中灵石物品实体时，生成岳币这一彩蛋物品。 */

    /** 玩家击杀任何有经验掉落的生物时，额外生成等值的蓝色灵力球（拾取恢复灵力）。 */

    /** 体质元素伤害统一按伤害类型标签判定，避免技能各自重复实现。 */

    /** 剑阶压制提示的节流标记，避免连续挥砍刷屏。 */

    /** 为携带灵纹附魔的 ItemStack（含附魔书）追加每条附魔的功能说明。 */

    /** 为没有专属 tooltip 的本模组物品统一补充用途说明。 */

    /** Deliver generated items without losing them when the inventory is full. */
    public static void giveOrDrop(Player player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        if (!player.addItem(stack)) player.drop(stack, false);
    }

    /** 七十二变只继承生物的被动阵营关系，不继承该生物主动搜寻玩家的仇恨。 */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void transformedTargeting(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.Mob mob)
                || !(event.getNewTarget() instanceof ServerPlayer player)) return;
        String form = TransformAbilityRegistry.activeFormForTargeting(player);
        if (TransformAbilityRegistry.shouldIgnoreMobTarget(mob, player, form)) {
            // 用 null 明确拒绝这次主动锁定；不取消事件，避免旧目标状态被事件调用方保留。
            event.setNewTarget(null);
            return;
        }
        // 人皇幡魂仆替主人吸引仇恨：怪物原锁定玩家时，改写为锁定附近魂仆。
        if (!cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.isServant(mob)
                && mob instanceof net.minecraft.world.entity.monster.Enemy) {
            net.minecraft.world.entity.LivingEntity servant =
                    cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.findNearestServant(player, mob);
            if (servant != null) event.setNewTarget(servant);
        }
    }

    /** 异火缠绕点燃的目标生物：维护其异火燃烧状态。 */
    @SubscribeEvent public void exoticFireBurnTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        SpiritEnchantmentLogic.tickBurningExoticFire(event.getEntity());
    }

    /** 七十二变复用实体的火焰/溺水抗性；只处理当前已确认的服务端形态。 */

    @SubscribeEvent public void giantPoseTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !(event.player instanceof ServerPlayer player)
                || !player.hasEffect(ModEffects.FA_TIAN_XIANG_DI.get())) return;
        // 在玩家实体处理移动前锁定站姿，阻止 Shift 数据触发巨型碰撞箱重算。
        player.setShiftKeyDown(false);
        if (player.getPose() != net.minecraft.world.entity.Pose.STANDING) {
            player.setPose(net.minecraft.world.entity.Pose.STANDING);
        }
        // 触及距离 5 倍在效果存续期间保持挂载（死亡、换维度后自动补挂）。
        applyGiantReach(player);
    }

    /**
     * Vanilla 的和平难度清除会在 Forge AllowDespawn 事件之前直接删除敌对魂仆。
     * 世界 tick 期间临时使用非和平难度，让带持久化标记的魂仆正常跳过该分支，
     * 然后在本 tick 结束前恢复玩家看到的难度。
     */
    /**
     * 物品实体浸没在灵液中计时转化：煤炭满十秒转化为等量灵煤；
     * 骨粉每满三秒最多转化八份等量灵肥，并真实消耗一格灵液源。
     * 剩余骨粉保留在原实体重新计时，产物按批堆叠并交给原版合并；只查实体当前位置。
     */

    /**
     * 消耗一格灵液：只在实体包围盒真正覆盖的方块里找灵液<b>水源</b>（LEVEL=0），
     * 找到即挖掉并返回 true。检测（{@link #isInSpiritLiquid}）与消耗使用同一坐标集合，
     * 避免“岸边包围盒擦到水格、却按单列扫描找不到水”的判定不一致；流水格会被上游
     * 回填、不构成真实成本，因此不作为可消耗对象，找不到水源时返回 false 继续等待。
     */

    /**
     * 统一解析击杀归属，覆盖近战、箭矢/投射物、持续伤害以及原版保留的击杀信用。
     * 只有服务端玩家才能获得修为与灵力奖励，避免把生物或玩家误当作奖励对象。
     */

    public static boolean hasRoot(CultivationData data, int root) { return (data.wuxingxiantu$getRootMask() & (1 << root)) != 0; }

    public static boolean isSoulProjection(Player player) {
        CultivationData data = data(player);
        if (data == null) return false;
        String active = data.wuxingxiantu$getActiveDivineArt();
        return "yuan_shen_chu_qiao".equals(active) || "yi_hun_ji_po".equals(active);
    }

}
