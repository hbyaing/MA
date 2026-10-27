package cn.blockforge.generated.generatedmod.client;

/** Physical framebuffer to logical GUI canvas. No rendering/input transforms or option writes. */
public final class ScreenFitPolicy {
    public static double scale(int pixelsWide,int pixelsHigh,int configured,int requiredWide,int requiredHigh,boolean unicode) {
        double maximum=Math.min(Math.max(1,configured),Math.min(Math.max(1,pixelsWide)/(double)Math.max(1,requiredWide),
                Math.max(1,pixelsHigh)/(double)Math.max(1,requiredHigh)));
        if(maximum>=1){
            int integer=(int)Math.floor(maximum);
            if(unicode&&integer>1&&(integer&1)!=0)integer--;
            return integer;
        }
        // Very small windows still contain the whole UI; vanilla uses the same canvas for mouse and rendering.
        return maximum;
    }
    private ScreenFitPolicy(){}
}
