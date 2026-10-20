package cn.blockforge.generated.generatedmod.fire;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class FireDiscovery {
    private static final String KEY = "wuxingxiantu_discovered_fires";
    public static void discover(ServerPlayer player, int tier) {
        if (FireCatalog.tier(tier) == null) return;
        var root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        var saved = root.getCompound(Player.PERSISTED_NBT_TAG);
        saved.putInt(KEY, saved.getInt(KEY) | (1 << tier));
    }
    @SubscribeEvent public static void clone(PlayerEvent.Clone event) {
        var root = event.getEntity().getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        root.getCompound(Player.PERSISTED_NBT_TAG).putInt(KEY,
                event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getInt(KEY));
    }
    private FireDiscovery() {}
}
