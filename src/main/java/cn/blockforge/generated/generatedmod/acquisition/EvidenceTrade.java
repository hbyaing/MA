package cn.blockforge.generated.generatedmod.acquisition;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.common.BasicItemListing;

/** The trade offer and its audit share the same price/result definition. */
public final class EvidenceTrade implements VillagerTrades.ItemListing {
    private final BasicItemListing listing;
    private final ItemStack input,output;
    public EvidenceTrade(ItemStack input,ItemStack output,int uses,int xp,float multiplier) {
        this.input=input.copy();this.output=output.copy();listing=new BasicItemListing(input,output,uses,xp,multiplier);
    }
    @Override public MerchantOffer getOffer(Entity trader,RandomSource random){return listing.getOffer(trader,random);}
    public void contribute(AcquisitionSourcesEvent event,ResourceLocation profession,int level) {
        event.add(output.getItem(),new SourceEvidence("trade",profession,
                List.of("villager level "+level,"output "+output.getCount()+"; tag="+output.getTag()),
                -1,true,List.of(input.getCount()+"x"+BuiltInRegistries.ITEM.getKey(input.getItem()))));
    }
}
