package cn.blockforge.generated.generatedmod.network.packet.talisman;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.network.service.TalismanService;

public record TalismanSubmitPacket(BlockPos pos, String talismanId, byte[] bits) {
        public static void encode(TalismanSubmitPacket packet, FriendlyByteBuf buf) {
            buf.writeBlockPos(packet.pos());
            buf.writeUtf(packet.talismanId(), 64);
            buf.writeByteArray(packet.bits());
        }
        public static TalismanSubmitPacket decode(FriendlyByteBuf buf) {
            return new TalismanSubmitPacket(buf.readBlockPos(), buf.readUtf(64),
                    buf.readByteArray(CANVAS_BIT_BYTES));
        }
        public static void handle(TalismanSubmitPacket packet, Supplier<NetworkEvent.Context> supplier) {
            TalismanService.handleTalismanSubmitPacket(packet, supplier);
        }
    }
