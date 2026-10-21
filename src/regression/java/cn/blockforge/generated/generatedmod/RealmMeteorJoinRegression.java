package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.realmflight.RealmMeteorJoin;
import cn.blockforge.generated.generatedmod.realmflight.RealmMeteorPath;
import cn.blockforge.generated.generatedmod.realmflight.RealmMeteorPath.Frame;
import cn.blockforge.generated.generatedmod.realmflight.RealmMeteorPath.Vec;
import java.util.ArrayList;
import java.util.List;

/** Reproduces the adaptive partial-tick neck and checks world-distance curvature joins. */
public final class RealmMeteorJoinRegression {
    private static int checks;
    private static void require(boolean condition, String message) {
        checks++; if (!condition) throw new AssertionError(message);
    }
    private static void near(double expected, double actual, double tolerance, String message) {
        require(Math.abs(expected - actual) <= tolerance,
                message + ": expected=" + expected + ", actual=" + actual);
    }
    private static Vec circle(double tick, double radius, double angle) {
        return new Vec(radius * Math.cos(tick * angle), 0, radius * Math.sin(tick * angle));
    }
    private static List<Frame> adaptiveCircle(int lod, double partial, Vec offset) {
        var path = new RealmMeteorPath();
        for (int tick = 0; tick <= 48; tick++) path.sample(circle(tick, 6, .15).add(offset), tick, true);
        Vec before = circle(47, 6, .15).add(offset), after = circle(48, 6, .15).add(offset);
        return path.renderAdaptive(47 + partial, before.add(after.subtract(before).scale(partial)), 28, 48, lod);
    }
    private static void basis(Frame frame, String label) {
        require(frame.position().finite() && frame.tangent().finite() && frame.side().finite() && frame.up().finite(), label + " finite");
        near(1, frame.tangent().length(), 1e-8, label + " tangent unit");
        near(1, frame.side().length(), 1e-8, label + " side unit");
        near(1, frame.up().length(), 1e-8, label + " up unit");
        near(0, frame.tangent().dot(frame.side()), 1e-8, label + " side perpendicular");
        near(0, frame.tangent().dot(frame.up()), 1e-8, label + " up perpendicular");
        near(0, frame.side().dot(frame.up()), 1e-8, label + " cross-section perpendicular");
    }
    public static void run() {
        checks = 0;
        adaptiveCounterexample();
        headingJoin();
        circleSampling();
        safetyAndReversal();
        System.out.println("Realm meteor join regression: " + checks + " partial/curvature/attachment checks passed");
    }

    private static void adaptiveCounterexample() {
        var failing = adaptiveCircle(0, .001, Vec.ZERO);
        double oldWidth = RealmMeteorPath.radiusLimit(failing, failing.size() - 2, 1.3);
        require(oldWidth < .02, "The recorded six-block turning path reproduces the original needle at partial .001");
        for (int lod = 0; lod <= 2; lod++) {
            for (double partial : new double[] {.001, .01, .05, .25, .5, .99}) {
                var body = adaptiveCircle(lod, partial, Vec.ZERO);
                var joined = RealmMeteorJoin.attachHead(body, 2.1);
                require(joined.size() == body.size() + RealmMeteorJoin.MAX_HEAD_FRAMES, "Head skin has exactly twenty bounded frames");
                require(joined.size() <= RealmMeteorJoin.MAX_JOINED_FRAMES, "Joined geometry retains the global budget");
                for (int i = 0; i < body.size(); i++) require(body.get(i) == joined.get(i), "Direct attachment preserves every rendered history frame");
                var support = new RealmMeteorJoin.RadiusSupport(joined);
                for (int i = Math.max(0, body.size() - 8); i < body.size() + 5; i++) {
                    double width = support.limit(i, 1.3);
                    require(width > 1.05 && width <= 1.3, "An ordinary turn retains the body width instead of a partial-dependent needle: LOD=" + lod + ", partial=" + partial + ", width=" + width);
                    near(width, RealmMeteorJoin.stableRadiusLimit(joined, i, 1.3), 0, "Cached and convenience radius queries agree");
                }
                Frame endpoint = body.get(body.size() - 1);
                double arc = endpoint.arc();
                for (int i = body.size(); i < joined.size(); i++) {
                    Frame skin = joined.get(i);
                    require(skin.tangent() == endpoint.tangent() && skin.side() == endpoint.side() && skin.up() == endpoint.up(), "The skin starts with the exact body radial phase");
                    require(skin.arc() > arc, "Skin arcs increase strictly toward the nose"); arc = skin.arc();
                    near(endpoint.tick(), skin.tick(), 0, "Skin never invents future history time");
                    near(endpoint.opacity(), skin.opacity(), 0, "Skin inherits history opacity");
                    near(0, skin.position().subtract(endpoint.position()).cross(endpoint.tangent()).length(), 1e-8, "Skin stays on the body's forward axis");
                    basis(skin, "Head skin");
                }
                near(2.1, joined.get(joined.size() - 1).position().subtract(endpoint.position()).length(), 1e-8, "The nose ends at the requested physical distance");
            }
        }
        var far = RealmMeteorJoin.attachHead(adaptiveCircle(0, .001, new Vec(20_000_000, 100, -20_000_000)), 2.1);
        var local = RealmMeteorJoin.attachHead(adaptiveCircle(0, .001, Vec.ZERO), 2.1);
        near(RealmMeteorJoin.stableRadiusLimit(local, local.size() - 22, 1.3),
                RealmMeteorJoin.stableRadiusLimit(far, far.size() - 22, 1.3), 1e-6,
                "Large world coordinates do not change the turn limiter");
    }

