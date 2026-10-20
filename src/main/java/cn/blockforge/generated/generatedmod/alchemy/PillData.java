package cn.blockforge.generated.generatedmod.alchemy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/** Forge 1.20.1 canonical stack data. Legacy serialized ids migrate before ItemStack registry lookup. */
public final class PillData {
    public enum Grade { LOW, MID, HIGH, PERFECT;
        public int value() { return ordinal() + 1; }
    }
    public enum Element { METAL, WOOD, WATER, FIRE, EARTH }
    public record View(PillCatalog pill, Grade grade, Element element, int cultivationTier) {}

    public static View read(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!key.getNamespace().equals("wuxingxiantu")) return null;
        String id = key.getPath();
        Grade legacyGrade = Grade.LOW;
        Element legacyElement = Element.METAL;
        if (id.startsWith("root_") && !id.equals("root_awakening_pill")) {
            String[] parts = id.split("_");
            if (parts.length != 3) return null;
            legacyElement = switch (parts[1]) {
                case "gold" -> Element.METAL;
                case "wood" -> Element.WOOD;
                case "water" -> Element.WATER;
                case "fire" -> Element.FIRE;
                case "earth" -> Element.EARTH;
                default -> null;
            };
            if (legacyElement == null) return null;
            legacyGrade = grade(parts[2], Grade.LOW);
            id = "root_awakening_pill";
        } else {
            for (Grade grade : Grade.values()) {
                String suffix = "_" + grade.name().toLowerCase(Locale.ROOT);
                if (id.endsWith(suffix)) {
                    id = id.substring(0, id.length() - suffix.length());
                    legacyGrade = grade;
                    break;
                }
            }
            if (id.equals("tribulation_pill_common")) id = "tribulation_pill";
        }
        PillCatalog pill = PillCatalog.byId(id);
        if (pill == null) return null;
        if (pill == PillCatalog.CALMING) return new View(pill, Grade.LOW, legacyElement, 0);
        CompoundTag tag = stack.getTag();
        Grade grade = tag == null ? legacyGrade : grade(tag.getString("Grade"), legacyGrade);
        Element element = legacyElement;
        if (tag != null && tag.contains("Element")) {
            try { element = Element.valueOf(tag.getString("Element")); }
            catch (IllegalArgumentException ignored) { element = legacyElement; }
        }
        int tier = tag == null ? 0 : Math.max(0, Math.min(8, tag.getInt("CultivationTier")));
        return new View(pill, grade, element, tier);
    }

    public static Grade grade(String value, Grade fallback) {
        try { return Grade.valueOf(value.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ignored) { return fallback; }
    }

    public static ItemStack create(PillCatalog pill, Grade grade, Element element, int tier) {
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation("wuxingxiantu", pill.id));
        ItemStack stack = new ItemStack(item);
        if (pill != PillCatalog.CALMING) stack.getOrCreateTag().putString("Grade", grade.name());
        if (pill == PillCatalog.ROOT) stack.getOrCreateTag().putString("Element", element.name());
        if (pill == PillCatalog.CULTIVATION) stack.getOrCreateTag().putInt("CultivationTier", Math.max(0, Math.min(8, tier)));
        return stack;
    }

    public static boolean isLegacy(ItemStack stack) {
        View view = read(stack);
        return view != null && !BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().equals(view.pill.id);
    }

    public static ItemStack normalize(ItemStack original) {
        View view = read(original);
        if (view == null) return original;
        ItemStack canonical = create(view.pill, view.grade, view.element, view.cultivationTier);
        if (original.hasTag()) {
            CompoundTag tags = original.getTag().copy();
            tags.remove("Grade");
            tags.merge(canonical.getOrCreateTag());
            canonical.setTag(tags);
        }
        canonical.setCount(original.getCount());
        return canonical;
    }

    public static Component name(View view) {
        if(view.pill==PillCatalog.CALMING)return Component.translatable("item.wuxingxiantu.calming_pill").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE);
        Component quality = Component.translatable("quality.wuxingxiantu." + view.grade.ordinal());
        Component type = view.pill == PillCatalog.ROOT
                ? Component.translatable("pill.wuxingxiantu.root_name", Component.translatable("root.wuxingxiantu." + view.element.ordinal()))
                : Component.translatable("item.wuxingxiantu." + view.pill.id);
        return cn.blockforge.generated.generatedmod.text.ItemTextPalette.colored(
                Component.translatable("pill.wuxingxiantu.graded_name", quality, type),
                cn.blockforge.generated.generatedmod.text.ItemTextPalette.pillGrade(view.grade.ordinal()));
    }

    private PillData() {}
}
