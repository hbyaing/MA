# 融合工作进度（2026-10-21）

执行入口：[用户确认的16项清单](CHECKLIST-16.md)。第3项见[数据与存档](DATA_AND_SAVE_AUDIT.md)，第4项见[经济核对](ECONOMY_AUDIT.md)，第5项见[灵脉草药核对](HERBS_VEINS_AUDIT.md)，第6项见[炼丹核对](ALCHEMY_AUDIT.md)。

定位：开发进度快照。原则：五行仙途主框架，仙途内容补充；未决重复项先询问，已确认货币等值结算只更新说明。

| 阶段 | 当前状态 | 证据/边界 |
|---|---|---|
| 固定代码基线 | 已记录 | 原058a4f0和三个源码树见acceptance-baseline.json |
| 修复恢复流程 | 基线独立clone与fsck已通过 | recovery-baseline-result.json；原包问题记录和附件保留 |
| 协作分支/进度表 | 恢复分支已推送，PR #1待合并；本轮核对另立功能分支 | 老UI、新UI和早期整合独立保留，不覆盖远程main |
| 完整服务端测试 | 基线321/321、数据经济327/327、灵脉草药331/331、炼丹334/334通过 | 基线见server-result-20261021.json；远程CI因130/119警告失败，已改测试API并清洁构建验证119/119，数据经济见server-data-economy-result-20261021.json；灵脉草药见server-herbs-veins-result-20261021.json；最新炼丹见server-alchemy-result-20261021.json |
| 无/有光影客户端 | 尚未执行 | 与服务端依赖分开，禁止捕获真实鼠标 |
| 自然生成与存档重载 | 尚未执行 | 不以注册存在或服务端测试代替实机证据 |
| 正式发布 | 未进行 | 不将当前快照改称正式完成版 |

恢复、构建、游戏测试与实机图像验收分别记录。日志完成校验由tools/verify_gametest_log.py执行；即使Gradle退出0，缺少本次GAME TESTS COMPLETE和必需测试通过标志也不能通过验收。
