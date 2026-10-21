package cn.blockforge.generated.generatedmod.arts;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.items.ArtManualItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 两门来源持续剑诀的独立注册，沿用原生秘籍学习和普通功法槽。 */
public final class ImportedSwordArts {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,GeneratedMod.MOD_ID);
    public static final RegistryObject<Item> WANJIAN_GUIZONG = ITEMS.register(SustainedSwordArts.WANJIAN,
            () -> new ArtManualItem(SustainedSwordArts.WANJIAN,4,new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> JIUXIAO_SWORD_RAIN = ITEMS.register(SustainedSwordArts.JIUXIAO,
            () -> new ArtManualItem(SustainedSwordArts.JIUXIAO,4,new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    private ImportedSwordArts() {}
}
