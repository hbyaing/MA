package cn.blockforge.generated.generatedmod.storage;
import cn.blockforge.generated.generatedmod.storage.menu.BagMenu;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkHooks;
public final class StorageBagItem extends Item {
 public StorageBagItem(Properties p){super(p);}
 @Override public boolean canFitInsideContainerItems(){return false;}
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){ItemStack bag=player.getItemInHand(hand);if(!player.isAlive()||player.isSpectator()||cn.blockforge.generated.generatedmod.ParalysisControl.blocked(player))return InteractionResultHolder.fail(bag);if(player instanceof ServerPlayer sp){var tag=bag.getOrCreateTag();if(!tag.hasUUID("bag_id"))tag.putUUID("bag_id",java.util.UUID.randomUUID());var id=tag.getUUID("bag_id");for(ServerPlayer other:sp.server.getPlayerList().getPlayers())if(other.containerMenu instanceof BagMenu m&&m.stillValid(other)&&id.equals(m.bagId)){player.displayClientMessage(Component.literal("§e这个储物袋正在被使用"),true);return InteractionResultHolder.fail(bag);}int slot=hand==InteractionHand.MAIN_HAND?player.getInventory().selected:40;NetworkHooks.openScreen(sp,new SimpleMenuProvider((n,inv,p)->new BagMenu(n,inv,slot),Component.literal("储物袋")),buf->{buf.writeVarInt(slot);buf.writeUUID(id);});}return InteractionResultHolder.sidedSuccess(bag,level.isClientSide);}
 @Override public void appendHoverText(ItemStack s,Level l,java.util.List<Component> tip,TooltipFlag f){tip.add(Component.literal("§b右键打开 · 自动扩展存储"));tip.add(Component.literal("§7搜索 / 分类 / 草药药龄 / 随身合成"));tip.add(Component.literal(s.hasTag()&&s.getTag().getBoolean("bag_magnet")?"§a磁铁：开启 · 9×9":"§7磁铁：关闭"));}
}
