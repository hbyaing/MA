package cn.blockforge.generated.generatedmod.client.shield;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Loader-independent curved shield fragments, adapted from the stylised-shield skill's
 * original Voronoi surface cutter. All positions and velocities are in unit-sphere space.
 * Keep each vertex's original position/UV as its material coordinates while moving it.
 */
public final class ShieldFractureGeometry {
    private static final double AREA_EPSILON = 1.0e-10;
    private static final List<Triangle> UNIT_SPHERE = sphereMesh(24, 48);

    public record Vec(double x, double y, double z) {
        public Vec add(Vec b) { return new Vec(x + b.x, y + b.y, z + b.z); }
        public Vec sub(Vec b) { return new Vec(x - b.x, y - b.y, z - b.z); }
        public Vec mul(double s) { return new Vec(x * s, y * s, z * s); }
        public double dot(Vec b) { return x * b.x + y * b.y + z * b.z; }
        public Vec cross(Vec b) {
            return new Vec(y * b.z - z * b.y, z * b.x - x * b.z, x * b.y - y * b.x);
        }
        public double length() { return Math.sqrt(dot(this)); }
        public Vec unit() {
            double length = length();
            return length > 1.0e-12 ? mul(1.0 / length) : new Vec(0, 0, 1);
        }
        /** Rodrigues rotation; the supplied axis must be a unit vector. */
        public Vec rotate(Vec axis, double angle) {
            double c = Math.cos(angle), s = Math.sin(angle);
            return mul(c).add(axis.cross(this).mul(s)).add(axis.mul(axis.dot(this) * (1 - c)));
        }
    }

    public record Vertex(Vec pos, Vec normal, double u, double v) {
        public Vertex mix(Vertex b, double t) {
            return new Vertex(pos.add(b.pos.sub(pos).mul(t)),
                    normal.add(b.normal.sub(normal).mul(t)),
                    u + (b.u - u) * t, v + (b.v - v) * t);
        }
    }

    public record Triangle(Vertex a, Vertex b, Vertex c) {
        public double area() { return b.pos.sub(a.pos).cross(c.pos.sub(a.pos)).length() * .5; }
        public Vec center() { return a.pos.add(b.pos).add(c.pos).mul(1.0 / 3); }
        public List<Vertex> vertices() { return List.of(a, b, c); }
        private double radius() {
            Vec q = center();
            return Math.max(a.pos.sub(q).length(), Math.max(b.pos.sub(q).length(), c.pos.sub(q).length()));
        }
    }

    public record Edge(Vertex a, Vertex b) {}

    public record Patch(List<Triangle> triangles, List<Edge> edges, Vec center,
                        Vec velocity, Vec axis, double delay, double spin) {
        /** A rigid piece retains its curved surface while rotating, dispersing and falling. */
        public Vec transform(Vec source, double age, double spinRate, double gravity, double speedMultiplier) {
            double t = Math.max(0, age - delay);
            if (t == 0) return source;
            return source.sub(center).rotate(axis, t * spinRate * spin).add(center)
                    .add(velocity.mul(t * speedMultiplier)).add(new Vec(0, -.5 * gravity * t * t, 0));
        }
        public Vec point(Vec source, double age, double spinRate, double gravity) {
            return transform(source, age, spinRate, gravity, 1);
        }
        public Vec normal(Vec sourceNormal, double age, double spinRate) {
            return sourceNormal.rotate(axis, Math.max(0, age - delay) * spinRate * spin);
        }
    }

    private ShieldFractureGeometry() {}

    /** Shared immutable mesh; 2,208 nondegenerate triangles at the existing shield's resolution. */
    public static List<Triangle> unitSphereMesh() { return UNIT_SPHERE; }

