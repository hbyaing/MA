package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.realmflight.FlightVisualDynamics;
import cn.blockforge.generated.generatedmod.realmflight.RealmFlightAppearance;
import cn.blockforge.generated.generatedmod.realmflight.RealmMeteorTuning;
import cn.blockforge.generated.generatedmod.realmflight.RealmMeteorPath.Vec;

/** Visual contract: finite, bounded banking and complete mode-exit recovery. */
public final class RealmFlightRegression {
    private static void require(boolean result,String label){if(!result)throw new AssertionError(label);}
    public static void run() {
        var state=new FlightVisualDynamics();
        for(int i=0;i<60;i++)state.tick(true,5,45);
        require(state.amount==1&&state.speed==1,"Flight reaches full visual weight");
        require(Math.abs(state.bank)<=24&&state.roll(1)==45,"The body gains full roll while the original camera range stays bounded");
        require(FlightVisualDynamics.bankTarget(90,.4,.4)==-FlightVisualDynamics.bankTarget(-90,.4,.4),"Left/right angular banking is symmetric");
        require(FlightVisualDynamics.bankTarget(0,.4,.4)==0,"Constant heading never banks, even with sideways movement");
        for(int i=0;i<40;i++)state.tick(false,5,24);
        require(state.amount==0&&state.speed==0&&state.bank==0,"Exit restores pose and camera completely");
        state.tick(true,Double.NaN,Float.NaN);
        require(Float.isFinite(state.speed)&&Float.isFinite(state.bank),"Invalid motion cannot corrupt the camera");
        require(FlightVisualDynamics.bankTarget(Double.POSITIVE_INFINITY,0,0)==0,"Invalid direction has neutral banking");
        require(FlightVisualDynamics.smooth(-1)==0&&FlightVisualDynamics.smooth(2)==1,"Animation weights are bounded");
        var slowCruise=new FlightVisualDynamics();
        for(int i=0;i<25;i++)slowCruise.tick(true,8.0/20*.91,0);
        require(slowCruise.speed==1,"The slowest realm cruise reaches the horizontal flight pose");
        for(int i=0;i<25;i++)slowCruise.tick(true,.025,0);
        require(slowCruise.speed==0,"Small hovering drift returns to an upright pose");
        trajectoryPose();
        turningDynamics();
        var orientation=new RealmFlightAppearance.Orientation();
        for(var desired:new Vec[]{new Vec(0,-1,0),new Vec(1,0,0),new Vec(-1,0,0),new Vec(0,0,1),new Vec(0,1,0)}){
            for(int i=0;i<30;i++){
                var before=orientation.heading;orientation.tick(desired,true);
                require(orientation.heading.finite()&&orientation.side.finite(),"Opposite and vertical turns remain finite");
                require(before.dot(orientation.heading)>=Math.cos(.61)-1e-9,"Turn stays within its per-tick angular bound");
                require(Math.abs(orientation.heading.dot(orientation.side))<1e-8,"Transported effect axes remain orthogonal");
            }
            require(orientation.heading.dot(desired)>.99999,"A direct reversal eventually points at the actual flight direction");
        }
        var resting=orientation.heading;orientation.tick(new Vec(0,1,0),false);
        require(orientation.heading.equals(resting),"Hovering cannot suddenly stand the moving effect back upright");
        for(int realm=4;realm<=8;realm++){
            var current=RealmFlightAppearance.profile(realm);
            var previous=RealmFlightAppearance.profile(realm-1);
            require(current.style()!=previous.style(),"Each unlocked realm selects its own visual field");
            require(current.radius()>previous.radius(),"Higher realms progress in wake width");
            if(realm>4)require(current.length()>=previous.length(),"Advanced realm trail lengths progress within the tuned limits");
            require(current.boostLength()<=RealmMeteorTuning.MAX_TRAIL_LENGTH,"Advanced realm wake lengths remain bounded");
        }
        System.out.println("Realm flight visual regression: speed poses, anticipatory head, angular banking, mirrored turn arms, six tiers and recovery passed");
    }
    private static void trajectoryPose(){
        var velocities=new Vec[]{new Vec(0,0,.4),new Vec(.4,0,0),new Vec(0,0,-.4),new Vec(-.4,0,0),
                new Vec(.2,.35,.1),new Vec(-.2,-.35,.1),new Vec(0,.4,0),new Vec(0,-.4,0)};
        for(double ratio:new double[]{1,1.30,1.50})for(var velocity:velocities){
            var state=new FlightVisualDynamics();double cruise=velocity.length()/ratio;
            for(int i=0;i<80;i++)state.tick(true,velocity.x(),velocity.y(),velocity.z(),37,cruise);
            var pose=state.pose(1,true);var desired=velocity.normalize();
            double bodyTarget=ratio==1?70:ratio==1.30?78:85,headLift=ratio==1?8:5;
            double lift=Math.toRadians(90-bodyTarget),spread=Math.toRadians(ratio==1?15:7);
            require(Math.abs(pose.leanPitch()-bodyTarget)<1e-4,"Observed cruise, fast and sprint speeds select 70, 78 and 85 degrees");
            var face=headDirection(pose,state,37);
            var torso=renderedDirection(new Vec(0,-1,0),state,37);
            var trajectoryUp=flightDirection(new Vec(0,1,0),state);
            require(Math.abs(face.dot(desired)-Math.cos(Math.toRadians(headLift)))<1e-6
                            &&Math.abs(face.dot(trajectoryUp)-Math.sin(Math.toRadians(headLift)))<1e-6,
                    "Head lift is eight degrees at cruise and five degrees at high speed in the actual flight plane");
            require(Math.abs(torso.dot(desired)-Math.cos(lift))<1e-6
                            &&Math.abs(torso.dot(trajectoryUp)-Math.sin(lift))<1e-6,
                    "The torso uses the requested 20-degree upward cruise tilt and eases toward level at speed");
            var sweptArm=rotateX(new Vec(0,1,0),pose.armSweep());
            var rightArm=renderedDirection(rotateZ(sweptArm,pose.rightArmSpread()),state,37);
            var leftArm=renderedDirection(rotateZ(sweptArm,-pose.leftArmSpread()),state,37);
            var lateral=renderedDirection(new Vec(1,0,0),state,37);
            require(Math.abs(pose.armSpread()-spread)<1e-6
                            &&Math.abs(pose.rightArmSpread()-pose.leftArmSpread())<1e-6,
                    "Straight cruise opens both arms fifteen degrees and fast flight tightens both to seven");
            require(Math.abs(rightArm.dot(torso)+Math.cos(.20)*Math.cos(spread))<1e-6
                            &&Math.abs(leftArm.dot(torso)+Math.cos(.20)*Math.cos(spread))<1e-6
                            &&rightArm.dot(desired)<0&&leftArm.dot(desired)<0,
                    "Both arms remain swept behind the body at every speed");
            double outward=Math.cos(.20)*Math.sin(spread);
            require(Math.abs(rightArm.dot(lateral)+outward)<1e-6&&Math.abs(leftArm.dot(lateral)-outward)<1e-6,
                    "The shoulder rotations produce equal mirrored outward hand directions");
            var pelvis=new Vec(0,(1.501-12.0/16)*.9375,0);
            require(renderedPoint(new Vec(0,12.0/16,0),state,37).subtract(pelvis).lengthSquared()<1e-20,
                    "Changing speed, azimuth and elevation cannot move the actual pelvis anchor");
            for(int i=0;i<40;i++)state.tick(true,0,0,0,37,cruise);
            require(state.pose(1,true).travel()==0&&state.pose(1,true).bodyPitch()==0&&state.pitch(1)==0,
                    "Stopping restores an upright torso and clears visual pitch");
            require(state.pose(1,true).armSpread()==0&&state.pose(1,true).armSweep()==0
                            &&state.pose(1,true).rightArmSpread()==0&&state.pose(1,true).leftArmSpread()==0,
                    "Hovering restores neutral arms as well as the standing torso");
            for(int i=0;i<40;i++)state.tick(false,velocity.x(),velocity.y(),velocity.z(),37,cruise);
            require(state.pose(1,true).amount()==0&&state.bank==0&&state.roll(1)==0,
                    "Exiting flight restores the full vanilla pose and both roll channels");
        }
        var wrap=new FlightVisualDynamics();
        for(int i=0;i<30;i++)wrap.tick(true,-Math.sin(Math.toRadians(179))*.4,0,Math.cos(Math.toRadians(179))*.4,179);
        float before=wrap.yaw(1);
        wrap.tick(true,-Math.sin(Math.toRadians(-179))*.4,0,Math.cos(Math.toRadians(-179))*.4,179);
        require(wrap.yaw(1)>before&&wrap.yaw(1)-before<2,"Crossing the yaw seam follows the short continuous turn");
        require(Math.abs(wrap.yaw(.5F)-(before+wrap.yaw(1))*.5F)<1e-4,"Render interpolation cannot jump a full revolution");
        var partialTurn=new FlightVisualDynamics();
        for(int i=0;i<60;i++)partialTurn.tick(true,-Math.sin(Math.toRadians(179))*.4,0,Math.cos(Math.toRadians(179))*.4,0);
        before=partialTurn.yawOffset(1)*partialTurn.pose(1,true).travel();
        for(int i=0;i<8;i++){
            partialTurn.tick(true,-Math.sin(Math.toRadians(-177))*.19,0,Math.cos(Math.toRadians(-177))*.19,0);
            float renderedYaw=partialTurn.yawOffset(1)*partialTurn.pose(1,true).travel();
            require(Math.abs(renderedYaw-before)<35,"Crossing relative 180 degrees at partial posture weight cannot flip the player");
            before=renderedYaw;
        }
        for(int i=0;i<20;i++){
            before=wrap.yaw(1);wrap.tick(true,0,0,.4,179);
            require(Math.abs(wrap.yaw(1)-before)<=28.0001,"Direction reversals obey the fixed-tick yaw limit");
        }
        wrap.tick(true,Double.NaN,Double.POSITIVE_INFINITY,0,Float.NaN);
        var invalid=wrap.pose(Float.NaN,true);
        require(Float.isFinite(invalid.bodyPitch())&&Float.isFinite(wrap.yaw(Float.NaN)),"Invalid velocity and render interpolation stay finite");
        require(wrap.pose(1,false).bodyPitch()==0&&wrap.pose(1,false).travel()==0,"The horizontal animation preference retains the upright mode");
    }
    private static void turningDynamics(){
        var response=new FlightVisualDynamics();
        for(int i=0;i<4;i++)response.tick(true,.4,35);
        require(response.roll(1)>=31.49&&response.roll(1)<35,"A turn reaches ninety percent of its target within 0.20 seconds");
        float initial=response.roll(1);
        for(int i=0;i<6;i++)response.tick(true,.4,0);
        require(response.roll(1)<=initial*.1001F,"Roll returns ninety percent toward neutral within 0.30 seconds");
        for(double ratio:new double[]{1,1.5})for(int sign:new int[]{-1,1}){
            var state=new FlightVisualDynamics();double cruise=.4,motion=cruise*ratio;
            for(int i=0;i<40;i++)state.tick(true,0,0,motion,0,cruise);
            require(state.roll(1)==0,"A first straight velocity sample cannot invent a turn");
            double step=ratio==1?4.5:9,target=ratio==1?17.5:45;
            for(int i=1;i<=40;i++){
                double yaw=Math.toRadians(i*step*sign);
                state.tick(true,-Math.sin(yaw)*motion,0,Math.cos(yaw)*motion,(float)(i*step*sign),cruise);
                require(Math.abs(state.roll(1))<=45&&Math.abs(state.bank(1))<=24,"Body and camera roll remain independently bounded");
                require(Math.abs(state.headLead(1))<=12,"Anticipatory head yaw never exceeds twelve degrees");
            }
            var pose=state.pose(1,true);
            require(Math.abs(state.roll(1)-sign*target)<1e-3,"Actual heading angular velocity and speed produce the intended inward roll");
            require(state.headLead(1)*sign>=5,"A sustained turn leads the face toward that turn by five to twelve degrees");
            float difference=(float)Math.toDegrees(pose.leftArmSpread()-pose.rightArmSpread());
            require(difference*sign>=5&&difference*sign<=10.001,"The inside arm tightens and the outside arm opens by a five to ten degree difference");
            var rightShoulder=renderedDirection(new Vec(-1,0,0),state,(float)(40*step*sign));
            require(rightShoulder.dot(flightDirection(new Vec(0,1,0),state))*sign<0,
                    "Right turns lower the right shoulder and left turns lower the left shoulder");
            double lead=Math.toRadians(state.headLead(1)),lift=Math.toRadians(ratio==1?8:5);
            var targetHead=flightDirection(new Vec(-Math.sin(lead)*Math.cos(lift),Math.sin(lift),Math.cos(lead)*Math.cos(lift)),state);
            require(headDirection(pose,state,(float)(40*step*sign)).dot(targetHead)>.999999,
                    "Compensated head Euler rotations preserve its world aim while the body banks");
            var pelvis=new Vec(0,(1.501-12.0/16)*.9375,0);
            require(renderedPoint(new Vec(0,12.0/16,0),state,(float)(40*step*sign)).subtract(pelvis).lengthSquared()<1e-20,
                    "Extreme bank cannot translate the true pelvis anchor");
            float before=state.roll(1),headBefore=state.headLead(1),yawBefore=state.yaw(1);
            for(int frame=0;frame<240;frame++)state.pose((frame%120)/120F,true);
            require(state.roll(1)==before&&state.headLead(1)==headBefore&&state.yaw(1)==yawBefore,
                    "Rendering at thirty, sixty or one hundred twenty FPS cannot advance the tick animation");
            double lastYaw=Math.toRadians(40*step*sign);
            for(int i=0;i<6;i++)state.tick(true,-Math.sin(lastYaw)*motion,0,Math.cos(lastYaw)*motion,(float)(40*step*sign),cruise);
            require(Math.abs(state.roll(1))<=Math.abs(before)*.1001&&Math.abs(state.headLead(1))<2,
                    "Holding a straight heading releases bank and head anticipation without using input keys");
        }
        require(FlightVisualDynamics.bankTarget(180,.4,.4)==35&&FlightVisualDynamics.bankTarget(180,.6,.4)==45,
                "Normal sharp turns cap at thirty-five degrees and extreme fast turns at forty-five");
        require(FlightVisualDynamics.bankTarget(90,.12,.4)<FlightVisualDynamics.bankTarget(90,.4,.4),
                "The same heading angular velocity banks more gently at low actual speed");
        var sideways=new FlightVisualDynamics();
        for(int i=0;i<30;i++)sideways.tick(true,.4,0,0,0,.4);
        require(sideways.roll(1)==0,"Constant sideways travel cannot be mistaken for ongoing turning");
    }
    private static Vec headDirection(FlightVisualDynamics.Pose pose,FlightVisualDynamics state,float bodyYaw){
        var local=rotateX(new Vec(0,0,-1),pose.headPitch());
        local=rotateY(local,pose.headYaw());local=rotateZ(local,pose.headRoll());
        return renderedDirection(local,state,bodyYaw);
    }
    private static Vec flightDirection(Vec direction,FlightVisualDynamics state){
        return rotateY(rotateX(direction,Math.toRadians(state.pitch(1))),Math.toRadians(-state.yaw(1)));
    }
    private static Vec renderedDirection(Vec modelDirection,FlightVisualDynamics state,float bodyYaw){
        // Vanilla's negative scale and 180-bodyYaw rotation are applied before Pre's root matrix.
        var scaled=new Vec(-modelDirection.x(),-modelDirection.y(),modelDirection.z());
        return rootDirection(rotateY(scaled,Math.toRadians(180-bodyYaw)),state,bodyYaw);
    }
    private static Vec renderedPoint(Vec modelPoint,FlightVisualDynamics state,float bodyYaw){
        var shifted=modelPoint.add(new Vec(0,-1.501,0));
        var vanilla=rotateY(new Vec(-shifted.x()*.9375,-shifted.y()*.9375,shifted.z()*.9375),Math.toRadians(180-bodyYaw));
        var pivot=new Vec(0,FlightVisualDynamics.PELVIS_HEIGHT,0);
        return rootDirection(vanilla.subtract(pivot),state,bodyYaw).add(pivot);
    }
    private static Vec rootDirection(Vec direction,FlightVisualDynamics state,float bodyYaw){
        var pose=state.pose(1,true);
        float yaw=bodyYaw+state.yawOffset(1)*pose.travel();
        var result=rotateY(direction,Math.toRadians(bodyYaw));
        result=rotateX(result,Math.toRadians(pose.leanPitch()));
        result=rotateZ(result,Math.toRadians(pose.roll()));
        result=rotateX(result,Math.toRadians(pose.trajectoryPitch()));
        return rotateY(result,Math.toRadians(-yaw));
    }
    private static Vec rotateX(Vec p,double angle){
        double c=Math.cos(angle),s=Math.sin(angle);return new Vec(p.x(),p.y()*c-p.z()*s,p.y()*s+p.z()*c);
    }
    private static Vec rotateY(Vec p,double angle){
        double c=Math.cos(angle),s=Math.sin(angle);return new Vec(p.x()*c+p.z()*s,p.y(),-p.x()*s+p.z()*c);
    }
    private static Vec rotateZ(Vec p,double angle){
        double c=Math.cos(angle),s=Math.sin(angle);return new Vec(p.x()*c-p.y()*s,p.x()*s+p.y()*c,p.z());
    }
    private RealmFlightRegression(){}
}
