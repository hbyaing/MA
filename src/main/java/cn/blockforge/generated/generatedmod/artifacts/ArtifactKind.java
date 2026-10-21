package cn.blockforge.generated.generatedmod.artifacts;

import net.minecraft.world.item.ArmorItem;

/** 只登记来源 BlankType.active() 为真的六种；顺序不再复用来源的停用序号。 */
public enum ArtifactKind {
    SWORD("sword",4,0,null),HAMMER("hammer",6,0,null),
    ARMOR("armor",0,6,ArmorItem.Type.CHESTPLATE),LEGGINGS("leggings",0,5,ArmorItem.Type.LEGGINGS),
    BOOTS("boots",0,3,ArmorItem.Type.BOOTS),HELMET("helmet",0,4,ArmorItem.Type.HELMET);
    public final String id;public final float attack,defense;public final ArmorItem.Type armor;
    ArtifactKind(String id,float attack,float defense,ArmorItem.Type armor){this.id=id;this.attack=attack;this.defense=defense;this.armor=armor;}
    public boolean weapon(){return armor==null;}
}
