# 第8项：修炼与保命机制核对

基线915473c，2026-10-21。开发核对完成，客户端操作与真实存档/多人验收仍归第12–15项。本项不改变D28/D29已确认规则。

| 范围 | 源码与既有验收 | 结论 |
|---|---|---|
| D28真实蒲团 | MeditationQte.eligible、QteGameTests | 必须服务端真实打坐座位/原蒲团/位置/境界阶段匹配；创造、旁观、心魔、寄魄、麻痹、满修为不能启动 |
| 三关与奖励 | QteRules、MeditationQte.hitAt | 服务端时钟、nonce与关卡；三关递进，奖励5–20%当前突破需求，上限服从同一修为账户；重放不能重复结算/越过突破 |
| 次数/中断 | MeditationQte UsedStages/事件 | 进入即消耗当前境界小阶段次数，取消/超时/退出不返次；换阶段/座位丢失终止；已用记录保存，临时会话不跨死亡/离线 |
| 走火惩罚 | CultivationDataImpl、QteGameTests | 红区施加18000在线tick减半；唯一正收益边界处理，奇数进位保存，不双重减半、不离线倒计时 |
| D29触发 | SoulPreservation.damage、SoulPreservationGameTests | 元婴及以上，LOWEST最终伤害发生在护盾/护甲之后；非致命不耗保命，致命取消本次伤害并安全遁逃 |
| 特殊死亡 | SoulPreservation.eligible | 强死/虚空/越界高度、渡劫、心境、元神/寄魄排除；真实安全落点只读取已加载地形，不强制生成区块 |
| 冷却与后续击退 | PlayerCrafts账户、SoulPreservation事件 | 世界时钟30000tick=25分钟；同UUID死亡替身/读写保持；脱战临时标记不赋予后续击退免疫 |

现有专项覆盖：QteGameTests 7项、SoulPreservationGameTests 6项，均在基线337项完整回归中通过。本轮不新增镜像测试。第13项汇总后再以最终源码完整回归。

尚待实机：默认K键交互、三关画面、实际死亡/重进/跨维度与多人网络。当前为开发进度快照，不标为全部实机完成。
