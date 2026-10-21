package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.ArtCatalog;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record SyncPacket(float currentMana, float maxMana, int realm, boolean ghostImmortal, int yinShenPurity,
                         long cultivation, long merit, int cultivationStage,
                         int rootMask, int[] rootQualities, boolean chaosRoot, int daoXin, int biguRemainTime, boolean meditating, int meridianStatus,
                         boolean woodDomainEnabled, boolean fireDomainEnabled, boolean goldDomainEnabled,
                          boolean waterDomainEnabled, boolean earthDomainEnabled, int physique, boolean physiqueLocked,
                         String heartMethod, String[] learnedHeartMethods, String[] arts, String[] passiveArts, String[] learnedArts, String[] knownSwordTechniques, int[] artMastery,
                         int thunderStrikes, boolean thunderUnlocked, boolean thunderActive,
                         boolean exoticUnlocked, boolean exoticActive, java.util.UUID heartForgeContract,boolean exoticFireHurtsNonHostile) {
    public Object[] stableState(){return new Object[]{realm,ghostImmortal,yinShenPurity,cultivationStage,rootMask,rootQualities,chaosRoot,daoXin,meditating,meridianStatus,woodDomainEnabled,fireDomainEnabled,goldDomainEnabled,waterDomainEnabled,earthDomainEnabled,physique,physiqueLocked,heartMethod,learnedHeartMethods,arts,passiveArts,learnedArts,knownSwordTechniques,artMastery,thunderStrikes,thunderUnlocked,thunderActive,exoticUnlocked,exoticActive,heartForgeContract,exoticFireHurtsNonHostile};}
    public SyncPacket(CultivationData data) {
        this(data.getCurrentMana(), data.getMaxMana(), data.wuxingxiantu$getRealm(), data.wuxingxiantu$isGhostImmortal(),
                data.wuxingxiantu$getYinShenPurity(), data.wuxingxiantu$getCultivation(), data.wuxingxiantu$getMerit(),
                data.wuxingxiantu$getCultivationStage(), data.wuxingxiantu$getRootMask(), rootQualities(data), data.wuxingxiantu$hasChaosRoot(), data.wuxingxiantu$getDaoXin(),
                data.wuxingxiantu$getBiguRemainTime(), data.wuxingxiantu$isMeditating(), data.wuxingxiantu$getMeridianStatus(),
                data.wuxingxiantu$isWoodDomainEnabled(), data.wuxingxiantu$isFireDomainEnabled(),
                data.wuxingxiantu$isGoldDomainEnabled(), data.wuxingxiantu$isWaterDomainEnabled(),
                data.wuxingxiantu$isEarthDomainEnabled(), data.wuxingxiantu$getPhysique().ordinal(),
                false,
                data.wuxingxiantu$getEquippedHeartMethod(), data.wuxingxiantu$getLearnedHeartMethods(), data.wuxingxiantu$getEquippedArts(), data.wuxingxiantu$getPassiveArts(), data.wuxingxiantu$getLearnedArts(), data.wuxingxiantu$getKnownSwordTechniques(), mastery(data),
                data.wuxingxiantu$getThunderStrikes(), data.wuxingxiantu$hasThunderRoot(), data.wuxingxiantu$isThunderRootActive(),
                data.wuxingxiantu$hasExoticRoot(), data.wuxingxiantu$isExoticRootActive(),data.wuxingxiantu$getHeartForgeContract(),
                data instanceof CultivationDataImpl impl&&impl.exoticFireHurtsNonHostile());
    }
    private static int[] mastery(CultivationData data) {
        int[] values = new int[ArtCatalog.ENTRIES.size()];
        for (int i = 0; i < values.length; i++) values[i] = data.wuxingxiantu$getArtMastery(ArtCatalog.ENTRIES.get(i).id());
        return values;
    }
    private static int[] rootQualities(CultivationData data) {
        int[] values = new int[5];
        for (int i = 0; i < values.length; i++) values[i] = data.wuxingxiantu$getRootQuality(i);
        return values;
    }
    public static void encode(SyncPacket p, FriendlyByteBuf b) {
        int start=b.writerIndex();
        b.writeFloat(p.currentMana); b.writeFloat(p.maxMana); b.writeInt(p.realm); b.writeBoolean(p.ghostImmortal); b.writeByte(p.yinShenPurity);
        b.writeLong(p.cultivation); b.writeLong(p.merit); b.writeByte(p.cultivationStage); b.writeInt(p.rootMask);
        for (int quality : p.rootQualities) b.writeByte(quality);
        b.writeBoolean(p.chaosRoot);
        b.writeByte(p.daoXin); b.writeInt(p.biguRemainTime); b.writeBoolean(p.meditating); b.writeByte(p.meridianStatus);
        b.writeBoolean(p.woodDomainEnabled); b.writeBoolean(p.fireDomainEnabled);
        b.writeBoolean(p.goldDomainEnabled); b.writeBoolean(p.waterDomainEnabled); b.writeBoolean(p.earthDomainEnabled);
        b.writeByte(p.physique); b.writeBoolean(p.physiqueLocked); b.writeUtf(p.heartMethod, 32);
        writeStrings(b, p.learnedHeartMethods);
         writeStrings(b, p.arts);
         writeStrings(b, p.passiveArts);
        writeStrings(b, p.learnedArts);
        writeStrings(b, p.knownSwordTechniques);
         b.writeVarInt(p.artMastery.length);
         for (int value : p.artMastery) b.writeByte(value);
         b.writeVarInt(p.thunderStrikes);
         b.writeBoolean(p.thunderUnlocked); b.writeBoolean(p.thunderActive);
         b.writeBoolean(p.exoticUnlocked); b.writeBoolean(p.exoticActive);
         b.writeNullable(p.heartForgeContract,FriendlyByteBuf::writeUUID);
         b.writeBoolean(p.exoticFireHurtsNonHostile);
        if(cn.blockforge.generated.generatedmod.network.SyncMetrics.ENABLED)
            cn.blockforge.generated.generatedmod.network.SyncMetrics.record(b.writerIndex()-start);
    }
    private static void writeStrings(FriendlyByteBuf b, String[] values) {
        b.writeVarInt(values.length);
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            b.writeBoolean(value != null);
            if (value != null) b.writeUtf(value, 64);
        }
    }
    public static SyncPacket decode(FriendlyByteBuf b) {
        float currentMana = b.readFloat();
        float maxMana = b.readFloat();
        int realm = b.readInt();
        boolean ghostImmortal = b.readBoolean();
        int yinShenPurity = b.readUnsignedByte();
        long cultivation = b.readLong();
        long merit = b.readLong();
        int cultivationStage = b.readUnsignedByte();
        int rootMask = b.readInt();
        int[] qualities = new int[5];
        for (int i = 0; i < qualities.length; i++) qualities[i] = b.readUnsignedByte();
        boolean chaosRoot = b.readBoolean();
        int daoXin = b.readUnsignedByte();
        int biguRemainTime = b.readInt();
        boolean meditating = b.readBoolean();
        int meridianStatus = b.readUnsignedByte();
        boolean woodDomainEnabled = b.readBoolean();
        boolean fireDomainEnabled = b.readBoolean();
        boolean goldDomainEnabled = b.readBoolean();
        boolean waterDomainEnabled = b.readBoolean();
        boolean earthDomainEnabled = b.readBoolean();
        int physique = b.readUnsignedByte();
        boolean physiqueLocked = b.readBoolean();
        String heartMethod = b.readUtf(32);
         String[] learnedHeartMethods = readStrings(b, HeartMethodCatalog.METHOD_IDS.size());
        String[] arts = readStrings(b, 8);
        String[] passiveArts = readStrings(b, 3);
        String[] learnedArts = readStrings(b, ArtCatalog.ENTRIES.size());
        String[] knownSwordTechniques = readStrings(b, ArtCatalog.ENTRIES.size());
         int masteryCount = b.readVarInt();
         if (masteryCount < 0 || masteryCount > ArtCatalog.ENTRIES.size())
             throw new io.netty.handler.codec.DecoderException("Invalid mastery array length: " + masteryCount);
         int[] artMastery = new int[ArtCatalog.ENTRIES.size()];
         for (int i = 0; i < masteryCount; i++) artMastery[i] = b.readUnsignedByte();
         int thunderStrikes = b.readVarInt();
         boolean thunderUnlocked = b.readBoolean();
         boolean thunderActive = b.readBoolean();
         boolean exoticUnlocked = b.readBoolean();
         boolean exoticActive = b.readBoolean();
        return new SyncPacket(currentMana, maxMana, realm, ghostImmortal, yinShenPurity, cultivation, merit, cultivationStage, rootMask, qualities, chaosRoot, daoXin,
                biguRemainTime, meditating, meridianStatus, woodDomainEnabled, fireDomainEnabled,
                goldDomainEnabled, waterDomainEnabled, earthDomainEnabled, physique, physiqueLocked,
                heartMethod, learnedHeartMethods, arts, passiveArts, learnedArts, knownSwordTechniques, artMastery,
                thunderStrikes, thunderUnlocked, thunderActive, exoticUnlocked, exoticActive,b.readNullable(FriendlyByteBuf::readUUID),b.readBoolean());
    }
    private static String[] readStrings(FriendlyByteBuf b, int max) {
        int count = b.readVarInt();
        if (count < 0 || count > max) throw new io.netty.handler.codec.DecoderException("Invalid string array length: " + count);
        String[] values = new String[max];
        for (int i = 0; i < count; i++) if (b.readBoolean()) values[i] = b.readUtf(64);
        return values;
    }
    public static void handle(SyncPacket p, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPacketSink.safe(ClientPacketSink.syncPacket, p));
        context.setPacketHandled(true);
    }
}
