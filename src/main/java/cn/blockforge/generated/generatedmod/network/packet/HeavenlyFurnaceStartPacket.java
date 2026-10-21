package cn.blockforge.generated.generatedmod.network.packet;

import cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/** Three explicit optional herb indices, ordered catalyst/effect/output; never supplied for refining. */
public record HeavenlyFurnaceStartPacket(UUID token, ResourceLocation recipe, int[] auxSlots) {
    public HeavenlyFurnaceStartPacket {
        Objects.requireNonNull(token); Objects.requireNonNull(recipe);
        if (auxSlots == null || auxSlots.length != 3) throw new IllegalArgumentException("Three auxiliary slots required");
        auxSlots = auxSlots.clone();
        for (int i = 0; i < auxSlots.length; i++) {
            if (auxSlots[i] < -1 || auxSlots[i] >= 36) throw new IllegalArgumentException("Auxiliary inventory slot out of range");
            for (int j = 0; j < i; j++) if (auxSlots[i] >= 0 && auxSlots[i] == auxSlots[j])
                throw new IllegalArgumentException("An auxiliary inventory slot cannot be reused");
        }
    }
    public HeavenlyFurnaceStartPacket(UUID token, ResourceLocation recipe) { this(token, recipe, new int[]{-1, -1, -1}); }
    @Override public int[] auxSlots() { return auxSlots.clone(); }
    @Override public boolean equals(Object other) {
        return other instanceof HeavenlyFurnaceStartPacket packet && token.equals(packet.token)
                && recipe.equals(packet.recipe) && Arrays.equals(auxSlots, packet.auxSlots);
    }
    @Override public int hashCode() { return 31 * Objects.hash(token, recipe) + Arrays.hashCode(auxSlots); }
    public static void encode(HeavenlyFurnaceStartPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.token); buffer.writeResourceLocation(packet.recipe);
        for (int slot : packet.auxSlots) buffer.writeVarInt(slot);
    }
    public static HeavenlyFurnaceStartPacket decode(FriendlyByteBuf buffer) {
        UUID token = buffer.readUUID(); ResourceLocation recipe = buffer.readResourceLocation();
        return new HeavenlyFurnaceStartPacket(token, recipe, new int[]{buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()});
    }
    public static void handle(HeavenlyFurnaceStartPacket packet, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) HeavenlyFurnace.start(context.getSender(), packet.token, packet.recipe, packet.auxSlots());
        });
        context.setPacketHandled(true);
    }
}
