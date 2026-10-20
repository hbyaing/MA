package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record BannerStatePacket(int containerId,
                                String[] types, int[] counts, int[] active, int[] dead, int[] modes, String[] names,
                                String[] myriadTypes, int[] myriadCounts, int[] myriadActive, int[] myriadDead,
                                int[] myriadModes, String[] myriadNames,
                                boolean transitReady, int soulPower, int combatMode) {
    public static void encode(BannerStatePacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.containerId());
        buf.writeBoolean(packet.transitReady());
        buf.writeVarInt(packet.soulPower());
        buf.writeVarInt(packet.combatMode());
        writeSouls(buf, packet.types(), packet.counts(), packet.active(), packet.dead(), packet.modes(), packet.names());
        writeSouls(buf, packet.myriadTypes(), packet.myriadCounts(), packet.myriadActive(), packet.myriadDead(),
                packet.myriadModes(), packet.myriadNames());
    }

    private static void writeSouls(FriendlyByteBuf buf, String[] types, int[] counts, int[] active,
                                   int[] dead, int[] modes, String[] names) {
        int count = Math.min(128, types.length);
        buf.writeVarInt(count);
        for (int i = 0; i < count; i++) {
            buf.writeUtf(types[i], 128);
            buf.writeVarInt(i < counts.length ? counts[i] : 0);
            buf.writeVarInt(i < active.length ? active[i] : 0);
            buf.writeVarInt(i < dead.length ? dead[i] : 0);
            buf.writeVarInt(i < modes.length ? modes[i] : HumanEmperorBannerItem.MODE_DEFENSE);
            buf.writeUtf(i < names.length && names[i] != null ? names[i] : "", 256);
        }
    }

    public static BannerStatePacket decode(FriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        boolean transitReady = buf.readBoolean();
        int soulPower = buf.readVarInt();
        int combatMode = buf.readVarInt();
        SoulArrays banner = readSouls(buf);
        SoulArrays myriad = readSouls(buf);
        return new BannerStatePacket(containerId,
                banner.types(), banner.counts(), banner.active(), banner.dead(), banner.modes(), banner.names(),
                myriad.types(), myriad.counts(), myriad.active(), myriad.dead(), myriad.modes(), myriad.names(),
                transitReady, soulPower, combatMode);
    }

    private static SoulArrays readSouls(FriendlyByteBuf buf) {
        int count = Math.min(128, Math.max(0, buf.readVarInt()));
        String[] types = new String[count];
        int[] counts = new int[count];
        int[] active = new int[count];
        int[] dead = new int[count];
        int[] modes = new int[count];
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            types[i] = buf.readUtf(128);
            counts[i] = buf.readVarInt();
            active[i] = buf.readVarInt();
            dead[i] = buf.readVarInt();
            modes[i] = buf.readVarInt();
            names[i] = buf.readUtf(256);
        }
        return new SoulArrays(types, counts, active, dead, modes, names);
    }

    private record SoulArrays(String[] types, int[] counts, int[] active, int[] dead, int[] modes, String[] names) {}
    public static void handle(BannerStatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPacketSink.safe(ClientPacketSink.bannerStatePacket, packet));
        context.setPacketHandled(true);
    }
}
