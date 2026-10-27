package cn.blockforge.generated.generatedmod.importedcontent;

import cn.blockforge.generated.generatedmod.WuXingBlocks;
import cn.blockforge.generated.generatedmod.WuXingItems;
import cn.blockforge.generated.generatedmod.registry.ModBlockEntities;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;

/** Source side content, registered through the existing target registries. */
public final class ImportedContent {
    public static final RegistryObject<Block> SPIRIT_BATH=WuXingBlocks.BLOCKS.register("spirit_bath",()->new SpiritBathBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.5F,6).sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<BlockEntityType<SpiritBathBlockEntity>> BATH_ENTITY=ModBlockEntities.BLOCK_ENTITIES.register("spirit_bath",
            ()->BlockEntityType.Builder.of(SpiritBathBlockEntity::new,SPIRIT_BATH.get()).build(null));
    public static final RegistryObject<Item> BATH_ITEM=WuXingItems.ITEMS.register("spirit_bath",()->new BlockItem(SPIRIT_BATH.get(),new Item.Properties()));
    public static void bootstrap() {}
    private ImportedContent() {}
}
