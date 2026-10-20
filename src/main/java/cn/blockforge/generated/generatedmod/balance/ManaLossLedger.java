package cn.blockforge.generated.generatedmod.balance;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import java.util.LinkedHashMap;

/** Bounded actual debit history; captured before recovery can obscure the debit. */
public final class ManaLossLedger {
    private final java.util.ArrayDeque<CompoundTag> rows=new java.util.ArrayDeque<>();
    private boolean enabled=true;
    private long version;
    private long viewVersion=-1;
    private java.util.List<CompoundTag> view=java.util.List.of();
    private static final StackWalker WALKER=StackWalker.getInstance();
    private static final java.util.Map<String,String> SOURCE_CACHE=new LinkedHashMap<>(128,.75f,true){@Override protected boolean removeEldestEntry(java.util.Map.Entry<String,String> e){return size()>256;}};
    /** Client UI read-only rows, rebuilt only when history changes. */
    public java.util.List<CompoundTag> view(){if(viewVersion!=version){view=java.util.List.copyOf(rows);viewVersion=version;}return view;}
    public boolean enabled(){return enabled;}
    public long version(){return version;}
    public void enabled(boolean value){if(enabled!=value){enabled=value;version++;}}
    private static final java.util.Map<String,String> LABELS=new LinkedHashMap<>();
    static{
        LABELS.put("exoticFireDomain","异火领域");LABELS.put("fireAura","业火领域");
        LABELS.put("thunderDomain","万雷领域");LABELS.put("tideDomain","潮汐领域");LABELS.put("gravityDomain","重力领域");
        LABELS.put("SpiritEnchantmentLogic","灵纹附魔 / 灵力御体");LABELS.put("XuanlingAnvilMenu","玄灵铁砧加工");
        LABELS.put("XuanlingAnvilBlock","玄灵铁砧自修复");LABELS.put("Alchemy","炼丹");
        LABELS.put("FlyingSword","御剑飞行");LABELS.put("DivineArt","神通");LABELS.put("HeartSword","心剑");
        LABELS.put("ArtRelease","功法释放");LABELS.put("Compass","罗盘");LABELS.put("Tribulation","渡劫");
        LABELS.put("YujianController","御剑飞行");LABELS.put("SwordNurture","飞剑温养");LABELS.put("RealmFlightControl","遁光");
        LABELS.put("ArtPassiveTicker","被动功法");LABELS.put("CrystallizePacket","灵力凝晶");LABELS.put("GuandingItem","灌顶");
        LABELS.put("HuaLingPeiItem","化灵佩");LABELS.put("CaveTokenBlock","洞天令");LABELS.put("Talisman","符箓");
        LABELS.put("Breakthrough","境界突破");LABELS.put("ModCommands","命令调整");LABELS.put("HumanEmperorBanner","人皇幡");
        LABELS.put("CultivationBlock","修炼设施");LABELS.put("InteractionEvents","五行交互");
    }
    public void debit(float amount){
        if(!enabled||!(amount>0)||!Float.isFinite(amount)||net.minecraftforge.fml.util.thread.EffectiveSide.get()!=net.minecraftforge.fml.LogicalSide.SERVER)return;
        String source=WALKER.walk(frames->{String fallback="其他灵力消耗";var it=frames.iterator();while(it.hasNext()){
            var frame=it.next();String type=frame.getClassName(),method=frame.getMethodName();if(!type.startsWith("cn.blockforge")||type.contains("CultivationData")||type.contains("ManaLossLedger"))continue;
            String key=type+"#"+method,label=SOURCE_CACHE.get(key);
            if(label==null){label="";for(var entry:LABELS.entrySet())if(type.contains(entry.getKey())||method.equals(entry.getKey())){label=entry.getValue();break;}SOURCE_CACHE.put(key,label);}
            if(!label.isEmpty())return label;fallback=type.substring(type.lastIndexOf('.')+1)+" / "+method;
        }return fallback;});
        record(source,amount);
    }
    public void record(String source,float amount){
        if(!enabled||!(amount>0)||!Float.isFinite(amount)||net.minecraftforge.fml.util.thread.EffectiveSide.get()!=net.minecraftforge.fml.LogicalSide.SERVER)return;
        var row=new CompoundTag();row.putString("Source",source);row.putFloat("Amount",amount);row.putLong("At",System.currentTimeMillis());rows.addFirst(row);
        while(rows.size()>32)rows.removeLast();
        version++;
    }
    public ListTag save(){var list=new ListTag();for(var row:rows)list.add(row.copy());return list;}
    public void load(ListTag list){rows.clear();for(int i=0;i<Math.min(32,list.size());i++)rows.addLast(list.getCompound(i).copy());version++;}
    public static java.util.List<CompoundTag> read(cn.blockforge.generated.generatedmod.CultivationData data){
        if(data==null)return java.util.List.of();
        if(data instanceof cn.blockforge.generated.generatedmod.CultivationDataImpl impl)return impl.manaLossView();
        var list=data.serializeNBT().getList("ManaLossLedger",Tag.TAG_COMPOUND);var result=new java.util.ArrayList<CompoundTag>();
        for(int i=0;i<list.size();i++)result.add(list.getCompound(i));return result;
    }
    private ManaLossLedger(boolean unused){} public ManaLossLedger(){}
}
