/* COMPILE-TIME STUB — see software/bernie/geckolib/package-info.java */
package software.bernie.geckolib.core.animation;

import java.util.ArrayList;
import java.util.List;

public final class RawAnimation {
    private final List<Object> stages = new ArrayList<>();

    public RawAnimation(List<Object> stages) {
        if (stages != null) this.stages.addAll(stages);
    }

    public static RawAnimation begin() {
        return new RawAnimation(null);
    }

    public RawAnimation thenPlay(String animation) { return this; }

    public RawAnimation thenLoop(String animation) { return this; }

    public RawAnimation thenWait(int ticks) { return this; }

    public RawAnimation thenPlayAndHold(String animation) { return this; }

    public RawAnimation thenPlayXTimes(String animation, int times) { return this; }

    public int getStageCount() { return stages.size(); }
}
