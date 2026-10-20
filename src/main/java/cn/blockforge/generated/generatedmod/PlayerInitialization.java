package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraft.util.RandomSource;

public final class PlayerInitialization {
    public static void initializeIfNeeded(CultivationData data, RandomSource random) {
        if (data.wuxingxiantu$isInitialized()) return;
        // Preserve roots from older saves that already contain cultivation progress.
        if (data.wuxingxiantu$getRootMask() == 0) {
            int count=cn.blockforge.generated.generatedmod.balance.RootBalance.drawCount(random);
            int[] roots={0,1,2,3,4};int mask=0;
            for(int i=0;i<5;i++)data.wuxingxiantu$setRootQuality(i,0);
            for(int i=0;i<count;i++) {
                int picked=i+random.nextInt(5-i),root=roots[picked];roots[picked]=roots[i];roots[i]=root;
                mask|=1<<root;
                data.wuxingxiantu$setRootQuality(root,cn.blockforge.generated.generatedmod.balance.RootBalance.drawQuality(random));
            }
            data.wuxingxiantu$setRootMask(mask);
            data.wuxingxiantu$setPhysique(PhysiqueType.values()[random.nextInt(7)]);
            data.wuxingxiantu$setAura(CultivationLogic.refreshMaxMana(data));
        }
        data.wuxingxiantu$setInitialized(true);
    }

    private PlayerInitialization() {}
}
