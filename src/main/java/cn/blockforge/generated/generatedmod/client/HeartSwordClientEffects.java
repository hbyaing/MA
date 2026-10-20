package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.balance.HeartSwordTimeline;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RenderHandEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** 心剑动作的客户端表现层；服务端包到达前不创建物品、不扣灵力。 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = net.minecraftforge.api.distmarker.Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HeartSwordClientEffects {
    private static final Map<Integer, Animation> ANIMATIONS = new HashMap<>();
    private static net.minecraft.client.multiplayer.ClientLevel animationLevel;

    private static void checkLevel() {
        var level = Minecraft.getInstance().level;
        if (animationLevel != level) { ANIMATIONS.clear(); animationLevel = level; }
    }

    private record Animation(byte action, long startedAt, ItemStack sword, HumanoidArm arm) {}
    private HeartSwordClientEffects() {}

    public static void accept(cn.blockforge.generated.generatedmod.network.packet.heartsword.HeartSwordVisualPacket packet) {
        checkLevel();
        Minecraft client = Minecraft.getInstance();
        if (packet.action() < 0 || HeartSwordTimeline.duration(packet.action()) == 0) { ANIMATIONS.remove(packet.entityId()); return; }
        long now = client.level == null ? 0L : client.level.getGameTime();
        var entity=client.level==null?null:client.level.getEntity(packet.entityId());
        var living=entity instanceof LivingEntity value?value:null;
        ANIMATIONS.put(packet.entityId(), new Animation(packet.action(), now,
                living==null?ItemStack.EMPTY:living.getMainHandItem().copy(),
                living==null?HumanoidArm.RIGHT:living.getMainArm()));
    }
    private static Animation animation(Entity entity) { return entity == null ? null : ANIMATIONS.get(entity.getId()); }
    public static ItemStack sword(LivingEntity entity) {
        var animation=animation(entity);
        return animation==null||animation.sword().isEmpty()?entity.getMainHandItem():animation.sword();
    }
    public static boolean leftHanded(LivingEntity entity) {
        var animation=animation(entity);
        return (animation==null?entity.getMainArm():animation.arm())==HumanoidArm.LEFT;
    }

    public static int action(Entity entity) {
        Animation a = animation(entity);
        return a == null ? -1 : a.action();
    }
    public static float progress(Entity entity, float partial) {
        Animation a = animation(entity);
        if (a == null) return 0;
        int duration = HeartSwordTimeline.duration(a.action());
        return Mth.clamp((entity.level().getGameTime() - a.startedAt() + partial) / duration, 0, 1);
    }
    @SubscribeEvent public static void renderHand(RenderHandEvent event) { HeartSwordPose.firstPerson(event); }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        checkLevel();
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) { ANIMATIONS.clear(); return; }
        long now = client.level.getGameTime();
        Iterator<Map.Entry<Integer, Animation>> iterator = ANIMATIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Animation> entry = iterator.next();
            Entity entity = client.level.getEntity(entry.getKey());
            Animation animation = entry.getValue();
            int duration = HeartSwordTimeline.duration(animation.action());
            long age = now - animation.startedAt();
            if (entity == null || age < 0 || age >= duration) { iterator.remove(); continue; }
        }
    }
}
