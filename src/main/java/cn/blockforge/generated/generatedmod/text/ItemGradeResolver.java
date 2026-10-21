package cn.blockforge.generated.generatedmod.text;

import cn.blockforge.generated.generatedmod.ArtCatalog;
import cn.blockforge.generated.generatedmod.ExoticFireColorRegistry;
import cn.blockforge.generated.generatedmod.TalismanData;
import cn.blockforge.generated.generatedmod.alchemy.PillData;
import cn.blockforge.generated.generatedmod.fire.FireBottleData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Resolve names and grade rows from the actual stack; rarity never substitutes for quality. */
public final class ItemGradeResolver {
    public record Display(int nameColor,Component grade,boolean creative,boolean internal) {}
    public static Display resolve(ItemStack stack,long time) {
        var entry=ItemTextCatalog.resolve(stack);
        if(entry==null)return null;
        boolean creative=entry.visibility().equals("creative"), internal=entry.visibility().equals("internal");
        if(creative||internal)return new Display(creative?ItemTextPalette.CREATIVE:ItemTextPalette.INTERNAL,
                ItemTextPalette.colored(Component.translatable(creative?"tooltip.wuxingxiantu.creative_test_badge"
                        :"tooltip.wuxingxiantu.internal_badge"),creative?ItemTextPalette.CREATIVE:ItemTextPalette.INTERNAL),creative,internal);
        var pill=PillData.read(stack);
        if(pill!=null) {
            if(pill.pill()==cn.blockforge.generated.generatedmod.alchemy.PillCatalog.CALMING)return new Display(0xC99CEB,
                    ItemTextPalette.colored(Component.translatable("pill.wuxingxiantu.quality",Component.translatable("quality.wuxingxiantu.special")),0xC99CEB),false,false);
            int color=ItemTextPalette.pillGrade(pill.grade().ordinal());
            return new Display(color,ItemTextPalette.colored(Component.translatable("pill.wuxingxiantu.quality",
                    Component.translatable("quality.wuxingxiantu."+pill.grade().ordinal())),color),false,false);
        }
        int fire=FireBottleData.tier(stack);
        if(fire>0)return new Display(entry.nameColor(),ItemTextPalette.colored(
                Component.translatable("tooltip.wuxingxiantu.fire_tier",fire),ExoticFireColorRegistry.accentColor(fire,time)),false,false);
        if(entry.id().equals("talisman")) {
            var quality=TalismanData.quality(stack);
            int color=quality==null?ItemTextPalette.COMMON:ItemTextPalette.talismanGrade(quality.ordinal());
            var name=quality==null?Component.translatable("tooltip.wuxingxiantu.talisman_no_quality")
                    :Component.translatable(quality.key());
            return new Display(color,ItemTextPalette.colored(Component.translatable("tooltip.wuxingxiantu.talisman_quality",name),color),false,false);
        }
        if(entry.id().startsWith("art_"))return new Display(entry.nameColor(),ItemTextPalette.colored(
                Component.translatable("tooltip.wuxingxiantu.art_tier",
                        Component.translatable("art_tier.wuxingxiantu."+ArtCatalog.tier(entry.id()))),
                ArtCatalog.tierColor(ArtCatalog.tier(entry.id()))),false,false);
        return new Display(entry.nameColor(),ItemTextPalette.colored(entry.text("category"),entry.gradeColor()),false,false);
    }
    private ItemGradeResolver() {}
}
