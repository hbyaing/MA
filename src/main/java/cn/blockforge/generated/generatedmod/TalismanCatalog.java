package cn.blockforge.generated.generatedmod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 符箓目录：登记每一种符箓的 ID、显示名、符文图样、绘制消耗与使用效果。
 * <p>
 * 画符机制参考《魔路》(molu) 的思路，但这里改为"临摹长条符纸"：
 * 符纸是一张 {@link #CANVAS_WIDTH}×{@link #CANVAS_HEIGHT} 的长方形画布，模板符文以
 * 半透明引导笔画叠加显示，玩家用朱砂在拖动中沿引导把符文临写出来（拖动时用 Bresenham
 * 补线防断笔）；提交后按覆盖率与越界率计算得分，并凭得分划分品级：
 * 废符(&lt;60)、下品符(60-79)、中品符(80-94)、极品符(≥95)。
 */
public final class TalismanCatalog {
    /** 长条符纸画布尺寸（宽×高，1:2 长宽比）。 */
    public static final int CANVAS_WIDTH = 16;
    public static final int CANVAS_HEIGHT = 32;

    public enum Effect { FIRE, SHIELD, HEAL, SPEED, GATHER, PURGE, THUNDER, WARD, THORN }

    /** 符箓品级：由画符评分决定。 */
    public enum Quality {
        WASTE("talisman_quality.wuxingxiantu.waste", ChatFormatting.DARK_GRAY),
        LOW("talisman_quality.wuxingxiantu.low", ChatFormatting.GRAY),
        MID("talisman_quality.wuxingxiantu.mid", ChatFormatting.GOLD),
        PERFECT("talisman_quality.wuxingxiantu.perfect", ChatFormatting.AQUA);

        private final String key;
        private final ChatFormatting color;

        Quality(String key, ChatFormatting color) {
            this.key = key;
            this.color = color;
        }
        public String key() { return key; }
        public ChatFormatting color() { return color; }
        public Component title() { return cn.blockforge.generated.generatedmod.text.ItemTextPalette.colored(
                Component.translatable(key),cn.blockforge.generated.generatedmod.text.ItemTextPalette.talismanGrade(ordinal())); }
    }

    private record Entry(String id, String nameKey, Effect effect, ChatFormatting color,
                         String[] glyph, int drawCost) {}

    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();

    private static void put(String id, String nameKey, Effect effect, ChatFormatting color,
                            String[] glyph, int drawCost) {
        ENTRIES.put(id, new Entry(id, nameKey, effect, color, glyph, drawCost));
    }

    /** 品级阈值：达到即至少为对应品级。 */
    private static final int PERFECT_MIN = 95;
    private static final int MID_MIN = 80;
    private static final int LOW_MIN = 60;

    static {
        put("gather", "talisman.wuxingxiantu.gather", Effect.GATHER, ChatFormatting.AQUA,
                new String[]{"................","................","....XXXXXXXX....",".....X.XX.......","..X....XX.......",".......X........",".....XXXXXX.....",".....XXXXXX.....",".....XX.........","....XXXXX.......","....XXXXX.X.....","......XXXXX.....",".......XXXXX....","...XXXXXXXXXX...","...XXX..X..XX...","..XX......XX....","..XX.XXXX.XXX...","..XX..XXX..XX...","...X...XX..XX...","..XXXX.XX.XXX...","..XXXXXXXXXXX...","..X..XXX........","......XX..X.....","...XXXXXXXXX....","..XXXXXXXXXX....","..XX.XXX..XX....","......XX........","......XX........","..XX.XXXX.XX....","..XX.XXX..XX....","................","................"}, 1);
        put("speed", "talisman.wuxingxiantu.speed", Effect.SPEED, ChatFormatting.GREEN,
                new String[]{"................","................","....XX.......X..","....X.....XXX...",".............X..",".......XX...XX..",".............X..","..X....XXX......",".......XXX......","..X....XX.......","......X.........","....XX..XX......","....XX..XXX.....",".......X..X.....",".....XX..XX..X..",".....X...X......",".....X.XX.......",".....X.X........",".....X.X.X......",".....X..X.......",".....XX.........",".....XX..X......","......XXX....X..","............X...",".............X..","......XXX.......","..X...XX.....X..","...........X....","..XX.......X....","..XXX......X.X..","................","................"}, 1);
        put("thunder", "talisman.wuxingxiantu.thunder", Effect.THUNDER, ChatFormatting.YELLOW,
                new String[]{"................","................","...X....XXX.....","...X...XXXXXXX..","......X..X..XX..",".....XX.XXX.....","........XXX.....",".........XXXXX..","..X.X...XXXXX...","...XX..X..X.....","..X.X....XX.....","....X...XXXX....",".......XXXX.X...","..X....XXXX.XX..","......XX.XXX....","......XX.XXX.X..","......XXXXXXX...",".........XX.XX..","...XX.....X.XX..","..X.X...XX.XXX..","..X.X...XX.XX...","....X..XXX.XXX..","..X.X...XX.XX...","...XX..XXX.XXX..","...XX...XX.XXX..","...X....XX...X..","...X...XX.X..X..","...X..XXX..X....","...X..XX..X..X..","..XX..XX........","................","................"}, 2);
        put("ward", "talisman.wuxingxiantu.ward", Effect.WARD, ChatFormatting.DARK_PURPLE,
                new String[]{"................","................","..........XXXX..",".........XXXXX..","...........XXX..",".........XXXX...","..........XXXX..","......XXXXXXXX..","...........XX...","...........XXX..","........XXXXXX..","........XX..XX..","......XXXXXX....",".....XXXX..XX...","..X..XXXX..X.X..","..XXXXX.XXXX.X..","..XXXX..X.XXXX..","..XXXXX....XXX..","..XXXXX....XXX..","..XXXXX....XXX..","..XXXXX.....XX..","..XXXX......XX..","..XXX......XXX..","..XX......XXXX..","..XX......X.XX..","............XX..","............XX..","..X........XXX..","..XXXXX......X..","..XXXXXX..XX....","................","................"}, 1);
        put("thorn", "talisman.wuxingxiantu.thorn", Effect.THORN, ChatFormatting.DARK_GREEN,
                new String[]{"................","................","..XXXXXXX.......","..X.XX.X.XX.XX..","...XX..XXXXXXX..",".......XXXXXX...","....XXXXXXXX....","...XXX...XXX.X..","..XX.X...XXX.X..","...XX...XXXX....",".......XXXX..X..","....XXXXXXXXX...","....XXXXXX.X....","..X..XXXX.......","..XXX..X........","..XX............",".....X..........",".............X..","................",".....XXXXXXX....","....XXXX.XXXXX..","..XXX.X....XXX..","..XXXX......XX..","....XXXX.X..XX..",".....XXXXX...X..","..XX.XXXXXXXXX..","..XXXX..XXX.XX..","...XXX....XXX...",".........XXXX...","................","................","................"}, 2);
        put("fire", "talisman.wuxingxiantu.fire", Effect.FIRE, ChatFormatting.RED,
                new String[]{"................","................","....XXXXXX......","....XXXXXXXX....","....XXXXX.......",".....XXXXX......","......XX........","...XXXXXXXXXX...","..XXXXXXXXXXX...","..XX..XX...X....","..XX.XXX........",".....XXX........","......XXX.......","...XXXXXXX......","...XXXXXXXX.....","...X.XXX....XX..",".....XXX...XXX..",".....XXX...XX...","....XXX.....X...","....XXXXX.......","...XXXXXX.......","..XXXXX.XXX.....","..XX....XXX.X...","....XXX.X.XXX...","..XXXXXXX.XXX...","..XXXXXX.XXX....","..X.XXXX.XXXX...","..XXXXX.XXX.....","..XXX..XXX......",".....XXXX.......","................","................"}, 2);
        put("purge", "talisman.wuxingxiantu.purge", Effect.PURGE, ChatFormatting.GOLD,
                new String[]{"................","................",".....X.X..XX....","....XX..X.XX....",".......XXXX.....",".....X.X........","....XX.XXXX.....","...XX..XX.......","........X.......","......XXXXX.....",".....X.XX.XX....","...X......X.....","...XX.XX.XX.....","...XX..X..X.....","...X...X..X.....","....XXXX.XX.....","...XX..X........","..XX...X..X.....","...X.XX...XX....","..XX.X....XX....","..X........X....","..X.XXX....X....","..X.XXXXX..X....","..XXXXX..XXX....","..XXXXX.XX.XX...","...X..X.XX.X....","...X.XX.XX......","..X.XXXXX.......","..X....X........","...XXXX......X..","................","................"}, 1);
        put("shield", "talisman.wuxingxiantu.shield", Effect.SHIELD, ChatFormatting.GOLD,
                new String[]{
                        "................",
                        "................",
                        "................",
                        "................",
                        "...XXXXXXXXXXX..",
                        "...X.........X..",
                        "...X.........X..",
                        "...X.........X..",
                        "...X.........X..",
                        "...X.........X..",
                        "...X.........X..",
                        "...X.........X..",
                        "...XXXXXXXXXXX..",
                        "...X.........X..",
                        "...X.........X..",
                        "...X.........X..",
                        "...X.........X..",
                        "...X.........X..",
                        "...XXXXXXXXXXX..",
                        "...X.........X..",
                        "...X.........X..",
                        "...X.........X..",
                        "...X.........X..",
                        "....X.......X...",
                        "....X.......X...",
                        ".....X.....X....",
                        "......X...X.....",
                        "......X...X.....",
                        ".......X.X......",
                        ".......X.X......",
                        "........X.......",
                        "................"}, 1);
        put("heal", "talisman.wuxingxiantu.heal", Effect.HEAL, ChatFormatting.GREEN,
                new String[]{
                        "................",
                        "................",
                        "................",
                        ".....X..X..X....",
                        ".....X..X..X....",
                        ".....X..X..X....",
                        ".....X..X..X....",
                        ".....X..X..X....",
                        ".....X..X..X....",
                        ".....XXXXXXX....",
                        "........X.......",
                        "........X.......",
                        "........X.......",
                        "........X.......",
                        "........X.......",
                        "........X.......",
                        "..XXXXXXXXXXXXX.",
                        "........X.......",
                        "........X.......",
                        "........X.......",
                        "........X.......",
                        "........X.......",
                        "........X.......",
                        "........X.......",
                        "........X.......",
                        "........X.......",
                        "........X.......",
                        "........X.......",
                        "........X.......",
                        "........X.......",
                        "................",
                        "................"}, 1);
    }

    private TalismanCatalog() {}

    public static boolean has(String id) {
        return ENTRIES.containsKey(id);
    }

    /** 全部已登记的符箓 ID（保持登记顺序）。 */
    public static List<String> ids() {
        return List.of("gather", "speed", "thunder", "ward", "thorn", "fire", "purge");
    }

    public static String nameKey(String id) {
        Entry entry = ENTRIES.get(id);
        return entry == null ? "talisman.wuxingxiantu.unknown" : entry.nameKey();
    }

    public static Component title(String id) {
        Entry entry = ENTRIES.get(id);
        if (entry == null) return Component.translatable("talisman.wuxingxiantu.unknown");
        return Component.translatable(entry.nameKey()).withStyle(entry.color());
    }

    public static ChatFormatting color(String id) {
        Entry entry = ENTRIES.get(id);
        return entry == null ? ChatFormatting.GRAY : entry.color();
    }

    /** 目标符文的布尔矩阵（{@code [HEIGHT][WIDTH]}，row-major）。 */
    public static boolean[][] glyph(String id) {
        Entry entry = ENTRIES.get(id);
        boolean[][] grid = new boolean[CANVAS_HEIGHT][CANVAS_WIDTH];
        if (entry == null) return grid;
        for (int r = 0; r < CANVAS_HEIGHT; r++) {
            String row = r < entry.glyph().length ? entry.glyph()[r] : "";
            for (int c = 0; c < CANVAS_WIDTH; c++) {
                grid[r][c] = c < row.length() && row.charAt(c) == 'X';
            }
        }
        return grid;
    }

    /** 目标图样中总共需要涂黑的格子数。 */
    public static int targetCount(String id) {
        int count = 0;
        for (boolean[] row : glyph(id)) for (boolean cell : row) if (cell) count++;
        return count;
    }

    /**
     * 画符评分：覆盖率减去越界惩罚，再换算成 0~100 分。
     * coverage = 命中格 / 模板总格；penalty = 越界格 / 模板总格（越界即涂在模板之外的朱砂）。
     */
    public static float score(String id, boolean[][] drawn) {
        boolean[][] target = glyph(id);
        int total = targetCount(id);
        if (total == 0 || drawn == null) return 0.0F;
        int hit = 0;
        int miss = 0;
        for (int r = 0; r < CANVAS_HEIGHT; r++) {
            for (int c = 0; c < CANVAS_WIDTH; c++) {
                boolean draw = r < drawn.length && c < drawn[r].length && drawn[r][c];
                if (!draw) continue;
                if (target[r][c]) hit++;
                else miss++;
            }
        }
        float coverage = hit / (float) total;
        float penalty = miss / (float) total;
        return Math.max(0.0F, (coverage - penalty) * 100.0F);
    }

    /** 由评分换成品级。 */
    public static Quality qualityOf(float score) {
        if (score >= PERFECT_MIN) return Quality.PERFECT;
        if (score >= MID_MIN) return Quality.MID;
        if (score >= LOW_MIN) return Quality.LOW;
        return Quality.WASTE;
    }

    /** 描红是否成功：达到最低品级（≥60 分，即非废符）即视为成符。 */
    public static boolean isSuccessful(String id, boolean[][] drawn) {
        return qualityOf(score(id, drawn)) != Quality.WASTE;
    }

    /** 需要命中目标格子的下限数（仅供客户端提示）。 */
    public static int succeedThreshold(String id) {
        return (int) Math.ceil(targetCount(id) * (LOW_MIN / 100.0F));
    }

    /** 绘制/镌刻一次消耗的朱砂墨（或金锭）数量。 */
    public static int drawCost(String id) {
        Entry entry = ENTRIES.get(id);
        return entry == null ? 1 : entry.drawCost();
    }

    /** 网包编码：布尔矩阵压缩为位集字节数组（{@code W*H} 位）。 */
    public static byte[] encode(boolean[][] grid) {
        byte[] data = new byte[(CANVAS_WIDTH * CANVAS_HEIGHT + 7) / 8];
        for (int r = 0; r < CANVAS_HEIGHT; r++) {
            for (int c = 0; c < CANVAS_WIDTH; c++) {
                boolean on = r < grid.length && c < grid[r].length && grid[r][c];
                if (on) {
                    int idx = r * CANVAS_WIDTH + c;
                    data[idx >> 3] |= (byte) (1 << (idx & 7));
                }
            }
        }
        return data;
    }

    /** 网包解码：位集字节数组还原布尔矩阵。 */
    public static boolean[][] decode(byte[] data) {
        boolean[][] grid = new boolean[CANVAS_HEIGHT][CANVAS_WIDTH];
        if (data == null) return grid;
        for (int idx = 0; idx < CANVAS_WIDTH * CANVAS_HEIGHT; idx++) {
            if ((data[idx >> 3] & (1 << (idx & 7))) != 0) {
                grid[idx / CANVAS_WIDTH][idx % CANVAS_WIDTH] = true;
            }
        }
        return grid;
    }

    /**
     * 结算符箓效果。绘制台/拓印台/直接右键符咒都走到这里。
     * 返回是否成功触发（符咒是否应被消耗）。
     */
    public static boolean effect(String id, Player player, Level level, ItemStack stack) {
        Entry entry = ENTRIES.get(id);
        if (entry == null || player == null) return false;
        if (level.isClientSide) return true;
        return TalismanEffects.activate(entry.effect(), player, stack);
    }

    /** Stable model codes do not change when catalog order changes. */
    public static int modelCode(String id) {
        return switch(id) { case "gather"->1; case "speed"->2; case "thunder"->3;
            case "ward"->4; case "thorn"->5; case "fire"->6; case "purge"->7; default->0; };
    }
}
