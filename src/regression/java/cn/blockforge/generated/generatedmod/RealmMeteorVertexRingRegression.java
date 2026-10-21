package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.realmflight.*;

/** Compare reused ring data with the previous per-vertex equations, including seam endpoints. */
public final class RealmMeteorVertexRingRegression {
    public static void run() {
        var ring=new RealmMeteorVertexRing(8);
        int checks=0;
        for(int style=1;style<=5;style++)for(int layer=0;layer<3;layer++)
        for(int segments:new int[]{4,6,8})for(boolean residual:new boolean[]{false,true})
        for(double behind:new double[]{-1.4,-.3,0,2,8,40})for(double phase:new double[]{0,127.99,256.1})
        for(double yaw:new double[]{0,.7,2.5}) {
            var tangent=new RealmMeteorPath.Vec(Math.cos(yaw),.2,Math.sin(yaw)).normalize();
            var side=tangent.cross(new RealmMeteorPath.Vec(0,1,0)).normalize();
            var up=side.cross(tangent).normalize();
            var frame=new RealmMeteorPath.Frame(new RealmMeteorPath.Vec(20000000.1,172,-19999999.7),tangent,side,up,10,257.35,.8);
            double radius=1.13,nose=2.4,headRadius=1.2;
            ring.prepare(frame,radius,behind,nose,headRadius,style,layer,phase,.18,-.24,segments,residual);
            for(int i=0;i<=segments;i++) {
                double angle=i*Math.PI*2/segments;
                var s=residual?RealmMeteorFlow.residualSection(angle,.18,-.24):RealmMeteorFlow.section(style,layer,behind,frame.arc(),phase,angle,.18,-.24);
                double nx=side.x()*s.normalSide()+up.x()*s.normalUp(),ny=side.y()*s.normalSide()+up.y()*s.normalUp(),nz=side.z()*s.normalSide()+up.z()*s.normalUp();
                if(behind<0&&!residual){double axial=-behind/(nose*nose),radial=radius/(headRadius*headRadius);nx=nx*radial+tangent.x()*axial;ny=ny*radial+tangent.y()*axial;nz=nz*radial+tangent.z()*axial;double length=Math.max(1e-9,Math.sqrt(nx*nx+ny*ny+nz*nz));nx/=length;ny/=length;nz/=length;}
                double[] expected={frame.position().x()+(side.x()*s.side()+up.x()*s.up())*radius,frame.position().y()+(side.y()*s.side()+up.y()*s.up())*radius,frame.position().z()+(side.z()*s.side()+up.z()*s.up())*radius,nx,ny,nz};
                double[] actual={ring.x(i),ring.y(i),ring.z(i),ring.nx(i),ring.ny(i),ring.nz(i)};
                for(int k=0;k<6;k++){checks++;if(Double.doubleToLongBits(expected[k])!=Double.doubleToLongBits(actual[k]))throw new AssertionError("Cached ring changed vertex " + style+"/"+layer+"/"+i+"/"+k);}
            }
        }
        System.out.println("PASS: "+checks+" bit-exact cached meteor vertex checks");
    }
}
