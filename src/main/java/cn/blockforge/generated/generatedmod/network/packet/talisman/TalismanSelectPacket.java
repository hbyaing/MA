package cn.blockforge.generated.generatedmod.network.packet.talisman;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.network.service.TalismanService;

public record TalismanSelectPacket(BlockPos pos, int index) {
        public static void encode(TalismanSelectPacket packet, FriendlyByteBuf buf) {
            buf.writeBlockPos(packet.pos());
            buf.writeVarInt(packet.index());
        }
        public static TalismanSelectPacket decode(FriendlyByteBuf buf) {
            return new TalismanSelectPacket(buf.readBlockPos(), buf.readVarInt());
        }
        public static void handle(TalismanSelectPacket packet, Supplier<NetworkEvent.Context> supplier) {
            TalismanService.handleTalismanSelectPacket(packet, supplier);
        }
    }
