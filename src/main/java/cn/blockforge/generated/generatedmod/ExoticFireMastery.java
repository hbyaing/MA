package cn.blockforge.generated.generatedmod;

/**
 * 异火掌握状态（为后续异火批次预留的正确数据与门槛概念）：
 *
 * <ul>
 *   <li>{@link #DISCOVERED}——已发现/封存：玩家找到了火种瓶，允许提前持有、收藏、交易；
 *       持有不等于学会，更不等于可用。</li>
 *   <li>{@link #REFINED}——已炼化：通过 ProgressionGate.canRefineFire 的正式炼化检定
 *       （预留 required_realm / required_previous_fire / required_furnace_tier 等特殊条件位）。</li>
 *   <li>{@link #MASTERED}——可正常驭使：战斗加成、炼丹炼器借火、放置驭使都必须再经
 *       ProgressionGate.canUseFire 按使用者本人进度复核；旧存档“已学会”但境界不足时
 *       数据保留，实际按未掌握结算，达标后自动恢复。</li>
 * </ul>
 *
 * 当前版本玩家侧数据仍然只有“已学习火种列表”（CultivationData.getLearnedFires），
 * 本枚举定义状态语义；后续异火药力培养/蜕变批次把每朵异火的成长阶段映射到这三档，
 * 判定入口统一走 ProgressionGate，禁止在物品或方块里另写境界数字。
 */
public enum ExoticFireMastery {
    DISCOVERED,
    REFINED,
    MASTERED;

    /** 火种瓶物品对应的初始状态：拿到瓶子只是“已发现”。 */
    public static ExoticFireMastery ofHeldSeed() {
        return DISCOVERED;
    }
}
