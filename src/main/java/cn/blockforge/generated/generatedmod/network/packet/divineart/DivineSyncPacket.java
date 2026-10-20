package cn.blockforge.generated.generatedmod.network.packet.divineart;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record DivineSyncPacket(String[] learned, String[] equipped, String active, int step,
                               String[] possessedForms, String activePossessedForm) {
    public static void encode(DivineSyncPacket packet, FriendlyByteBuf buf) {
        String[] learned = packet.learned() == null ? new String[0] : packet.learned();
        String[] equipped = packet.equipped() == null ? new String[0] : packet.equipped();
        String[] forms = packet.possessedForms() == null ? new String[0] : packet.possessedForms();
        int learnedCount = Math.min(8, learned.length);
        int equippedCount = Math.min(3, equipped.length);
        int formCount = Math.min(256, forms.length);
        buf.writeVarInt(learnedCount);
        for (int i = 0; i < learnedCount; i++) buf.writeUtf(learned[i] == null ? "" : learned[i], 64);
        buf.writeByte(equippedCount);
        for (int i = 0; i < equippedCount; i++) {
            String value = equipped[i];
            boolean present = value != null && !value.isBlank();
            buf.writeBoolean(present);
            if (present) buf.writeUtf(value, 64);
        }
        // 该顺序是协议的一部分：不要把 active/step 放到形态列表之后。
        buf.writeUtf(packet.active() == null ? "" : packet.active(), 64);
        buf.writeByte(Math.max(0, Math.min(7, packet.step())));
        buf.writeVarInt(formCount);
        for (int i = 0; i < formCount; i++) buf.writeUtf(forms[i] == null ? "" : forms[i], 128);
        buf.writeUtf(packet.activePossessedForm() == null ? "" : packet.activePossessedForm(), 128);
    }
    public static DivineSyncPacket decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        if (count < 0 || count > 8) throw new IllegalArgumentException("Invalid divine learned count: " + count);
        String[] learned = new String[count];
        for (int i = 0; i < count; i++) learned[i] = buf.readUtf(64);
        int equippedCount = buf.readUnsignedByte();
        if (equippedCount > 3) throw new IllegalArgumentException("Invalid divine equipped count: " + equippedCount);
        String[] equipped = new String[3];
        for (int i = 0; i < equippedCount; i++) if (buf.readBoolean()) equipped[i] = buf.readUtf(64);
        // 必须先读 active 和 step；旧版把 active 的 UTF-8 长度当成了形态数量，导致读包越界并断开连接。
        String active = buf.readUtf(64);
        int step = buf.readUnsignedByte();
        if (step > 7) throw new IllegalArgumentException("Invalid divine step: " + step);
        int formCount = buf.readVarInt();
        if (formCount < 0 || formCount > 256) throw new IllegalArgumentException("Invalid possessed form count: " + formCount);
        String[] forms = new String[formCount];
        for (int i = 0; i < formCount; i++) forms[i] = buf.readUtf(128);
        return new DivineSyncPacket(learned, equipped, active, step, forms, buf.readUtf(128));
    }
    public static void handle(DivineSyncPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPacketSink.safe(ClientPacketSink.divineSyncPacket, packet));
        context.setPacketHandled(true);
    }
}
