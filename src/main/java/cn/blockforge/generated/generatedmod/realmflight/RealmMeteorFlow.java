package cn.blockforge.generated.generatedmod.realmflight;

import java.util.List;

/** Bounded cross-sectional flow in the path's transported frame. All distances are world blocks. */
public final class RealmMeteorFlow {
    private static final double SHARED_REFERENCE_LENGTH = 28;

    public record Bend(double side, double up) {
        public double magnitude() { return Math.min(1, Math.hypot(side, up)); }
    }

    /** Coordinates and normals relative to one path frame; no second history or camera-facing plane. */
    public record Section(double side, double up, double normalSide, double normalUp) {}

    /** All rank-specific changes begin four blocks behind the body, with zero first derivative there. */
    public static double detailWeight(double behind) { return RealmMeteorTuning.smooth((behind - 4) / 6); }

    /**
     * A common nose/body envelope meets the rank's historical taper over blocks 4..8.
     * The former radius interpolation and rank-specific tail length changed even the first four blocks.
     */
    public static double width(double behind, double length, double baseRadius, double rankRadius) {
        if (!Double.isFinite(behind) || !Double.isFinite(length) || !Double.isFinite(baseRadius)
                || !Double.isFinite(rankRadius) || length <= 0 || baseRadius <= 0 || rankRadius <= 0) return 0;
        if (behind < 0) return RealmMeteorTuning.wrapWidth(behind, SHARED_REFERENCE_LENGTH, baseRadius);
        if (behind >= length) return 0;
        double common = RealmMeteorTuning.width(behind, SHARED_REFERENCE_LENGTH, baseRadius);
        double ranked = RealmMeteorTuning.width(behind, length, rankRadius);
        double transition = RealmMeteorTuning.smooth((behind - 4) / 4);
        double width = common + (ranked - common) * transition;
        // During initial tail growth a short terminal must also close smoothly.
        if (length < 8) width *= RealmMeteorTuning.smooth((length - behind) / Math.min(2, length));
        return Math.max(0, width);
    }

    public static double remaining(double behind, double length) {
        if (!Double.isFinite(behind) || !Double.isFinite(length) || length <= 0 || behind >= length) return 0;
        double common = 1 - RealmMeteorTuning.unit(behind / SHARED_REFERENCE_LENGTH);
        double ranked = 1 - RealmMeteorTuning.unit(behind / length);
        return common + (ranked - common) * RealmMeteorTuning.smooth((behind - 4) / 4);
    }

    /** Curvature points into the turn; its side/up projection is independent of the observer. */
    public static Bend bend(List<RealmMeteorPath.Frame> path, int index) {
        if (path == null || path.size() < 2 || index < 0 || index >= path.size()) return new Bend(0, 0);
        int left = Math.max(0, index - 1), right = Math.min(path.size() - 1, index + 1);
        var before = path.get(left); var after = path.get(right); var frame = path.get(index);
        double span = after.arc() - before.arc();
        if (!Double.isFinite(span) || span <= 1e-6) return new Bend(0, 0);
        var curvature = after.tangent().subtract(before.tangent()).scale(3 / span);
        double side=signedUnit(curvature.dot(frame.side())), up=signedUnit(curvature.dot(frame.up()));
        double magnitude=Math.max(1,Math.hypot(side,up));
        return new Bend(side/magnitude,up/magnitude);
    }

    /** Conservative section bound, blended in after the common body region. */
    public static double sectionBound(int style, double behind) {
        double extra = style <= 1 ? 0 : style == 2 ? .28 : style == 3 ? .32 : style == 4 ? .60 : .75;
        return 1 + extra * detailWeight(behind);
    }

