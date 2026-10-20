package cn.blockforge.generated.generatedmod.network.packet.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.network.service.AlchemyService;

public record RefiningPacket(BlockPos pos, ResourceLocation recipeId) {
        public static void encode(RefiningPacket packet, FriendlyByteBuf buf) {
            buf.writeBlockPos(packet.pos());
            buf.writeUtf(packet.recipeId().toString(), 256);
        }

        public static RefiningPacket decode(FriendlyByteBuf buf) {
            return new RefiningPacket(buf.readBlockPos(), new ResourceLocation(buf.readUtf(256)));
        }

        public static void handle(RefiningPacket packet, Supplier<NetworkEvent.Context> supplier) {
            AlchemyService.handleRefiningPacket(packet, supplier);
        }

        /** 把承华台的材料缺口拼成“玄铁锭×2、灵晶×1”这样的点名清单，并附上服务端实际看到的载物台数。 */


        /** 从承华台扣除素材，并让被扣走的那部分以供品实体飞入炉心异火。 */

    }
