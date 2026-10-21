package cn.blockforge.generated.generatedmod.network.packet.talisman;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.network.service.TalismanService;

public record TalismanRubPacket(BlockPos pos, int copies) {
        public static void encode(TalismanRubPacket packet, FriendlyByteBuf buf) {
            buf.writeBlockPos(packet.pos());
            buf.writeVarInt(packet.copies());
        }
        public static TalismanRubPacket decode(FriendlyByteBuf buf) {
            return new TalismanRubPacket(buf.readBlockPos(), buf.readVarInt());
        }
        public static void handle(TalismanRubPacket packet, Supplier<NetworkEvent.Context> supplier) {
            TalismanService.handleTalismanRubPacket(packet, supplier);
        }
    }
