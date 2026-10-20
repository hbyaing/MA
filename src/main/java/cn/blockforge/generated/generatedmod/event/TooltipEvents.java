package cn.blockforge.generated.generatedmod.event;

import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import java.util.Map;
import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.text.ItemGradeResolver;
import cn.blockforge.generated.generatedmod.text.ItemTextCatalog;
import cn.blockforge.generated.generatedmod.text.ItemTextPalette;
import static cn.blockforge.generated.generatedmod.GeneratedMod.*;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class TooltipEvents {

    private static void addSpiritEnchantmentTips(ItemStack stack, List<Component> tooltip) {
        java.util.Map<net.minecraft.world.item.enchantment.Enchantment, Integer> enchantments =
                new java.util.LinkedHashMap<>();
        if (stack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK)) {
            for (net.minecraft.nbt.Tag raw : net.minecraft.world.item.EnchantedBookItem.getEnchantments(stack)) {
                if (!(raw instanceof CompoundTag entry)) continue;
                ResourceLocation key = ResourceLocation.tryParse(entry.getString("id"));
                if (key == null) continue;
                enchantments.put(net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT.get(key),
                        entry.getInt("lvl"));
            }
        } else {
            enchantments.putAll(net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantments(stack));
        }
        for (var element : enchantments.entrySet()) {
            ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT.getKey(element.getKey());
            if (key == null || !MOD_ID.equals(key.getNamespace()) || element.getValue() <= 0) continue;
            tooltip.add(Component.translatable("enchantment." + MOD_ID + "." + key.getPath() + ".desc")
                    .withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    @SubscribeEvent public static void itemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if(cn.blockforge.generated.generatedmod.HerbPreparation.prepared(stack)){
            if(!event.getToolTip().isEmpty())event.getToolTip().set(0,Component.translatable("tooltip.wuxingxiantu.herb_prepared_name",event.getToolTip().get(0)));
            event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.herb_prepared").withStyle(ChatFormatting.GREEN));
        }
        if (stack.is(net.minecraft.world.item.Items.COAL)
                || stack.is(net.minecraft.world.level.block.Blocks.COAL_BLOCK.asItem())) {
            event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.coal_conversion")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        // 灵纹附魔（附魔书或已附魔装备）在 tooltip 中直接展示功能说明。
        addSpiritEnchantmentTips(stack, event.getToolTip());
        addHeartCombatTips(event);
        if (id == null || !MOD_ID.equals(id.getNamespace())) return;
        int fireTier=cn.blockforge.generated.generatedmod.text.ExoticFireDescriptions.tier(stack);
        if(fireTier>0) {
            long time=event.getEntity()==null?0:event.getEntity().level().getGameTime();
            var tooltip=event.getToolTip();
            if(!tooltip.isEmpty())tooltip.set(0,ItemTextPalette.colored(tooltip.get(0),
                    cn.blockforge.generated.generatedmod.text.ExoticFireDescriptions.color(fireTier,time)));
            tooltip.removeIf(line->line.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text
                    &&text.getKey().equals("tooltip.wuxingxiantu.fire_tier"));
            tooltip.addAll(Math.min(1,tooltip.size()),cn.blockforge.generated.generatedmod.text.ExoticFireDescriptions.lines(fireTier,time));
            if(stack.getItem() instanceof net.minecraft.world.item.BlockItem)
                tooltip.add(Component.translatable("tooltip.wuxingxiantu.fire_detail.placement").withStyle(ChatFormatting.GRAY));
            return;
        }
        String path = id.getPath();
        if(path.equals("dragon_scale_grass"))event.getToolTip().add(Component.literal("仅限末地主岛；野生最多3株，每7个游戏日换随机地点刷新。").withStyle(ChatFormatting.AQUA));
        if(path.endsWith("_earth_core")||path.endsWith("_wood_seed")||path.endsWith("_gold_marrow")||path.endsWith("_water_pearl")||path.endsWith("_fire_flame")){
            String obtain=path.endsWith("_earth_core")?"挖掘土壤（泥土、草方块、泥巴等；不含石头）":path.endsWith("_wood_seed")?"砍伐原木":path.endsWith("_gold_marrow")?"挖掘铁、金或钻石矿":path.endsWith("_water_pearl")?"钓鱼":"品阶合成；当前没有随机掉落来源";
            event.getToolTip().add(Component.literal("获得方式："+obtain).withStyle(ChatFormatting.AQUA));
            if(!path.endsWith("_fire_flame"))event.getToolTip().add(Component.literal(path.endsWith("_water_pearl")?"钓鱼额外掉落：5%，仅下品":"每次采集掉落率5%；无幸运时下/中/上/极品占70% / 23% / 6% / 1%；幸运提高高品概率").withStyle(ChatFormatting.GRAY));
            if(!path.endsWith("_fire_flame")&&!path.endsWith("_water_pearl")){
                var chances=cn.blockforge.generated.generatedmod.balance.ElementMaterialDrops.chances(event.getEntity()==null?0:event.getEntity().getLuck());
                String rates=String.format(java.util.Locale.ROOT,"当前每次采集：下品%.2f%% / 中品%.2f%% / 上品%.2f%% / 极品%.2f%%",chances[0]*5,chances[1]*5,chances[2]*5,chances[3]*5);
                event.getToolTip().add(Component.literal(rates).withStyle(ChatFormatting.AQUA));
            }
            event.getToolTip().add(Component.literal("四份同品阶材料可合成一份下一品阶（以已解锁配方为准）").withStyle(ChatFormatting.GRAY));
        }
        if(path.equals("meditation_cushion")||path.equals("spirit_cushion")||path.equals("meditation_platform"))event.getToolTip().add(Component.literal(path.equals("meditation_cushion")?"蒲团：修炼效率×1.00，灵力回复×2（相对站立回复）":path.equals("spirit_cushion")?"灵蒲团：修炼效率×1.30，灵力回复×3（相对站立回复）":"打坐台：修炼效率×1.60，灵力回复×4（相对站立回复）").withStyle(ChatFormatting.AQUA));
        var entry=ItemTextCatalog.resolve(stack);
        if(entry!=null) {
            addIntroduction(event,entry);
            if(!entry.visibility().equals("player"))return;
        }
        if(cn.blockforge.generated.generatedmod.alchemy.PillData.read(stack)!=null
                ||stack.getItem() instanceof cn.blockforge.generated.generatedmod.items.FireSeedItem)return;
        // 灵植（含龙鳞草/发光蓝蘑菇）由 SpiritPlantItem 自带药性/稀有度/培养三行 Tooltip，
        // 不再叠加通用材料说明。
        if (cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry.DEFINITIONS.containsKey(path)) return;
        // SpiritSwordItem 自己负责剑灵力、射程和穿透说明，避免旧的御剑操作提示重复出现。
        if (path.endsWith("_sword")) return;
        // 经验结晶由物品类显示“每枚 5 点经验”，不再叠加通用材料说明。
        if (path.equals("experience_crystal") || path.equals("cultivation_insight")
                || path.equals("soul_pearl") || path.equals("broken_soul_pearl")
                || path.equals("myriad_soul_pearl") || path.equals("creative_myriad_soul_pearl")
                || path.equals("destroy_soul_pearl") || path.equals("soul_energy_pearl")
                || path.equals("creative_soul_energy_pearl")) return;
        String key = tooltipKey(path);
        if (entry==null && path.startsWith("breakthrough_pill_")) {
            event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.breakthrough_pill",
                    stack.getHoverName()).withStyle(ChatFormatting.GRAY));
            return;
        }
        if (entry==null && path.equals("heart_demon_spawn_egg")) {
            event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.heart_demon_spawn_egg",
                    stack.getHoverName()).withStyle(ChatFormatting.GRAY));
            return;
        }
        if (entry==null && path.equals("cave_token")) {
            event.getToolTip().add(Component.translatable(key, stack.getHoverName(), CaveTokenBlock.PROTECTION_RADIUS)
                    .withStyle(ChatFormatting.GRAY));
        } else if(entry==null) {
            event.getToolTip().add(Component.translatable(key, stack.getHoverName()).withStyle(ChatFormatting.GRAY));
        }
        if (path.equals("guanding_manual")) {
            event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.guanding_method")
                    .withStyle(ChatFormatting.AQUA));
            event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.guanding_cost")
                    .withStyle(ChatFormatting.GOLD));
        }
        var bookSource=cn.blockforge.generated.generatedmod.acquisition.CultivationBookSources.find(id);
        if (bookSource!=null) {
            event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.book_source",
                    bookSource.merchantLevel()).withStyle(ChatFormatting.GRAY));
        }
        if (path.startsWith("art_")) {
            String art = path;
            event.getToolTip().add(ArtCatalog.function(art).copy().withStyle(ChatFormatting.AQUA));
            event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.art_cost",
                            ArtCatalog.fixedManaCost(art), ArtCatalog.percentageCost(art))
                    .withStyle(ChatFormatting.GOLD));
            event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.knowledge_requirement",
                    cn.blockforge.generated.generatedmod.items.CultivationLogic.realmName(ProgressionGate.requiredRealmForArt(art)))
                    .withStyle(ChatFormatting.GOLD));
            event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.study_art").withStyle(ChatFormatting.GRAY));
            boolean learned = data(event.getEntity()) != null && data(event.getEntity()).wuxingxiantu$hasLearnedArt(art);
            event.getToolTip().add(Component.translatable(learned
                            ? "tooltip.wuxingxiantu.learned"
                            : "tooltip.wuxingxiantu.not_learned")
                    .withStyle(learned ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        }
        if (path.startsWith("heart_method_")) {
            String methodId = path.substring("heart_method_".length());
            if (HeartMethodCatalog.isValid(methodId)) {
                event.getToolTip().add(HeartMethodCatalog.description(methodId).copy()
                        .withStyle(ChatFormatting.AQUA));
                boolean canLearn=event.getEntity()==null||ProgressionGate.canLearnHeartMethod(event.getEntity(),methodId);
                event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.knowledge_requirement",
                        cn.blockforge.generated.generatedmod.items.CultivationLogic.realmName(ProgressionGate.requiredRealmForHeartMethod(methodId)))
                        .withStyle(canLearn?ChatFormatting.GOLD:ChatFormatting.RED));
                event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.study_heart").withStyle(ChatFormatting.GRAY));
                boolean learned = data(event.getEntity()) != null
                        && data(event.getEntity()).wuxingxiantu$hasLearnedHeartMethod(methodId);
                event.getToolTip().add(Component.translatable(learned
                                ? "tooltip.wuxingxiantu.learned"
                                : "tooltip.wuxingxiantu.not_learned")
                        .withStyle(learned ? ChatFormatting.GREEN : ChatFormatting.GRAY));
            }
        }
    }

    private static void addIntroduction(ItemTooltipEvent event,ItemTextCatalog.Entry entry) {
        var stack=event.getItemStack();
        long time=event.getEntity()==null?0:event.getEntity().level().getGameTime();
        var display=ItemGradeResolver.resolve(stack,time);
        if(display==null)return;
        var tooltip=event.getToolTip();
        if(!tooltip.isEmpty())tooltip.set(0,ItemTextPalette.colored(tooltip.get(0),display.nameColor())
                .withStyle(style->display.creative()?style.withItalic(true):style));
        // A single grade row replaces the item-specific row, keeping all other dynamic data.
        tooltip.removeIf(line->line.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text
                &&java.util.Set.of("pill.wuxingxiantu.quality","tooltip.wuxingxiantu.fire_tier",
                "tooltip.wuxingxiantu.talisman_quality","tooltip.wuxingxiantu.creative_test_badge").contains(text.getKey())
                &&(!text.getKey().equals("tooltip.wuxingxiantu.talisman_quality")||entry.id().equals("talisman")));
        var intro=new java.util.ArrayList<Component>();
        if(!entry.id().equals("xuanling_anvil"))intro.add(display.grade());
        if(!display.internal()) {
            intro.add(entry.id().equals("xuanling_anvil")?ItemTextPalette.colored(entry.text("lore"),display.nameColor()):entry.text("lore").copy().withStyle(ChatFormatting.GRAY));
            for(int i=0;i<entry.actions();i++) {
                var action=entry.text("action."+i);
                String text=action.getString();
                var color=text.startsWith("右键")||text.startsWith("潜行")||text.startsWith("研读")
                        ||text.startsWith("种下")||text.startsWith("长按")?ChatFormatting.GRAY
                        :text.startsWith("仅")||text.startsWith("创造模式")?ChatFormatting.GOLD:ChatFormatting.AQUA;
                intro.add(entry.id().equals("xuanling_anvil")&&i==0?ItemTextPalette.colored(action,display.nameColor()):action.copy().withStyle(color));
            }
        }
        tooltip.addAll(Math.min(1,tooltip.size()),intro);
    }

    private static void addHeartCombatTips(ItemTooltipEvent event) {
        var stack=event.getItemStack();
        var player=event.getEntity();
        if(!cn.blockforge.generated.generatedmod.combat.HeartSwordCombatProfile.isHeartSword(stack)
                &&!SpiritSwordItem.isHeartForged(stack))return;
        if(!cn.blockforge.generated.generatedmod.combat.HeartSwordCombatProfile.isValidHeartOwner(player,stack)) {
            event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.heart_no_resonance").withStyle(ChatFormatting.GRAY));
            return;
        }
        event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.heart_true").withStyle(ChatFormatting.AQUA));
        event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.heart_resonance",
                Math.round((cn.blockforge.generated.generatedmod.combat.HeartSwordCombatProfile.heartSwordRealmMultiplier(player)-1)*100),
                Math.round((cn.blockforge.generated.generatedmod.combat.HeartSwordCombatProfile.heartSwordStageMultiplier(player)-1)*100))
                .withStyle(ChatFormatting.AQUA));
        double projected=cn.blockforge.generated.generatedmod.combat.HeartSwordCombatProfile.projectHeartSwordDamage(player);
        double selected=cn.blockforge.generated.generatedmod.combat.HeartSwordCombatProfile.attackBaseline(player,stack);
        event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.heart_damage",
                String.format(java.util.Locale.ROOT,"%.2f",selected)).withStyle(ChatFormatting.GOLD));
        if(SpiritSwordItem.isHeartForged(stack))event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.heart_forged_damage",
                String.format(java.util.Locale.ROOT,"%.2f",cn.blockforge.generated.generatedmod.combat.HeartSwordCombatProfile.originalSwordDamage(player,stack)),
                String.format(java.util.Locale.ROOT,"%.2f",projected)).withStyle(ChatFormatting.GRAY));
    }

    private static String tooltipKey(String path) {
        if (path.equals("guanding_manual")) return "tooltip.wuxingxiantu.guanding_manual";
        if (path.equals("funiao_fu_fragment") || path.equals("daode_jing_fragment") || path.equals("gangdou_starmap") || path.equals("zhonglv_fragment")
                || path.equals("yin_fu_jing_yi_shen_fragment")
                || path.equals("da_pin_tian_xian_jue_fragment_one")
                || path.equals("da_pin_tian_xian_jue_fragment_two")
                 || path.equals("ri_yue_ting_lun_fragment")) return "tooltip.wuxingxiantu.divine_manual";
        if (path.startsWith("heart_method_")) return "tooltip.wuxingxiantu.heart_method";
        if (path.startsWith("art_")) return "tooltip.wuxingxiantu.art_manual";
        if (path.equals("spirit_liquid_bottle")) return "tooltip.wuxingxiantu.spirit_liquid_bottle";
        if (path.equals("spirit_liquid_bucket")) return "tooltip.wuxingxiantu.spirit_liquid_bucket";
        if (path.equals("spirit_liquid_pot")) return "tooltip.wuxingxiantu.spirit_liquid_pot";
        if (path.equals("empty_spirit_liquid_pot")) return "tooltip.wuxingxiantu.empty_spirit_liquid_pot";
        if (path.equals("soul_binding_pearl")) return "tooltip.wuxingxiantu.soul_binding_pearl";
        if (path.equals("soul_capture_pearl")) return "tooltip.wuxingxiantu.soul_capture_pearl";
        if (path.equals("myriad_soul_pearl")) return "tooltip.wuxingxiantu.myriad_soul_pearl";
        if (path.equals("creative_myriad_soul_pearl")) return "tooltip.wuxingxiantu.creative_myriad_soul_pearl";
        if (path.equals("destroy_soul_pearl")) return "tooltip.wuxingxiantu.destroy_soul_pearl";
        if (path.equals("soul_energy_pearl")) return "tooltip.wuxingxiantu.soul_energy_pearl";
        if (path.equals("creative_soul_energy_pearl")) return "tooltip.wuxingxiantu.creative_soul_energy_pearl";
        if (path.equals("human_emperor_banner")) return "tooltip.wuxingxiantu.human_emperor_banner";
        if (path.equals("yue_coin")) return "tooltip.wuxingxiantu.yue_coin";
        if (path.equals("glowing_blue_mushroom")) return "tooltip.wuxingxiantu.glowing_blue_mushroom";
        if (path.equals("spirit_fertilizer")) return "tooltip.wuxingxiantu.spirit_fertilizer";
        if (path.equals("spirit_soil")) return "tooltip.wuxingxiantu.spirit_soil";
        if (path.equals("spirit_liquid")) return "tooltip.wuxingxiantu.spirit_liquid";
        if (path.equals("spirit_coal")) return "tooltip.wuxingxiantu.spirit_coal";
        if (path.equals("glazed_crystal")) return "tooltip.wuxingxiantu.glazed_crystal";
        if (path.equals("empty_fire_seed_bottle") || path.startsWith("fire_seed_")) {
            return "tooltip.wuxingxiantu.fire_seed";
        }
        if (path.equals("cave_token")) return "tooltip.wuxingxiantu.cave_token";
        if (path.endsWith("_sword")) return "tooltip.wuxingxiantu.item_generic";
        if (path.contains("pill") || path.startsWith("root_")) return "tooltip.wuxingxiantu.pill";
        if (path.contains("furnace") || path.contains("platform") || path.contains("array")
                || path.contains("cushion") || path.contains("token") || path.contains("peach_")) {
            return "tooltip.wuxingxiantu.block_item";
        }
        if (path.contains("marrow") || path.contains("seed") || path.contains("pearl")
                || path.contains("flame") || path.contains("core") || path.equals("five_essence")
                || path.equals("spirit_stone") || path.equals("spirit_coal") || path.equals("aura_crystal")
                || path.equals("dragon_scale_grass")) {
            return "tooltip.wuxingxiantu.material";
        }
        return "tooltip.wuxingxiantu.item_generic";
    }
    private TooltipEvents() {}
}
