package cn.blockforge.generated.generatedmod.text;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Name rarity and system-specific grades share colors, never gameplay meanings. */
public final class ItemTextPalette {
    public static final int COMMON=0xD6D6D6, UNCOMMON=0x79C58B, RARE=0x80D9F2,
            EPIC=0xE2A7F5, LEGENDARY=0xFFC94A, UNIQUE=0xFF6B6B,
            MORTAL=0xB7B0A3, CREATIVE=0x777777, INTERNAL=0x555555;
    public static int pillGrade(int ordinal) {
        return switch(ordinal) {case 1 -> RARE; case 2 -> EPIC; case 3 -> LEGENDARY; default -> UNCOMMON;};
    }
    public static int talismanGrade(int ordinal) {
        return switch(ordinal) {case 1 -> UNCOMMON; case 2 -> RARE; case 3 -> LEGENDARY; default -> CREATIVE;};
    }
    public static MutableComponent colored(Component text,int color) {
        return text.copy().withStyle(style->style.withColor(color&0xFFFFFF));
    }
    private ItemTextPalette() {}
}
