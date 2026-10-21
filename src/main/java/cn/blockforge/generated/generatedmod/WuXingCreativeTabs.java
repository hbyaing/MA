package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.registry.ModTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** @deprecated 创造模式标签页已迁移到 {@link ModTabs}。 */
@Deprecated
public final class WuXingCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = ModTabs.TABS;
    public static final RegistryObject<CreativeModeTab> CULTIVATION = ModTabs.WUXINGXIANTU_TAB;

    private WuXingCreativeTabs() {}
}
