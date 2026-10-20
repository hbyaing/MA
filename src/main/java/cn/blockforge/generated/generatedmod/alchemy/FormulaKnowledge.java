package cn.blockforge.generated.generatedmod.alchemy;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-side, saved per-player knowledge. Each formula covers every result grade. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class FormulaKnowledge {
    private static final String KEY = "wuxingxiantu_formulas";
    private static final String UNLOCKED = "alchemyUnlocked";

    public static boolean unlocked(Player player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(UNLOCKED);
    }

    public static boolean unlock(ServerPlayer player) {
        CompoundTag root = saved(player);
        if (root.getBoolean(UNLOCKED)) return false;
        root.putBoolean(UNLOCKED, true);
        return true;
    }

    public static void reset(ServerPlayer player) {
        saved(player).remove(KEY);
        saved(player).putBoolean(UNLOCKED, false);
    }

    public static int learnAll(ServerPlayer player) {
        unlock(player);
        int learned = 0;
        for (PillCatalog pill : PillCatalog.values())
            if (learn(player, new ResourceLocation(GeneratedMod.MOD_ID, pill.id))) learned++;
        return learned;
    }

    private static CompoundTag saved(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(Player.PERSISTED_NBT_TAG)) persistent.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return persistent.getCompound(Player.PERSISTED_NBT_TAG);
    }

    public static boolean knows(Player player, ResourceLocation formula) {
        CompoundTag persistent = player.getPersistentData();
        return persistent.getCompound(Player.PERSISTED_NBT_TAG).getCompound(KEY).getBoolean(formula.toString());
    }

    public static boolean learn(ServerPlayer player, ResourceLocation formula) {
        return learn(player,formula,true);
    }
    public static boolean valid(ResourceLocation formula) {
        return formula!=null&&formula.getNamespace().equals(GeneratedMod.MOD_ID)&&PillCatalog.byId(formula.getPath())!=null;
    }
    public static boolean learn(ServerPlayer player, ResourceLocation formula, boolean notify) {
        if (!valid(formula)) return false;
        CompoundTag root = saved(player);
        CompoundTag formulas = root.getCompound(KEY);
        if (formulas.getBoolean(formula.toString())) return false;
        formulas.putBoolean(formula.toString(), true);
        root.put(KEY, formulas);
        if(notify)player.displayClientMessage(Component.translatable("message.wuxingxiantu.formula_learned",
                Component.translatable("item.wuxingxiantu." + formula.getPath())), false);
        return true;
    }

    /** Old stack data migrates once on login, never as a recurring knowledge reward. */
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag root = saved(player);
        int version = root.getInt("alchemyDataVersion");
        if (version >= 2) return;
        // Existing knowledge remains usable after upgrading, without granting new recipes.
        if (!root.getCompound(KEY).isEmpty()) root.putBoolean(UNLOCKED, true);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            var stack = player.getInventory().getItem(slot);
            if (version < 1 && PillData.isLegacy(stack)) player.getInventory().setItem(slot, PillData.normalize(stack));
        }
        root.putInt("alchemyDataVersion", 2);
        player.getInventory().setChanged();
    }

    @SubscribeEvent public static void clone(PlayerEvent.Clone event) {
        CompoundTag original = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag target = saved(event.getEntity());
        if (original.contains(KEY)) target.put(KEY, original.getCompound(KEY).copy());
        target.putInt("alchemyDataVersion", original.getInt("alchemyDataVersion"));
        target.putBoolean(UNLOCKED, original.getBoolean(UNLOCKED));
    }

    private FormulaKnowledge() {}
}
