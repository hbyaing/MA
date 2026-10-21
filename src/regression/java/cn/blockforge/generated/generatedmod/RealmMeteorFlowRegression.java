package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.realmflight.*;
import java.util.ArrayList;
import java.util.List;

/** Geometry contracts for the real flow implementation, including the front-light isolation. */
public final class RealmMeteorFlowRegression {
    private static int checks;
    private static void require(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    private static void near(double expected,double actual,double tolerance,String label){
        require(Double.isFinite(actual)&&Math.abs(expected-actual)<=tolerance,label+": "+expected+" != "+actual);
    }
    public static void run(){
        double baseRadius=RealmFlightAppearance.profile(4).radius();
        for(int realm=4;realm<=8;realm++){
            var profile=RealmFlightAppearance.profile(realm);
            for(double length:new double[]{profile.length(),profile.boostLength()}){
                for(int step=0;step<=120;step++){
                    double behind=-baseRadius*1.48+step*(baseRadius*1.48+4)/120;
                    near(RealmMeteorFlow.width(behind,28,baseRadius,baseRadius),
                         RealmMeteorFlow.width(behind,length,baseRadius,profile.radius()),1e-12,
                         "All advanced ranks retain the same full nose/body envelope at both flight speeds");
                    near(RealmMeteorFlow.remaining(behind,28),RealmMeteorFlow.remaining(behind,length),1e-12,
                         "A longer rank tail cannot raise opacity near the player");
                }
                for(double join:new double[]{4,8}){
                    double eps=1e-4;
                    double at=RealmMeteorFlow.width(join,length,baseRadius,profile.radius());
                    double left=(at-RealmMeteorFlow.width(join-eps,length,baseRadius,profile.radius()))/eps;
                    double right=(RealmMeteorFlow.width(join+eps,length,baseRadius,profile.radius())-at)/eps;
                    near(left,right,.001,"Rank envelope enters and leaves its transition without a derivative seam");
                }
                near(0,RealmMeteorFlow.width(length,length,baseRadius,profile.radius()),0,"The historical tail ends at zero width");
            }
        }
        for(int style=1;style<=5;style++)for(int layer=0;layer<3;layer++)for(int tick=0;tick<16;tick++){
            double arc=tick*3.71,phase=tick*.13;
            var front=RealmMeteorFlow.section(style,layer,3.9,arc,phase,.71,.4,-.3);
            near(Math.cos(.71),front.side(),1e-12,"Rank folds never deform the common front");
            near(Math.sin(.71),front.up(),1e-12,"Rank folds never deform the common front");
            var first=RealmMeteorFlow.section(style,layer,12,arc,phase,0,.4,-.3);
            var last=RealmMeteorFlow.section(style,layer,12,arc,phase,2*Math.PI,.4,-.3);
            near(first.side(),last.side(),1e-9,"Cross section has no angular seam");
            near(first.up(),last.up(),1e-9,"Cross section has no angular seam");
            for(int side=0;side<32;side++){
                var section=RealmMeteorFlow.section(style,layer,12,arc,phase,side*Math.PI/16,.4,-.3);
                require(Double.isFinite(section.side())&&Double.isFinite(section.up()),"Compound flow remains finite");
                near(1,Math.hypot(section.normalSide(),section.normalUp()),1e-9,"Deformed section normals remain unit length");
                require(Math.hypot(section.side(),section.up())<=RealmMeteorFlow.sectionBound(style,12)+1e-9,
                        "Geometry stays inside the bound used for acute-bend clipping");
            }
        }
        curvature();
        // An inward unit bend is the strongest possible metadata. Sweep the
        // independent deformation phases so clipping never underestimates it.
        for(int style=2;style<=5;style++)for(int layer=0;layer<3;layer++)for(int arcStep=0;arcStep<80;arcStep++)
            for(int phaseStep=0;phaseStep<12;phaseStep++)for(int angleStep=0;angleStep<24;angleStep++){
                double angle=angleStep*Math.PI/12,turn=phaseStep*Math.PI/6;
                var section=RealmMeteorFlow.section(style,layer,20,arcStep*2.31,phaseStep*.93,angle,Math.cos(turn),Math.sin(turn));
                require(Math.hypot(section.side(),section.up())<=RealmMeteorFlow.sectionBound(style,20)+1e-9,
                        "Strong compound bends remain inside the conservative clipping envelope");
            }
        for(double behind:new double[]{-2,0,4,6,30,68})
            near(behind,RealmMeteorFlow.packBehind(behind)/256.0-4,.002,"Signed-short behind metadata survives the vertex pipeline");
        for(double age:new double[]{0,3.125,20,60})
            near(age,RealmMeteorFlow.packAge(age)/256.0,.002,"History age survives the vertex pipeline");
        for(double bend:new double[]{-1,-.22,0,.31,1})
            near(bend,RealmMeteorFlow.packBend(bend)/16384.0,.00004,"Signed curvature retains bend direction");
        require(RealmMeteorFlow.packBend(Double.NaN)==0,"Invalid curvature produces neutral metadata");
        System.out.println("Realm meteor flow regression: "+checks+" envelope/curve/frame/metadata checks passed");
    }
    private static void curvature(){
        var curve=new ArrayList<RealmMeteorPath.Frame>();
        double radius=12;
        for(int step=0;step<=40;step++){
            double angle=step*.035;
            var side=new RealmMeteorPath.Vec(Math.cos(angle),0,Math.sin(angle));
            var tangent=new RealmMeteorPath.Vec(-Math.sin(angle),0,Math.cos(angle));
            curve.add(new RealmMeteorPath.Frame(side.scale(radius),tangent,side,new RealmMeteorPath.Vec(0,1,0),step,step*.035*radius,1));
        }
        var bend=RealmMeteorFlow.bend(curve,20);
        near(-3/radius,bend.side(),.001,"Curvature points inward on a known world-space circular path");
        near(0,bend.up(),1e-9,"A level turn adds no vertical curvature");
        var outside=RealmMeteorFlow.residualSection(0,bend.side(),bend.up());
        var inside=RealmMeteorFlow.residualSection(Math.PI,bend.side(),bend.up());
        require(Math.hypot(outside.side(),outside.up())>Math.hypot(inside.side(),inside.up()),"Residual haze expands the outside of the actual old bend");
        for(int style=1;style<=5;style++){
            require(RealmMeteorFlow.residualWeight(style,20,27,bend.side(),bend.up())==0,"Old bend haze expires instead of accumulating");
            require(RealmMeteorFlow.residualWeight(style,3,8,bend.side(),bend.up())==0,"Turn residue cannot brighten the front body wrap");
            require(RealmMeteorFlow.residualWeight(style,20,8,0,0)==0,"Straight flight produces no false curved residue");
            if(style>=4)require(RealmMeteorFlow.residualWeight(style,20,8,bend.side(),bend.up())>0,"Higher ranks keep brief haze on the historical bend");
        }
        var duplicate=List.of(curve.get(0),curve.get(0));
        near(0,RealmMeteorFlow.bend(duplicate,0).magnitude(),0,"Repeated history points cannot generate infinite curvature");
    }
    private RealmMeteorFlowRegression(){}
}
