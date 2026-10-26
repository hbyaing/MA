package cn.blockforge.generated.generatedmod.formation.carving;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.entity.*;import net.minecraft.world.item.*;import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.*;import net.minecraftforge.common.extensions.IForgeMenuType;import net.minecraftforge.eventbus.api.IEventBus;
public final class CarvingRegistry {
 public static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(ForgeRegistries.BLOCKS,GeneratedMod.MOD_ID);
 public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,GeneratedMod.MOD_ID);
 public static final DeferredRegister<BlockEntityType<?>> TILES=DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,GeneratedMod.MOD_ID);
 public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(ForgeRegistries.MENU_TYPES,GeneratedMod.MOD_ID);
 public static final RegistryObject<Block> CORE=BLOCKS.register("formation_carving_core",()->new CarvingBlock(false));
 public static final RegistryObject<Block> EYE=BLOCKS.register("carved_formation_eye",()->new CarvingBlock(true));
 public static final RegistryObject<Item> CORE_ITEM=ITEMS.register("formation_carving_core",()->new BlockItem(CORE.get(),new Item.Properties().stacksTo(1)){
  @Override public void appendHoverText(ItemStack stack,net.minecraft.world.level.Level level,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){super.appendHoverText(stack,level,lines,flag);lines.add(net.minecraft.network.chat.Component.literal("放置后右键选择阵法，刻画后核心转为阵眼"));lines.add(net.minecraft.network.chat.Component.literal("手持灵石右键阵眼放入；潜行右键打开供能界面"));lines.add(net.minecraft.network.chat.Component.literal("费用与允许档位需在服务器配置中设置"));}
 });
 public static final RegistryObject<BlockEntityType<CarvingEye>> TILE=TILES.register("formation_carving",()->BlockEntityType.Builder.of(CarvingEye::new,CORE.get(),EYE.get()).build(null));
 public static final RegistryObject<MenuType<CarvingMenu>> MENU=MENUS.register("formation_carving",()->IForgeMenuType.create(CarvingMenu::fromNetwork));
 public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);TILES.register(bus);MENUS.register(bus);}
 private CarvingRegistry(){}
}
