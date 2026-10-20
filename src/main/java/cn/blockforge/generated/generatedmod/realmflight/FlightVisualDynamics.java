package cn.blockforge.generated.generatedmod.realmflight;

/** Fixed-tick cosmetic dynamics. Never changes movement, entity rotation, or permissions. */
public final class FlightVisualDynamics {
    // Angles below are degrees, speeds are blocks/tick, and transition times are seconds.
    // Model-space X rotation: 90° is level, so 70° is 20° upward.
    public static final float TICK_SECONDS=.05F, NORMAL_FLIGHT_PITCH=70, FAST_FLIGHT_PITCH=78, SPRINT_FLIGHT_PITCH=85;
    public static final float NORMAL_ARM_SPREAD=15, FAST_ARM_SPREAD=7, ARM_SWEEP_RADIANS=.20F;
    public static final float NORMAL_HEAD_LIFT=8, FAST_HEAD_LIFT=5, MIN_HEAD_LEAD=5, MAX_HEAD_LEAD=12;
    public static final float MAX_TURN_ROLL=35, EXTREME_TURN_ROLL=45, CAMERA_ROLL_LIMIT=24;
    public static final float MIN_TURN_ARM_DIFFERENCE=5, MAX_TURN_ARM_DIFFERENCE=10, MAX_YAW_STEP=28, MAX_PITCH_STEP=18;
    public static final float TAKEOFF_TRANSITION=.30F, LANDING_TRANSITION=.35F, TURN_SMOOTH_TIME=.20F, TURN_RETURN_TIME=.30F;
    public static final double VELOCITY_RETENTION=.91, DEFAULT_CRUISE_SPEED=8.0/20*VELOCITY_RETENTION;
    public static final double FAST_SPEED_RATIO=1.30, SPRINT_SPEED_RATIO=1.50, DRIFT_SPEED=.06, FULL_POSE_SPEED=.26;
    // Vanilla player scale, LivingEntityRenderer root offset, and leg pivot (12 pixels).
    public static final double PELVIS_HEIGHT=(1.501-12.0/16)*.9375;

    /** Existing optional camera channel remains capped at its original 24 degrees. */
    public float amount,previousAmount,speed,previousSpeed,bank,previousBank;
    private float roll,previousRoll,speedRatio,previousSpeedRatio,headLead,previousHeadLead;
    private float headingYaw,previousHeadingYaw,headingPitch,previousHeadingPitch;
    private float bodyYawContinuous,lastBodyYaw,relativeYaw,previousRelativeYaw,lastMotionYaw;
    private boolean headingInitialized,motionHeadingValid;

    /** Euler targets are radians in model space; root pitch/roll fields are degrees. */
    public record Pose(float amount,float travel,float bodyPitch,float headPitch,float armSweep,float armSpread,
                       float trajectoryPitch,float leanPitch,float roll,float headYaw,float headRoll,
                       float rightArmSpread,float leftArmSpread) {}

