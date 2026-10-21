package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.realmflight.RealmMeteorPath;
import cn.blockforge.generated.generatedmod.realmflight.RealmMeteorPath.Frame;
import cn.blockforge.generated.generatedmod.realmflight.RealmMeteorPath.Vec;
import java.util.List;

/** Behavioral checks for path time, geometric continuity, and complete flight exit. */
public final class RealmMeteorPathRegression {
    private static int checks;
    private static void require(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    private static void near(double expected, double actual, double tolerance, String message) {
        require(Math.abs(expected - actual) <= tolerance, message + ": expected=" + expected + ", actual=" + actual);
    }
    private static RealmMeteorPath straight(int ticks, double speed) {
        var path = new RealmMeteorPath();
        for (int tick = 0; tick <= ticks; tick++) path.sample(new Vec(tick * speed, 0, 0), tick, true);
        return path;
    }
    private static void orthonormal(List<Frame> frames, String label) {
        double arc = -Double.MAX_VALUE;
        for (Frame frame : frames) {
            require(frame.position().finite() && frame.tangent().finite() && frame.side().finite() && frame.up().finite(), label + " finite basis");
            near(1, frame.tangent().length(), 1e-8, label + " tangent unit length");
            near(1, frame.side().length(), 1e-8, label + " side unit length");
            near(1, frame.up().length(), 1e-8, label + " up unit length");
            near(0, frame.tangent().dot(frame.side()), 1e-8, label + " side orthogonality");
            near(0, frame.tangent().dot(frame.up()), 1e-8, label + " up orthogonality");
            require(frame.arc() > arc, label + " measured arc increases"); arc = frame.arc();
            require(frame.opacity() >= 0 && frame.opacity() <= 1, label + " bounded opacity");
        }
    }
    public static void run() {
        checks = 0;
        var line = straight(80, .65);
        require(line.sampleCount() <= RealmMeteorPath.MAX_SAMPLES, "History capacity stays bounded under sustained flight");
        for (double partial : new double[] {0, .25, .5, .75, 1}) {
            double time = 79 + partial, head = time * .65;
            var frames = line.render(time, new Vec(head, 0, 0), 3, 18);
            require(frames.size() > 2, "An established flight has a smooth centerline");
            near(head, frames.get(frames.size() - 1).position().x(), 1e-8, "Head matches the entity interpolation time");
            double previous = -Double.MAX_VALUE;
            for (Frame frame : frames) {
                require(frame.tick() <= time + 1e-9, "Future history never makes the head double back");
                require(frame.position().x() >= previous, "Straight flight never develops a backwards head hook");
                previous = frame.position().x();
            }
            near(18, frames.get(frames.size() - 1).arc() - frames.get(0).arc(), 1e-8, "Maximum length clips the tail continuously");
            orthonormal(frames, "Straight flight");
        }
        var bend = new RealmMeteorPath();
        for (int tick = 0; tick <= 45; tick++) {
            double angle = tick * .05;
            bend.sample(new Vec(12 * Math.cos(angle), 5 * Math.sin(angle * .7), 12 * Math.sin(angle)), tick, true);
        }
        var curve = bend.render(44.5, new Vec(12 * Math.cos(44.5 * .05), 5 * Math.sin(44.5 * .05 * .7), 12 * Math.sin(44.5 * .05)), 3, 30);
        orthonormal(curve, "Ascending curved flight");
        double measured = 0;
        for (int i = 1; i < curve.size(); i++) {
            measured += curve.get(i).position().subtract(curve.get(i - 1).position()).length();
            require(curve.get(i).side().dot(curve.get(i - 1).side()) > .95, "A continuous curve has no cross-section flips");
        }
        near(measured, curve.get(curve.size() - 1).arc() - curve.get(0).arc(), 1e-8, "Shader arc uses the actual curved distance");
        require(curve.get(0).position().subtract(curve.get(curve.size() - 1).position()).length() < measured, "The turning path remains curved in world space");
        var beforeTrim = bend.render(44, new Vec(12 * Math.cos(2.2), 5 * Math.sin(1.54), 12 * Math.sin(2.2)), 3, 30);
        require(beforeTrim.get(0).side().dot(curve.get(0).side()) > .95, "Rendering interpolation preserves the transported starting frame");

        var stop = straight(30, .5);
        stop.sample(new Vec(15, 0, 0), 31, false);
        var justStopped = stop.render(31, new Vec(100, 0, 0), 3, 18);
        near(15, justStopped.get(justStopped.size() - 1).position().x(), 1e-8, "Inactive history stays in the world instead of following the player");
        var fading = stop.render(40, new Vec(100, 0, 0), 3, 18);
        require(!fading.isEmpty() && fading.get(fading.size() - 1).opacity() < justStopped.get(justStopped.size() - 1).opacity(), "Stopped trail fades before expiring");
        require(stop.render(49, new Vec(100, 0, 0), 3, 18).isEmpty(), "Stopped trail is completely gone after 0.9 seconds");
        for (int tick = 32; tick <= 55; tick++) stop.sample(new Vec(15, 0, 0), tick, false);
        stop.sample(new Vec(15.5, 0, 0), 56, true);
        require(stop.render(55.5, new Vec(15.25, 0, 0), 3, 18).isEmpty(), "Starting again cannot resurrect an expired old trail");

        var teleport = straight(20, .5);
        teleport.sample(new Vec(10000, 5, -10000), 21, true);
        require(teleport.sampleCount() == 1 && teleport.render(21, new Vec(10000, 5, -10000), 3, 36).isEmpty(), "Teleport severs the old trajectory");
        teleport.sample(new Vec(10001, 5, -10000), 0, true);
        require(teleport.sampleCount() == 1, "A clock reset cannot connect a previous world history");
        teleport.sample(new Vec(Double.NaN, 0, 0), 1, true);
        require(teleport.sampleCount() == 0 && teleport.speed() == 0, "Non-finite input clears contaminated history");

        var hovering = new RealmMeteorPath();
        for (int tick = 0; tick < 100; tick++) hovering.sample(new Vec(2, 3, 4), tick, true);
        require(hovering.render(99, new Vec(2, 3, 4), 3, 36).isEmpty(), "Stationary hovering never invents a long tail");
        near(0, hovering.speed(), 1e-9, "Stationary speed remains zero");
        var vertical = new RealmMeteorPath();
        for (int tick = 0; tick <= 30; tick++) vertical.sample(new Vec(0, tick * .3, 0), tick, true);
        orthonormal(vertical.render(29.5, new Vec(0, 8.85, 0), 3, 18), "Vertical flight");
        var distant = new RealmMeteorPath();
        for (int tick = 0; tick <= 25; tick++) distant.sample(new Vec(20_000_000 + tick * .5, 0, 20_000_000), tick, true);
        var distantFrames = distant.render(24.5, new Vec(20_000_012.25, 0, 20_000_000), 3, 18);
        near(20_000_012.25, distantFrames.get(distantFrames.size() - 1).position().x(), 1e-7, "Far world coordinates retain sub-block interpolation precision");
        orthonormal(distantFrames, "Far coordinate flight");
        adaptiveChecks();
        anchorTransportChecks();
        System.out.println("Realm meteor path regression: " + checks + " geometry/time/lifecycle checks passed");
    }

    /**
     * The first active sample starts with a vertical tangent and an X side.
     * Straight X flight minimally transports that side into -Y. Projecting the
     * original X side directly onto the smoothed X tangent loses its radial
     * phase and chooses Z instead, until the age cut removes that first sample.
     * This reproduces a ninety-degree material roll despite an unchanged path.
     */
    private static void anchorTransportChecks() {
        Vec expectedSide = new Vec(0, -1, 0);
        for (Vec offset : new Vec[] {Vec.ZERO, new Vec(32, 137.33803375512343, .004376726039510263),
                new Vec(20_000_000, 140, -20_000_000)}) {
            var path = new RealmMeteorPath();
            for (int tick = 0; tick <= 60; tick++) path.sample(offset.add(new Vec(tick * .5, 0, 0)), tick, true);
            for (double lifetime : new double[] {16, 24, 48}) {
                for (double length : new double[] {8, 28, 100}) {
                    for (int lod = 0; lod <= 2; lod++) {
                        Frame previous = null;
                        for (double delta : new double[] {-1, -.000001, 0, .000001, .001, .5, .999999, 1}) {
                            double time = lifetime + delta;
                            Vec head = offset.add(new Vec(time * .5, 0, 0));
                            var frames = path.renderAdaptive(time, head, length, lifetime, lod);
                            require(frames.size() >= 2, "Anchor-age boundary retains a real rendered path");
                            Frame terminal = frames.get(frames.size() - 1);
                            require(terminal.side().dot(expectedSide) > .999999,
                                    "Age/length/partial cut preserves the original transported phase instead of reselecting a world axis");
                            if (previous != null) require(terminal.side().dot(previous.side()) > .999999,
                                    "Removing the stationary anchor cannot roll the material ninety degrees");
                            previous = terminal;
                            for (Frame frame : frames) {
                                require(frame.side().dot(expectedSide) > .999999, "Every retained ring shares the transported straight-flight phase");
                                require(frame.tick() <= time + 1e-7, "Anchor transport never changes history time");
                            }
                        }
                    }
                    var fixed = path.render(lifetime - .000001,
                            offset.add(new Vec((lifetime - .000001) * .5, 0, 0)), 3, length, lifetime);
                    require(fixed.get(fixed.size() - 1).side().dot(expectedSide) > .999999,
                            "Fixed-subdivision rendering transports the same historical anchor");
                }
            }
            // Advancing simulation eventually evicts the seed sample as well as
            // shifting the age window. Neither event may change the radial phase.
            Frame previous = null;
            for (int tick = 61; tick <= 78; tick++) {
                path.sample(offset.add(new Vec(tick * .5, 0, 0)), tick, true);
                double time = tick - .9;
                var frames = path.renderAdaptive(time, offset.add(new Vec(time * .5, 0, 0)), 28, 60, 0);
                Frame terminal = frames.get(frames.size() - 1);
                require(terminal.side().dot(expectedSide) > .999999, "Simulation history eviction retains its transported phase");
                if (previous != null) require(terminal.side().dot(previous.side()) > .999999, "Consecutive simulation ticks never reselect a radial axis");
                previous = terminal;
            }
        }
    }

    private static void adaptiveChecks() {
        var line = straight(80, .65);
        var legacy = line.render(79.5, new Vec(79.5 * .65, 0, 0), 3, 28, 24);
        int historyCount = line.sampleCount(); double historySpeed = line.speed();
        int previousCount = Integer.MAX_VALUE;
        for (int lod = 0; lod <= 2; lod++) {
            var frames = line.renderAdaptive(79.5, new Vec(79.5 * .65, 0, 0), 28, 24, lod);
            require(frames.size() <= previousCount, "Distant output removes subdivisions before the historical silhouette");
            previousCount = frames.size();
            require(frames.size() <= RealmMeteorPath.MAX_ADAPTIVE_FRAMES, "Adaptive geometry has a strict total frame budget");
            orthonormal(frames, "Adaptive straight LOD " + lod);
            near(79.5 * .65, frames.get(frames.size() - 1).position().x(), 1e-8, "All LODs keep the exact interpolated head");
            for (int i = 0; i < frames.size(); i++) {
                require(frames.get(i).tick() <= 79.5, "Adaptive interpolation never reads future entity positions");
                near(.8, RealmMeteorPath.radiusLimit(frames, i, .8), 1e-8, "Straight tubes do not acquire artificial necks");
            }
            require(frames.equals(line.renderAdaptive(79.5, new Vec(79.5 * .65, 0, 0), 28, 24, lod)), "Repeated render samples are deterministic");
        }
        require(historyCount == line.sampleCount(), "Rendering all LODs does not add historical samples");
        near(historySpeed, line.speed(), 0, "Rendering all LODs does not advance velocity filters");
        require(legacy.equals(line.render(79.5, new Vec(79.5 * .65, 0, 0), 3, 28, 24)), "Adaptive rendering leaves the legacy output unchanged");

        var tight = new RealmMeteorPath();
        double chord = 1.4 * Math.sin(.25);
        for (int tick = 0; tick <= 47; tick++) tight.sample(new Vec(.7 * Math.cos(tick * .5), 0, .7 * Math.sin(tick * .5)), tick, true);
        var matchedStraight = straight(47, chord);
        for (int lod = 0; lod <= 2; lod++) {
            var curve = tight.renderAdaptive(47, null, 40, 48, lod);
            var straight = matchedStraight.renderAdaptive(47, null, 40, 48, lod);
            require(curve.size() > straight.size(), "Curvature adds geometry at equal speed for LOD " + lod);
            require(curve.size() <= RealmMeteorPath.MAX_ADAPTIVE_FRAMES, "Repeated high curvature remains bounded");
            orthonormal(curve, "Adaptive tight curve LOD " + lod);
            for (int i = 1; i < curve.size(); i++) require(curve.get(i).side().dot(curve.get(i - 1).side()) >= 0, "Curved tube cross-sections do not flip");
            double width = RealmMeteorPath.radiusLimit(curve, curve.size() / 2, 2);
            require(width > 0 && width < 1, "Tight curves limit the inner tube radius");
        }

        var maximum = straight(RealmMeteorPath.MAX_SAMPLES-1, 3.9);
        var budget = maximum.renderAdaptive(RealmMeteorPath.MAX_SAMPLES-1, null, 1000, RealmMeteorPath.HISTORY_TICKS, 0);
        require(budget.size() == RealmMeteorPath.MAX_ADAPTIVE_FRAMES, "Worst-case distance reaches but never exceeds the six-way subdivision budget");
        var clipped = maximum.renderAdaptive(46.75, new Vec(46.75 * 3.9, 0, 0), 28, 48, 0);
        require(clipped.size() <= RealmMeteorPath.MAX_ADAPTIVE_FRAMES, "Interpolated length cut does not exceed the global budget");
        near(28, clipped.get(clipped.size() - 1).arc() - clipped.get(0).arc(), 1e-8, "Adaptive clipping retains an exact maximum length");

        var reversal = new RealmMeteorPath();
        for (int tick = 0; tick <= 40; tick++) reversal.sample(new Vec((tick <= 20 ? tick : 40 - tick) * .5, 0, 0), tick, true);
        var reversed = reversal.renderAdaptive(40, null, 40, 48, 0);
        orthonormal(reversed, "Adaptive true reversal");
        int peak = 0;
        for (int i = 0; i < reversed.size(); i++) {
            require(reversed.get(i).position().x() >= -1e-8 && reversed.get(i).position().x() <= 10 + 1e-8, "Reversal tangents never overshoot the recorded cusp");
            if (reversed.get(i).position().x() > reversed.get(peak).position().x()) peak = i;
        }
        near(10, reversed.get(peak).position().x(), 1e-8, "Reversal retains its real historical turn point");
        near(0, RealmMeteorPath.radiusLimit(reversed, peak, 1), 1e-8, "An exact turnaround pinches instead of self-folding");

        var climb = new RealmMeteorPath();
        Vec position = Vec.ZERO;
        for (int tick = 0; tick <= 47; tick++) {
            double heading = Math.toRadians(160 + tick * 2.5), elevation = Math.toRadians(tick < 24 ? 25 : -25);
            position = position.add(new Vec(Math.cos(heading) * Math.cos(elevation), Math.sin(elevation), Math.sin(heading) * Math.cos(elevation)).scale(.55));
            climb.sample(position, tick, true);
        }
        for (int lod = 0; lod <= 2; lod++) {
            var frames = climb.renderAdaptive(47, null, 24, 24, lod);
            orthonormal(frames, "Adaptive climb/dive/yaw seam LOD " + lod);
            for (int i = 1; i < frames.size(); i++) require(frames.get(i).side().dot(frames.get(i - 1).side()) > 0, "Climb/dive across 180 degrees preserves cross-section phase");
        }

        var stop = straight(30, .5);
        stop.sample(new Vec(15, 0, 0), 31, false);
        var stopped = stop.renderAdaptive(31, new Vec(100, 0, 0), 28, 24, 0);
        near(15, stopped.get(stopped.size() - 1).position().x(), 1e-8, "Adaptive inactive trail stays at its last world position");
        require(stop.renderAdaptive(49, new Vec(100, 0, 0), 28, 24, 0).isEmpty(), "Adaptive stop fade still expires on tick time");
        stop.sample(new Vec(100, 0, 0), 50, true);
        require(stop.renderAdaptive(50, null, 28, 24, 0).isEmpty(), "Adaptive teleport never stitches separate histories");
        require(line.renderAdaptive(79, null, 28, Double.NaN, 0).isEmpty(), "Non-finite adaptive lifetime is rejected");
        near(0, RealmMeteorPath.radiusLimit(List.of(), 0, 1), 0, "Empty radius query is safe");
        near(0, RealmMeteorPath.radiusLimit(reversed, peak, Double.NaN), 0, "Non-finite radius cannot contaminate geometry");
    }
    public static void main(String[] args) { run(); }
    private RealmMeteorPathRegression() {}
}
