package cn.blockforge.generated.generatedmod.client.tribulation;
import cn.blockforge.generated.generatedmod.ModEffects;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.geom.ModelPart;
/** Final model pose overrides locomotion/use/swimming animations. */
public final class ParalysisPose {
 private static final java.util.Map<net.minecraft.world.entity.LivingEntity,Integer> START=new java.util.WeakHashMap<>();
 public static double age(net.minecraft.world.entity.LivingEntity p,double ticks){return ticks-START.computeIfAbsent(p,k->p.tickCount);}
 public static void cleanup(){START.keySet().removeIf(p->p.isRemoved()||!p.hasEffect(ModEffects.PARALYSIS.get()));}
 static double jerk(double t,long seed){double age=t-Math.floor(t/12)*12,d=age-.25;return d>=0&&d<6?Math.sin(d*Math.PI*1.1)*Math.exp(-d*.32):0;}
 static Vec3Transform transform(double t,long seed){return new Vec3Transform(t,seed);}
 record Vec3Transform(double t,long seed){net.minecraft.world.phys.Vec3 apply(net.minecraft.world.phys.Vec3 v,int region){
  double j=jerk(t,seed),side=LightningMath.unit(seed+(long)Math.floor(t/12)*104729,32)>.5?1:-1,x,z;net.minecraft.world.phys.Vec3 pivot;
  if(region==7){pivot=new net.minecraft.world.phys.Vec3(0,.47,0);x=.24+j*.22;z=side*j*.18;}
  else if(region==3||region==4){double sign=region==3?-1:1;pivot=new net.minecraft.world.phys.Vec3(sign*.43,.47,0);x=-.55+j*(sign==side?.34:.17);z=sign*(.28+j*.16)+Math.sin(t*2.7)*.012;}
  else if(region>=5){double sign=region==5?-1:1;pivot=new net.minecraft.world.phys.Vec3(sign*.16,-.98,0);x=.08+j*.07;z=sign*j*.022;}
  else{pivot=new net.minecraft.world.phys.Vec3(0,-.24,0);x=.12+j*.08;z=side*j*.06;}
  var p=v.subtract(pivot);double y=p.y*Math.cos(x)-p.z*Math.sin(x),zz=p.y*Math.sin(x)+p.z*Math.cos(x);return new net.minecraft.world.phys.Vec3(p.x*Math.cos(z)-y*Math.sin(z),p.x*Math.sin(z)+y*Math.cos(z),zz).add(pivot);
 }}
 public static boolean apply(PlayerModel<AbstractClientPlayer> m,AbstractClientPlayer p,float ticks){
  if(!p.hasEffect(ModEffects.PARALYSIS.get())){START.remove(p);return false;}
  float age=(float)age(p,ticks);m.crouching=false;m.riding=false;m.swimAmount=0;m.attackTime=0;
  m.head.setPos(0,0,0);m.body.setPos(0,0,0);m.leftArm.setPos(5,2,0);m.rightArm.setPos(-5,2,0);m.leftLeg.setPos(1.9F,12,0);m.rightLeg.setPos(-1.9F,12,0);
   double j=jerk(age,p.getUUID().hashCode()),side=LightningMath.unit(p.getUUID().hashCode()+(long)Math.floor(age/12)*104729,32)>.5?1:-1;
   for(var part:new ModelPart[]{m.head,m.body,m.leftArm,m.rightArm,m.leftLeg,m.rightLeg}){part.xRot=0;part.yRot=0;part.zRot=0;}
   m.head.xRot=(float)(.24+j*.22);m.head.zRot=(float)(side*j*.18);m.body.xRot=(float)(.12+j*.08);m.body.zRot=(float)(side*j*.06);
   m.leftArm.xRot=(float)(-.55+j*(side<0?.34:.17));m.rightArm.xRot=(float)(-.55+j*(side>0?.34:.17));m.leftArm.zRot=(float)(-.28-j*.16);m.rightArm.zRot=(float)(.28+j*.16);
   m.leftLeg.xRot=m.rightLeg.xRot=(float)(.08+j*.07);m.leftLeg.zRot=(float)(-j*.022);m.rightLeg.zRot=(float)(j*.022);
   m.hat.copyFrom(m.head);m.jacket.copyFrom(m.body);m.leftSleeve.copyFrom(m.leftArm);m.rightSleeve.copyFrom(m.rightArm);m.leftPants.copyFrom(m.leftLeg);m.rightPants.copyFrom(m.rightLeg);
  return true;
 }
}
