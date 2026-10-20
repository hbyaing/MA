package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.boss.combat.*;
import cn.blockforge.generated.generatedmod.network.packet.divineart.TransformAbilityPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

/** Every skill is available regardless of the original NPC's phase threshold. */
public final class BossTransformSkillScreen extends Screen{
    private final String form;
    private final List<BossCombatCatalog.Skill> skills=new ArrayList<>();
    private int offset,visible;
    public BossTransformSkillScreen(String form){super(Component.literal("七十二变 · Boss 技能"));this.form=form;var profile=BossPlayerSkills.profile(form);if(profile!=null){skills.add(BossBasicAttacks.get(profile.id()).skill(profile.id()));skills.addAll(profile.skills());}}
    @Override protected void init(){clearWidgets();visible=Math.max(1,Math.min(11,(height-92)/24));offset=Math.max(0,Math.min(offset,Math.max(0,skills.size()-visible)));int left=Math.max(8,width/2-155),size=Math.min(310,width-16);
        for(int row=0;row<visible&&offset+row<skills.size();row++){var skill=skills.get(offset+row);addRenderableWidget(Button.builder(Component.literal(skill.name()+"  ·  "+(skill.cooldown()/20.0)+"秒"),button->{Network.CHANNEL.sendToServer(new TransformAbilityPacket(skill.id()));onClose();}).bounds(left,54+row*24,size,20).tooltip(Tooltip.create(Component.literal("前摇 "+skill.windup()+"刻 · 收招 "+skill.recovery()+"刻\n"+skill.hint()))).build());}
    }
    @Override public void tick(){var data=minecraft.player==null?null:GeneratedMod.data(minecraft.player);if(data==null||!"qi_shi_er_bian".equals(data.wuxingxiantu$getActiveDivineArt())||!form.equals(data.wuxingxiantu$getActivePossessedForm()))onClose();}
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partial){renderBackground(graphics);graphics.drawCenteredString(font,title,width/2,12,0xE8C477);graphics.drawCenteredString(font,Component.literal("点击施放 · 左键普通攻击 · 滚轮翻页"),width/2,30,0xFFFFFF);super.render(graphics,mouseX,mouseY,partial);}
    @Override public boolean mouseScrolled(double x,double y,double delta){offset+=delta>0?-1:1;init();return true;}
    @Override public boolean isPauseScreen(){return false;}
}
