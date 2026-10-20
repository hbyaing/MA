package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.ChenghuaPedestalBlock;
import cn.blockforge.generated.generatedmod.ChenghuaPedestalBlockEntity;
import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.ExoticFireColorRegistry;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.PedestalContainer;
import cn.blockforge.generated.generatedmod.RefiningForgeBlock;
import cn.blockforge.generated.generatedmod.RefiningForgeBlockEntity;
import cn.blockforge.generated.generatedmod.RefiningRecipe;
import cn.blockforge.generated.generatedmod.items.FireSeedItem;
import cn.blockforge.generated.generatedmod.registry.ModRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 炼器台界面：深色鎏金面板，配方翻页、产出展示、材料卡实时对照承华台存量，
 * 显示真实异火阶位；开始按钮与服务端做同一套校验，不满足时点名缺什么。
 * 材料卡显示材料本名，悬停物品可查看详细信息；关闭时记住停留的配方页。
 */
public final class RefiningForgeScreen extends Screen {
    private static final int PANEL_WIDTH = 452;
    private static final int PANEL_HEIGHT = 302;

    // 面板内布局（相对 top）。
    private static final int Y_INDEX = 55;
    private static final int Y_SLOT = 74;
    private static final int SLOT_SIZE = 58;
    private static final int Y_OUTPUT = 140;
    private static final int Y_MATERIALS = 40;
    private static final int Y_CARDS = 52;
    private static final int CARD_HEIGHT = 26;
    private static final int Y_INFO1 = 166;
    private static final int Y_INFO2 = 201;
    private static final int Y_FIRE = 195;
    private static final int Y_STATUS = 209;
    private static final int Y_BUTTON = 241;
    private static final int BUTTON_HEIGHT = 26;
    private static final int Y_REASON = 277;
    private static final int CARD_WIDTH = 138;
    private static final int NAV_LEFT = 164, NAV_RIGHT = 264, NAV_Y = 95;
    private static final int ACTION_X = 16, ACTION_WIDTH = PANEL_WIDTH - 32;

    // 主题色。
    private static final int GOLD = 0xFFD8B158;
    private static final int GOLD_BRIGHT = 0xFFFFE9BB;
    private static final int INK = 0xFF9DB1C6;
    private static final int LINE = 0xFF3E4E63;
    private static final int CARD_BG = 0xFF1B2634;
    private static final int OK_GREEN = 0xFF7BE0A0;
    private static final int BAD_RED = 0xFFFF9088;

    private final BlockPos corePos;
    private int recipeIndex;
    /** 页码在客户端本地跨会话记忆：记录上次停留的配方 id，写进 config 目录。 */
    private static final Path PAGE_FILE = FMLPaths.CONFIGDIR.get().resolve("wuxingxiantu_refining_page.json");
    private static String savedRecipeId = readSavedRecipe();
    private boolean pageRestored;
    /** 本帧鼠标悬停要展示详情的物品（产出槽或材料卡）。 */
    private ItemStack hoverStack = ItemStack.EMPTY;
    private int hoverHave;
    private int hoverNeed;
    private boolean hoverIsMaterial;
    private int hoverMouseX, hoverMouseY;

    public RefiningForgeScreen(BlockPos corePos) {
        super(Component.translatable("screen.wuxingxiantu.refining.title"));
        this.corePos = corePos;
    }

    private List<RefiningRecipe> recipes() {
        if (minecraft == null || minecraft.level == null) return List.of();
        return minecraft.level.getRecipeManager().getAllRecipesFor(ModRecipes.REFINING_TYPE.get());
    }

    private RefiningRecipe selectedRecipe() {
        List<RefiningRecipe> values = recipes();
        if (values.isEmpty()) return null;
        recipeIndex = Math.floorMod(recipeIndex, values.size());
        return values.get(recipeIndex);
    }

    // ------------------------------------------------------------- 页码记忆

