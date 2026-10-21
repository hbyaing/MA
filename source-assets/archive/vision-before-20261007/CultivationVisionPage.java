package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.items.FireSeedItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** 内视只显示自身修行状态；所有操作调用现有网络消息。 */
public final class CultivationVisionPage extends CultivationPage {
    private record Binding(CultivationUi.Action button, Supplier<Component> label, BooleanSupplier active) {}
    private final List<Binding> bindings = new ArrayList<>();
    private int rootsY, domainsY, firesY, actionsY, column, right;
    private boolean wide, effectsMode;
    public CultivationVisionPage() { super("内视"); }
    private CultivationData data() { return GeneratedMod.data(minecraft.player); }
    @Override public int contentHeight(int w, int h) {
        if (effectsMode) return Math.max(220, 42 + net.minecraft.client.Minecraft.getInstance().player.getActiveEffects().size() * 32);
        return Math.max(h, w >= 280 ? 287 : 818);
    }
    private void showEffects(boolean value) { effectsMode = value; host.selectPage("vision"); host.scrollToTop(); }
    private void button(int x, int y, int w, Supplier<Component> label, Runnable action, BooleanSupplier active, BooleanSupplier selected) {
        var b = addRenderableWidget(new CultivationUi.Action(x, y, w, 22, label.get(), ignored -> action.run(), selected));
        bindings.add(new Binding(b, label, active));
    }
    @Override protected void init() {
        bindings.clear();
        if (effectsMode) {
            addRenderableWidget(new CultivationUi.Action(width - 70, 0, 70, 20, Component.literal("修炼状态"), b -> showEffects(false)));
            return;
        }
        addRenderableWidget(new CultivationUi.Action(width - 70, 0, 70, 20, Component.literal("药水状态"), b -> showEffects(true)));
        wide = width >= 280;
        if (wide) { initCompact(); tick(); return; }
        column = wide ? (width - 16) / 2 : width - 8; right = wide ? column + 12 : 0;
        rootsY = wide ? 150 : 242; domainsY = wide ? 150 : 362;
        firesY = wide ? 330 : 520; actionsY = wide ? 330 : 650;
        int col = (column - 4) / 2;
        button(0, rootsY + 80, col, () -> Component.literal(CultivationLogic.isThunderForm(data()) ? "木灵根" : "天雷灵根"),
                () -> Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.RootFormPacket(0)),
                () -> data() != null && CultivationLogic.thunderRootAvailable(data()), () -> data() != null && CultivationLogic.isThunderForm(data()));
        button(col + 4, rootsY + 80, col, () -> Component.literal(CultivationLogic.isExoticForm(data()) ? "火灵根" : "异火灵根"),
                () -> Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.RootFormPacket(1)),
                () -> data() != null && CultivationLogic.exoticRootAvailable(data()), () -> data() != null && CultivationLogic.isExoticForm(data()));
        for (int i = 0; i < 5; i++) {
            final int root = i;
            button(right, domainsY + 18 + i * 23, column,
                    () -> domainLabel(root), () -> toggleDomain(root), () -> ownsDomain(root), () -> domainActive(root));
        }
        button(right, domainsY + 137, col,
                () -> Component.literal(CultivationLogic.isThunderForm(data()) ? "木灵领域" : "天雷领域"),
                () -> Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.RootFormPacket(0)),
                () -> ownsDomain(1) && CultivationLogic.thunderRootAvailable(data()), () -> CultivationLogic.isThunderForm(data()));
        button(right + col + 4, domainsY + 137, col,
                () -> Component.literal(CultivationLogic.isExoticForm(data()) ? "业火领域" : "异火领域"),
                () -> Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.RootFormPacket(1)),
                () -> ownsDomain(3) && CultivationLogic.exoticRootAvailable(data()), () -> CultivationLogic.isExoticForm(data()));
        for (int i = 1; i <= 9; i++) {
            final int tier = i;
            var fire = addRenderableWidget(new CultivationUi.Icon(((i - 1) % 3) * 36, firesY + 18 + (i - 1) / 3 * 36, 32, "",
                    () -> new net.minecraft.world.item.ItemStack(FireSeedItem.seedForTier(tier)),
                    () -> Component.literal(FireSeedItem.getFireName(tier) + "\n" + (data() != null && data().wuxingxiantu$hasLearnedFire(tier)
                            ? data().wuxingxiantu$getEquippedFire() == tier ? "已装备" : "已获得" : "未获得")),
                    b -> Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.FireSelectPacket(data().wuxingxiantu$getEquippedFire() == tier ? 0 : tier)),
                    () -> data() != null && data().wuxingxiantu$getEquippedFire() == tier));
            bindings.add(new Binding(fire, () -> Component.literal(FireSeedItem.getFireName(tier)),
                    () -> data() != null && data().wuxingxiantu$hasLearnedFire(tier)));
        }
        button(right, actionsY + 20, column, () -> Component.literal("渡劫"),
                () -> Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.TribulationPacket()),
                () -> TribulationManager.canStart(minecraft.player, data()), () -> false);
        button(right, actionsY + 110, column, () -> Component.literal("凝晶"),
                () -> Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.CrystallizePacket()),
                () -> data() != null && data().wuxingxiantu$getAura() >= CultivationLogic.CRYSTALLIZE_AURA_COST, () -> false);
        repeatButton(0,786,width-8);
        tick();
    }
    @Override public void tick() {
        if (effectsMode) { height = contentHeight(width, 220); return; }
        for (var binding : bindings) {
            binding.button().setMessage(binding.label().get()); binding.button().active = binding.active().getAsBoolean();
        }
    }
    private void initCompact() {
        repeatButton(0,261,width-8);
        column = (width - 20) / 3; right = column + 10;
        for (var widget : children()) if (widget instanceof CultivationUi.Action action && action.getMessage().getString().equals("药水状态")) {
            action.setX(right); action.setY(106); action.setWidth(column);
        }
        rootsY = domainsY = firesY = 140; actionsY = 0;
        for (int i = 0; i < 5; i++) {
            final int root = i;
            var action = addRenderableWidget(new CultivationUi.Action(right, 158 + i * 19, column, 18,
                    domainLabel(root), b -> toggleDomain(root), () -> domainActive(root)));
            bindings.add(new Binding(action, () -> domainLabel(root), () -> ownsDomain(root)));
        }
        for (int i = 0; i < 2; i++) {
            final int form = i;
            button(i * (column / 2 + 1), 230, column / 2 - 1,
                    () -> Component.literal(form == 0 ? CultivationLogic.isThunderForm(data()) ? "木灵根" : "天雷根"
                            : CultivationLogic.isExoticForm(data()) ? "火灵根" : "异火根"),
                    () -> Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.RootFormPacket(form)),
                    () -> form == 0 ? CultivationLogic.thunderRootAvailable(data()) : CultivationLogic.exoticRootAvailable(data()),
                    () -> form == 0 ? CultivationLogic.isThunderForm(data()) : CultivationLogic.isExoticForm(data()));
        }
        for (int i = 1; i <= 9; i++) {
            final int tier = i;
            var fire = addRenderableWidget(new CultivationUi.Icon(right * 2 + (i - 1) % 3 * 26, 159 + (i - 1) / 3 * 26, 24, "",
                    () -> new net.minecraft.world.item.ItemStack(FireSeedItem.seedForTier(tier)),
                    () -> Component.literal(FireSeedItem.getFireName(tier) + " · " + (data().wuxingxiantu$getEquippedFire() == tier ? "已装备" : data().wuxingxiantu$hasLearnedFire(tier) ? "已获得" : "未获得")),
                    b -> Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.FireSelectPacket(data().wuxingxiantu$getEquippedFire() == tier ? 0 : tier)),
                    () -> data().wuxingxiantu$getEquippedFire() == tier));
            bindings.add(new Binding(fire, () -> Component.literal(FireSeedItem.getFireName(tier)), () -> data() != null && data().wuxingxiantu$hasLearnedFire(tier)));
        }
        button(right * 2, 24, column, () -> Component.literal("渡劫"),
                () -> Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.TribulationPacket()),
                () -> data() != null && TribulationManager.canStart(minecraft.player, data()), () -> false);
        button(right * 2, 105, column, () -> Component.literal("凝晶"),
                () -> Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.CrystallizePacket()),
                () -> data() != null && data().wuxingxiantu$getAura() >= CultivationLogic.CRYSTALLIZE_AURA_COST, () -> false);
    }
    private void repeatButton(int x,int y,int w) {
        button(x,y,w,()->Component.translatable("gui.wuxingxiantu.rechallenge"),
                ()->minecraft.player.connection.sendCommand("wuxingxiantu rechallenge"),
                ()->data()!=null&&!data().wuxingxiantu$isDaoHeartUnstable(),()->false);
    }
    private void compactText(GuiGraphics g, Component text, int x, int y, int color, int mx, int my) {
        CultivationUi.fit(g, text, x, y, column, color, mx, my);
    }
    private void renderCompact(GuiGraphics g, int mx, int my) {
        var d = data();
        for (int i = 0; i < 3; i++) {
            CultivationUi.panel(g, i * right - 2, 0, column + 4, 133, false);
            CultivationUi.panel(g, i * right - 2, 137, column + 4, 116, false);
        }
        heading(g, "修炼状态", 0, 4); heading(g, "修行根基", right, 4); heading(g, "修炼操作", right * 2, 4);
        compactText(g, Component.literal(CultivationLogic.realmStageName(minecraft.player, d)), 0, 21, CultivationUi.GOLD, mx, my);
        CultivationUi.bar(g, "修为", 0, 35, column, d.wuxingxiantu$getCultivation(), CultivationLogic.breakthroughRequirement(d), CultivationUi.GOLD, mx, my);
        CultivationUi.bar(g, "灵力", 0, 59, column, d.wuxingxiantu$getCurrentMana(), CultivationLogic.maxAura(d), CultivationUi.CYAN, mx, my);
        CultivationUi.bar(g, "生命", 0, 83, column, minecraft.player.getHealth(), minecraft.player.getMaxHealth(), CultivationUi.RED, mx, my);
        compactText(g, Component.literal("攻击 " + CultivationUi.number(minecraft.player.getAttributeValue(Attributes.ATTACK_DAMAGE))), 0, 109, CultivationUi.TEXT, mx, my);
        compactText(g, Component.literal("功德 " + (d.wuxingxiantu$getMerit() == Long.MAX_VALUE ? "∞" : CultivationUi.number(d.wuxingxiantu$getMerit()))), 0, 121, CultivationUi.MUTED, mx, my);
        int dao = Math.max(0, Math.min(8, d.wuxingxiantu$getDaoXin()));
        compactText(g, Component.literal("道心：").append(Component.translatable("dao.wuxingxiantu." + dao)), right, 23, dao == 4 ? 0xFFDC143C : CultivationUi.TEXT, mx, my);
        int status = d.wuxingxiantu$getMeridianStatus();
        compactText(g, Component.literal("经脉：").append(Component.translatable("gui.wuxingxiantu.inner_vision.meridian_" + (status >= 2 ? "shattered" : status == 1 ? "damaged" : "intact"))), right, 44, status > 0 ? CultivationUi.RED : CultivationUi.GREEN, mx, my);
        compactText(g, Component.literal("体质：").append(Component.translatable("physique.wuxingxiantu." + d.wuxingxiantu$getPhysique().id())), right, 65, CultivationUi.GREEN, mx, my);
        compactText(g, Component.literal("阳神纯度：" + d.wuxingxiantu$getYinShenPurity()), right, 86, CultivationUi.CYAN, mx, my);
        compactText(g, Component.literal(TribulationManager.canStart(minecraft.player, d) ? "可渡劫" : "渡劫条件未满足"), right * 2, 51, CultivationUi.MUTED, mx, my);
        compactText(g, Component.literal("耗费 " + CultivationUi.number(CultivationLogic.CRYSTALLIZE_AURA_COST) + " 灵力"), right * 2, 72, CultivationUi.CYAN, mx, my);
        compactText(g, Component.literal("凝成 1 枚灵力结晶"), right * 2, 87, CultivationUi.MUTED, mx, my);
        heading(g, "灵根", 0, 141); heading(g, "领域", right, 141); heading(g, "异火", right * 2, 141);
        String[] glyphs = {"金", CultivationLogic.isThunderForm(d) ? "雷" : "木", "水", CultivationLogic.isExoticForm(d) ? "焰" : "火", "土", "混沌"};
        int[] colors = {CultivationUi.GOLD, CultivationUi.GREEN, CultivationUi.CYAN, CultivationUi.RED, CultivationUi.GOLD, CultivationUi.GOLD};
        for (int i = 0; i < 6; i++) {
            int x = i % 3 * (column / 3), y = 159 + i / 3 * 33;
            boolean owned = i == 5 ? d.wuxingxiantu$hasChaosRoot() : (d.wuxingxiantu$getRootMask() & (1 << i)) != 0;
            CultivationUi.slot(g, x, y, 22, 20, CultivationUi.BORDER);
            g.drawCenteredString(font, glyphs[i], x + 11, y + 6, owned ? colors[i] : CultivationUi.MUTED);
            Component quality = Component.literal(!owned ? "未有" : i == 5 ? "已成" : "极品");
            if (owned && i < 5) quality = Component.translatable("quality.wuxingxiantu." + Math.max(0, Math.min(3, d.wuxingxiantu$getRootQuality(i) - 1)));
            CultivationUi.fit(g, quality, x, y + 22, column / 3 - 1, CultivationUi.MUTED, mx, my);
            if (mx >= x && mx < x + column / 3 && my >= y && my < y + 32)
                CultivationUi.tooltip(rootHint(i));
        }
    }
    private Component rootHint(int root) {
        var d = data();
        String key = root == 5 ? "chaos" : root == 1 && CultivationLogic.isThunderForm(d) ? "thunder"
                : root == 3 && CultivationLogic.isExoticForm(d) ? "exotic_fire" : Integer.toString(root);
        boolean owned = root == 5 ? d.wuxingxiantu$hasChaosRoot() : (d.wuxingxiantu$getRootMask() & 1 << root) != 0;
        var hint = Component.translatable("root.wuxingxiantu." + key).append("\n")
                .append(Component.translatable(owned ? "tooltip.wuxingxiantu.root_owned" : "tooltip.wuxingxiantu.root_missing"));
        if (root < 5 && owned) hint.append(" · ").append(Component.translatable("quality.wuxingxiantu." + Math.max(0, Math.min(3,d.wuxingxiantu$getRootQuality(root)-1))));
        hint.append("\n").append(Component.translatable("tooltip.wuxingxiantu.root_" + key));
        if (root < 5) {
            String domain = root == 1 && CultivationLogic.isThunderForm(d) ? "thunder" : root == 3 && CultivationLogic.isExoticForm(d) ? "exotic" : new String[]{"gold","wood","water","fire","earth"}[root];
            hint.append("\n").append(Component.translatable("effect.wuxingxiantu." + domain + "_domain.description"));
            hint.append("\n").append(Component.translatable(!ownsDomain(root) ? "tooltip.wuxingxiantu.domain_locked" : domainActive(root) ? "tooltip.wuxingxiantu.domain_active" : "tooltip.wuxingxiantu.domain_inactive"));
        }
        return hint;
    }
    private boolean ownsDomain(int root) {
        return data() != null && (data().wuxingxiantu$getRootMask() & (1 << root)) != 0 && data().wuxingxiantu$getRootQuality(root) >= 2;
    }
    private boolean domainActive(int root) {
        var d = data(); if (d == null) return false;
        return switch (root) {
            case 0 -> d.wuxingxiantu$isGoldDomainEnabled(); case 1 -> d.wuxingxiantu$isWoodDomainEnabled();
            case 2 -> d.wuxingxiantu$isWaterDomainEnabled(); case 3 -> d.wuxingxiantu$isFireDomainEnabled();
            default -> d.wuxingxiantu$isEarthDomainEnabled();
        };
    }
    private Component domainLabel(int root) {
        String name = switch (root) {
            case 0 -> "gold"; case 1 -> CultivationLogic.isThunderForm(data()) ? "thunder" : "wood";
            case 2 -> "water"; case 3 -> CultivationLogic.isExoticForm(data()) ? "exotic" : "fire"; default -> "earth";
        };
        return Component.translatable("gui.wuxingxiantu.inner_vision." + name + "_domain_name")
                .append(ownsDomain(root) ? domainActive(root) ? " · 生效中" : " · 未开启" : " · 未拥有");
    }
    private void toggleDomain(int root) {
        boolean enable = !domainActive(root);
        Object packet = switch (root) {
            case 0 -> new cn.blockforge.generated.generatedmod.network.packet.cultivation.GoldDomainPacket(enable); case 1 -> new cn.blockforge.generated.generatedmod.network.packet.cultivation.WoodDomainPacket(enable);
            case 2 -> new cn.blockforge.generated.generatedmod.network.packet.cultivation.WaterDomainPacket(enable); case 3 -> new cn.blockforge.generated.generatedmod.network.packet.cultivation.FireDomainPacket(enable);
            default -> new cn.blockforge.generated.generatedmod.network.packet.cultivation.EarthDomainPacket(enable);
        };
        Network.CHANNEL.sendToServer(packet);
    }
    private void text(GuiGraphics g, String text, int y, int color) { g.drawString(font, text, 0, y, color, false); }
    private void heading(GuiGraphics g, String text, int x, int y) {
        g.drawString(font, text, x, y, CultivationUi.GOLD, false); g.fill(x, y + 12, x + column, y + 13, CultivationUi.BORDER);
    }
    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        if (effectsMode) { renderEffects(g, mx, my); super.render(g, mx, my, delta); return; }
        var d = data(); if (d == null) { text(g, "正在等待修炼数据同步", 8, CultivationUi.MUTED); return; }
        if (wide) { renderCompact(g, mx, my); super.render(g, mx, my, delta); return; }
        int base = wide ? 30 : 143;
        CultivationUi.panel(g, -2, 23, column + 4, 112, false);
        CultivationUi.panel(g, right - 2, base - 3, column + 4, 91, false);
        CultivationUi.panel(g, -2, rootsY - 3, column + 4, 109, false);
        CultivationUi.panel(g, right - 2, domainsY - 3, column + 4, 166, false);
        CultivationUi.panel(g, -2, firesY - 3, column + 4, 131, false);
        CultivationUi.panel(g, right - 2, actionsY - 3, column + 4, 141, false);
        text(g, CultivationLogic.realmStageName(minecraft.player, d), 4, CultivationUi.GOLD);
        CultivationUi.bar(g, "修为", 0, 26, column, d.wuxingxiantu$getCultivation(), CultivationLogic.breakthroughRequirement(d), CultivationUi.GOLD, mx, my);
        CultivationUi.bar(g, "灵力", 0, 51, column, d.wuxingxiantu$getCurrentMana(), CultivationLogic.maxAura(d), CultivationUi.CYAN, mx, my);
        CultivationUi.bar(g, "生命", 0, 76, column, minecraft.player.getHealth(), minecraft.player.getMaxHealth(), CultivationUi.GREEN, mx, my);
        text(g, "攻击：" + CultivationUi.number(minecraft.player.getAttributeValue(Attributes.ATTACK_DAMAGE)), 103, CultivationUi.MUTED);
        text(g, "功德：" + (d.wuxingxiantu$getMerit() == Long.MAX_VALUE ? "∞" : CultivationUi.number(d.wuxingxiantu$getMerit())), 119, CultivationUi.MUTED);
        heading(g, "修行根基", right, base);
        int dao = Math.max(0, Math.min(8, d.wuxingxiantu$getDaoXin()));
        CultivationUi.fit(g, Component.literal("道心：").append(Component.translatable("dao.wuxingxiantu." + dao)), right, base + 22, column, dao == 4 ? 0xFFDC143C : CultivationUi.TEXT, mx, my);
        int status = d.wuxingxiantu$getMeridianStatus();
        CultivationUi.fit(g, Component.literal("经脉：").append(Component.translatable("gui.wuxingxiantu.inner_vision.meridian_" + (status >= 2 ? "shattered" : status == 1 ? "damaged" : "intact"))), right, base + 41, column, status > 0 ? CultivationUi.RED : CultivationUi.GREEN, mx, my);
        CultivationUi.fit(g, Component.literal("体质：").append(Component.translatable("physique.wuxingxiantu." + d.wuxingxiantu$getPhysique().id())), right, base + 60, column, CultivationUi.TEXT, mx, my);
        CultivationUi.fit(g, Component.literal("阳神纯度：" + d.wuxingxiantu$getYinShenPurity()), right, base + 79, column, CultivationUi.CYAN, mx, my);
        heading(g, "灵根", 0, rootsY);
        int[] colors = {0xFFE0C487, 0xFF8BC89D, 0xFF9FBDDE, 0xFFE39485, 0xFFC7AB8A};
        for (int root = 0; root < 5; root++) {
            int x = root % 3 * (column / 3), y = rootsY + 20 + root / 3 * 28;
            boolean owned = (d.wuxingxiantu$getRootMask() & (1 << root)) != 0;
            String key = root == 1 && CultivationLogic.isThunderForm(d) ? "thunder" : root == 3 && CultivationLogic.isExoticForm(d) ? "exotic_fire" : Integer.toString(root);
            g.fill(x, y, x + 18, y + 18, CultivationUi.PANEL);
            String glyph = root == 1 && CultivationLogic.isThunderForm(d) ? "雷" : root == 3 && CultivationLogic.isExoticForm(d) ? "焰" : new String[]{"金", "木", "水", "火", "土"}[root];
            g.drawCenteredString(font, glyph, x + 9, y + 5, owned ? colors[root] : CultivationUi.MUTED);
            String quality = owned ? Component.translatable("quality.wuxingxiantu." + Math.max(0, Math.min(3, d.wuxingxiantu$getRootQuality(root) - 1))).getString() : "未拥有";
            CultivationUi.fit(g, Component.literal(quality), x + 21, y + 5, column / 3 - 23, owned ? CultivationUi.TEXT : CultivationUi.MUTED, mx, my);
            if (mx >= x && mx < x + column / 3 && my >= y && my < y + 20)
                CultivationUi.tooltip(rootHint(root));
        }
        g.drawString(font, "混沌", column / 3 * 2, rootsY + 53, d.wuxingxiantu$hasChaosRoot() ? CultivationUi.GOLD : CultivationUi.MUTED, false);
        if (mx >= column / 3 * 2 && mx < column && my >= rootsY + 48 && my < rootsY + 72)
            CultivationUi.tooltip(rootHint(5));
        heading(g, "领域 · 单选", right, domainsY);
        heading(g, "异火", 0, firesY);
        heading(g, "修炼操作", right, actionsY);
        CultivationUi.fit(g, Component.literal(TribulationManager.canStart(minecraft.player, d) ? "可渡劫" : "未满足渡劫条件"), right, actionsY + 48, column, CultivationUi.MUTED, mx, my);
        int crystals = 0;
        for (var stack : minecraft.player.getInventory().items) if (stack.is(WuXingItems.AURA_CRYSTAL.get())) crystals += stack.getCount();
        if (minecraft.player.getOffhandItem().is(WuXingItems.AURA_CRYSTAL.get())) crystals += minecraft.player.getOffhandItem().getCount();
        CultivationUi.fit(g, Component.literal(CultivationUi.number(CultivationLogic.CRYSTALLIZE_AURA_COST) + " 灵力 → 1 结晶"), right, actionsY + 72, column, CultivationUi.CYAN, mx, my);
        CultivationUi.fit(g, Component.literal("随身结晶：" + crystals), right, actionsY + 91, column, CultivationUi.MUTED, mx, my);
        super.render(g, mx, my, delta);
        if (my >= actionsY + 20 && my < actionsY + 60 && mx >= right && mx < right + column)
            host.setTooltipForNextRenderPass(font.split(Component.literal("渡劫资格沿用当前修炼条件：大境界巅峰、足够修为、七成灵力、引劫材料，并离开洞天。"), 240));
    }
    private void renderEffects(GuiGraphics g, int mx, int my) {
        var effects = minecraft.player.getActiveEffects().stream()
                .sorted(java.util.Comparator.comparing((net.minecraft.world.effect.MobEffectInstance e) -> !e.getEffect().isBeneficial())
                        .thenComparing(e -> e.getEffect().getDisplayName().getString())).toList();
        text(g, "药水状态 · " + effects.size(), 6, CultivationUi.GOLD);
        CultivationUi.rule(g, 0, 27, width);
        if (effects.isEmpty()) text(g, "暂无药水效果", 42, CultivationUi.MUTED);
        for (int i = 0; i < effects.size(); i++) {
            var instance = effects.get(i);
            var effect = instance.getEffect();
            int y = 36 + i * 32;
            int color = effect.isBeneficial() ? CultivationUi.GREEN : effect.getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL ? CultivationUi.RED : CultivationUi.CYAN;
            g.fill(0, y, 2, y + 26, color);
            g.blit(6, y + 3, 0, 18, 18, minecraft.getMobEffectTextures().get(effect));
            Component name = effect.getDisplayName().copy().append(" " + (instance.getAmplifier() + 1));
            CultivationUi.fit(g, name, 31, y + 1, width - 38, CultivationUi.TEXT, mx, my);
            Component duration = net.minecraft.world.effect.MobEffectUtil.formatDuration(instance, 1.0F);
            CultivationUi.fit(g, Component.literal(effect.isBeneficial() ? "增益 · " : effect.getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL ? "减益 · " : "状态 · ")
                    .append(duration), 31, y + 14, width - 38, color, mx, my);
            if (mx >= 0 && mx < width && my >= y && my < y + 27) CultivationUi.tooltip(name.copy().append("\n").append(duration));
        }
    }
}
