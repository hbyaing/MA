
package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.HeartDemonEntity;
import cn.blockforge.generated.generatedmod.WuXingBlocks;
import cn.blockforge.generated.generatedmod.WuXingItems;
import cn.blockforge.generated.generatedmod.WuXingProfessions;
import cn.blockforge.generated.generatedmod.registry.ModEntities;
import cn.blockforge.generated.generatedmod.registry.ModBlockEntities;
import cn.blockforge.generated.generatedmod.registry.ModFluids;
import cn.blockforge.generated.generatedmod.registry.ModMenus;
import cn.blockforge.generated.generatedmod.registry.ModRecipes;
import cn.blockforge.generated.generatedmod.items.FireSeedItem;
import cn.blockforge.generated.generatedmod.ExoticFireColorRegistry;
import cn.blockforge.generated.generatedmod.ExoticFireState;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.client.renderer.RenderType;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.SheepFurLayer;
import net.minecraft.world.entity.npc.Villager;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** 客户端实体渲染器注册。 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModClientEvents {
    private ModClientEvents() {}

    @SubscribeEvent
    public static void timeStopReload(net.minecraftforge.client.event.RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener)
                manager -> TimeStopOverlay.reload());
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.HERB_PROCESSING_TABLE.get(),HerbProcessingTableScreen::new);
            MenuScreens.register(ModMenus.ALCHEMY_RESEARCH_TABLE.get(),ResearchTableScreen::new);
            MenuScreens.register(ModMenus.INSCRIPTION_TABLE.get(),InscriptionScreen::new);
            MenuScreens.register(ModMenus.REFORGE_TABLE.get(),ReforgeScreen::new);
            net.minecraft.client.renderer.item.ItemProperties.register(WuXingItems.TALISMAN.get(),
                    new ResourceLocation(GeneratedMod.MOD_ID,"talisman_kind"),(stack,level,entity,seed)->
                            cn.blockforge.generated.generatedmod.TalismanCatalog.modelCode(cn.blockforge.generated.generatedmod.TalismanData.getTalisman(stack)));
            net.minecraft.client.renderer.item.ItemProperties.register(net.minecraft.world.item.Items.ENCHANTED_BOOK,
                    ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID, "spirit_book"),
                    (stack, level, entity, seed) -> {
                        for (var raw : net.minecraft.world.item.EnchantedBookItem.getEnchantments(stack)) {
                            if (!(raw instanceof net.minecraft.nbt.CompoundTag entry)) continue;
                            var id = ResourceLocation.tryParse(entry.getString("id"));
                            if (id != null && GeneratedMod.MOD_ID.equals(id.getNamespace()) && entry.getInt("lvl") > 0) return 1F;
                        }
                        return 0F;
                    });
            WuXingClientPackets.register();
            net.minecraft.client.renderer.item.ItemProperties.register(WuXingItems.ALCHEMY_FORMULA.get(),
                    new ResourceLocation(GeneratedMod.MOD_ID, "aura_formula"), (stack, level, entity, seed) ->
                            stack.hasTag() && "wuxingxiantu:aura_pill".equals(stack.getTag().getString("FormulaId")) ? 1F : 0F);
            MenuScreens.register(ModMenus.ROBE.get(),RobeScreen::new);
            MenuScreens.register(ModMenus.XUANLING_ANVIL.get(), XuanlingAnvilScreen::new);
            ItemBlockRenderTypes.setRenderLayer(WuXingBlocks.EXOTIC_FIRE_SOURCE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(WuXingBlocks.PEACH_LEAVES.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(WuXingBlocks.PEACH_SAPLING.get(), RenderType.cutout());
            for (var plant : cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry.PLANT_BLOCKS) {
                ItemBlockRenderTypes.setRenderLayer(plant.get(), RenderType.cutout());
            }
            ItemBlockRenderTypes.setRenderLayer(WuXingBlocks.PEACH_DOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(WuXingBlocks.PEACH_TRAPDOOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(WuXingBlocks.SPIRIT_LIQUID.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(WuXingBlocks.REFINING_FORGE_CORE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(WuXingBlocks.RUNE_STONE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(WuXingBlocks.ETERNAL_SPIRIT_FIRE.get(), RenderType.cutout());
            for (net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.Block> fire : WuXingBlocks.EXOTIC_FIRES) {
                ItemBlockRenderTypes.setRenderLayer(fire.get(), RenderType.cutout());
            }
            ItemBlockRenderTypes.setRenderLayer(ModFluids.SPIRIT_LIQUID_SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.SPIRIT_LIQUID_FLOWING.get(), RenderType.translucent());
            MenuScreens.register(ModMenus.SOUL_FURNACE.get(), SoulFurnaceScreen::new);
            MenuScreens.register(ModMenus.SOUL_HEALING_ROOM.get(), SoulHealingRoomScreen::new);
            MenuScreens.register(ModMenus.HUMAN_EMPEROR_BANNER.get(), HumanEmperorBannerScreen::new);
            MenuScreens.register(ModMenus.TALISMAN_PLATFORM.get(), TalismanPlatformScreen::new);
            MenuScreens.register(ModMenus.ALCHEMY_FURNACE.get(), AlchemyScreen::new);
            ItemBlockRenderTypes.setRenderLayer(WuXingBlocks.ENGRAVING_PLATFORM.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(WuXingBlocks.RUBBING_PLATFORM.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(WuXingBlocks.DRAWING_PLATFORM.get(), RenderType.cutout());
        });
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(cn.blockforge.generated.generatedmod.ModParticles.GOLDEN_SOUL_FLAME.get(),
                net.minecraft.client.particle.FlameParticle.Provider::new);
        cn.blockforge.generated.generatedmod.ModParticles.ART_MATERIALS.forEach((id, type) ->
                event.registerSpriteSet(type.get(), sprites ->
                        new cn.blockforge.generated.generatedmod.client.arts.ArtMaterialParticle.Provider(id, sprites)));
        event.registerSpriteSet(cn.blockforge.generated.generatedmod.ModParticles.EXOTIC_FIRE_CORE.get(),
                sprites -> new CodeFireParticleProvider(sprites, CodeFireParticle.Kind.CORE));
        event.registerSpriteSet(cn.blockforge.generated.generatedmod.ModParticles.EXOTIC_FIRE_OUTER.get(),
                sprites -> new CodeFireParticleProvider(sprites, CodeFireParticle.Kind.FLAME));
        event.registerSpriteSet(cn.blockforge.generated.generatedmod.ModParticles.EXOTIC_FIRE_EMBER.get(),
                sprites -> new CodeFireParticleProvider(sprites, CodeFireParticle.Kind.EMBER));
        event.registerSpriteSet(cn.blockforge.generated.generatedmod.ModParticles.EXOTIC_FIRE_FLAME.get(),
                sprites -> new CodeFireParticleProvider(sprites, CodeFireParticle.Kind.FLAME));
        event.registerSpriteSet(cn.blockforge.generated.generatedmod.ModParticles.EXOTIC_FIRE_DROPLET.get(),
                sprites -> {
                    // 环形缠绕水滴由发射器直接构造，不走 Provider，图集在这里一并注入。
                    ExoticFireRingParticle.initSprites(sprites);
                    return new ExoticFireDropletParticleProvider(sprites);
                });
        event.registerSpriteSet(cn.blockforge.generated.generatedmod.ModParticles.HEART_SWORD_SPARK.get(),
                HeartSwordParticleProvider::new);
        event.registerSpriteSet(cn.blockforge.generated.generatedmod.ModParticles.HEART_MOTE.get(),
                HeartSwordParticleProvider::new);
        event.registerSpriteSet(cn.blockforge.generated.generatedmod.ModParticles.HEART_FLAME.get(),
                HeartSwordParticleProvider::new);
    }

    @SubscribeEvent
    public static void registerSpiritFireTextures(net.minecraftforge.client.event.RegisterTextureAtlasSpriteLoadersEvent event) {
        for (int tier = 0; tier <= 9; tier++) {
            event.register("spirit_fire_" + tier, new SpiritFireSpriteLoader(tier));
        }
    }

    /** 火种瓶和九种已注册异火方块共用 Spiritfire 风格贴图，图层按品阶动态染色。 */
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            if (tintIndex != 1 || !(stack.getItem() instanceof FireSeedItem seed) || seed.tier() <= 0) {
                return 0xFFFFFFFF;
            }
            long time = Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
            return 0xFF000000 | ExoticFireColorRegistry.color(cn.blockforge.generated.generatedmod.fire.FireBottleData.tier(stack), time);
        },
                WuXingItems.EMPTY_FIRE_SEED_BOTTLE.get(), WuXingItems.FIRE_SEED_RED_FLOW.get(),
                WuXingItems.FIRE_SEED_MOLTEN_GOLD.get(), WuXingItems.FIRE_SEED_COLD_ASH.get(),
                WuXingItems.FIRE_SEED_AZURE_WOOD.get(), WuXingItems.FIRE_SEED_COBALT_SEA.get(),
                WuXingItems.FIRE_SEED_VIOLET_THUNDER.get(), WuXingItems.FIRE_SEED_SUN_SAINT.get(),
                WuXingItems.FIRE_SEED_NETHER_VOID.get(), WuXingItems.FIRE_SEED_NIRVANA.get());

        // 异火方块物品使用已调色的动画图集，不再进行第二次乘色。
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SOUL_BODY.get(), SoulBodyRenderer::new);
        event.registerEntityRenderer(ModEntities.SPIRIT_DEVOURER.get(),SpiritDevourerRenderer::new);
        event.registerEntityRenderer(ModEntities.CONTROLLED_SWORD.get(), ControlledSwordRenderer::new);
        event.registerEntityRenderer(ModEntities.MEDITATION_SEAT.get(), MeditationSeatRenderer::new);
        event.registerEntityRenderer(ModEntities.HEART_DEMON.get(), HeartDemonRenderer::new);
        ModEntities.VISUAL_BOSSES.forEach((id, type) -> event.registerEntityRenderer(type.get(), context ->
                new cn.blockforge.generated.generatedmod.client.boss.BossGeoRenderer<>(context,
                        cn.blockforge.generated.generatedmod.boss.BossVisualRegistry.get(id))));
        event.registerEntityRenderer(ModEntities.OFFERING_ITEM.get(),
                OfferingItemRenderer::new);
        event.registerEntityRenderer(ModEntities.SPIRIT_ORB.get(), SpiritOrbRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.REFINING_FORGE.get(), RefiningForgeBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SPIRIT_PLATFORM.get(),
                PedestalFloatingRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHENGHUA_PEDESTAL.get(),
                PedestalFloatingRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HUMAN_EMPEROR_BANNER_STAND.get(),
                HumanEmperorBannerStandRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ALCHEMY_FURNACE.get(),
                AlchemyFurnaceBlockEntityRenderer::new);
        // 七十二变/附身鱼类形态：原版鳕鱼、鲑鱼、热带鱼、河豚、蝌蚪、海龟的模型与渲染器
        // 从不应用俯仰角，鱼在水中永远水平滑行。这里用子类渲染器覆盖原版注册，补上
        // 绕鱼身中心的“竖直游泳”俯仰旋转（低头下潜、抬头上浮）。
        event.registerEntityRenderer(EntityType.COD, VerticalSwimFishRenderer.TiltingCod::new);
        event.registerEntityRenderer(EntityType.SALMON, VerticalSwimFishRenderer.TiltingSalmon::new);
        event.registerEntityRenderer(EntityType.TROPICAL_FISH, VerticalSwimFishRenderer.TiltingTropicalFish::new);
        event.registerEntityRenderer(EntityType.PUFFERFISH, VerticalSwimFishRenderer.TiltingPufferfish::new);
        event.registerEntityRenderer(EntityType.TADPOLE, VerticalSwimFishRenderer.TiltingTadpole::new);
        event.registerEntityRenderer(EntityType.TURTLE, VerticalSwimFishRenderer.TiltingTurtle::new);
    }

    /**
     * 为自定义配方类型注册配方书分类。炼器/炼丹配方由各自的 GUI 展示，
     * 归入 {@link RecipeBookCategories#UNKNOWN} 即可消除原版配方书加载时
     * "Unknown recipe category" 的告警，同时不会污染工作台配方书面板。
     */
    @SubscribeEvent
    public static void registerRecipeBookCategories(
            net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent event) {
        event.registerRecipeCategoryFinder(ModRecipes.REFINING_TYPE.get(),
                recipe -> RecipeBookCategories.UNKNOWN);
        event.registerRecipeCategoryFinder(ModRecipes.ALCHEMY_TYPE.get(),
                recipe -> RecipeBookCategories.UNKNOWN);
    }

    /**
     * 把人皇幡物品模型替换成带使用态补偿的包装模型：手持释放光线时幡杆逐帧校正为
     * 竖直向上（配合 UseAnim.BOW 的水平手臂姿态），见 {@link HumanEmperorBannerPoseModel}。
     */
    @SubscribeEvent
    public static void wrapBannerPoseModel(net.minecraftforge.client.event.ModelEvent.ModifyBakingResult event) {
        net.minecraft.resources.ResourceLocation id =
                new net.minecraft.resources.ResourceLocation(GeneratedMod.MOD_ID, "item/human_emperor_banner");
        net.minecraft.client.resources.model.BakedModel model = event.getModels().get(id);
        if (model != null && !(model instanceof HumanEmperorBannerPoseModel)) {
            event.getModels().put(id, new HumanEmperorBannerPoseModel(model));
        }
    }

    @Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ClientForgeEvents {
        private static boolean giantDimensionsActive;
        private static final Map<java.util.UUID, LivingEntity> DISGUISE_ENTITIES = new HashMap<>();
        private static final Map<java.util.UUID, Integer> DISGUISE_ANIMATION_TICKS = new HashMap<>();
        private static final Map<java.util.UUID, Integer> DISGUISE_ATTACK_REQUESTS = new HashMap<>();
        private static final Map<java.util.UUID, Integer> DISGUISE_CREEPER_PRIMES = new HashMap<>();
        private static final Map<java.util.UUID, Boolean> DISGUISE_ATTACKING = new HashMap<>();
        /** 潜影贝发射潜影贝弹 / 挥击后，壳保持张开的截止游戏刻；用于驱动“打开潜影壳”的动画。 */
        private static final Map<java.util.UUID, Integer> DISGUISE_SHULKER_PEEK_UNTIL = new HashMap<>();
        /** 潜影贝伪装壳的当前开合量 0.0~1.0，由客户端直接写入 currentPeekAmount 字段驱动模型，不依赖原版 DATA_PEEK 链路。 */
        private static final Map<java.util.UUID, Float> DISGUISE_SHULKER_PEEK = new HashMap<>();
        private static final Map<Class<?>, Field[]> MODEL_PART_FIELDS = new HashMap<>();
        private static final Map<Class<?>, Field[]> NESTED_MODEL_FIELDS = new HashMap<>();
        private static final Field[] RENDER_LAYER_LIST_FIELDS = findFields(LivingEntityRenderer.class,
                java.util.List.class);
        private static long lastFireTintRefresh = Long.MIN_VALUE;
        private static long lastExoticParticleTick = Long.MIN_VALUE;
        private static Field particleBucketsField;

        private ClientForgeEvents() {}


        /** 保留原版火焰粒子；异火状态只通过方块颜色和附加特效体现。 */
        @SubscribeEvent
        public static void replaceExoticFireParticles(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
            // 不删除 vanilla FireBlock 的 FlameParticle/SmokeParticle，避免普通火焰消失。
        }

        /** 为尚未满意的品阶补充与名称对应的视觉符号；4、8、9阶保持上一版粒子表现。 */
        private static void spawnTierAccent(Minecraft client, BlockPos pos, int tier, int color, long time) {
            if (tier == 4 || tier == 8 || tier == 9) return;
            net.minecraft.util.RandomSource random = client.level.random;
            double x = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
            double y = pos.getY() + 0.45D + random.nextDouble() * 0.8D;
            double z = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;
            int accent = ExoticFireColorRegistry.accentColor(tier, time);
            switch (tier) {
                case 1 -> {
                    if (random.nextInt(3) == 0) addColoredDust(client, accent, 0.42F, x, y, z,
                            (random.nextDouble() - 0.5D) * 0.08D, 0.045D + random.nextDouble() * 0.05D,
                            (random.nextDouble() - 0.5D) * 0.08D);
                }
                case 2 -> addColoredDust(client, accent, 0.5F, x, y, z,
                        (random.nextDouble() - 0.5D) * 0.045D, 0.035D + random.nextDouble() * 0.05D,
                        (random.nextDouble() - 0.5D) * 0.045D);
                case 3 -> {
                    if (random.nextInt(2) == 0) client.level.addParticle(ParticleTypes.ASH, x, y, z,
                            (random.nextDouble() - 0.5D) * 0.012D, 0.012D + random.nextDouble() * 0.018D,
                            (random.nextDouble() - 0.5D) * 0.012D);
                }
                case 5 -> {
                    if (random.nextInt(2) == 0) client.level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z,
                            (random.nextDouble() - 0.5D) * 0.018D, 0.012D + random.nextDouble() * 0.025D,
                            (random.nextDouble() - 0.5D) * 0.018D);
                    addColoredDust(client, accent, 0.3F, x, y, z,
                            (random.nextDouble() - 0.5D) * 0.018D, 0.02D, (random.nextDouble() - 0.5D) * 0.018D);
                }
                case 6 -> {
                    client.level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z,
                            (random.nextDouble() - 0.5D) * 0.09D, 0.03D + random.nextDouble() * 0.06D,
                            (random.nextDouble() - 0.5D) * 0.09D);
                    addColoredDust(client, accent, 0.34F, x, y, z,
                            (random.nextDouble() - 0.5D) * 0.04D, 0.04D, (random.nextDouble() - 0.5D) * 0.04D);
                }
                case 7 -> {
                    if (random.nextInt(2) == 0) client.level.addParticle(ParticleTypes.END_ROD, x, y, z,
                            (random.nextDouble() - 0.5D) * 0.012D, 0.025D + random.nextDouble() * 0.035D,
                            (random.nextDouble() - 0.5D) * 0.012D);
                    addColoredDust(client, accent, 0.38F, x, y, z,
                            (random.nextDouble() - 0.5D) * 0.02D, 0.03D, (random.nextDouble() - 0.5D) * 0.02D);
                }
                default -> { }
            }
        }

        private static void addColoredDust(Minecraft client, int color, float scale, double x, double y, double z,
                                           double vx, double vy, double vz) {
            client.level.addParticle(new DustParticleOptions(ExoticFireColorRegistry.vector(color), scale),
                    x, y, z, vx, vy, vz);
        }

        private static void removeVanillaFireParticles(Minecraft client,
                                                         java.util.Map<BlockPos, Integer> fires) {
            try {
                if (particleBucketsField == null) {
                    particleBucketsField = net.minecraft.client.particle.ParticleEngine.class
                            .getDeclaredField("particles");
                    particleBucketsField.setAccessible(true);
                }
                Object value = particleBucketsField.get(client.particleEngine);
                if (!(value instanceof java.util.Map<?, ?> buckets)) return;
                for (Object bucket : buckets.values()) {
                    if (!(bucket instanceof java.util.Collection<?> particles)) continue;
                    particles.removeIf(object -> object instanceof Particle particle
                            && (particle instanceof FlameParticle || particle instanceof SmokeParticle)
                            && isNearExoticFire(particle, fires));
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // 某些安全环境可能禁止反射；方块本体的颜色替换仍然有效。
            }
        }

        private static boolean isNearExoticFire(Particle particle,
                                                 java.util.Map<BlockPos, Integer> fires) {
            net.minecraft.world.phys.Vec3 point = particle.getPos();
            BlockPos particleBlock = BlockPos.containing(point.x, point.y, point.z);
            for (BlockPos pos : BlockPos.betweenClosed(particleBlock.offset(-1, -1, -1),
                    particleBlock.offset(1, 1, 1))) {
                if (fires.containsKey(pos) || ExoticFireState.clientVisualTier(clientLevel(), pos) > 0) {
                    double dx = point.x - (pos.getX() + 0.5D);
                    double dz = point.z - (pos.getZ() + 0.5D);
                    double dy = point.y - pos.getY();
                    if (dx * dx + dz * dz <= 1.7D && dy >= -0.25D && dy <= 2.8D) return true;
                }
            }
            return false;
        }

        private static net.minecraft.client.multiplayer.ClientLevel clientLevel() {
            return Minecraft.getInstance().level;
        }

        /** 服务端同步解除后立即丢弃伪装实体，避免下次变身复用旧模型动画状态。 */
        public static void clearDisguise(java.util.UUID playerId) {
            DISGUISE_ENTITIES.remove(playerId);
            DISGUISE_ANIMATION_TICKS.remove(playerId);
            DISGUISE_ATTACK_REQUESTS.remove(playerId);
            DISGUISE_CREEPER_PRIMES.remove(playerId);
            DISGUISE_ATTACKING.remove(playerId);
            DISGUISE_SHULKER_PEEK_UNTIL.remove(playerId);
            DISGUISE_SHULKER_PEEK.remove(playerId);
        }

        /** 记录一次由服务端校验的变身左键，渲染帧只消费请求，不重复重播动画。 */
        public static void requestDisguiseAttack(net.minecraft.client.player.LocalPlayer player) {
            DISGUISE_ATTACK_REQUESTS.put(player.getUUID(), player.tickCount + 1);
        }

        private static boolean consumeDisguiseAttack(java.util.UUID playerId) {
            return DISGUISE_ATTACK_REQUESTS.remove(playerId) != null;
        }

        public static void requestDisguiseCreeperPrime(net.minecraft.client.player.LocalPlayer player) {
            DISGUISE_CREEPER_PRIMES.put(player.getUUID(), player.tickCount + 12);
        }

        /** 潜影贝发射潜影贝弹（按 Y / 左键挥击）时打开潜影壳的动画窗口；窗口内壳张开，之后按原版节奏合上。 */
        public static void requestDisguiseShulkerPeek(net.minecraft.client.player.LocalPlayer player) {
            DISGUISE_SHULKER_PEEK_UNTIL.put(player.getUUID(), player.tickCount + 20);
        }

        @SubscribeEvent
        public static void tickDisguiseEntities(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
            if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || DISGUISE_DISABLED) return;
            LivingEntity fake = DISGUISE_ENTITIES.get(client.player.getUUID());
            if (!(fake instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon)) return;
            CultivationData data = GeneratedMod.data(client.player);
            if (data == null || !"qi_shi_er_bian".equals(data.wuxingxiantu$getActiveDivineArt())
   || !"minecraft:ender_dragon".equals(data.wuxingxiantu$getActivePossessedForm())) return;
            dragon.setPos(client.player.getX(), client.player.getY(), client.player.getZ());
            // 原版 EnderDragonRenderer 的龙身朝向完全取自 dragon.positions 里的“延迟转向历史”
            // （头用最新一格、身体滞后约 7 格），根本不读实体 yRot；伪装龙不跑 aiStep，历史缓冲
            // 从未写入，模型才会只平移不转向。这里按原版节奏每 tick 推进一格环形缓冲，并对
            // 玩家视角的急转做每 tick 限速平滑，龙就会像原版那样甩头、弯颈、整条龙弧形转向。
            double[][] history = dragon.positions;
            float wantedYaw = net.minecraft.util.Mth.wrapDegrees(client.player.getYRot() + 180.0F);
            if (dragon.posPointer >= 0) {
                float previousYaw = (float) history[dragon.posPointer][0];
                float delta = net.minecraft.util.Mth.clamp(net.minecraft.util.Mth.wrapDegrees(wantedYaw - previousYaw),
                        -15.0F, 15.0F);
                wantedYaw = net.minecraft.util.Mth.wrapDegrees(previousYaw + delta);
            }
            dragon.setYRot(wantedYaw);
            dragon.setXRot(client.player.getXRot());
            dragon.setYHeadRot(wantedYaw);
            dragon.setYBodyRot(wantedYaw);
            if (dragon.posPointer < 0) {
                for (int i = 0; i < history.length; i++) {
                    history[i][0] = wantedYaw;
                    history[i][1] = dragon.getY();
                }
            }
            if (++dragon.posPointer == history.length) dragon.posPointer = 0;
            history[dragon.posPointer][0] = wantedYaw;
            history[dragon.posPointer][1] = dragon.getY();
            dragon.tickCount = client.player.tickCount;

            dragon.oFlapTime = dragon.flapTime;
            dragon.flapTime += client.player.getDeltaMovement().horizontalDistance() > 0.001D ? 0.12F : 0.035F;
            Integer requestTick = DISGUISE_ATTACK_REQUESTS.remove(client.player.getUUID());
            if (requestTick != null && dragon.getPhaseManager().getCurrentPhase().isSitting()) {
                dragon.getPhaseManager().setPhase(net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase.HOVERING);
            }
        }

        /** 兜底：即使玩家站桩或打开界面（移动输入被抑制），效果消失后也在客户端 tick 立即复原视角。 */
        @SubscribeEvent
        public static void restoreGiantCamera(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
            if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
            net.minecraft.client.player.LocalPlayer player =
   net.minecraft.client.Minecraft.getInstance().player;
            if (player == null || !giantDimensionsActive) return;
            if (player.hasEffect(cn.blockforge.generated.generatedmod.ModEffects.FA_TIAN_XIANG_DI.get())) return;
            player.refreshDimensions();
            giantDimensionsActive = false;
        }

        @SubscribeEvent
        public static void stabilizeGiantPose(MovementInputUpdateEvent event) {
            if (!(event.getEntity() instanceof net.minecraft.client.player.LocalPlayer player)) return;
            CultivationData data = GeneratedMod.data(player);
            boolean giant = data != null && "fa_tian_xiang_di".equals(data.wuxingxiantu$getActiveDivineArt())
   && player.hasEffect(cn.blockforge.generated.generatedmod.ModEffects.FA_TIAN_XIANG_DI.get());
            if (!giant) {
                // 神通结束（效果消失或提前停止）时立即重算尺寸与视角，不再等下蹲触发。
                if (giantDimensionsActive) {
   player.refreshDimensions();
   giantDimensionsActive = false;
                }
                return;
            }
            event.getInput().shiftKeyDown = false;
            player.setShiftKeyDown(false);
            if (player.getPose() != Pose.STANDING) player.setPose(Pose.STANDING);
            if (!giantDimensionsActive) {
                player.refreshDimensions();
                giantDimensionsActive = true;
            }
        }

        @SubscribeEvent
        public static void hideSpiritCultivatorHat(RenderLivingEvent.Pre<?, ?> event) {
            if (!(event.getEntity() instanceof Villager villager)
   || (villager.getVillagerData().getProfession() != WuXingProfessions.SPIRIT_CULTIVATOR.get()
       && villager.getVillagerData().getProfession() != WuXingProfessions.TAOIST.get())
   || !(event.getRenderer().getModel() instanceof VillagerModel<?> model)) return;

            ModelPart head = model.getHead();
            head.getChild("hat").skipDraw = true;
            head.getChild("hat").getChild("hat_rim").skipDraw = true;
        }

        @SubscribeEvent
        public static void restoreVillagerHat(RenderLivingEvent.Post<?, ?> event) {
            if (!(event.getEntity() instanceof Villager villager)
   || (villager.getVillagerData().getProfession() != WuXingProfessions.SPIRIT_CULTIVATOR.get()
       && villager.getVillagerData().getProfession() != WuXingProfessions.TAOIST.get())
   || !(event.getRenderer().getModel() instanceof VillagerModel<?> model)) return;

            ModelPart head = model.getHead();
            head.getChild("hat").skipDraw = false;
            head.getChild("hat").getChild("hat_rim").skipDraw = false;
        }

        /** 灵修者被僵尸感染后外观：感染成僵尸村民时同样隐藏道袍头巾，保持与灵修者一致的服饰。 */
        @SubscribeEvent
        public static void hideSpiritCultivatorZombieHat(RenderLivingEvent.Pre<?, ?> event) {
            if (!(event.getEntity() instanceof net.minecraft.world.entity.monster.ZombieVillager zombie)
    || zombie.getVillagerData().getProfession() != WuXingProfessions.SPIRIT_CULTIVATOR.get()
    || !(event.getRenderer().getModel() instanceof net.minecraft.client.model.ZombieVillagerModel model)) return;
            model.hatVisible(false);
        }

        @SubscribeEvent
        public static void restoreSpiritCultivatorZombieHat(RenderLivingEvent.Post<?, ?> event) {
            if (!(event.getEntity() instanceof net.minecraft.world.entity.monster.ZombieVillager zombie)
    || zombie.getVillagerData().getProfession() != WuXingProfessions.SPIRIT_CULTIVATOR.get()
    || !(event.getRenderer().getModel() instanceof net.minecraft.client.model.ZombieVillagerModel model)) return;
            model.hatVisible(true);
        }

        @SubscribeEvent
        public static void openSoulTradeScreen(ScreenEvent.Opening event) {
            if (!(event.getNewScreen() instanceof MerchantScreen screen)
   || screen instanceof SoulTradeScreen
   || !(screen.getTitle().getContents() instanceof TranslatableContents contents)
   || !"screen.wuxingxiantu.soul_trade".equals(contents.getKey())) return;
            var player = Minecraft.getInstance().player;
            if (player == null) return;
            event.setNewScreen(new SoulTradeScreen(screen.getMenu(), player.getInventory(), screen.getTitle()));
        }

        @SubscribeEvent
        public static void renderSeventyTwoChanges(RenderPlayerEvent.Pre event) {
            try {
                renderSeventyTwoChangesInternal(event);
            } catch (Throwable throwable) {
                // 伪装渲染出过一次问题就整体停用，恢复普通玩家模型，保证游戏不再被渲染异常崩掉。
                DISGUISE_DISABLED = true;
                DISGUISE_ENTITIES.clear();
                DISGUISE_ANIMATION_TICKS.clear();
                org.slf4j.LoggerFactory.getLogger("WuxingDisguise")
       .warn("七十二变伪装渲染异常，已停用伪装模型", throwable);
            }
        }

        private static boolean DISGUISE_DISABLED;

        private static void renderSeventyTwoChangesInternal(RenderPlayerEvent.Pre event) {
            if (DISGUISE_DISABLED) return;
            var player = event.getEntity();
            CultivationData data = GeneratedMod.data(player);
            if (data == null || !"qi_shi_er_bian".equals(data.wuxingxiantu$getActiveDivineArt())) {
                clearDisguise(player.getUUID());
                return;
            }
            String form = data.wuxingxiantu$getActivePossessedForm();
            if (form == null || form.isBlank()) {
                clearDisguise(player.getUUID());
                return;
            }
            ResourceLocation key = ResourceLocation.tryParse(form);
            if (key == null) return;
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(key);
            if (type == null || type == EntityType.PLAYER) return;
            Entity raw = DISGUISE_ENTITIES.get(player.getUUID());
            LivingEntity fake = raw instanceof LivingEntity living ? living : null;
            if (fake == null || fake.getType() != type || fake.level() != player.level()) {
                Entity created = type.create(player.level());
                if (!(created instanceof LivingEntity living)) {
                        DISGUISE_ENTITIES.remove(player.getUUID());
                     return;
                 }
                 fake = living;
                DISGUISE_ENTITIES.put(player.getUUID(), fake);
            }
            if (fake instanceof HeartDemonEntity heartDemon) {
                heartDemon.bindClientOwner(player.getUUID(), player.getGameProfile().getName());
                heartDemon.clientTransformPhase();
            }
            if(fake instanceof cn.blockforge.generated.generatedmod.boss.BossVisualEntity visual)cn.blockforge.generated.generatedmod.client.boss.BossTransformMotions.apply(player.getUUID(),form,visual);
            fake.setPos(player.getX(), player.getY(), player.getZ());
            fake.xo = player.xo;
            fake.yo = player.yo;
            fake.zo = player.zo;
            fake.xOld = player.xOld;
            fake.yOld = player.yOld;
            fake.zOld = player.zOld;
            fake.setYRot(player.getYRot());
            fake.setXRot(player.getXRot());
            // 同步身体、头部和历史旋转；只同步 yHeadRot 会让羊头相对身体转到极端角度，
            // 在羊模型的头部动画/羊毛层叠加后看起来像“头消失”。
            fake.setYHeadRot(player.getYHeadRot());
            fake.setYBodyRot(player.yBodyRot);
            fake.yRotO = player.yRotO;
            fake.xRotO = player.xRotO;
            fake.yHeadRotO = player.yHeadRotO;
            fake.yBodyRotO = player.yBodyRotO;
            fake.tickCount = player.tickCount;
            fake.setDeltaMovement(player.getDeltaMovement());
            fake.setOnGround(player.onGround());
            // 伪装假实体从不调度 tick，isInWater() 会永远停在 false：鱼模型既一直播放
            // 离水扑腾动画，竖直游泳俯仰也会被跳过。baseTick 在客户端只刷新流体/火等
            // 状态（空气扣减与挤压伤害都有 isClientSide 守卫），同步时调用一次是安全的。
            if (isAquaticForm(fake)) fake.baseTick();
            Integer lastTick = DISGUISE_ANIMATION_TICKS.put(player.getUUID(), player.tickCount);
            if (lastTick == null || lastTick.intValue() != player.tickCount) {
                fake.walkAnimation.update(player.walkAnimation.speed(), 1.0F);
                if (fake instanceof Creeper creeper) {
                    Integer primeUntil = DISGUISE_CREEPER_PRIMES.get(player.getUUID());
                    if (primeUntil != null && primeUntil >= player.tickCount) {
                        // 不调用 ignite()（那会锁定点燃态，松开后无法再回退）；只推进膨胀方向。
                        creeper.setSwellDir(1);
                        creeper.tick();
                    } else {
                        // 松开 Y / 引爆后让伪装苦力怕的膨胀动画回退，避免模型卡在鼓包状态。
                        DISGUISE_CREEPER_PRIMES.remove(player.getUUID());
                        creeper.setSwellDir(-1);
                        creeper.tick();
                    }
                }
                // 守卫者/远古守卫者的摆尾、棘刺张合与游动气泡全部由 Guardian.aiStep() 的客户端
                // 分支推进（原版守卫者在 LivingEntity.tick 里每个客户端游戏刻调用一次）；伪装假实
                // 体从不调度 tick，clientSideTailAnimation 恒为 0，模型便一动不动。这里每个游戏刻
                // 手动推进一次，并按玩家游动速度补设 MOVING 数据标记——游动时摆尾加快、棘刺贴身
                // 并吐出气泡，静止时慢摆、棘刺张开，与原版守卫者表现一致。
                if (fake instanceof net.minecraft.world.entity.monster.Guardian guardian) {
                    setGuardianMoving(guardian, player.walkAnimation.speed() > 0.01F);
                    guardian.aiStep();
                    // aiStep 末尾会走 LivingEntity.travel（本地实体受控），重力/浮力会让 fake 在
                    // 这个刻内产生亚格位移；随即按玩家位置与速度回正，避免动画推进的那一帧身体
                    // 出现半格抖动。
                    guardian.setPos(player.getX(), player.getY(), player.getZ());
                    guardian.setDeltaMovement(player.getDeltaMovement());
                }
                // 潜影贝的壳开合量 currentPeekAmount 只由原版 Shulker.tick() 在客户端推进；伪装假
                // 实体不跑 AI，currentPeekAmount 恒为 0，模型便永远闭壳。原版 updatePeekAmount 每
                // 刻只推进 0.05（需 20 刻才能完全张开），攻击的刹那看不出壳动；且 setupAnim 里壳
                // 张过半后还会叠加 sin(partialTick*0.1)*0.7 的周期抖动，看起来像在原地左右摆动。
                // 这里改为直接写 currentPeekAmount / currentPeekAmountO 字段：攻击时快速张开、
                // 平时缓慢合拢，开合量完全由客户端控制，壳的开合动画才真正到位。
                if (fake instanceof net.minecraft.world.entity.monster.Shulker shulker) {
                    boolean peekWindow = player.tickCount < DISGUISE_SHULKER_PEEK_UNTIL.getOrDefault(player.getUUID(), 0);
                    boolean skewering = player.swinging || player.attackAnim > 0.01F
                            || consumeDisguiseAttack(player.getUUID());
                    boolean open = peekWindow || skewering;
                    float current = DISGUISE_SHULKER_PEEK.getOrDefault(player.getUUID(), 0.0F);
                    current = open ? Math.min(1.0F, current + 0.15F) : Math.max(0.0F, current - 0.10F);
                    DISGUISE_SHULKER_PEEK.put(player.getUUID(), current);
                    setShulkerPeekFields(shulker, current);
                    shulker.setPos(player.getX(), player.getY(), player.getZ());
                    shulker.setDeltaMovement(player.getDeltaMovement());
                }
            }
            if (fake instanceof net.minecraft.world.entity.monster.warden.Warden warden) {
                boolean attacking = player.swinging || player.attackAnim > 0.01F
                        || consumeDisguiseAttack(player.getUUID());
                boolean wasAttacking = DISGUISE_ATTACKING.getOrDefault(player.getUUID(), false);
                if (attacking && !wasAttacking) warden.attackAnimationState.start(player.tickCount);
                DISGUISE_ATTACKING.put(player.getUUID(), attacking);
            }
            // 鱼类变身在水中使用原版游泳姿态，离水后恢复站立姿态，同时同步 swimming 标记。
            if (isAquaticForm(fake)) {
                boolean swimming = player.isInWaterOrBubble();
                fake.setSwimming(swimming);
                fake.setPose(swimming ? Pose.SWIMMING : Pose.STANDING);
            }
            // 乌贼渲染器的俯仰读 xBodyRot（原版由乌贼 AI 自行计算，-90° 为水平巡航），
            // 伪装假实体不跑 AI 会一直是 0。把玩家俯仰映射进同一坐标系，变身乌贼/发光
            // 乌贼时低头即可下潜、抬头上浮，与鱼类竖直游泳表现一致。
            if (fake instanceof net.minecraft.world.entity.animal.Squid squid) {
                squid.xBodyRotO = -90.0F - player.xRotO;
                squid.xBodyRot = -90.0F - player.getXRot();
                squid.zBodyRotO = 0.0F;
                squid.zBodyRot = 0.0F;
            }
            fake.walkAnimation.setSpeed(player.walkAnimation.speed());
            fake.swinging = player.swinging;
            fake.swingTime = player.swingTime;
            fake.attackAnim = player.attackAnim;
            fake.oAttackAnim = player.oAttackAnim;
            if (fake instanceof net.minecraft.world.entity.AgeableMob ageable) ageable.setAge(0);
            if (fake instanceof net.minecraft.world.entity.animal.Sheep sheep) sheep.setSheared(false);
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            EntityRenderer<?> renderer = dispatcher.getRenderer(fake);
            boolean sheepRenderer = fake instanceof net.minecraft.world.entity.animal.Sheep
   && renderer instanceof LivingEntityRenderer<?, ?>;
            event.setCanceled(true);
            event.getPoseStack().pushPose();
            try {
                if (sheepRenderer) restoreSheepModelVisibility((LivingEntityRenderer<?, ?>) renderer);
                ((EntityRenderer) renderer).render(fake, fake.getYRot(), event.getPartialTick(), event.getPoseStack(),
       event.getMultiBufferSource(), event.getPackedLight());
            } finally {
                // SheepRenderer 与 SheepFurLayer 复用模型实例；渲染前后都清理可见性，避免共享状态让羊头消失。
                if (sheepRenderer) restoreSheepModelVisibility((LivingEntityRenderer<?, ?>) renderer);
                event.getPoseStack().popPose();
            }
        }

        private static boolean isAquaticForm(LivingEntity entity) {
            EntityType<?> type = entity.getType();
            return type == EntityType.AXOLOTL || type == EntityType.COD || type == EntityType.DOLPHIN
   || type == EntityType.GLOW_SQUID || type == EntityType.PUFFERFISH || type == EntityType.SALMON
   || type == EntityType.SQUID || type == EntityType.TADPOLE || type == EntityType.TROPICAL_FISH
   || type == EntityType.TURTLE || type == EntityType.GUARDIAN || type == EntityType.ELDER_GUARDIAN;
        }

        private static net.minecraft.network.syncher.EntityDataAccessor<Boolean> guardianMovingAccessor;
        private static boolean guardianMovingProbed;

        /**
         * 补写守卫者的 MOVING 同步标记。该字段是 Guardian 私有的 EntityDataAccessor，setMoving 也是
         * 包级可见；伪装假实体没人跑 AI 去写它，游动加速摆尾/收棘刺/吐气泡这条分支就永远走不到。
         * 首次调用时按"值为 Boolean 的静态访问器"探测字段（不依赖映射字段名字面量），探测失败只退
         * 化为常驻静止慢摆，不影响其余动画。
         */
        @SuppressWarnings("unchecked")
        private static void setGuardianMoving(net.minecraft.world.entity.monster.Guardian guardian, boolean moving) {
            if (guardianMovingAccessor == null && !guardianMovingProbed) {
                guardianMovingProbed = true;
                for (Field field : net.minecraft.world.entity.monster.Guardian.class.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())
   || field.getType() != net.minecraft.network.syncher.EntityDataAccessor.class) continue;
                    try {
   field.setAccessible(true);
   Object value = field.get(null);
   if (value instanceof net.minecraft.network.syncher.EntityDataAccessor<?> accessor
   && guardian.getEntityData().get(accessor) instanceof Boolean) {
       guardianMovingAccessor = (net.minecraft.network.syncher.EntityDataAccessor<Boolean>) accessor;
                   }
                    } catch (ReflectiveOperationException | RuntimeException ignored) {
                        // 运行环境拒绝反射时放弃移动标记同步，保持静止慢摆的原版默认观感。
                    }
                }
            }
            if (guardianMovingAccessor != null && guardian.isMoving() != moving) {
                guardian.getEntityData().set(guardianMovingAccessor, moving);
            }
        }

        private static java.lang.reflect.Field[] shulkerPeekFields;
        private static boolean shulkerPeekFieldsProbed;

        /**
         * 直接写伪装潜影贝的开合量字段。渲染器的 getClientPeekAmount 只读 currentPeekAmountO /
         * currentPeekAmount 这两个 float 字段做插值，写进去即可驱动壳的开合。
         *
         * 注意不能用字段名的字面量（如 "currentPeekAmount"）来挑选：运行时字段名是 SRG 名
         * （f_xxx_），而字符串字面量不会随字节码一起重映射，按名字匹配永远找不到字段。这里改为
         * 按“float 类型 + 非静态”来探测——Shulker 恰好只有这两个非静态 float 字段（currentPeekAmountO
         * 与 currentPeekAmount）。把两者都写成同一值 amount，getClientPeekAmount 便恒返回 amount，
         * 壳的开合完全受控，且与映射名无关。探测失败时保留紧闭壳，不影响其他变身形态。
         */
        private static void setShulkerPeekFields(net.minecraft.world.entity.monster.Shulker shulker,
                                                 float amount) {
            if (!shulkerPeekFieldsProbed) {
                shulkerPeekFieldsProbed = true;
                java.util.List<java.lang.reflect.Field> fields = new java.util.ArrayList<>();
                for (java.lang.reflect.Field field : net.minecraft.world.entity.monster.Shulker.class.getDeclaredFields()) {
                    if (field.getType() != float.class) continue;
                    if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
                    try { field.setAccessible(true); } catch (RuntimeException ignored) {
                    }
                    fields.add(field);
                }
                shulkerPeekFields = fields.toArray(new java.lang.reflect.Field[0]);
            }
            if (shulkerPeekFields.length == 0) return;
            for (java.lang.reflect.Field field : shulkerPeekFields) {
                try { field.setFloat(shulker, amount); } catch (ReflectiveOperationException | RuntimeException ignored) {
                    // 运行环境拒绝反射时放弃开壳，保持闭合壳的原版默认观感。
                }
            }
        }

        private static void restoreSheepModelVisibility(LivingEntityRenderer<?, ?> renderer) {
            EntityModel<?> model = renderer.getModel();
            if (model instanceof SheepModel<?>) restoreModelParts(model);
            for (Field layersField : RENDER_LAYER_LIST_FIELDS) {
                try {
   Object layers = layersField.get(renderer);
   if (!(layers instanceof Iterable<?> iterable)) continue;
   for (Object layer : iterable) {
       if (!(layer instanceof SheepFurLayer)) continue;
       for (Field modelField : NESTED_MODEL_FIELDS.computeIfAbsent(layer.getClass(),
               type -> findFields(type, EntityModel.class))) {
           Object nestedModel = modelField.get(layer);
           if (nestedModel instanceof EntityModel<?> entityModel) restoreModelParts(entityModel);
       }
   }
                } catch (IllegalAccessException | RuntimeException ignored) {
   // 模型可见性修复失败时仍保留原版渲染，不让显示修复影响游戏运行。
                }
            }
        }

        private static void restoreModelParts(EntityModel<?> model) {
            for (Field modelPartField : MODEL_PART_FIELDS.computeIfAbsent(model.getClass(),
   type -> findFields(type, ModelPart.class))) {
                try {
   Object root = modelPartField.get(model);
   if (root instanceof ModelPart part) {
       part.getAllParts().forEach(child -> {
           child.visible = true;
           child.skipDraw = false;
       });
   }
                } catch (IllegalAccessException | RuntimeException ignored) {
   // 单个模型部件访问失败不应中断假实体渲染。
                }
            }
        }

        private static Field[] findFields(Class<?> owner, Class<?> fieldType) {
            java.util.ArrayList<Field> fields = new java.util.ArrayList<>();
            for (Class<?> type = owner; type != null; type = type.getSuperclass()) {
                for (Field field : type.getDeclaredFields()) {
   if (Modifier.isStatic(field.getModifiers()) || !fieldType.isAssignableFrom(field.getType())) continue;
   try {
       field.setAccessible(true);
       fields.add(field);
   } catch (RuntimeException ignored) {
       // 运行环境拒绝反射时跳过该字段，保留原版模型渲染。
   }
                }
            }
            return fields.toArray(Field[]::new);
        }

        @SubscribeEvent
        public static void orientRenderPlayer(RenderPlayerEvent.Pre event) {
            var player = event.getEntity();
            var data = GeneratedMod.data(player);
            if (data != null && "fa_tian_xiang_di".equals(data.wuxingxiantu$getActiveDivineArt())) {
                event.getPoseStack().scale(5.0F, 5.0F, 5.0F);
            }
        }
    }
}
