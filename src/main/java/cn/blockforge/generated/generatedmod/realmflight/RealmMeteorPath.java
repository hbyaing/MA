package cn.blockforge.generated.generatedmod.realmflight;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** World-space flight history. No Minecraft, camera, renderer, or frame-rate dependency. */
public final class RealmMeteorPath {
    public static final int MAX_SAMPLES = 64;
    public static final int MAX_ADAPTIVE_FRAMES = (MAX_SAMPLES - 1) * 6 + 1;
    public static final double HISTORY_TICKS = 64, STOP_FADE_TICKS = 18;
    private static final double TELEPORT_DISTANCE = 4, MIN_MOVEMENT = .015, EPSILON = 1e-9;

    public record Vec(double x, double y, double z) {
        public static final Vec ZERO = new Vec(0, 0, 0);
        public Vec add(Vec other) { return new Vec(x + other.x, y + other.y, z + other.z); }
        public Vec subtract(Vec other) { return new Vec(x - other.x, y - other.y, z - other.z); }
        public Vec scale(double value) { return new Vec(x * value, y * value, z * value); }
        public double dot(Vec other) { return x * other.x + y * other.y + z * other.z; }
        public Vec cross(Vec other) { return new Vec(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x); }
        public double lengthSquared() { return dot(this); }
        public double length() { return Math.sqrt(lengthSquared()); }
        public boolean finite() { return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z); }
        public Vec normalize() { double length = length(); return length > EPSILON ? scale(1 / length) : ZERO; }
    }

    /** Arc is measured along the smoothed world-space centerline; opacity includes stop fading. */
    public record Frame(Vec position, Vec tangent, Vec side, Vec up, double tick, double arc, double opacity) {}
    private record Sample(Vec position, double tick, double arc, Vec tangent, Vec side) {}
    private record Knot(Vec position, double tick, double arc) {}

    private final ArrayDeque<Sample> samples = new ArrayDeque<>();
    private Vec previous, direction = new Vec(0, 1, 0), transportedSide = new Vec(1, 0, 0);
    private double previousTick = Double.NaN, stoppedAt = Double.NaN, arc, speed;

    public void clear() {
        samples.clear(); previous = null; previousTick = Double.NaN; stoppedAt = Double.NaN;
        arc = speed = 0; direction = new Vec(0, 1, 0); transportedSide = new Vec(1, 0, 0);
    }
    public int sampleCount() { return samples.size(); }
    public double speed() { return speed; }
    public Vec direction() { return direction; }

    /** Called once per simulation tick with the player's current, un-interpolated center. */
    public void sample(Vec position, double tick, boolean active) {
        if (position == null || !position.finite() || !Double.isFinite(tick)) { clear(); return; }
        if (Double.isFinite(previousTick) && tick < previousTick) clear();
        if (tick == previousTick) return;
        Vec delta = previous == null ? Vec.ZERO : position.subtract(previous);
        double distance = delta.length(), elapsed = Double.isFinite(previousTick) ? Math.max(1, tick - previousTick) : 1;
        if (distance > TELEPORT_DISTANCE || elapsed > HISTORY_TICKS) {
            clear(); delta = Vec.ZERO; distance = 0; elapsed = 1;
        }
        boolean moving = active && distance > MIN_MOVEMENT;
        speed += ((moving ? distance / elapsed : 0) - speed) * .35;
        while (!samples.isEmpty() && tick - samples.peekFirst().tick() > HISTORY_TICKS) samples.removeFirst();
        if (moving) stoppedAt = Double.NaN;
        else if (!Double.isFinite(stoppedAt)) stoppedAt = tick;
        if (Double.isFinite(stoppedAt) && tick - stoppedAt >= STOP_FADE_TICKS) samples.clear();
        if (active) {
            if (moving) {
                Vec next = delta.normalize();
                transportedSide = transport(transportedSide, direction, next); direction = next;
            }
            Sample last = samples.peekLast();
            if (last != null) arc += position.subtract(last.position()).length();
            samples.addLast(new Sample(position, tick, arc, direction, transportedSide));
            while (samples.size() > MAX_SAMPLES) samples.removeFirst();
        }
        previous = position; previousTick = tick;
    }

    /**
     * The caller uses clock - 1 + partialTick, the same time as entity interpolation.
     * Current-tick samples beyond that time are deliberately excluded, avoiding a backwards head segment.
     */
    public List<Frame> render(double renderTick, Vec interpolatedHead, int subdivisions, double maxLength) {
        return render(renderTick,interpolatedHead,subdivisions,maxLength,48);
    }
    public List<Frame> render(double renderTick, Vec interpolatedHead, int subdivisions, double maxLength,double maximumAge) {
        return renderInternal(renderTick, interpolatedHead, subdivisions, maxLength, maximumAge, -1);
    }

    /** Adaptive output geometry only: fixed-tick history and its transported anchor are untouched. */
    public List<Frame> renderAdaptive(double renderTick, Vec interpolatedHead, double maxLength, double maximumAge, int lod) {
        if (!Double.isFinite(maximumAge)) return List.of();
        return renderInternal(renderTick, interpolatedHead, 1, maxLength, maximumAge, Math.max(0, Math.min(2, lod)));
    }

    private List<Frame> renderInternal(double renderTick, Vec interpolatedHead, int subdivisions, double maxLength, double maximumAge, int adaptiveLod) {
        maximumAge=Math.max(1,Math.min(HISTORY_TICKS,maximumAge));
        if (!Double.isFinite(renderTick) || !Double.isFinite(maxLength) || maxLength <= 0 || samples.isEmpty()) return List.of();
        double stopOpacity = Double.isFinite(stoppedAt) ? clamp(1 - Math.max(0, renderTick - stoppedAt) / STOP_FADE_TICKS) : 1;
        if (stopOpacity <= 0) return List.of();
        int divisions = Math.max(1, Math.min(6, subdivisions));
        var raw = new ArrayList<Knot>(samples.size() + 1);
        Sample first = null, lastPast = null, firstFuture = null;
        for (Sample sample : samples) {
            if (sample.tick() > renderTick) { firstFuture = sample; break; }
            if (renderTick - sample.tick() > maximumAge) continue;
            if (first == null) first = sample;
            lastPast = sample;
            append(raw, new Knot(sample.position(), sample.tick(), sample.arc()));
        }
        if (lastPast == null) return List.of();
        // Add a head only when a current/future history sample brackets this render time.
        // A stopped path remains fixed in the world rather than following a walking player.
        if (firstFuture != null && interpolatedHead != null && interpolatedHead.finite()
                && interpolatedHead.subtract(lastPast.position()).length() <= TELEPORT_DISTANCE) {
            append(raw, new Knot(interpolatedHead, renderTick, lastPast.arc()));
        }
        if (raw.size() < 2) return List.of();
        var smooth = adaptiveLod < 0 ? smooth(raw, divisions) : smoothAdaptive(raw, adaptiveLod);
        if (smooth.size() < 2) return List.of();
        // Preserve the historical anchor, then derive UV distance from the actual smoothed geometry.
        double accumulated = raw.get(0).arc();
        for (int i = 0; i < smooth.size(); i++) {
            Knot knot = smooth.get(i);
            if (i > 0) accumulated += knot.position().subtract(smooth.get(i - 1).position()).length();
            smooth.set(i, new Knot(knot.position(), knot.tick(), accumulated));
        }
        double minimumArc = smooth.get(smooth.size() - 1).arc() - maxLength;
        var clipped = new ArrayList<Knot>(smooth.size());
        for (int i = 0; i < smooth.size(); i++) {
            Knot knot = smooth.get(i);
            if (knot.arc() < minimumArc) continue;
            if (clipped.isEmpty() && i > 0 && smooth.get(i - 1).arc() < minimumArc) {
                Knot before = smooth.get(i - 1);
                double fraction = (minimumArc - before.arc()) / (knot.arc() - before.arc());
                clipped.add(new Knot(lerp(before.position(), knot.position(), fraction), lerp(before.tick(), knot.tick(), fraction), minimumArc));
            }
            clipped.add(knot);
        }
        if (clipped.size() < 2) return List.of();
        var result = new ArrayList<Frame>(clipped.size());
        // Walk the retained smooth centerline before the length cut as well.
        // Merely projecting its oldest side onto the clipped start tangent would rotate the
        // strand phases whenever a bend passes the maximum-length boundary.
        // The anchor side belongs to its stored tangent. Transport it into the
        // smoothed tangent; projection alone can collapse a valid side and
        // reselect a world axis when the stationary takeoff seed expires.
        Vec tangent = tangent(smooth, 0), side = transport(first.side(), first.tangent(), tangent), previousTangent = tangent;
        double firstArc = clipped.get(0).arc();
        for (int i = 1; i < smooth.size() && smooth.get(i).arc() <= firstArc; i++) {
            tangent = tangent(smooth, i); side = transport(side, previousTangent, tangent); previousTangent = tangent;
        }
        tangent = tangent(clipped, 0); side = transport(side, previousTangent, tangent); previousTangent = tangent;
        for (int i = 0; i < clipped.size(); i++) {
            Knot knot = clipped.get(i); tangent = tangent(clipped, i);
            if (i > 0) side = transport(side, previousTangent, tangent);
            double age = clamp(1 - Math.max(0, renderTick - knot.tick()) / maximumAge);
            result.add(new Frame(knot.position(), tangent, side, tangent.cross(side).normalize(), knot.tick(), knot.arc(), age * stopOpacity));
            previousTangent = tangent;
        }
        return List.copyOf(result);
    }

    /**
     * Conservative local tube radius at a turn. A straight segment keeps its requested
     * width; a true reversal collapses at the cusp instead of folding the inner shell.
     * This does not promise to separate distinct historical paths that cross in space.
     */
    public static double radiusLimit(List<Frame> frames, int index, double requestedWidth) {
        if (!Double.isFinite(requestedWidth) || requestedWidth <= 0 || frames == null || index < 0 || index >= frames.size()) return 0;
        if (frames.size() < 3) return requestedWidth;
        int center = Math.max(1, Math.min(frames.size() - 2, index));
        Vec incoming = frames.get(center).position().subtract(frames.get(center - 1).position());
        Vec outgoing = frames.get(center + 1).position().subtract(frames.get(center).position());
        double left = incoming.length(), right = outgoing.length();
        if (!Double.isFinite(left) || !Double.isFinite(right) || left <= EPSILON || right <= EPSILON) return 0;
        double cosine = Math.max(-1, Math.min(1, incoming.dot(outgoing) / (left * right)));
        if (cosine > 1 - 1e-10) return requestedWidth;
        if (cosine < -1 + 1e-10) return 0;
        // R = min(chord lengths) / (2 tan(turn / 2)); 45% keeps the inner tube outside the cusp.
        double tangentHalfAngle = Math.sqrt((1 - cosine) / Math.max(1e-12, 1 + cosine));
        return Math.min(requestedWidth, .45 * Math.min(left, right) / (2 * tangentHalfAngle));
    }

    private static ArrayList<Knot> smoothAdaptive(List<Knot> raw, int lod) {
        var result = new ArrayList<Knot>(MAX_ADAPTIVE_FRAMES);
        double segmentLimit = lod == 0 ? .35 : lod == 1 ? .70 : 1.40;
        double angleLimit = Math.toRadians(lod == 0 ? 10 : lod == 1 ? 18 : 28);
        double errorLimit = lod == 0 ? .014 : lod == 1 ? .04 : .12;
        for (int i = 0; i < raw.size() - 1; i++) {
            Knot a = raw.get(Math.max(0, i - 1)), b = raw.get(i), c = raw.get(i + 1), d = raw.get(Math.min(raw.size() - 1, i + 2));
            Vec chord = c.position().subtract(b.position());
            double length = chord.length();
            Vec left = forwardTangent(c.position().subtract(a.position()).scale(.5), chord, length);
            Vec right = forwardTangent(d.position().subtract(b.position()).scale(.5), chord, length);
            // Opposing adjacent travel has a real cusp. Keep it at its tick position instead of overshooting it.
            if (i > 0 && b.position().subtract(a.position()).dot(chord) < 0) left = Vec.ZERO;
            if (i + 2 < raw.size() && d.position().subtract(c.position()).dot(chord) < 0) right = Vec.ZERO;
            double error = 0, angle = 0;
            Vec previousDirection = derivative(b.position(), c.position(), left, right, 0).normalize();
            for (int probe = 1; probe <= 4; probe++) {
                double t = probe / 4.0;
                Vec position = hermite(b.position(), c.position(), left, right, t);
                error = Math.max(error, position.subtract(lerp(b.position(), c.position(), t)).length());
                Vec nextDirection = derivative(b.position(), c.position(), left, right, t).normalize();
                if (previousDirection.lengthSquared() > EPSILON && nextDirection.lengthSquared() > EPSILON)
                    angle += Math.acos(Math.max(-1, Math.min(1, previousDirection.dot(nextDirection))));
                if (nextDirection.lengthSquared() > EPSILON) previousDirection = nextDirection;
            }
            int subdivisions = Math.max(1, Math.min(6, (int)Math.ceil(Math.max(length / segmentLimit,
                    Math.max(angle / angleLimit, Math.sqrt(error / errorLimit))))));
            for (int step = 0; step < subdivisions; step++) {
                double t = (double)step / subdivisions;
                append(result, new Knot(hermite(b.position(), c.position(), left, right, t), lerp(b.tick(), c.tick(), t), 0));
            }
        }
        append(result, raw.get(raw.size() - 1));
        return result;
    }
    private static Vec forwardTangent(Vec candidate, Vec chord, double length) {
        if (candidate.dot(chord) <= 0) return Vec.ZERO;
        return limit(candidate, length);
    }
    private static Vec hermite(Vec a, Vec b, Vec left, Vec right, double t) {
        double t2 = t * t, t3 = t2 * t;
        return a.scale(2 * t3 - 3 * t2 + 1).add(left.scale(t3 - 2 * t2 + t))
                .add(b.scale(-2 * t3 + 3 * t2)).add(right.scale(t3 - t2));
    }
    private static Vec derivative(Vec a, Vec b, Vec left, Vec right, double t) {
        double t2 = t * t;
        return a.subtract(b).scale(6 * t2 - 6 * t).add(left.scale(3 * t2 - 4 * t + 1)).add(right.scale(3 * t2 - 2 * t));
    }

    private static ArrayList<Knot> smooth(List<Knot> raw, int subdivisions) {
        var result = new ArrayList<Knot>((raw.size() - 1) * subdivisions + 1);
        for (int i = 0; i < raw.size() - 1; i++) {
            Knot a = raw.get(Math.max(0, i - 1)), b = raw.get(i), c = raw.get(i + 1), d = raw.get(Math.min(raw.size() - 1, i + 2));
            double length = c.position().subtract(b.position()).length();
            Vec left = limit(c.position().subtract(a.position()).scale(.5), length);
            Vec right = limit(d.position().subtract(b.position()).scale(.5), length);
            for (int step = 0; step < subdivisions; step++) {
                double t = (double) step / subdivisions, t2 = t * t, t3 = t2 * t;
                Vec position = b.position().scale(2 * t3 - 3 * t2 + 1).add(left.scale(t3 - 2 * t2 + t))
                        .add(c.position().scale(-2 * t3 + 3 * t2)).add(right.scale(t3 - t2));
                append(result, new Knot(position, lerp(b.tick(), c.tick(), t), 0));
            }
        }
        append(result, raw.get(raw.size() - 1)); return result;
    }
    private static void append(List<Knot> values, Knot value) {
        if (values.isEmpty() || values.get(values.size() - 1).position().subtract(value.position()).lengthSquared() > EPSILON * EPSILON) values.add(value);
    }
    private static Vec tangent(List<Knot> knots, int index) {
        Vec current = knots.get(index).position();
        Vec direction = knots.get(Math.min(knots.size() - 1, index + 1)).position()
                .subtract(knots.get(Math.max(0, index - 1)).position());
        if (direction.lengthSquared() <= EPSILON * EPSILON) {
            direction = index + 1 < knots.size() ? knots.get(index + 1).position().subtract(current)
                    : current.subtract(knots.get(index - 1).position());
        }
        return direction.normalize();
    }
    private static Vec transport(Vec side, Vec from, Vec to) {
        Vec rotation = from.cross(to); double sine = rotation.length(), cosine = Math.max(-1, Math.min(1, from.dot(to)));
        if (sine > EPSILON) {
            Vec axis = rotation.scale(1 / sine);
            side = side.scale(cosine).add(axis.cross(side).scale(sine)).add(axis.scale(axis.dot(side) * (1 - cosine)));
        }
        return project(side, to);
    }
    private static Vec project(Vec side, Vec tangent) {
        Vec projected = side.subtract(tangent.scale(side.dot(tangent)));
        if (projected.lengthSquared() <= EPSILON * EPSILON) {
            Vec reference = Math.abs(tangent.y()) < .8 ? new Vec(0, 1, 0) : new Vec(0, 0, 1);
            projected = tangent.cross(reference);
        }
        return projected.normalize();
    }
    private static Vec limit(Vec vector, double maximum) { return vector.length() > maximum ? vector.normalize().scale(maximum) : vector; }
    private static Vec lerp(Vec a, Vec b, double t) { return a.add(b.subtract(a).scale(t)); }
    private static double lerp(double a, double b, double t) { return a + (b - a) * t; }
    private static double clamp(double value) { return Math.max(0, Math.min(1, value)); }
}
