package cn.blockforge.generated.generatedmod;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class MeditationVariantGameTests {
    @GameTest(template="empty")
    public static void importedCushionUsesOneSeatAndTargetTier(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(1, 1, 1));
        h.getLevel().setBlockAndUpdate(pos, MeditationVariants.WOVEN_CUSHION.get().defaultBlockState());
        var first = FakePlayerFactory.get(h.getLevel(), new GameProfile(java.util.UUID.randomUUID(), "cushion_first"));
        var second = FakePlayerFactory.get(h.getLevel(), new GameProfile(java.util.UUID.randomUUID(), "cushion_second"));
        first.setPos(Vec3.atCenterOf(pos));
        second.setPos(Vec3.atCenterOf(pos));
        first.setDeltaMovement(Vec3.ZERO);
        second.setDeltaMovement(Vec3.ZERO);
        var firstData = GeneratedMod.data(first);
        var secondData = GeneratedMod.data(second);
        CultivationBlock.startMeditation(first, firstData, pos, 0);
        h.assertTrue(first.getVehicle() instanceof MeditationSeatEntity && firstData.wuxingxiantu$isMeditating()
                && firstData.wuxingxiantu$getMeditationTier() == 0, "Imported cushion created a second bonus tier");
        var seat = first.getVehicle();
        CultivationBlock.startMeditation(second, secondData, pos, 0);
        h.assertTrue(!second.isPassenger() && !secondData.wuxingxiantu$isMeditating(), "Occupied cushion accepted another seat");
        CultivationBlock.stopMeditation(first, firstData, false);
        h.assertTrue(seat.isRemoved() && !first.isPassenger() && !firstData.wuxingxiantu$isMeditating(), "Dismount left cultivation active");
        CultivationBlock.startMeditation(second, secondData, pos, 0);
        h.assertTrue(second.isPassenger(), "Freed cushion remained occupied");
        CultivationBlock.stopMeditation(second, secondData, false);
        h.succeed();
    }

    @GameTest(template="empty")
    public static void wovenOutlineMatchesSourceWithoutChangingOldCushion(GameTestHelper h) {
        var woven = MeditationVariants.WOVEN_CUSHION.get().defaultBlockState();
        var old = WuXingBlocks.MEDITATION_CUSHION.get().defaultBlockState();
        var wide = woven.getShape(h.getLevel(), BlockPos.ZERO, CollisionContext.empty()).bounds();
        var narrow = old.getShape(h.getLevel(), BlockPos.ZERO, CollisionContext.empty()).bounds();
        h.assertTrue(wide.minX == 0 && wide.maxX == 1 && wide.maxY == 3.0D / 16.0D,
                "Woven cushion outline differs from imported model");
        h.assertTrue(narrow.minX == 2.0D / 16.0D && narrow.maxX == 14.0D / 16.0D,
                "Existing cushion geometry changed");
        h.succeed();
    }
}
