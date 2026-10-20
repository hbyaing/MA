package cn.blockforge.generated.generatedmod.mindscape;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/** Server-built, reusable arenas. All decoration is inert vanilla geometry. */
public final class MindscapeArenas {
    public static final int HALF_SIZE = 28;
    public static final int FLOOR = 64;
    public static final int CEILING = 92;
    private static final int WIDTH = HALF_SIZE * 2 + 1;
    private static final Block[] ELEMENTS = {
            Blocks.RED_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.GREEN_TERRACOTTA,
            Blocks.WHITE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA
    };

    private MindscapeArenas() {}

    /** Rebuilds the complete owned volume, including air, when a slot is reused. */
    public static void prepare(ServerLevel level, BlockPos center, String arenaId) {
        Theme theme = Theme.forId(arenaId);
        Plan plan = new Plan();
        for (int x = -HALF_SIZE; x <= HALF_SIZE; x++) {
            for (int z = -HALF_SIZE; z <= HALF_SIZE; z++) {
                plan.put(x, -1, z, Blocks.SMOOTH_STONE);
                plan.put(x, 0, z, floor(theme, x, z));
            }
        }
        switch (theme) {
            case VISION -> vision(plan);
            case ECHO -> echo(plan);
            case MIASMA -> miasma(plan);
            case TASTE -> taste(plan);
            case TOUCH -> touch(plan);
            case DELUSION -> delusion(plan);
            case TREASURE -> treasure(plan);
            case FEAST -> feast(plan);
            case PURSUIT -> pursuit(plan);
            case HEART -> heart(plan);
            case TEST -> test(plan);
        }

        // Keep both spawn volumes and the central fighting floor free of scenery.
        plan.clear(-4, 1, -16, 4, 10, -8);
        plan.clear(-5, 1, 3, 5, 12, 13);
        for (int x = -9; x <= 9; x++) {
            for (int z = -9; z <= 9; z++) {
                if (x * x + z * z <= 81) plan.clear(x, 1, z, x, 10, z);
            }
        }
        placeAnchors(plan, center, theme);
        enclose(plan, theme);
        plan.apply(level, center);
    }

    /** Positions are pillar bases at Y=65, ordered clockwise from north. */
    public static List<BlockPos> anchors(BlockPos center) {
        return List.of(at(center, 0, FLOOR + 1, -20), at(center, 19, FLOOR + 1, -6),
                at(center, 12, FLOOR + 1, 16), at(center, -12, FLOOR + 1, 16),
                at(center, -19, FLOOR + 1, -6));
    }

    public static boolean inside(BlockPos center, Vec3 position) {
        return Math.abs(position.x - (center.getX() + 0.5)) <= 26.0
                && Math.abs(position.z - (center.getZ() + 0.5)) <= 26.0
                && position.y >= FLOOR + 1 && position.y <= CEILING - 2;
    }

    public static Vec3 playerSpawn(BlockPos center) {
        return new Vec3(center.getX() + 0.5, FLOOR + 1.1, center.getZ() - 11.5);
    }

    public static Vec3 bossSpawn(BlockPos center) {
        return new Vec3(center.getX() + 0.5, FLOOR + 1.1, center.getZ() + 8.5);
    }

    /** Change only the cap; the caller passes a position returned by anchors(). */
    public static void setAnchorActive(ServerLevel level, BlockPos anchor, boolean active) {
        BlockPos cap = new BlockPos(anchor.getX(), FLOOR + 2, anchor.getZ());
        BlockState state = (active ? Blocks.SEA_LANTERN : Blocks.REDSTONE_LAMP).defaultBlockState();
        if (!level.getBlockState(cap).equals(state)) level.setBlock(cap, state, 2);
    }

    public static boolean isAnchor(BlockPos center, BlockPos clicked) {
        if (clicked.getY() < FLOOR || clicked.getY() > FLOOR + 3) return false;
        for (BlockPos anchor : anchors(center)) {
            if (Math.abs(anchor.getX() - clicked.getX()) <= 1
                    && Math.abs(anchor.getZ() - clicked.getZ()) <= 1) return true;
        }
        return false;
    }

