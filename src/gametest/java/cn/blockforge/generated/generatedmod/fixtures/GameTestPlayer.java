package cn.blockforge.generated.generatedmod.fixtures;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameType;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;

/** Real server-player gameplay with an outbound packet sink, confined to the GameTest source set. */
public final class GameTestPlayer extends ServerPlayer implements AutoCloseable {
    private boolean closed;
    private boolean joined;
    private static final TicketType<UUID> TEST_TICKET=TicketType.create("gametest_player",Comparator.<UUID>naturalOrder());
    private record HeldChunk(ServerLevel level,ChunkPos pos) {}
    private final List<HeldChunk> heldChunks=new ArrayList<>();

    public void keepTicking(ServerLevel level,BlockPos position) {
        if(closed)throw new IllegalStateException("Test player is closed");
        var chunk=new ChunkPos(position);
        var held=new HeldChunk(level,chunk);
        if(heldChunks.contains(held))return;
        level.getChunkSource().addRegionTicket(TEST_TICKET,chunk,2,getUUID());
        level.getChunk(chunk.x,chunk.z);
        heldChunks.add(held);
    }

    public void cleanupAfter(GameTestHelper helper, Runnable cleanup) {
        try {
            Field field=GameTestHelper.class.getDeclaredField("testInfo");field.setAccessible(true);
            ((GameTestInfo)field.get(helper)).addListener(new GameTestListener() {
                @Override public void testStructureLoaded(GameTestInfo info) {}
                @Override public void testPassed(GameTestInfo info) {finish();}
                @Override public void testFailed(GameTestInfo info) {
                    com.mojang.logging.LogUtils.getLogger().error("GameTest failed: "+info.getTestName(),info.getError());
                    finish();
                }
                private void finish() {if(!closed)try {cleanup.run();} finally {close();}}
            });
        } catch(ReflectiveOperationException error) {throw new IllegalStateException("Forge 1.20.1 test fixture mapping changed",error);}
    }

    public GameTestPlayer(ServerLevel level) {
        super(level.getServer(),level,new GameProfile(UUID.randomUUID(),"GT"+UUID.randomUUID().toString().substring(0,12)));
        connection=new ServerGamePacketListenerImpl(server,new Connection(PacketFlow.SERVERBOUND),this) {
            @Override public void send(Packet<?> packet) {}
            @Override public void send(Packet<?> packet,PacketSendListener listener) {}
        };
        setGameMode(GameType.SURVIVAL);
        // GameTestServer does not load DedicatedServer's PvP setting from server.properties.
        // Combat fixtures explicitly require player-versus-player damage to exercise mitigation.
        server.setPvpAllowed(true);
        // A test begins after login protection, without waiting sixty ticks or changing damage methods.
        try {
            Field field=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
            field.setAccessible(true);field.setInt(this,0);
        } catch(ReflectiveOperationException error) {throw new IllegalStateException("Forge 1.20.1 player fixture mapping changed",error);}
    }

    @SuppressWarnings("unchecked")
    private Map<UUID,ServerPlayer> playerIndex() {
        try {
            Field field=PlayerList.class.getDeclaredField("playersByUUID");field.setAccessible(true);
            return (Map<UUID,ServerPlayer>)field.get(server.getPlayerList());
        } catch(ReflectiveOperationException error) {throw new IllegalStateException("Forge 1.20.1 player-list fixture mapping changed",error);}
    }

    public void join() {
        if(closed||joined)throw new IllegalStateException("Test player already joined or closed");
        // Register both lookup paths before any owner-bound entity joins. No login disk I/O or production bypass.
        try {
            playerIndex().put(getUUID(),this);
            mutablePlayers().add(this);
            serverLevel().addNewPlayer(this);
            if(server.getPlayerList().getPlayer(getUUID())!=this||!serverLevel().players().contains(this))
                throw new IllegalStateException("Test player registration failed");
            joined=true;
        } catch(RuntimeException|Error failure) {close();throw failure;}
    }

    @SuppressWarnings("unchecked")
    private List<ServerPlayer> mutablePlayers() {
        try {
            Field field=PlayerList.class.getDeclaredField("players");field.setAccessible(true);
            return (List<ServerPlayer>)field.get(server.getPlayerList());
        } catch(ReflectiveOperationException error) {throw new IllegalStateException("Forge 1.20.1 player-list fixture mapping changed",error);}
    }

    @Override public void tick() {
        super.tick();
        // Normally invoked by the connected client's packet listener tick.
        if(!closed)doTick();
    }

    @Override public void close() {
        if(closed)return;
        closed=true;
        mutablePlayers().remove(this);
        playerIndex().remove(getUUID(),this);
        discard();
        for(var held:heldChunks)held.level().getChunkSource().removeRegionTicket(TEST_TICKET,held.pos(),2,getUUID());
        heldChunks.clear();
    }
}
