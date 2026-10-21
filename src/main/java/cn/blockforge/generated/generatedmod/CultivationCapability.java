package cn.blockforge.generated.generatedmod;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.INBTSerializable;
import javax.annotation.Nullable;

public final class CultivationCapability {
    public static final Capability<CultivationData> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});
    /** 兼容现有调用点；新代码统一使用 INSTANCE。 */
    public static final Capability<CultivationData> DATA = INSTANCE;

    private CultivationCapability() {}

    public static final class Provider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        private final CultivationDataImpl data = new CultivationDataImpl();
        private final LazyOptional<CultivationData> optional = LazyOptional.of(() -> data);
        @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
            return capability == INSTANCE ? optional.cast() : LazyOptional.empty();
        }
        @Override public CompoundTag serializeNBT() { return data.serializeNBT(); }
        @Override public void deserializeNBT(CompoundTag tag) { data.deserializeNBT(tag); }
        public void invalidate() { optional.invalidate(); }
    }
}
