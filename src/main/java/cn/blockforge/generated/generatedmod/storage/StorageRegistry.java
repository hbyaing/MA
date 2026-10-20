package cn.blockforge.generated.generatedmod.storage;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.storage.menu.BagMenu;
import cn.blockforge.generated.generatedmod.storage.network.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.*;
import java.util.Optional;

public final class StorageRegistry {
    public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,GeneratedMod.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(ForgeRegistries.MENU_TYPES,GeneratedMod.MOD_ID);
    public static final RegistryObject<Item> BAG_ITEM=ITEMS.register("storage_bag",()->new StorageBagItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<MenuType<BagMenu>> STORAGE_BAG=MENUS.register("storage_bag",()->IForgeMenuType.create(BagMenu::create));
    public static void register(IEventBus bus){ITEMS.register(bus);MENUS.register(bus);}
    public static int registerPackets(SimpleChannel channel,int id){
        channel.registerMessage(id++,BagStatePacket.class,BagStatePacket::encode,BagStatePacket::new,BagStatePacket::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(id++,BagScrollPacket.class,BagScrollPacket::encode,BagScrollPacket::new,BagScrollPacket::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(id++,BagFilterPacket.class,BagFilterPacket::encode,BagFilterPacket::new,BagFilterPacket::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));
        return id;
    }
    private StorageRegistry(){}
}
