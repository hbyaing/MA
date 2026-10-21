package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** 槽位、列表与详情分区；三种系统各自发送既有网络请求。 */
public final class CultivationArtsPage extends CultivationPage {
    private enum Tab { ART("功法"), DIVINE("神通"), HEART("心法");
        final String name; Tab(String name) { this.name = name; }
    }
    private Tab tab = Tab.ART;
    private int selectedSlot, listOffset, detailOffset, detailHeight;
    private int bodyY, bodyH, listW, detailX;
    private String search = "";
    private String selected, lastData = "";
    private boolean wheelMode, passiveTarget;
    private int draggingScroll;
    private double scrollGrab;
    private final List<CultivationUi.Icon> slots = new ArrayList<>();
    private CultivationUi.Action equip, clear;
    public CultivationArtsPage() { super("功法"); }
    private CultivationData data() { return GeneratedMod.data(minecraft.player); }
    @Override public int contentHeight(int w, int h) { return Math.max(h, wheelMode ? 264 : w < 280 ? 230 : 180); }
    private List<String> learned() {
        var d = data(); if (d == null) return List.of();
        if (tab == Tab.HEART) return HeartMethodCatalog.METHOD_IDS.stream().filter(d::wuxingxiantu$hasLearnedHeartMethod).toList();
        if (tab == Tab.DIVINE) return Arrays.stream(d.wuxingxiantu$getLearnedDivineArts()).filter(DivineArtCatalog::isValid).toList();
        var learnedArts = Arrays.stream(ArtCatalog.sortLearned(d.wuxingxiantu$getLearnedArts()))
                .filter(id -> !id.equals("art_sword_flight") && !SwordTechniqueManager.isSwordTechnique(id))
                .filter(id -> !passiveTarget || ArtCatalog.isPassiveCompatible(id))
                .filter(id -> search.isBlank() || ArtCatalog.artName(id).getString().contains(search)).toList();
        return learnedArts;
    }
    private Component name(String id) {
        if (id == null || id.isBlank()) return Component.literal("空槽");
        return switch (tab) {
            case DIVINE -> DivineArtCatalog.name(id); case HEART -> HeartMethodCatalog.shortName(id);
            default -> ArtCatalog.labeledName(id);
        };
    }
    private ItemStack icon(String id, boolean divine) {
        if (id == null || id.isBlank()) return ItemStack.EMPTY;
        if (divine) {
            var entry = DivineArtCatalog.entry(id);
            return entry == null ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.get(
                    new ResourceLocation(GeneratedMod.MOD_ID, entry.itemId())));
        }
        else if (tab == Tab.HEART && !wheelMode) id = "heart_method_" + id;
        return new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(GeneratedMod.MOD_ID, id)));
    }
    private String equipped(int slot, boolean divine) {
        if (data() == null) return null;
        String[] ids = divine ? data().wuxingxiantu$getEquippedDivineArts()
                : data().wuxingxiantu$getEquippedArts();
        return slot < ids.length ? ids[slot] : null;
    }
    private boolean unlocked(int slot) { return data() != null && slot < CultivationLogic.unlockedArtSlots(data().wuxingxiantu$getRealm()); }
    private String targetArt() { return passiveTarget ? passiveArt(selectedSlot) : equipped(selectedSlot, tab == Tab.DIVINE); }
    private String passiveArt(int slot) { return data() == null ? null : data().wuxingxiantu$getPassiveArts()[slot]; }
    @Override protected void init() { build(); }
    private void choose(String id) { selected = id; detailOffset = 0; tickButtons(); }
    private void build() {
        clearWidgets(); slots.clear(); equip = clear = null;
        height = contentHeight(width, host.contentViewportHeight());
        var tabs = Arrays.asList(Tab.values());
        int tabW = Math.min(58, (width - 9) / tabs.size());
        for (int i = 0; i < tabs.size(); i++) {
            var t = tabs.get(i);
            addRenderableWidget(new CultivationUi.Tab(i * (tabW + 3), 0, tabW, 20, Component.literal(t.name), b -> {
                tab = t; passiveTarget = false; selected = null; selectedSlot = listOffset = detailOffset = 0; search = ""; wheelMode = false; build(); host.scrollToTop();
            }, () -> tab == t && !wheelMode));
        }
        addRenderableWidget(new CultivationUi.Action(width - 80, 27, 78, 20,
                Component.literal(wheelMode ? "返回配置" : "轮盘配置"), b -> { wheelMode = !wheelMode; build(); host.scrollToTop(); }));
        if (tab == Tab.ART && !wheelMode) for (int i = 0; i < 3; i++) {
            final int slot = i;
            addRenderableWidget(new CultivationUi.Icon(width - 80 + i * 27, 51, 24, Integer.toString(i + 1),
                    () -> icon(passiveArt(slot), false),
                    () -> Component.literal("被动功法槽 " + (slot + 1) + "\n").append(ArtCatalog.isValid(passiveArt(slot)) ? ArtCatalog.labeledName(passiveArt(slot)) : Component.literal("空槽")),
                    b -> { tab = Tab.ART; passiveTarget = true; selectedSlot = slot; selected = passiveArt(slot); wheelMode = false; listOffset = detailOffset = 0; build(); },
                    () -> passiveTarget && selectedSlot == slot));
        }
        if (wheelMode) { buildWheel(); return; }
        if (tab == Tab.HEART) {
            addRenderableWidget(new CultivationUi.Icon(0,50,24,"1",
                    () -> icon(data()==null?null:data().wuxingxiantu$getEquippedHeartMethod(),false),
                    () -> Component.literal("心法槽\n").append(data()==null || data().wuxingxiantu$getEquippedHeartMethod()==null
                            || data().wuxingxiantu$getEquippedHeartMethod().isBlank()?Component.literal("空槽")
                            :HeartMethodCatalog.shortName(data().wuxingxiantu$getEquippedHeartMethod())),
                    b -> { if(data()!=null)choose(data().wuxingxiantu$getEquippedHeartMethod()); },() -> true));
        }
        boolean divine = tab == Tab.DIVINE;
        int count = tab == Tab.HEART ? 0 : divine ? 3 : 8;
        int columns = width < (tab == Tab.ART ? 344 : 280) ? 4 : 8;
        for (int i = 0; i < count; i++) {
            final int slot = i;
            var button = new CultivationUi.Icon(i % columns * 28, 50 + i / columns * 28, 24, Integer.toString(i + 1),
                    () -> icon(equipped(slot, divine), divine),
                    () -> Component.literal((divine ? "神通槽 " : "功法槽 ") + (slot + 1) + "\n")
                            .append(!divine && !unlocked(slot) ? Component.literal("未解锁") : name(equipped(slot, divine))),
                    b -> { passiveTarget = false; selectedSlot = slot; choose(equipped(slot, divine)); build(); }, () -> !passiveTarget && selectedSlot == slot);
            slots.add(addRenderableWidget(button));
        }
        bodyY = count == 0 ? 94 : 68 + ((count + columns - 1) / columns) * 28;
        bodyH = height - bodyY - 30; listW = (width - 12) * 45 / 100; detailX = listW + 12;
        if (tab == Tab.ART) {
            var box = new net.minecraft.client.gui.components.EditBox(font, 34, bodyY - 15, listW - 38, 12, Component.literal("搜索功法"));
            box.setMaxLength(30); box.setHint(Component.literal("搜索")); box.setValue(search);
            box.setResponder(value -> { search = value; listOffset = 0; tickButtons(); });
            addRenderableWidget(box);
        }
        var ids = learned();
        if (selected == null && !ids.isEmpty()) selected = ids.get(0);
        if (selected != null && !ids.contains(selected)) selected = null;
        listOffset = Math.max(0, Math.min(listOffset, Math.max(0, ids.size() * 31 - bodyH)));
        equip = addRenderableWidget(new CultivationUi.Action(detailX, height - 26, width - detailX - 2, 22,
                Component.literal(tab == Tab.HEART ? "运转心法" : "装备到槽 " + (selectedSlot + 1)), b -> equip()));
        if (tab != Tab.HEART) clear = addRenderableWidget(new CultivationUi.Action(0, height - 26, listW, 22,
                Component.literal("卸下槽 " + (selectedSlot + 1)), b -> {
                    if (tab == Tab.DIVINE) Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.EquipDivineArtPacket(selectedSlot, null));
                    else if (passiveTarget) Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.EquipPassiveArtPacket(selectedSlot, null));
                    else Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.EquipArtPacket(selectedSlot, null));
                }));
        tickButtons();
    }
    private void buildWheel() {
        int cx = width / 2, cy = 125, radius = Math.min(56, (width - 50) / 2);
        for (int i = 0; i < 8; i++) {
            final int slot = i;
            double angle = -Math.PI / 2 + i * Math.PI / 4;
            var button = addRenderableWidget(new CultivationUi.Icon(cx + (int) (Math.cos(angle) * radius) - 15,
                    cy + (int) (Math.sin(angle) * radius) - 15, 30, Integer.toString(i + 1),
                    () -> icon(equipped(slot, false), false),
                    () -> Component.literal("功法槽 " + (slot + 1) + "\n").append(unlocked(slot)
                            ? ArtCatalog.isValid(equipped(slot, false)) ? ArtCatalog.labeledName(equipped(slot, false)) : Component.literal("空槽")
                            : Component.literal("未解锁")), b -> openSlot(slot, false), () -> false));
            button.active = unlocked(slot);
        }
        for (int i = 0; i < 3; i++) {
            final int slot = i;
            addRenderableWidget(new CultivationUi.Icon(cx - 49 + i * 34, 222, 30, Integer.toString(i + 1),
                    () -> icon(equipped(slot, true), true), () -> Component.literal("神通外圈 " + (slot + 1) + "\n")
                            .append(DivineArtCatalog.isValid(equipped(slot, true)) ? DivineArtCatalog.name(equipped(slot, true)) : Component.literal("空槽")),
                    b -> openSlot(slot, true), () -> false));
        }
    }
    private void openSlot(int slot, boolean divine) {
        tab = divine ? Tab.DIVINE : Tab.ART; passiveTarget = false; selectedSlot = slot; selected = equipped(slot, divine);
        wheelMode = false; listOffset = detailOffset = 0; build();
    }
    private void equip() {
        if (selected == null || !learned().contains(selected)) return;
        if (tab == Tab.HEART) Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.SelectHeartMethodPacket(selected));
        else if (tab == Tab.DIVINE) Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.EquipDivineArtPacket(selectedSlot, selected));
        else if (passiveTarget) Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.EquipPassiveArtPacket(selectedSlot, selected));
        else if (unlocked(selectedSlot)) Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.EquipArtPacket(selectedSlot, selected));
    }
    private void tickButtons() {
        if (wheelMode) return;
        for (int i = 0; i < slots.size(); i++) slots.get(i).active = tab == Tab.DIVINE || unlocked(i);
        if (equip != null) {
            equip.active = selected != null && learned().contains(selected) && (passiveTarget || tab == Tab.DIVINE || tab == Tab.HEART || unlocked(selectedSlot));
            equip.setMessage(Component.literal(tab == Tab.HEART ? "运转心法" : "装备到" + (passiveTarget ? "被动槽 " : "槽 ") + (selectedSlot + 1)));
        }
        if (clear != null) {
            String id = targetArt();
            clear.active = (passiveTarget || tab == Tab.DIVINE || unlocked(selectedSlot)) && id != null && !id.isBlank();
            clear.setMessage(Component.literal("卸下" + (passiveTarget ? "被动槽 " : "槽 ") + (selectedSlot + 1)));
        }
    }
    @Override public void tick() {
        var d = data();
        String snapshot = d == null ? "" : learned().toString() + Arrays.toString(d.wuxingxiantu$getEquippedArts())
                + Arrays.toString(d.wuxingxiantu$getPassiveArts())
                + Arrays.toString(d.wuxingxiantu$getEquippedDivineArts()) + d.wuxingxiantu$getEquippedHeartMethod() + d.wuxingxiantu$getRealm();
        if (!snapshot.equals(lastData)) { lastData = snapshot; build(); }
        tickButtons();
    }
    private List<Component> details() {
        if (selected == null) return List.of(Component.literal("尚未选择功法"));
        var lines = new ArrayList<Component>(); lines.add(name(selected));
        if (tab == Tab.HEART) lines.add(HeartMethodCatalog.description(selected));
        else if (tab == Tab.DIVINE) {
            lines.add(Component.literal("灵力：" + DivineArtCatalog.costText(selected)));
            lines.add(Component.literal("冷却：" + DivineArtCatalog.cooldownText(selected)));
            lines.add(Component.translatable("gui.wuxingxiantu.art_wheel.detail_divine_realm", DivineArtCatalog.requiredRealm(selected) + 1));
            lines.add(DivineArtCatalog.function(selected));
        } else {
            int mastery = data() == null ? 0 : data().wuxingxiantu$getArtMastery(selected);
            lines.add(Component.literal(ArtCatalog.type(selected).typeName() + " · " + ArtCatalog.masteryName(mastery)));
            float maxMana = data() == null ? 0.0F : data().wuxingxiantu$getMaxMana();
            lines.add(ArtCatalog.manaCostDescription(selected,maxMana));
            lines.add(Component.literal("冷却：" + ArtCatalog.cooldownText(selected, mastery)));
            lines.add(Component.literal("熟练度：" + mastery + " / 100"));
            lines.add(ArtCatalog.function(selected));
            lines.add(Component.literal(ArtCatalog.currentEffect(selected, mastery)));
            lines.add(Component.literal(ArtCatalog.extraEffect(selected, mastery)));
        }
        return lines;
    }
    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        if (wheelMode) {
            g.drawCenteredString(font, "功法八槽", width / 2, 121, CultivationUi.GOLD);
            g.drawCenteredString(font, "神通外圈", width / 2, 208, CultivationUi.CYAN);
            super.render(g, mx, my, delta); return;
        }
        CultivationUi.panel(g, -2, 25, width, bodyY - 44, false);
        CultivationUi.panel(g, -2, bodyY - 19, detailX - 1, bodyH + 23, false);
        CultivationUi.panel(g, detailX - 3, bodyY - 19, width - detailX + 1, bodyH + 23, false);
        CultivationUi.fit(g, Component.literal(tab == Tab.HEART ? "当前心法" : tab == Tab.DIVINE ? "神通三槽" : passiveTarget ? "选择功法装入被动槽" : "已装备功法"),
                6, 34, width - 94, CultivationUi.GOLD, mx, my);
        if (tab == Tab.ART) g.drawString(font, "被动功法", width - 120, 59, CultivationUi.MUTED, false);
        if (tab == Tab.HEART) {
            String current = data() == null ? null : data().wuxingxiantu$getEquippedHeartMethod();
            CultivationUi.fit(g, current == null || current.isBlank() ? Component.literal("无") : HeartMethodCatalog.shortName(current),
                    30, 57, width - 40, CultivationUi.CYAN, mx, my);
        }
        g.drawString(font, "已学习", 5, bodyY - 12, CultivationUi.GOLD, false);
        g.drawString(font, "详情", detailX + 5, bodyY - 12, CultivationUi.GOLD, false);
        CultivationUi.softDivider(g,listW+5,bodyY-16,height-bodyY-16,true);
        var ids = learned();
        host.clipPage(g, 0, bodyY, listW, bodyH);
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i); int y = bodyY + i * 31 - listOffset;
            boolean chosen = id.equals(selected);
            if (chosen || mx >= 0 && mx < listW && my >= y && my < y + 29) g.fill(0, y, listW - 3, y + 29, CultivationUi.PANEL);
            if (chosen) g.fill(0, y, 2, y + 29, CultivationUi.GOLD);
            g.renderItem(icon(id, tab == Tab.DIVINE), 5, y + 5);
            CultivationUi.fit(g, tab == Tab.ART ? ArtCatalog.artName(id) : name(id), 25, y + 5, listW - 30, chosen ? CultivationUi.GOLD : CultivationUi.TEXT, mx, my);
            CultivationUi.fit(g, Component.literal(tab == Tab.HEART ? "心法" : tab == Tab.DIVINE ? "神通" : ArtCatalog.tierName(ArtCatalog.tier(id)) + " " + ArtCatalog.type(id).typeName()),
                    25, y + 17, listW - 30, CultivationUi.MUTED, mx, my);
        }
        if (ids.isEmpty()) g.drawString(font, "尚未习得", 2, bodyY + 5, CultivationUi.MUTED, false);
        g.disableScissor();
        host.clipPage(g, detailX, bodyY, width - detailX - 2, bodyH);
        int y = bodyY + 5 - detailOffset, paragraph = 0;
        for (var text : details()) {
            int color = paragraph++ == 0 ? CultivationUi.GOLD : CultivationUi.TEXT;
            for (var line : font.split(text, Math.max(1, width - detailX - 16))) { g.drawString(font, line, detailX + 5, y, color, false); y += 12; }
            y += 6;
        }
        detailHeight = y - bodyY + detailOffset;
        g.disableScissor();
        scrollbar(g, listW - 2, bodyY, bodyH, ids.size() * 31, listOffset);
        scrollbar(g, width - 2, bodyY, bodyH, detailHeight, detailOffset);
        super.render(g, mx, my, delta);
    }
    private void scrollbar(GuiGraphics g, int x, int y, int h, int total, int offset) {
        if (total <= h) return;
        int track=h-6,thumb = Math.min(track,Math.max(12, track * h / total)), top = y+3 + (track - thumb) * offset / Math.max(1, total - h);
        CultivationUi.scrollbar(g,x,y,h,top,thumb);
    }
    @Override public boolean mouseClicked(double x, double y, int button) {
        if(!wheelMode&&button==0&&y>=bodyY&&y<bodyY+bodyH) {
            int target=x>=listW-8&&x<listW+2?1:x>=width-8&&x<width?2:0;
            int total=target==1?learned().size()*31:detailHeight;
            if(target!=0&&total>bodyH) {
                int track=bodyH-6,thumb=Math.min(track,Math.max(12,track*bodyH/total));
                int offset=target==1?listOffset:detailOffset;
                int top=bodyY+3+(track-thumb)*offset/(total-bodyH);
                scrollGrab=y>=top&&y<top+thumb?y-top:thumb/2.0;
                draggingScroll=target;moveScroll(y);return true;
            }
        }
        if (!wheelMode && button == 0 && x >= 0 && x < listW && y >= bodyY && y < bodyY + bodyH) {
            int index = ((int)y - bodyY + listOffset) / 31;
            var ids = learned(); if (index < ids.size()) choose(ids.get(index));
            return true;
        }
        return super.mouseClicked(x, y, button);
    }
    private void moveScroll(double y) {
        int total=draggingScroll==1?learned().size()*31:detailHeight;
        int track=bodyH-6,max=Math.max(0,total-bodyH),thumb=Math.min(track,Math.max(12,track*bodyH/Math.max(1,total)));
        int offset=(int)Math.round(Math.max(0,Math.min(1,(y-bodyY-3-scrollGrab)/Math.max(1,track-thumb)))*max);
        if(draggingScroll==1)listOffset=offset;else detailOffset=offset;
    }
    @Override public boolean mouseDragged(double x,double y,int button,double dx,double dy) {
        if(draggingScroll!=0){moveScroll(y);return true;}return super.mouseDragged(x,y,button,dx,dy);
    }
    @Override public boolean mouseReleased(double x,double y,int button) {
        if(draggingScroll!=0){draggingScroll=0;return true;}return super.mouseReleased(x,y,button);
    }
    @Override public boolean mouseScrolled(double x, double y, double amount) {
        if (!wheelMode && y >= bodyY && y < bodyY + bodyH) {
            if (x >= 0 && x < listW) listOffset = Math.max(0, Math.min(Math.max(0, learned().size() * 31 - bodyH), listOffset - (int)(amount * 24)));
            else if (x >= detailX) detailOffset = Math.max(0, Math.min(Math.max(0, detailHeight - bodyH), detailOffset - (int)(amount * 24)));
            else return false;
            return true;
        }
        return false;
    }
}
