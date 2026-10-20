package cn.blockforge.generated.generatedmod.network.packet.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.network.service.AlchemyService;

public record AlchemyPacket(BlockPos pos, ResourceLocation recipeId) {
        public static void encode(AlchemyPacket packet, FriendlyByteBuf buf) {
            buf.writeBlockPos(packet.pos);
            buf.writeUtf(packet.recipeId.toString(), 256);
        }
        public static AlchemyPacket decode(FriendlyByteBuf buf) {
            return new AlchemyPacket(buf.readBlockPos(), new ResourceLocation(buf.readUtf(256)));
        }
        public static void handle(AlchemyPacket packet, Supplier<NetworkEvent.Context> supplier) {
            AlchemyService.handleAlchemyPacket(packet, supplier);
        }
    }
