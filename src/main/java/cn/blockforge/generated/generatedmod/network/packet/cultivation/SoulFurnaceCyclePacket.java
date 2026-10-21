package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record SoulFurnaceCyclePacket(BlockPos pos, int cycles) {
    public static void encode(SoulFurnaceCyclePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos());
        buf.writeVarInt(packet.cycles());
    }

    public static SoulFurnaceCyclePacket decode(FriendlyByteBuf buf) {
        return new SoulFurnaceCyclePacket(buf.readBlockPos(), buf.readVarInt());
    }

    public static void handle(SoulFurnaceCyclePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || packet.cycles() < 1
                    || packet.cycles() > SoulFurnaceBlockEntity.MAX_CYCLES
                    || !player.blockPosition().closerThan(packet.pos(), 8.0D)) return;
            if (!(player.level() instanceof ServerLevel level)
                    || !(level.getBlockEntity(packet.pos()) instanceof SoulFurnaceBlockEntity furnace)
                    || furnace.kind() != SoulFurnaceBlockEntity.Kind.SOUL_REFINING) return;
            furnace.configureCycles(packet.cycles());
            player.displayClientMessage(Component.translatable(
                    "message.wuxingxiantu.soul_furnace_cycle_set", packet.cycles()), true);
        });
        context.setPacketHandled(true);
    }
}
