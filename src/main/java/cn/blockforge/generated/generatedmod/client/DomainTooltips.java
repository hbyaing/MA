package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.balance.DamageDomainBalance;
import cn.blockforge.generated.generatedmod.balance.DamageDomainBalance.Domain;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.Locale;
import cn.blockforge.generated.generatedmod.text.ItemTextPalette;

/** Live quotes share server balance functions; all root qualities remain inspectable. */
public final class DomainTooltips {
    private DomainTooltips(){}
    private static String number(double value){return String.format(Locale.ROOT,"%.1f",value);}
    /** Root icons show the current usable tier; the selection menu retains all tiers. */
    public static Component describeCurrent(Player player,CultivationData data,int root){
        if(player==null||data==null)return Component.literal("等待同步");
        boolean thunder=root==1&&CultivationLogic.isThunderForm(data),exotic=root==3&&CultivationLogic.isExoticForm(data);
        if(root==0||root==1&&!thunder)return describe(player,data,root);
        int q=data.wuxingxiantu$getRootQuality(root);
        if(q<2)return Component.literal("领域需要中品及以上灵根").withStyle(ChatFormatting.GRAY);
        Domain d=root==1?Domain.THUNDER:root==2?Domain.TIDE:root==3?(exotic?Domain.EXOTIC:Domain.FIRE):Domain.GRAVITY;
        var text=Component.literal("当前领域效果").withStyle(ChatFormatting.GOLD);
        line(text,"半径"+DamageDomainBalance.radius(d,q)+"格；最多"+DamageDomainBalance.targetLimit(d,q)+"个敌对目标",ChatFormatting.GRAY);
        line(text,"单目标伤害 "+number(CultivationLogic.domainDamage(player,d,q))+"；"+(q==2&&(d==Domain.TIDE||d==Domain.GRAVITY)?"仅控制":DamageDomainBalance.damageIntervalTicks(d,q)/20+"秒一次"),ChatFormatting.WHITE);
        int cost=CultivationLogic.domainManaCost(player,data,d,q,1,true,false);
        int quiet=CultivationLogic.domainManaCost(player,data,d,q,1,false,false);
        line(text,"单目标灵力："+(d==Domain.TIDE&&q==3?quiet+"～"+cost:cost)+" / 秒",ChatFormatting.AQUA);
        line(text,features(d,q),ChatFormatting.GRAY);
        line(text,"空场免费，多目标额外收费；灵力不足不释放",ChatFormatting.GRAY);
        line(text,"各品质完整效果可在领域选择列表中查看",ChatFormatting.AQUA);
        return text;
    }
    public static Component describe(Player player,CultivationData data,int root){
        if(player==null||data==null||root<0||root>4)return Component.literal("等待领域数据同步");
        boolean thunder=root==1&&CultivationLogic.isThunderForm(data),exotic=root==3&&CultivationLogic.isExoticForm(data);
        String id=root==0?"gold":root==1?(thunder?"thunder":"wood"):root==2?"water":root==3?(exotic?"exotic":"fire"):"earth";
        var text=Component.translatable("gui.wuxingxiantu.inner_vision."+id+"_domain_name").withStyle(ChatFormatting.GOLD);
        int actual=data.wuxingxiantu$getRootQuality(root);
        text.append("\n").append(Component.literal("当前灵根品质：").withStyle(ChatFormatting.GRAY))
                .append((data.wuxingxiantu$getRootMask()&(1<<root))==0?Component.literal("未获得").withStyle(ChatFormatting.GRAY):quality(actual,false));
        gradeLine(text,1,actual,"：领域未解锁");
        if(root==0||root==1&&!thunder){
            for(int q=2;q<=4;q++){
                String effect;
                if(root==0)effect=q==2?"铁矿→金矿，20灵力/次，无冷却":q==3?"提取矿物，25%双倍；30灵力/次，冷却1秒":"矿石→对应矿物块；40灵力/次，冷却1秒";
                else effect="催生"+(q==4?7:q==3?5:3)+"×"+(q==4?7:q==3?5:3)+"植物，"+(q==4?5:q==3?3:2)+"灵力/次";
                gradeLine(text,q,actual,"："+effect);
            }
            line(text,root==0?"潜行右键对应矿石，成功才扣费":"右键催生植物，成功才扣费",ChatFormatting.GRAY);
            return text;
        }
        Domain domain=root==1?Domain.THUNDER:root==2?Domain.TIDE:root==3?(exotic?Domain.EXOTIC:Domain.FIRE):Domain.GRAVITY;
        line(text,"攻击力："+number(player.getAttributeValue(Attributes.ATTACK_DAMAGE))+"（实时）",ChatFormatting.GRAY);
        for(int q=2;q<=4;q++){
            String interval=DamageDomainBalance.damageIntervalTicks(domain,q)==40?"2秒":"1秒";
            gradeLine(text,q,actual," · 半径"+DamageDomainBalance.radius(domain,q)+"格 · "+((domain==Domain.TIDE||domain==Domain.GRAVITY)&&q==2?"每秒控制":interval+"/伤害"));
            float damage=CultivationLogic.domainDamage(player,domain,q);
            int cost=CultivationLogic.domainManaCost(player,data,domain,q,1,true,false);
            int quiet=CultivationLogic.domainManaCost(player,data,domain,q,1,false,false);
            String mana=domain==Domain.TIDE&&q==3?quiet+"～"+cost:Integer.toString(cost);
            line(text,"单目标伤害 "+number(damage)+" · 灵力 "+mana+"/秒",ChatFormatting.WHITE);
            line(text,features(domain,q),ChatFormatting.GRAY);
        }
        line(text,"空领域不扣费；多目标提高耗蓝，不足则本轮不释放",ChatFormatting.GRAY);
        if(domain==Domain.TIDE)line(text,"极品踏水也消耗灵力；单目标费用包含控制效果",ChatFormatting.GRAY);
        if(thunder||exotic)line(text,"上位形态还需满足其解锁与显化条件",ChatFormatting.GRAY);
        return text;
    }
    private static void line(MutableComponent text,String value,ChatFormatting color){text.append("\n").append(Component.literal(value).withStyle(color));}
    private static MutableComponent quality(int grade,boolean current){
        int index=Math.max(0,Math.min(3,grade-1));
        return Component.translatable("quality.wuxingxiantu."+index)
                .withStyle(style->style.withColor(ItemTextPalette.pillGrade(index)).withBold(current));
    }
    private static void gradeLine(MutableComponent text,int grade,int actual,String details){
        boolean current=grade==actual;
        var label=quality(grade,current);
        if(current)label.append("〔当前〕");
        text.append("\n").append(label).append(Component.literal(details).withStyle(ChatFormatting.GRAY));
    }
    private static String features(Domain domain,int q){
        return switch(domain){
            case FIRE -> "点燃2秒，最多12个敌对目标";
            case EXOTIC -> "装备异火续燃不叠层，最多12个敌对目标";
            case THUNDER -> "最多"+(q-1)+"道雷；麻痹"+number(DamageDomainBalance.paralysisTicks(q)/20.0)+"秒，自身免雷";
            case TIDE -> "减速"+(q==4?40:q==3?30:20)+"%，跳跃降低"+(q==4?50:25)+"%"+(q>=3?"；"+(q==4?20:10)+"%冻结2秒":"");
            case GRAVITY -> "减速"+(q==4?35:q==3?25:15)+"%，抗击退降低50%"+(q>=3?"；"+(q==4?15:10)+"%定身2秒":"")+(q==4?"，跳跃降低50%":"");
        };
    }
}
