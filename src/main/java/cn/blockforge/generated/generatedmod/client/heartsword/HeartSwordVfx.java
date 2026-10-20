package cn.blockforge.generated.generatedmod.client.heartsword;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * Small, deterministic meshes attached to the authored heart sword. No world particles,
 * billboards or post-processing are needed: the same effect follows either hand and
 * the forge/summon poses. All lengths below are in the normalized OBJ's local units.
 */
public final class HeartSwordVfx {
    private static final float TAU = (float) (Math.PI * 2.0);
    private static final int STREAM_SEGMENTS = 18;
    private static final int CRESCENT_SEGMENTS = 12;
    private static final int GLOW_SEGMENTS = 8;

    /**
     * The input pose is the OBJ local frame, with the grip at GRIP_Y; do not add the
     * vanilla item-model half-block translation. Time is measured in game ticks.
     * Reveal is the remaining visible fraction, for both summoning and dispersing.
     */
    public static void render(PoseStack pose, MultiBufferSource buffers, float time, float reveal,
            boolean summon, boolean firstPerson) {
        reveal = Mth.clamp(reveal, 0.0F, 1.0F);
        if (reveal <= 0.0F) return;
        float limit = Mth.lerp(reveal, HeartSwordModelGeometry.MIN_Y, HeartSwordModelGeometry.MAX_Y);
        Mesh mesh = new Mesh(buffers.getBuffer(HeartSwordRenderTypes.LIGHT), pose.last().pose(), limit);
        float strength = firstPerson ? 0.82F : 1.0F;
        float guardWeight = summon ? smooth((reveal - 0.88F) / 0.12F) : 1.0F;

        bladeStreams(mesh, time, strength);
        gripThread(mesh, time, strength);
        for (int i = 0; i < HeartSwordModelGeometry.CORE_Y.length; i++) {
            core(mesh, HeartSwordModelGeometry.CORE_Y[i], 0.015F, time, i * 1.9F, strength);
        }
        core(mesh, HeartSwordModelGeometry.GUARD_Y, 0.020F, time, 0.8F, strength);
        core(mesh, HeartSwordModelGeometry.POMMEL_Y, 0.013F, time, 3.8F, strength);
        if (guardWeight > 0.0F) crescents(mesh, time, strength * guardWeight);
        tip(mesh, time, strength);
        motes(mesh, time, strength);

        // Only submit our own batch. The caller retains control of item/arm/world passes.
        if (buffers instanceof MultiBufferSource.BufferSource source) {
            source.endBatch(HeartSwordRenderTypes.LIGHT);
        }
    }

    private static void bladeStreams(Mesh mesh, float time, float strength) {
        float root = HeartSwordModelGeometry.BLADE_ROOT_Y;
        float length = HeartSwordModelGeometry.MAX_Y - root;
        for (int face = -1; face <= 1; face += 2) {
            float z = face * (HeartSwordModelGeometry.HALF_DEPTH + 0.002F);
            for (int strand = 0; strand < 3; strand++) {
                float phase = strand * TAU / 3.0F;
                for (int step = 0; step < STREAM_SEGMENTS; step++) {
                    float t0 = step / (float) STREAM_SEGMENTS;
                    float t1 = (step + 1) / (float) STREAM_SEGMENTS;
                    float x0 = streamX(t0, phase, time);
                    float x1 = streamX(t1, phase, time);
                    float alpha0 = streamAlpha(t0, phase, time) * strength;
                    float alpha1 = streamAlpha(t1, phase, time) * strength;
                    // Three narrow translucent veins, with a feather on each edge.
                    ribbon(mesh, x0, root + t0 * length, z * taper(t0),
                            x1, root + t1 * length, z * taper(t1),
                            0.0020F * taper(t0), 0.0020F * taper(t1), alpha0, alpha1,
                            0.40F + strand * 0.10F, 0.76F + strand * 0.06F, 1.0F);
                }
            }
        }
    }

