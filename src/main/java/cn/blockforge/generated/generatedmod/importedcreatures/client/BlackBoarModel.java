package cn.blockforge.generated.generatedmod.importedcreatures.client;
import cn.blockforge.generated.generatedmod.importedcreatures.BlackBoarEntity;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
public final class BlackBoarModel extends PigModel<BlackBoarEntity> {
 public static final ModelLayerLocation LAYER=new ModelLayerLocation(new ResourceLocation("wuxingxiantu","black_boar"),"main");
 public BlackBoarModel(ModelPart root){super(root);}
 @Override public void setupAnim(BlackBoarEntity e,float limbSwing,float limbAmount,float age,float yaw,float pitch){super.setupAnim(e,limbSwing,limbAmount,age,yaw,pitch);head.getChild("tusk_rear_-1").visible=e.king();head.getChild("tusk_rear_1").visible=e.king();}
 public static LayerDefinition layer(){MeshDefinition mesh=PigModel.createBodyMesh(6,CubeDeformation.NONE);PartDefinition head=mesh.getRoot().getChild("head");
 head.addOrReplaceChild("snout",CubeListBuilder.create().texOffs(0,32).addBox(-2,0,-11,4,3,7).texOffs(22,32).addBox(-1.5F,.2F,-12,3,2,1),PartPose.ZERO);
 for(int side:new int[]{-1,1}){
  tusk(head,"tusk_front_"+side,side,2.0F,2.7F,-10F,1F);
  tusk(head,"tusk_rear_"+side,side,2.0F,-.1F,-10.35F,.65F);
  head.addOrReplaceChild("eye_"+side,CubeListBuilder.create().texOffs(48,32).addBox(-.75F,-.55F,-.09F,1.5F,1.1F,.09F),PartPose.offset(side*2.55F,-1.7F,-8.03F));
  head.addOrReplaceChild("pupil_"+side,CubeListBuilder.create().texOffs(56,32).addBox(-.2F,-.35F,-.03F,.4F,.7F,.03F),PartPose.offset(side*2.55F,-1.7F,-8.14F));
 }
 return LayerDefinition.create(mesh,64,64);}
 private static void tusk(PartDefinition head,String name,int side,float baseX,float baseY,float z,float scale){
  PartDefinition root=head.addOrReplaceChild(name,CubeListBuilder.create(),PartPose.ZERO);
  float[][] points={{0,0},{.65F,.3F},{1.25F,-.25F},{1.48F,-1.3F},{1.4F,-2.5F},{1.12F,-3.3F}};
  for(int i=0;i<points.length-1;i++){
   float x=side*(baseX+points[i][0]*scale),y=baseY+points[i][1]*scale;
   float dx=side*(points[i+1][0]-points[i][0])*scale,dy=(points[i+1][1]-points[i][1])*scale;
   float length=(float)Math.hypot(dx,dy),w=(.8F-i*.14F)*scale;
   root.addOrReplaceChild("curve_"+i,CubeListBuilder.create().texOffs(36,32).addBox(-w/2,-.03F,-w/2,w,length+.08F,w),PartPose.offsetAndRotation(x,y,z,0,0,(float)Math.atan2(-dx,dy)));
  }
 }

}
