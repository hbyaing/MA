package cn.blockforge.generated.generatedmod.artifacts;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.crafting.*;
import cn.blockforge.generated.generatedmod.economy.SpiritCurrency;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 来源辅材和补缺接入旧器台；统一职业、异火、货币和唯一主人。 */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class ArtifactInfusion {
    @SubscribeEvent(priority=EventPriority.HIGH) public static void use(PlayerInteractEvent.RightClickBlock event){
        var player=event.getEntity();if(event.getHand()!=InteractionHand.MAIN_HAND||!player.isShiftKeyDown()
                ||!RefiningForgeBlock.isCore(event.getLevel().getBlockState(event.getPos())))return;
        var main=player.getMainHandItem();var off=player.getOffhandItem();
        boolean auxiliary=off.getItem() instanceof ArtifactBootstrap.AuxiliaryItem;
        boolean repair=off.is(Items.IRON_INGOT)&&ArtifactData.artifact(main);
        if((!auxiliary&&!repair)||!EquipmentCrafting.eligible(main))return;
        event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
        if(player instanceof ServerPlayer server)apply(server,event.getPos());
    }
    public static boolean apply(ServerPlayer player,BlockPos core){
        var stack=player.getMainHandItem();var staged=player.getOffhandItem();var level=player.serverLevel();
        if(cn.blockforge.generated.generatedmod.ParalysisControl.blocked(player)||!player.isAlive()||player.isSpectator()||!player.isShiftKeyDown()||!EquipmentCrafting.eligible(stack)
                ||!EquipmentBond.canUse(player,stack)||player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(core))>64
                ||!RefiningForgeBlock.isStructureComplete(level,core)
                ||!(level.getBlockEntity(core) instanceof RefiningForgeBlockEntity forge)||forge.isCrafting()||forge.hasPendingReward())return deny(player,"station");
        boolean repair=staged.is(Items.IRON_INGOT)&&ArtifactData.artifact(stack);
        ArtifactAux auxiliary=staged.getItem() instanceof ArtifactBootstrap.AuxiliaryItem item?item.aux:null;
        if(auxiliary==null&&!repair)return false;
        if(auxiliary!=null&&!ArtifactPropertyPolicy.ready(auxiliary.trait))return deny(player,"policy_pending");
        if(auxiliary!=null&&!ArtifactPropertyPolicy.candidateTrait(auxiliary.trait)&&!ArtifactData.artifact(stack))return deny(player,"artifact_required");
        var craft=EquipmentCrafting.read(stack);if(craft.contains("Pending"))return deny(player,"pending");
        if(auxiliary!=null&&ArtifactPropertyPolicy.candidateTrait(auxiliary.trait)&&!ArtifactPropertyPolicy.canCandidate(stack,auxiliary)) {
            player.displayClientMessage(Component.literal("锋锐辅材仅供武器；坚固辅材需要可损耗耐久的装备。"),true);return false;
        }
        if(repair&&craft.getInt("ArtifactDefects")<=0&&stack.getDamageValue()<=0)return deny(player,"perfect");
        int fire=RefiningForgeBlock.fireTier(level,core),required=repair?1:auxiliary.fire();
        var data=GeneratedMod.data(player);var profession=PlayerCrafts.get(player.server).profession(player.getUUID(),"refining");
        if(!profession.getBoolean("Learned")||PlayerCrafts.rank(player,"refining")<required-1)return deny(player,"profession");
        if(fire<required||!ProgressionGate.canUseFire(player,fire)
                ||!ProgressionGate.canUseRefiningForge(player,RefiningForgeBlock.temperatureForTier(required)))return deny(player,"fire");
        long now=level.getGameTime();if(craft.contains("ArtifactWorkReady")&&now<craft.getLong("ArtifactWorkReady"))return deny(player,"cooldown");
        int aura=repair?10:10+required*2,stones=repair?2:3+required;
        if(data==null||data.wuxingxiantu$getAura()<aura||SpiritCurrency.value(player.getInventory().items)<stones)return deny(player,"cost");
        if(!SpiritCurrency.pay(player.getInventory(),stones))return deny(player,"cost");
        // SpiritCurrency替换主背包副本；重新取得持有对象后才写属性，避免扣费后改到游离副本。
        stack=player.getMainHandItem();staged=player.getOffhandItem();
        if(repair){
            var write=EquipmentCrafting.write(stack);write.putInt("ArtifactDefects",Math.max(0,write.getInt("ArtifactDefects")-1));stack.setDamageValue(0);
        }else ArtifactData.infuse(stack,auxiliary);
        staged.shrink(1);data.wuxingxiantu$setAura(data.wuxingxiantu$getAura()-aura);
        EquipmentCrafting.write(stack).putLong("ArtifactWorkReady",now+60);
        PlayerCrafts.get(player.server).receipt(player.getUUID(),"refining",stack,Math.min(4,EquipmentCrafting.refine(stack)/2));
        player.getInventory().setChanged();player.containerMenu.broadcastChanges();GeneratedMod.sync(player);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.artifacts."+(repair?"repaired":ArtifactPropertyPolicy.candidateTrait(auxiliary.trait)?"candidate":"infused")),true);
        return true;
    }
    private static boolean deny(ServerPlayer player,String reason){player.displayClientMessage(Component.translatable("message.wuxingxiantu.artifacts."+reason),true);return false;}
    private ArtifactInfusion(){}
}