    /** 首次渲染时把上次关闭界面前停留的配方页恢复出来（配方列表顺序可能变化，按 id 定位）。 */
    private void restorePageOnce() {
        if (pageRestored) return;
        pageRestored = true;
        if (savedRecipeId == null) return;
        List<RefiningRecipe> values = recipes();
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).getId().toString().equals(savedRecipeId)) {
                recipeIndex = i;
                return;
            }
        }
    }

    /** 记录当前停留的配方页并落盘，下次打开炼器台直接回到这一页。 */
    private void rememberPage() {
        RefiningRecipe recipe = selectedRecipe();
        if (recipe == null) return;
        savedRecipeId = recipe.getId().toString();
        try {
            Files.writeString(PAGE_FILE, "{\"recipe\":\"" + savedRecipeId + "\"}");
        } catch (Exception ignored) {
            // 写失败只影响跨重启记忆，本次会话内仍然生效。
        }
    }

    private static String readSavedRecipe() {
        try {
            if (Files.exists(PAGE_FILE)) {
                String raw = Files.readString(PAGE_FILE);
                int begin = raw.indexOf("\"recipe\":\"");
                int end = begin < 0 ? -1 : raw.indexOf('"', begin + 10);
                if (end > begin + 10) return raw.substring(begin + 10, end);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public void onClose() {
        rememberPage();
        super.onClose();
    }

    // ------------------------------------------------------------------ 渲染

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        restorePageOnce();
        hoverStack = ItemStack.EMPTY;
        hoverIsMaterial = false;
        int screenMouseX=mouseX,screenMouseY=mouseY;
        float scale=panelScale();
        mouseX=(int)((mouseX-panelLeft())/scale);mouseY=(int)((mouseY-panelTop())/scale);
        hoverMouseX=mouseX;hoverMouseY=mouseY;
        graphics.pose().pushPose();graphics.pose().translate(panelLeft(),panelTop(),0);graphics.pose().scale(scale,scale,1);
        int left = 0;
        int top = 0;
        int center = left + PANEL_WIDTH / 2;

        List<RefiningRecipe> values = recipes();
        RefiningRecipe recipe = selectedRecipe();
        int currentTier = minecraft == null || minecraft.level == null ? 0
                : RefiningForgeBlock.fireTier(minecraft.level, corePos);
        int currentColor = currentTier == 0 ? 0x667080 : ExoticFireColorRegistry.color(currentTier,
                minecraft == null || minecraft.level == null ? 0L : minecraft.level.getGameTime());
        List<ItemStack> stacks = pedestalStacks();
        int[] have = recipe == null ? new int[0] : availability(recipe, stacks);
        int aura = currentAura();
        Component reason = blockedReason(recipe, have, currentTier, aura);
        // 炼制状态：读取方块实体同步的进度（服务端权威，见 RefiningForgeBlockEntity）。
        RefiningForgeBlockEntity forge = minecraft != null && minecraft.level != null
                && minecraft.level.getBlockEntity(corePos) instanceof RefiningForgeBlockEntity f ? f : null;
        boolean crafting = forge != null && forge.isCrafting();
        boolean ready = forge != null && forge.hasPendingReward();
        float progress = crafting && minecraft != null && minecraft.level != null
                ? forge.craftProgress(minecraft.level.getGameTime()) : 0.0F;
        // 就绪时以“实际炼成的成品”为准显示，避免配方书结果与真实发放的成品不一致时出现“空气”。
        ItemStack pendingReward = forge != null ? forge.pendingReward() : ItemStack.EMPTY;
        // 兜底：若界面判定已就绪但成品暂为空（极端不同步），回退为当前配方的产出名，杜绝显示“空气”。
        if (ready && pendingReward.isEmpty() && recipe != null) {
            pendingReward = recipe.getResultItem(minecraft.level.registryAccess());
        }

        drawPanelBackground(graphics, left, top);
        drawCloseButton(graphics, left, top, mouseX, mouseY);

        RefiningUiAssets.draw(graphics,"title",center-98,top+7,196,18);
        drawSeparator(graphics, left, top + 34);
        graphics.drawCenteredString(font, Component.translatable("screen.wuxingxiantu.refining.recipe_count",
                recipe == null ? 0 : recipeIndex + 1, values.size()), center, top + Y_INDEX, INK);

        drawRecipeArea(graphics, recipe, left, top, center, have, mouseX, mouseY);
        drawInfoLines(graphics, recipe, left, top, center, stacks.size(), aura);
        drawFireArea(graphics, recipe, left, top, center, currentTier, currentColor);
        drawActionArea(graphics, left, top, center, crafting, ready, progress, reason == null,
                pendingReward, mouseX, mouseY);
        drawReasonLine(graphics, left, top, center, crafting, ready, pendingReward, reason);
        graphics.pose().popPose();
        drawHoverTooltip(graphics, screenMouseX, screenMouseY);
        super.render(graphics, screenMouseX, screenMouseY, delta);
    }

    /** 悬停详情：产出槽显示成品完整信息，材料卡额外带上承华台已有数量。 */
    private void drawHoverTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (minecraft == null || hoverStack.isEmpty()) return;
        if (!hoverIsMaterial) {
            graphics.renderTooltip(font, hoverStack, mouseX, mouseY);
            return;
        }
        List<Component> lines = new ArrayList<>(
                hoverStack.getTooltipLines(minecraft.player, TooltipFlag.Default.NORMAL));
        lines.add(Component.translatable("screen.wuxingxiantu.refining.need_count", hoverNeed)
                .withStyle(ChatFormatting.YELLOW));
        lines.add(Component.translatable("screen.wuxingxiantu.refining.pedestal_have", hoverHave, hoverNeed)
                .withStyle(hoverHave >= hoverNeed ? ChatFormatting.GREEN : ChatFormatting.RED));
        graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
    }

    private void drawPanelBackground(GuiGraphics graphics, int left, int top) {
        graphics.fill(left,top,left+PANEL_WIDTH,top+PANEL_HEIGHT,0xFF0A1821);
        RefiningUiAssets.draw(graphics,"landscape",left+3,top+3,PANEL_WIDTH-6,PANEL_HEIGHT-6);
        graphics.fillGradient(left+3,top+3,left+PANEL_WIDTH-3,top+PANEL_HEIGHT-3,0x2510202B,0xA005111B);
        RefiningUiAssets.frame(graphics,"panel",left,top,PANEL_WIDTH,PANEL_HEIGHT,48,11);
    }

    private void drawSeparator(GuiGraphics graphics, int left, int y) {
        RefiningUiAssets.draw(graphics,"separator",left+14,y-3,PANEL_WIDTH-28,6);
    }

    private void drawRecipeArea(GuiGraphics graphics, RefiningRecipe recipe, int left, int top, int center,
                                int[] have, int mouseX, int mouseY) {
        // 产出槽（居中金框）。
        int slotX = center - SLOT_SIZE / 2;
        int slotY = top + Y_SLOT;
        graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE,0xAA13212D);
        RefiningUiAssets.frame(graphics,"output",slotX,slotY,SLOT_SIZE,SLOT_SIZE,24,6);
        if (recipe != null && minecraft != null && minecraft.level != null) {
            ItemStack output = recipe.getResultItem(minecraft.level.registryAccess());
            graphics.pose().pushPose();
            graphics.pose().translate(center-16,slotY+13,0);
            graphics.pose().scale(2,2,1);
            graphics.renderItem(output,0,0);
            graphics.renderItemDecorations(font,output,0,0);
            graphics.pose().popPose();
            graphics.drawCenteredString(font, Component.translatable("screen.wuxingxiantu.refining.output",
                    fitText(output.getHoverName(),104)), center, top + Y_OUTPUT, GOLD_BRIGHT);
            if (inRect(mouseX, mouseY, slotX, slotY, SLOT_SIZE, SLOT_SIZE)) {
                hoverStack = output;
                hoverIsMaterial = false;
            }
        }
        // 翻页按钮（产出槽两侧）。
        drawNavButton(graphics, left + NAV_LEFT, top + NAV_Y, "<", mouseX, mouseY);
        drawNavButton(graphics, left + NAV_RIGHT, top + NAV_Y, ">", mouseX, mouseY);

        // 材料区。
        graphics.drawString(font, Component.translatable("screen.wuxingxiantu.refining.materials"),
                left + 18, top + Y_MATERIALS, GOLD_BRIGHT, false);
        RefiningUiAssets.draw(graphics,"material_rule",left+72,top+Y_MATERIALS+1,PANEL_WIDTH-90,5);
        if (recipe == null) {
            graphics.drawCenteredString(font, Component.translatable("screen.wuxingxiantu.refining.no_recipes"),
                    center, top + Y_CARDS + 16, BAD_RED);
            return;
        }
        int n = recipe.getIngredients().size();
        int rows = Math.max(4,(n+1)/2);
        int step = Math.min(26,102/rows);
        int cardHeight = Math.min(CARD_HEIGHT,step-2);
        int cardWidth = CARD_WIDTH;
        int materialRows = Math.max(1,(n+1)/2);
        for (int cell = 0; cell < rows*2; cell++) {
            int column = cell / rows, row = cell % rows;
            int i = column * materialRows + row;
            int x = left + (column==0?16:PANEL_WIDTH-16-CARD_WIDTH);
            int cardY = top + Y_CARDS + row*step;
            graphics.fill(x,cardY,x+cardWidth,cardY+cardHeight,0x9910202B);
            RefiningUiAssets.frame(graphics,"card",x,cardY,cardWidth,cardHeight,16,4);
            if (row >= materialRows || i >= n) continue;
            int need = recipe.counts().get(i);
            boolean enough = have[i] >= need;
            ItemStack sample = recipe.sample(i);
            if (!sample.isEmpty()) {
                graphics.renderItem(sample, x + 7, cardY + (cardHeight-16)/2);
                graphics.renderItemDecorations(font, sample, x + 7, cardY + (cardHeight-16)/2);
            }
            // 名字行：显示所需材料本身的名字，过宽时截断加省略号（需求数已由下方分数体现，不再重复显示）。
            Component name = sample.isEmpty()
                    ? Component.translatable("screen.wuxingxiantu.refining.unknown_material")
                    : sample.getHoverName();
            Component quantity=Component.translatable("screen.wuxingxiantu.refining.have_need",have[i],need);
            int quantityX=x+cardWidth-9-font.width(quantity);
            int textY=cardY+(cardHeight-font.lineHeight)/2;
            graphics.drawString(font, fitText(name, Math.max(12,quantityX-x-36)),
                    x + 30, textY, 0xFFDDEAF6, false);
            graphics.drawString(font, quantity, quantityX, textY,
                    enough ? OK_GREEN : BAD_RED, false);
            if (inRect(mouseX, mouseY, x, cardY, cardWidth, cardHeight) && !sample.isEmpty()) {
                hoverStack = sample;
                hoverIsMaterial = true;
                hoverHave = have[i];
                hoverNeed = need;
            }
        }
    }

    private void drawInfoLines(GuiGraphics graphics, RefiningRecipe recipe, int left, int top, int center,
                               int pedestalCount, int aura) {
        if (recipe == null) return;
        int x=left+16,w=PANEL_WIDTH-32;
        RefiningUiAssets.infoFrame(graphics,x,top+160,w,22);
        RefiningUiAssets.infoFrame(graphics,x,top+190,w,34);
        RefiningUiAssets.draw(graphics,"aura_icon",x+5,top+163,14,16);
        RefiningUiAssets.draw(graphics,"time_icon",left+272,top+164,12,15);
        RefiningUiAssets.draw(graphics,"pedestal_icon",x+5,top+198,14,18);
        Component auraText = Component.translatable("screen.wuxingxiantu.refining.aura", cn.blockforge.generated.generatedmod.progression.PlayerCrafts.refiningCost(recipe.auraCost(),PlayerProgressionClient.rank("refining")), aura);
        graphics.drawString(font, fitText(auraText,220), left + 40, top + Y_INFO1,
                aura >= cn.blockforge.generated.generatedmod.progression.PlayerCrafts.refiningCost(recipe.auraCost(),PlayerProgressionClient.rank("refining")) ? 0xFFB8DFFF : BAD_RED, false);
        Component timeText = Component.translatable("screen.wuxingxiantu.refining.time", recipe.craftTime() / 20);
        graphics.drawString(font, fitText(timeText,137), left + 291, top + Y_INFO1,
                0xFFEAD6A2, false);
        Component hint = Component.translatable("screen.wuxingxiantu.refining.pedestal_hint", pedestalCount);
        graphics.drawString(font, fitText(hint,244), left+40, top + 190 + (34-font.lineHeight)/2,
                pedestalCount > 0 ? 0xFF9FE0C8 : 0xFF8A98AC);
        if(font.width(hint)>244 && inRect(hoverMouseX,hoverMouseY,left+16,top+190,272,34))
            CultivationUi.tooltip(hint);
    }

    private void drawFireArea(GuiGraphics graphics, RefiningRecipe recipe, int left, int top, int center,
                              int currentTier, int currentColor) {
        RefiningUiAssets.draw(graphics,"fire_icon",left+300,top+197,16,20);
        Component fire = currentTier > 0
                ? Component.translatable("screen.wuxingxiantu.refining.fire_name", currentTier, FireSeedItem.getFireName(currentTier))
                : Component.translatable("screen.wuxingxiantu.refining.fire_none");
        graphics.drawString(font, fitText(fire,112), left+321, top + Y_FIRE, 0xFF000000 | currentColor,false);
        if (recipe != null) graphics.drawString(font,
                fitText(Component.translatable("screen.wuxingxiantu.refining.fire_requirement", currentTier, recipe.requiredFireTier()),112),
                left+321, top + Y_STATUS, currentTier >= recipe.requiredFireTier() ? OK_GREEN : BAD_RED,false);
    }

    /**
     * 炼器操作区三态：炼制中显示百分比进度条；成品就绪显示"领取"按钮；空闲显示"开始炼器"。
     * 就绪时一并显示实际炼成成的成品名。
     */
    private void drawActionArea(GuiGraphics graphics, int left, int top, int center,
                                boolean crafting, boolean ready, float progress, boolean canStart,
                                ItemStack pendingReward, int mouseX, int mouseY) {
        int x = left + ACTION_X;
        int w = ACTION_WIDTH;
        int y = top + Y_BUTTON;
        if (crafting) {
            drawCraftProgress(graphics, x, y, w, center, Math.max(0.0F, Math.min(1.0F, progress)));
            return;
        }
        if (ready) {
            // 在按钮上方标注这次要领取的成品名，杜绝界面只显示"领取"而把成品名藏起来。
            // Product is already named below the button; keep fire status unobstructed.
            boolean hovered = inRect(mouseX, mouseY, x, y, w, BUTTON_HEIGHT);
            RefiningUiAssets.button(graphics,x,y,w,BUTTON_HEIGHT,true,hovered);
            Component label = Component.translatable("screen.wuxingxiantu.refining.collect");
            int textY = y + (BUTTON_HEIGHT - font.lineHeight) / 2;
            graphics.drawCenteredString(font, label, center, textY, GOLD_BRIGHT);
            return;
        }
        drawStartButton(graphics, left, top, center, canStart, mouseX, mouseY);
    }

    /** 炼制进度条：整条背景 + 金色已炼部分 + 居中百分比。 */
    private void drawCraftProgress(GuiGraphics graphics, int x, int y, int w, int center, float progress) {
        RefiningUiAssets.button(graphics,x,y,w,BUTTON_HEIGHT,true,false,progress);
        String pct = (int) Math.floor(progress * 100.0F) + "%";
        int textY = y + (BUTTON_HEIGHT - font.lineHeight) / 2;
        graphics.drawCenteredString(font, Component.literal(pct), center, textY, GOLD_BRIGHT);
    }

    private void drawStartButton(GuiGraphics graphics, int left, int top, int center, boolean enabled,
                                 int mouseX, int mouseY) {
        int x = left + ACTION_X;
        int w = ACTION_WIDTH;
        int y = top + Y_BUTTON;
        boolean hovered = enabled && inRect(mouseX, mouseY, x, y, w, BUTTON_HEIGHT);
        RefiningUiAssets.button(graphics,x,y,w,BUTTON_HEIGHT,enabled,hovered);
        Component label = Component.translatable("screen.wuxingxiantu.refining.start");
        int textY = y + (BUTTON_HEIGHT - font.lineHeight) / 2;
        graphics.drawCenteredString(font, label, center, textY, enabled ? GOLD_BRIGHT : 0xFF8C9AA5);
    }

    private void drawReasonLine(GuiGraphics graphics, int left, int top, int center, boolean crafting,
                                boolean ready, ItemStack pendingReward, Component reason) {
        if (crafting) {
            graphics.drawCenteredString(font, Component.translatable("screen.wuxingxiantu.refining.processing"),
                    center, top + Y_REASON, 0xFFEAD6A2);
        } else if (ready) {
            // 就绪时明确显示成品名，不落成“领取 空气”。
            Component label = pendingReward.isEmpty()
                    ? Component.translatable("screen.wuxingxiantu.refining.ready_collect")
                    : Component.translatable("screen.wuxingxiantu.refining.ready_collect_item",
                            pendingReward.getHoverName());
            graphics.drawCenteredString(font, label, center, top + Y_REASON, OK_GREEN);
        } else if (reason == null) {
            graphics.drawCenteredString(font, Component.translatable("screen.wuxingxiantu.refining.ready"),
                    center, top + Y_REASON, OK_GREEN);
        } else {
            graphics.drawCenteredString(font, fitText(reason,PANEL_WIDTH-40), center, top + Y_REASON, BAD_RED);
        }
    }

    private void drawNavButton(GuiGraphics graphics, int x, int y, String glyph, int mouseX, int mouseY) {
        boolean hovered = inRect(mouseX, mouseY, x, y, 26, 26);
        graphics.setColor(1,1,1,hovered?1:.85F);
        RefiningUiAssets.draw(graphics,glyph.equals("<")?"previous":"next",x,y,26,26);
        graphics.setColor(1,1,1,1);
    }

    private void drawCloseButton(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        int x = left + PANEL_WIDTH - 26;
        int y = top + 8;
        boolean hovered = inRect(mouseX, mouseY, x, y, 18, 18);
        graphics.setColor(1,1,1,hovered?1:.85F);
        RefiningUiAssets.draw(graphics,"close",x,y,18,18);graphics.setColor(1,1,1,1);
    }

    // ------------------------------------------------------------------ 交互

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX=(mouseX-panelLeft())/panelScale();mouseY=(mouseY-panelTop())/panelScale();
        if (button == 0) {
            int mx = (int) mouseX;
            int my = (int) mouseY;
            int left = 0;
            int top = 0;
            int slotY = top + Y_SLOT;
            if (inRect(mx, my, left + PANEL_WIDTH - 26, top + 8, 18, 18)) {
                onClose();
                return true;
            }
            if (inRect(mx, my, left + NAV_LEFT, top + NAV_Y, 26, 26)) {
                recipeIndex--;
                rememberPage();
                return true;
            }
            if (inRect(mx, my, left + NAV_RIGHT, top + NAV_Y, 26, 26)) {
                recipeIndex++;
                rememberPage();
                return true;
            }
            if (inRect(mx, my, left + ACTION_X, top + Y_BUTTON, ACTION_WIDTH, BUTTON_HEIGHT)) {
                RefiningForgeBlockEntity forge = forgeBlockEntity();
                // 炼制中：按钮已变成进度条，不响应点击。
                if (forge != null && forge.isCrafting()) return true;
                // 成品就绪：点击"领取"，从界面领取成品。
                if (forge != null && forge.hasPendingReward()) {
                    // 客户端先行清空本地成品：界面立即回到"开始炼器"、悬浮成品立即消失，
                    // 不再依赖服务端同步包（可能因网络延迟/丢包而残留"领取"按钮与悬浮剑）。
                    // takePendingReward 在客户端只清空字段，其 syncToClients 因 level 非服务端直接返回，无副作用。
                    forge.takePendingReward();
                    Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.alchemy.CollectRefiningPacket(corePos));
                    return true;
                }
                RefiningRecipe recipe = selectedRecipe();
                if (recipe != null && computeReason(recipe) == null) {
                    Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.alchemy.RefiningPacket(corePos, recipe.getId()));
                    // 保留界面打开，让玩家实时看到进度百分比。
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ------------------------------------------------------------------ 校验

    /** 与服务端同一套规则的客户端预检；返回 null 表示可以开炼。 */
    private Component computeReason(RefiningRecipe recipe) {
        List<ItemStack> stacks = pedestalStacks();
        int[] have = recipe == null ? new int[0] : availability(recipe, stacks);
        int currentTier = minecraft == null || minecraft.level == null ? 0
                : RefiningForgeBlock.fireTier(minecraft.level, corePos);
        return blockedReason(recipe, have, currentTier, currentAura());
    }

    private RefiningForgeBlockEntity forgeBlockEntity() {
        if (minecraft == null || minecraft.level == null) return null;
        return minecraft.level.getBlockEntity(corePos) instanceof RefiningForgeBlockEntity forge ? forge : null;
    }

    private int currentAura() {
        if (minecraft == null || minecraft.player == null) return 0;
        CultivationData data = GeneratedMod.data(minecraft.player);
        return data == null ? 0 : Math.round(data.wuxingxiantu$getAura());
    }

    private Component blockedReason(RefiningRecipe recipe, int[] have, int currentTier, int aura) {
        if (recipe == null) return Component.translatable("screen.wuxingxiantu.refining.no_recipes");
        List<Component> parts = new ArrayList<>();
        for (int i = 0; i < have.length; i++) {
            int need = recipe.counts().get(i);
            if (have[i] >= need) continue;
            ItemStack sample = recipe.sample(i);
            parts.add((sample.isEmpty()
                    ? Component.translatable("screen.wuxingxiantu.refining.unknown_material")
                    : sample.getHoverName().copy()).append(Component.literal("×" + (need - have[i]))));
        }
        if (!parts.isEmpty()) {
            net.minecraft.network.chat.MutableComponent list = Component.empty();
            for (int k = 0; k < parts.size(); k++) {
                if (k > 0) list.append(Component.literal("、"));
                list.append(parts.get(k));
            }
            return Component.translatable("screen.wuxingxiantu.refining.missing", list);
        }
        if (currentTier < recipe.requiredFireTier()) {
            return Component.translatable("screen.wuxingxiantu.refining.fire_short",
                    recipe.requiredFireTier(), currentTier);
        }
        if (aura < cn.blockforge.generated.generatedmod.progression.PlayerCrafts.refiningCost(recipe.auraCost(),PlayerProgressionClient.rank("refining"))) {
            return Component.translatable("screen.wuxingxiantu.refining.aura_short", cn.blockforge.generated.generatedmod.progression.PlayerCrafts.refiningCost(recipe.auraCost(),PlayerProgressionClient.rank("refining")));
        }
        return null;
    }

    /** 客户端扫描炉心周围承载物品的承华台（范围与判定条件都与服务端 PedestalContainer.around 一致）。 */
    private List<ItemStack> pedestalStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        if (minecraft == null || minecraft.level == null) return stacks;
        int r = PedestalContainer.SEARCH_RADIUS;
        for (BlockPos probe : BlockPos.betweenClosed(corePos.offset(-r, -1, -r), corePos.offset(r, 2, r))) {
            // 服务端会跳过本体不是承华台的槽位；客户端必须同样过滤，
            // 否则残影方块实体数据会让界面误报“材料齐全”。
            if (!ChenghuaPedestalBlock.isPedestal(minecraft.level.getBlockState(probe))) continue;
            if (minecraft.level.getBlockEntity(probe) instanceof ChenghuaPedestalBlockEntity pedestal
                    && pedestal.hasFloating()) {
                stacks.add(pedestal.floatingStack());
            }
        }
        return stacks;
    }

    /** 与服务端 RefiningRecipe.missing 相同的贪心分配，返回每种材料在承华台上的持有量。 */
    private int[] availability(RefiningRecipe recipe, List<ItemStack> stacks) {
        int n = recipe.getIngredients().size();
        int[] have = new int[n];
        int[] rem = new int[stacks.size()];
        for (int s = 0; s < rem.length; s++) rem[s] = stacks.get(s).getCount();
        for (int i = 0; i < n; i++) {
            Ingredient ingredient = recipe.getIngredients().get(i);
            int need = recipe.counts().get(i);
            for (int s = 0; s < rem.length && need > 0; s++) {
                if (!ingredient.test(stacks.get(s))) continue;
                int used = Math.min(need, rem[s]);
                rem[s] -= used;
                need -= used;
            }
            have[i] = recipe.counts().get(i) - need;
        }
        return have;
    }

    // ------------------------------------------------------------------ 工具

    /** 把文本裁到指定宽度内，超出部分以省略号收尾（材料名长短不一，防止溢出卡片）。 */
    private Component fitText(Component text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        String plain = text.getString();
        int end = plain.length();
        while (end > 0 && font.width(plain.substring(0, end) + "…") > maxWidth) end--;
        return Component.literal(end > 0 ? plain.substring(0, end) + "…" : "…");
    }

    private float panelScale(){return Math.max(.1F,Math.min(1F,Math.min((width-12)/(float)PANEL_WIDTH,(height-12)/(float)PANEL_HEIGHT)));}
    private float panelLeft(){return (width-PANEL_WIDTH*panelScale())/2F;}
    private float panelTop(){return (height-PANEL_HEIGHT*panelScale())/2F;}

    private static boolean inRect(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static int mix(int first, int second, float amount) {
        float t = Math.max(0.0F, Math.min(1.0F, amount));
        int a = (first >>> 24 & 255) + (int) (((second >>> 24 & 255) - (first >>> 24 & 255)) * t);
        int r = (first >> 16 & 255) + (int) (((second >> 16 & 255) - (first >> 16 & 255)) * t);
        int g = (first >> 8 & 255) + (int) (((second >> 8 & 255) - (first >> 8 & 255)) * t);
        int b = (first & 255) + (int) (((second & 255) - (first & 255)) * t);
        return a << 24 | r << 16 | g << 8 | b;
    }

    @Override public boolean isPauseScreen() { return false; }
}