    private static BlockPos at(BlockPos center, int x, int y, int z) {
        return new BlockPos(center.getX() + x, y, center.getZ() + z);
    }

    private static Block floor(Theme theme, int x, int z) {
        int radius = (int) Math.round(Math.sqrt(x * x + z * z));
        int square = Math.max(Math.abs(x), Math.abs(z));
        int manhattan = Math.abs(x) + Math.abs(z);
        // Inlaid light stays flush with the floor and never blocks movement.
        if (Math.abs(x) % 12 == 0 && Math.abs(z) % 12 == 0) return Blocks.SEA_LANTERN;
        return switch (theme) {
            case VISION -> radius == 9 || radius == 18 || radius == 24
                    ? ELEMENTS[sector(x, z)] : ((x + z) % 6 == 0 ? Blocks.QUARTZ_BRICKS : Blocks.SMOOTH_QUARTZ);
            case ECHO -> radius % 7 == 0 ? Blocks.WAXED_CUT_COPPER
                    : ((x + z) % 3 == 0 ? Blocks.POLISHED_DEEPSLATE : Blocks.DEEPSLATE_TILES);
            case MIASMA -> radius >= 14 && radius <= 16 ? Blocks.GREEN_STAINED_GLASS
                    : ((x * x + z * 3) % 7 == 0 ? Blocks.MOSS_BLOCK : Blocks.GREEN_TERRACOTTA);
            case TASTE -> Math.abs(x) <= 3 || Math.abs(z) <= 3 ? Blocks.RED_TERRACOTTA
                    : ((Math.floorDiv(x, 4) + Math.floorDiv(z, 4)) % 2 == 0 ? Blocks.SMOOTH_QUARTZ : Blocks.BRICKS);
            case TOUCH -> radius == 11 || radius == 22 ? Blocks.CYAN_STAINED_GLASS
                    : ((x - z) % 7 == 0 ? Blocks.AMETHYST_BLOCK : Blocks.CALCITE);
            case DELUSION -> square % 7 == 0 ? Blocks.PURPLE_TERRACOTTA
                    : ((Math.floorDiv(x, 3) + Math.floorDiv(z, 3)) % 2 == 0 ? Blocks.SMOOTH_BASALT : Blocks.END_STONE_BRICKS);
            case TREASURE -> manhattan == 12 || manhattan == 25 || square == 23 ? Blocks.GOLD_BLOCK
                    : ((x + z) % 6 == 0 ? Blocks.CHISELED_POLISHED_BLACKSTONE : Blocks.POLISHED_BLACKSTONE_BRICKS);
            case FEAST -> Math.abs(x) <= 5 ? Blocks.PURPLE_TERRACOTTA
                    : (Math.abs(x) >= 12 && Math.abs(x) <= 15 && Math.abs(z) <= 19
                    ? Blocks.PURPLE_STAINED_GLASS : Blocks.DARK_OAK_PLANKS);
            case PURSUIT -> radius == 8 || radius == 16 || radius == 23 ? Blocks.SMOOTH_SANDSTONE
                    : (radius <= 4 ? Blocks.CHISELED_QUARTZ_BLOCK : ELEMENTS[sector(x, z)]);
            case HEART -> (x == 0 || z == 0 || x == z || x == -z) ? Blocks.CRYING_OBSIDIAN
                    : (radius == 18 ? Blocks.POLISHED_BLACKSTONE : Blocks.OBSIDIAN);
            case TEST -> square % 8 == 0 ? Blocks.POLISHED_ANDESITE : Blocks.SMOOTH_STONE;
        };
    }

    private static int sector(int x, int z) {
        double angle = Math.atan2(z, x) + Math.PI / 2.0 + Math.PI / 5.0;
        if (angle < 0) angle += Math.PI * 2.0;
        return ((int) (angle / (Math.PI * 2.0 / 5.0))) % 5;
    }

