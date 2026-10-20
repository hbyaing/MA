package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.client.shield.ShieldFractureGeometry;
import cn.blockforge.generated.generatedmod.client.shield.ShieldFractureGeometry.Edge;
import cn.blockforge.generated.generatedmod.client.shield.ShieldFractureGeometry.Patch;
import cn.blockforge.generated.generatedmod.client.shield.ShieldFractureGeometry.Triangle;
import cn.blockforge.generated.generatedmod.client.shield.ShieldFractureGeometry.Vec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Independent executable geometry checks; no Forge/client initialization is required. */
public final class ShieldFractureGeometryRegression {
    private static int checks;

    private static void require(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void near(double expected, double actual, double tolerance, String message) {
        require(Math.abs(expected - actual) <= tolerance, message + ": expected " + expected + ", got " + actual);
    }

    private static String key(Vec p) {
        return Math.round(p.x() * 1.0e5) + "," + Math.round(p.y() * 1.0e5) + "," + Math.round(p.z() * 1.0e5);
    }

    public static void main(String[] args) { run(); }

    public static void run() {
        checks = 0;
        List<Triangle> sphere = ShieldFractureGeometry.unitSphereMesh();
        require(sphere == ShieldFractureGeometry.unitSphereMesh(), "The immutable normal mesh must be cached");
        require(sphere.size() == 2208, "The 24 by 48 sphere excludes only degenerate pole triangles");
        double area = sphere.stream().mapToDouble(Triangle::area).sum();
        near(4 * Math.PI, area, .05, "Normal surface tessellation must approximate a unit sphere");
        for (Triangle triangle : sphere) {
            require(triangle.b().pos().sub(triangle.a().pos()).cross(triangle.c().pos().sub(triangle.a().pos()))
                    .dot(triangle.center()) > 0, "Sphere triangles must wind outward");
        }

        Vec hit = new Vec(.4, .2, .9).unit();
        List<Patch> one = ShieldFractureGeometry.fracture(sphere, hit, 1, 71, 2.8);
        require(one.size() == 1 && one.get(0).edges().isEmpty(),
                "A whole sphere must have no internal triangle, UV seam, or pole boundary edges");
        long started = System.nanoTime();
        List<Patch> patches = ShieldFractureGeometry.fracture(sphere, hit, 110, 9194, 2.8);
        double elapsedMs = (System.nanoTime() - started) / 1.0e6;
        require(patches.size() == 110, "Requested Voronoi seeds must each produce a curved fragment");
        require(patches.equals(ShieldFractureGeometry.fracture(sphere, hit, 110, 9194, 2.8)),
                "One seed must reproduce geometry, velocity, spin, and crack propagation");
        near(area, patches.stream().flatMap(p -> p.triangles().stream()).mapToDouble(Triangle::area).sum(),
                1.0e-7, "Cuts must conserve surface area without holes or overlaps");

        int curvedFragments = 0;
        double boundaryLength = 0, allTriangleEdgeLength = 0;
        for (Patch patch : patches) {
            require(patch.triangles().size() > 3, "Fragments must span the curved surface, not become triangle tiles");
            require(patch.edges().size() >= 3, "Every fragment requires a polygonal boundary");
            near(1, patch.axis().length(), 1.0e-12, "Rotation axes must be normalized");
            near(.045 + Math.min(1, patch.center().sub(hit).length() / 2.25) * .22,
                    patch.delay(), 1.0e-12, "Crack release delay must propagate from the impact point");
            Map<String, Integer> degrees = new HashMap<>();
            for (Edge edge : patch.edges()) {
                degrees.merge(key(edge.a().pos()), 1, Integer::sum);
                degrees.merge(key(edge.b().pos()), 1, Integer::sum);
                boundaryLength += edge.b().pos().sub(edge.a().pos()).length();
            }
            for (int degree : degrees.values()) require(degree == 2, "Fragment outlines must close without internal branches");

            Triangle first = patch.triangles().get(0);
            Vec a = first.a().pos(), b = first.b().pos();
            near(0, patch.transform(a, 0, 3, 1.8, 1).sub(a).length(), 0,
                    "At break time every source vertex must be exactly on the existing shell");
            near(0, patch.transform(a, patch.delay() * .99, 3, 1.8, 1).sub(a).length(), 0,
                    "A fragment may not detach before its crack arrives");
            near(a.sub(b).length(), patch.transform(a, 1.15, 3, 1.8, 1)
                    .sub(patch.transform(b, 1.15, 3, 1.8, 1)).length(), 1.0e-12,
                    "Translation, fall and rotation must preserve fragment edge lengths");
            near(0, patch.normal(first.a().normal(), 0, 3).sub(first.a().normal()).length(), 0,
                    "Initial shading normal must stay frozen with its material");
            near(first.a().normal().length(), patch.normal(first.a().normal(), 1.15, 3).length(), 1.0e-12,
                    "Rotating fragment normals must preserve their length");

            double localAge = 1.15 - patch.delay();
            Vec noGravity = patch.transform(a, 1.15, 3, 0, 1);
            Vec withGravity = patch.transform(a, 1.15, 3, 1.8, 1);
            near(-.5 * 1.8 * localAge * localAge, withGravity.y() - noGravity.y(), 1.0e-12,
                    "Fragments must accelerate downward after their own release");
            boolean curved = false;
            for (Triangle triangle : patch.triangles()) {
                for (var vertex : triangle.vertices()) {
                    require(vertex.pos().length() <= 1 + 1.0e-12, "Cuts must retain the original coarse surface");
                    near(0, vertex.normal().sub(vertex.pos()).length(), 1.0e-12,
                            "Material position and interpolated original spherical normal must survive clipping");
                    if (vertex.normal().unit().dot(first.a().normal().unit()) < .99) curved = true;
                }
                allTriangleEdgeLength += triangle.a().pos().sub(triangle.b().pos()).length()
                        + triangle.b().pos().sub(triangle.c().pos()).length()
                        + triangle.c().pos().sub(triangle.a().pos()).length();
            }
            if (curved) curvedFragments++;
        }
        require(curvedFragments > patches.size() * .9, "Almost all fragments need varying normals along retained curvature");
        require(boundaryLength < allTriangleEdgeLength * .25,
                "Crack geometry must exclude the dense internal triangulation edges");
        System.out.println("PASS: " + checks + " shield fracture geometry checks; " + patches.size()
                + " patches, " + String.format(java.util.Locale.ROOT, "%.1f", elapsedMs) + " ms cold fracture");
    }
}
