package cn.blockforge.generated.generatedmod.formation;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 来源四剑与剑图独立注册，不占用旧功法、御剑或神器等级表。 */
public final class FormationBootstrap {
    public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,GeneratedMod.MOD_ID);
    public static final RegistryObject<Item> ZHUXIAN_SWORD=ITEMS.register("zhuxian_sword",ChaosSwordItem::new);
    public static final RegistryObject<Item> LUXIAN_SWORD=ITEMS.register("luxian_sword",ChaosSwordItem::new);
    public static final RegistryObject<Item> XIANXIAN_SWORD=ITEMS.register("xianxian_sword",ChaosSwordItem::new);
    public static final RegistryObject<Item> JUEXIAN_SWORD=ITEMS.register("juexian_sword",ChaosSwordItem::new);
    public static final RegistryObject<Item> ZHUXIAN_SWORD_CHART=ITEMS.register("zhuxian_sword_chart",SwordChartItem::new);
    public static java.util.List<RegistryObject<Item>> swords(){return java.util.List.of(ZHUXIAN_SWORD,LUXIAN_SWORD,XIANXIAN_SWORD,JUEXIAN_SWORD);}
    public static java.util.List<RegistryObject<Item>> items(){return java.util.List.of(ZHUXIAN_SWORD,LUXIAN_SWORD,XIANXIAN_SWORD,JUEXIAN_SWORD,ZHUXIAN_SWORD_CHART);}
    private FormationBootstrap(){}
}