    public static float approach(float value,float target,float step){return value<target?Math.min(target,value+step):Math.max(target,value-step);}
    public static float smooth(float value){float x=unit(value);return x*x*(3-2*x);}
    /** Signed actual heading angular velocity, rather than sideways movement or keys. */
    public static float bankTarget(double angularVelocity,double horizontalSpeed,double cruiseSpeed){
        if(!Double.isFinite(angularVelocity)||!Double.isFinite(horizontalSpeed)||!Double.isFinite(cruiseSpeed)
                ||horizontalSpeed<=DRIFT_SPEED||cruiseSpeed<=0)return 0;
        double ratio=horizontalSpeed/cruiseSpeed;
        double limit=MAX_TURN_ROLL+(EXTREME_TURN_ROLL-MAX_TURN_ROLL)*smooth((float)((ratio-FAST_SPEED_RATIO)/(SPRINT_SPEED_RATIO-FAST_SPEED_RATIO)));
        double speedGain=.65+.35*unit((float)((ratio-.4)/.6));
        return (float)Math.max(-limit,Math.min(limit,angularVelocity/180*limit*speedGain));
    }
    /** Retained pure-filter entry point for contracts; live rendering uses velocity samples. */
    public void tick(boolean active,double motionSpeed,float targetBank){
        previousAmount=amount;previousSpeed=speed;previousBank=bank;previousRoll=roll;previousSpeedRatio=speedRatio;
        float duration=active?TAKEOFF_TRANSITION:LANDING_TRANSITION;
        amount=approach(amount,active?1:0,TICK_SECONDS/duration);
        float target=active&&Double.isFinite(motionSpeed)?unit((float)((motionSpeed-DRIFT_SPEED)/(FULL_POSE_SPEED-DRIFT_SPEED))):0;
        speed=approach(speed,target,TICK_SECONDS/(target>speed?TAKEOFF_TRANSITION:LANDING_TRANSITION));
        speedRatio=filter(speedRatio,active&&Double.isFinite(motionSpeed)?(float)Math.max(0,Math.min(4,motionSpeed/DEFAULT_CRUISE_SPEED)):0,TURN_SMOOTH_TIME);
        float rollTarget=active&&Float.isFinite(targetBank)?Math.max(-EXTREME_TURN_ROLL,Math.min(EXTREME_TURN_ROLL,targetBank)):0;
        float response=Math.abs(rollTarget)<Math.abs(roll)?TURN_RETURN_TIME:TURN_SMOOTH_TIME;
        roll=filter(roll,rollTarget,response);
        bank=filter(bank,Math.max(-CAMERA_ROLL_LIMIT,Math.min(CAMERA_ROLL_LIMIT,rollTarget)),response);
    }
    public void tick(boolean active,double vx,double vy,double vz,float bodyYaw){tick(active,vx,vy,vz,bodyYaw,DEFAULT_CRUISE_SPEED);}
    /** Derive speed, turning, and aim entirely from observed motion on the client tick. */
    public void tick(boolean active,double vx,double vy,double vz,float bodyYaw,double cruiseSpeed){
        if(!headingInitialized){
            headingYaw=Float.isFinite(bodyYaw)?bodyYaw:0;
            previousHeadingYaw=bodyYawContinuous=lastBodyYaw=headingYaw;headingInitialized=true;
        }
        previousHeadingYaw=headingYaw;previousHeadingPitch=headingPitch;previousRelativeYaw=relativeYaw;previousHeadLead=headLead;
        if(Float.isFinite(bodyYaw)){bodyYawContinuous+=wrapDegrees(bodyYaw-lastBodyYaw);lastBodyYaw=bodyYaw;}
        double horizontal=Math.hypot(vx,vz),motion=Math.hypot(horizontal,vy);
        boolean moving=active&&Double.isFinite(motion)&&motion>DRIFT_SPEED;
        float targetYaw=Float.isFinite(bodyYaw)?bodyYawContinuous:headingYaw,targetPitch=0,angularVelocity=0;
        if(moving){
            if(horizontal>DRIFT_SPEED){
                targetYaw=(float)Math.toDegrees(Math.atan2(-vx,vz));
                if(motionHeadingValid)angularVelocity=wrapDegrees(targetYaw-lastMotionYaw)/TICK_SECONDS;
                lastMotionYaw=targetYaw;motionHeadingValid=true;
            }else{targetYaw=headingYaw;motionHeadingValid=false;}
            targetPitch=(float)Math.toDegrees(Math.atan2(-vy,horizontal));
        }else motionHeadingValid=false;
        double normal=Double.isFinite(cruiseSpeed)&&cruiseSpeed>DRIFT_SPEED?cruiseSpeed:DEFAULT_CRUISE_SPEED;
        float targetRoll=moving?bankTarget(angularVelocity,horizontal,normal):0;
        tick(active,motion,targetRoll);
        // The generic filter has no realm dependency; replace only its speed ratio
        // with observed speed relative to the existing realm cruise speed.
        speedRatio=filter(previousSpeedRatio,moving?(float)Math.min(4,motion/normal):0,TURN_SMOOTH_TIME);
        headingYaw+=Math.max(-MAX_YAW_STEP,Math.min(MAX_YAW_STEP,wrapDegrees(targetYaw-headingYaw)*response(TURN_SMOOTH_TIME)));
        headingPitch+=Math.max(-MAX_PITCH_STEP,Math.min(MAX_PITCH_STEP,(targetPitch-headingPitch)*response(TURN_SMOOTH_TIME)));
        if(Math.abs(targetPitch-headingPitch)<.001F)headingPitch=targetPitch;
        float leadTarget=Math.abs(angularVelocity)<4?0:Math.copySign(MIN_HEAD_LEAD+(MAX_HEAD_LEAD-MIN_HEAD_LEAD)*unit(Math.abs(angularVelocity)/180),angularVelocity);
        headLead=filter(headLead,moving?leadTarget:0,TURN_SMOOTH_TIME);
        // Keep the relative branch continuous while its animation weight is partial.
        relativeYaw=previousRelativeYaw+wrapDegrees(headingYaw-bodyYawContinuous-previousRelativeYaw);
        // Fully upright is the safe point to discard accumulated revolutions.
        // Keep both endpoints on the same branch for the next takeoff.
        if(amount==0&&previousAmount==0&&speed==0&&previousSpeed==0){
            relativeYaw=wrapDegrees(relativeYaw);previousRelativeYaw=relativeYaw;
        }
        if(amount==1&&previousAmount==1&&speed==1&&previousSpeed==1){
            relativeYaw=wrapDegrees(relativeYaw);
            previousRelativeYaw=relativeYaw-wrapDegrees(relativeYaw-previousRelativeYaw);
        }
    }
    public float amount(float partial){return smooth(interpolate(previousAmount,amount,partial));}
    public float speed(float partial){return smooth(interpolate(previousSpeed,speed,partial));}
    public float bank(float partial){return interpolate(previousBank,bank,partial);}
    public float roll(float partial){return interpolate(previousRoll,roll,partial);}
    public float yaw(float partial){return previousHeadingYaw+wrapDegrees(headingYaw-previousHeadingYaw)*unit(partial);}
    public float yawOffset(float partial){return interpolate(previousRelativeYaw,relativeYaw,partial);}
    public float pitch(float partial){return interpolate(previousHeadingPitch,headingPitch,partial);}
    public float headLead(float partial){return interpolate(previousHeadLead,headLead,partial);}
    public float speedRatio(float partial){return interpolate(previousSpeedRatio,speedRatio,partial);}
    public Pose pose(float partial,boolean horizontal){
        float amount=amount(partial),speed=horizontal?speed(partial):0,travel=amount*speed,ratio=speedRatio(partial);
        float fast=smooth((float)((ratio-1)/(FAST_SPEED_RATIO-1))),sprint=smooth((float)((ratio-FAST_SPEED_RATIO)/(SPRINT_SPEED_RATIO-FAST_SPEED_RATIO)));
        float bodyTarget=NORMAL_FLIGHT_PITCH+(FAST_FLIGHT_PITCH-NORMAL_FLIGHT_PITCH)*fast+(SPRINT_FLIGHT_PITCH-FAST_FLIGHT_PITCH)*sprint;
        float headLift=NORMAL_HEAD_LIFT+(FAST_HEAD_LIFT-NORMAL_HEAD_LIFT)*fast;
        float spread=radians(NORMAL_ARM_SPREAD+(FAST_ARM_SPREAD-NORMAL_ARM_SPREAD)*fast)*speed;
        float bodyRoll=roll(partial)*travel,turnMagnitude=Math.abs(roll(partial));
        float difference=(MIN_TURN_ARM_DIFFERENCE+(MAX_TURN_ARM_DIFFERENCE-MIN_TURN_ARM_DIFFERENCE)*unit(turnMagnitude/MAX_TURN_ROLL))*smooth(turnMagnitude/5);
        float armOffset=radians(Math.copySign(difference*.5F,roll(partial)))*speed;
        // A prone head uses model Z for visible yaw. Solve its direction instead of
        // adding model Y yaw at the near-vertical compensation singularity.
        double lead=Math.toRadians(headLead(partial)),lift=Math.toRadians(headLift),b=Math.toRadians(roll(partial));
        double x=-Math.sin(lead)*Math.cos(lift),y=Math.sin(lift),z=Math.cos(lead)*Math.cos(lift);
        double localX=x*Math.cos(b)+y*Math.sin(b),localY=-x*Math.sin(b)+y*Math.cos(b);
        double lean=Math.toRadians(bodyTarget);
        double canonicalY=localY*Math.cos(lean)+z*Math.sin(lean),canonicalZ=-localY*Math.sin(lean)+z*Math.cos(lean);
        float headPitch=(float)-Math.acos(Math.max(-1,Math.min(1,canonicalZ)));
        float headRoll=(float)Math.atan2(localX,canonicalY);
        float trajectory=travel*pitch(partial),leanPitch=travel*bodyTarget;
        return new Pose(amount,travel,trajectory+leanPitch,headPitch,ARM_SWEEP_RADIANS*speed,spread,
                trajectory,leanPitch,bodyRoll,0,headRoll,spread-armOffset,spread+armOffset);
    }
    public static float wrapDegrees(float degrees){
        if(!Float.isFinite(degrees))return 0;
        float wrapped=degrees%360;return wrapped>=180?wrapped-360:wrapped<-180?wrapped+360:wrapped;
    }
    /** 90% response within the requested seconds; once-per-tick rather than per-frame. */
    private static float response(float seconds){return (float)(1-Math.exp(-Math.log(10)*TICK_SECONDS/seconds));}
    private static float filter(float current,float target,float seconds){float result=current+(target-current)*response(seconds);return Math.abs(result-target)<.0001F?target:result;}
    private static float interpolate(float previous,float current,float partial){return previous+(current-previous)*unit(partial);}
    private static float radians(float degrees){return (float)Math.toRadians(degrees);}
    private static float unit(float value){return Float.isFinite(value)?Math.max(0,Math.min(1,value)):0;}
}
