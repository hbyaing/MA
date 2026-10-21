# 第7项炼器与法宝验证（2026-10-21）

基线7ad6d69（PR #4两项远程CI已通过）。本轮不修改生产玩法/费用/UI，核对证据见REFINING_ARTIFACT_AUDIT.md。

- verifyFull构建成功，337/337必需GameTests通过，耗时2m14s；报告server-refining-artifact-result-20261021.json记录原始日志SHA256。
- 新增3项：实际辅材加工中找零失败逐槽/NBT/灵力无变化，成功扣费及保存候选仅主人单次采用；保存炼器任务中断的带名/耐久/主人/NBT材料与灵力单次退款；六器型待领成品保存保持成长且领取后保存不再出现。
- 静态核对15条炼器配方、6器型配方/模型、15辅材获取配方/模型/名称；清单refining-artifact-resource-audit-20261021.json。来源法宝品级仍硬上限12=极品先天灵宝。
- 继续使用现有工作台主人、命名、保护晶/护纹玉符、操作时序测试；没有删减旧测试或提高警告门槛。

原始日志：工作区work/myriad-refining-artifacts-verifyFull.log。NBT与任务重载为服务端测试，不能替代真实退出重进、多人争用或客户端视觉验收。仍为开发进度快照。
