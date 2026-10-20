package cn.blockforge.generated.generatedmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

/** A small leaf burst within the current growth shape, never a full-block debris cloud. */
public final class SpiritPlantEffects implements IClientBlockExtensions {
    @Override public boolean addDestroyEffects(BlockState state,net.minecraft.world.level.Level level,BlockPos pos,ParticleEngine engine){
        if(!(level instanceof ClientLevel client))return true;
        var bounds=state.getShape(level,pos).bounds();
        for(int i=0;i<12;i++){
            double x=bounds.minX+client.random.nextDouble()*bounds.getXsize();
            double y=bounds.minY+client.random.nextDouble()*bounds.getYsize();
            double z=bounds.minZ+client.random.nextDouble()*bounds.getZsize();
            engine.add(new TerrainParticle(client,pos.getX()+x,pos.getY()+y,pos.getZ()+z,
                    (x-.5)*.35,(y-bounds.getYsize()*.5)*.3,(z-.5)*.35,state,pos).setPower(.35F).scale(.45F));
        }
        return true;
    }
    @Override public boolean addHitEffects(BlockState state,net.minecraft.world.level.Level level,net.minecraft.world.phys.HitResult target,ParticleEngine engine){
        if(!(target instanceof BlockHitResult hit))return true;
        var client=Minecraft.getInstance().level;if(client==null)return true;
        var p=hit.getLocation();var pos=hit.getBlockPos();
        engine.add(new TerrainParticle(client,p.x,p.y,p.z,0,.015,0,state,pos).setPower(.2F).scale(.35F));
        return true;
    }
}