    private static void vision(Plan p) {
        for (int sx : new int[]{-1, 1}) for (int sz : new int[]{-1, 1}) {
            prism(p, sx * 13, sz * 9, 4, Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.QUARTZ_PILLAR);
            int x = sx * 21, z = sz * 21;
            p.fill(x - 2, 1, z - 2, x - 2, 7, z + 2, Blocks.QUARTZ_PILLAR);
            p.fill(x + 2, 1, z - 2, x + 2, 7, z + 2, Blocks.QUARTZ_PILLAR);
            p.fill(x - 3, 8, z - 3, x + 3, 8, z + 3, Blocks.SMOOTH_QUARTZ);
            p.put(x, 7, z, Blocks.SEA_LANTERN);
            p.put(x, 9, z, Blocks.MAGENTA_STAINED_GLASS);
        }
    }

    private static void echo(Plan p) {
        for (int sx : new int[]{-1, 1}) for (int sz : new int[]{-1, 1}) {
            rock(p, sx * 13, sz * 10, 5, Blocks.COBBLED_DEEPSLATE, Blocks.POLISHED_DEEPSLATE);
        }
        for (int n = -24; n <= 24; n += 6) {
            int height = 7 + Math.floorMod(n, 3);
            for (int side : new int[]{-1, 1}) {
                p.fill(n - 1, 1, side * 25, n + 1, height, side * 25, Blocks.DEEPSLATE_BRICKS);
                p.fill(side * 25, 1, n - 1, side * 25, height, n + 1, Blocks.DEEPSLATE_BRICKS);
            }
        }
        // A high broken cornice suggests a cavern without lowering combat headroom.
        for (int n = -25; n <= 25; n++) for (int side : new int[]{-1, 1}) {
            p.fill(n, 11, side * 24 - 1, n, 12, side * 24 + 1, Blocks.DEEPSLATE_TILES);
            p.fill(side * 24 - 1, 11, n, side * 24 + 1, 12, n, Blocks.DEEPSLATE_TILES);
            if (n % 8 == 0) {
                p.put(n, 10, side * 24, Blocks.SEA_LANTERN);
                p.put(side * 24, 10, n, Blocks.SEA_LANTERN);
            }
        }
    }

    private static void miasma(Plan p) {
        for (int sx : new int[]{-1, 1}) for (int sz : new int[]{-1, 1}) {
            deadTree(p, sx * 22, sz * 18, sx);
            deadTree(p, sx * 16, sz * 24, -sx);
            rock(p, sx * 14, sz * 8, 3, Blocks.MOSSY_STONE_BRICKS, Blocks.STRIPPED_DARK_OAK_LOG);
        }
        for (int side : new int[]{-1, 1}) {
            for (int z = -12; z <= 12; z++) {
                p.put(side * 24, 0, z, Blocks.LIME_STAINED_GLASS);
                p.put(side * 23, 0, z, Blocks.MOSSY_COBBLESTONE);
            }
        }
    }

    private static void taste(Plan p) {
        for (int side : new int[]{-1, 1}) {
            for (int z : new int[]{-12, 11}) table(p, side * 22, z, 1, 5, Blocks.BRICKS, Blocks.SMOOTH_QUARTZ);
            for (int z : new int[]{-8, 8}) table(p, side * 13, z, 1, 2, Blocks.POLISHED_GRANITE, Blocks.WHITE_TERRACOTTA);
            int x = side * 13;
            p.fill(x - 3, 1, 23, x + 3, 6, 25, Blocks.BRICKS);
            p.fill(x - 1, 1, 22, x + 1, 3, 22, Blocks.POLISHED_BLACKSTONE);
            p.put(x, 2, 22, Blocks.SHROOMLIGHT);
            p.fill(x - 1, 7, 24, x + 1, 11, 25, Blocks.BRICKS);
        }
        for (int x = -21; x <= 21; x += 7) {
            p.fill(x, 1, -25, x, 7, -25, Blocks.QUARTZ_PILLAR);
            p.put(x, 8, -25, Blocks.SHROOMLIGHT);
        }
        p.fill(-22, 9, -25, 22, 9, -25, Blocks.RED_TERRACOTTA);
    }

