package cn.blockforge.generated.generatedmod.crafting;

/** Shared compact geometry: server menu slots and client rendering use one contract. */
public final class EquipmentWorkstationLayout {
    public static final int WIDTH=360, INSCRIPTION_HEIGHT=284, REFORGE_HEIGHT=266;
    public static final int EQUIPMENT_X=141,EQUIPMENT_Y=63,BOOK_X=113,BOOK_Y=136;
    public static final int INVENTORY_X=16,STEP=18,HOTBAR_GAP=4;
    public static int inventoryY(boolean inscription){return inscription?195:177;}
    public static String profile(boolean inscription){return inscription?"inscription_compact_v1":"reforge_compact_v1";}
    private EquipmentWorkstationLayout(){}
}
