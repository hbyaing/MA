package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.client.weaponflame.WeaponFlameModules;
import cn.blockforge.generated.generatedmod.client.weaponflame.WeaponFlameModules.Module;
import cn.blockforge.generated.generatedmod.client.weaponflame.WeaponFlameModules.Slice;
import java.util.ArrayList;

/** Exercise negative phases, UV continuity, explicit gaps and dying trail geometry. */
public final class WeaponFlameModulesRegression {
    private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    private static ArrayList<Slice> sample(double a,double b,boolean fading) {
        var out=new ArrayList<Slice>();WeaponFlameModules.slices(a,b,fading,out::add);return out;
    }
    private static Slice at(ArrayList<Slice> slices,double x) {
        for(var s:slices)if(x>=s.start()&&x<s.end())return s;return null;
    }
    public static void run() {
        var whole=sample(-8.25,18.75,false);var split=new ArrayList<Slice>();
        for(int i=0;i<108;i++)WeaponFlameModules.slices(-8.25+i*.25,-8+i*.25,false,split::add);
        for(double x=-8.247;x<18.75;x+=.031) {
            var a=at(whole,x);var b=at(split,x);
            require((a==null)==(b==null),"Mesh subdivision changed a module gap");
            if(a!=null) {
                require(a.module()==b.module(),"Mesh subdivision changed sprite selection");
                double ua=a.u0()+(a.u1()-a.u0())*(x-a.start())/(a.end()-a.start());
                double ub=b.u0()+(b.u1()-b.u0())*(x-b.start())/(b.end()-b.start());
                require(Math.abs(ua-ub)<1e-5,"Mesh subdivision changed texture scale");
            }
        }
        double area=0;var seen=java.util.EnumSet.noneOf(Module.class);
        for(var s:whole) {
            require(s.u0()>=0&&s.u1()<=1&&s.u1()>s.u0(),"UV outside sprite");
            double expected=(s.u1()-s.u0())*s.module().aspect*s.width();
            require(Math.abs(expected-(s.end()-s.start()))<1e-5,"Sprite stretched along trail");
            seen.add(s.module());area+=s.end()-s.start();
        }
        require(seen.size()==3&&seen.contains(Module.TAIL),"Missing arc stages");
        require(area<26&&area>16,"Segment gaps missing or excessively sparse");
        var fading=sample(-2,8,true);require(!fading.isEmpty(),"Missing decay stage");
        require(fading.stream().allMatch(s->s.module()==Module.TAIL&&s.width()<.5F),"Decay must use small tail blocks");
        require(sample(2,2,false).isEmpty()&&sample(2,1,false).isEmpty(),"Zero/reversed paths must emit nothing");
        require(sample(Double.NaN,1,false).isEmpty()&&sample(0,Double.POSITIVE_INFINITY,false).isEmpty(),"Nonfinite path accepted");
        require(sample(0,100000,false).isEmpty(),"Unbounded path accepted");
        System.out.println("Weapon flame modular layout regression passed: tessellation, UV scale, gaps, decay and bounds.");
    }
    private WeaponFlameModulesRegression() {}
}