    public static List<Triangle> sphereMesh(int rows, int columns) {
        if (rows < 2 || columns < 3) throw new IllegalArgumentException("Sphere needs at least 2 rows and 3 columns");
        Vertex[][] vertices = new Vertex[rows + 1][columns + 1];
        for (int row = 0; row <= rows; row++) {
            for (int column = 0; column <= columns; column++) {
                double u = column / (double) columns, v = row / (double) rows;
                double phi = v * Math.PI, theta = u * Math.PI * 2;
                Vec p = new Vec(Math.sin(phi) * Math.cos(theta), Math.cos(phi), Math.sin(phi) * Math.sin(theta));
                vertices[row][column] = new Vertex(p, p.unit(), u, v);
            }
        }
        List<Triangle> out = new ArrayList<>(2 * rows * columns);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Vertex a = vertices[row][column], b = vertices[row][column + 1];
                Vertex c = vertices[row + 1][column], d = vertices[row + 1][column + 1];
                Triangle first = new Triangle(a, b, c), second = new Triangle(b, d, c);
                if (first.area() > AREA_EPSILON) out.add(first);
                if (second.area() > AREA_EPSILON) out.add(second);
            }
        }
        return List.copyOf(out);
    }

    /**
     * Exactly cuts the triangulated surface into cells. Sampling only chooses the seed
     * positions: the fragments themselves are clipped polygons spanning many triangles.
     * Speed is captured into each velocity once; use transform's multiplier of 1 normally.
     */
    public static List<Patch> fracture(List<Triangle> mesh, Vec unitHit, int shardCount, long seed, double speed) {
        if (mesh.isEmpty()) return List.of();
        if (shardCount < 1 || !Double.isFinite(speed) || speed < 0) {
            throw new IllegalArgumentException("Positive shard count and finite nonnegative speed required");
        }
        Random random = new Random(seed);
        int count = Math.min(shardCount, mesh.size());
        List<Vec> centers = seedCenters(mesh, count, random);
        List<List<Triangle>> groups = new ArrayList<>(count);
        for (int i = 0; i < count; i++) groups.add(new ArrayList<>());

        for (Triangle tri : mesh) {
            Vec center = tri.center();
            double nearest = Double.POSITIVE_INFINITY;
            double[] distances = new double[count];
            for (int i = 0; i < count; i++) {
                distances[i] = centers.get(i).sub(center).length();
                nearest = Math.min(nearest, distances[i]);
            }
            // A seed farther than this bound cannot own any point of this triangle.
            double radius = tri.radius();
            List<Integer> candidates = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                if (distances[i] <= nearest + 2 * radius + 1.0e-6) candidates.add(i);
            }
            for (int i : candidates) {
                List<Vertex> polygon = tri.vertices();
                Vec a = centers.get(i);
                for (int j : candidates) {
                    if (polygon.size() < 3) break;
                    if (i != j) {
                        Vec b = centers.get(j);
                        polygon = clip(polygon, b.sub(a), (b.dot(b) - a.dot(a)) * .5);
                    }
                }
                for (int j = 1; j + 1 < polygon.size(); j++) {
                    Triangle piece = new Triangle(polygon.get(0), polygon.get(j), polygon.get(j + 1));
                    if (piece.area() > AREA_EPSILON) groups.get(i).add(piece);
                }
            }
        }

        List<Patch> out = new ArrayList<>(count);
        for (List<Triangle> triangles : groups) {
            if (triangles.isEmpty()) continue;
            double area = 0;
            Vec center = new Vec(0, 0, 0), normal = new Vec(0, 0, 0);
            Map<String, Edge> edges = new LinkedHashMap<>();
            Map<String, Integer> edgeCounts = new LinkedHashMap<>();
            for (Triangle triangle : triangles) {
                double triArea = triangle.area();
                area += triArea;
                center = center.add(triangle.center().mul(triArea));
                normal = normal.add(triangle.a.normal.add(triangle.b.normal).add(triangle.c.normal).mul(triArea / 3));
                List<Vertex> vertices = triangle.vertices();
                for (int j = 0; j < 3; j++) {
                    Vertex a = vertices.get(j), b = vertices.get((j + 1) % 3);
                    String ka = pointKey(a.pos), kb = pointKey(b.pos);
                    if (ka.equals(kb)) continue;
                    String key = ka.compareTo(kb) < 0 ? ka + "|" + kb : kb + "|" + ka;
                    edgeCounts.merge(key, 1, Integer::sum);
                    edges.putIfAbsent(key, new Edge(a, b));
                }
            }
            center = center.mul(1 / area);
            normal = normal.unit();
            Vec tangent = randomVector(random), away = center.sub(unitHit).unit();
            Vec velocity = normal.mul(.6 + random.nextDouble() * .5)
                    .add(away.mul(.35)).add(tangent.mul(.28)).mul(speed);
            Vec axis = randomVector(random);
            double delay = .045 + Math.min(1, center.sub(unitHit).length() / 2.25) * .22;
            double spin = (random.nextBoolean() ? -1 : 1) * (.45 + random.nextDouble());
            List<Edge> outline = new ArrayList<>();
            for (var entry : edges.entrySet()) {
                if (edgeCounts.get(entry.getKey()) == 1) outline.add(entry.getValue());
            }
            out.add(new Patch(List.copyOf(triangles), List.copyOf(outline), center, velocity, axis, delay, spin));
        }
        return List.copyOf(out);
    }

    private static List<Vec> seedCenters(List<Triangle> mesh, int count, Random random) {
        List<Vec> centers = new ArrayList<>(count);
        centers.add(mesh.get(random.nextInt(mesh.size())).center());
        double[] distance = new double[mesh.size()];
        Arrays.fill(distance, Double.POSITIVE_INFINITY);
        for (int s = 1; s < count; s++) {
            int best = 0;
            double farthest = -1;
            Vec last = centers.get(centers.size() - 1);
            for (int j = 0; j < mesh.size(); j++) {
                Vec q = mesh.get(j).center().sub(last);
                distance[j] = Math.min(distance[j], q.dot(q));
                double score = distance[j] * (.88 + random.nextDouble() * .24);
                if (score > farthest) { farthest = score; best = j; }
            }
            centers.add(mesh.get(best).center());
        }
        return centers;
    }

    private static List<Vertex> clip(List<Vertex> polygon, Vec normal, double distance) {
        List<Vertex> out = new ArrayList<>();
        for (int i = 0; i < polygon.size(); i++) {
            Vertex a = polygon.get(i), b = polygon.get((i + 1) % polygon.size());
            double da = a.pos.dot(normal) - distance, db = b.pos.dot(normal) - distance;
            if (da <= 0) out.add(a);
            if ((da <= 0) != (db <= 0)) out.add(a.mix(b, da / (da - db)));
        }
        return out;
    }

    private static String pointKey(Vec p) {
        // Also merges the UV seam and pole duplicates without joining different curves.
        return Math.round(p.x * 1.0e5) + "," + Math.round(p.y * 1.0e5) + "," + Math.round(p.z * 1.0e5);
    }

    private static Vec randomVector(Random random) {
        return new Vec(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1).unit();
    }
}
