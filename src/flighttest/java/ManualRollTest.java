import cn.blockforge.generated.generatedmod.yujian.ManualRoll;

public final class ManualRollTest {
    public static void main(String[] args) {
        for(int turns:new int[]{-30,-8,0,8,30}) {
            var roll=new ManualRoll();
            roll.angle=Math.toRadians(turns*360+140);
            double before=roll.angle, target=Math.toRadians(turns*360);
            roll.settleUpright();
            if(Math.abs(roll.angle-before)>Math.toRadians(36)+1e-8)
                throw new AssertionError("Hover must not unwind accumulated turns");
            for(int i=0;i<100;i++)roll.settleUpright();
            if(Math.abs(roll.angle-target)>1e-7)throw new AssertionError("Nearest upright not reached");
        }
        System.out.println("ManualRollTest PASS: nearest upright after positive/negative accumulated turns");
    }
}
