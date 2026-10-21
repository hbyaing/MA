package cn.blockforge.generated.generatedmod.realmflight;

/** Reusable world-space ring. Adjacent quads share computed coordinates and normals. */
public final class RealmMeteorVertexRing {
    private final double[] x,y,z,nx,ny,nz;
    public RealmMeteorVertexRing(int maximumSegments) {
        if(maximumSegments<3)throw new IllegalArgumentException("At least three radial segments required");
        int size=maximumSegments+1;
        x=new double[size];y=new double[size];z=new double[size];
        nx=new double[size];ny=new double[size];nz=new double[size];
    }
    public void prepare(RealmMeteorPath.Frame frame,double radius,double behind,double nose,double headRadius,
                        int style,int layer,double flowPhase,double bendSide,double bendUp,int segments,boolean residual) {
        if(segments<3||segments>=x.length)throw new IllegalArgumentException("Ring capacity exceeded");
        var side=frame.side();var up=frame.up();var center=frame.position();
        // Include the separate 2*pi endpoint to retain the previous seam's exact arithmetic.
        for(int i=0;i<=segments;i++) {
            double angle=i*Math.PI*2/segments;
            var section=residual?RealmMeteorFlow.residualSection(angle,bendSide,bendUp)
                    :RealmMeteorFlow.section(style,layer,behind,frame.arc(),flowPhase,angle,bendSide,bendUp);
            double normalX=side.x()*section.normalSide()+up.x()*section.normalUp();
            double normalY=side.y()*section.normalSide()+up.y()*section.normalUp();
            double normalZ=side.z()*section.normalSide()+up.z()*section.normalUp();
            if(behind<0&&!residual) {
                double axial=-behind/(nose*nose),radialNormal=radius/(headRadius*headRadius);
                normalX=normalX*radialNormal+frame.tangent().x()*axial;
                normalY=normalY*radialNormal+frame.tangent().y()*axial;
                normalZ=normalZ*radialNormal+frame.tangent().z()*axial;
                double length=Math.max(1e-9,Math.sqrt(normalX*normalX+normalY*normalY+normalZ*normalZ));
                normalX/=length;normalY/=length;normalZ/=length;
            }
            x[i]=center.x()+(side.x()*section.side()+up.x()*section.up())*radius;
            y[i]=center.y()+(side.y()*section.side()+up.y()*section.up())*radius;
            z[i]=center.z()+(side.z()*section.side()+up.z()*section.up())*radius;
            nx[i]=normalX;ny[i]=normalY;nz[i]=normalZ;
        }
    }
    public double x(int i){return x[i];} public double y(int i){return y[i];} public double z(int i){return z[i];}
    public double nx(int i){return nx[i];} public double ny(int i){return ny[i];} public double nz(int i){return nz[i];}
}
