package cn.blockforge.generated.generatedmod.robe;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class RobeCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent event){
        event.getDispatcher().register(Commands.literal("wuxing").then(Commands.literal("robe").requires(s->s.hasPermission(2))
                .then(Commands.literal("open").executes(c->{var p=c.getSource().getPlayerOrException();
                    if(!p.isAlive()||p.isSpectator()||!p.containerMenu.getCarried().isEmpty())return 0;
                    net.minecraftforge.network.NetworkHooks.openScreen(p,new net.minecraft.world.SimpleMenuProvider(
                            (id,inventory,owner)->new RobeMenu(id,inventory,new RobeContainer(p)),Component.translatable("menu.wuxingxiantu.robe")));return 1;}))
                .then(Commands.literal("inspect").executes(c->{var p=c.getSource().getPlayerOrException();
                    c.getSource().sendSuccess(()->Component.literal("ROBE: "+RobeSlots.getRobe(p).save(new net.minecraft.nbt.CompoundTag())),false);return 1;}))
                .then(Commands.literal("clear").executes(c->{var p=c.getSource().getPlayerOrException();p.closeContainer();
                    var previous=RobeSlots.getRobe(p);RobeSlots.setRobe(p,ItemStack.EMPTY);if(!previous.isEmpty())p.getInventory().placeItemBackInInventory(previous);return 1;}))));
    }
    private RobeCommands(){}
}
