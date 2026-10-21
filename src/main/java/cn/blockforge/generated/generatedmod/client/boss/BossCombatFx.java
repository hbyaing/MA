package cn.blockforge.generated.generatedmod.client.boss;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.boss.BossVisualEntity;
import cn.blockforge.generated.generatedmod.client.TrailShaderBridge;
import cn.blockforge.generated.generatedmod.network.packet.boss.BossCombatFxPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/**
 * Bounded client-only telegraphs. They describe server hit volumes and never decide hits.
 * Ground geometry keeps depth testing; shader packs use the existing captured-depth bridge.
 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BossCombatFx {
    private static final int MAX_EFFECTS = 128, MAX_PARTICLES_PER_TICK = 32;
    private static final double DRAW_DISTANCE_SQUARED = 192 * 192;
    private static final BufferBuilder BUFFER = new BufferBuilder(262144);
    private static final Map<Key, Effect> EFFECTS = new LinkedHashMap<>();
    private static ClientLevel trackedLevel;
    private static long nextSoundTick;
    private static BufferBuilder.RenderedBuffer pending;
    private static Matrix4f pendingProjection;

    private record Key(int boss, int effect) {}

    private static final class Effect {
        final BossCombatFxPacket packet;
        final UUID owner;
        final long born;
        boolean impacted;

        Effect(BossCombatFxPacket packet, Entity owner, long born) {
            this.packet = packet;
            this.owner = owner.getUUID();
            this.born = born;
        }

        double age(long now, float partial) { return Math.max(0, now - born + partial); }
    }

    /** Immutable, render-only data for the separate ability caption HUD. */
    record Cue(String label, int color, int windup, int lifetime, double age) {}

    public static void accept(BossCombatFxPacket packet) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null || !packet.valid()) return;
        changeLevel(client.level);
        if (packet.shape() == BossCombatFxPacket.CLEAR) {
            EFFECTS.entrySet().removeIf(entry -> entry.getKey().boss() == packet.bossId());
            return;
        }
        if(packet.shape()==BossCombatFxPacket.REMOVE){EFFECTS.remove(new Key(packet.bossId(),packet.effectId()));return;}
        Entity owner = client.level.getEntity(packet.bossId());
        // Capture UUID as well as id so an entity id reused after unloading cannot inherit a telegraph.
        if (!(owner instanceof BossVisualEntity)&&!(owner instanceof net.minecraft.world.entity.player.Player))return;
        if (!owner.isAlive() || owner.isRemoved()
                || owner.distanceToSqr(client.player) > DRAW_DISTANCE_SQUARED
                || owner.position().distanceToSqr(packet.start()) > 192 * 192) return;
        Key key = new Key(packet.bossId(), packet.effectId());
        EFFECTS.remove(key);
        if (EFFECTS.size() >= MAX_EFFECTS) EFFECTS.remove(EFFECTS.keySet().iterator().next());
        EFFECTS.put(key, new Effect(packet, owner, client.level.getGameTime()));
    }

    private static void changeLevel(ClientLevel level) {
        if (trackedLevel != level) {
            clear();
            trackedLevel = level;
        }
    }

    public static void clear() {
        EFFECTS.clear();
        trackedLevel = null;
        nextSoundTick = 0;
        if (pending != null) { pending.release(); pending = null; }
        pendingProjection = null;
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { clear(); }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) clear();
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null || !client.player.isAlive()) { clear(); return; }
        changeLevel(client.level);
        if (client.isPaused()) return;
        long now = client.level.getGameTime();
        int particleBudget = MAX_PARTICLES_PER_TICK;
        Iterator<Effect> iterator = EFFECTS.values().iterator();
        while (iterator.hasNext()) {
            Effect effect = iterator.next();
            BossCombatFxPacket p = effect.packet;
            Entity owner = client.level.getEntity(p.bossId());
            long age = now - effect.born;
            if (age < 0 || age >= p.lifetime() || owner == null || !owner.isAlive() || owner.isRemoved()
                    || !owner.getUUID().equals(effect.owner) || owner.distanceToSqr(client.player) > DRAW_DISTANCE_SQUARED) {
                iterator.remove();
                continue;
            }
            if (!effect.impacted && age >= p.windup()) {
                effect.impacted = true;
                if (p.shape() != BossCombatFxPacket.MARK && particleBudget > 0) {
                    int count = Math.min(8, particleBudget);
                    impact(client.level, p, count);
                    particleBudget -= count;
                    if (now >= nextSoundTick && client.player.distanceToSqr(p.start()) < 48 * 48) {
                        client.level.playLocalSound(p.start().x, p.start().y, p.start().z,
                                p.shape() == BossCombatFxPacket.LINE ? SoundEvents.TRIDENT_HIT_GROUND : SoundEvents.AMETHYST_BLOCK_CHIME,
                                SoundSource.HOSTILE, 0.32F, 0.85F + Math.floorMod(p.effectId(), 5) * 0.08F, false);
                        nextSoundTick = now + 8;
                    }
                }
            }
        }
    }

    private static void impact(ClientLevel level, BossCombatFxPacket p, int count) {
        Vector3f color = new Vector3f(((p.color() >>> 16) & 255) / 255F,
                ((p.color() >>> 8) & 255) / 255F, (p.color() & 255) / 255F);
        DustParticleOptions dust = new DustParticleOptions(color, 0.85F);
        for (int i = 0; i < count; i++) {
            double t = (i + 0.5) / count, angle = t * Math.PI * 2;
            Vec3 at;
            if (p.shape() == BossCombatFxPacket.LINE) at = p.start().lerp(p.end(), t);
            else if (p.shape() == BossCombatFxPacket.CONE) {
                Vec3 direction = flatDirection(p.start(), p.end());
                double heading = Math.atan2(direction.z, direction.x);
                angle = heading + Math.toRadians(p.width()) * (t - 0.5);
                at = p.start().add(Math.cos(angle) * p.radius() * 0.75, 0, Math.sin(angle) * p.radius() * 0.75);
            } else at = p.start().add(Math.cos(angle) * p.radius() * 0.8, 0, Math.sin(angle) * p.radius() * 0.8);
            level.addParticle(dust, at.x, at.y + 0.2, at.z, 0, 0.035, 0);
        }
    }

    static Cue cue(float partial) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || trackedLevel != client.level) return null;
        long now = client.level.getGameTime();
        Effect selected = null;
        for (Effect effect : EFFECTS.values()) {
            BossCombatFxPacket p = effect.packet;
            if (p.label().isBlank() || now - effect.born >= (p.windup() > 0 ? p.windup() + 12 : 45)) continue;
            if (selected == null || effect.born > selected.born
                    || (effect.born == selected.born && p.windup() > selected.packet.windup())) selected = effect;
        }
        if (selected == null) return null;
        BossCombatFxPacket p = selected.packet;
        return new Cue(p.label(), p.color(), p.windup(), p.lifetime(), selected.age(now, partial));
    }

    @SubscribeEvent
    public static void frame(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) submit();
        else if (pending != null) { pending.release(); pending = null; }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void beforeHud(RenderGuiEvent.Pre event) {
        if (pending != null) { event.getGuiGraphics().flush(); submit(); }
    }

    private static void submit() {
        if (pending == null) return;
        BufferBuilder.RenderedBuffer mesh = pending;
        pending = null;
        TrailShaderBridge.draw(mesh, pendingProjection);
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null || trackedLevel != client.level || EFFECTS.isEmpty()) return;
        long now = client.level.getGameTime();
        Vec3 camera = event.getCamera().getPosition();
        Mesh mesh = new Mesh(event.getPoseStack().last().pose(), camera);
        BUFFER.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        try {
            for (Effect effect : EFFECTS.values()) {
                BossCombatFxPacket p = effect.packet;
                if (camera.distanceToSqr(p.start()) > DRAW_DISTANCE_SQUARED) continue;
                double age = effect.age(now, event.getPartialTick());
                if (age >= p.lifetime()) continue;
                AABB bounds = new AABB(p.start(), p.shape() == BossCombatFxPacket.LINE ? p.end() : p.start())
                        .inflate(Math.max(p.radius(), p.shape() == BossCombatFxPacket.CONE ? 1 : p.width()) + 3);
                if (!event.getFrustum().isVisible(bounds)) continue;
                draw(mesh, p, age);
            }
            BufferBuilder.RenderedBuffer built = BUFFER.end();
            if (TrailShaderBridge.shaderPackDetected() && TrailShaderBridge.captureDepth()) {
                if (pending != null) pending.release();
                pending = built;
                pendingProjection = new Matrix4f(event.getProjectionMatrix()).mul(RenderSystem.getModelViewMatrix());
            } else drawNormal(built);
        } finally {
            if (BUFFER.building()) BUFFER.end().release();
        }
    }

    private static void draw(Mesh mesh, BossCombatFxPacket p, double age) {
        boolean winding = p.windup() > 0 && age < p.windup();
        double progress = p.windup() == 0 ? 1 : Math.min(1, age / p.windup());
        float fade = (float) Math.min(1, (p.lifetime() - age) / 8);
        // Slow breathing and a moving countdown edge remain legible without rapid flashing.
        float pulse = (float) (0.87 + 0.13 * Math.sin(age * 0.16));
        float fill = (winding ? 0.10F + (float) progress * 0.06F : 0.27F) * fade;
        float edge = (winding ? 0.78F : 0.94F) * fade * pulse;
        Vec3 at = p.start().add(0, 0.045, 0);
        int color = p.color();
        int accent = mix(color, 0xFFF0CD, 0.4F);
        double stroke = Math.min(0.18, Math.max(0.065, p.radius() * 0.012));
        switch (p.shape()) {
            case BossCombatFxPacket.CIRCLE -> {
                mesh.sector(at, 0, p.radius(), 0, Math.PI * 2, color, fill);
                mesh.arc(at.add(0, 0.004, 0), p.radius(), stroke, 0, Math.PI * 2, color, edge);
                ticks(mesh, at, p.radius(), age * 0.009, color, edge, 12);
                if (winding) mesh.arc(at.add(0, 0.009, 0), p.radius() * (1 - progress), 0.07,
                        0, Math.PI * 2, accent, edge * 0.72F);
                if (color == BossCombatFxPacket.SAFE_COLOR) {
                    double size = Math.min(0.6, p.radius() * 0.3);
                    mesh.groundStroke(at.add(-size, 0.012, 0), at.add(size, 0.012, 0), 0.09, accent, edge);
                    mesh.groundStroke(at.add(0, 0.012, -size), at.add(0, 0.012, size), 0.09, accent, edge);
                } else dangerGlyph(mesh, at, Math.min(0.65, p.radius() * 0.23), accent, edge);
            }
            case BossCombatFxPacket.LINE -> line(mesh, p, at, age, winding, fill, edge, color, accent);
            case BossCombatFxPacket.RING -> {
                double inner = Math.max(0, p.radius() - p.width());
                mesh.sector(at, inner, p.radius(), 0, Math.PI * 2, color, fill);
                mesh.arc(at.add(0, 0.004, 0), p.radius(), stroke, 0, Math.PI * 2, color, edge);
                if (inner > 0) mesh.arc(at.add(0, 0.004, 0), inner, stroke, 0, Math.PI * 2, accent, edge);
                double scan = inner + (p.radius() - inner) * (winding ? progress : (age * 0.035) % 1);
                mesh.arc(at.add(0, 0.009, 0), scan, 0.08, 0, Math.PI * 2, accent, edge * 0.7F);
                ticks(mesh, at, p.radius(), 0, color, edge, 16);
            }
            case BossCombatFxPacket.CONE -> {
                Vec3 direction = flatDirection(p.start(), p.end());
                double heading = Math.atan2(direction.z, direction.x), halfAngle = Math.toRadians(p.width()) / 2;
                double a = heading - halfAngle, b = heading + halfAngle;
                mesh.sector(at, 0, p.radius(), a, b, color, fill);
                mesh.arc(at.add(0, 0.004, 0), p.radius(), stroke, a, b, color, edge);
                mesh.groundStroke(at, polar(at, p.radius(), a), stroke, color, edge);
                mesh.groundStroke(at, polar(at, p.radius(), b), stroke, color, edge);
                double scan = p.radius() * (winding ? progress : (age * 0.03) % 1);
                mesh.arc(at.add(0, 0.009, 0), scan, 0.075, a, b, accent, edge * 0.75F);
                for (int i = 1; i <= 3; i++) arrow(mesh, at.add(direction.scale(p.radius() * i / 4)), direction,
                        Math.min(0.5, p.radius() * 0.12), accent, edge);
            }
            case BossCombatFxPacket.MARK -> mark(mesh, at, p.radius(), age, color, accent, edge);
            default -> { }
        }
        if (!winding && age - p.windup() < 8 && p.shape() != BossCombatFxPacket.MARK) {
            float impulse = (float) (1 - (age - p.windup()) / 8);
            // A low, local pulse rather than a full-screen flash.
            mesh.arc(at.add(0, 0.08, 0), Math.max(0.2, p.radius() * (1 - impulse * 0.3)),
                    0.13, 0, Math.PI * 2, accent, impulse * 0.6F);
        }
    }

    private static void line(Mesh mesh, BossCombatFxPacket p, Vec3 a, double age,
                             boolean winding, float fill, float edge, int color, int accent) {
        Vec3 b = p.end().add(0, 0.045, 0), direction = flatDirection(a, b);
        double length = a.distanceTo(b), half = p.width() * 0.5;
        if (length < 0.01) return;
        Vec3 side = new Vec3(-direction.z, 0, direction.x).scale(half);
        mesh.quad(a.add(side), a.subtract(side), b.subtract(side), b.add(side), color, fill);
        mesh.groundStroke(a.add(side), b.add(side), 0.08, color, edge);
        mesh.groundStroke(a.subtract(side), b.subtract(side), 0.08, color, edge);
        mesh.groundStroke(a.add(side), a.subtract(side), 0.065, color, edge);
        mesh.groundStroke(b.add(side), b.subtract(side), 0.065, color, edge);
        int steps = Math.max(1, Math.min(18, (int) (length / 1.8)));
        for (int i = 0; i < steps; i++) {
            double t = (i + (age * 0.035) % 1) / steps;
            arrow(mesh, a.lerp(b, t).add(0, 0.007, 0), direction, Math.min(0.6, half * 0.65), accent, edge * 0.75F);
        }
        if (!winding) {
            // The impact beam is low and translucent so it does not cover the next ground warning.
            Vec3 highA = a.add(0, 0.22, 0), highB = b.add(0, 0.22, 0);
            mesh.ribbon(highA, highB, Math.min(0.16, half * 0.3), accent, edge * 0.7F);
        }
    }

    private static void mark(Mesh mesh, Vec3 at, float radius, double age, int color, int accent, float alpha) {
        double size = Math.max(0.28, Math.min(1.35, radius * 0.6));
        mesh.arc(at, radius, 0.08, 0, Math.PI * 2, color, alpha * 0.75F);
        mesh.arc(at.add(0, 0.015, 0), radius * 0.8, 0.035, age * 0.014, age * 0.014 + Math.PI * 1.5, accent, alpha * 0.6F);
        Vec3 center = at.add(0, 0.9 + Math.sin(age * 0.045) * 0.1, 0);
        Vec3 top = center.add(0, size * 1.35, 0), bottom = center.add(0, -size * 0.8, 0);
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3 + age * 0.016;
            Vec3 a = polar(center, size * 0.55, angle), b = polar(center, size * 0.55, angle + Math.PI / 3);
            mesh.triangle(top, a, b, i % 2 == 0 ? color : accent, alpha * 0.22F);
            mesh.triangle(bottom, b, a, color, alpha * 0.15F);
            mesh.ribbon(top, a, 0.022, accent, alpha * 0.8F);
            mesh.ribbon(bottom, a, 0.018, color, alpha * 0.75F);
            mesh.ribbon(a, b, 0.02, accent, alpha * 0.6F);
            Vec3 rune = polar(at.add(0, 0.4 + Math.sin(age * 0.035 + i) * 0.12, 0), radius * 0.85, -angle);
            Vec3 offset = new Vec3(Math.cos(angle) * 0.1, 0, Math.sin(angle) * 0.1);
            mesh.ribbon(rune.add(0, 0.15, 0), rune.add(offset), 0.02, accent, alpha * 0.7F);
            mesh.ribbon(rune.add(offset), rune.add(0, -0.15, 0), 0.02, accent, alpha * 0.7F);
            mesh.ribbon(rune.add(0, -0.15, 0), rune.subtract(offset), 0.02, accent, alpha * 0.7F);
            mesh.ribbon(rune.subtract(offset), rune.add(0, 0.15, 0), 0.02, accent, alpha * 0.7F);
        }
        // A stable cross underneath makes the interactable visible without relying on hue.
        mesh.groundStroke(at.add(-size * 0.4, 0.015, 0), at.add(size * 0.4, 0.015, 0), 0.075, accent, alpha);
        mesh.groundStroke(at.add(0, 0.015, -size * 0.4), at.add(0, 0.015, size * 0.4), 0.075, accent, alpha);
    }

    private static void ticks(Mesh mesh, Vec3 at, double radius, double offset, int color, float alpha, int count) {
        for (int i = 0; i < count; i++) {
            double angle = i * Math.PI * 2 / count + offset;
            mesh.groundStroke(polar(at.add(0, 0.007, 0), radius * 0.94, angle),
                    polar(at.add(0, 0.007, 0), radius * 0.82, angle), 0.065, color, alpha * 0.8F);
        }
    }

    private static void dangerGlyph(Mesh mesh, Vec3 at, double size, int color, float alpha) {
        Vec3 a = at.add(0, 0.012, -size), b = at.add(size * 0.9, 0.012, size * 0.6), c = at.add(-size * 0.9, 0.012, size * 0.6);
        mesh.groundStroke(a, b, 0.055, color, alpha * 0.65F);
        mesh.groundStroke(b, c, 0.055, color, alpha * 0.65F);
        mesh.groundStroke(c, a, 0.055, color, alpha * 0.65F);
    }

    private static void arrow(Mesh mesh, Vec3 at, Vec3 direction, double size, int color, float alpha) {
        Vec3 side = new Vec3(-direction.z, 0, direction.x).scale(size * 0.65);
        Vec3 tip = at.add(direction.scale(size * 0.55)), tail = at.subtract(direction.scale(size * 0.45));
        mesh.groundStroke(tail.add(side), tip, 0.055, color, alpha);
        mesh.groundStroke(tail.subtract(side), tip, 0.055, color, alpha);
    }

    private static Vec3 flatDirection(Vec3 a, Vec3 b) {
        Vec3 delta = new Vec3(b.x - a.x, 0, b.z - a.z);
        return delta.lengthSqr() < 1.0E-8 ? new Vec3(0, 0, 1) : delta.normalize();
    }

    private static Vec3 polar(Vec3 center, double radius, double angle) {
        return center.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
    }

    private static int mix(int a, int b, float t) {
        int r = (int) Mth.lerp(t, (a >>> 16) & 255, (b >>> 16) & 255);
        int g = (int) Mth.lerp(t, (a >>> 8) & 255, (b >>> 8) & 255);
        int blue = (int) Mth.lerp(t, a & 255, b & 255);
        return (r << 16) | (g << 8) | blue;
    }

    private static void drawNormal(BufferBuilder.RenderedBuffer mesh) {
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND), depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE), mask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int src = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB), dst = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int srcA = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA), dstA = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        var shader = RenderSystem.getShader();
        float[] color = RenderSystem.getShaderColor().clone();
        try {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferUploader.drawWithShader(mesh);
        } finally {
            RenderSystem.setShader(() -> shader);
            RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
            RenderSystem.depthMask(mask);
            if (depth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
            if (cull) RenderSystem.enableCull(); else RenderSystem.disableCull();
            RenderSystem.blendFuncSeparate(src, dst, srcA, dstA);
            if (blend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
        }
    }

    private static final class Mesh {
        private final Matrix4f matrix;
        private final Vec3 camera;

        Mesh(Matrix4f matrix, Vec3 camera) { this.matrix = matrix; this.camera = camera; }

        void vertex(Vec3 point, int color, float alpha) {
            // Subtract before converting to float, preserving precision in distant personal instances.
            Vec3 p = point.subtract(camera);
            BUFFER.vertex(matrix, (float) p.x, (float) p.y, (float) p.z)
                    .color((color >>> 16) & 255, (color >>> 8) & 255, color & 255, (int) (255 * Mth.clamp(alpha, 0, 1)))
                    .endVertex();
        }

        void triangle(Vec3 a, Vec3 b, Vec3 c, int color, float alpha) {
            vertex(a, color, alpha); vertex(b, color, alpha); vertex(c, color, alpha);
        }

        void quad(Vec3 a, Vec3 b, Vec3 c, Vec3 d, int color, float alpha) {
            triangle(a, b, c, color, alpha); triangle(a, c, d, color, alpha);
        }

        void sector(Vec3 center, double inner, double outer, double start, double end, int color, float alpha) {
            if (outer < 0.005 || end <= start) return;
            int steps = Math.max(3, Math.min(72, (int) Math.ceil((end - start) * Math.max(5, outer * 1.5))));
            for (int i = 0; i < steps; i++) {
                double a = start + (end - start) * i / steps, b = start + (end - start) * (i + 1) / steps;
                quad(polar(center, inner, a), polar(center, outer, a), polar(center, outer, b), polar(center, inner, b), color, alpha);
            }
        }

        void arc(Vec3 center, double radius, double width, double start, double end, int color, float alpha) {
            sector(center, Math.max(0, radius - width * 0.5), radius + width * 0.5, start, end, color, alpha);
        }

        void groundStroke(Vec3 a, Vec3 b, double width, int color, float alpha) {
            Vec3 d = b.subtract(a);
            Vec3 side = new Vec3(-d.z, 0, d.x).normalize().scale(width * 0.5);
            quad(a.add(side), a.subtract(side), b.subtract(side), b.add(side), color, alpha);
        }

        void ribbon(Vec3 a, Vec3 b, double width, int color, float alpha) {
            Vec3 side = b.subtract(a).cross(camera.subtract(a)).normalize().scale(width * 0.5);
            quad(a.add(side), a.subtract(side), b.subtract(side), b.add(side), color, alpha);
        }
    }

    private BossCombatFx() {}
}
