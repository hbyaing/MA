package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.WuXingItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 人皇幡手持特效：旗帜中部（幡面星云区）持续逸散紫色星尘，爪心宝石处闪出白色星芒。
 *
 * 建模提示词接口备注：模型（models/item/human_emperor_banner.json）为静态烘焙物品模型，
 * 无法自带粒子发射器——「旗帜中部的粒子发射器」按提示词要求由本类在客户端逐 tick 模拟，
 * 锚点为持幡玩家手部前方；当前模型总高 72u，握点在 y≈24u，布面位于握点上方。
 * 粒子随准星射线方向生成，视觉上与幡面朝向同一目标。
 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HumanEmperorBannerFX {
    // 提示词指定的星云高光紫 RGB(150,30,200)，渐隐到幡底暗紫。
    private static final Vector3f NEBULA_FROM = new Vector3f(0.588F, 0.118F, 0.784F);
    private static final Vector3f NEBULA_TO = new Vector3f(0.31F, 0.02F, 0.5F);

    private HumanEmperorBannerFX() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (mc.level == null || player == null || player.isSpectator()) return;
        boolean main = holds(player.getMainHandItem());
        boolean off = holds(player.getOffhandItem());
        if (!main && !off) return;
        net.minecraft.util.RandomSource rand = mc.level.getRandom();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double side = main ? 0.42D : -0.42D;
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        // 旗面星尘带：以三分之一握点为基准，粒子落在连续布面中下段并向上飘。
        if (rand.nextInt(3) == 0) {
            double u = rand.nextDouble() * 1.05D;
            Vec3 p = eye.add(look.scale(0.45D)).add(right.scale(side + (rand.nextDouble() - 0.5D) * 0.6D))
                    .add(0, -0.62D + u, 0);
            mc.level.addParticle(new DustColorTransitionOptions(NEBULA_FROM, NEBULA_TO, 0.85F),
                    p.x, p.y, p.z,
                    (rand.nextDouble() - 0.5D) * 0.01D, 0.015D + rand.nextDouble() * 0.01D,
                    (rand.nextDouble() - 0.5D) * 0.01D);
        }
        // 宝冠宝石：偶发白色星芒，冠饰位于握点上方约半格。
        if (rand.nextInt(10) == 0) {
            Vec3 g = eye.add(look.scale(0.35D)).add(right.scale(side * 0.6D))
                    .add(0, -0.02D + rand.nextDouble() * 0.22D, 0);
            mc.level.addParticle(ParticleTypes.END_ROD, g.x, g.y, g.z, 0, 0.01D, 0);
        }
    }

    private static boolean holds(ItemStack stack) {
        return !stack.isEmpty() && stack.is(WuXingItems.HUMAN_EMPEROR_BANNER.get());
    }
}