    private static float streamX(float t, float phase, float time) {
        float braid = Mth.sin(t * TAU * 1.6F + phase - time * 0.027F);
        float ripple = Mth.sin(t * TAU * 3.0F - phase + time * 0.011F) * 0.22F;
        return (braid + ripple) * HeartSwordModelGeometry.HALF_WIDTH * 0.52F * taper(t);
    }

    private static float streamAlpha(float t, float phase, float time) {
        // A slow pulse travels from the guard towards the point, rather than flashing
        // the complete blade. Root and tip remain visually attached to the mesh.
        float pulse = 0.5F + 0.5F * Mth.sin(t * TAU * 1.7F - time * 0.085F + phase);
        pulse *= pulse;
        return (0.085F + pulse * pulse * 0.23F) * smooth(t * 12.0F)
                * (1.0F - smooth((t - 0.965F) / 0.035F));
    }

    private static void gripThread(Mesh mesh, float time, float strength) {
        float bottom = HeartSwordModelGeometry.POMMEL_Y;
        float top = HeartSwordModelGeometry.GUARD_Y;
        for (int face = -1; face <= 1; face += 2) {
            float z = face * (HeartSwordModelGeometry.CORE_Z + 0.003F);
            for (int step = 0; step < 8; step++) {
                float t0 = step / 8.0F, t1 = (step + 1) / 8.0F;
                float x0 = Mth.sin(t0 * TAU - time * 0.02F) * 0.003F;
                float x1 = Mth.sin(t1 * TAU - time * 0.02F) * 0.003F;
                float a0 = (0.075F + 0.04F * Mth.sin(t0 * TAU - time * 0.07F)) * strength;
                float a1 = (0.075F + 0.04F * Mth.sin(t1 * TAU - time * 0.07F)) * strength;
                ribbon(mesh, x0, Mth.lerp(t0, bottom, top), z,
                        x1, Mth.lerp(t1, bottom, top), z, 0.0015F, 0.0015F, a0, a1,
                        0.46F, 0.81F, 1.0F);
            }
        }
    }

    private static void core(Mesh mesh, float y, float radius, float time, float phase, float strength) {
        float breathe = 0.88F + 0.12F * Mth.sin(time * 0.042F + phase);
        float glowRadius = radius * (2.15F + 0.12F * Mth.sin(time * 0.035F + phase));
        for (int face = -1; face <= 1; face += 2) {
            float z = face * (HeartSwordModelGeometry.CORE_Z + 0.003F);
            mesh.color(0.35F, 0.70F, 1.0F);
            // Radial vertex alpha gives a soft halo, without an opaque rectangular sprite.
            for (int step = 0; step < GLOW_SEGMENTS; step++) {
                float angle0 = TAU * step / GLOW_SEGMENTS;
                float angle1 = TAU * (step + 1) / GLOW_SEGMENTS;
                mesh.quad(0.0F, y, z, 0.23F * breathe * strength,
                        Mth.cos(angle0) * glowRadius, y + Mth.sin(angle0) * glowRadius, z, 0.0F,
                        Mth.cos(angle1) * glowRadius, y + Mth.sin(angle1) * glowRadius, z, 0.0F,
                        0.0F, y, z, 0.23F * breathe * strength);
            }
            mesh.color(0.67F, 0.88F, 1.0F);
            diamond(mesh, 0.0F, y, z + face * 0.0005F,
                    radius * 0.48F, radius * 0.68F, 0.40F * breathe * strength);
            mesh.color(0.84F, 0.95F, 1.0F);
            diamond(mesh, 0.0F, y, z + face * 0.0008F,
                    radius * 0.19F, radius * 0.28F, 0.52F * breathe * strength);
        }
    }

