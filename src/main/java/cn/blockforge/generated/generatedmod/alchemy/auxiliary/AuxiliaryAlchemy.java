package cn.blockforge.generated.generatedmod.alchemy.auxiliary;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.alchemy.PillData;
import cn.blockforge.generated.generatedmod.plants.HerbAge;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** r115 secondary-herb rules over the target's existing material reservation and pill schema. */
public final class AuxiliaryAlchemy {
    public static final String TAG="MyriadAuxiliaryAlchemy";
    public enum Role { CHANCE,EFFECT,OUTPUT }
    public enum Essence {
        SPIRIT(400,0,1),ORIGIN(600,0,0),PURITY(600,0,1),BREAK(600,0,0),VITALITY(400,0,1),
        FROST(600,0,0),FLAME(600,0,-2),METAL(600,0,3),VENOM(200,0,-2),WIND(600,0,1),
        WOOD(1200,0,2),ILLUSION(400,0,0),THUNDER(600,1,-1),STONE(600,1,1),FORTUNE(600,0,2),ABUNDANCE(600,0,1);
        public final int duration,amplifier,stability;
        Essence(int duration,int amplifier,int stability){this.duration=duration;this.amplifier=amplifier;this.stability=stability;}
    }
    public record Herb(Role role,Essence essence){}
    public record Injection(Essence essence,int tier,int duration,int amplifier,String source){}
    public record Allocation(int[] counts,Plan plan){}
    public record Input(Role role,ItemStack paid){}
    public static Herb herb(ItemStack stack){
        if(stack.isEmpty()||!BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(GeneratedMod.MOD_ID))return null;
        return switch(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()){
            case "herb_xingchen" -> new Herb(Role.CHANCE,Essence.PURITY);
            case "herb_tianji" -> new Herb(Role.CHANCE,Essence.FORTUNE);
            case "herb_jingang" -> new Herb(Role.CHANCE,Essence.METAL);
            case "herb_huanling" -> new Herb(Role.EFFECT,Essence.ILLUSION);
            case "herb_hanbing" -> new Herb(Role.EFFECT,Essence.FROST);
            case "herb_lieyang" -> new Herb(Role.EFFECT,Essence.FLAME);
            case "herb_duzhen" -> new Herb(Role.EFFECT,Essence.VENOM);
            case "herb_jifeng" -> new Herb(Role.EFFECT,Essence.WIND);
            case "herb_lingzhi" -> new Herb(Role.EFFECT,Essence.WOOD);
            case "herb_leiteng" -> new Herb(Role.EFFECT,Essence.THUNDER);
            case "herb_yanjia" -> new Herb(Role.EFFECT,Essence.STONE);
            case "herb_fengling","herb_lingdao" -> new Herb(Role.OUTPUT,Essence.ABUNDANCE);
            default -> null;
        };
    }
    public static Role role(ItemStack stack){var h=herb(stack);return h==null?null:h.role;}

    /** Ordinary old UI: at most one spare slot per source role, one plant paid per role. */
    public static Allocation allocate(Container container,int[] base){
        int[] selected={-1,-1,-1};
        for(int slot=0;slot<Math.min(8,Math.min(base.length,container.getContainerSize()));slot++){
            var role=role(container.getItem(slot));
            if(base[slot]==0&&role!=null&&selected[role.ordinal()]<0&&HerbAge.legacyEffectBand(container.getItem(slot))>0)selected[role.ordinal()]=slot;
        }
        return allocate(container,base,selected);
    }
    /** Heavenly furnace only uses explicit choices; never scan unrelated backpack herbs. */
    public static Allocation allocate(Container container,int[] base,int[] selected){
        if(selected==null||selected.length!=3||base.length>container.getContainerSize())throw new IllegalArgumentException("Invalid auxiliary allocation");
        int[] counts=base.clone();var used=new HashSet<Integer>();var inputs=new ArrayList<Input>();
        for(var role:Role.values()){
            int slot=selected[role.ordinal()];if(slot==-1)continue;
            if(slot<0||slot>=base.length||base[slot]!=0||!used.add(slot)||role(container.getItem(slot))!=role||HerbAge.legacyEffectBand(container.getItem(slot))<1)
                throw new IllegalArgumentException("Auxiliary choice is missing, reserved, repeated, unaged, or in the wrong role");
            counts[slot]=1;inputs.add(new Input(role,container.getItem(slot).copyWithCount(1)));
        }
        return new Allocation(counts,new Plan(inputs));
    }

