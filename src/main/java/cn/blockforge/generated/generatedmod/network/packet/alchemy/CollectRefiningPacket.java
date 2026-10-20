package cn.blockforge.generated.generatedmod.network.packet.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.network.service.AlchemyService;

public record CollectRefiningPacket(BlockPos pos) {
        public static void encode(CollectRefiningPacket packet, FriendlyByteBuf buf) {
            buf.writeBlockPos(packet.pos());
        }
        public static CollectRefiningPacket decode(FriendlyByteBuf buf) {
            return new CollectRefiningPacket(buf.readBlockPos());
        }
        public static void handle(CollectRefiningPacket packet, Supplier<NetworkEvent.Context> supplier) {
            AlchemyService.handleCollectRefiningPacket(packet, supplier);
        }
    }
