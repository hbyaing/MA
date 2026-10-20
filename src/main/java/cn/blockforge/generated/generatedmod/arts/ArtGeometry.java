package cn.blockforge.generated.generatedmod.arts;

/** Pure geometry shared by targeting tests and the server's broad-phase query. Units are blocks. */
public final class ArtGeometry {
    public static boolean cone(double x, double z, double forwardX, double forwardZ, double range, double angle) {
        double distance = Math.hypot(x, z);
        return distance <= range && (distance < 1.0E-6D
                || (x * forwardX + z * forwardZ) / distance >= Math.cos(Math.toRadians(angle * .5D)));
    }
    public static boolean circle(double x, double z, double radius) { return x * x + z * z <= radius * radius; }
    private ArtGeometry() {}
}