    public static final class Plan {
        private final List<Input> inputs;
        public Plan(List<Input> inputs){
            var accepted=new ArrayList<Input>();var roles=EnumSet.noneOf(Role.class);
            for(var input:inputs){
                var h=herb(input.paid);if(accepted.size()>=3||h==null||h.role!=input.role||!roles.add(input.role)||HerbAge.legacyEffectBand(input.paid)<1)continue;
                accepted.add(new Input(input.role,input.paid.copyWithCount(1)));
            }this.inputs=List.copyOf(accepted);
        }
        public boolean empty(){return inputs.isEmpty();}
        public List<ItemStack> paid(){return inputs.stream().map(input->input.paid.copy()).toList();}
        public List<Injection> injections(){
            var merged=new EnumMap<Essence,Injection>(Essence.class);
            for(var input:inputs){
                var essence=herb(input.paid).essence;int tier=Math.max(1,Math.min(5,HerbAge.legacyEffectBand(input.paid)));
                double strength=input.role==Role.EFFECT?1:.5;
                int duration=(int)Math.max(20,Math.min(7200,Math.round(essence.duration*(1+.5*(tier-1))*strength)));
                int amplifier=essence.amplifier+(tier-1)/2;if(strength<1&&amplifier>0)amplifier--;
                amplifier=Math.max(0,Math.min(4,amplifier));String source=BuiltInRegistries.ITEM.getKey(input.paid.getItem()).toString();
                var old=merged.get(essence);
                merged.put(essence,old==null?new Injection(essence,tier,duration,amplifier,source):new Injection(essence,Math.max(tier,old.tier),Math.min(7200,old.duration+duration/2),Math.min(4,old.amplifier+1),old.source));
            }return List.copyOf(merged.values());
        }
        public boolean clash(){var effects=injections().stream().map(Injection::essence).toList();return effects.contains(Essence.FROST)&&effects.contains(Essence.FLAME);}
        public boolean backlash(){var effects=injections().stream().map(Injection::essence).toList();return effects.contains(Essence.VENOM)&&!effects.contains(Essence.PURITY)&&!effects.contains(Essence.WOOD);}
        public int successBonus(){int stability=0;for(var input:inputs)if(input.role==Role.CHANCE)stability=herb(input.paid).essence.stability*HerbAge.legacyEffectBand(input.paid);return Math.max(-12,Math.min(12,stability))-(clash()?8:0);}
        public int extraOutput(){for(var input:inputs)if(input.role==Role.OUTPUT)return HerbAge.legacyEffectBand(input.paid)/2;return 0;}
        public CompoundTag save(){var tag=new CompoundTag();var list=new ListTag();for(var input:inputs){var n=new CompoundTag();n.putString("Role",input.role.name());n.put("Paid",input.paid.save(new CompoundTag()));list.add(n);}tag.put("Inputs",list);tag.putInt("Schema",1);return tag;}
        public static Plan load(CompoundTag tag){var inputs=new ArrayList<Input>();var rows=tag.getList("Inputs",Tag.TAG_COMPOUND);for(int i=0;i<Math.min(3,rows.size());i++){var n=rows.getCompound(i);try{inputs.add(new Input(Role.valueOf(n.getString("Role")),ItemStack.of(n.getCompound("Paid"))));}catch(IllegalArgumentException ignored){}}return new Plan(inputs);}
    }

    /** Call once on the rolled successful pill, before the existing job persists it. */
    public static ItemStack finish(ItemStack original,Plan plan){
        if(original.isEmpty()||PillData.read(original)==null||plan.empty()||original.hasTag()&&original.getTag().contains(TAG))return original;
        var result=original.copy();result.getOrCreateTag().put(TAG,plan.save());
        result.setCount(Math.min(result.getMaxStackSize(),result.getCount()+plan.extraOutput()));return result;
    }
    public static Plan read(ItemStack pill){return Plan.load(pill.hasTag()?pill.getTag().getCompound(TAG):new CompoundTag());}
    private AuxiliaryAlchemy(){}
}
