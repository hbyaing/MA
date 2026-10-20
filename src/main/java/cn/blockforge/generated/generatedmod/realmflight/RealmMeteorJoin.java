package cn.blockforge.generated.generatedmod.realmflight;

import cn.blockforge.generated.generatedmod.realmflight.RealmMeteorPath.Frame;
import cn.blockforge.generated.generatedmod.realmflight.RealmMeteorPath.Vec;
import java.util.ArrayList;
import java.util.List;

/** Visual tube joins derived only from the rendered world-space centerline. */
public final class RealmMeteorJoin {
    public static final int MAX_HEAD_FRAMES = 20;
    public static final int MAX_JOINED_FRAMES = RealmMeteorPath.MAX_ADAPTIVE_FRAMES + MAX_HEAD_FRAMES;
    public static final double HEADING_JOIN_LENGTH = 3;
    private static final double EPSILON = 1e-9;

    /**
     * Limits a tube on a fixed world-distance support, independent of adaptive ring spacing.
     * A render partial near zero can leave a tiny final chord; that chord is not a tighter turn.
     * At a path endpoint the complete probe window moves inward, rather than shrinking to it.
     */
    public static double stableRadiusLimit(List<Frame> frames, int index, double requestedWidth) {
        return new RadiusSupport(frames).limit(index, requestedWidth);
    }

    /** Validates one immutable rendered path once; every radius query then uses binary arc probes. */
    public static final class RadiusSupport {
        private final List<Frame> frames;
        private final boolean valid;

        public RadiusSupport(List<Frame> frames) {
            this.frames = frames;
            valid = frames != null && !frames.isEmpty() && validCenterline(frames);
        }

        public double limit(int index, double requestedWidth) {
            if (!valid || !Double.isFinite(requestedWidth) || requestedWidth <= 0
                    || index < 0 || index >= frames.size()) return 0;
            if (frames.size() < 3) return requestedWidth;
            double first = frames.get(0).arc(), last = frames.get(frames.size() - 1).arc();
            double support = Math.min(Math.max(.5, Math.min(1, requestedWidth * .6)), (last - first) * .5);
            if (support <= EPSILON) return requestedWidth;
            double center = Math.max(first + support, Math.min(last - support, frames.get(index).arc()));
            Vec middle = atArc(frames, center);
            Vec incoming = middle.subtract(atArc(frames, center - support));
            Vec outgoing = atArc(frames, center + support).subtract(middle);
            double left = incoming.length(), right = outgoing.length();
            if (left <= EPSILON || right <= EPSILON) return 0;
            double cosine = Math.max(-1, Math.min(1, incoming.dot(outgoing) / (left * right)));
            if (cosine > 1 - 1e-10) return requestedWidth;
            // The real turnaround remains a pinch; a wide tube must not fold across the cusp.
            if (cosine < -1 + 1e-10) return 0;
            double cross = incoming.cross(outgoing).length();
            double chord = incoming.add(outgoing).length();
            double radius = left * right * chord / (2 * cross);
            return Double.isFinite(radius) ? Math.min(requestedWidth, .45 * radius) : 0;
        }
    }

    /**
     * Continues the front skin directly from the actual last rendered frame, retaining its
     * complete basis so every radial phase meets exactly.
     * The extra frames are a visual attachment, with unchanged history time and opacity.
     */
    public static List<Frame> attachHead(List<Frame> frames, double nose) {
        return attachHead(frames, nose, null);
    }