    /**
     * Inner-roll, packet-merge, and compound-curl sections share a single central path.
     * Deformations are local packets, rather than independent full-length orbiting ribbons.
     */
    public static Section section(int style, int layer, double behind, double arc, double flowPhase,
                                  double angle, double bendSide, double bendUp) {
        double c = Math.cos(angle), s = Math.sin(angle), gate = detailWeight(behind);
        if (style <= 1 || gate <= 0) return new Section(c, s, c, s);
        double bendLength=Math.max(1,Math.hypot(bendSide,bendUp));
        bendSide/=bendLength;bendUp/=bendLength;
        double inner = layer == 2 ? .45 : layer == 1 ? 1 : .70;
        double turn = Math.min(1, Math.hypot(bendSide, bendUp));
        double rotation, scaleSide, scaleUp, lobe, shiftSide, shiftUp;
        if (style == 2) {
            // Compact, alternating inward folds exchange axes as short roll packets pass.
            double roll = arc * .90 + flowPhase * 3.3;
            double packet = packet(arc * .63 + flowPhase * 2.1);
            rotation = gate * inner * .45 * Math.sin(roll * .52);
            scaleSide = 1 + gate * inner * (.10 * Math.sin(roll) - .07 * packet);
            scaleUp = 1 + gate * inner * (-.10 * Math.sin(roll) - .07 * packet);
            lobe = 1 + gate * inner * .065 * Math.sin(2 * angle + roll) * packet;
            shiftSide = gate * inner * .055 * Math.cos(roll * .61) * packet;
            shiftUp = gate * inner * .055 * Math.sin(roll * .61) * packet;
        } else {
            // Two packet velocities periodically overtake and merge, producing broad/narrow folds.
            double slow = arc * .56 + flowPhase * 1.8, fast = arc * .82 + flowPhase * 3.7;
            double merge = packet(slow) * packet(fast);
            double wave = .65 * Math.sin(slow) + .35 * Math.sin(fast);
            rotation = gate * inner * .20 * Math.sin(slow * .59);
            scaleSide = 1 + gate * inner * (.09 * wave + .07 * merge);
            scaleUp = 1 + gate * inner * (-.055 * wave + .055 * merge);
            lobe = 1 + gate * inner * .045 * Math.sin(3 * angle + fast) * (.4 + .6 * merge);
            shiftSide = gate * inner * .035 * Math.sin(slow * .72);
            shiftUp = gate * inner * .035 * Math.cos(slow * .72);
            if (style >= 5) {
                // Large low-frequency curl, medium folds, and fine material streams coexist.
                double curl = arc * .29 + flowPhase * .93;
                rotation += gate * inner * .34 * Math.sin(curl);
                scaleSide += gate * inner * .055 * Math.sin(curl * .77);
                scaleUp -= gate * inner * .055 * Math.sin(curl * .77);
                lobe += gate * inner * .035 * Math.sin(2 * angle - curl);
                shiftSide += gate * inner * .055 * Math.cos(curl) * packet(curl * .73);
                shiftUp += gate * inner * .055 * Math.sin(curl) * packet(curl * .73);
            }
        }
        double cr = Math.cos(rotation), sr = Math.sin(rotation);
        double x = (c * scaleSide * cr - s * scaleUp * sr) * lobe;
        double y = (c * scaleSide * sr + s * scaleUp * cr) * lobe;
        if (style >= 4 && turn > 1e-6) {
            // Expand the outside and contract the inside of the recorded bend.
            double outside = -(c * bendSide + s * bendUp);
            double turnStretch = 1 + gate * .15 * outside;
            x = x * turnStretch - gate * .085 * bendSide;
            y = y * turnStretch - gate * .085 * bendUp;
        }
        double nx = c / scaleSide * cr - s / scaleUp * sr;
        double ny = c / scaleSide * sr + s / scaleUp * cr;
        double normalLength = Math.max(1e-6, Math.hypot(nx, ny));
        return new Section(x + shiftSide, y + shiftUp, nx / normalLength, ny / normalLength);
    }

    /** Brief residual haze lives on an old curved path, never in front of the current heading. */
    public static double residualWeight(int style, double behind, double ageTicks, double bendSide, double bendUp) {
        if (style < 4 || !Double.isFinite(ageTicks) || ageTicks <= 3 || ageTicks >= 20) return 0;
        double turn = RealmMeteorTuning.smooth(Math.hypot(bendSide, bendUp) / .32);
        double age = RealmMeteorTuning.smooth((ageTicks - 3) / 3)
                * (1 - RealmMeteorTuning.smooth((ageTicks - 10) / 10));
        return detailWeight(behind) * turn * age * (style == 5 ? .13 : .11);
    }

    public static Section residualSection(double angle, double bendSide, double bendUp) {
        double bendLength=Math.max(1,Math.hypot(bendSide,bendUp));
        bendSide/=bendLength;bendUp/=bendLength;
        double c = Math.cos(angle), s = Math.sin(angle);
        double outside = Math.max(0, -(c * bendSide + s * bendUp));
        double spread = 1.13 + .22 * outside;
        return new Section(c * spread - bendSide * .15, s * spread - bendUp * .15, c, s);
    }

    /** Signed-short vertex metadata; both OpenGL integer attributes remain within their exact range. */
    public static int packBehind(double behind) { return (int)Math.round(clamp((behind + 4) * 256, 0, 32767)); }
    public static int packAge(double ageTicks) { return (int)Math.round(clamp(ageTicks * 256, 0, 32767)); }
    public static int packBend(double bend) { return (int)Math.round(signedUnit(bend) * 16384); }

    private static double packet(double phase) { double value = .5 + .5 * Math.sin(phase); return value * value; }
    private static double signedUnit(double value) { return Double.isFinite(value) ? clamp(value, -1, 1) : 0; }
    private static double clamp(double value, double low, double high) { return Math.max(low, Math.min(high, value)); }
    private RealmMeteorFlow() {}
}
