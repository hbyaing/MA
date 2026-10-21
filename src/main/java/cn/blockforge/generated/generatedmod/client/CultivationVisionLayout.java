package cn.blockforge.generated.generatedmod.client;

/** GUI units stay native-sized; narrow windows stack cards instead of shrinking text. */
public record CultivationVisionLayout(int width, int height, int paperWidth,
        Box status, Box selections, Box foundation, Box roots, Box actions, Box effects) {
    public record Box(int x, int y, int w, int h) {
        public boolean contains(double px, double py) { return px >= x && py >= y && px < x+w && py < y+h; }
    }
    public static CultivationVisionLayout of(int width, int viewportHeight) {
        if (width >= 384) {
            int buff = Math.max(108, Math.min(144, width / 4));
            int paper = width - buff;
            int gap = 0;
            int col = (paper - gap) * 53 / 100;
            int right = col + gap;
            int h = Math.max(248, viewportHeight);
            int extra = h - 248;
            return new CultivationVisionLayout(width, h, paper,
                    new Box(0,0,col,188+extra), new Box(right,104+extra,paper-right,100),
                    new Box(right,0,paper-right,104+extra), new Box(0,188+extra,col,60),
                    new Box(right,204+extra,paper-right,44), new Box(paper,0,buff,h));
        }
        return new CultivationVisionLayout(width, Math.max(732,viewportHeight), width,
                new Box(0,0,width,200), new Box(0,384,width,100),
                new Box(0,270,width,114), new Box(0,200,width,70),
                new Box(0,484,width,44), new Box(0,528,width,204));
    }

    public Box domain() { return new Box(selections.x(),selections.y(),selections.w(),50); }
    public Box fire() { return new Box(selections.x(),selections.y()+50,selections.w(),50); }
}
