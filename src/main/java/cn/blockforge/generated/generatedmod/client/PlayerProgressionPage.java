package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** The page renders only the server's account snapshot. */
public final class PlayerProgressionPage extends CultivationPage {
    private final boolean sense;
    private final java.util.List<CultivationUi.Action> actions=new java.util.ArrayList<>();
    public PlayerProgressionPage(boolean sense){super(sense?"识海":"百艺");this.sense=sense;}
    @Override public int contentHeight(int w,int h){return Math.max(h,sense?370:470);}
    @Override protected void init(){
        actions.clear();
        PlayerProgressionClient.request("refresh");
        if(sense){
            String[] ids={"open","scan","identify","lock"},names={"开辟识海 · 金丹","神识探查 · 8点","凝神辨识 · 6点","锁定 / 解除 · 4点"};
            for(int i=0;i<ids.length;i++){final String id=ids[i];actions.add(addRenderableWidget(new CultivationUi.Action(12,70+i*28,Math.max(100,width-24),22,Component.literal(names[i]),b->PlayerProgressionClient.request(id))));}
        }else for(int i=0;i<3;i++){
            final String id=PlayerCrafts.IDS[i];
            actions.add(addRenderableWidget(new CultivationUi.Action(Math.max(12,width-96),90+i*122,84,22,Component.literal("习艺 / 晋升"),b->PlayerProgressionClient.request("craft:"+id))));
        }
    }
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        CultivationUi.panel(g,0,0,width,Math.max(height,contentHeight(width,height)),false);
        var n=PlayerProgressionClient.state;
        if(sense){
            actions.get(0).setMessage(Component.literal(n.getBoolean("SenseOpen")?"识海已开辟":"开辟识海 · 金丹"));
            actions.get(0).active=!n.getBoolean("SenseOpen")&&n.getInt("Realm")>=2&&n.getInt("Channel")==0;
            line(g,"识海 · "+(n.getBoolean("SenseOpen")?"已开辟":"未开辟"),12,12,CultivationUi.GOLD);
            int cap=n.getInt("Capacity");float ratio=cap==0?0:Math.max(0,Math.min(1,n.getFloat("Sense")/cap));
            line(g,"神识 "+Math.round(n.getFloat("Sense"))+" / "+cap+"  · 探查半径 "+n.getInt("Radius")+"格",12,30,CultivationUi.CYAN);
            g.fill(12,48,width-12,52,CultivationUi.PANEL);g.fill(12,48,12+(int)((width-24)*ratio),52,CultivationUi.CYAN);
            String status=n.getInt("Channel")>0?"凝神中，剩余 "+(int)Math.ceil(n.getInt("Remaining")/20.0)+"秒":n.getString("Lock").isEmpty()?"未锁定目标":"锁定："+n.getString("Lock");
            line(g,status,12,191,CultivationUi.GOLD);
            g.drawWordWrap(font,Component.literal("开辟识海需静止5秒；辨识需凝神2秒。受击或移动会中断。锁定每秒消耗1点，遮挡或超距自动解除。可在按键设置中绑定快捷键。"),12,211,width-24,CultivationUi.MUTED);
            g.drawWordWrap(font,Component.literal(n.getString("Notice")),12,274,width-24,CultivationUi.TEXT);
        }else{
            for(int i=0;i<3;i++){
                var p=n.getCompound(PlayerCrafts.IDS[i]);int rank=Math.max(0,Math.min(8,p.getInt("Rank"))),next=Math.min(8,rank+1),y=18+i*122,bonus=p.getBoolean("Learned")?rank+1:0;
                actions.get(i).setMessage(Component.literal(!p.getBoolean("Learned")?"习得技艺":rank==8?"已臻九品":"考核晋升"));actions.get(i).active=!p.getBoolean("Learned")||rank<8;
                line(g,PlayerCrafts.NAMES[i]+" · "+(p.getBoolean("Learned")?PlayerCrafts.RANKS[rank]:"未习得"),12,y,CultivationUi.GOLD);
                line(g,i==0?"废丹率 -"+bonus+"个百分点 · 最低2%":i==1?"炼器耗灵 -"+(bonus*2.5)+"%":"有效笔迹评分 +"+bonus+"分",12,y+17,CultivationUi.CYAN);
                line(g,"心得 "+p.getInt("Xp")+"  · 代表作 "+p.getInt("WorksCount"),12,y+31,CultivationUi.TEXT);
                String req=rank==8?"已臻九品": "晋升：心得"+PlayerCrafts.XP[next]+" / 作品"+PlayerCrafts.WORKS[next]+" / 作品档次"+(PlayerCrafts.TIERS[next]+1)+" / 境界"+realmName(PlayerCrafts.REALMS[next]);
                g.drawWordWrap(font,Component.literal(req),12,y+45,width-24,CultivationUi.MUTED);
                CultivationUi.rule(g,12,y+105,width-24);
            }
            g.drawWordWrap(font,Component.literal("三艺可兼修。成功制作获得心得；重复衰减，拓印不奖励。加成不绕过配方、异火和境界限制。"),12,390,width-24,CultivationUi.MUTED);
            g.drawWordWrap(font,Component.literal(n.getString("Notice")),12,435,width-24,CultivationUi.TEXT);
        }
        super.render(g,mx,my,delta);
    }
    private static String realmName(int r){return cn.blockforge.generated.generatedmod.items.CultivationLogic.realmName(r);}
    private void line(GuiGraphics g,String text,int x,int y,int color){CultivationUi.fit(g,Component.literal(text),x,y,width-x-12,color,-1,-1);}
}
