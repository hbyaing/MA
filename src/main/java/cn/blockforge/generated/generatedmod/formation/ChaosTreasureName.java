package cn.blockforge.generated.generatedmod.formation;

import net.minecraft.network.chat.Component;

/** 保留来源金紫渐变，不改动五行的品阶/名字框架。 */
public final class ChaosTreasureName {
    public static Component gradient(Component name){
        int[] glyphs=name.getString().codePoints().toArray();var result=Component.empty();
        for(int i=0;i<glyphs.length;i++){
            double t=glyphs.length<2?0:(double)i/(glyphs.length-1);
            int r=(int)Math.round(255+(178-255)*t),g=(int)Math.round(203+(91-203)*t),b=(int)Math.round(66+(255-66)*t);
            int color=r<<16|g<<8|b;
            result.append(Component.literal(new String(Character.toChars(glyphs[i]))).withStyle(style->style.withColor(color)));
        }
        return result;
    }
    private ChaosTreasureName(){}
}
