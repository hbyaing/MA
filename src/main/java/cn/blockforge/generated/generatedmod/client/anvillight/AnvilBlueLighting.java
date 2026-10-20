/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cn.blockforge.generated.generatedmod.WuXingBlocks
 *  cn.blockforge.generated.generatedmod.XuanlingAnvilBlock
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RegisterShadersEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package cn.blockforge.generated.generatedmod.client.anvillight;

import cn.blockforge.generated.generatedmod.WuXingBlocks;
import cn.blockforge.generated.generatedmod.XuanlingAnvilBlock;
import cn.blockforge.generated.generatedmod.client.anvillight.AnvilRuneEmission;
import cn.blockforge.generated.generatedmod.client.anvillight.Lighting;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="wuxingxiantu", value={Dist.CLIENT})
public final class AnvilBlueLighting {
    private static final List<Vec3> LIGHTS = new ArrayList<Vec3>();
    private static Object world;
    private static int ticks;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != world) {
            LIGHTS.clear();
            world = mc.level;
            Lighting.release();
            AnvilRuneEmission.clear();
            ticks = 0;
        }
        if (mc.level == null || mc.player == null || ticks++ % 20 != 0) {
            return;
        }
        LIGHTS.clear();
        BlockPos c = mc.player.blockPosition();
        for (BlockPos p2 : BlockPos.betweenClosed((BlockPos)c.offset(-8, -5, -8), (BlockPos)c.offset(8, 5, 8))) {
            BlockState state = mc.level.getBlockState(p2);
            if (!state.is((Block)WuXingBlocks.XUANLING_ANVIL.get()) || (Integer)state.getValue((Property)XuanlingAnvilBlock.DAMAGE) >= 3) continue;
            LIGHTS.add(Vec3.atCenterOf((Vec3i)p2));
        }
        LIGHTS.sort(Comparator.comparingDouble(p -> p.distanceToSqr(mc.player.position())));
        if (LIGHTS.size() > 4) {
            LIGHTS.subList(4, LIGHTS.size()).clear();
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent e) {
        if(e.getStage()==RenderLevelStageEvent.Stage.AFTER_PARTICLES){AnvilRuneEmission.prepare(e,LIGHTS);Lighting.prepare(e,LIGHTS);return;}
        if (e.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            AnvilRuneEmission.render(e, LIGHTS);
            return;
        }
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            return;
        }
        var level=Minecraft.getInstance().level;
        if(level==null)return;
        var active=LIGHTS.stream().filter(light->level.getBlockState(BlockPos.containing(light))
                .is(WuXingBlocks.XUANLING_ANVIL.get())).toList();
        Lighting.render(e,active);
    }
    @SubscribeEvent public static void post(net.minecraftforge.client.event.RenderGuiEvent.Pre e){e.getGuiGraphics().flush();Lighting.post();AnvilRuneEmission.post();}
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.HIGHEST)public static void frame(TickEvent.RenderTickEvent e){if(e.phase==TickEvent.Phase.START)AnvilRuneEmission.frameStart();else{Lighting.post();AnvilRuneEmission.post();}}
    public static int runeDraws(){return AnvilRuneEmission.draws();}

    @Mod.EventBusSubscriber(modid="wuxingxiantu", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class Shaders {
        @SubscribeEvent
        public static void register(RegisterShadersEvent e) {
            Lighting.release();
            Lighting.register(e);
            AnvilRuneEmission.register(e);
        }
    }
}

