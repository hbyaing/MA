package cn.blockforge.generated.generatedmod.importedcontent;

import cn.blockforge.generated.generatedmod.WuXingBlocks;
import cn.blockforge.generated.generatedmod.WuXingItems;
import cn.blockforge.generated.generatedmod.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
    public static final RegistryObject<Block> LAOYANG_DOLL=WuXingBlocks.BLOCKS.register("laoyang_doll",()->new LaoyangDollBlock(true,
            BlockBehaviour.Properties.of().strength(1).sound(SoundType.WOOL).noOcclusion()));
    public static final RegistryObject<Item> LAOYANG_ITEM=WuXingItems.ITEMS.register("laoyang_doll",()->new BlockItem(LAOYANG_DOLL.get(),new Item.Properties()));
    public static final RegistryObject<Block> CHENGRAN_DOLL=WuXingBlocks.BLOCKS.register("chengran_doll",()->new LaoyangDollBlock(false,
            BlockBehaviour.Properties.of().strength(1).sound(SoundType.WOOL).noOcclusion()));
    public static final RegistryObject<Item> CHENGRAN_ITEM=WuXingItems.ITEMS.register("chengran_doll",()->new BlockItem(CHENGRAN_DOLL.get(),new Item.Properties().rarity(Rarity.EPIC)) {
        @Override public Component getName(ItemStack stack) {return Component.translatable("block.wuxingxiantu.chengran_doll").withStyle(ChatFormatting.LIGHT_PURPLE);}
    });
    public static final RegistryObject<Item> LAOYANG_PILL=WuXingItems.ITEMS.register("laoyang_secret_pill",()->new LaoyangPillItem(new Item.Properties()));
    public static void bootstrap() {}
    private ImportedContent() {}
}
