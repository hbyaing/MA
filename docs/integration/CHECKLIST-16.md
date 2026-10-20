# 16项融合工作清单（2026-10-21）

本表是当前执行入口，保留用户的顺序。以五行仙途为框架，仙途r115填充内容；当前为开发进度快照。代码存在、自动测试通过、客户端验收是三个独立状态。下列文件均在当前源码树实际存在；文件存在不等于功能已完整验收。

源码前缀：`src/main/java/cn/blockforge/generated/generatedmod/`；测试前缀：`src/gametest/java/cn/blockforge/generated/generatedmod/`。

| 顺序 | 工作项目 | 实际源码/测试入口 | 状态与剩余验收 |
|---|---|---|---|
| 1 | 整理融合源码与分支 | RECOVERY.md、verify_recovery_bundle.py | 已完成恢复验证；原恢复包缺失父提交问题保留，老UI/新UI/早期融合分支独立保留；PR #1尚未合并 |
| 2 | 统一融合进度表 | 本表、accepted-decisions.json、SOURCE_CONTENT_COVERAGE.md | 本轮建立16项执行映射；D01–D30是已确认决策，历史“已接入”不当作全部验收 |
| 3 | 统一底层数据与存档 | CultivationDataImpl.java、event/PlayerLifecycleEvents.java、progression/PlayerCrafts.java、crafting/EquipmentBond.java；IntegrationDataGameTests.java | 已核对唯一数据入口及兼容边界，新增4项测试通过；真实磁盘重载/旧存档仍归第15项，见DATA_AND_SAVE_AUDIT.md |
| 4 | 统一灵石与经济 | economy/SpiritCurrency.java、SpiritTradePayments.java、crafting/EquipmentCrafting.java、formation/SwordArrayUpkeep.java；MyriadFoundationGameTests.java、SpiritTradeCurrencyGameTests.java | 四档与原子找零已接入；主要扣费/兑换/交易入口已核对，见ECONOMY_AUDIT.md；新增兑换与混合付款测试，实机/完整退款仍待验收；剑阵5极品等值结算是已确认规则 |
| 5 | 完善灵脉与草药 | world/SpiritVeinFeature.java、SpiritVeinData.java、plants/ImportedHerbs.java、HerbCultivationData.java、divinesurvey/DivineSurvey.java；MyriadFoundationGameTests.java、VeinCompassGameTests.java | 18来源草药资源链已逐项核对；已修复新矿脉跨入旧邻区块的写入边界，新增4项回归通过；自然分布/原档边界实测待第15项，见HERBS_VEINS_AUDIT.md |
| 6 | 完善炼丹融合 | alchemy/FormulaKnowledge.java、alchemy/auxiliary/AuxiliaryAlchemy.java、network/service/AlchemyService.java；AuxiliaryAlchemyGameTests.java、AlchemyMaterialTransferGameTests.java | D30已接入且本轮自动测试覆盖；副药交互、取消/退款的实机矩阵待做，丹阶/成色/传承逐项专项核对待做 |
| 7 | 完善炼器与法宝 | crafting/RefiningOperation.java、EquipmentBond.java、artifacts/ArtifactAux.java、ArtifactInfusion.java；EquipmentCraftingGameTests.java、BoundEquipmentCraftingGameTests.java、ArtifactNamingGameTests.java | 既有流程已接入；辅材/认主/托管完整生命周期待专项核对与实机 |
| 8 | 完善修炼与保命机制 | meditationqte/MeditationQte.java、soulpreservation/SoulPreservation.java；meditationqte/QteGameTests.java、SoulPreservationGameTests.java | D28–D29自动测试通过；K键小游戏、特殊死亡及冷却实机未验收 |
| 9 | 完善术法融合 | arts/SustainedSwordArts.java、ArtCatalog.java；SustainedSwordArtsGameTests.java | 原功法槽/学习/耗能已接入；与原有技能定位及参数、客户端取消/换维度待专项验收 |
| 10 | 完善诛仙剑阵 | formation/SwordArrayServer.java、SwordArrayUpkeep.java、formation/client/SwordArrayClient.java；formation/SwordArrayGameTests.java | 已有固定阵位/动画/清理/等值收费；动画与预览对齐需实机；实际防御/伤害仍是另立设计任务 |
| 11 | 完善储物袋与内容物品 | storage/StorageBagItem.java、BagMagnetRules.java、importedcontent/SpiritBathBlockEntity.java、LaoyangDollBlock.java；StorageBagGameTests.java、ImportedCreaturesGameTests.java、ImportedContentGameTests.java | 已接入内容和测试存在；获取/用途/死亡/离线/容器嵌套等生命周期仍需专项验收 |
| 12 | 统一老UI与说明 | knowledge/KnowledgeItems.java、client/HerbProcessingTableScreen.java、client/EquipmentWorkstationScreen.java；EntryUiGameTests.java、KnowledgeIntegrationGameTests.java | 老UI交付、新UI暂存；历史截图非本次验收；按钮/说明/JEI需实机 |
| 13 | 完整服务端回归 | verifyFull、tools/verify_gametest_log.py | 基线321项通过，数据经济轮327项通过；本轮第5项新增4项后331项通过；先前清洁构建325项/警告119通过，最新远程CI待确认 |
| 14 | 客户端与光影验收 | ACCEPTANCE.md | 尚未执行本轮无/有光影模型、护罩、遮挡与渲染状态恢复验收 |
| 15 | 自然生成、存档与多人验收 | DATA_AND_SAVE_AUDIT.md、ACCEPTANCE.md | 尚未执行真实自然生成、磁盘重载、旧档、多人全矩阵；NBT往返不等于此项通过 |
| 16 | 正式打包与发布 | build.gradle、jar-audit-20261021.json | 基线QA排除检查通过；只有恢复快照，无正式完成包；后续改动后重新核验客户端/服务端版本 |

执行方式：第3–12项各自保留“源码核对、自动测试、实机、待决重叠”证据，不把第13项全绿反填为所有功能完成。第14–15项作为集中实机验收。发现未确认重复项先询问用户，不自行取舍。

本轮没有新增融合取舍。旧仙途命名空间迁移未实现，应先取得独立备份旧档与明确字段映射，再作为迁移任务实施；不得直接用融合包打开唯一原档。
