package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** The page renders only the server's account snapshot. */
public final class PlayerProgressionPage extends CultivationPage {
    private final boolean sense;
    private final java.util.List<net.minecraft.client.gui.components.Button> actions=new java.util.ArrayList<>();
    public PlayerProgressionPage(boolean sense){super(sense?"识海":"百艺");this.sense=sense;}
    @Override public int contentHeight(int w,int h){return Math.max(h,sense?(w<300?410:256):470);}
    @Override protected void init(){
        actions.clear();
        PlayerProgressionClient.request("refresh");
        if(sense){
            int columns=width<300?1:2,gap=8,cardWidth=(width-24-gap*(columns-1))/columns;
            actions.add(addRenderableWidget(new SenseFurnaceArt.Button(width-96,10,84,18,"开辟识海 · 金丹",b->PlayerProgressionClient.request("open"))));
            actions.get(0).setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("金丹境可开辟识海，保持静止5秒。移动、受击或切换维度会中断；完成后充满本境界神识。")));
            String[] ids={"scan","identify","lock","survey"};
            for(int i=0;i<4;i++){final String id=ids[i];int x=12+(i%columns)*(cardWidth+gap),y=64+(i/columns)*70;
                actions.add(addRenderableWidget(new SenseFurnaceArt.Button(x+cardWidth-49,y+43,43,18,new String[]{"探查","辨识","锁定","开启"}[i],b->PlayerProgressionClient.request(id))));
                actions.get(i+1).setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(new String[]{
                    "探查范围内可见生灵；遮挡气息只给近似方位，不显示穿墙目标。8点神识，冷却4秒。",
                    "保持静止与视线2秒，辨识生灵、物品或方块；受击、移动或转开视线会中断。消耗6点。",
                    "瞄准24格内可见生灵。首次4点，每秒1点；遮挡或超距自动解除。再次点击解除。",
                    "化神期：分批扫描8×8区块，矿物与结构保持现有标记。启动8点，每秒1点，每30秒复查另需8点。"}[i])));
            }
        }else for(int i=0;i<3;i++){
            final String id=PlayerCrafts.IDS[i];
            actions.add(addRenderableWidget(new CultivationUi.Action(Math.max(12,width-96),90+i*122,84,22,Component.literal("习艺 / 晋升"),b->PlayerProgressionClient.request("craft:"+id))));
        }
    }
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        CultivationUi.panel(g,0,0,width,Math.max(height,contentHeight(width,height)),false);
        var n=PlayerProgressionClient.state;
        if(sense){
            boolean opened=n.getBoolean("SenseOpen"),channel=n.getInt("Channel")>0;
            boolean surveying=cn.blockforge.generated.generatedmod.divinesurvey.client.SurveyClient.active();
            boolean locked=!n.getString("Lock").isEmpty();float power=n.getFloat("Sense");int realm=n.getInt("Realm");
            actions.get(0).visible=!opened;actions.get(0).active=!opened&&realm>=2&&!channel;
            actions.get(1).active=opened&&!channel&&power>=8&&n.getInt("ScanCooldown")==0;
            actions.get(2).active=opened&&!channel&&power>=6;
            actions.get(3).active=locked||opened&&!channel&&power>=4;actions.get(3).setMessage(Component.literal(locked?"解除":"锁定"));
            actions.get(4).active=surveying||opened&&realm>=4&&!channel&&power>=8;actions.get(4).setMessage(Component.literal(surveying?"收回":"开启"));
            line(g,"识海",12,12,CultivationUi.GOLD);
            if(opened)g.drawString(font,"已开辟",width-52,14,CultivationUi.CYAN,false);
            int cap=n.getInt("Capacity");float ratio=cap==0?0:Math.max(0,Math.min(1,power/cap));
            line(g,"神识 "+Math.round(power)+" / "+cap+" · 探查半径 "+n.getInt("Radius")+"格",12,35,CultivationUi.CYAN);
            g.fill(12,50,width-12,53,CultivationUi.PANEL);g.fill(12,50,12+(int)((width-24)*ratio),53,CultivationUi.CYAN);
            int columns=width<300?1:2,gap=8,cardWidth=(width-24-gap*(columns-1))/columns;
            String[] titles={"神识探查","凝神辨识","神识锁定","矿物 / 结构"};String[] icons={"scan","identify","lock","survey"};
            String[] descriptions={"感知周围生灵","聚焦眼前目标","追踪可见目标","化神期 · 8×8区块"};
            String[] costs={n.getInt("ScanCooldown")>0?"冷却 "+(int)Math.ceil(n.getInt("ScanCooldown")/20d)+"秒":"8点 · 冷却4秒","6点 · 凝神2秒","4点 · 维持1点/秒","8点 · 维持1点/秒"};
            for(int i=0;i<4;i++){int x=12+(i%columns)*(cardWidth+gap),y=64+(i/columns)*70;
                SenseFurnaceArt.frame(g,"panel",x,y,cardWidth,64);SenseFurnaceArt.icon(g,icons[i],x+7,y+9,27);
                g.drawString(font,titles[i],x+39,y+10,CultivationUi.GOLD,false);
                g.drawString(font,descriptions[i],x+39,y+25,CultivationUi.MUTED,false);
                g.drawString(font,font.plainSubstrByWidth(costs[i],cardWidth-62),x+7,y+46,CultivationUi.CYAN,false);
            }
            int bottom=64+((4+columns-1)/columns)*70+5;
            SenseFurnaceArt.frame(g,"panel",12,bottom,width-24,27);
            String status=channel?"凝神中 · 剩余 "+(int)Math.ceil(n.getInt("Remaining")/20d)+"秒":locked?"锁定："+n.getString("Lock"):"未锁定目标";
            g.drawString(font,font.plainSubstrByWidth(status,(width-38)/2),20,bottom+9,CultivationUi.GOLD,false);
            String survey=surveying?"矿物探查中":"矿物探查未开启";g.drawString(font,survey,width-20-font.width(survey),bottom+9,CultivationUi.MUTED,false);
            String notice=n.getString("Notice");if(notice.isEmpty())notice="受击或移动将中断凝神 · 详情悬停技能按键";
            g.drawWordWrap(font,Component.literal(notice),12,bottom+35,width-24,CultivationUi.MUTED);
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