    private static void headingJoin() {
        Vec desired = new Vec(-Math.sin(47.6 * .15), .08, Math.cos(47.6 * .15)).normalize();
        for (int lod = 0; lod <= 2; lod++) {
            for (double partial : new double[] {0, .000001, .001, .01, .05, .25, .5, .99}) {
                var original = adaptiveCircle(lod, partial, Vec.ZERO);
                var joined = RealmMeteorJoin.attachHead(original, 2.1, desired);
                int head = original.size() - 1;
                for (int i = 0; i < original.size(); i++) {
                    Frame before = original.get(i), after = joined.get(i);
                    require(before.position().equals(after.position()), "Heading transition never changes a recorded world position");
                    near(before.tick(), after.tick(), 0, "Heading transition preserves tick");
                    near(before.arc(), after.arc(), 0, "Heading transition preserves world arc");
                    near(before.opacity(), after.opacity(), 0, "Heading transition preserves fade");
                    basis(after, "Heading join");
                    if (original.get(head).arc() - before.arc() >= RealmMeteorJoin.HEADING_JOIN_LENGTH)
                        require(before == after, "Older history beyond three blocks is untouched");
                    if (i > 0 && original.get(head).arc() - before.arc() < RealmMeteorJoin.HEADING_JOIN_LENGTH) {
                        require(after.tangent().dot(joined.get(i - 1).tangent()) > .90, "Heading change is distributed over the neck instead of one terminal ring");
                        require(after.side().dot(joined.get(i - 1).side()) > .90, "Distributed transport has no radial phase flip");
                    }
                }
                Frame terminal = joined.get(head), skin = joined.get(head + 1);
                near(1, terminal.tangent().dot(desired), 1e-8, "Body endpoint follows the supplied smooth heading");
                require(terminal.tangent() == skin.tangent() && terminal.side() == skin.side() && terminal.up() == skin.up(), "Body and head skin share exactly one basis");
            }
            var atTick = adaptiveCircle(lod, 0, Vec.ZERO);
            var immediatelyAfter = adaptiveCircle(lod, .000001, Vec.ZERO);
            Frame oldAt = atTick.get(atTick.size() - 1), oldAfter = immediatelyAfter.get(immediatelyAfter.size() - 1);
            require(oldAt.tangent().dot(oldAfter.tangent()) < .999, "The test exercises the original terminal tangent jump across the tick boundary");
            var fixedAt = RealmMeteorJoin.attachHead(atTick, 2.1, desired);
            var fixedAfter = RealmMeteorJoin.attachHead(immediatelyAfter, 2.1, desired);
            Frame newAt = fixedAt.get(atTick.size() - 1), newAfter = fixedAfter.get(immediatelyAfter.size() - 1);
            near(1, newAt.tangent().dot(newAfter.tangent()), 1e-8, "Smoothed head direction is continuous across partial zero");
            require(newAt.side().dot(newAfter.side()) > .999, "Historical radial phase stays continuous across partial zero");
        }
    }

