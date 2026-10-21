package cn.blockforge.generated.generatedmod.artifacts;

/** 保留来源15种辅材的固有差别；成长仍仅使用五行 Refine/铭纹/温养。 */
public enum ArtifactAux {
    XUANTIE("xuantie",Trait.SHARP,2,0),LEIJING("leijing",Trait.THUNDER,.5F,0),
    HUXINYU("huxinyu",Trait.SHIELD,0,2),LONGXUE("longxue",Trait.BLOODTHIRST,1,0),XUANGUIJIA("xuanguijia",Trait.TENACITY,0,6),
    XINGTIE("xingtie",Trait.SHARP,3,3),CHIXIAO("chixiao",Trait.THUNDER,1.2F,2),TAIYIN("taiyin",Trait.SHIELD,0,10),
    YAOYANG("yaoyang",Trait.SHARP,4,0),KONGMING("kongming",Trait.SHIELD,.5F,5),
    ZILEI("zilei",Trait.THUNDER,2,4),QINGMU("qingmu",Trait.TENACITY,0,12),YOUMING("youming",Trait.BLOODTHIRST,2.5F,1),
    LONGWEN("longwen",Trait.TENACITY,1.5F,8),HUNYUAN("hunyuan",Trait.BLOODTHIRST,1,10);
    public enum Trait{SHARP,THUNDER,SHIELD,BLOODTHIRST,TENACITY}
    public final String id;public final Trait trait;public final float attack;public final int durability;
    ArtifactAux(String id,Trait trait,float attack,int durability){this.id=id;this.trait=trait;this.attack=attack;this.durability=durability;}
    public int fire(){return ordinal()<5?1:ordinal()<10?3:5;}
    public static ArtifactAux find(String id){for(var aux:values())if(aux.id.equals(id))return aux;return null;}
}