    private static void touch(Plan p) {
        for (int sx : new int[]{-1, 1}) for (int sz : new int[]{-1, 1}) {
            prism(p, sx * 13, sz * 9, 4, Blocks.AMETHYST_BLOCK, Blocks.CALCITE);
            prism(p, sx * 23, sz * 21, 9, Blocks.PURPLE_STAINED_GLASS, Blocks.AMETHYST_BLOCK);
            prism(p, sx * 20, sz * 24, 6, Blocks.CYAN_STAINED_GLASS, Blocks.CALCITE);
        }
        for (int side : new int[]{-1, 1}) {
            p.fill(-8, 0, side * 24 - 1, 8, 0, side * 24 + 1, Blocks.CYAN_STAINED_GLASS);
            for (int x = -8; x <= 8; x += 4) p.put(x, -1, side * 24, Blocks.SEA_LANTERN);
        }
    }

    private static void delusion(Plan p) {
        for (int sx : new int[]{-1, 1}) for (int sz : new int[]{-1, 1}) {
            mirror(p, sx * 14, sz * 10);
            arch(p, sx * 17, sz * 24, 3, 7, Blocks.END_STONE_BRICKS, Blocks.PURPLE_TERRACOTTA, false);
            arch(p, sx * 24, sz * 17, 3, 7, Blocks.END_STONE_BRICKS, Blocks.PURPLE_TERRACOTTA, true);
        }
        for (int side : new int[]{-1, 1}) {
            p.fill(-22, 10, side * 25, 22, 10, side * 25, Blocks.PURPUR_BLOCK);
            p.fill(side * 25, 10, -22, side * 25, 10, 22, Blocks.PURPUR_BLOCK);
        }
    }

