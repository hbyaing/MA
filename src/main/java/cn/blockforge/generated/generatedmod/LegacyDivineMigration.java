package cn.blockforge.generated.generatedmod;

import net.minecraft.nbt.*;

/** Read-only compatibility boundary. Newly written data always uses the current ID. */
public final class LegacyDivineMigration {
    public static final String OLD_ID="guang_yin_suo";
    public static final String ID="ri_yue_ting_lun";
    private static String canonical(String value){return OLD_ID.equals(value)?ID:value;}
    public static CompoundTag migrate(CompoundTag source){return (CompoundTag)visit(source);}
    private static Tag visit(Tag value){
        if(value instanceof StringTag text)return StringTag.valueOf(canonical(text.getAsString()));
        if(value instanceof CompoundTag compound){
            var result=new CompoundTag();
            for(String key:compound.getAllKeys()){
                String name=canonical(key);
                if(!name.equals(key)&&compound.contains(name))continue;
                result.put(name,visit(compound.get(key)));
            }return result;
        }
        if(value instanceof ListTag list){var result=new ListTag();for(Tag entry:list)result.add(visit(entry));return result;}
        return value.copy();
    }
    private LegacyDivineMigration(){}
}
