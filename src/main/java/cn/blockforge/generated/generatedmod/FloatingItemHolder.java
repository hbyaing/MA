package cn.blockforge.generated.generatedmod;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 可悬浮承载物品的基座方块实体统一接口。
 * 灵蕴台与承华台共用同一套悬浮渲染与炼器台素材抽取逻辑。
 * （getLevel() 由 BlockEntity 基类天然实现，这里只是把它纳入接口。）
 */
public interface FloatingItemHolder {
    /** 当前承载的物品（只读语义，调用方修改前请自行 copy）。 */
    ItemStack floatingStack();

    default boolean hasFloating() {
        return !floatingStack().isEmpty();
    }

    Level getLevel();
}
