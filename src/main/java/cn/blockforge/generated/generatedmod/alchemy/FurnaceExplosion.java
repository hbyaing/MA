package cn.blockforge.generated.generatedmod.alchemy;

import cn.blockforge.generated.generatedmod.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Server-authoritative destructive failure, only called after a valid free-mode ignition. */
public final class FurnaceExplosion {
    public static final float POWER = 8.0F;
    public static boolean detonate(AlchemyFurnaceBlockEntity furnace, Player operator) {
        if (!(furnace.getLevel() instanceof ServerLevel level) || furnace.isRemoved()
                || furnace.brewing() || furnace.hasResult() || !furnace.explosionPending()&&(!furnace.ignited() || !furnace.hasAnyMaterial())) return false;
        var pos = furnace.getBlockPos().immutable();
        furnace.clearExplosionWarning();
        // Consume the submitted batch before onRemove can drop it. Keep fire bottle/core drops native.
        for (int slot=0;slot<AlchemyFurnaceBlockEntity.MATERIAL_SLOT_COUNT;slot++)
            furnace.setItem(slot,net.minecraft.world.item.ItemStack.EMPTY);
        furnace.sync();
        AlchemyFurnaceStructure.dismantle(level,pos,null);
        level.destroyBlock(pos,false,operator);
        // BLOCK interaction destroys terrain independently of the mobGriefing rule.
        level.explode(operator,pos.getX()+.5,pos.getY()+1.5,pos.getZ()+.5,POWER,false,Level.ExplosionInteraction.BLOCK);
        return true;
    }
    private FurnaceExplosion() {}
}
