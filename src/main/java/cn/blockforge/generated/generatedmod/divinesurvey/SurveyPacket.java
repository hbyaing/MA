package cn.blockforge.generated.generatedmod.divinesurvey;
import net.minecraft.network.FriendlyByteBuf;import net.minecraftforge.network.NetworkEvent;import java.util.*;import java.util.function.Supplier;
public record SurveyPacket(String dimension,int mode,int completed,List<Target> targets) {
 public record Target(double x,double y,double z,double ex,double ey,double ez,String name,int color,int kind) {}
 public SurveyPacket{if(dimension.length()>128||mode<0||mode>2||completed<0||completed>64||targets.size()>512)throw new IllegalArgumentException("survey packet bounds");targets=List.copyOf(targets);}
 public SurveyPacket(FriendlyByteBuf b){this(b.readUtf(128),b.readVarInt(),b.readVarInt(),read(b));}
 private static List<Target> read(FriendlyByteBuf b){int n=b.readVarInt();if(n<0||n>512)throw new IllegalArgumentException("sense batch");List<Target> ts=new ArrayList<>();for(int i=0;i<n;i++)ts.add(new Target(b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readUtf(256),b.readInt(),b.readVarInt()));return ts;}
 public void encode(FriendlyByteBuf b){b.writeUtf(dimension);b.writeVarInt(mode);b.writeVarInt(completed);b.writeVarInt(targets.size());for(var t:targets){b.writeDouble(t.x);b.writeDouble(t.y);b.writeDouble(t.z);b.writeDouble(t.ex);b.writeDouble(t.ey);b.writeDouble(t.ez);b.writeUtf(t.name);b.writeInt(t.color);b.writeVarInt(t.kind);}}
 public static void handle(SurveyPacket p,Supplier<NetworkEvent.Context> c){c.get().enqueueWork(()->net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->cn.blockforge.generated.generatedmod.divinesurvey.client.SurveyClient.accept(p)));c.get().setPacketHandled(true);}
}