    /**
     * Spreads the smoothed flight heading over the final three world blocks. Every original
     * side follows its own tangent by the minimal rotation, retaining the historical radial
     * phase instead of snapping just the terminal ring to an unrelated orientation.
     */
    public static List<Frame> attachHead(List<Frame> frames, double nose, Vec desiredHeading) {
        if (frames == null || frames.isEmpty() || frames.size() > RealmMeteorPath.MAX_ADAPTIVE_FRAMES
                || !validCenterline(frames)) return List.of();
        for (Frame frame : frames) if (!validBasis(frame)) return List.of();
        if (!Double.isFinite(nose) || nose <= EPSILON) return List.copyOf(frames);
        Frame body = frames.get(frames.size() - 1);
        var joined = new ArrayList<Frame>(frames.size() + MAX_HEAD_FRAMES);
        boolean orient = desiredHeading != null && desiredHeading.finite()
                && desiredHeading.lengthSquared() > EPSILON * EPSILON;
        Vec heading = orient ? desiredHeading.normalize() : body.tangent();
        for (Frame frame : frames) {
            double behind = body.arc() - frame.arc();
            if (!orient || behind >= HEADING_JOIN_LENGTH) { joined.add(frame); continue; }
            double weight = 1 - Math.max(0, behind) / HEADING_JOIN_LENGTH;
            weight = weight * weight * (3 - 2 * weight);
            Vec tangent = directionLerp(frame.tangent(), heading, frame.up(), weight);
            Vec side = transport(frame.side(), frame.tangent(), tangent);
            joined.add(new Frame(frame.position(), tangent, side, tangent.cross(side).normalize(),
                    frame.tick(), frame.arc(), frame.opacity()));
        }
        body = joined.get(joined.size() - 1);
        for (int step = 1; step <= MAX_HEAD_FRAMES; step++) {
            double forward = nose * Math.sin(step * Math.PI / (2 * MAX_HEAD_FRAMES));
            joined.add(new Frame(body.position().add(body.tangent().scale(forward)),
                    body.tangent(), body.side(), body.up(), body.tick(), body.arc() + forward, body.opacity()));
        }
        return List.copyOf(joined);
    }

    private static boolean validCenterline(List<Frame> frames) {
        double previous = -Double.MAX_VALUE;
        Frame before = null;
        for (Frame frame : frames) {
            if (frame == null || frame.position() == null || !frame.position().finite()
                    || !Double.isFinite(frame.arc()) || !Double.isFinite(frame.tick())
                    || !Double.isFinite(frame.opacity()) || frame.arc() < previous
                    || before != null && frame.arc() == previous
                    && frame.position().subtract(before.position()).lengthSquared() > EPSILON * EPSILON) return false;
            previous = frame.arc();
            before = frame;
        }
        return true;
    }

    private static boolean validBasis(Frame frame) {
        return frame.tangent() != null && frame.side() != null && frame.up() != null
                && frame.tangent().finite() && frame.side().finite() && frame.up().finite()
                && Math.abs(frame.tangent().lengthSquared() - 1) < 1e-6
                && Math.abs(frame.side().lengthSquared() - 1) < 1e-6
                && Math.abs(frame.up().lengthSquared() - 1) < 1e-6
                && Math.abs(frame.tangent().dot(frame.side())) < 1e-6
                && Math.abs(frame.tangent().dot(frame.up())) < 1e-6
                && Math.abs(frame.side().dot(frame.up())) < 1e-6;
    }

    private static Vec atArc(List<Frame> frames, double arc) {
        if (arc <= frames.get(0).arc()) return frames.get(0).position();
        if (arc >= frames.get(frames.size() - 1).arc()) return frames.get(frames.size() - 1).position();
        int low = 0, high = frames.size() - 1;
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (frames.get(middle).arc() <= arc) low = middle + 1;
            else high = middle;
        }
        Frame after = frames.get(low), before = frames.get(low - 1);
        double fraction = Math.max(0, Math.min(1, (arc - before.arc()) / (after.arc() - before.arc())));
        return before.position().add(after.position().subtract(before.position()).scale(fraction));
    }

    private static Vec directionLerp(Vec from, Vec to, Vec up, double weight) {
        if (weight >= 1) return to;
        if (weight <= 0) return from;
        double cosine = Math.max(-1, Math.min(1, from.dot(to)));
        if (cosine > 1 - 1e-10) return from.add(to.subtract(from).scale(weight)).normalize();
        double angle = Math.acos(cosine);
        Vec transverse = to.subtract(from.scale(cosine));
        transverse = transverse.lengthSquared() > EPSILON * EPSILON ? transverse.normalize() : up;
        return from.scale(Math.cos(angle * weight)).add(transverse.scale(Math.sin(angle * weight))).normalize();
    }

    private static Vec transport(Vec side, Vec from, Vec to) {
        Vec rotation = from.cross(to);
        double sine = rotation.length(), cosine = Math.max(-1, Math.min(1, from.dot(to)));
        if (sine > EPSILON) {
            Vec axis = rotation.scale(1 / sine);
            side = side.scale(cosine).add(axis.cross(side).scale(sine))
                    .add(axis.scale(axis.dot(side) * (1 - cosine)));
        }
        // For an exact reversal the original side is a valid, phase-preserving rotation axis.
        return side.subtract(to.scale(side.dot(to))).normalize();
    }

    private RealmMeteorJoin() {}
}
