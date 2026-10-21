package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record CrystallizePacket(boolean stack) {
    public CrystallizePacket(){this(false);}
    public static void encode(CrystallizePacket packet, FriendlyByteBuf buf) {buf.writeBoolean(packet.stack());}
    public static CrystallizePacket decode(FriendlyByteBuf buf) { return new CrystallizePacket(buf.readBoolean()); }
    public static void handle(CrystallizePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            CultivationData data = GeneratedMod.data(player);
            if (data == null) return;
            ItemStack crystals=new ItemStack(WuXingItems.AURA_CRYSTAL.get());
            int count=packet.stack()?crystals.getMaxStackSize():1;
            int cost = Math.multiplyExact(CultivationLogic.CRYSTALLIZE_AURA_COST,count);
            if (data.wuxingxiantu$getAura() < cost) {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.crystallize_missing", cost), true);
                return;
            }
            data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - cost);
            crystals.setCount(count);
            GeneratedMod.giveOrDrop(player, crystals);
            player.displayClientMessage(count==1?Component.translatable("message.wuxingxiantu.crystallize_done"):
                    Component.literal("已凝成 "+count+" 枚灵力结晶"), true);
            GeneratedMod.sync(player);
        });
        context.setPacketHandled(true);
    }
}
