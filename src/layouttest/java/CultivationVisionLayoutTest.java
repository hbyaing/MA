import cn.blockforge.generated.generatedmod.client.CultivationVisionLayout;
import cn.blockforge.generated.generatedmod.client.CultivationVisionLayout.Box;

/** Panel geometry and readable native-size controls across both responsive modes. */
public final class CultivationVisionLayoutTest {
    public static void main(String[] args) {
        int cases=0;
        for(int width:new int[]{204,230,272,300,340,360,383,384,388,408,512,640,960})
            for(int viewport:new int[]{160,210,240,258,270,300,450}) {
                var l=CultivationVisionLayout.of(width,viewport);
                Box[] boxes={l.status(),l.selections(),l.foundation(),l.roots(),l.actions(),l.effects()};
                for(int i=0;i<boxes.length;i++) {
                    var b=boxes[i];
                    check(b.x()>=0&&b.y()>=0&&b.x()+b.w()<=width&&b.y()+b.h()<=l.height(),"card exceeds page");
                    for(int j=i+1;j<boxes.length;j++)check(!overlaps(b,boxes[j]),"cards overlap");
                }
                check(l.height()>=viewport,"scroll range drops bottom controls");
                check(l.effects().w()>=108&&l.effects().h()-28>=152,"effect list too small for native icons");
                check((l.roots().w()-24)/5>=21,"root grade cannot fit two Chinese glyphs beside switch");
                check(l.selections().w()-10>=78,"fire selector cannot fit three native item slots");
                if(width>=384){
                    check(l.height()==Math.max(248,viewport)&&l.effects().y()==0,"desktop view unexpectedly needs vertical scroll");
                    check(l.effects().x()==l.paperWidth()&&l.effects().x()+l.effects().w()==width,"page and effect sidebar must share an edge");
                }
                check(!overlaps(l.domain(),l.fire()),"selector cards overlap");
                check(l.domain().y()+38<=l.domain().y()+l.domain().h()&&l.fire().y()+38<=l.fire().y()+l.fire().h(),"selector control escapes card");
                cases++;
            }
        System.out.println("PASS: "+cases+" vision layout cases");
    }
    private static boolean overlaps(Box a,Box b){return a.x()<b.x()+b.w()&&b.x()<a.x()+a.w()&&a.y()<b.y()+b.h()&&b.y()<a.y()+a.h();}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
