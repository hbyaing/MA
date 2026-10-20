package cn.blockforge.generated.generatedmod.crafting;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import java.util.*;

/** Equipment-owned results and pity survive ordinary container transfers and logout. */
public final class EquipmentCrafting {
    public static final String KEY="WuxingCraft";
    public static final String[] RUNES={"guard","tough","light","control","charge"};
    public static final String[] NAMES={"灵御","坚韧","轻身","御器","蓄势"};
    public static final String[] TEMPER={"锋锐","坚固","通灵","轻盈"},QUALITY={"普通","上佳","极品","天成"};
    public static final int[] REFINE_CHANCE={100,95,90,80,70,60,45,30,20},PITY={0,2,2,3,3,4,5,6,8};
    public static final List<String> REASONS=List.of("","请放入白名单护甲或灵剑","请先采用或放弃待选胚性","此装备不能刻入该铭纹","铭纹已满阶","铭纹槽已满","每件装备只能有一道主纹","精炼已满阶","锋锐胚性仅供武器","请先在百艺学习炼器师","境界或炼器师等级不足","需要可驾驭的对应品阶异火","背包材料或灵力不足","未知操作","坚固需要有耐久的装备","通灵胚性仅供灵剑","轻盈胚性仅供武器或靴子","此装备已有其他主人");
    private EquipmentCrafting(){}
    public static ItemStack inherit(ItemStack result,List<ItemStack> consumed){
        if(eligible(result)){
            ItemStack source=ItemStack.EMPTY;boolean multiple=false;
            for(var s:consumed)if(eligible(s)&&!read(s).isEmpty()){
                if(!source.isEmpty()){multiple=true;break;}source=s;
            }
            if(!multiple&&!source.isEmpty())result.getOrCreateTag().put(KEY,read(source).copy());
        }
        return EquipmentBond.inheritOwner(result,consumed);
    }
    public static CompoundTag read(ItemStack s){return s.hasTag()?s.getTag().getCompound(KEY):new CompoundTag();}
    public static CompoundTag write(ItemStack s){return s.getOrCreateTagElement(KEY);}
    public static boolean eligible(ItemStack s){
        if(s.getCount()!=1)return false;
        var id=net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(s.getItem());
        return id!=null&&(id.getNamespace().equals("minecraft")||id.getNamespace().equals(GeneratedMod.MOD_ID))
            &&(s.getItem() instanceof ArmorItem||s.getItem() instanceof SwordItem)
            &&!id.getPath().equals("heart_sword");
    }
    public static int level(ItemStack s,String rune){return Math.max(0,Math.min(rune.equals("guard")?4:3,read(s).getCompound("Runes").getCompound(rune).getInt("Level")));}
    public static double potency(ItemStack s,String rune){int q=Math.max(0,Math.min(2,read(s).getCompound("Runes").getCompound(rune).getInt("Quality")));return 1+q*.05;}
    public static int guardLevel(ItemStack s){return Math.max(level(s,"guard"),EnchantmentHelper.getItemEnchantmentLevel(SpiritEnchantment.SPIRIT_GUARD.get(),s));}
    public static int refine(ItemStack s){return Math.max(0,Math.min(9,read(s).getInt("Refine")));}
    public static int capacity(ItemStack s){
        if(s.getItem() instanceof SpiritSwordItem sword)return sword.profile().tier()>=4?3:sword.profile().tier()>=2?2:1;
        var id=net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(s.getItem());
        return id!=null&&id.getNamespace().equals(GeneratedMod.MOD_ID)?s.getRarity()==Rarity.EPIC?3:2:1;
    }
    public static boolean main(String rune){return rune.equals("guard")||rune.equals("control")||rune.equals("charge");}
    public static boolean applies(ItemStack s,String rune){return switch(rune){
        case "guard"->s.getItem() instanceof ArmorItem;
        case "tough"->s.getItem() instanceof ArmorItem;
        case "light"->s.getItem() instanceof ArmorItem a&&a.getType()==ArmorItem.Type.BOOTS;
        case "control","charge"->s.getItem() instanceof SpiritSwordItem;
        default->false;};}
    public static String runeGate(ItemStack s,String rune){
        if(!eligible(s)||!applies(s,rune))return "此装备不能刻入该铭纹";
        int max=rune.equals("guard")?4:3;if(level(s,rune)>=max)return "铭纹已满阶";
        var runes=read(s).getCompound("Runes");
        if(level(s,rune)==0&&runes.size()>=capacity(s))return "铭纹槽已满";
        if(main(rune))for(String key:RUNES)if(!key.equals(rune)&&main(key)&&level(s,key)>0)return "每件装备只能有一道主纹";
        return "";
    }
    public static void setRune(ItemStack s,String rune,int level,int quality){
        var n=write(s);var runes=n.getCompound("Runes");var r=new CompoundTag();r.putInt("Level",Math.max(1,Math.min(rune.equals("guard")?4:3,level)));r.putInt("Quality",Math.max(0,Math.min(2,quality)));runes.put(rune,r);n.put("Runes",runes);
    }
    public static boolean migrate(ItemStack s){
        if(!(s.getItem() instanceof ArmorItem))return false;
        int legacy=EnchantmentHelper.getItemEnchantmentLevel(SpiritEnchantment.SPIRIT_GUARD.get(),s);if(legacy<=0)return false;
        if(legacy>level(s,"guard"))setRune(s,"guard",Math.min(4,legacy),0);
        var ench=new LinkedHashMap<>(EnchantmentHelper.getEnchantments(s));ench.remove(SpiritEnchantment.SPIRIT_GUARD.get());EnchantmentHelper.setEnchantments(ench,s);
        write(s).putInt("Migration",1);return true;
    }
    public static int refineChance(ItemStack s){int target=refine(s)+1;if(target>9)return 0;int fails=read(s).getCompound("RefinePity").getInt("L"+target);return fails>=PITY[target-1]?100:Math.min(100,REFINE_CHANCE[target-1]+Math.max(0,fails)*10);}
    public static int runeChance(ItemStack s,String rune,int rank){int target=level(s,rune)+1;if(target>4)return 0;if(target==1)return 100;int fails=read(s).getCompound("RunePity").getInt(rune+target);return fails>=target?100:Math.min(95,new int[]{100,80,60,40}[target-1]+Math.max(0,Math.min(8,rank)));}
    public static int legacyBookLevel(ItemStack s){return s.is(Items.ENCHANTED_BOOK)?EnchantmentHelper.getEnchantments(s).getOrDefault(SpiritEnchantment.SPIRIT_GUARD.get(),0):0;}
    public static void convertBook(ItemStack s){
        int l=legacyBookLevel(s);if(l<=0)return;var ench=new LinkedHashMap<>(EnchantmentHelper.getEnchantments(s));ench.remove(SpiritEnchantment.SPIRIT_GUARD.get());s.removeTagKey("StoredEnchantments");EnchantmentHelper.setEnchantments(ench,s);write(s).putInt("GuardCredit",Math.max(Math.min(4,l),read(s).getInt("GuardCredit")));
    }
    public static double temper(ItemStack s,int kind){var n=read(s);return n.getInt("Temper")==kind&&n.contains("TemperBp")?Math.max(0,Math.min(10000,n.getInt("TemperBp")))/10000d:0;}
    public static float upkeep(ItemStack s,float cost){double control=new double[]{0,.03,.05,.08}[level(s,"control")]*potency(s,"control");double penalty=s.getItem() instanceof SpiritSwordItem?.04*temper(s,0)+.03*temper(s,3):0;return (float)(cost*Math.max(.75,1-control-.05*temper(s,2)+penalty));}
    public static int maxDamage(ItemStack s,int base){if(base<=0||!s.hasTag()||!s.getTag().contains(KEY)||!eligible(s))return base;return Math.max(1,(int)Math.min(Integer.MAX_VALUE,Math.round(base*(1+refine(s)*.02+.12*temper(s,1)-.08*temper(s,0)-.06*temper(s,3)))));}
    public static int durabilityCost(ItemStack s,int amount,net.minecraft.util.RandomSource random){
        if(amount<=0||!s.hasTag()||!eligible(s))return amount;int level=level(s,"tough");if(level==0)return amount;double save=new double[]{0,.05,.08,.12}[level]*potency(s,"tough");double expected=amount*(1-save);int whole=(int)expected;return whole+(random.nextDouble()<expected-whole?1:0);
    }
    public record Quote(int action,int chance,int stone,int metal,int lapis,int essence,int crystal,int aura,String reason){public boolean ready(){return reason.isEmpty();}}
    public static Quote quote(ServerPlayer p,ItemStack s,int action){
        int l=1,chance=100,stone=0,metal=0,lapis=0,essence=0,crystal=0,aura=0;String reason="";
        if(!eligible(s))reason="请放入白名单护甲或灵剑";
        else if(read(s).contains("Pending"))reason="请先采用或放弃待选胚性";
        if(action>=0&&action<5){String rune=RUNES[action];l=level(s,rune)+1;chance=runeChance(s,rune,PlayerCrafts.rank(p,"refining"));if(reason.isEmpty())reason=runeGate(s,rune);stone=4*l;lapis=4*l;metal=l;crystal=l;}
        else if(action>=20&&action<=22){l=refine(s)+1;chance=refineChance(s);if(l>9)reason="精炼已满阶";stone=l*4;metal=l;crystal=l>=4?l-3:0;if(action==20){stone=(stone*3+1)/2;metal=(metal*3+1)/2;crystal=(crystal*3+1)/2;}if(action==22&&!usesRefineSeal(p,s,action))crystal++;}
        else if(action>=30&&action<=33){l=3;stone=12;metal=4;essence=1;if(action==30&&!(s.getItem() instanceof SwordItem))reason="锋锐胚性仅供武器";if(action==31&&s.getMaxDamage()<=0)reason="坚固需要有耐久的装备";if(action==32&&!(s.getItem() instanceof SpiritSwordItem))reason="通灵胚性仅供灵剑";if(action==33&&!(s.getItem() instanceof SwordItem)&&!(s.getItem() instanceof ArmorItem a&&a.getType()==ArmorItem.Type.BOOTS))reason="轻盈胚性仅供武器或靴子";}
        else reason="未知操作";
        aura=0;var data=GeneratedMod.data(p);int rank=PlayerCrafts.rank(p,"refining");
        if(reason.isEmpty()&&!PlayerCrafts.get(p.server).profession(p.getUUID(),"refining").getBoolean("Learned"))reason="请先在百艺学习炼器师";
        int realm=action<5?Math.max(0,l-1)+(l==4?1:0):l>=7?4:l>=4?2:0;
        int requiredRank=action<5?Math.max(0,l-1):l>=7?3:l>=4?2:0;
        if(reason.isEmpty()&&(data==null||data.wuxingxiantu$getRealm()<realm||rank<requiredRank*2))reason="境界或炼器师品阶不足";
        if(reason.isEmpty()&&action>=20&&(data.wuxingxiantu$getEquippedFire()<(l>=7?5:l>=4?3:1)||!ProgressionGate.canUseFire(p,data.wuxingxiantu$getEquippedFire())))reason="需要可驾驭的对应品阶异火";
        if(reason.isEmpty()&&!p.isCreative()&&(data.wuxingxiantu$getAura()<aura||cn.blockforge.generated.generatedmod.economy.SpiritCurrency.value(p.getInventory().items)<stone||count(p,WuXingItems.DARK_IRON.get())<metal||count(p,Items.LAPIS_LAZULI)<lapis||count(p,WuXingItems.FIVE_ESSENCE.get())<essence||count(p,(action<5?WuXingItems.INSCRIPTION_CRYSTAL.get():WuXingItems.GLAZED_CRYSTAL.get()))<crystal))reason="背包材料或灵力不足";
        if(!EquipmentBond.canUse(p,s))reason="此装备已有其他主人";
        return new Quote(action,chance,stone,metal,lapis,essence,crystal,aura,reason);
    }
    private static int count(ServerPlayer p,Item item){int n=0;for(var s:p.getInventory().items)if(s.is(item))n+=s.getCount();return n;}
    /** Keep the original crystal payment when possible; one jade seal replaces only its protection surcharge. */
    public static boolean usesRefineSeal(ServerPlayer p,ItemStack s,int action){
        if(action!=22)return false;
        int target=refine(s)+1,base=target>=4?target-3:0;
        return count(p,WuXingItems.GLAZED_CRYSTAL.get())<base+1&&count(p,WuXingItems.REFINE_SEAL.get())>0;
    }
    private static void consume(ServerPlayer p,Item item,int count){for(var s:p.getInventory().items)if(s.is(item)){int used=Math.min(count,s.getCount());s.shrink(used);count-=used;if(count==0)break;}}
    public static String attempt(ServerPlayer p,ItemStack s,int action){
        migrate(s);var q=quote(p,s,action);if(!q.ready())return q.reason();
        boolean seal=usesRefineSeal(p,s,action);
        if(!p.isCreative()){if(!cn.blockforge.generated.generatedmod.economy.SpiritCurrency.pay(p.getInventory(),q.stone()))return "灵石不足或背包无法容纳找零";consume(p,WuXingItems.DARK_IRON.get(),q.metal());consume(p,Items.LAPIS_LAZULI,q.lapis());consume(p,WuXingItems.FIVE_ESSENCE.get(),q.essence());consume(p,(action<5?WuXingItems.INSCRIPTION_CRYSTAL.get():WuXingItems.GLAZED_CRYSTAL.get()),q.crystal());if(seal)consume(p,WuXingItems.REFINE_SEAL.get(),1);var d=GeneratedMod.data(p);d.wuxingxiantu$setAura(d.wuxingxiantu$getAura()-q.aura());}
        // Failed exact-change payment must not create an equipment identity or mutate its progress.
        var n=write(s);if(!n.hasUUID("Id"))n.putUUID("Id",UUID.randomUUID());
        var rng=p.getRandom();boolean success=rng.nextInt(100)<q.chance();String result;String work;
        if(action<5){String rune=RUNES[action];int target=level(s,rune)+1;var pity=n.getCompound("RunePity");String key=rune+target;pity.putInt(key,success?0:Math.min(4,pity.getInt(key)+1));n.put("RunePity",pity);
            if(success){int roll=rng.nextInt(100),quality=roll<70?0:roll<95?1:2;quality=Math.max(quality,Math.max(0,Math.min(2,read(s).getCompound("Runes").getCompound(rune).getInt("Quality"))));setRune(s,rune,target,quality);result=NAMES[action]+"刻印成功 · "+QUALITY[quality];}else result="刻印失败，旧铭纹与装备保留";work="rune/"+rune+"/"+target;
        }else if(action<=22){int target=refine(s)+1;var pity=n.getCompound("RefinePity");String key="L"+target;pity.putInt(key,success?0:Math.min(8,pity.getInt(key)+1));n.put("RefinePity",pity);
            if(success){n.putInt("Refine",target);result="精炼成功 +"+target;}else if(action==21&&target>=7&&rng.nextInt(100)<30){n.putInt("Refine",Math.max(0,target-2));result="精炼失败，降一阶；目标保底保留";}else result="精炼失败，装备保留；保底已累积";work="refine/"+target;
        }else{int rare=n.getInt("ReforgeRare"),natural=n.getInt("ReforgeNatural");int roll=rng.nextInt(100),quality=natural>=50?3:rare>=10?(roll<90?2:3):roll<60?0:roll<90?1:roll<99?2:3;
            int[] low={6000,7600,8900,9800},high={7500,8800,9700,10000};var pending=new CompoundTag();pending.putInt("Type",action-30);pending.putInt("Quality",quality);pending.putInt("Bp",low[quality]+rng.nextInt(high[quality]-low[quality]+1));n.put("Pending",pending);n.putInt("ReforgeRare",quality>=2?0:Math.min(10,rare+1));n.putInt("ReforgeNatural",quality==3?0:Math.min(50,natural+1));result="新胚性已生成，请采用或放弃";work="reforge/"+(action-30);success=false;
        }
        if(success)receipt(p,s,work);normalizeDamage(s);p.getInventory().setChanged();GeneratedMod.sync(p);return result;
    }
    public static void receipt(ServerPlayer p,ItemStack s,String key){var n=write(s);var seen=n.getCompound("Works");if(seen.getBoolean(key))return;seen.putBoolean(key,true);n.put("Works",seen);PlayerCrafts.get(p.server).receipt(p.getUUID(),"refining",s,Math.min(4,Math.max(refine(s)/2,level(s,"guard"))));}
    public static boolean choose(ServerPlayer p,ItemStack s,boolean accept){if(!EquipmentBond.canUse(p,s))return false;var n=read(s);if(!n.contains("Pending"))return false;var candidate=n.getCompound("Pending");if(accept){n.putInt("Temper",candidate.getInt("Type"));n.putInt("TemperQuality",candidate.getInt("Quality"));n.putInt("TemperBp",candidate.getInt("Bp"));receipt(p,s,"reforge/"+candidate.getInt("Type"));normalizeDamage(s);}n.remove("Pending");return true;}
    private static void normalizeDamage(ItemStack s){int max=s.getMaxDamage();if(max>0&&s.getDamageValue()>=max)s.setDamageValue(max-1);}
}
