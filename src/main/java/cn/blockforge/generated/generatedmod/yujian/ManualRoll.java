package cn.blockforge.generated.generatedmod.yujian;

/** 连续累计弧度及弧度/刻。松键保持角度；主动回正在八刻内平滑结束。 */
public final class ManualRoll {
    public enum ReleaseMode { HOLD_ANGLE, RECENTER }
    public double angle, velocity;
    private int previousInput, holdTicks, recoveryTicks;
    private double upright, recoveryStart;
    private boolean recovery, tapRelease;
    public void tick(int input, double limit, double acceleration, double start, double damping) {
        tick(input, limit, acceleration, start, damping, 1, ReleaseMode.HOLD_ANGLE, false);
    }
    public void tick(int input, double limit, double acceleration, double start, double damping,
                     double sensitivity, ReleaseMode releaseMode, boolean recenter) {
        if (recenter) beginRecovery();
        if (input != 0 && !recenter) {
            recovery = false;
            if (previousInput != input) holdTicks = 0;
            holdTicks++;
            double intent = smooth((holdTicks - 4) / 5.0);
            double nudge = Math.toRadians(32) / 20 * Math.min(1.2, sensitivity);
            double accel = acceleration * sensitivity;
            // 目标速度连续抬升，任何情况下均不能钳断已有速度，反向必须经过零点。
            double target = input * (nudge + (limit - nudge) * intent);
            double rate = Math.signum(velocity) == -input ? accel * 1.8 : accel;
            velocity = approach(velocity, target, rate);
            tapRelease = holdTicks <= 4;
            previousInput = input;
            angle += velocity;
            return;
        }
        previousInput = 0; holdTicks = 0;
        if (recovery) {
            recoveryTicks++;
            double next = recoveryStart + (upright - recoveryStart) * smooth(recoveryTicks / 8.0);
            velocity = next - angle; angle = next;
            if (recoveryTicks >= 8) { angle = upright; velocity = 0; recovery = false; }
            return;
        }
        velocity *= tapRelease ? 0.45 : damping;
        if (Math.abs(velocity) < Math.toRadians(.2) / 20) velocity = 0;
        angle += velocity;
        if (releaseMode == ReleaseMode.RECENTER && Math.abs(velocity) < Math.toRadians(35) / 20
                && Math.abs(angle - Math.rint(angle / (2 * Math.PI)) * 2 * Math.PI) > 0.0001) beginRecovery();
    }
    private void beginRecovery() {
        recovery = true; recoveryTicks = 0; recoveryStart = angle;
        upright = Math.rint(angle / (2 * Math.PI)) * 2 * Math.PI;
    }
    public boolean recovering() { return recovery; }
    public int holdTicks() { return holdTicks; }
    public void halt(double value) { angle=value; velocity=0; recovery=false; holdTicks=0; previousInput=0; }
    public void settleUpright() {
        double nearest=Math.rint(angle/(2*Math.PI))*2*Math.PI;
        halt(angle+(nearest-angle)*0.2);
    }
    private static double smooth(double t) { t=Math.max(0,Math.min(1,t)); return t*t*(3-2*t); }
    private static double approach(double value,double target,double amount) {
        return value + Math.max(-amount,Math.min(amount,target-value));
    }
}
