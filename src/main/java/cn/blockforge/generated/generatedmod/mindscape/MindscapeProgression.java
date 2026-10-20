package cn.blockforge.generated.generatedmod.mindscape;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.server.level.ServerPlayer;

/** Selects all ten formal challenges in order; explicit pending selections take precedence. */
public final class MindscapeProgression {
    private static final String PENDING = "pending_challenge";
    private MindscapeProgression() {}

    public static void markUnstableFor(ServerPlayer player, String challengeId) {
        MindscapeChallenges.Definition definition = MindscapeChallenges.get(challengeId);
        if (definition == null || !definition.triggerStage().equals("formal")) return;
        CultivationData data = GeneratedMod.data(player);
        if (data == null) return;
        data.wuxingxiantu$getMindscapeData().putString(PENDING, definition.id().toString());
        data.wuxingxiantu$setDaoXin(4);
        data.wuxingxiantu$setHeartDemonInvasionCause("PILL");
    }

    public static MindscapeChallenges.Definition pending(ServerPlayer player) {
        CultivationData data = GeneratedMod.data(player);
        if (data == null) return null;
        String id = data.wuxingxiantu$getMindscapeData().getString(PENDING);
        return id.isBlank() ? null : MindscapeChallenges.get(id);
    }

    public static MindscapeChallenges.Definition chooseNext(ServerPlayer player) {
        CultivationData data = GeneratedMod.data(player);
        if (data == null) return null;
        MindscapeChallenges.Definition current = pending(player);
        if (current != null) return current;
        var completed = data.wuxingxiantu$getMindscapeData().getList("completed_challenges", 8);
        if (data.wuxingxiantu$getMindscapeData().getBoolean("final_completed"))
            return null;
        int highest = -1;
        for (var tag : completed) {
            MindscapeChallenges.Definition definition = MindscapeChallenges.get(tag.getAsString());
            if (definition != null) highest = Math.max(highest, definition.progressIndex());
        }
        return MindscapeChallenges.formalByIndex(highest + 1, data.wuxingxiantu$getRealm());
    }

    public static void clearPending(ServerPlayer player) {
        CultivationData data = GeneratedMod.data(player);
        if (data != null) data.wuxingxiantu$getMindscapeData().remove(PENDING);
    }
    /** A consumed pill explicitly starts a trial, including a replay after final completion. */
    public static boolean requestFromPill(ServerPlayer player) {
        var data=GeneratedMod.data(player);
        if(data==null||!player.isAlive()||player.isSpectator()||data.wuxingxiantu$isDaoHeartUnstable()
                ||player.level().dimension()==MindscapeManager.DIMENSION
                ||data.wuxingxiantu$getMindscapeData().hasUUID("session"))return false;
        var challenge=chooseNext(player);
        if(challenge==null&&data.wuxingxiantu$getMindscapeData().getBoolean("final_completed"))
            challenge=MindscapeChallenges.finalChallenge(data.wuxingxiantu$getRealm());
        if(challenge==null||challenge.requiredRealm()>data.wuxingxiantu$getRealm())return false;
        MindscapeManager.markUnstableFor(player,challenge.id().toString());
        return data.wuxingxiantu$isDaoHeartUnstable();
    }
    public static boolean requestRepeat(ServerPlayer player) {
        var data=GeneratedMod.data(player);
        if(data==null||!player.isAlive()||player.isSpectator()||data.wuxingxiantu$isDaoHeartUnstable()
                ||data.wuxingxiantu$getMindscapeData().hasUUID("session")
                ||!data.wuxingxiantu$getMindscapeData().getBoolean("final_completed"))return false;
        var challenge=MindscapeChallenges.finalChallenge(data.wuxingxiantu$getRealm());
        if(challenge==null)return false;
        MindscapeManager.markUnstableFor(player,challenge.id().toString());
        data.wuxingxiantu$setHeartDemonInvasionCause("REPLAY");
        GeneratedMod.sync(player);
        return true;
    }
}
