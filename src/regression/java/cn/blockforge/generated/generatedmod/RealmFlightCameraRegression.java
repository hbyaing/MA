package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.realmflight.RealmFlightBalance;
import cn.blockforge.generated.generatedmod.realmflight.RealmFlightCameraDynamics;

public final class RealmFlightCameraRegression {
    private static int checks;
    private static void require(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    public static void run(){
        for(int realm=3;realm<=8;realm++)for(int stage=0;stage<=3;stage++){
            double cruise=RealmFlightBalance.speed(realm,stage,false)/20*.91;
            double boost=RealmFlightBalance.speed(realm,stage,true)/20*.91;
            var state=new RealmFlightCameraDynamics();
            for(double stalled:new double[]{0,.02,cruise*.25,cruise,cruise*1.04}){
                for(int tick=0;tick<20;tick++)state.tick(true,true,stalled,cruise,boost);
                require(state.distance(1)==4,"Sprint below actual boosted speed keeps ordinary camera distance");
            }
            double previous=4;
            for(int tick=0;tick<80;tick++){
                state.tick(true,true,boost,cruise,boost);
                double current=state.distance(1);
                require(current>=previous&&current<=6.8,"Boost pullback is monotone and bounded");
                require(state.distance(0)==previous,"The next tick begins at the previous endpoint");
                double midpoint=state.distance(.5F);
                require(midpoint>=previous&&midpoint<=current,"Render interpolation stays inside tick endpoints");
                for(int render=0;render<240;render++)state.distance(render/239F);
                require(state.distance(1)==current&&state.targetDistance()==6.8,"Extra render frames cannot advance camera simulation");
                previous=current;
            }
            require(state.distance(1)==6.8,"Sustained actual boost settles at full pullback");
            for(int tick=0;tick<80;tick++){
                state.tick(true,false,boost,cruise,boost);
                double current=state.distance(1);
                require(current<=previous&&current>=4,"Releasing sprint smoothly returns while momentum remains");
                previous=current;
            }
            require(state.distance(1)==4,"Sprint release restores exact ordinary distance");
            for(int tick=0;tick<10;tick++)state.tick(true,true,boost,cruise,boost);
            state.tick(false,true,boost,cruise,boost);
            for(float partial:new float[]{0,.5F,1})require(state.distance(partial)==4,"Flight exit clears both render endpoints immediately");
            for(int tick=0;tick<10;tick++)state.tick(true,true,boost,cruise,boost);
            state.reset();require(state.previousDistance()==4&&state.currentDistance()==4&&state.targetDistance()==4,
                    "Changing world/player/camera discards all old pullback state");
        }
        var invalid=new RealmFlightCameraDynamics();
        for(double value:new double[]{Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,-1}){
            invalid.tick(true,true,value,1,1.5);
            require(invalid.distance(Float.NaN)==4,"Invalid speed cannot poison camera position");
            require(RealmFlightCameraDynamics.boostWeight(true,2,value,3)==0,"Invalid cruise speed cannot generate boost");
        }
        require(RealmFlightCameraDynamics.boostWeight(false,10,1,1.5)==0,"Fast travel without realm boost cannot pull camera");
        require(RealmFlightCameraDynamics.boostWeight(true,2,1,1)==0,"Invalid boost range cannot divide by zero");
        require(RealmFlightCameraDynamics.boostWeight(true,2,1,Double.NaN)==0,"Invalid boost range cannot poison target");
        require(invalid.distance(-1)==invalid.distance(0)&&invalid.distance(2)==invalid.distance(1),"Out-of-range render partial ticks clamp to endpoints");
        System.out.println("Realm flight camera regression: "+checks+" checks passed");
    }
    private RealmFlightCameraRegression(){}
}
