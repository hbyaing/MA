package cn.blockforge.generated.generatedmod.registry;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.AlchemyFurnaceMenu;
import cn.blockforge.generated.generatedmod.SoulFurnaceMenu;
import cn.blockforge.generated.generatedmod.SoulHealingRoomMenu;
import cn.blockforge.generated.generatedmod.HumanEmperorBannerMenu;
import cn.blockforge.generated.generatedmod.TalismanPlatformMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 五行仙途容器菜单注册。 */
public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, GeneratedMod.MOD_ID);
    public static final RegistryObject<MenuType<cn.blockforge.generated.generatedmod.crafting.EquipmentWorkstationMenu>> INSCRIPTION_TABLE=MENUS.register("ming_ke_tai",()->IForgeMenuType.create(cn.blockforge.generated.generatedmod.crafting.EquipmentWorkstationMenu::fromNetwork));
    public static final RegistryObject<MenuType<cn.blockforge.generated.generatedmod.crafting.EquipmentWorkstationMenu>> REFORGE_TABLE=MENUS.register("jing_zhu_tai",()->IForgeMenuType.create(cn.blockforge.generated.generatedmod.crafting.EquipmentWorkstationMenu::fromNetwork));
    public static final RegistryObject<MenuType<cn.blockforge.generated.generatedmod.alchemy.research.ResearchTableMenu>> ALCHEMY_RESEARCH_TABLE=MENUS.register("alchemy_research_table",()->IForgeMenuType.create(cn.blockforge.generated.generatedmod.alchemy.research.ResearchTableMenu::fromNetwork));
    public static final RegistryObject<MenuType<cn.blockforge.generated.generatedmod.HerbProcessingTableMenu>> HERB_PROCESSING_TABLE=MENUS.register(
            "herb_processing_table",()->IForgeMenuType.create(cn.blockforge.generated.generatedmod.HerbProcessingTableMenu::fromNetwork));
    public static final RegistryObject<MenuType<net.minecraft.world.inventory.AnvilMenu>> XUANLING_ANVIL = MENUS.register(
            "xuanling_anvil", () -> IForgeMenuType.create(cn.blockforge.generated.generatedmod.XuanlingAnvilMenu::fromNetwork));
    public static final RegistryObject<MenuType<cn.blockforge.generated.generatedmod.robe.RobeMenu>> ROBE=MENUS.register(
            "robe",()->IForgeMenuType.create(cn.blockforge.generated.generatedmod.robe.RobeMenu::fromNetwork));

    public static final RegistryObject<MenuType<SoulFurnaceMenu>> SOUL_FURNACE = MENUS.register(
            "soul_furnace", () -> IForgeMenuType.create(SoulFurnaceMenu::fromNetwork));
    public static final RegistryObject<MenuType<SoulHealingRoomMenu>> SOUL_HEALING_ROOM = MENUS.register(
            "soul_healing_room", () -> IForgeMenuType.create(SoulHealingRoomMenu::fromNetwork));
    public static final RegistryObject<MenuType<HumanEmperorBannerMenu>> HUMAN_EMPEROR_BANNER = MENUS.register(
            "human_emperor_banner", () -> IForgeMenuType.create(HumanEmperorBannerMenu::fromNetwork));
    public static final RegistryObject<MenuType<TalismanPlatformMenu>> TALISMAN_PLATFORM = MENUS.register(
            "talisman_platform", () -> IForgeMenuType.create(TalismanPlatformMenu::fromNetwork));
    public static final RegistryObject<MenuType<AlchemyFurnaceMenu>> ALCHEMY_FURNACE = MENUS.register(
            "alchemy_furnace", () -> IForgeMenuType.create(AlchemyFurnaceMenu::fromNetwork));

    private ModMenus() {}
}
