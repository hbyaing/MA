package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.items.FireSeedItem;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import cn.blockforge.generated.generatedmod.client.CultivationVisionLayout.Box;

/** Reference layout, actual player effects and existing server-authoritative operations. */
public final class CultivationVisionPage extends CultivationPage {
    private record Binding(CultivationUi.Action button, Supplier<Component> label, BooleanSupplier enabled) {}
    private static final String[] ROOT_NAMES={"gold","wood","water","fire","earth"};
    private static final int[] ROOT_WIDTHS={72,74,74,76,73};
    private final List<Binding> bindings=new ArrayList<>();
    private final List<CultivationUi.Action> options=new ArrayList<>();
    private CultivationVisionLayout layout;
    private Box popup;
    private int popupMode,optionFocus,effectScroll;
    private boolean draggingEffects;
    public CultivationVisionPage(){super("内视");}
    private CultivationData data(){return GeneratedMod.data(minecraft.player);}
    @Override public int contentHeight(int w,int h){return CultivationVisionLayout.of(w,h).height();}
    private void button(int x,int y,int w,int h,Supplier<Component> label,Runnable action,BooleanSupplier enabled){
        var b=addRenderableWidget(new CultivationUi.Action(x,y,w,h,label.get(),ignored->action.run()));
        b.chrome=h==20?1:2;
        bindings.add(new Binding(b,label,enabled));
    }
    @Override protected void init(){
        layout=CultivationVisionLayout.of(width,height);bindings.clear();closePopup();
        Box s=layout.selections(),a=layout.actions(),r=layout.roots();
        button(s.x()+5,s.y()+16,s.w()-10,20,this::selectedDomain,()->openPopup(1),()->data()!=null);
        button(s.x()+5,s.y()+58,s.w()-10,20,this::selectedFire,()->openPopup(2),()->data()!=null);
        for(int root=0;root<5;root++){
            final int index=root;
            var switchButton=addRenderableWidget(new CultivationUi.Action(rootX(r,root)+(rootWidth(r,root)-24)/2,r.y()+18,24,26,
                    Component.literal(ROOT_NAMES[root]),ignored->{if(index==1||index==3)Network.CHANNEL.sendToServer(new RootFormPacket(index==1?0:1));}) {
                @Override protected void renderWidget(GuiGraphics g,int mx,int my,float delta) {
                    boolean owned=data()!=null&&(data().wuxingxiantu$getRootMask()&(1<<index))!=0;
                    var texture=ResourceLocation.fromNamespaceAndPath("wuxingxiantu","textures/gui/wuxia/vision/roots/"+ROOT_NAMES[index]+".png");
                    com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();g.setColor(1,1,1,owned?1:.35F);
                    boolean thunder=data()!=null&&index==1&&CultivationLogic.isThunderForm(data());
                    boolean exotic=data()!=null&&index==3&&CultivationLogic.isExoticForm(data());
                    if(thunder||exotic)CultivationVisionAssets.draw(g,thunder?"root_thunder":"root_exotic",getX(),getY(),24,26);
                    else g.blit(texture,getX(),getY(),24,26,0,0,ROOT_WIDTHS[index],79,ROOT_WIDTHS[index],79);
                    g.setColor(1,1,1,1);com.mojang.blaze3d.systems.RenderSystem.disableBlend();
                    if(thunder||exotic){
                        g.drawString(font,thunder?"雷":"异",getX()+8,getY()+9,thunder?0xD9C4FF:0xA0F0FF,false);
                    }
                    if(isHoveredOrFocused()){
                        CultivationUi.tooltip((index==1||index==3)?rootHint(index).copy().append("\n").append(formHint(index==1?0:1)):rootHint(index));
                    }
                }
            });
            bindings.add(new Binding(switchButton,()->Component.literal(ROOT_NAMES[index]),
                    ()->data()!=null&&(index==1||index==3)&&(data().wuxingxiantu$getRootMask()&(1<<index))!=0));
        }
        int actionWidth=(a.w()-14)/2;
        button(a.x()+5,a.y()+18,actionWidth,22,()->Component.literal("渡劫"),()->Network.CHANNEL.sendToServer(new TribulationPacket()),
                ()->data()!=null&&TribulationManager.canStart(minecraft.player,data()));
        button(a.x()+9+actionWidth,a.y()+18,a.w()-14-actionWidth,22,()->Component.literal("凝晶"),()->Network.CHANNEL.sendToServer(new CrystallizePacket()),
                ()->data()!=null&&data().wuxingxiantu$getAura()>=CultivationLogic.CRYSTALLIZE_AURA_COST);
        tick();
    }
    private Component formHint(int form){
        var d=data();
        var hint=Component.literal(form==0?"木 / 天雷灵根切换":"火 / 异火灵根切换");
        if(d==null)return hint;
        boolean ready=form==0?CultivationLogic.thunderRootAvailable(d):CultivationLogic.exoticRootAvailable(d);
        if(ready)return hint.append("\n点击灵根图标切换形态");
        if(form==0){
            hint.append("\n天雷淬体："+Math.min(d.wuxingxiantu$getThunderStrikes(),CultivationLogic.THUNDER_STRIKES_REQUIRED)+" / "+CultivationLogic.THUNDER_STRIKES_REQUIRED);
            hint.append("\n显化要求：").append(Component.literal(CultivationLogic.realmName(minecraft.player,ProgressionGate.THUNDER_ROOT_REALM)));
            hint.append("，木灵根").append(Component.translatable("quality.wuxingxiantu."+(ProgressionGate.THUNDER_ROOT_QUALITY-1))).append("及以上");
        }else hint.append("\n需解锁异火灵根，并满足境界、火灵根品质及已掌握异火品阶条件");
        return hint;
    }
    @Override public void tick(){
        for(var b:bindings){b.button().setMessage(b.label().get());b.button().active=b.enabled().getAsBoolean();}
        for(int i=0;i<options.size();i++){
            options.get(i).active=popupMode==1?ownsDomain(i):i==9?data()!=null:data()!=null&&data().wuxingxiantu$hasLearnedFire(i+1);
            if(popupMode==1)options.get(i).setMessage(domainLabel(i));
        }
        effectScroll=Math.max(0,Math.min(effectScroll,maxEffectScroll()));
    }
    private Component selectedDomain(){
        if(data()!=null)for(int i=0;i<5;i++)if(domainActive(i))return domainName(i).append("  ▾");
        return Component.literal("未开启领域  ▾");
    }
    private Component selectedFire(){int tier=data()==null?0:data().wuxingxiantu$getEquippedFire();return Component.literal((tier>0?FireSeedItem.getFireName(tier):"未装备异火")+"  ▾");}
    private void openPopup(int mode){
        popupMode=mode;options.clear();optionFocus=0;Box s=layout.selections();int h=mode==1?116:112,anchor=s.y()+(mode==1?16:58);
        popup=new Box(s.x()+5,Math.max(0,anchor-h-3),s.w()-10,h);
        if(mode==1)for(int i=0;i<5;i++){
            final int root=i;
            options.add(new CultivationUi.Action(popup.x()+4,popup.y()+14+i*20,popup.w()-8,19,domainLabel(i),b->{toggleDomain(root);closePopup();},()->domainActive(root)));
        }else{
            int start=popup.x()+(popup.w()-78)/2;
            for(int i=1;i<=9;i++){
                final int tier=i;
                options.add(new CultivationUi.Icon(start+(i-1)%3*26,popup.y()+14+(i-1)/3*26,24,"",()->new ItemStack(FireSeedItem.seedForTier(tier)),
                        ()->Component.literal(FireSeedItem.getFireName(tier)+" · "+(data().wuxingxiantu$getEquippedFire()==tier?"已装备":data().wuxingxiantu$hasLearnedFire(tier)?"已获得":"未获得")),
                        b->{Network.CHANNEL.sendToServer(new FireSelectPacket(data().wuxingxiantu$getEquippedFire()==tier?0:tier));closePopup();},()->data()!=null&&data().wuxingxiantu$getEquippedFire()==tier));
            }
            options.add(new CultivationUi.Action(popup.x()+4,popup.y()+94,popup.w()-8,16,Component.literal("卸下异火"),b->{Network.CHANNEL.sendToServer(new FireSelectPacket(0));closePopup();}));
        }
        tick();
    }
    private void closePopup(){popupMode=0;popup=null;options.clear();}
    @Override public boolean closeTransientUi(){if(popupMode==0)return false;closePopup();return true;}
    @Override public boolean keyPressed(int key,int scan,int mods){
        if(popupMode==0)return super.keyPressed(key,scan,mods);
        if(key==256){closePopup();return true;}
        if(key==265||key==264||key==258){optionFocus=Math.floorMod(optionFocus+(key==265||(key==258&&(mods&1)!=0)?-1:1),options.size());return true;}
        if(key==257||key==335||key==32){var b=options.get(optionFocus);if(b.active)b.onPress();return true;}
        return true;
    }
    @Override public boolean mouseClicked(double x,double y,int button){
        if(popupMode!=0){if(!popup.contains(x,y)){closePopup();return true;}for(var b:List.copyOf(options))if(b.mouseClicked(x,y,button))return true;return true;}
        Box e=layout.effects();
        if(button==0&&maxEffectScroll()>0&&x>=e.x()+e.w()-8&&x<e.x()+e.w()&&y>=e.y()+22&&y<e.y()+e.h()-6){draggingEffects=true;moveEffectScroll(y);return true;}
        return super.mouseClicked(x,y,button);
    }
    @Override public boolean mouseReleased(double x,double y,int button){if(draggingEffects){draggingEffects=false;return true;}return super.mouseReleased(x,y,button);}
    @Override public boolean mouseDragged(double x,double y,int button,double dx,double dy){if(draggingEffects){moveEffectScroll(y);return true;}return super.mouseDragged(x,y,button,dx,dy);}
    private void moveEffectScroll(double y){
        Box e=layout.effects();int track=e.h()-28,total=effects().size()*28,thumb=Math.max(16,track*track/Math.max(1,total));
        effectScroll=(int)Math.round(Math.max(0,Math.min(1,(y-e.y()-22-thumb/2.0)/Math.max(1,track-thumb)))*maxEffectScroll());
    }
    @Override public boolean mouseScrolled(double x,double y,double amount){
        if(popupMode!=0)return true;
        if(layout.effects().contains(x,y)&&maxEffectScroll()>0){effectScroll=(int)Math.max(0,Math.min(maxEffectScroll(),effectScroll-amount*28));return true;}
        return super.mouseScrolled(x,y,amount);
    }
    private List<MobEffectInstance> effects(){
        if(minecraft==null||minecraft.player==null)return List.of();
        return minecraft.player.getActiveEffects().stream().sorted(Comparator.comparing((MobEffectInstance e)->!e.getEffect().isBeneficial()).thenComparing(e->e.getEffect().getDisplayName().getString())).toList();
    }
    private int maxEffectScroll(){return layout==null?0:Math.max(0,effects().size()*28-(layout.effects().h()-28));}
    private void card(GuiGraphics g,Box b,String title){
        g.fill(b.x(),b.y(),b.x()+b.w(),b.y()+b.h(),0x65F6EBDD);CultivationBorderAssets.frame(g,"ink",b.x(),b.y(),b.w(),b.h());
        g.drawString(font,title,b.x()+5,b.y()+4,CultivationUi.INK,false);
        int start=b.x()+Math.min(b.w()-8,font.width(title)+12);
        CultivationBorderAssets.draw(g,"rule_ink",start,b.y()+6,b.x()+b.w()-5-start,4);
    }
    private void line(GuiGraphics g,Box b,Component value,int y,int color,int mx,int my){CultivationUi.fit(g,value,b.x()+6,b.y()+y,b.w()-12,color,mx,my);}
    private void attributeLine(GuiGraphics g,Box b,String icon,Component value,int y,int color,int mx,int my){
        CultivationVisionAssets.draw(g,icon,b.x()+5,b.y()+y+(font.lineHeight-18)/2,18,18);
        CultivationUi.fit(g,value,b.x()+28,b.y()+y,b.w()-34,color,mx,my);
    }
    private void labelledAttribute(GuiGraphics g,Box b,String icon,String label,Component value,int y,int color,int mx,int my){
        CultivationVisionAssets.draw(g,icon,b.x()+5,b.y()+y+(font.lineHeight-18)/2,18,18);
        int x=b.x()+28,labelWidth=font.width(label+"：");
        g.drawString(font,label+"：",x,b.y()+y,CultivationUi.INK,false);
        CultivationUi.fit(g,value,x+labelWidth,b.y()+y,Math.max(0,b.w()-34-labelWidth),color,mx,my);
    }
    private void compactAttribute(GuiGraphics g,Box b,String icon,Component value,int y,int color){
        int inset=27;
        int available=Math.max(1,b.w()-inset-3);
        float scale=Math.max(.8F,Math.min(1F,available/(float)Math.max(1,font.width(value))));
        Component visible=value;
        if(font.width(value)*scale>available)visible=Component.literal(font.plainSubstrByWidth(value.getString(),Math.max(0,(int)(available/scale)-font.width("…")))+"…");
        if(icon!=null)CultivationVisionAssets.draw(g,icon,b.x()+4,b.y()+y+Math.round((font.lineHeight*scale-18)/2F),18,18);
        g.pose().pushPose();g.pose().translate(b.x()+inset,b.y()+y,0);g.pose().scale(scale,scale,1);
        g.drawString(font,visible,0,0,color,false);g.pose().popPose();
    }
    private void resource(GuiGraphics g,Box b,String label,int y,double value,double max,int color,int mx,int my){
        String icon=label.equals("生命")?"health":label.equals("灵力")?"mana":"cultivation";
        // Center the icon across the text row and its bar (y through y+19).
        CultivationVisionAssets.draw(g,icon,b.x()+5,b.y()+y+(19-18)/2,18,18);
        CultivationUi.fit(g,Component.literal(label+"："+CultivationUi.number(value)+" / "+CultivationUi.number(max)),
                b.x()+28,b.y()+y,b.w()-34,CultivationUi.INK,mx,my);
        int x=b.x()+28,w=b.w()-34;
        String asset=label.equals("生命")?"bar_health":label.equals("灵力")?"bar_mana":"bar_cultivation";
        CultivationVisionAssets.draw(g,asset+"_track",x,b.y()+y+12,w,7);
        CultivationVisionAssets.drawProgress(g,asset,x,b.y()+y+12,w,7,value/Math.max(1,max));
    }
    private void renderStatus(GuiGraphics g,int mx,int my){
        var d=data();Box b=layout.status();card(g,b,"修炼状态");
        attributeLine(g,b,"realm",Component.literal("境界："+CultivationLogic.realmStageName(minecraft.player,d)),22,CultivationUi.INK,mx,my);
        resource(g,b,"修为",42,d.wuxingxiantu$getCultivation(),CultivationLogic.breakthroughRequirement(d),0xFFD6AF5C,mx,my);
        resource(g,b,"灵力",68,d.wuxingxiantu$getCurrentMana(),CultivationLogic.maxAura(d),0xFF4BAFC4,mx,my);
        resource(g,b,"生命",94,minecraft.player.getHealth(),minecraft.player.getMaxHealth(),0xFFCE7666,mx,my);
        int half=(b.w()-6)*56/100;
        compactAttribute(g,new Box(b.x(),b.y(),half,b.h()),"attack",Component.literal("攻击："+CultivationUi.number(minecraft.player.getAttributeValue(Attributes.ATTACK_DAMAGE))),128,CultivationUi.INK);
        compactAttribute(g,new Box(b.x()+half+6,b.y(),b.w()-half-6,b.h()),"armor",Component.literal("护甲："+CultivationUi.number(minecraft.player.getAttributeValue(Attributes.ARMOR))),128,CultivationUi.INK);
        String heart=d.wuxingxiantu$getEquippedHeartMethod();
        Component method=Component.literal("心法：").append(heart==null||heart.isBlank()?Component.literal("无"):HeartMethodCatalog.shortName(heart));
        Component merit=Component.literal("功德："+(d.wuxingxiantu$getMerit()==Long.MAX_VALUE?"∞":CultivationUi.number(d.wuxingxiantu$getMerit())));
        int meritWidth=b.w()-half-6;
        compactAttribute(g,new Box(b.x(),b.y(),half,b.h()),"heart_method",method,151,0xFF266779);
        compactAttribute(g,new Box(b.x()+half+6,b.y(),meritWidth,b.h()),"merit",merit,151,d.wuxingxiantu$getMerit()<0?0xFFAB302C:CultivationUi.INK);
        if(b.contains(mx,my)&&my>=b.y()+122&&my<b.y()+142)CultivationUi.tooltip(Component.literal("攻击："+CultivationUi.number(minecraft.player.getAttributeValue(Attributes.ATTACK_DAMAGE))+" · 护甲："+CultivationUi.number(minecraft.player.getAttributeValue(Attributes.ARMOR))+"\n读取当前属性。"));
        if(b.contains(mx,my)&&my>=b.y()+145)CultivationUi.tooltip(method.copy().append("\n").append(merit));
    }
    private void renderFoundation(GuiGraphics g,int mx,int my){
        var d=data();Box b=layout.foundation();card(g,b,"修行根基");int dao=Math.max(0,Math.min(8,d.wuxingxiantu$getDaoXin()));
        labelledAttribute(g,b,"dao","道心",Component.translatable("dao.wuxingxiantu."+dao),22,dao==4?0xFFAB302C:CultivationUi.INK,mx,my);
        int status=d.wuxingxiantu$getMeridianStatus();
        labelledAttribute(g,b,"meridian","经脉",Component.translatable("gui.wuxingxiantu.inner_vision.meridian_"+(status>=2?"shattered":status==1?"damaged":"intact")),40,status>0?0xFFAB302C:0xFF30694F,mx,my);
        labelledAttribute(g,b,"physique","体质",Component.translatable("physique.wuxingxiantu."+d.wuxingxiantu$getPhysique().id()),58,0xFF266779,mx,my);
        labelledAttribute(g,b,"purity","阳神纯度",Component.literal(Integer.toString(d.wuxingxiantu$getYinShenPurity())),76,0xFF266779,mx,my);
        if(b.contains(mx,my)) {
            int row=my-b.y();
            if(row>=17&&row<35)CultivationUi.tooltip(Component.translatable("dao.wuxingxiantu."+dao).append("\n").append(Component.translatable("dao.effect.wuxingxiantu."+dao)));
            else if(row>=35&&row<53)CultivationUi.tooltip(status==0?Component.literal("经脉完整\n经脉无损伤减益"):
                    Component.translatable("effect.wuxingxiantu.meridian_"+(status>=2?"shattered":"damaged")+".description"));
            else if(row>=53&&row<71)CultivationUi.tooltip(physiqueHint(d));
        }
    }
    private Component physiqueHint(CultivationData d){
        boolean advanced=d.wuxingxiantu$getRealm()>=3;
        String effect=switch(d.wuxingxiantu$getPhysique()){
            case TAICHU->"无额外体质效果";
            case QINGLIAN->advanced?"灵力上限 +10%，灵力恢复 +25%":"灵力恢复 +15%";
            case JIUQIAO->advanced?"灵力上限 +20%，灵力恢复 +10%":"灵力上限 +10%";
            case XUANHUANG->advanced?"生命上限 +20%，护甲 +4":"生命上限 +10%，护甲 +2";
            case ZHANWANG->advanced?"攻击伤害 +15%":"攻击伤害 +8%";
            case TAIYIN->"冰冻伤害 +15%，受到火焰伤害 -20%";
            case TAIYANG->"火焰伤害 +15%，受到冰冻伤害 -20%";
            case WANLING->"生命上限 +25%，攻击伤害 +15%，抗性提升 I\n灵力上限、灵力恢复 "+(advanced?"+25%":"+15%");
        };
        return Component.translatable("physique.wuxingxiantu."+d.wuxingxiantu$getPhysique().id()).append("\n"+effect);
    }
    private void renderRoots(GuiGraphics g,int mx,int my){
        var d=data();Box b=layout.roots();card(g,b,"灵根");
        for(int i=0;i<5;i++){
            int cell=rootWidth(b,i),x=rootX(b,i),y=b.y()+18;boolean owned=(d.wuxingxiantu$getRootMask()&(1<<i))!=0;
            Component grade=!owned?Component.literal("未有"):Component.translatable("quality.wuxingxiantu."+Math.max(0,Math.min(3,d.wuxingxiantu$getRootQuality(i)-1)));
            CultivationUi.fit(g,grade,x+(cell-font.width(grade))/2,b.y()+46,cell,CultivationUi.INK,mx,my);
            if(mx>=x&&mx<x+cell&&my>=b.y()+44&&my<b.y()+59)CultivationUi.tooltip(rootHint(i));
        }
        line(g,b,Component.literal("混沌灵根："+(d.wuxingxiantu$hasChaosRoot()?"已成":"未成")),61,CultivationUi.INK,mx,my);
        if(b.contains(mx,my)&&my>=b.y()+60)CultivationUi.tooltip(rootHint(5));
    }
    private static int rootWidth(Box b,int root){return (b.w()-10)/5;}
    private static int rootX(Box b,int root){int x=b.x()+5;for(int i=0;i<root;i++)x+=rootWidth(b,i);return x;}
    private void renderSelections(GuiGraphics g){Box b=layout.selections();card(g,b,"领域");g.drawString(font,"异火",b.x()+5,b.y()+44,CultivationUi.INK,false);CultivationUi.softDivider(g,b.x()+28,b.y()+48,b.w()-33,false,0x888E7B58);}
    private void renderActions(GuiGraphics g,int mx,int my){
        var d=data();Box b=layout.actions();
        g.drawString(font,"修炼操作",b.x()+5,b.y()+4,CultivationUi.INK,false);
        int rule=b.x()+font.width("修炼操作")+12;
        CultivationBorderAssets.draw(g,"rule_ink",rule,b.y()+6,Math.max(0,b.x()+b.w()-5-rule),4);
        if(b.contains(mx,my)&&my>=b.y()+18&&my<b.y()+40){
            if(mx<b.x()+b.w()/2)CultivationUi.tooltip(Component.literal((TribulationManager.canStart(minecraft.player,d)?"已满足渡劫条件":"渡劫条件未满足")+"\n渡劫条件：大境界巅峰、足够修为、七成灵力、引劫材料，并离开洞天。"));
            else CultivationUi.tooltip(Component.literal("消耗 "+CultivationUi.number(CultivationLogic.CRYSTALLIZE_AURA_COST)+" 灵力\n凝成 1 枚灵力结晶"));
        }
    }
    private void renderEffects(GuiGraphics g,int mx,int my){
        Box b=layout.effects();CultivationUi.background(g,"overview",b.x(),b.y(),b.w(),b.h());var list=effects();
        g.drawString(font,"药水状态 · "+list.size(),b.x()+6,b.y()+6,CultivationUi.GOLD,false);CultivationUi.softDivider(g,b.x()+5,b.y()+18,b.w()-10,false);
        int view=b.h()-28;effectScroll=Math.min(effectScroll,Math.max(0,list.size()*28-view));host.clipPage(g,b.x()+4,b.y()+22,b.w()-13,view);
        if(list.isEmpty())g.drawString(font,"暂无药水效果",b.x()+7,b.y()+28,CultivationUi.MUTED,false);
        for(int i=0;i<list.size();i++){
            int y=b.y()+22+i*28-effectScroll;if(y+27<b.y()+22||y>=b.y()+b.h()-6)continue;
            var instance=list.get(i);var effect=instance.getEffect();int color=effect.isBeneficial()?CultivationUi.GREEN:effect.getCategory()==MobEffectCategory.HARMFUL?CultivationUi.RED:CultivationUi.CYAN;
            g.blit(b.x()+6,y+3,0,18,18,minecraft.getMobEffectTextures().get(effect));
            String level=effect==ModEffects.REALM_FLIGHT.get()
                    ?cn.blockforge.generated.generatedmod.realmflight.RealmFlightLevel.labelForAmplifier(instance.getAmplifier())
                    :"Lv."+(instance.getAmplifier()+1);
            Component name=Component.translatable(effect.getDescriptionId()).append(" "+level);
            int nameWidth=Math.max(1,b.w()-40);
            float nameScale=Math.min(1F,nameWidth/(float)Math.max(1,font.width(name)));
            g.pose().pushPose();g.pose().translate(b.x()+28,y+1,0);g.pose().scale(nameScale,nameScale,1);
            g.drawString(font,name,0,0,CultivationUi.TEXT,false);g.pose().popPose();
            var duration=MobEffectUtil.formatDuration(instance,1.0F);CultivationUi.fit(g,duration,b.x()+28,y+14,Math.max(12,b.w()-40),CultivationUi.GOLD,mx,my);
            CultivationUi.softDivider(g,b.x()+5,y+26,b.w()-15,false,0x665D635C);
            if(b.contains(mx,my)&&mx<b.x()+b.w()-9&&my>=Math.max(y,b.y()+22)&&my<Math.min(y+27,b.y()+b.h()-6))CultivationUi.tooltip(name.copy().append("\n").append(effect.getDisplayName()).append("\n").append(duration));
        }
        g.disableScissor();
        if(maxEffectScroll()>0){int total=list.size()*28,thumb=Math.max(16,view*view/total),y=b.y()+22+effectScroll*(view-thumb)/maxEffectScroll();g.fill(b.x()+b.w()-2,b.y()+22,b.x()+b.w(),b.y()+b.h()-6,0xFF46514C);g.fill(b.x()+b.w()-3,y,b.x()+b.w(),y+thumb,CultivationUi.GOLD);}
    }
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        if(data()==null){g.drawString(font,"正在等待修炼数据同步",6,6,CultivationUi.MUTED,false);return;}
        // The host paints the continuous paper background through the outer frame padding.
        renderStatus(g,mx,my);renderFoundation(g,mx,my);renderRoots(g,mx,my);renderSelections(g);renderActions(g,mx,my);renderEffects(g,mx,my);super.render(g,mx,my,delta);
        if(popupMode!=0){
            g.pose().pushPose();g.pose().translate(0,0,100);g.fill(popup.x()-2,popup.y()-2,popup.x()+popup.w()+2,popup.y()+popup.h()+2,0xFF101E26);g.renderOutline(popup.x()-2,popup.y()-2,popup.w()+4,popup.h()+4,CultivationUi.GOLD);
            g.drawString(font,popupMode==1?"选择领域 · 再点关闭":"选择异火",popup.x()+4,popup.y()+3,CultivationUi.GOLD,false);
            for(int i=0;i<options.size();i++){var b=options.get(i);b.render(g,mx,my,delta);if(i==optionFocus)g.renderOutline(b.getX(),b.getY(),b.getWidth(),b.getHeight(),CultivationUi.CYAN);}g.pose().popPose();
        }
    }
    private Component rootHint(int root){
        var d=data();String key=root==5?"chaos":root==1&&CultivationLogic.isThunderForm(d)?"thunder":root==3&&CultivationLogic.isExoticForm(d)?"exotic_fire":Integer.toString(root);
        boolean owned=root==5?d.wuxingxiantu$hasChaosRoot():(d.wuxingxiantu$getRootMask()&1<<root)!=0;
        var hint=Component.translatable("root.wuxingxiantu."+key).append("\n").append(Component.translatable(owned?"tooltip.wuxingxiantu.root_owned":"tooltip.wuxingxiantu.root_missing"));
        if(root<5&&owned)hint.append(" · ").append(Component.translatable("quality.wuxingxiantu."+Math.max(0,Math.min(3,d.wuxingxiantu$getRootQuality(root)-1))));
        hint.append("\n").append(Component.translatable("tooltip.wuxingxiantu.root_"+key));if(root==1||root==3)hint.append("\n点击图标切换灵根形态（需解锁）");
        if(root<5){
            String domain=root==1&&CultivationLogic.isThunderForm(d)?"thunder":root==3&&CultivationLogic.isExoticForm(d)?"exotic":ROOT_NAMES[root];
            hint.append("\n").append(Component.translatable("effect.wuxingxiantu."+domain+"_domain.description"));
            hint.append("\n").append(Component.translatable(!ownsDomain(root)?"tooltip.wuxingxiantu.domain_locked":domainActive(root)?"tooltip.wuxingxiantu.domain_active":"tooltip.wuxingxiantu.domain_inactive"));
        }
        return hint;
    }
    private boolean ownsDomain(int root){return data()!=null&&(data().wuxingxiantu$getRootMask()&(1<<root))!=0&&data().wuxingxiantu$getRootQuality(root)>=2;}
    private boolean domainActive(int root){var d=data();if(d==null)return false;return switch(root){case 0->d.wuxingxiantu$isGoldDomainEnabled();case 1->d.wuxingxiantu$isWoodDomainEnabled();case 2->d.wuxingxiantu$isWaterDomainEnabled();case 3->d.wuxingxiantu$isFireDomainEnabled();default->d.wuxingxiantu$isEarthDomainEnabled();};}
    private net.minecraft.network.chat.MutableComponent domainName(int root){String name=switch(root){case 0->"gold";case 1->CultivationLogic.isThunderForm(data())?"thunder":"wood";case 2->"water";case 3->CultivationLogic.isExoticForm(data())?"exotic":"fire";default->"earth";};return Component.translatable("gui.wuxingxiantu.inner_vision."+name+"_domain_name");}
    private Component domainLabel(int root){return domainName(root);}
    private void toggleDomain(int root){boolean enable=!domainActive(root);Object packet=switch(root){case 0->new GoldDomainPacket(enable);case 1->new WoodDomainPacket(enable);case 2->new WaterDomainPacket(enable);case 3->new FireDomainPacket(enable);default->new EarthDomainPacket(enable);};Network.CHANNEL.sendToServer(packet);}
}
