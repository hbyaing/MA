package cn.blockforge.generated.generatedmod.yujian;

public record SwordRecallPacket() {
    public static void encode(SwordRecallPacket p,net.minecraft.network.FriendlyByteBuf b){}
    public static SwordRecallPacket decode(net.minecraft.network.FriendlyByteBuf b){return new SwordRecallPacket();}
    public static void handle(SwordRecallPacket p,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier){
        var context=supplier.get();context.enqueueWork(()->{var player=context.getSender();if(player!=null){YujianController.stop(player);player.serverLevel().getEntitiesOfClass(ControlledSwordEntity.class,player.getBoundingBox().inflate(100),s->player.getUUID().equals(s.ownerId)).forEach(net.minecraft.world.entity.Entity::discard);}});context.setPacketHandled(true);
    }
}
