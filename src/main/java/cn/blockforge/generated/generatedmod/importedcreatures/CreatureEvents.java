package cn.blockforge.generated.generatedmod.importedcreatures;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.acquisition.*;
import cn.blockforge.generated.generatedmod.crafting.EquipmentCrafting;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class CreatureEvents {
    @SubscribeEvent public static void sources(AcquisitionSourcesEvent event) {
        event.register("imported-beasts",sink->{
            var evidence=new SourceEvidence("mob_drop",new ResourceLocation(GeneratedMod.MOD_ID,"black_boar_hunt"),
                    List.of("Player-credit kill of naturally spawned black_boar or black_boar_king in #wuxingxiantu:black_boar_habitat",
                            "No drops from revived Human Emperor Banner servants"),0,true,List.of());
            sink.add(ImportedCreatures.BLOOD.get(),evidence);sink.add(ImportedCreatures.HIDE.get(),evidence);
        });
    }
    @SubscribeEvent public static void attributes(ItemAttributeModifierEvent event) {
        ItemStack stack=event.getItemStack();int defense=BeastMaterialEffects.defense(stack);
        if(defense<=0||!EquipmentCrafting.eligible(stack))return;
        boolean slot=stack.getItem() instanceof ArmorItem armor?armor.getEquipmentSlot()==event.getSlotType():
                stack.getItem() instanceof SwordItem&&event.getSlotType()==EquipmentSlot.MAINHAND;
        if(!slot)return;
        UUID id=UUID.nameUUIDFromBytes(("myriad/beast_hide/"+event.getSlotType()).getBytes(StandardCharsets.UTF_8));
        event.addModifier(Attributes.ARMOR,new AttributeModifier(id,"Beast hide reinforcement",defense,AttributeModifier.Operation.ADDITION));
    }
    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        ItemStack stack=event.getItemStack();int defense=BeastMaterialEffects.defense(stack);
        if(defense>0)event.getToolTip().add(Component.translatable("beast.wuxingxiantu.defense_bonus",defense));
        if(stack.hasTag()&&stack.getTag().contains(BeastMaterialEffects.STONE))event.getToolTip().add(
                Component.translatable("beast.wuxingxiantu.stone_bonus",Math.min(3,stack.getTag().getCompound(BeastMaterialEffects.STONE).getInt("Amplifier")+1)));
    }
    private CreatureEvents() {}
}
