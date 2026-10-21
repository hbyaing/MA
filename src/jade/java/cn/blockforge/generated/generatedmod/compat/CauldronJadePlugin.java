package cn.blockforge.generated.generatedmod.compat;

import cn.blockforge.generated.generatedmod.AlchemyFurnaceBlock;
import cn.blockforge.generated.generatedmod.AlchemyFurnaceBlockEntity;
import cn.blockforge.generated.generatedmod.AlchemyFurnaceStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

/** Optional Jade integration. Only the HUD accessor is redirected; no world blocks are changed. */
@WailaPlugin
public final class CauldronJadePlugin implements IWailaPlugin {
    @Override public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(FurnaceInfo.INSTANCE,AlchemyFurnaceBlockEntity.class);
    }
    @Override public void registerClient(IWailaClientRegistration registration) {
        registration.addRayTraceCallback((hit,accessor,original)-> {
            if (!(accessor instanceof BlockAccessor block)) return accessor;
            return redirect(block,registration::blockAccessor);
        });
        registration.registerBlockComponent(FurnaceInfo.INSTANCE,AlchemyFurnaceBlock.class);
    }

    public static BlockAccessor redirect(BlockAccessor block,java.util.function.Supplier<BlockAccessor.Builder> builders) {
            BlockPos core=AlchemyFurnaceStructure.findCore(block.getLevel(),block.getPosition());
            if (core==null || !(block.getLevel().getBlockEntity(core) instanceof AlchemyFurnaceBlockEntity furnace)) return block;
            var state=block.getLevel().getBlockState(core);
            if (!AlchemyFurnaceBlock.isFormed(state)) return block;
            ItemStack icon=new ItemStack(state.getBlock());
            icon.setHoverName(Component.translatable("jade.wuxingxiantu.cauldron"));
            BlockHitResult coreHit=new BlockHitResult(block.getHitResult().getLocation(),block.getSide(),core,block.getHitResult().isInside());
            return builders.get().from(block).hit(coreHit).blockState(state)
                    .blockEntity(furnace).fakeBlock(icon).build();
    }

    public enum FurnaceInfo implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        INSTANCE;
        @Override public ResourceLocation getUid() { return new ResourceLocation("wuxingxiantu","cauldron"); }
        @Override public void appendServerData(CompoundTag data,BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof AlchemyFurnaceBlockEntity core)
                    || !AlchemyFurnaceBlock.isFormed(core.getBlockState())) return;
            data.putBoolean("CauldronFormed",true);
            data.putInt("CauldronTier",core.tier());
            data.putBoolean("CauldronBrewing",core.brewing());
            long duration=Math.max(1,core.finishTick()-core.startTick());
            long elapsed=Math.max(0,accessor.getLevel().getGameTime()-core.startTick());
            data.putInt("CauldronProgress",(int)Math.min(100,elapsed*100/duration));
        }
        @Override public void appendTooltip(ITooltip tooltip,BlockAccessor accessor,IPluginConfig config) {
            if (!AlchemyFurnaceBlock.isFormed(accessor.getBlockState())) return;
            CompoundTag data=accessor.getServerData();
            if (!data.getBoolean("CauldronFormed")) return;
            tooltip.add(AlchemyFurnaceBlock.tierName(data.getInt("CauldronTier")));
            tooltip.add(Component.translatable(data.getBoolean("CauldronBrewing")
                    ? "jade.wuxingxiantu.cauldron_brewing" : "jade.wuxingxiantu.cauldron_idle"));
            if (data.getBoolean("CauldronBrewing")) tooltip.add(Component.translatable(
                    "gui.wuxingxiantu.alchemy.progress_percent",data.getInt("CauldronProgress")));
        }
    }
}