    private static void treasure(Plan p) {
        for (int sx : new int[]{-1, 1}) for (int sz : new int[]{-1, 1}) {
            int x = sx * 14, z = sz * 9;
            p.fill(x - 1, 1, z - 1, x + 1, 1, z + 1, Blocks.POLISHED_BLACKSTONE);
            p.fill(x, 2, z, x, 5, z, Blocks.GOLD_BLOCK);
            p.fill(x - 1, 6, z - 1, x + 1, 6, z + 1, Blocks.CHISELED_POLISHED_BLACKSTONE);
            p.put(x, 7, z, Blocks.SEA_LANTERN);
            rock(p, sx * 22, sz * 22, 4, Blocks.RAW_GOLD_BLOCK, Blocks.GOLD_BLOCK);
        }
        arch(p, 0, 25, 6, 10, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.GOLD_BLOCK, false);
        arch(p, 0, -25, 6, 8, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.GOLD_BLOCK, false);
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            if (Math.abs(x) == 3 || Math.abs(z) == 3) p.put(x, 14, z, Blocks.GOLD_BLOCK);
        }
        p.put(0, 13, 0, Blocks.SEA_LANTERN);
        p.fill(0, 14, 0, 0, 18, 0, Blocks.IRON_BARS);
    }

    private static void feast(Plan p) {
        for (int side : new int[]{-1, 1}) {
            for (int z : new int[]{-13, 9}) {
                table(p, side * 19, z, 2, 5, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.PURPUR_BLOCK);
                for (int dishZ = z - 3; dishZ <= z + 3; dishZ += 3) {
                    p.put(side * 19, 3, dishZ, Blocks.MELON);
                    p.put(side * 18, 3, dishZ, Blocks.RED_MUSHROOM_BLOCK);
                }
            }
            for (int z : new int[]{-8, 8}) table(p, side * 12, z, 1, 1, Blocks.DARK_OAK_LOG, Blocks.RED_TERRACOTTA);
            arch(p, side * 16, 25, 5, 9, Blocks.NETHER_BRICKS, Blocks.PURPUR_BLOCK, false);
            arch(p, side * 24, -17, 4, 8, Blocks.NETHER_BRICKS, Blocks.PURPUR_BLOCK, true);
        }
        // Wine channels are colored glass over light, never a flowing liquid.
        for (int x : new int[]{-14, 14}) for (int z = -18; z <= 18; z += 6) p.put(x, -1, z, Blocks.SEA_LANTERN);
    }

    private static void pursuit(Plan p) {
        int[][] shrines = {{0, -24}, {23, -7}, {14, 20}, {-14, 20}, {-23, -7}};
        for (int i = 0; i < shrines.length; i++) {
            int x = shrines[i][0], z = shrines[i][1];
            boolean rotated = Math.abs(x) > Math.abs(z);
            arch(p, x, z, 2, 7, Blocks.SMOOTH_SANDSTONE, ELEMENTS[i], rotated);
            p.put(x, 8, z, Blocks.SEA_LANTERN);
            p.put(x, 9, z, ELEMENTS[i]);
        }
        for (int sx : new int[]{-1, 1}) for (int sz : new int[]{-1, 1}) {
            p.fill(sx * 21 - 2, 1, sz * 22 - 2, sx * 21 + 2, 1, sz * 22 + 2, Blocks.SMOOTH_SANDSTONE);
            p.fill(sx * 21 - 1, 2, sz * 22 - 1, sx * 21 + 1, 2, sz * 22 + 1, Blocks.CUT_SANDSTONE);
            p.put(sx * 21, 3, sz * 22, Blocks.CHISELED_SANDSTONE);
        }
    }

    private static void heart(Plan p) {
        for (int sx : new int[]{-1, 1}) for (int sz : new int[]{-1, 1}) {
            rock(p, sx * 14, sz * 10, 4, Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN);
            arch(p, sx * 18, sz * 24, 3, 9, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRYING_OBSIDIAN, false);
        }
        for (int side : new int[]{-1, 1}) {
            p.fill(side * 25, 1, -3, side * 25, 11, 3, Blocks.OBSIDIAN);
            p.fill(side * 24, 4, -1, side * 24, 8, 1, Blocks.CRYING_OBSIDIAN);
        }
    }

    private static void test(Plan p) {
        for (int sx : new int[]{-1, 1}) for (int sz : new int[]{-1, 1}) {
            p.fill(sx * 14, 1, sz * 10, sx * 14, 3, sz * 10, Blocks.POLISHED_ANDESITE);
            p.put(sx * 14, 4, sz * 10, Blocks.SEA_LANTERN);
        }
    }

    private static void placeAnchors(Plan p, BlockPos center, Theme theme) {
        List<BlockPos> positions = anchors(center);
        for (int i = 0; i < positions.size(); i++) {
            BlockPos pos = positions.get(i);
            int x = pos.getX() - center.getX(), z = pos.getZ() - center.getZ();
            // The surrounding five-by-five space remains approachable from every side.
            p.clear(x - 2, 1, z - 2, x + 2, 6, z + 2);
            p.fill(x - 2, 0, z - 2, x + 2, 0, z + 2, theme.trim);
            p.fill(x - 1, 0, z - 1, x + 1, 0, z + 1, ELEMENTS[i]);
            p.put(x, 1, z, theme.pillar);
            p.put(x, 2, z, Blocks.REDSTONE_LAMP);
            p.put(x, -1, z, Blocks.SEA_LANTERN);
        }
    }

    private static void enclose(Plan p, Theme theme) {
        for (int x = -HALF_SIZE; x <= HALF_SIZE; x++) {
            for (int z = -HALF_SIZE; z <= HALF_SIZE; z++) {
                p.put(x, CEILING - FLOOR, z, Blocks.BARRIER);
                if (Math.abs(x) == HALF_SIZE || Math.abs(z) == HALF_SIZE) {
                    p.fill(x, 1, z, x, CEILING - FLOOR - 1, z, Blocks.BARRIER);
                } else if (Math.abs(x) == HALF_SIZE - 1 || Math.abs(z) == HALF_SIZE - 1) {
                    p.put(x, 0, z, theme.trim);
                    boolean alongZ = Math.abs(x) == HALF_SIZE - 1;
                    boolean alongX = Math.abs(z) == HALF_SIZE - 1;
                    BlockState rail = Blocks.IRON_BARS.defaultBlockState()
                            .setValue(BlockStateProperties.NORTH, alongZ && z > -HALF_SIZE + 1)
                            .setValue(BlockStateProperties.SOUTH, alongZ && z < HALF_SIZE - 1)
                            .setValue(BlockStateProperties.WEST, alongX && x > -HALF_SIZE + 1)
                            .setValue(BlockStateProperties.EAST, alongX && x < HALF_SIZE - 1);
                    p.put(x, 1, z, rail);
                    if ((x + z) % 7 == 0) {
                        p.put(x, 1, z, theme.pillar);
                        p.put(x, 2, z, Blocks.SEA_LANTERN);
                    }
                }
            }
        }
    }

    private static void rock(Plan p, int x, int z, int height, Block base, Block core) {
        p.fill(x - 1, 1, z - 1, x + 1, 2, z + 1, base);
        p.fill(x, 3, z, x, height, z, core);
        if (height >= 5) p.fill(x - 1, 3, z, x + 1, height - 2, z, core);
    }

    private static void prism(Plan p, int x, int z, int height, Block crystal, Block base) {
        p.fill(x - 1, 1, z - 1, x + 1, 1, z + 1, base);
        p.fill(x - 1, 2, z, x + 1, height - 1, z, crystal);
        p.fill(x, 2, z - 1, x, height - 1, z + 1, crystal);
        p.put(x, height, z, crystal);
        p.put(x, 2, z, Blocks.SEA_LANTERN);
    }

    private static void deadTree(Plan p, int x, int z, int bend) {
        p.fill(x, 1, z, x, 5, z, Blocks.DARK_OAK_LOG);
        p.fill(x, 5, z, x + bend * 2, 5, z, Blocks.STRIPPED_DARK_OAK_LOG);
        p.fill(x + bend * 2, 6, z, x + bend * 2, 8, z, Blocks.DARK_OAK_LOG);
        p.fill(x + bend * 2 - 2, 8, z - 2, x + bend * 2 + 2, 8, z + 2, Blocks.MOSS_BLOCK);
        p.put(x + bend * 2, 9, z, Blocks.MOSS_BLOCK);
        p.put(x, 4, z + 1, Blocks.SHROOMLIGHT);
    }

    private static void table(Plan p, int x, int z, int halfX, int halfZ, Block legs, Block top) {
        for (int dx : new int[]{-halfX, halfX}) for (int dz : new int[]{-halfZ, halfZ}) p.put(x + dx, 1, z + dz, legs);
        p.fill(x - halfX, 2, z - halfZ, x + halfX, 2, z + halfZ, top);
    }

    private static void mirror(Plan p, int x, int z) {
        p.fill(x - 2, 1, z, x + 2, 5, z, Blocks.QUARTZ_BRICKS);
        p.fill(x - 1, 2, z, x + 1, 4, z, Blocks.TINTED_GLASS);
        p.put(x, 6, z, Blocks.SEA_LANTERN);
    }

    private static void arch(Plan p, int x, int z, int halfWidth, int height, Block post, Block lintel, boolean rotated) {
        if (rotated) {
            p.fill(x, 1, z - halfWidth, x, height, z - halfWidth, post);
            p.fill(x, 1, z + halfWidth, x, height, z + halfWidth, post);
            p.fill(x, height, z - halfWidth - 1, x, height, z + halfWidth + 1, lintel);
        } else {
            p.fill(x - halfWidth, 1, z, x - halfWidth, height, z, post);
            p.fill(x + halfWidth, 1, z, x + halfWidth, height, z, post);
            p.fill(x - halfWidth - 1, height, z, x + halfWidth + 1, height, z, lintel);
        }
        p.put(x, height - 1, z, Blocks.SEA_LANTERN);
    }

    private enum Theme {
        VISION(Blocks.QUARTZ_BRICKS, Blocks.QUARTZ_PILLAR),
        ECHO(Blocks.WAXED_CUT_COPPER, Blocks.CHISELED_DEEPSLATE),
        MIASMA(Blocks.MOSSY_STONE_BRICKS, Blocks.WAXED_CUT_COPPER),
        TASTE(Blocks.WHITE_TERRACOTTA, Blocks.BRICKS),
        TOUCH(Blocks.CALCITE, Blocks.AMETHYST_BLOCK),
        DELUSION(Blocks.END_STONE_BRICKS, Blocks.PURPUR_PILLAR),
        TREASURE(Blocks.GOLD_BLOCK, Blocks.CHISELED_POLISHED_BLACKSTONE),
        FEAST(Blocks.RED_TERRACOTTA, Blocks.NETHER_BRICKS),
        PURSUIT(Blocks.SMOOTH_SANDSTONE, Blocks.CHISELED_SANDSTONE),
        HEART(Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRYING_OBSIDIAN),
        TEST(Blocks.POLISHED_ANDESITE, Blocks.CHISELED_STONE_BRICKS);

        final Block trim;
        final Block pillar;

        Theme(Block trim, Block pillar) {
            this.trim = trim;
            this.pillar = pillar;
        }

        static Theme forId(String id) {
            if (id == null) return TEST;
            return switch (id) {
                case "six_thieves_1" -> VISION;
                case "six_thieves_2" -> ECHO;
                case "six_thieves_3" -> MIASMA;
                case "six_thieves_4" -> TASTE;
                case "six_thieves_5" -> TOUCH;
                case "six_thieves_6" -> DELUSION;
                case "three_corps_1" -> TREASURE;
                case "three_corps_2" -> FEAST;
                case "three_corps_3" -> PURSUIT;
                case "heart_demon_1" -> HEART;
                default -> TEST;
            };
        }
    }

    /** Null means air. Compare the final blueprint, avoiding clear-and-rebuild churn. */
    private static final class Plan {
        private final BlockState[][][] states = new BlockState[WIDTH][CEILING - FLOOR + 2][WIDTH];

        void put(int x, int dy, int z, Block block) {
            put(x, dy, z, block.defaultBlockState());
        }

        void put(int x, int dy, int z, BlockState state) {
            if (Math.abs(x) <= HALF_SIZE && Math.abs(z) <= HALF_SIZE && dy >= -1 && dy <= CEILING - FLOOR) {
                states[x + HALF_SIZE][dy + 1][z + HALF_SIZE] = state.isAir() ? null : state;
            }
        }

        void fill(int x1, int y1, int z1, int x2, int y2, int z2, Block block) {
            BlockState state = block.defaultBlockState();
            for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
                for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                    for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) put(x, y, z, state);
                }
            }
        }

        void clear(int x1, int y1, int z1, int x2, int y2, int z2) {
            fill(x1, y1, z1, x2, y2, z2, Blocks.AIR);
        }

        void apply(ServerLevel level, BlockPos center) {
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            BlockState air = Blocks.AIR.defaultBlockState();
            for (int x = -HALF_SIZE; x <= HALF_SIZE; x++) {
                for (int z = -HALF_SIZE; z <= HALF_SIZE; z++) {
                    for (int dy = -1; dy <= CEILING - FLOOR; dy++) {
                        cursor.set(center.getX() + x, FLOOR + dy, center.getZ() + z);
                        BlockState next = states[x + HALF_SIZE][dy + 1][z + HALF_SIZE];
                        if (next == null) next = air;
                        if (!level.getBlockState(cursor).equals(next)) level.setBlock(cursor, next, 2);
                    }
                }
            }
        }
    }
}