    private static void crescents(Mesh mesh, float time, float strength) {
        // Match the two authored crescent tips and outer edges; these are light traces,
        // not a rotation of the model's physical floating guards.
        float outer = HeartSwordModelGeometry.GUARD_HALF_WIDTH + 0.002F;
        float inner = HeartSwordModelGeometry.CRESCENT_X - 0.017F;
        float radiusY = 0.046F;
        for (int face = -1; face <= 1; face += 2) {
            float z = face * (HeartSwordModelGeometry.HALF_DEPTH + 0.003F);
            for (int side = -1; side <= 1; side += 2) {
                for (int step = 0; step < CRESCENT_SEGMENTS; step++) {
                    float angle0 = (float) Math.PI * step / CRESCENT_SEGMENTS;
                    float angle1 = (float) Math.PI * (step + 1) / CRESCENT_SEGMENTS;
                    float x0 = side * (inner + (outer - inner) * Mth.sin(angle0));
                    float x1 = side * (inner + (outer - inner) * Mth.sin(angle1));
                    float y0 = HeartSwordModelGeometry.GUARD_Y + radiusY * Mth.cos(angle0);
                    float y1 = HeartSwordModelGeometry.GUARD_Y + radiusY * Mth.cos(angle1);
                    float a0 = (0.12F + 0.12F * square(0.5F + 0.5F
                            * Mth.sin(angle0 * 2.0F - time * 0.044F + side))) * strength;
                    float a1 = (0.12F + 0.12F * square(0.5F + 0.5F
                            * Mth.sin(angle1 * 2.0F - time * 0.044F + side))) * strength;
                    ribbon(mesh, x0, y0, z, x1, y1, z,
                            0.0027F, 0.0027F, a0, a1, 0.48F, 0.81F, 1.0F);
                }
            }
        }
    }

    private static void tip(Mesh mesh, float time, float strength) {
        float point = HeartSwordModelGeometry.MAX_Y;
        float bottom = point - 0.16F;
        float alpha = (0.17F + 0.04F * Mth.sin(time * 0.052F)) * strength;
        for (int face = -1; face <= 1; face += 2) {
            for (int side = -1; side <= 1; side += 2) {
                ribbon(mesh, side * 0.012F, bottom, face * 0.008F,
                        0.0F, point, 0.0F, 0.0019F, 0.0002F,
                        alpha, 0.0F, 0.64F, 0.87F, 1.0F);
            }
        }
    }

    private static void motes(Mesh mesh, float time, float strength) {
        float root = HeartSwordModelGeometry.BLADE_ROOT_Y + 0.06F;
        float span = HeartSwordModelGeometry.MAX_Y - root - 0.13F;
        // Exactly four tiny local crystals. They do not spawn, accumulate or enter the
        // world particle system, and their path stays close to the blade silhouette.
        for (int i = 0; i < 4; i++) {
            float travel = fraction(time * (0.00115F + i * 0.00007F) + i * 0.251F);
            float fade = smooth(travel * 7.0F) * smooth((1.0F - travel) * 7.0F);
            float wave = time * 0.009F + i * 2.4F;
            float side = (i & 1) == 0 ? -1.0F : 1.0F;
            float x = side * (0.037F + 0.012F * Mth.sin(wave));
            float y = root + span * travel;
            float z = Mth.cos(wave) * 0.024F;
            float size = 0.0024F + 0.0007F * (i % 3);
            float alpha = 0.34F * fade * strength;
            mesh.color(0.52F, 0.83F, 1.0F);
            diamond(mesh, x, y, z, size, size * 2.0F, alpha);
            // A crossed plane retains a small facet when the sword is viewed edge-on.
            mesh.quad(x, y - size * 2.0F, z, 0.0F,
                    x, y, z + size, alpha, x, y + size * 2.0F, z, 0.0F,
                    x, y, z - size, alpha);
        }
    }

