package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.alchemy.observation.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Reloadable labels/units/layout rows over real menu stacks, not a second alchemy engine. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class AlchemyObservationView {
    private static final ResourceLocation RESOURCE=ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,"alchemy/observation.json");
    private static volatile ObservationDefinitions definitions;
    public record Row(String label,String value,List<Component> detail){}
    @SubscribeEvent public static void listeners(RegisterClientReloadListenersEvent event){
        event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener)manager->{
            try(var stream=manager.getResourceOrThrow(RESOURCE).open()){
                var parsed=ObservationDefinitions.parse(new String(stream.readAllBytes(),StandardCharsets.UTF_8),MaterialObservation.providers());
                definitions=parsed;
            }catch(Exception error){com.mojang.logging.LogUtils.getLogger().error("Alchemy observation definitions rejected; retaining last valid catalog",error);}
        });
    }
    public static List<Row> rows(cn.blockforge.generated.generatedmod.AlchemyFurnaceMenu menu){
        if(definitions==null)return List.of(new Row("定义不可用","—",List.of(Component.literal("观察定义加载失败；没有推算或伪造参数。"))));
        var snapshot=MaterialObservation.read(menu.container(),cn.blockforge.generated.generatedmod.AlchemyFurnaceMenu.MATERIAL_SLOT_COUNT,definitions);
        var rows=new ArrayList<Row>();
        for(var p:definitions.parameters())rows.add(new Row(p.label(),String.format(java.util.Locale.ROOT,"%.0f",MaterialObservation.value(p.source(),snapshot))+p.unit(),List.of(Component.literal("参数："+p.id()),Component.literal("数据源："+p.source()),Component.literal("当前菜单公开原料；不是V5反应结果。"))));
        for(var t:snapshot.tags()){
            String name=Component.translatable(t.translationKey()).getString();
            rows.add(new Row("材·"+name,"存在",List.of(Component.literal("标签："+t.id()),Component.literal("类别："+definitions.tags().category()),Component.literal("来源药槽："+t.sourceSlots()),Component.literal(definitions.tags().strengthLabel()),Component.literal(definitions.tags().budgetLabel()),Component.literal("存在不等于强度，也不授予丹药能力。"))));
        }
        return rows;
    }
    private AlchemyObservationView(){}
}
