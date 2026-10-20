package cn.blockforge.generated.generatedmod.client.arts;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import java.util.HashMap;
import java.util.Map;

/** V2 source textures, nearest atlas sampling, world anchored planes, short bounded lifetime. */
public final class ArtMaterialParticle extends TextureSheetParticle {
    private static final Map<String, SpriteSet> SPRITES = new HashMap<>();
    private final float halfWidth, halfHeight;
    private final Vec3 direction;
    private final boolean ground;
    private final float rotation;

    private ArtMaterialParticle(ClientLevel level, String material, Vec3 position, Vec3 velocity,
                                double width, double height, int lifetime, int color, boolean ground,
                                Vec3 direction, float rotation) {
        super(level, position.x, position.y, position.z);
        pickSprite(SPRITES.get(material));
        this.xd = velocity.x; this.yd = velocity.y; this.zd = velocity.z;
        this.halfWidth = (float) width * .5F; this.halfHeight = (float) height * .5F;
        this.direction = direction; this.ground = ground; this.rotation = rotation;
        this.lifetime = Math.min(100, Math.max(4, lifetime));
        this.hasPhysics = false; this.friction = 1; this.gravity = 0;
        setColor((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F);
    }
    public static void spawn(ClientLevel level, String material, Vec3 position, Vec3 velocity,
                      double width, double height, int lifetime, int color, boolean ground,
                      Vec3 direction, float rotation) {
        if (SPRITES.containsKey(material)) net.minecraft.client.Minecraft.getInstance().particleEngine.add(
                new ArtMaterialParticle(level, material, position, velocity, width, height, lifetime, color, ground, direction, rotation));
    }
    @Override public void tick() {
        super.tick();
        alpha = Math.min(1F, (lifetime - age) / (float) Math.max(3, lifetime / 3));
    }
    @Override public int getLightColor(float partialTick) { return LightTexture.FULL_BRIGHT; }
    @Override public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
    @Override public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Vec3 center = new Vec3(Mth.lerp(partialTick, xo, x), Mth.lerp(partialTick, yo, y), Mth.lerp(partialTick, zo, z))
                .subtract(camera.getPosition());
        Vec3 right, up;
        if (ground) { right = new Vec3(1, 0, 0); up = new Vec3(0, 0, 1); }
        else if (direction.lengthSqr() > .001) {
            up = direction.normalize();right=directionRight(up,camera.getPosition().subtract(new Vec3(x,y,z)));
        } else {
            Vector3f r = new Vector3f(1, 0, 0).rotate(camera.rotation());
            Vector3f u = new Vector3f(0, 1, 0).rotate(camera.rotation());
            right = new Vec3(r.x, r.y, r.z); up = new Vec3(u.x, u.y, u.z);
        }
        double c = Math.cos(rotation), s = Math.sin(rotation);
        Vec3 axisX = right.scale(c).add(up.scale(s)).scale(halfWidth);
        Vec3 axisY = up.scale(c).subtract(right.scale(s)).scale(halfHeight);
        vertex(buffer, center.subtract(axisX).subtract(axisY), getU1(), getV1());
        vertex(buffer, center.subtract(axisX).add(axisY), getU1(), getV0());
        vertex(buffer, center.add(axisX).add(axisY), getU0(), getV0());
        vertex(buffer, center.add(axisX).subtract(axisY), getU0(), getV1());
    }
    public static Vec3 directionRight(Vec3 up,Vec3 view) {
        Vec3 right=up.cross(view).normalize();
        if(right.lengthSqr()<.01)right=up.cross(Math.abs(up.x)<.8?new Vec3(1,0,0):new Vec3(0,1,0)).normalize();
        return right;
    }
    private void vertex(VertexConsumer b, Vec3 p, float u, float v) {
        b.vertex(p.x, p.y, p.z).uv(u, v).color(rCol, gCol, bCol, alpha).uv2(LightTexture.FULL_BRIGHT).endVertex();
    }
    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final String material;
        public Provider(String material, SpriteSet sprites) { this.material = material; SPRITES.put(material, sprites); }
        @Override public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                                 double vx, double vy, double vz) {
            return new ArtMaterialParticle(level, material, new Vec3(x,y,z), new Vec3(vx,vy,vz), .2,.2,12,0xFFFFFF,false,Vec3.ZERO,0);
        }
    }
}
