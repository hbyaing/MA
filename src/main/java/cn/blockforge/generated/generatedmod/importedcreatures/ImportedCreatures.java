package cn.blockforge.generated.generatedmod.importedcreatures;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.WuXingItems;
import cn.blockforge.generated.generatedmod.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.RegistryObject;

/** Source beasts join the existing target registries and lifecycle. */
public final class ImportedCreatures {
    public static final RegistryObject<EntityType<BlackBoarEntity>> BLACK_BOAR=ModEntities.ENTITY_TYPES.register("black_boar",
            ()->EntityType.Builder.<BlackBoarEntity>of(BlackBoarEntity::new,MobCategory.MONSTER).sized(.9F,.9F).clientTrackingRange(8).build(GeneratedMod.MOD_ID+":black_boar"));
    public static final RegistryObject<EntityType<BlackBoarEntity>> BLACK_BOAR_KING=ModEntities.ENTITY_TYPES.register("black_boar_king",
            ()->EntityType.Builder.<BlackBoarEntity>of(BlackBoarEntity::new,MobCategory.MONSTER).sized(1.3F,1.3F).clientTrackingRange(10).build(GeneratedMod.MOD_ID+":black_boar_king"));
    public static final RegistryObject<Item> BLOOD=WuXingItems.ITEMS.register("beast_blood",()->new BeastMaterialItem(true,new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> HIDE=WuXingItems.ITEMS.register("beast_hide",()->new BeastMaterialItem(false,new Item.Properties()));
    public static final RegistryObject<Item> BOAR_EGG=WuXingItems.ITEMS.register("black_boar_spawn_egg",()->new ForgeSpawnEggItem(BLACK_BOAR,0x17191A,0xB8AD8B,new Item.Properties()));
    public static final RegistryObject<Item> KING_EGG=WuXingItems.ITEMS.register("black_boar_king_spawn_egg",()->new ForgeSpawnEggItem(BLACK_BOAR_KING,0x101213,0xA78949,new Item.Properties()));
    public static void bootstrap() {}
    private ImportedCreatures() {}
}
