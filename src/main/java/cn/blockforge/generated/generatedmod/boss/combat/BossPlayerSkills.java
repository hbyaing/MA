package cn.blockforge.generated.generatedmod.boss.combat;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.boss.BossVisualRegistry;
import cn.blockforge.generated.generatedmod.network.packet.boss.BossTransformMotionPacket;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/** Player casts use the same bounded server timelines as their original bosses. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class BossPlayerSkills {
    private static final Map<UUID,BossCombatController> CASTERS=new HashMap<>();
    private static final Map<UUID,Map<String,Long>> COOLDOWNS=new HashMap<>();
    public static final List<String> FORMS=java.util.stream.Stream.concat(BossVisualRegistry.TEST_ENTITY_IDS.stream(),java.util.stream.Stream.of("heart_demon")).map(id->"wuxingxiantu:"+id).toList();
    public static BossCombatCatalog.Profile profile(String form){if(!FORMS.contains(form))return null;String id=form.substring(form.indexOf(':')+1);return id.equals("heart_demon")?HeartDemonSkillPrograms.PROFILE:BossCombatCatalog.get(id);}
    public static String activeForm(ServerPlayer player){var data=GeneratedMod.data(player);return data!=null&&"qi_shi_er_bian".equals(data.wuxingxiantu$getActiveDivineArt())&&Arrays.asList(data.wuxingxiantu$getPossessedForms()).contains(data.wuxingxiantu$getActivePossessedForm())?data.wuxingxiantu$getActivePossessedForm():"";}
    public static BossCombatController encounter(ServerPlayer player){return CASTERS.get(player.getUUID());}
    public static boolean cast(ServerPlayer player,String skill){
        String form=activeForm(player);var profile=profile(form);
        if(profile==null||!player.isAlive()||player.isSpectator())return false;
        boolean basic=skill.equals(profile.id()+"_basic");
        if(!basic&&profile.skills().stream().noneMatch(s->s.id().equals(skill)))return false;
        boolean noCooldown=GeneratedMod.data(player).wuxingxiantu$hasCreationBlessing();
        var cooldowns=COOLDOWNS.computeIfAbsent(player.getUUID(),id->new HashMap<>());
        if(!noCooldown&&player.level().getGameTime()<cooldowns.getOrDefault(skill,0L)){if(!basic)player.displayClientMessage(Component.literal("该 Boss 技能尚未冷却。"),true);return false;}
        BossCombatController controller=CASTERS.get(player.getUUID());
        if(controller==null||controller.closed()){
            var entity=BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(form)).create(player.serverLevel());
            if(!(entity instanceof Mob proxy))return false;
            proxy.setPos(player.position());proxy.setNoAi(true);
            controller=BossCombatController.playerCast(proxy,player,profile);CASTERS.put(player.getUUID(),controller);
        }
        boolean result=controller.castPlayerSkill(skill,noCooldown);
        if(result){var definition=basic?BossBasicAttacks.get(profile.id()).skill(profile.id()):profile.skills().stream().filter(s->s.id().equals(skill)).findFirst().orElseThrow();cooldowns.put(skill,player.level().getGameTime()+definition.cooldown());}
        if(!result&&!basic)player.displayClientMessage(Component.literal("正在施法、处于破绽期或该技能尚未冷却。"),true);
        return result;
    }
    public static void motion(ServerPlayer player,String boss,String animation,int duration){if(player==null)return;Network.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->player),new BossTransformMotionPacket(player.getUUID(),"wuxingxiantu:"+boss,animation,player.level().getGameTime(),duration));}
    public static void clear(ServerPlayer player){var c=CASTERS.remove(player.getUUID());if(c!=null)c.close();}
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){if(event.phase==TickEvent.Phase.END&&event.player instanceof ServerPlayer player){var c=encounter(player);if(c!=null){c.tick();if(c.closed())CASTERS.remove(player.getUUID());}}}
    @SubscribeEvent public static void hurt(LivingHurtEvent event){if(event.getEntity() instanceof ServerPlayer player){var c=encounter(player);if(c!=null&&!c.closed())event.setAmount(c.incoming(event.getSource(),event.getAmount()));}}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer player){clear(player);COOLDOWNS.remove(player.getUUID());}}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event){if(event.getEntity() instanceof ServerPlayer player)clear(player);}
    @SubscribeEvent public static void tracking(PlayerEvent.StartTracking event){if(event.getEntity() instanceof ServerPlayer viewer&&event.getTarget() instanceof ServerPlayer player){var c=encounter(player);if(c!=null&&!c.closed()){var visual=(cn.blockforge.generated.generatedmod.boss.BossVisualEntity)c.boss;long duration=visual.previewUntil()-visual.previewStarted();if(!visual.previewAnimation().isEmpty()&&visual.previewUntil()>player.level().getGameTime())Network.sendToPlayer(viewer,new BossTransformMotionPacket(player.getUUID(),activeForm(player),visual.previewAnimation(),visual.previewStarted(),(int)duration));}}}
    @SubscribeEvent public static void stop(net.minecraftforge.event.server.ServerStoppingEvent event){for(var c:List.copyOf(CASTERS.values()))c.close();CASTERS.clear();COOLDOWNS.clear();}
    private BossPlayerSkills(){}
}