    private static void ribbon(Mesh mesh, float x0, float y0, float z0, float x1, float y1, float z1,
            float width0, float width1, float alpha0, float alpha1, float red, float green, float blue) {
        float dx = x1 - x0, dy = y1 - y0;
        float inverse = 1.0F / Math.max(0.00001F, Mth.sqrt(dx * dx + dy * dy));
        float nx = dy * inverse, ny = -dx * inverse;
        mesh.color(red, green, blue);
        mesh.quad(x0 - nx * width0, y0 - ny * width0, z0, 0.0F,
                x1 - nx * width1, y1 - ny * width1, z1, 0.0F,
                x1, y1, z1, alpha1, x0, y0, z0, alpha0);
        mesh.quad(x0, y0, z0, alpha0, x1, y1, z1, alpha1,
                x1 + nx * width1, y1 + ny * width1, z1, 0.0F,
                x0 + nx * width0, y0 + ny * width0, z0, 0.0F);
    }

    private static void diamond(Mesh mesh, float x, float y, float z, float width, float height, float alpha) {
        mesh.quad(x, y - height, z, alpha * 0.30F, x + width, y, z, alpha,
                x, y + height, z, alpha * 0.30F, x - width, y, z, alpha);
    }

    private static float taper(float t) { return 1.0F - 0.96F * smooth((t - 0.66F) / 0.34F); }
    private static float square(float value) { return value * value; }
    private static float fraction(float value) { return value - Mth.floor(value); }
    private static float smooth(float value) {
        value = Mth.clamp(value, 0.0F, 1.0F);
        return value * value * (3.0F - 2.0F * value);
    }

    /** Reusable clipping scratch arrays keep the reveal front aligned with the OBJ. */
    private static final class Mesh {
        private final VertexConsumer out;
        private final Matrix4f pose;
        private final float limit;
        private final float[] first = new float[32];
        private final float[] second = new float[32];
        private float red, green, blue;

        Mesh(VertexConsumer out, Matrix4f pose, float limit) {
            this.out = out;
            this.pose = pose;
            this.limit = limit;
        }

        void color(float red, float green, float blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        void quad(float x0, float y0, float z0, float a0, float x1, float y1, float z1, float a1,
                float x2, float y2, float z2, float a2, float x3, float y3, float z3, float a3) {
            if (Math.min(Math.min(y0, y1), Math.min(y2, y3)) > limit) return;
            if (Math.max(Math.max(y0, y1), Math.max(y2, y3)) < HeartSwordModelGeometry.MIN_Y) return;
            set(0, x0, y0, z0, a0);
            set(1, x1, y1, z1, a1);
            set(2, x2, y2, z2, a2);
            set(3, x3, y3, z3, a3);
            int count = clip(first, 4, second, limit, true);
            if (count < 3) return;
            count = clip(second, count, first, HeartSwordModelGeometry.MIN_Y, false);
            if (count < 3) return;
            if (count == 4) {
                vertex(0); vertex(1); vertex(2); vertex(3);
            } else {
                for (int i = 1; i < count - 1; i++) {
                    vertex(0); vertex(i); vertex(i + 1); vertex(i + 1);
                }
            }
        }

        private void set(int index, float x, float y, float z, float alpha) {
            int base = index * 4;
            first[base] = x;
            first[base + 1] = y;
            first[base + 2] = z;
            first[base + 3] = alpha;
        }

        private static int clip(float[] source, int count, float[] target, float plane, boolean below) {
            int result = 0;
            for (int i = 0; i < count; i++) {
                int a = i * 4, b = ((i + 1) % count) * 4;
                float ay = source[a + 1], by = source[b + 1];
                boolean inA = below ? ay <= plane : ay >= plane;
                boolean inB = below ? by <= plane : by >= plane;
                if (inA) {
                    System.arraycopy(source, a, target, result * 4, 4);
                    result++;
                }
                if (inA != inB) {
                    float t = (plane - ay) / (by - ay);
                    for (int field = 0; field < 4; field++) {
                        target[result * 4 + field] = Mth.lerp(t, source[a + field], source[b + field]);
                    }
                    result++;
                }
            }
            return result;
        }

        private void vertex(int index) {
            int base = index * 4;
            out.vertex(pose, first[base], first[base + 1], first[base + 2])
                    .color(red, green, blue, first[base + 3]).endVertex();
        }
    }

    private HeartSwordVfx() { }
}
