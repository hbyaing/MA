package cn.blockforge.generated.generatedmod;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegistryObject;

/** Imported woven appearance; all seating and cultivation still use the target's tier-zero cushion. */
public final class MeditationVariants {
    public static final RegistryObject<Block> WOVEN_CUSHION = WuXingBlocks.BLOCKS.register(
            "woven_meditation_cushion", () -> new CultivationBlock(CultivationBlock.Kind.CUSHION,
                    BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.WOOL).noOcclusion(), true));
    public static final RegistryObject<Item> WOVEN_CUSHION_ITEM = WuXingItems.ITEMS.register(
            "woven_meditation_cushion", () -> new BlockItem(WOVEN_CUSHION.get(), new Item.Properties()));

    private MeditationVariants() {}
    public static void bootstrap() {}
}
