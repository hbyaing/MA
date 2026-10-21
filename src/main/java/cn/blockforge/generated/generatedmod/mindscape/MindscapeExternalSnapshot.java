package cn.blockforge.generated.generatedmod.mindscape;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.robe.RobeSlots;
import cn.blockforge.generated.generatedmod.robe.YujianSlot;
import cn.blockforge.generated.generatedmod.yujian.YujianController;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;

/** Stores all temporary real-world state while the player's mind is in a challenge. */
public final class MindscapeExternalSnapshot {
    private static final int VERSION = 1;
    private static final String KEY = "external_snapshot";
    private static final java.util.Set<java.util.UUID> RESTORING = new java.util.HashSet<>();

    private MindscapeExternalSnapshot() {}

    private static CompoundTag root(ServerPlayer player) {
        return GeneratedMod.data(player).wuxingxiantu$getMindscapeData();
    }

    public static boolean isSealed(ServerPlayer player) {
        return isValid(player) && root(player).getCompound(KEY).getBoolean("external_sealed");
    }

    public static boolean isValid(ServerPlayer player) {
        CompoundTag snapshot = root(player).getCompound(KEY);
        return snapshot.getInt("version") == VERSION && snapshot.getBoolean("valid")
                && !snapshot.getBoolean("restored")
                && snapshot.contains("inventory", net.minecraft.nbt.Tag.TAG_LIST)
                && snapshot.contains("robe_stack", net.minecraft.nbt.Tag.TAG_COMPOUND)
                && snapshot.contains("yujian_stack", net.minecraft.nbt.Tag.TAG_COMPOUND);
    }

    public static boolean capture(ServerPlayer player) {
        CultivationData data = GeneratedMod.data(player);
        if (data == null) return false;
        CompoundTag mindscape = data.wuxingxiantu$getMindscapeData();
        CompoundTag existing = mindscape.getCompound(KEY);
        if (existing.getBoolean("valid") && !existing.getBoolean("restored")) {
            if (!isValid(player)) return false;
            existing.putBoolean("external_sealed", true);
            mindscape.put(KEY, existing);
            return true;
        }

        YujianController.stop(player);
        player.closeContainer();
        player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);

        CompoundTag snapshot = new CompoundTag();
        snapshot.putInt("version", VERSION);
        snapshot.putBoolean("valid", true);
        snapshot.putBoolean("restored", false);
        snapshot.putInt("selected_slot", player.getInventory().selected);
        ListTag inventory = new ListTag();
        player.getInventory().save(inventory);
        snapshot.put("inventory", inventory);
        snapshot.put("robe_stack", RobeSlots.getRobe(player).save(new CompoundTag()));
        snapshot.put("yujian_stack", YujianSlot.activeFlyingSword(player).save(new CompoundTag()));
        ListTag effects = new ListTag();
        for (MobEffectInstance effect : player.getActiveEffects()) effects.add(effect.save(new CompoundTag()));
        snapshot.put("effects", effects);
        snapshot.putFloat("original_health", player.getHealth());
        snapshot.putFloat("original_absorption", player.getAbsorptionAmount());
        snapshot.putInt("original_food", player.getFoodData().getFoodLevel());
        snapshot.putFloat("original_saturation", player.getFoodData().getSaturationLevel());
        snapshot.putBoolean("external_sealed", true);
        mindscape.put(KEY, snapshot);

        // Remove equipment attribute modifiers before clearing the inventory. This is
        // required because the custom robe and the vanilla armor both affect max health,
        // armor, movement, and attack through the live attribute map.
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack equipped = player.getItemBySlot(slot);
            if (!equipped.isEmpty()) removeItemModifiers(player, slot, equipped);
            player.setItemSlot(slot, ItemStack.EMPTY);
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            player.getInventory().setItem(slot, ItemStack.EMPTY);
        }
        data.wuxingxiantu$setRobeStack(ItemStack.EMPTY);
        data.wuxingxiantu$setYujianStack(ItemStack.EMPTY);
        for (MobEffectInstance effect : player.getActiveEffects().toArray(MobEffectInstance[]::new)) {
            player.removeEffect(effect.getEffect());
        }
        CultivationLogic.applyHealthBonus(player, data);
        player.setAbsorptionAmount(0.0F);
        player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
        player.getInventory().selected = 0;
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        snapshot.putBoolean("external_sealed", true);
        mindscape.put(KEY, snapshot);
        return true;
    }

    public static boolean restore(ServerPlayer player) {
        CultivationData data = GeneratedMod.data(player);
        if (data == null) return false;
        CompoundTag mindscape = data.wuxingxiantu$getMindscapeData();
        CompoundTag snapshot = mindscape.getCompound(KEY);
        if (snapshot.getBoolean("restored")) return true;
        if (!isValid(player)) return false;

        // Prevent re-entry without destroying the durable recovery record on failure.
        if (!RESTORING.add(player.getUUID())) return false;
        try {

        ListTag inventory = snapshot.getList("inventory", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack equipped = player.getItemBySlot(slot);
            if (!equipped.isEmpty()) removeItemModifiers(player, slot, equipped);
            player.setItemSlot(slot, ItemStack.EMPTY);
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            player.getInventory().setItem(slot, ItemStack.EMPTY);
        }
        player.getInventory().load(inventory);
        player.getInventory().selected = Math.max(0, Math.min(8, snapshot.getInt("selected_slot")));
        data.wuxingxiantu$setRobeStack(ItemStack.of(snapshot.getCompound("robe_stack")));
        data.wuxingxiantu$setYujianStack(ItemStack.of(snapshot.getCompound("yujian_stack")));
        for (MobEffectInstance effect : player.getActiveEffects().toArray(MobEffectInstance[]::new)) {
            player.removeEffect(effect.getEffect());
        }
        for (var raw : snapshot.getList("effects", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            MobEffectInstance effect = raw instanceof CompoundTag compound ? MobEffectInstance.load(compound) : null;
            if (effect != null) player.addEffect(effect);
        }
        player.getFoodData().setFoodLevel(Math.max(0, Math.min(20, snapshot.getInt("original_food"))));
        player.getFoodData().setSaturation(finiteClamp(snapshot.getFloat("original_saturation"), 0, player.getFoodData().getFoodLevel()));
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            player.getAttributes().addTransientAttributeModifiers(player.getItemBySlot(slot).getAttributeModifiers(slot));
        }
        CultivationLogic.applyHealthBonus(player, data);
        player.setAbsorptionAmount(finiteClamp(snapshot.getFloat("original_absorption"), 0, 1_000_000));
        player.setHealth(finiteClamp(snapshot.getFloat("original_health"), 1, player.getMaxHealth()));
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        RobeSlots.sync(player);
        snapshot.putBoolean("restored", true);
        snapshot.putBoolean("external_sealed", false);
        mindscape.put(KEY, snapshot);
        return true;
        } finally {
            RESTORING.remove(player.getUUID());
        }
    }

    private static float finiteClamp(float value, float min, float max) {
        return Float.isFinite(value) ? Math.max(min, Math.min(max, value)) : min;
    }

    private static void removeItemModifiers(ServerPlayer player, EquipmentSlot slot, ItemStack stack) {
        for (var entry : stack.getAttributeModifiers(slot).entries()) {
            Attribute attribute = entry.getKey();
            var instance = player.getAttribute(attribute);
            if (instance != null) instance.removeModifier(entry.getValue());
        }
    }
}
