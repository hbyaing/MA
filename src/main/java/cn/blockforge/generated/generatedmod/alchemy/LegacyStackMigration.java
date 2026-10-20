package cn.blockforge.generated.generatedmod.alchemy;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

/** Runs before ItemStack resolves its registry id, including stacks in unopened containers. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class LegacyStackMigration {
    public record Alias(String item, String grade, String element) {}

    public static Alias alias(ResourceLocation key) {
        if (key == null || !key.getNamespace().equals(GeneratedMod.MOD_ID)) return null;
        String path = key.getPath();
        if (path.equals("tribulation_pill_common")) return new Alias("tribulation_pill", "LOW", "");
        for (PillData.Grade grade : PillData.Grade.values()) {
            String suffix = "_" + grade.name().toLowerCase(java.util.Locale.ROOT);
            if (!path.endsWith(suffix)) continue;
            String base = path.substring(0, path.length() - suffix.length());
            if (PillCatalog.byId(base) != null) return new Alias(base, grade.name(), "");
            String element = switch (base) {
                case "root_gold" -> "METAL";
                case "root_wood" -> "WOOD";
                case "root_water" -> "WATER";
                case "root_fire" -> "FIRE";
                case "root_earth" -> "EARTH";
                default -> "";
            };
            if (!element.isEmpty()) return new Alias("root_awakening_pill", grade.name(), element);
        }
        return null;
    }

    public static CompoundTag migrate(CompoundTag original) {
        if (original == null) return null;
        Alias alias = alias(ResourceLocation.tryParse(original.getString("id")));
        CompoundTag tag = original.getCompound("tag");
        if (alias == null && !hasRemovedEnchantment(tag)) return original;
        // Preserve Count, ForgeCaps and all unknown stack fields without mutating the containing save data.
        CompoundTag migrated = original.copy();
        CompoundTag data = migrated.getCompound("tag");
        if (alias != null) {
            migrated.putString("id", GeneratedMod.MOD_ID + ":" + alias.item());
            if (!data.contains("Grade")) data.putString("Grade", alias.grade());
            if (!alias.element().isEmpty() && !data.contains("Element")) data.putString("Element", alias.element());
        }
        removeLegacyEnchantment(data);
        migrated.put("tag", data);
        return migrated;
    }

    private static boolean hasRemovedEnchantment(CompoundTag tag) {
        for (String key : new String[]{"Enchantments", "StoredEnchantments"}) {
            var list = tag.getList(key, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++)
                if (list.getCompound(i).getString("id").equals("wuxingxiantu:levitation")) return true;
        }
        return false;
    }

    public static void removeLegacyEnchantment(CompoundTag tag) {
        for (String key : new String[]{"Enchantments", "StoredEnchantments"}) {
            var list = tag.getList(key, Tag.TAG_COMPOUND);
            for (int i = list.size() - 1; i >= 0; i--)
                if (list.getCompound(i).getString("id").equals("wuxingxiantu:levitation")) list.remove(i);
        }
    }

    @SubscribeEvent public static void missingMappings(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<Item> mapping : event.<Item>getMappings(Registries.ITEM, GeneratedMod.MOD_ID)) {
            Alias alias = alias(mapping.getKey());
            if (alias != null) mapping.remap(ForgeRegistries.ITEMS.getValue(new ResourceLocation(GeneratedMod.MOD_ID, alias.item())));
        }
        for (var mapping : event.getMappings(Registries.ENCHANTMENT, GeneratedMod.MOD_ID))
            if (mapping.getKey().getPath().equals("levitation")) mapping.ignore();
    }

    private LegacyStackMigration() {}
}
