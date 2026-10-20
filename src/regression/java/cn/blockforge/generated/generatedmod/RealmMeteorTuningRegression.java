package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.realmflight.*;

/** Actual production width, speed response, and history lifetime contracts. */
public final class RealmMeteorTuningRegression {
    private static int checks;
    private static void require(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void run(){
        for(int realm=4;realm<=8;realm++){
            double cruise=RealmFlightBalance.speed(realm,3,false)/20;
            double boost=RealmFlightBalance.speed(realm,3,true)/20;
            require(Math.abs(RealmMeteorTuning.targetLength(cruise,realm,3)-RealmFlightAppearance.profile(realm).length())<1e-9,"Stable cruise reaches the tier's normal length");
            require(Math.abs(RealmMeteorTuning.targetLength(boost,realm,3)-RealmFlightAppearance.profile(realm).boostLength())<1e-9,"Stable boost reaches the tier's boosted length");
            double radius=RealmFlightAppearance.profile(realm).radius();
            for(double length:new double[]{2,12,28,44,68}){
                double previous=Double.POSITIVE_INFINITY;
                for(int i=0;i<=1000;i++){
                    double behind=length*i/1000,width=RealmMeteorTuning.width(behind,length,radius);
                    require(Double.isFinite(width)&&width>=0&&width<=previous+1e-12,"Tube radius continuously narrows along history");
                    require(width*RealmMeteorTuning.LAYER_WIDTH[0]<=radius*1.13,"Wrapping wake stays within its bounded body envelope");
                    previous=width;
                }
                require(RealmMeteorTuning.width(length,length,radius)==0,"Terminal width reaches zero");
                require(Math.abs(RealmMeteorTuning.width(0,length,radius)/radius-RealmMeteorTuning.ROOT_WIDTH_RATIO)<1e-12,"Tail root envelopes the orb equator");
            }
            require(RealmMeteorTuning.wrapWidth(-radius*RealmMeteorTuning.ORB_STRETCH_MAX,28,radius)==0,"Forward nose closes smoothly");
            require(Math.abs(RealmMeteorTuning.wrapWidth(-1e-6,28,radius)-RealmMeteorTuning.width(0,28,radius))<1e-6,"Equator width is continuous");
            double previous=0;
            for(int i=0;i<500;i++){
                double target=RealmMeteorTuning.targetLength(i*.01,realm,3);
                require(target>=previous&&target<=RealmMeteorTuning.MAX_TRAIL_LENGTH,"Actual speed increases visual length smoothly within budget");previous=target;
            }
        }
        for(double motion:new double[]{0,.25,.5,.75,1}){
            double nose=RealmMeteorTuning.headLength(motion),radius=RealmMeteorTuning.headRadius(motion);
            require(RealmMeteorTuning.headWrapWidth(-nose,nose,radius)==0,"Head attachment closes at the actual volume pole");
            require(Math.abs(RealmMeteorTuning.headWrapWidth(0,nose,radius)-radius)<1e-12,"Head attachment meets the actual equator");
            double previous=0;
            for(int i=0;i<=1000;i++){
                double z=nose*(1-i/1000.0),r=RealmMeteorTuning.headWrapWidth(-z,nose,radius);
                require(r>=previous-1e-12&&Double.isFinite(r),"Forward flow expands continuously from pole to equator");
                require(Math.abs(z*z/(nose*nose)+r*r/(radius*radius)-1)<1e-9,"Flow skin lies on the same ellipsoid as the leading volume");
                previous=r;
            }
        }
        double value=1;
        for(int i=0;i<100;i++){double next=RealmMeteorTuning.filter(value,0,.35);require(next>=0&&next<value,"Stopped orb fades continuously");value=next;}
        require(value<.001,"Stopped orb eventually disappears");
        require(RealmMeteorTuning.join(-1)==0&&RealmMeteorTuning.join(0)==1,"Nose fades in and equator joins the wake without an alpha gap");
        var path=new RealmMeteorPath();
        for(int i=0;i<=100;i++)path.sample(new RealmMeteorPath.Vec(i*.65,0,0),i,true);
        var frames=path.render(99.5,new RealmMeteorPath.Vec(99.5*.65,0,0),2,28,RealmMeteorTuning.TRAIL_LIFETIME_TICKS);
        var longFrames=path.renderAdaptive(99.5,new RealmMeteorPath.Vec(99.5*.65,0,0),68,RealmMeteorTuning.TRAIL_LIFETIME_TICKS,0);
        require(longFrames.get(longFrames.size()-1).arc()-longFrames.get(0).arc()>35,"Three-second history actually retains a long wake at moderate speed");
        for(var frame:frames)require(frame.tick()>=99.5-RealmMeteorTuning.TRAIL_LIFETIME_TICKS&&frame.tick()<=99.5,"New trail lifetime keeps actual history within three seconds");
        System.out.println("Realm meteor tuning regression: "+checks+" width/speed/fade/history checks passed");
    }
}
