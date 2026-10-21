package cn.blockforge.generated.generatedmod.knowledge;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Uses vanilla's existing written-book reader, with an authoritative snapshot before every open. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class MyriadCompendiumItem extends Item {
    public static final String MARKER="wuxingxiantu_myriad_compendium";
    public MyriadCompendiumItem(Properties properties) {super(properties);}
    public static boolean isCompendium(ItemStack stack) {
        return stack.is(Items.WRITTEN_BOOK)&&stack.hasTag()&&stack.getTag().getBoolean(MARKER);
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        if(!(player instanceof ServerPlayer server))return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),level.isClientSide);
        // Vanilla's open-book packet accepts exactly WRITTEN_BOOK; the craftable token becomes that book once.
        ItemStack book=new ItemStack(Items.WRITTEN_BOOK);
        book.getOrCreateTag().putBoolean(MARKER,true);
        player.setItemInHand(hand,book);
        open(server,book,hand);
        return InteractionResultHolder.consume(book);
    }
    public static void open(ServerPlayer player,ItemStack book,InteractionHand hand) {
        CompendiumSnapshot.refresh(book,player);
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if(player.containerMenu!=player.inventoryMenu)player.containerMenu.broadcastChanges();
        player.openItemGui(book,hand);
    }
    @SubscribeEvent public static void read(PlayerInteractEvent.RightClickItem event) {
        if(!isCompendium(event.getItemStack()))return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        // Client use waits for the server's open-book packet and current-reader slot update.
        if(event.getEntity() instanceof ServerPlayer player)open(player,event.getItemStack(),event.getHand());
    }
    @SubscribeEvent public static void toss(ItemTossEvent event) {
        ItemStack stack=event.getEntity().getItem();
        if(event.getPlayer().level().isClientSide||!isCompendium(stack))return;
        CompendiumSnapshot.clear(stack);
        event.getEntity().setItem(stack);
    }
    @SubscribeEvent public static void lectern(PlayerInteractEvent.RightClickBlock event) {
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        Level level=event.getLevel();
        var state=level.getBlockState(event.getPos());
        if(!(state.getBlock() instanceof LecternBlock))return;
        if(level.getBlockEntity(event.getPos()) instanceof LecternBlockEntity lectern&&isCompendium(lectern.getBook())) {
            // Old saves/commands can contain such a book. Return it rather than opening a shared lectern snapshot.
            ItemStack returned=lectern.getBook().copy();
            CompendiumSnapshot.clear(returned);
            lectern.setBook(ItemStack.EMPTY);
            lectern.setChanged();
            level.setBlock(event.getPos(),state.setValue(LecternBlock.HAS_BOOK,false),3);
            level.updateNeighbourForOutputSignal(event.getPos(),state.getBlock());
            player.getInventory().placeItemBackInInventory(returned);
        } else if(!isCompendium(event.getItemStack()))return;
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("knowledge.wuxingxiantu.handheld_only"),true);
        level.sendBlockUpdated(event.getPos(),state,level.getBlockState(event.getPos()),3);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
