# 五行仙途退役准备（2026-10-24）

当前状态：必要差异资料已归档至万界仙途，旧目录仍保留，尚未执行删除或移入垃圾目录。这里是历史资料，不代表已合入玩法，也不代表最新整体完成验收。

目标基线：Myriad-Ascension 的 origin/main `bb5a2bb`。来源：`E:/五行仙途`，分支 `codex/xuanshui-white-rebuild-20261020`，HEAD `0288f13`。来源工作区有 UI 布局配置修改及未跟踪资料；原工作区未修改。

## 已迁入的资料

`authoring.zip` 保存 578 个缺失或不同的制作文件，包括制作脚本、三个工作台源文件、参考素材、模型源文件及玄水妖白模重建资料。`history.zip` 保存 1,499 个历史源码、配置及设计文档差异文件，包括未提交的 UI 布局配置。两个包均逐文件校验 SHA-256，并通过 ZIP CRC 校验。

`preservation.json` 给出每个包及归档文件的哈希、原相对路径和分类。`inventory.json` 记录 43,730 个非缓存文件：9,213 个相同、32,804 个缺失、1,713 个不同，共 33,705,187,083 字节。缺失不等于应当成为当前运行资源。`.git`、Gradle/build 缓存和 Python 缓存不在这个文件清单内。

归档保留原相对路径；如需查看，解压到独立目录。禁止直接覆盖当前项目。旧的 `src` 有 1,380 个文件与当前实现不同；旧 assets/reference_assets/source-assets 亦有差异。按项目融合约定，任何启用旧实现、替换当前资源的动作，都必须先比较双方并确认取舍。这里只保全证据，不作功能取舍。

历史工具可能含旧目录、旧 Java/Python 环境及测试实例路径，未列入当前构建入口。三个工作台仅保存文件，未启动验收：AGENTS.md 指定的 `C:/Users/33140/Documents/Codex/Workbenches/启动全部工作台.py` 在本机不存在，不能绕过总入口启动。

## 本地恢复资料

`E:/备份/2026-10-24/五行仙途退役准备/` 保存：

- `wuxing-all-refs.bundle`：旧仓库全部 refs 的历史；已在 `restore-check` 实际独立克隆，`git fsck --full --no-dangling` 退出码为 0。
- `uncommitted.patch`、`wuxingxiantu-ui-layout.json`、`git-status.txt`：未提交配置和文件状态。
- `codex-local/`：旧本地 Codex 配置，仅本地保留，不上传共享仓库。

bundle 不包含未跟踪文件、被忽略文件和悬空 Git 对象。旧 `.git` 有悬空对象，原目录仍保留这些对象，不以 bundle 冒充完整目录备份。历史交付包约 23.95 GB、报告截图约 8.62 GB、exports/output、第三方 JEI jar 和其他生成文件仍在旧目录；没有证据足以将其全部永久删除。

## 删除前的剩余事项

1. 已推送共享分支 `codex/migrate-wuxing-resources-20261024`，从 GitHub 独立克隆到本地 `cloud-restore-check`，fsck 通过，两个归档包及全部 2,077 个归档文件哈希核对通过。尚未合入 main。
2. 恢复工作台总入口，确认实际工具链与 Blockbench 等外部程序没有继续依赖旧路径。当前项目运行配置/工具未发现旧绝对路径依赖，外部程序范围尚未完成验证。
3. Codex 中旧项目仍注册，三个玄水妖聊天仍关联旧目录。仅移动聊天的项目归属不能证明其工作目录已迁移；本轮没有修改聊天。
4. 真正退役时，再确认没有程序/聊天使用旧目录，把完整 `E:/五行仙途` 移到唯一的新目录 `E:/垃圾/日期/五行仙途退役/五行仙途`，保留原 `.git`、历史交付物和未跟踪文件；核对路径并验证移动结果。不要永久删除，也不要删除 GitHub 上游仓库。

本次请求是“准备删除”，因此本轮不执行整个旧目录的移动或删除。

## 验证

万界仙途 `gradlew.bat build --no-daemon --console=plain -PpythonExecutable=C:/Users/Nostzephert/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe` 通过，包含资源检查、专用检查、数值回归和 GameTest 编译。默认 PATH Python 缺少 Pillow，应用自带 Python 通过。归档不加入正式 Mod 包；客户端光影、自然生成、存档重载不属于本轮归档验证结论。

在固定提交 `5eb17fc` 上另行执行完整 `runGameTestServer`：339 个必测全部通过，Gradle 退出码 0。日志：本地 `reports/legacy-migration-gametest.log`。旧历史恢复克隆中执行 `git apply --check uncommitted.patch` 也通过。远程归档可用 `python tools/verify_legacy_archive.py <克隆目录>/integration/legacy-wuxing-20261024` 重新验证。
