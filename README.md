# 万界仙途 · Myriad Ascension — Forge 1.20.1

基于《五行仙途》0.2.1 已发布源码继续开发。《仙途 · Path of Immortality》的内容迁移正在逐项确认，融合尚未完成。当前名称修改保留 `wuxingxiantu` 注册 ID、资源路径与持久化键。

构建产物为 `build/libs/myriad-ascension-0.2.jar`。安装时用新包替换旧的《五行仙途》包，二者使用同一模组 ID。

合并前历史测试安装成品：`wuxingxiantu-0.2-20261008-project-build.jar`。当时源码完整构建通过，包含 UI 与 JEI 调整及近期灵植素材；发光蓝蘑菇采用单一外观和小蘑菇式扩散。历史记录不作为本次融合的验收结论。

源码当前说明见 [CURRENT_STATUS.md](CURRENT_STATUS.md)，实现入口见 [AI_SOURCE_MAP.md](AI_SOURCE_MAP.md)。历史构建测试结果仅代表对应版本，不能作为当前版本的全量验证结论。

安装时将构建产物放入 Forge 1.20.1 实例的 `mods` 目录，替换旧版模组包。当前源码仓库不包含自动安装脚本。

旧版发布叙述与本次清理前完整说明保存在 `docs/archive/20261008-document-cleanup/`。

Java 17、Forge 47.4.10、GeckoLib 4.8.4。构建：Windows 执行 `gradlew.bat build`，Linux/macOS 执行 `./gradlew build`。成品位于 `build/libs/`。

资源检查：`python tools/check_resources.py`，也已接入 Gradle `check` / `build`。专项图像检查需要 Python 3 与 Pillow（`python -m pip install 'Pillow>=10,<12'`）；Python 不在 PATH 时传入 `-PpythonExecutable=绝对路径`。CI 安装依赖后执行完整验证；编译不能代替客户端、专用服务器与联机测试。

审计记录保存在 `docs/audits/`。历次报告保存在 `docs/archive/`，属于历史记录，不能作为当前验证证据。当前实现入口见 `AI_SOURCE_MAP.md`，验证和未验收事项见 `CURRENT_STATUS.md`。原始模型、美术与设计约束仍在 `reference_assets/` 和 `source-assets/`。




完整验证入口为 `verifyFull`；默认数值报表由 `balanceReport` 输出至 `reports/balance/`，也接入 `check/build`。服务端灵力、司南、渡劫和魂仆设置位于世界 `serverconfig/wuxingxiantu-gameplay-server.toml`。管理员可执行 `/wuxing audit acquisition` 导出当前数据包的获取来源证据；未收录来源不等于无法获得。

丹道需研读“丹道入门”明确解锁。分类丹书传授指定丹方，全书从 PillCatalog 动态读取全部丹方且仅供创造/管理员使用。管理命令：`/wuxing alchemy <玩家> unlock|all|reset|learn <丹方ID>`，要求权限等级 2。

心境异常恢复：`/wuxing mindscape force_restore`，权限等级 2；必须存在有效、未恢复的外界快照。恢复记录仅在操作成功后标记完成。

心境使用 64 个持久化实例槽；离线玩家仍占用租约。首次进入时初始化竞技场，后续复用。