    private static List<Frame> polygonCircle(boolean uneven) {
        var result = new ArrayList<Frame>();
        double angle = -1.5, arc = 0; Vec previous = null;
        for (int step = 0; angle <= 1.5; step++) {
            Vec position = new Vec(8 * Math.cos(angle), 0, 8 * Math.sin(angle));
            if (previous != null) arc += position.subtract(previous).length();
            Vec side = new Vec(Math.cos(angle), 0, Math.sin(angle));
            result.add(new Frame(position, new Vec(-Math.sin(angle), 0, Math.cos(angle)), side, new Vec(0, 1, 0), step, arc, 1));
            previous = position;
            angle += uneven ? (step % 3 == 0 ? .012 : step % 3 == 1 ? .065 : .025) : .03;
        }
        return result;
    }
    private static void circleSampling() {
        for (boolean uneven : new boolean[] {false, true}) {
            var circle = polygonCircle(uneven);
            var support = new RealmMeteorJoin.RadiusSupport(circle);
            for (int i = 5; i < circle.size() - 5; i++)
                near(3.6, support.limit(i, 10), .18, "An eight-block circle retains its physical turn radius within the polygon interpolation error under uniform/uneven resampling");
        }
    }

    private static Frame lineFrame(double x, double arc) {
        return new Frame(new Vec(x, 0, 0), new Vec(1, 0, 0), new Vec(0, 1, 0), new Vec(0, 0, 1), arc, arc, 1);
    }
    private static void safetyAndReversal() {
        var line = List.of(lineFrame(0, 0), lineFrame(.001, .001), lineFrame(1, 1), lineFrame(5, 5));
        var support = new RealmMeteorJoin.RadiusSupport(line);
        for (int i = 0; i < line.size(); i++) near(1.3, support.limit(i, 1.3), 0, "Uneven straight sampling retains width");
        var duplicate = new ArrayList<Frame>(line); duplicate.add(1, line.get(0));
        near(1.3, RealmMeteorJoin.stableRadiusLimit(duplicate, 1, 1.3), 0, "Exact duplicate arc/position is safe");
        var reversal = new RealmMeteorPath();
        for (int tick = 0; tick <= 40; tick++) reversal.sample(new Vec((tick <= 20 ? tick : 40 - tick) * .5, 0, 0), tick, true);
        var turn = reversal.renderAdaptive(40, null, 40, 48, 0);
        int peak = 0;
        for (int i = 1; i < turn.size(); i++) if (turn.get(i).position().x() > turn.get(peak).position().x()) peak = i;
        near(0, RealmMeteorJoin.stableRadiusLimit(turn, peak, 1.3), 0, "A true 180-degree turnaround still pinches safely");
        var opposite = RealmMeteorJoin.attachHead(line, 2.1, new Vec(-1, 0, 0));
        for (Frame frame : opposite) basis(frame, "Reverse-heading transport");
        near(0, RealmMeteorJoin.stableRadiusLimit(List.of(), 0, 1), 0, "Empty query is safe");
        near(0, RealmMeteorJoin.stableRadiusLimit(null, 0, 1), 0, "Null query is safe");
        near(0, support.limit(0, Double.NaN), 0, "Invalid width is safe");
        near(0, support.limit(-1, 1), 0, "Invalid index is safe");
        var invalid = new ArrayList<Frame>(line); invalid.set(1, lineFrame(Double.NaN, .001));
        near(0, RealmMeteorJoin.stableRadiusLimit(invalid, 0, 1), 0, "Invalid positions cannot poison probes");
        invalid.set(1, lineFrame(.001, -1));
        near(0, RealmMeteorJoin.stableRadiusLimit(invalid, 0, 1), 0, "Backwards arcs are rejected");
        require(RealmMeteorJoin.attachHead(invalid, 2.1).isEmpty(), "Invalid centerlines do not produce a skin");
        require(RealmMeteorJoin.attachHead(line, Double.NaN).equals(line), "Invalid nose does not inject non-finite geometry");
        var maximum = new RealmMeteorPath();
        for (int tick = 0; tick < RealmMeteorPath.MAX_SAMPLES; tick++) maximum.sample(new Vec(tick * 3.9, 0, 0), tick, true);
        var budget = maximum.renderAdaptive(RealmMeteorPath.MAX_SAMPLES - 1, null, 1000, RealmMeteorPath.HISTORY_TICKS, 0);
        require(RealmMeteorJoin.attachHead(budget, 2.1).size() == RealmMeteorJoin.MAX_JOINED_FRAMES, "Worst-case adaptive history plus skin reaches exactly the declared budget");
    }
    public static void main(String[] args) { run(); }
    private RealmMeteorJoinRegression() {}
}
