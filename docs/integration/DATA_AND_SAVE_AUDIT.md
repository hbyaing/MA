# 底层数据与存档核对（第3项，2026-10-21）

核对基线：86fbfce（PR #1），本轮增补测试在同源功能分支。只采用五行框架，不复制仙途的第二套账户。

| 数据 | 当前唯一权威入口 | 保存边界与本轮结论 |
|---|---|---|
| 境界、修为 | CultivationCapability.INSTANCE → CultivationDataImpl | 能力键wuxingxiantu:cultivation_data；Realm、Cultivation、CultivationStage；读取限制境界0–8/阶段0–3与修为上限 |
| 灵力 | CultivationData.getCurrentMana/setCurrentMana | wuxingxiantu$getAura/setAura是同一余额的兼容别名；写CurrentMana和兼容Aura，读取优先CurrentMana，旧Aura仅兜底；不相加、不另建余额 |
| 神识 | progression/SpiritualSense → PlayerCrafts.account(UUID) | wuxingxiantu_player_crafts世界SavedData内SenseOpen/Sense；境界容量取主能力；运行中ACTIVE只保存会话，不是第二个持久神识账户 |
| 玩家职业 | progression/PlayerCrafts.profession(UUID,id) | alchemy/refining/talisman同一Accounts树；RankVersion=2，旧五级映射九级只迁移一次；村民WuXingProfessions不是玩家职业账户 |
| 装备主人 | crafting/EquipmentBond → HeartSwordItem.owner | 沿用HeartOwner UUID；不新增来源Owner账户；非法UUID拒绝使用；主人在物品NBT中保存 |
| 重生 | event/PlayerLifecycleEvents.clonePlayer | revive原能力、序列化复制、invalidate原能力；非keepInventory普通死亡清理能力装备槽，心境快照等特殊路径按原规则保留 |
| 登录/换维度 | PlayerLifecycleEvents与各子系统事件 | 恢复/同步能力；清理运行中神通、罗盘会话、炼器动作等；尚未逐个系统执行实机退出/重连矩阵 |

本轮新增IntegrationDataGameTests四项：

1. 旧Aura读取、CurrentMana优先、通过兼容API扣费、重复读写不增加余额。
2. 真实ServerPlayer能力失效/重生事件复制修为灵力；普通死亡不把御剑槽复制为额外装备。
3. 世界职业/神识NBT往返、RankVersion不重复转换、不同UUID账户隔离、读取不修改输入快照。
4. 物品NBT往返保留主人、其他玩家拒绝使用、损坏主人字段不被当作无主。

四项均在325项完整服务端测试中通过。它们验证NBT和生命周期处理函数，不能替代真实进程重启、网络客户端或多人同步验收。

## 迁移范围

- 五行旧档：保留wuxingxiantu注册ID、能力键和世界SavedData键；源码包含Aura、神通旧槽、职业RankVersion等明确兼容路径。仅能声明这些路径存在并有针对性测试，不声明所有历代存档已验收。
- 朋友融合旧UI快照：本轮恢复的是相同源码树，无注册命名空间切换；仍需独立存档副本的保存重载实测。
- 原仙途r115旧档（mod_c1f9fd00）：未发现该命名空间的MissingMappings或玩家/世界账户转换器，不支持自动完整迁移的结论。模型注释里的旧路径不是迁移器。
- 运行中效果：剑阵、施法、锁定、探测等不因数据序列化测试通过而视为可跨重启恢复，须在第10/15项核对其明确结束/恢复规则。

下一验收：在独立备份世界分别测试保存退出重入、死亡keepInventory两态、末地返回/跨维度、两个UUID、职业离线结算、装备与背包槽。原仙途旧档转换如需实施，先提出字段/注册ID映射与无法转换项目，请用户确认，不擅自删改未知NBT。
