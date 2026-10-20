package cn.blockforge.generated.generatedmod.client.weaponflame;

import java.util.function.Consumer;

/** Distance-based sprite layout, independent of frame rate and mesh subdivision. */
public final class WeaponFlameModules {
    public enum Module {
        MAIN("main_arc",2172,724,95,84,2078,690),
        TRANSITION("transition_arc",1774,887,146,232,1628,675),
        TAIL("tail_fragment",1585,992,593,388,992,644),
        SCATTER("scatter_particle",1484,1060,375,225,1186,831);
        public final String texture;
        public final float u0,v0,u1,v1,aspect;
        Module(String texture,int width,int height,int left,int top,int right,int bottom) {
            this.texture=texture;u0=left/(float)width;v0=top/(float)height;
            u1=right/(float)width;v1=bottom/(float)height;aspect=(right-left)/(float)(bottom-top);
        }
    }
    public record Slice(Module module,double start,double end,float u0,float u1,float width) {}
    private static final Module[] ORDER={Module.MAIN,Module.TRANSITION,Module.TAIL};
    private static final float[] WIDTHS={1F,.68F,.40F};
    private static final double GAP=.22;
    private static final double CYCLE=Module.MAIN.aspect+Module.TRANSITION.aspect*.68F+Module.TAIL.aspect*.40F+GAP*3;

    /** Coordinates are measured in band-width units. Gaps emit no geometry. */
    public static void slices(double from,double to,boolean fading,Consumer<Slice> emit) {
        if(!Double.isFinite(from)||!Double.isFinite(to)||to<=from)return;
        double period=fading?Module.TAIL.aspect*.48F+GAP*2:CYCLE;
        long first=(long)Math.floor(from/period),last=(long)Math.floor(to/period);
        // A teleported or corrupt path cannot allocate unbounded geometry.
        if(Math.abs(from)>1e9||Math.abs(to)>1e9||last-first>32)return;
        for(long cycle=first;cycle<=last;cycle++) {
            double start=cycle*period;
            int count=fading?1:ORDER.length;
            for(int i=0;i<count;i++) {
                Module module=fading?Module.TAIL:ORDER[i];
                float width=fading?.48F:WIDTHS[i];
                double length=module.aspect*width,end=start+length;
                double a=Math.max(from,start),b=Math.min(to,end);
                if(b>a+1e-8)emit.accept(new Slice(module,a,b,(float)((a-start)/length),(float)((b-start)/length),width));
                start=end+GAP;
            }
        }
    }
    private WeaponFlameModules() {}
}
