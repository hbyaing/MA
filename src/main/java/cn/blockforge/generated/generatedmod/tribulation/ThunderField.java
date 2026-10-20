package cn.blockforge.generated.generatedmod.tribulation;
import cn.blockforge.generated.generatedmod.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
public final class ThunderField {
 public static void emit(ServerLevel level,LivingEntity owner,Vec3 end,int realm,boolean strong,boolean grounded){long event=level.random.nextLong();double height=Math.max(193,end.y+32);var p=new TribulationVisualPacket(owner.getUUID(),level.dimension().location().toString(),event,Math.max(0,Math.min(7,realm)),strong?5:4,1,level.random.nextInt(),level.getGameTime(),new Vec3(end.x,height,end.z),end,grounded);for(var watcher:level.players())if(watcher.position().distanceToSqr(end)<256*256)Network.sendToPlayer(watcher,p);level.playSound(null,net.minecraft.core.BlockPos.containing(end),net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER,net.minecraft.sounds.SoundSource.WEATHER,2,1);if(grounded)level.playSound(null,net.minecraft.core.BlockPos.containing(end),net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_IMPACT,net.minecraft.sounds.SoundSource.WEATHER,1,1);}
 public static boolean grounded(LivingEntity e){return !e.level().noCollision(e,e.getBoundingBox().move(0,-.12,0));}
}
