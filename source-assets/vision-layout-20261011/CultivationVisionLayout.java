package cn.blockforge.generated.generatedmod.client;

/** GUI units stay native-sized; narrow windows stack cards instead of shrinking text. */
public record CultivationVisionLayout(int width, int height, int paperWidth,
        Box status, Box selections, Box foundation, Box roots, Box actions, Box effects) {
    public record Box(int x, int y, int w, int h) {
        public boolean contains(double px, double py) { return px >= x && py >= y && px < x+w && py < y+h; }
    }
    public static CultivationVisionLayout of(int width, int viewportHeight) {
        if (width >= 384) {
            int buff = Math.max(104, Math.min(112, width / 4));
            int paper = width - buff + 4;
            int gap = 6;
            int col = (paper - gap) * 53 / 100;
            int right = col + gap;
            int h = Math.max(258, viewportHeight);
            return new CultivationVisionLayout(width, h, paper,
                    new Box(0,0,col,166), new Box(right,102,paper-right,126),
                    new Box(right,0,paper-right,92), new Box(0,176,col,72),
                    new Box(right,182,paper-right,46), new Box(paper,0,width-paper+5,h));
        }
        return new CultivationVisionLayout(width, Math.max(708,viewportHeight), width,
                new Box(0,0,width,166), new Box(0,346,width,126),
                new Box(0,172,width,92), new Box(0,270,width,72),
                new Box(0,426,width,46), new Box(0,478,width,180));
    }
}
